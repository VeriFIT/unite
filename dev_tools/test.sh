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

# Launch the triplestore
# Then install, lanuch and test the adapter

HELP="
   Runs the run_all script and then Postman testsuites for the adapter using Newman.
"
USAGE="   Usage: $0 [-a]
      -t ... \"tools\" - Runs an additional testsuite which requires some analysis
                         tools to be installed (i.e. will not work on all machines)
      -h ... help
"

USRPATH=$PWD                        # get the call directory
ROOTDIR=$(dirname $(realpath $0))   # get the script directory
cd $ROOTDIR                         # move to the script directory


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


main() {
    # process arguments
    if [ "$#" -eq 0 ]; then
        : # all good
    elif [ "$#" -eq 1 ]; then
        if [ "$1" = "-h" ]; then
            echo "$HELP"
            echo "$USAGE"
            exit 0
        elif [ "$1" != "-t" ]; then
            echo -e "Invalid arguments\n"
            echo "$USAGE"
            exit 1
        fi
    else
        echo -e "Invalid arguments\n"
        echo "$USAGE"
        exit 1
    fi

    # make sure configuration files exist
    if [ ! -f "../analysis/VeriFitAnalysis.properties" ]; then
        echo -e "ERROR: Configuration file \"$ROOTDIR/../analysis/VeriFitAnalysis.properties\" not found."
        echo -e "  The adapter needs to be configured to be able to run!"
        echo -e "  See the \"VeriFitAnalysisExample.properties\" file for instructions and use it as a template."
        exit 1
    fi
    if [ ! -f "../compilation/VeriFitCompilation.properties" ]; then
        echo -e "ERROR: Configuration file \"$ROOTDIR/../compilation/VeriFitCompilation.properties\" not found."
        echo -e "  The adapter needs to be configured to be able to run!"
        echo -e "  See the \"VeriFitCompilationExample.properties\" file for instructions and use it as a template."
        exit 1
    fi


    # lookup analysis adapter config
    analysis_host=$(cat ../analysis/VeriFitAnalysis.properties | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
    analysis_port=$(cat ../analysis/VeriFitAnalysis.properties | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")
    analysis_url="$analysis_host:$analysis_port/analysis/"



    echo "Booting up the Universal Analysis Adapter"
    ../run_all.sh &>$USRPATH/logs/run_all.log &
    curl_poll "$analysis_url"       # poll the analysis adapter because that one starts last in the run script
    echo -e "Adapter up and running\n"


    compilationRes=0
    analysisRes=0
    analysisToolsRes=0
    
    echo
    echo "Running Compilation adapter test suite" 
    time newman run ../compilation/tests/TestSuite.postman_collection
    compilationRes=$?


    echo
    echo "Running Analysis adapter test suite" 
    time newman run ../analysis/tests/TestSuite.postman_collection
    analysisRes=$?

    if [ "$1" = "-t" ]; then
        echo
        echo "Running Analysis adapter Tested Tools test suite" 
        time newman run ../analysis/tests/TestSuite_TestedTools.postman_collection
    fi
    analysisToolsRes=$?

    echo -e "\nShutting down the adaters" 
    trap '' INT TERM     # ignore INT and TERM while shutting down
    kill -TERM 0
    wait
    echo "All done."

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