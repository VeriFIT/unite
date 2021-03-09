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
USAGE="   Usage: $0 [-t|-h|-l|-ci]
      -t ... tools - Runs an additional testsuite which requires some analysis
                         tools to be installed (i.e. will not work on all machines)
      -l ... live - Only runs the test suites without launching the adapter. Expects
                    the adapter to be running already.
      -ci ... gitlab CI - Used when running this script in gitlab CI. Will not kill
              subprocesses at the end.
      -h ... help
"

USRPATH=$PWD                        # get the call directory
ROOTDIR=$(dirname $(realpath $0))   # get the script directory
cd $ROOTDIR                         # move to the script directory
cd ..
ADAPTER_ROOT_DIR=$PWD               # get the adapter root directory
cd $ROOTDIR                         # move back to the script directory

print_help() {
    echo "$HELP"
    echo "$USAGE"
    exit 0
}

# $1 ... name of the invalid arg
invalid_arg() {
    echo -e "\n   Invalid argument: $1\n"
    echo "$USAGE"
    exit 1
}


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


# polls an address using curl until the request returns zero (i.e. the address responds)
# patams: address - URL for curl to poll
curl_poll()
{
    curl_ret=42
    while [ $curl_ret != 0 ]
    do
        sleep 3
        echo -n "."
        curl $1 &> /dev/null
        curl_ret=$?
    done
    echo
}


# Check that all required configuration files exist.
# Outputs an error message and exits the script if 
# a conf file is missing.
checkConfFiles()
{
    if [ ! -f "$ADAPTER_ROOT_DIR/analysis/conf/VeriFitAnalysis.properties" ]; then
        echo -e "ERROR: Configuration file \"$ADAPTER_ROOT_DIR/analysis/conf/VeriFitAnalysis.properties\" not found."
        echo -e "  The adapter needs to be configured to be able to run!"
        echo -e "  See the \"VeriFitAnalysisExample.properties\" file for instructions and use it as a template."
        exit 1
    fi
    if [ ! -f "$ADAPTER_ROOT_DIR/compilation/conf/VeriFitCompilation.properties" ]; then
        echo -e "ERROR: Configuration file \"$ADAPTER_ROOT_DIR/compilation/conf/VeriFitCompilation.properties\" not found."
        echo -e "  The adapter needs to be configured to be able to run!"
        echo -e "  See the \"VeriFitCompilationExample.properties\" file for instructions and use it as a template."
        exit 1
    fi
    if [ ! -f "$ADAPTER_ROOT_DIR/sparql_triplestore/start.ini" ]; then
        echo -e "ERROR: Configuration file \"$ADAPTER_ROOT_DIR/sparql_triplestore/start.ini\" not found."
        echo -e "  The adapter needs to be configured to be able to run!"
        echo -e "  See the \"startExample.ini\" file for instructions and use it as a template."
        exit 1
    fi
}

main() {
    # process arguments
    unset testedToolsFlag liveAdapterFlag gitlabCI
    for arg in "$@"
    do
        case $arg in
            -t) testedToolsFlag=true ; shift ;;
            -l) liveAdapterFlag=true ; shift ;;
            -ci) gitlabCI=true ; shift ;;
            -h) print_help ; shift ;;
            *) invalid_arg "$arg" ;;
        esac
    done


    echo "\nNOTE:\n  MAKE SURE THAT THE ADAPTER IS RUNNING ON THE DEFAULT PORTS!\n  THE TESTSUITE CURRENTLY HAS PORTS HARDCODED INSIDE IT\n" # TODO


    # lookup analysis adapter config
    analysis_host=$(cat $ADAPTER_ROOT_DIR/analysis/conf/VeriFitAnalysis.properties | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
    analysis_port=$(cat $ADAPTER_ROOT_DIR/analysis/conf/VeriFitAnalysis.properties | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")
    analysis_url="$analysis_host:$analysis_port/analysis/"


    if [ -z $liveAdapterFlag ]; then
        echo "Booting up the Universal Analysis Adapter"
        $ADAPTER_ROOT_DIR/run_all.sh &>/dev/null &
        curl_poll "$analysis_url"       # poll the analysis adapter because that one starts last in the run script
        echo -e "Adapter up and running\n"
    else
        echo "Skipping Adapter boot. Adapter expected to be running already." 
        
        # make sure configuration files exist
        checkConfFiles
    fi


    compilationRes=0
    analysisRes=0
    analysisToolsRes=0
    
    echo
    echo "Running Compilation adapter test suite" 
    time newman run $ADAPTER_ROOT_DIR/compilation/tests/TestSuite.postman_collection
    compilationRes=$?


    echo
    echo "Running Analysis adapter test suite" 
    time newman run $ADAPTER_ROOT_DIR/analysis/tests/TestSuite.postman_collection
    analysisRes=$?

    echo
    if [ -n "$testedToolsFlag" ]; then
        echo "Running Analysis adapter Tested Tools test suite" 
        time newman run $ADAPTER_ROOT_DIR/analysis/tests/TestSuite_TestedTools.postman_collection
        analysisToolsRes=$?
    else
        echo "Skipping Analysis adapter Tested Tools test suite" 
    fi

    if [ -z $gitlabCI ]; then
        echo -e "\nShutting down the adaters" 
        trap '' INT TERM     # ignore INT and TERM while shutting down
        kill -TERM 0
        wait
        echo "All done."
    fi

    # return non-zero if there were failed tests
    if [ "$compilationRes" -ne 0 ] || [ "$analysisRes" -ne 0 ] || [ "$analysisToolsRes" -ne 0 ]; then
        echo -e "\n\n  TESTS FAILED\n"
        exit 1
    else
        echo -e "\n\n  TESTS PASSED\n"
        exit 0
    fi
}

main "$@"