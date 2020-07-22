#!/bin/bash

cd "${BASH_SOURCE%/*}"

trap 'killall' INT

killall() {
    trap '' INT TERM     # ignore INT and TERM while shutting down
    echo "Shutting down..."
    kill -TERM 0
    wait
    echo "All done."
    exit
}


echo "Starting the Triplestore"
cd sparql_triplestore
./run.sh 2>&1 | grep "Started ServerConnector" &

echo "Starting the Compilation adapter"
cd ../compilation
mvn jetty:run-exploded 2>&1 | grep "Started ServerConnector" &

echo "Starting the Analysis adapter"
cd ../analysis
mvn jetty:run-exploded 2>&1 | grep "Started ServerConnector" &

echo
echo "Wait till startup finishes (3 messages with address:host at the end)"
echo "Use ctrl+c to exit..."

cat # wait forever
