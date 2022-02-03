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

# variables used to control which log tailing terminals
# can be killed during a failed startup
# (i.e. their corresponding component started succesfully)
unset KILL_TRIPLESTORE_TAIL KILL_COMPILATION_TAIL KILL_ANALYSIS_TAIL
killTailTerminals(){
    if [ -n "$KILL_ANALYSIS_TAIL" ] || [ -n $1 ]; then
        kill "$(ps -ef | grep "tail -f .*/logs/../logs/../logs/analysis_" | head -1 | awk '{ print $2 }')" &> /dev/null     # funny path /logs/../logs/../logs/ to avoid killing unwanted tail commands
    fi
    if [ -n "$KILL_COMPILATION_TAIL" ] || [ -n $1 ]; then
        kill "$(ps -ef | grep "tail -f .*/logs/../logs/../logs/compilation_" | head -1 | awk '{ print $2 }')" &> /dev/null  # TODO use PIDs instead ($!)
    fi
    if [ -n "$KILL_TRIPLESTORE_TAIL" ] || [ -n $1 ]; then
        kill "$(ps -ef | grep "tail -f .*/logs/../logs/../logs/triplestore_" | head -1 | awk '{ print $2 }')" &> /dev/null
    fi
}

# catch ctrl+c and kill all subprocesses
trap 'killall' INT
killall() {
    trap '' INT TERM     # ignore INT and TERM while shutting down
    echo -e "\nShutting down..."
    kill -TERM 0
    if [ -n "$ARG_TAIL" ]; then
        killTailTerminals true
    fi
    wait
    echo "All done."

    exit 0
}

