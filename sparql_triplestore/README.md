# Jetty server with an Apache Jena Fuseki SPARQL triplestore webapp
https://jena.apache.org/documentation/fuseki2/

## How to run
Execute the ./run.sh 

## Changing the port and host
In file jetty-distribution/start.ini change "jetty.http.port" and "jetty.http.host"

## Triplestore storage location
In the "triplestore" directory.
Currently set in the ./run.sh script

## How to configure the server
The server has a web UI at http://localhost:8081/fuseki. There you can create a new dataset or manage existing datasets. The Anaconda OSLC Adapter works with in-memory datasets and with persistent datasets (not with persistent TDB2). Dataset endpoints can be found in dataset info.
