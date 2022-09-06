#### Installation
- prerequisites: java 1.8, maven, curl, bash (unix) or powershell (win)
- useful tools: Postman, Newman
1) clone repository
2) execute build.sh or build.ps1

#### Configuration
Main things that need to be configured - analysis host&port, compilation host&port, triplestore host&port - defaults are "localhost" and ports "8080, 8081, 8082".
Other configuration includes authentication, persistency, dataset endpoints.

All configuration files should be placed into the \*cloned_repo\*/conf directory. See the *cloned_repo*/tutorials/conf_example directory for a guide on how to create the conf directory.
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
    - in *conf/analysis_advanced/PluginFilters* define output filters using a .java file and a .properties file for every filter. Use the "ExamplePluginFilter" definition in *conf_example/analysis_advanced/PluginFilters* as a guide on how to define your own. For more details refer to the [wiki](https://pajda.fit.vutbr.cz/verifit/oslc-generic-analysis/-/wikis/Usage-Guide/3.-Plugin-Output-Filters).

Alternatively, one can use environmental variables to override configuration from configuration files.
The available environmental variables are: UNITE_ANALYSIS_PORT, UNITE_ANALYSIS_HOST, UNITE_COMPILATION_PORT, UNITE_COMPILATION_HOST