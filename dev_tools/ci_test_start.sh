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

# duration for polling (via curl)
SLEEP=3

USRPATH=$PWD                        # get the call directory
ROOTDIR=$(dirname $(realpath $0))   # get the script directory
cd $ROOTDIR                         # move to the script directory
cd ..
ADAPTER_ROOT_DIR=$PWD               # get the adapter root directory
cd $ROOTDIR                         # move back to the script directory

# source shared utils
source $ADAPTER_ROOT_DIR/dev_tools/shared.sh

main () {
    # build 
    echo -e "Building\n"
    $ADAPTER_ROOT_DIR/build.sh &>/dev/null

    # lookup config URLs
    triplestore_url=$(lookupTriplestoreURL "$ADAPTER_ROOT_DIR")
    compilation_url=$(lookupCompilationURL "$ADAPTER_ROOT_DIR")
    analysis_url=$(lookupAnalysisURL "$ADAPTER_ROOT_DIR")

    echo -e "Booting\n"
    $ADAPTER_ROOT_DIR/run_all.sh &>/dev/null &

    echo "triplestore"
    curl_poll "$triplestore_url" "$SLEEP" 40 # 40 * 3 = 120 seconds
    ret=$?
    if [ $ret -ne 0 ]; then
        tail $ADAPTER_ROOT_DIR/logs/triplestore*
        failed $ret
    fi
    echo -e "running\n"

    echo "compilation"
    curl_poll "$compilation_url" "$SLEEP" 40
    ret=$?
    if [ $ret -ne 0 ]; then
        tail $ADAPTER_ROOT_DIR/logs/compilation*
        failed $ret
    fi
    echo -e "running\n"

    echo "analysis"
    curl_poll "$analysis_url" "$SLEEP" 40
    ret=$?
    if [ $ret -ne 0 ]; then
        tail $ADAPTER_ROOT_DIR/logs/analysis*
        failed $ret
    fi
    echo -e "running\n"

    echo "Success!"

    exit 0
}

main "$@"