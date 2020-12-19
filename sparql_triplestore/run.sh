#!/bin/sh
# launch the Fuseki SPARQL triplestore in Jetty

cd $(dirname $0)/jetty-distribution
exec java -DFUSEKI_BASE="../triplestore" -jar start.jar
