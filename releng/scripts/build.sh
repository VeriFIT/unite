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

# This script is responsible for building the Docker images and pushing them to the container registry.
deployment_count="$(jq 'length' "${DEPLOYMENT_FILE}")"

# Check if there are any deployments defined in the JSON file
if [[ "${deployment_count}" -eq 0 ]]; then
	echo "No deployments found in ${DEPLOYMENT_FILE}." >&2
	exit 1
fi

while IFS= read -r deployment; do
	app_name="$(jq -r '.app_name // "unknown-app"' <<<"${deployment}")"
	echo "Processing image for ${app_name}"

	entries_found=0
	while IFS= read -r dockerfile_entry; do
		entries_found=1
		dockerfile_path="$(jq -r '.dockerfile_path // empty' <<<"${dockerfile_entry}")"
		image=$(jq -r '.image // empty' <<<"${dockerfile_entry}")
		resolved_image=$(bash -lc "echo \"${image}\"")
		building_path="$(jq -r '.building_path // "."' <<<"${dockerfile_entry}")"

		if [[ -z "${dockerfile_path}" || -z "${image}" ]]; then
			echo "Invalid dockerfiles entry for ${app_name}: missing dockerfile_path or image." >&2
			exit 1
		fi

		build_args=()
		while IFS= read -r build_arg; do
			if [[ -n "${build_arg}" ]]; then
				build_args+=("--build-arg" "${build_arg}")
			fi
		done < <(jq -r '.build_arguments[]? // empty' <<<"${dockerfile_entry}")

		# Build from repository root so app paths in Dockerfiles are valid.
		buildah bud "${build_args[@]}" -t $resolved_image -f "${dockerfile_path}" "${building_path}"
	done < <(jq -c '.dockerfiles[]?' <<<"${deployment}")

	if [[ "${entries_found}" -eq 0 ]]; then
		echo "No dockerfiles entries found for ${app_name}." >&2
		exit 1
	fi

	image="$(jq -r '.dockerfiles[-1].image // empty' <<<"${deployment}")"
	if [[ -z "${image}" ]]; then
		echo "No image found in dockerfiles for ${app_name}" >&2
		exit 1
	fi

	# Push the built image to the container registry.
	buildah push $resolved_image
done < <(jq -c '.[]' "${DEPLOYMENT_FILE}")
