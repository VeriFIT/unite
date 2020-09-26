@echo off
:: launch the Fuseki SPARQL triplestore in Jetty

cd %~dp0

cd jetty-distribution
java -DFUSEKI_BASE="../triplestore" -jar start.jar