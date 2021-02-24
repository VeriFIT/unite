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

SLEEP=1

HELP="
   Launches the sparql triplestore, the analysis adapter and
   the compilation adapter. The triplestore needs to finish
   startup before both adapters, which is controled by polling
   the triplestore using curl until it responds.
"
USAGE="   Usage: $0 [-t|-h]
      -t ... \"tail\" - Opens tail -f for each output log in new gnome-terminals.
      -h ... help
"

USRPATH=$PWD                        # get the call directory
ROOTDIR=$(dirname $(realpath $0))   # get the script directory
cd $ROOTDIR                         # move to the script directory



killTailTerminals(){
    kill $(ps -ef | grep "tail -f .*/logs/../logs/../logs/analysis_" | head -1 | awk '{ print $2 }') &> /dev/null   # funny path /logs/../logs/../logs/ to avoid killing unwanted tail commands 
    kill $(ps -ef | grep "tail -f .*/logs/../logs/../logs/compilation_" | head -1 | awk '{ print $2 }') &> /dev/null
    kill $(ps -ef | grep "tail -f .*/logs/../logs/../logs/triplestore_" | head -1 | awk '{ print $2 }') &> /dev/null
}

# catch ctrl+c and kill all subprocesses
trap 'killall' INT
killall() {
    trap '' INT TERM     # ignore INT and TERM while shutting down
    echo -e "\nShutting down..."
    kill -TERM 0
    killTailTerminals
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
    if [ ! -f "./analysis/VeriFitAnalysis.properties" ]; then
        echo -e "ERROR: Configuration file \"$ROOTDIR/analysis/VeriFitAnalysis.properties\" not found."
        echo -e "  The adapter needs to be configured to be able to run!"
        echo -e "  See the \"VeriFitAnalysisExample.properties\" file for instructions and use it as a template."
        exit 1
    fi
    if [ ! -f "./compilation/VeriFitCompilation.properties" ]; then
        echo -e "ERROR: Configuration file \"$ROOTDIR/compilation/VeriFitCompilation.properties\" not found."
        echo -e "  The adapter needs to be configured to be able to run!"
        echo -e "  See the \"VeriFitCompilationExample.properties\" file for instructions and use it as a template."
        exit 1
    fi
    if [ ! -f "./sparql_triplestore/jetty-distribution/start.ini" ]; then
        echo -e "ERROR: Configuration file \"$ROOTDIR/sparql_triplestore/jetty-distribution/start.ini\" not found."
        echo -e "  The adapter needs to be configured to be able to run!"
        echo -e "  See the \"startExample.ini\" file for instructions and use it as a template."
        exit 1
    fi
}

main () {
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
    checkConfFiles

    # get and output version
    VERSION=$(cat ./VERSION.md 2>/dev/null)
    echo -e "\n########################################################"
    echo -e "    OSLC Universal Analysis, $VERSION"
    echo -e "########################################################\n"


    # lookup triplestore config
    triplestore_host=$(cat sparql_triplestore/jetty-distribution/start.ini | grep "^ *jetty.http.host=" | sed "s/^ *jetty.http.host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
    triplestore_port=$(cat sparql_triplestore/jetty-distribution/start.ini | grep "^ *jetty.http.port=" | sed "s/^ *jetty.http.port=//")
    triplestore_url="$triplestore_host:$triplestore_port/fuseki/"
    # lookup compilation adapter config
    compilation_host=$(cat compilation/VeriFitCompilation.properties | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
    compilation_port=$(cat compilation/VeriFitCompilation.properties | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")
    compilation_url="$compilation_host:$compilation_port/compilation/"
    # lookup analysis adapter config
    analysis_host=$(cat analysis/VeriFitAnalysis.properties | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
    analysis_port=$(cat analysis/VeriFitAnalysis.properties | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")
    analysis_url="$analysis_host:$analysis_port/analysis/"


    # create log files and append headings
    mkdir $USRPATH/logs &> /dev/null
    CURTIME=$(date +%F_%T)
    echo -e "########################################################\n    Running version: $VERSION\n    Started at: $CURTIME\n########################################################\n" > "$USRPATH/logs/triplestore_$CURTIME.log"
    echo -e "########################################################\n    Running version: $VERSION\n    Started at: $CURTIME\n########################################################\n" > "$USRPATH/logs/compilation_$CURTIME.log"
    echo -e "########################################################\n    Running version: $VERSION\n    Started at: $CURTIME\n########################################################\n" > "$USRPATH/logs/analysis_$CURTIME.log"

    # open new terminals that tail the log files and record their PIDs to kill later
    if [ "$1" = "-t" ]; then
        gnome-terminal --title="tail: Triplestore Log" -- /bin/bash -c "tail -f $USRPATH/logs/../logs/../logs/triplestore_$CURTIME.log" # funny path /logs/../logs/../logs/ to avoid killing unwanted tail commands 
        gnome-terminal --title="tail: Compilation Log" -- /bin/bash -c "tail -f $USRPATH/logs/../logs/../logs/compilation_$CURTIME.log"
        gnome-terminal --title="tail: Analysis Log" -- /bin/bash -c "tail -f $USRPATH/logs/../logs/../logs/analysis_$CURTIME.log"
    fi

    # start the triplestore
    echo "Starting the Triplestore"
    cd sparql_triplestore
    ./run.sh &> "$USRPATH/logs/triplestore_$CURTIME.log" &
    echo "Waiting for the Triplestore to finish startup"
    curl_poll "$triplestore_url"
    echo -e "Triplestore running\n"

    # start the compilation adapter
    echo "Starting the Compilation adapter"
    cd ../compilation
    mvn jetty:run-exploded &> "$USRPATH/logs/compilation_$CURTIME.log" &
    echo "Waiting for the Compilation adapter to finish startup"
    curl_poll "$compilation_url"
    echo -e "Compilation adapter running\n"

    # start the analysis adapter
    echo "Starting the Analysis adapter"
    cd ../analysis
    mvn jetty:run-exploded &> "$USRPATH/logs/analysis_$CURTIME.log" &
    echo "Waiting for the Analysis adapter to finish startup"
    curl_poll "$analysis_url"
    echo -e "Analysis adapter running\n"

    echo "Ready to go!"
    echo "Use ctrl+c to exit..."
    cat # wait forever
}

main "$@"