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

HELP="
   Runs the run_all script and then Postman testsuites for the adapter using Newman.
"
USAGE="   Usage: $0 [-h|-t|-n|-l|-ci]
      -t ... tools - Runs an additional testsuite which requires some analysis
                         tools to be installed (i.e. will not work on all machines)
      -n ... keep last N - Only runs keep_last_N test suites for both adapters. Does NOT
                           run any other test suites. Requires the adapter to be configured
                           to have keep_last_n enabled with a value of 10.
      -l ... live - Only runs the test suites without launching the adapter. Expects
                    the adapter to be running already.
      -ci ... gitlab CI - Used when running this script in gitlab CI. Will not kill
              subprocesses at the end.
      -h ... help
"

# duration for polling (via curl)
SLEEP=3

USRPATH="$PWD"                          # get the call directory
ROOTDIR="$(dirname "$(realpath "$0")")" # get the script directory
cd "$ROOTDIR"                           # move to the script directory

cd ..
ADAPTER_ROOT_DIR="$PWD"                 # get the adapter root directory
cd "$ROOTDIR"                           # move back to the script directory

# source shared utils
source "$ADAPTER_ROOT_DIR/dev_tools/shared.sh"


# catch ctrl+c and kill all subprocesses
trap 'killall' INT
killall() {
    trap '' INT TERM     # ignore INT and TERM while shutting down
    echo -e "\nAborting..."
    kill -TERM 0
    wait
    echo "All done."
    exit 0
}

main() {
    # process arguments
    unset testedToolsFlag liveAdapterFlag gitlabCI keepLastNflag
    for arg in "$@"
    do
        case "$arg" in
            -t) testedToolsFlag=true ; shift ;;
            -l) liveAdapterFlag=true ; shift ;;
            -ci) gitlabCI=true ; shift ;;
            -n) keepLastNflag=true ; shift ;;
            -h) print_help "$HELP" "$USAGE"; shift ;;
            *) invalid_arg "$arg" "$USAGE" ;;
        esac
    done


    echo -e "\nNOTE:\n  MAKE SURE THAT THE ADAPTER IS RUNNING ON THE DEFAULT PORTS!\n  THE TESTSUITE CURRENTLY HAS PORTS HARDCODED INSIDE IT\n" # TODO


    # lookup analysis adapter config
    analysis_url="$(lookupAnalysisURL "$ADAPTER_ROOT_DIR")"

    if [ -z "$liveAdapterFlag" ]; then
        echo "Booting up the Universal Analysis Adapter"
        "$ADAPTER_ROOT_DIR/run_all.sh" &>/dev/null &
        curl_poll "$analysis_url" "$SLEEP" 0 # poll the analysis adapter because that one starts last in the run script
        echo -e "Adapter up and running\n"
    else
        echo "Skipping Adapter boot. Adapter expected to be running already." 
        
        # make sure configuration files exist
        checkConfFiles "$ADAPTER_ROOT_DIR"
    fi


    compilationRes=0
    compilationKeepLastNRes=0
    analysisRes=0
    analysisToolsRes=0
    analysisKeepLastNRes=0
    
    if [ -z "$keepLastNflag" ]; then
        echo
        echo "Running Compilation adapter test suite" 
        time newman run "$ADAPTER_ROOT_DIR/compilation/tests/TestSuite.postman_collection"
        compilationRes="$?"

        echo
        echo "Running Analysis adapter test suite" 
        time newman run "$ADAPTER_ROOT_DIR/analysis/tests/TestSuite.postman_collection"
        analysisRes="$?"

        echo
        if [ -n "$testedToolsFlag" ]; then
            echo "Running Analysis adapter Tested Tools test suite" 
            time newman run "$ADAPTER_ROOT_DIR/analysis/tests/TestSuite_TestedTools.postman_collection"
            analysisToolsRes="$?"
        else
            echo "Skipping Analysis adapter Tested Tools test suite" 
        fi

        echo
        echo "Skipping keep_last_n test suites." 

    else
        echo
        echo "Skipping all test suites except the keep_last_n test suites" 

        echo ""
        echo "Running Compilation adapter keep_last_n test suite" 
        time newman run "$ADAPTER_ROOT_DIR/compilation/tests/TestSuite_KeepLastN.postman_collection"
        compilationKeepLastNRes="$?"

        echo
        echo "Running Analysis adapter keep_last_n test suite" 
        time newman run "$ADAPTER_ROOT_DIR/analysis/tests/TestSuite_KeepLastN.postman_collection"
        analysisKeepLastNRes="$?"
    fi

    if [ -z "$gitlabCI" ]; then
        echo -e "\nShutting down the adaters" 
        trap '' INT TERM     # ignore INT and TERM while shutting down
        kill -TERM 0
        wait
        echo "All done."
    fi

    # return non-zero if there were failed tests
    if [ "$compilationRes" -ne 0 ] || [ "$analysisRes" -ne 0 ] || [ "$analysisToolsRes" -ne 0 ] || [ "$compilationKeepLastNRes" -ne 0 ] || [ "$analysisKeepLastNRes" -ne 0 ]; then
        echo -e "\n\n  TESTS FAILED\n"
        exit 1
    else
        echo -e "\n\n  TESTS PASSED\n"
        exit 0
    fi
}

main "$@"