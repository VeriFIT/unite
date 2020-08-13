#!/bin/bash

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

# append headings to .log files
CURTIME=$(date)
echo -e "\n####################################################n## New run started at: $CURTIME" >> $USRPATH/triplestore.log
echo -e "\n####################################################n## New run started at: $CURTIME" >> $USRPATH/compilation.log
echo -e "\n####################################################n## New run started at: $CURTIME" >> $USRPATH/analysis.log

# start all 3 processes
echo "Starting the Triplestore"
cd sparql_triplestore
./run.sh 2>&1 | tee -a $USRPATH/triplestore.log | grep "Started ServerConnector" &

echo "Starting the Compilation adapter"
cd ../compilation
mvn jetty:run-exploded 2>&1 | tee -a $USRPATH/compilation.log | grep "Started ServerConnector" &

echo "Starting the Analysis adapter"
cd ../analysis
mvn jetty:run-exploded 2>&1 | tee -a $USRPATH/analysis.log | grep "Started ServerConnector" &

echo -e "\nWait till startup finishes (3 messages with address:host at the end)\nUse ctrl+c to exit..."

cat # wait forever
