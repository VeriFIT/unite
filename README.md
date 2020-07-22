# Universal OSLC Automation Adapter
author: Ondrej Vasicek, xvasic25@stud.fit.vutbr.cz
TODO brief

## Directory structure
- model - Lyo Designer modeling project used to generate base code
- analysis - eclipse maven project, OSLC analysis adapter as a java webapp
- compilation - eclipse maven project, OSLC compilation adapter as a java webapp
- sparql_triplestore - jetty distribution with an Apache Jena Fuseki SPARQL server WAR 
- dev_tools - scripts used during the development

## How to Install and Run
The adapters need a SPARQL triplestore. Start the triplestore before launching the Adapter. A Fuseki jetty server is included in this repository but feel free to use any other.

#### Configuration files
Things that need to be configured - analysis host&port, compilation host&port, triplestore host&port, and optionally dataset endpoints.
Defaults are "localhost" and ports "8080, 8081, 8082".
- Adapters configuration
	- in *analysis/VeriFitAnalysis.properties* configure all properties (adapter host and port, sparql, ...)
	- in *compilation/VeriFitCompilation.properties* configure all properties (adapter host and port, sparql, ...)
- Fuseki SPARQL triplestore 
	- in *sparql_triplestore/jetty-distribution/start.ini* change *jetty.http.host* and *jetty.http.port*

#### Run all at once
##### Linux
Use the run_all.sh script. Then use ctrl+c to exit.
##### Windows
Use the run_all.bat script. Then press any key to exit. Do not use ctrl+c otherwise subprocesses will not be terminated (they run in their own consoles so they will be visible and can be closed manually)

#### Run manually
Analysis adapter
```
$ cd *cloned_repo*/analysis
$ mvn jetty:run-exploded
server online at - http://*host*:*port*/analysis/
```
Compilation adapter
```
$ cd *cloned_repo*/compilation
$ mvn jetty:run-exploded
server online at - http://*host*:*port*/compilation/
```

#### Fuseki SPARQL jetty 
```
$ cd *cloned_repo*/sparql_triplestore
$ ./run.sh

server online at - http://*host*:*port*/fuseki/
```
The triplestore comes with two non-persistent datasets. If you want persistent ones, create two new datasets using Fuseki's Web UI.
1) open a Web browser at *host*:*port*/fuseki/
2) go to "manage datasets -> add new dataset"
3) create a two new datasets (one for each adapter) type "Persistent" and name them based on your configuration in the .properties files.
