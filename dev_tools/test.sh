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
