# Unite
author: Ondrej Vasicek, ivasicek@fit.vut.cz

This is a development repository of *Unite* (UNIversal analysis adapTEr based on the OSLC standard) which aims to provide an easy way of adding an OSLC Automation interface to as many analysis tools as possible by leveraging their command-line similarities. The adapter consists of two main components - Analysis Adapter and Compilation Adapter. The Compilation Adapter manages SUT resources, and the Analysis Adapter executes analysis on SUT resources using any configured analysis tool. The repository also includes a distribution of Jetty with an Apache Fuseki WAR for user's convenience (to make the setup process easier) which allows the adapter to be connected to a SPARQL triplestore for resource persistence and query capabilities.

## Directory structure
- analysis - OSLC analysis adapter as a maven webapp project.
- compilation - OSLC compilation adapter as a maven webapp project.
- conf - Place your configuration files in this directory.
- tutorials
    - conf_example - Example configuration.
    - Wiki - A downloaded version of the [Wiki](https://pajda.fit.vutbr.cz/verifit/oslc-generic-analysis/-/wikis/home)
    - Tutorial.postman_collection.json - A Postman collection with basic steps to take when using the adapter 
- dev_tools - scripts used during the development, etc.
- lib - External dependencies that were NOT created as a part of this project ([Jetty](https://www.eclipse.org/jetty/)
- license - Licence files for the adapter and Eclipse Lyo generated code.
- logs - Directory for logs produced by running the adapter.
- model - Lyo Designer modeling project used to generate base code, and models cloned from Lyo Domain.
- shared - Shared resources for both adapters as a maven project.
- domain - Domain resource classes for both adapters as a maven project.
- sparql_triplestore - Jetty base directory with an [Apache Jena Fuseki](https://jena.apache.org/documentation/fuseki2/) SPARQL server WAR deployed in it.
- docker - docker files for Unite

## How To Configure
Main things that need to be configured - analysis host&port, compilation host&port, triplestore host&port - defaults are "localhost" and ports "8080, 8081, 8082".
Other configuration includes authentication, persistency, dataset endpoints.

All configuration files should be placed into the *cloned_repo*/conf directory. See the *cloned_repo*/conf_example directory for a guide on how to create the conf directory.
- Adapters configuration
    - create *conf/VeriFitAnalysis.properties* based on *conf_example/VeriFitAnalysis.properties* and configure all properties (adapter host and port, sparql, ...)
    - create *conf/VeriFitCompilation.properties* based on *conf_example/VeriFitCompilation.properties* and configure all properties (adapter host and port, sparql, ...)
- Fuseki SPARQL triplestore 
	- create *conf/TriplestoreConf.ini* based on *conf_example*/TriplestoreConf.ini (change *jetty.http.host* and *jetty.http.port*)
    - The triplestore comes with two non-persistent datasets. If you want persistent ones, create two new datasets using Fuseki's Web UI.
        1) open a Web browser at *host*:*port*/fuseki/
        2) go to "manage datasets -> add new dataset"
        3) create a two new datasets (one for each adapter) type "Persistent" and name them based on your configuration in the .properties files.
        4) in *conf/VeriFitCompilation.properties* set *persist_sut_dirs=true*
- Analysis tool definition
    - in *conf/analysis_advanced/AnalysisTools* define an AutomationPlan in a .rdf file and a .properties file for every tool that you want to run using the adapter. Use the "ExampleTool" definition in *conf_example/analysis_advanced/AnalysisTools* as a guide on how to define your own. For more details refer to the [wiki](https://pajda.fit.vutbr.cz/verifit/oslc-generic-analysis/-/wikis/Usage-Guide/2.-Analysis-Tool-Definition).
- Output filter definition
    - in *conf/analysis_advanced/PluginFilters* define an output filters using a .java file and a .properties file for every filter. Use the "ExamplePluginFilter" definition in *conf_example/analysis_advanced/PluginFilters* as a guide on how to define your own. For more details refer to the [wiki](https://pajda.fit.vutbr.cz/verifit/oslc-generic-analysis/-/wikis/Usage-Guide/3.-Plugin-Output-Filters).

Alternatively, one can use environmental variables to override configuration from configuration files.
The available environmental variables are: UNITE_ANALYSIS_PORT, UNITE_ANALYSIS_HOST, UNITE_COMPILATION_PORT, UNITE_COMPILATION_HOST

## How To Run
Make sure you run a build script (build.sh or build.bat) before attempting to run anything, or use the -b parameter when running the run_all script!

#### Option 1) Run all at once
The easiest way to run Unite. Outputs of all three components of the Adapter will be saved in a *cloned_repo*/log directory.

##### Linux
- Use the run_all.sh script. Then use ctrl+c to exit.
- Or use the oslc_master script which runs the Adapter as background services. NOTE - the oslc_master handles log files in an incompatible way to the run_all.sh script
##### Windows
- Use the run_all.ps1 script. Then use ctrl+c to exit. Do not close the script by clicking X on the powershell window otherwise subprocesses will not be terminated (they run in their own consoles so they will be visible and can be closed manually)

#### Option 2) Run manually (advanced)
This option is mainly used for debugging. Note that conf files need to be moved into corresponding conf directories for each of the components manually (analysis/conf/, compilation/conf/, sparql_triplestore/start.ini). Launch each of the three components in separate terminals. Outputs will be visible directly through stdout and stderr with no implicit logging. The triple store needs to be up and running in order for the Analysis and Compilation adapters to launch successfully. 

##### Fuseki SPARQL jetty 
```
$ cd *cloned_repo*/sparql_triplestore
$ ./run.[sh/ps1] 
server online at - http://*host*:*port*/fuseki/
```
##### Analysis adapter
```
$ cd *cloned_repo*/analysis
$ ./run.[sh/ps1] 
server online at - http://*host*:*port*/analysis/
```
##### Compilation adapter
```
$ cd *cloned_repo*/compilation
$ ./run.[sh/ps1] 
server online at - http://*host*:*port*/compilation/
```

# Acknowledgement
This work was supported by the [AuFoVer](https://www.vutbr.cz/en/rad/projects/detail/29833) project and the [Arrowhead](https://www.fit.vut.cz/research/project/1299/.en) project.
