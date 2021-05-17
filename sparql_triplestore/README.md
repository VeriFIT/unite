# Jetty server with an Apache Jena Fuseki SPARQL triplestore webapp
https://jena.apache.org/documentation/fuseki2/

## How to run
Execute the ./run.sh or ./run.ps1 

## Changing the port and host
In file ./start.ini change "jetty.http.port" and "jetty.http.host"

## Triplestore storage location
In the "triplestore" directory.
Currently set in the run script

## How to configure the server
The server has a web UI at http://localhost:8081/fuseki (based on your configuration). There you can create a new dataset or manage existing datasets. The adapter works with in-memory datasets and with persistent datasets (not with persistent TDB2). Dataset endpoints can be found in dataset info.
