# Universal OSLC Analysis Adapter
author: Ondrej Vasicek, xvasic25@stud.fit.vutbr.cz

This is a development repository of the Universal OSLC Analysis Adapter which aims to provide an easy way of adding an OSLC Automation interface to as many analysis tools as possible by leveraging their command-line similarities. The adapter consists of two main components - Analysis Adapter and Compilation Adapter. The Compilation Adapter manages SUT resources, and the Analysis Adapter executes analysis on SUT resources using any configured analysis tool. The repository also includes a distribution of Jetty with an Apache Fuseki WAR for user's convenience (to make the setup process easier) which allows the adapter to be connected to a SPARQL triplestore for resource persistence and query capabilities.

## Directory structure
- model - Lyo Designer modeling project used to generate base code
- analysis - eclipse maven project, OSLC analysis adapter as a java webapp
- compilation - eclipse maven project, OSLC compilation adapter as a java webapp
- sparql_triplestore - [Jetty](https://www.eclipse.org/jetty/) distribution with an [Apache Jena Fuseki](https://jena.apache.org/documentation/fuseki2/) SPARQL server WAR 
- dev_tools - scripts used during the development

## How To Configure
Things that need to be configured - analysis host&port, compilation host&port, triplestore host&port, and optionally dataset endpoints.
Defaults are "localhost" and ports "8080, 8081, 8082".
- Adapters configuration
	- in *analysis/VeriFitAnalysis.properties* configure all properties (adapter host and port, sparql, ...)
	- in *compilation/VeriFitCompilation.properties* configure all properties (adapter host and port, sparql, ...)
- Fuseki SPARQL triplestore 
	- in *sparql_triplestore/jetty-distribution/start.ini* change *jetty.http.host* and *jetty.http.port*
    - The triplestore comes with two non-persistent datasets. If you want persistent ones, create two new datasets using Fuseki's Web UI.
        1) open a Web browser at *host*:*port*/fuseki/
        2) go to "manage datasets -> add new dataset"
        3) create a two new datasets (one for each adapter) type "Persistent" and name them based on your configuration in the .properties files.
        4) in *compilation/VeriFitCompilation.properties* set *persist_sut_dirs=true*
- Analysis tool definition
    - in *analysis/AnalysisToolDefinitions* define an AutomationPlan in a .rdf file and a .properties file for every tool that you want to run using the adapter. Use the "ExampleTool" definition as a guide on how to define your own. For more details refer to the [wiki](https://pajda.fit.vutbr.cz/xvasic/oslc-generic-analysis/-/wikis/Usage-Guide/2.-Analysis-Tool-Definition).

## How To Run
#### Option 1) Run all at once
The easiest way to run the Universal Analysis Adapter. Outputs of all three components of the Adapter will be saved in a ./log directory.

##### Linux
- Use the run_all.sh script. Then use ctrl+c to exit.
- Or use the oslc_master script which runs the Adapter as background services. IMPORTANT - the oslc_master handles log files in an incompatible way to the run_all.sh script (so stick to using one or clear the logs before switching).
##### Windows
- Use the run_all.bat script. Then press any key to exit. Do not use ctrl+c otherwise subprocesses will not be terminated (they run in their own consoles so they will be visible and can be closed manually)

#### Option 2) Run manually
This option is mainly used for debugging or simply to have more control. Launch each of the three components in separate terminals. Outputs will be visible directly through stdout and stderr with no implicit logging. The triple store needs to be up and running in order for the Analysis and Compilation adapters to launch successfully. 

##### Fuseki SPARQL jetty 
```
$ cd *cloned_repo*/sparql_triplestore
$ ./run.[sh/bat] 
server online at - http://*host*:*port*/fuseki/
```
##### Analysis adapter
```
$ cd *cloned_repo*/analysis
$ mvn jetty:run-exploded
server online at - http://*host*:*port*/analysis/
```
##### Compilation adapter
```
$ cd *cloned_repo*/compilation
$ mvn jetty:run-exploded
server online at - http://*host*:*port*/compilation/
```

# Acknowledgement
This work was supported by the [AuFoVer](https://www.vutbr.cz/en/rad/projects/detail/29833) project.
