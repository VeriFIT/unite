#!/bin/bash

HELP=" Launches the sparql triplestore, and then the analysis adapter and the compilation adapter.
 The triplestore needs to finish startup before both adapters, which is controled by giving
 the triplestore a headstart. Duration of the headstart in seconds is controlled using an
 optional argument (default 3).

 Usage: run_all.sh [triplestore_sleep_seconds]"

# process arguments
SLEEP=3
if [ "$#" -eq 0 ]; then
    echo "Using default 3s sleep for triplestore to startup"
elif [ "$#" -eq 1 ]; then
    if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        echo "$HELP"
        exit 0
    elif [ "$1" -eq "$1" ] &> /dev/null; then # if isnumber 
        SLEEP="$1"
    else
        echo -e " Argument has to be a number\n Usage: run_all.sh [triplestore_sleep_seconds]"
        exit 1
    fi
else
    echo -e " Invalid arguments\n Usage: run_all.sh [triplestore_sleep_seconds]"
    exit 1
fi


USRPATH=$PWD # get the call directory
cd "${BASH_SOURCE%/*}" # move to the script directory

# catch ctrl+c and kill all subprocesses
trap 'killall' INT
killall() {
    trap '' INT TERM     # ignore INT and TERM while shutting down
    echo "Shutting down..."
    kill -TERM 0
    wait
    echo "All done."
    exit
}

# create log files and append headings
mkdir $USRPATH/logs &> /dev/null
CURTIME=$(date +%F_%T)
echo -e "####################################################\n## Run started at: $CURTIME" > "$USRPATH/logs/triplestore_$CURTIME.log"
echo -e "####################################################\n## Run started at: $CURTIME" > "$USRPATH/logs/compilation_$CURTIME.log"
echo -e "####################################################\n## Run started at: $CURTIME" > "$USRPATH/logs/analysis_$CURTIME.log"

# start all 3 processes
echo "Starting the Triplestore"
cd sparql_triplestore
./run.sh 2>&1 | tee -a "$USRPATH/logs/triplestore_$CURTIME.log" | grep "Started ServerConnector" &
# wait a while to let the triplestore start
sleep "$SLEEP"

echo "Starting the Compilation adapter"
cd ../compilation
mvn jetty:run-exploded 2>&1 | tee -a "$USRPATH/logs/compilation_$CURTIME.log" | grep "Started ServerConnector" &

echo "Starting the Analysis adapter"
cd ../analysis
mvn jetty:run-exploded 2>&1 | tee -a "$USRPATH/logs/analysis_$CURTIME.log" | grep "Started ServerConnector" &

echo -e "\nWait till startup finishes (3 messages with address:host at the end)\nUse ctrl+c to exit..."

cat # wait forever
