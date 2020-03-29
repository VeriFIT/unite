#!/bin/bash
# launch the Fuseki SPARQL triplestore in Jetty

cd "${BASH_SOURCE%/*}"

cd jetty-distribution
java -DFUSEKI_BASE="../triplestore" -jar start.jar