# used by the script in case of failure
abortAll() {
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

    # firt check that the required utilities are available
    if ! type "curl" &> /dev/null; then
        echo -e "\nERORR: The '${RED}curl${NC}' utility is required by Unite but is ${RED}not available${NC}.\n"
        exit "$?"
    fi
    if ! type "mvn" &> /dev/null; then
        echo -e "\nERORR: The '${RED}mvn${NC}' utility is required by Unite but is ${RED}not available${NC}.\n"
        exit "$?"
    fi

    # build Unite first if requested by args
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
    echo -e "    ${BOLD}Unite${NORMAL} ${VERSION}"
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

    # rotate log files (delete all but last 5)
    cd "$ROOTDIR/logs"
    for log in `ls | grep triplestore_.*\.log | head -n -5` ; do rm $log ; done
    for log in `ls | grep compilation_.*\.log | head -n -5` ; do rm $log ; done
    for log in `ls | grep analysis_.*\.log | head -n -5` ; do rm $log ; done
    cd -

    ############################################################################################################
    #  Triplestore
    ############################################################################################################

    # open new terminal that tails the log file and record its PID to kill later
    if [ -n "$ARG_TAIL" ]; then
        gnome-terminal --title="tail: Triplestore Log (feel free to close this window)" -- /bin/bash -c "tail -f \"$ROOTDIR/logs/../logs/../logs/triplestore_$CURTIME.log\"" # funny path /logs/../logs/../logs/ to avoid killing unwanted tail commands 
    fi

    # start the triplestore
    echo "Starting the Triplestore"
    "$ROOTDIR/sparql_triplestore/run.sh" &> "$ROOTDIR/logs/triplestore_$CURTIME.log" &
    PROCESS_PID=$!
    echo -e "Waiting for the Triplestore to finish startup"
    waitForUrlOnline "$triplestore_url" "$PROCESS_PID" "$SLEEP" 0
    ret="$?"
    if [ "$ret" -eq 0 ]; then
        echo -e "Triplestore ${GREEN}running${NC} (1/3)\n"
        KILL_TRIPLESTORE_TAIL=true
    else
        echo -e "Triplestore ${RED}failed${NC} to start!\n"
        if [ -n "$ARG_TAIL" ]; then
            echo -e "See the \"tail: Triplestore Log\" terminal window for the startup log."
            echo -e "Or try checking the logs: \"$ROOTDIR/logs/triplestore_$CURTIME.log\"\n"
        else
            echo -e "Try checking the logs: \"$ROOTDIR/logs/triplestore_$CURTIME.log\""
            echo -e "Or run again with '-t' to see the logs during startup.\n"
        fi
        abortAll # exit
    fi


    ############################################################################################################
    #  Compilation
    ############################################################################################################

    # open new terminal that tails the log file and record its PID to kill later
    if [ -n "$ARG_TAIL" ]; then
        gnome-terminal --title="tail: Compilation Log (feel free to close this window)" -- /bin/bash -c "tail -f \"$ROOTDIR/logs/../logs/../logs/compilation_$CURTIME.log\"" # TODO use PIDs instead ($!)
    fi

    # start the compilation adapter
    echo "Starting the Compilation adapter"
    cd "$ROOTDIR/compilation"
    mvn jetty:run-exploded &> "$ROOTDIR/logs/compilation_$CURTIME.log" &
    PROCESS_PID=$!
    echo $PROCESS_PID
    cd - > /dev/null
    echo -e "Waiting for the Compilation adapter to finish startup"
    waitForUrlOnline "$compilation_url" "$PROCESS_PID" "$SLEEP" 0
    ret="$?"
    if [ "$ret" -eq 0 ]; then
        echo -e "Compilation adapter ${GREEN}running${NC} (2/3)\n"
        KILL_COMPILATION_TAIL=true
    else
        echo -e "Compilation adapter ${RED}failed${NC} to start!\n"
        if [ -n "$ARG_TAIL" ]; then
            echo -e "See the \"tail: Compilation Log\" terminal window for the startup log."
            echo -e "Or try checking the logs: \"$ROOTDIR/logs/compilation_$CURTIME.log\"\n"
        else
            echo -e "Try checking the logs: \"$ROOTDIR/logs/compilation_$CURTIME.log\""
            echo -e "Or run again with '-t' to see the logs during startup.\n"
        fi
        abortAll # exit
    fi


    ############################################################################################################
    #  Analysis
    ############################################################################################################

    # open new terminal that tails the log file and record its PID to kill later
    if [ -n "$ARG_TAIL" ]; then
        gnome-terminal --title="tail: Analysis Log (feel free to close this window)" -- /bin/bash -c "tail -f \"$ROOTDIR/logs/../logs/../logs/analysis_$CURTIME.log\""
    fi

    # start the analysis adapter
    echo "Starting the Analysis adapter"
    cd "$ROOTDIR/analysis"
    mvn jetty:run-exploded &> "$ROOTDIR/logs/analysis_$CURTIME.log" &
    PROCESS_PID=$!
    cd - > /dev/null
    echo -e "Waiting for the Analysis adapter to finish startup"
    waitForUrlOnline "$analysis_url" "$PROCESS_PID" "$SLEEP" 0
    ret="$?"
    if [ "$ret" -eq 0 ]; then
        echo -e "Analysis adapter ${GREEN}running${NC} (3/3)\n"
        KILL_ANALYSIS_TAIL=true
    else
        echo -e "Analysis adapter ${RED}failed${NC} to start!\n"
        if [ -n "$ARG_TAIL" ]; then
            echo -e "See the \"tail: Analysis Log\" terminal window for the startup log."
            echo -e "Or try checking the logs: \"$ROOTDIR/logs/analysis_$CURTIME.log\"\n"
        else
            echo -e "Try checking the logs: \"$ROOTDIR/logs/analysis_$CURTIME.log\""
            echo -e "Or run again with '-t' to see the logs during startup.\n"
        fi
        abortAll # exit
    fi

    echo -e "${GREEN}Ready to go!${NC}"
    echo "Use ctrl+c to exit..."
    cat # wait forever
}

main "$@"