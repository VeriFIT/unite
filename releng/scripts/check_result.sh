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

# This script is responsible for polling the downstream deployment pipeline and fetching the deployment results.

# Poll deployment pipeline
STATUS="running"
while [ "$STATUS" == "running" ] || [ "$STATUS" == "pending" ] || [ "$STATUS" == "created" ]; do
    # Sleep for a while before checking the status again to avoid hitting the API too much
    sleep 10

    STATUS_JSON=$(curl --silent \
        --header "PRIVATE-TOKEN: $VERIFIT_KUBCTL_API" \
        "$CI_API_V4_URL/projects/$DOWNSTREAM_PROJECT_ID/pipelines/$DOWNSTREAM_PIPELINE_ID")
        
    STATUS=$(echo "$STATUS_JSON" | jq -r '.status')
done
echo "Deployment pipeline finished with status: $STATUS"

# Fetch deployment results
JOBS=$(
    curl --silent \
        --header "PRIVATE-TOKEN: $VERIFIT_KUBCTL_API" \
        --url "$CI_API_V4_URL/projects/$DOWNSTREAM_PROJECT_ID/pipelines/$DOWNSTREAM_PIPELINE_ID/jobs"
)

# Extract the job ID of the deploy job
JOB_ID=$(
    echo "$JOBS" | jq -r '.[] | select(.stage == "deploy") | .id' | head -n 1
)

# Fetch the deployment report artifact
curl --silent --fail --output output.txt \
--header "PRIVATE-TOKEN: $VERIFIT_KUBCTL_API" \
"$CI_API_V4_URL/projects/$DOWNSTREAM_PROJECT_ID/jobs/$JOB_ID/artifacts/report.txt"

echo "-------  DEPLOYMENT SUMMARY  --------"
echo " "

if [ -s output.txt ]; then
    cat output.txt
else
    echo "No details available. Some unexpected error occurred. Please try triggering the pipeline again."
fi

echo " "
echo "---------  END OF SUMMARY  ----------"

if [ "$STATUS" == "success" ]; then
    exit 0
else 
    exit 1
fi
