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
   Launches the sparql triplestore, the analysis adapter and
   the compilation adapter. The triplestore needs to finish
   startup before both adapters, which is controled by polling
   the triplestore using curl until it responds.

   Also reloads all configuration files.
"
USAGE="   Usage: $0 [-t|-h|-b]
      -t ... tail - Opens tail -f for each output log in new gnome-terminals.
      -b ... build - Runs the build script first.
      -h ... help
"

# duration for polling (via curl)
SLEEP=3

USRPATH="$PWD"                          # get the call directory
ROOTDIR="$(dirname "$(realpath "$0")")" # get the script directory

# source shared utils
source "$ROOTDIR/dev_tools/shared.sh"

killTailTerminals(){
    kill "$(ps -ef | grep "tail -f .*/logs/../logs/../logs/analysis_" | head -1 | awk '{ print $2 }')" &> /dev/null     # funny path /logs/../logs/../logs/ to avoid killing unwanted tail commands 
    kill "$(ps -ef | grep "tail -f .*/logs/../logs/../logs/compilation_" | head -1 | awk '{ print $2 }')" &> /dev/null  # TODO use PIDs instead ($!)
    kill "$(ps -ef | grep "tail -f .*/logs/../logs/../logs/triplestore_" | head -1 | awk '{ print $2 }')" &> /dev/null
}

# catch ctrl+c and kill all subprocesses
trap 'killall' INT
killall() {
    trap '' INT TERM     # ignore INT and TERM while shutting down
    echo -e "\nShutting down..."
    kill -TERM 0
    if [ -n "$ARG_TAIL" ]; then
        killTailTerminals
    fi
    wait
    echo "All done."

    exit 0
}

main () {
    # process arguments
    unset ARG_TAIL ARG_BUILD
    for arg in "$@"
    do
        case "$arg" in
            -t) ARG_TAIL=true ; shift ;;
            -b) ARG_BUILD=true ; shift ;;
            -h) print_help "$HELP" "$USAGE"; shift ;;
            *) invalid_arg "$arg" "$USAGE" ;;
        esac
    done

    # build first if requested by args
    if [ -n "$ARG_BUILD" ]; then
        echo -e "\nRunning build.sh first"
        "$ROOTDIR/build.sh"
        if [ "$?" -ne 0 ]; then
            echo -e "\nBuild failed. Aborting start.\n"
            exit "$?"
        fi
    else # just update conf
        processConfFiles "$ROOTDIR"
    fi

    # get and output version
    VERSION="$(cat "$ROOTDIR/VERSION.md" 2>/dev/null)"
    echo -e "\n########################################################"
    echo -e "    ${BOLD}Unite${NORMAL}, ${VERSION}"
    echo -e "########################################################\n"

    # lookup config URLs
    triplestore_url="$(lookupTriplestoreURL "$ROOTDIR")"
    compilation_url="$(lookupCompilationURL "$ROOTDIR")"
    analysis_url="$(lookupAnalysisURL "$ROOTDIR")"

    # create log files and append headings
    mkdir "$ROOTDIR/logs" &> /dev/null
    CURTIME="$(date +%F_%T)"
    echo -e "########################################################\n    Running version: $VERSION\n    Started at: $CURTIME\n########################################################\n" > "$ROOTDIR/logs/triplestore_$CURTIME.log"
    echo -e "########################################################\n    Running version: $VERSION\n    Started at: $CURTIME\n########################################################\n" > "$ROOTDIR/logs/compilation_$CURTIME.log"
    echo -e "########################################################\n    Running version: $VERSION\n    Started at: $CURTIME\n########################################################\n" > "$ROOTDIR/logs/analysis_$CURTIME.log"

    # open new terminals that tail the log files and record their PIDs to kill later
    if [ -n "$ARG_TAIL" ]; then
        gnome-terminal --title="tail: Triplestore Log (feel free to close this window)" -- /bin/bash -c "tail -f \"$ROOTDIR/logs/../logs/../logs/triplestore_$CURTIME.log\"" # funny path /logs/../logs/../logs/ to avoid killing unwanted tail commands 
        gnome-terminal --title="tail: Compilation Log (feel free to close this window)" -- /bin/bash -c "tail -f \"$ROOTDIR/logs/../logs/../logs/compilation_$CURTIME.log\"" # TODO use PIDs instead ($!)
        gnome-terminal --title="tail: Analysis Log (feel free to close this window)" -- /bin/bash -c "tail -f \"$ROOTDIR/logs/../logs/../logs/analysis_$CURTIME.log\""
    fi

    # start the triplestore
    echo "Starting the Triplestore"
    "$ROOTDIR/sparql_triplestore/run.sh" &> "$ROOTDIR/logs/triplestore_$CURTIME.log" &
    echo -e "Waiting for the Triplestore to finish startup"
    echo -e "(If it takes too long, try checking the logs)"         # TODO rework to check if PID is still going to be able to detect failed start instead of wating for ever
    curl_poll "$triplestore_url" "$SLEEP" 0
    echo -e "Triplestore ${GREEN}running${NC} (1/3)\n"

    # start the compilation adapter
    echo "Starting the Compilation adapter"
    cd "$ROOTDIR/compilation"
    mvn jetty:run-exploded &> "$ROOTDIR/logs/compilation_$CURTIME.log" &
    cd - > /dev/null
    echo -e "Waiting for the Compilation adapter to finish startup"
    echo -e "(If it takes too long, try checking the logs)"
    curl_poll "$compilation_url" "$SLEEP" 0
    echo -e "Compilation adapter ${GREEN}running${NC} (2/3)\n"

    # start the analysis adapter
    echo "Starting the Analysis adapter"
    cd "$ROOTDIR/analysis"
    mvn jetty:run-exploded &> "$ROOTDIR/logs/analysis_$CURTIME.log" &
    cd - > /dev/null
    echo -e "Waiting for the Analysis adapter to finish startup"
    echo -e "(If it takes too long, try checking the logs)"
    curl_poll "$analysis_url" "$SLEEP" 0
    echo -e "Analysis adapter ${GREEN}running${NC} (3/3)\n"

    echo -e "${GREEN}Ready to go!${NC}"
    echo "Use ctrl+c to exit..."
    cat # wait forever
}

main "$@"