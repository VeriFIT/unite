#!/bin/bash

# Launch the triplestore
# Then install, lanuch and test the adapter

cd "${BASH_SOURCE%/*}"


echo "  Running the SPARQL triplestore.."
timeout 200s ../sparql_triplestore/run.sh &> /dev/null &

cd ../oslc_adapter
echo "  Building the Adapter.."
mvn clean install &> /dev/null

echo "  Running the Adapter.."
timeout 150s mvn jetty:run-exploded &> /dev/null &
sleep 30s

echo "  Running Newman test suite.."
newman run tests/Tests.postman_collection.json
