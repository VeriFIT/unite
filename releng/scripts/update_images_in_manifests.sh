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

# This script is responsible for updating the Kubernetes manifest files with the new image tags.

# Iterate over each deployment entry in the JSON file
jq -c '.[]' "$DEPLOYMENT_FILE" | while IFS= read -r deployment; do
    image=$(jq -r '.dockerfiles[-1].image // empty' <<< "$deployment")
    resolved_image=$(bash -lc "echo \"${image}\"")
    deployment_path="$(jq -r '.kubernetes_manifests.deployment // empty' <<< "$deployment")"

    if [ -z "$resolved_image" ] || [ -z "$deployment_path" ]; then
        echo "Skipping deployment entry due to missing image or deployment path. Exiting."
        exit 1
    fi

    if [ -f "releng/$deployment_path" ]; then
        deployment_file="releng/$deployment_path"
    else
        echo "Deployment file not found for image=$resolved_image (path=$deployment_path)"
        exit 1
    fi

    NEW_IMAGE=$resolved_image
    export NEW_IMAGE
    echo "Updating $deployment_file with image: $NEW_IMAGE"

    yq -i '(.spec.template.spec.containers[] | select(has("image")) | .image) = env(NEW_IMAGE)' "$deployment_file"

    grep -n "image:" "$deployment_file" || true
done