#!/bin/bash
#
# Copyright (C) 2026 Jan Fiedor <fiedorjan@centrum.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Viktoriia Martynovych <xmartyn@fi.muni.cz> (initial implementation)
#   Jan Fiedor <fiedorjan@centrum.cz>
#

# This script is responsible for triggering the downstream deployment pipeline with the necessary variables.

# Build the variables JSON for the API request
VARS='['
VARS="$VARS{\"key\":\"TRIGGER_AUTO_DEPLOY\",\"value\":\"true\"}"
seq=1

manifest_paths=$(jq -r '.[] | .kubernetes_manifests.volumes[]?, .kubernetes_manifests.deployment, .kubernetes_manifests.service  | select(. != null and . != "")' "${DEPLOYMENT_FILE}")

for manifest_path in $manifest_paths; do
    if [ -f "$manifest_path" ]; then
        file="$manifest_path"
    elif [ -f "releng/$manifest_path" ]; then
        file="releng/$manifest_path"
    else
        echo "Manifest not found: $manifest_path"
        continue
    fi

    file_name=$(basename "${file}")
    file_key="a${seq}_${file_name%.yaml}"
    file_value=$(base64 < "$file" | tr -d '\n')
    VARS="$VARS,{\"key\":\"${file_key}\",\"value\":\"$file_value\"}"
    seq=$((seq + 1))
done
VARS="$VARS]"
VARIABLES_JSON="{\"variables\":$VARS}"

# Trigger deployment pipeline
RESPONSE=$(curl --silent --show-error --request POST \
--header "PRIVATE-TOKEN: $VERIFIT_KUBCTL_API" \
--header "Content-Type: application/json" \
--data "$VARIABLES_JSON" \
--url "$CI_API_V4_URL/projects/$DOWNSTREAM_PROJECT_ID/pipeline?ref=master")

DOWNSTREAM_PIPELINE_ID=$(echo "$RESPONSE" | jq -r '.id')
export DOWNSTREAM_PIPELINE_ID
echo "Triggered downstream pipeline with ID: ${DOWNSTREAM_PIPELINE_ID}"

if [ "$DOWNSTREAM_PIPELINE_ID" = "null" ] || [ -z "$DOWNSTREAM_PIPELINE_ID" ]; then
    echo "Failed to trigger pipeline! API Response:"
    echo "$RESPONSE" | jq -r '.message'
    exit 1
fi

echo "DOWNSTREAM_PIPELINE_ID=$DOWNSTREAM_PIPELINE_ID" > downstream.env
