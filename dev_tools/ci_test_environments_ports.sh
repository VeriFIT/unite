#!/bin/bash

##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################

USRPATH="$PWD"                          # get the call directory
ROOTDIR="$(dirname "$(realpath "$0")")" # get the script directory
cd "$ROOTDIR"                           # move to the script directory
cd ..
ADAPTER_ROOT_DIR="$PWD"                 # get the adapter root directory
cd "$ROOTDIR"                           # move back to the script directory


main () {
    # build 
    echo -e "Building\n"
    "$ADAPTER_ROOT_DIR/build.sh" &>/dev/null
    ret="$?"
    if [ "$ret" -ne 0 ]; then
        failed "$ret"
    fi
    echo -e "done\n"

    echo -e "Setting environment variables UNITE_ANALYSIS_PORT=9090, UNITE_COMPILATION_PORT=9091"
    export UNITE_ANALYSIS_PORT=9090
    export UNITE_COMPILATION_PORT=9091

    echo -e "Changing ports inside of the Newman test suite\n"
    sed -i 's|http://localhost:8080|http://localhost:9080|' "$ADAPTER_ROOT_DIR/analysis/tests/TestSuite.postman_collection"
    sed -i 's|http://localhost:8081|http://localhost:9081|' "$ADAPTER_ROOT_DIR/analysis/tests/TestSuite.postman_collection"
    sed -i 's|http://localhost:8081|http://localhost:9081|' "$ADAPTER_ROOT_DIR/compilation/tests/TestSuite.postman_collection"

    # run tests
    echo -e "Running test script\n"
    "$ADAPTER_ROOT_DIR/dev_tools/test.sh" -ci
    exit "$?"
}

main "$@"