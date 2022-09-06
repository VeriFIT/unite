Make sure you run a build script (build.sh or build.bat) before attempting to run anything, or use the -b parameter when running the run_all script!!

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
$ mvn jetty:run-exploded
server online at - http://*host*:*port*/analysis/
```
##### Compilation adapter
```
$ cd *cloned_repo*/compilation
$ mvn jetty:run-exploded
server online at - http://*host*:*port*/compilation/
```