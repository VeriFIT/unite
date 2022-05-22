### v.2.8.0
- added AHT framework service registration

### v2.7.4
- run_all log now prints URL's of the started services
- unite-only docker rename to unite-base
- unite-base docker now uses the docker-base Git tag as its code base

### v2.7.3
- Added configuration of host/port through environment variables.
  Env variables override conf files. Available env variables are:
  - UNITE_ANALYSIS_PORT
  - UNITE_ANALYSIS_HOST
  - UNITE_COMPILATION_PORT
  - UNITE_COMPILATION_HOST
- Added dedicated run.sh/ps1 to analysis and compilation

### v2.7.2
- Added docker files for Theta and Valgrind

### v2.7.1
- Added docker files for Unite and Unite+Infer
- conf directory now contains the whole dir structure (empty) in git

### v2.7.0
- Analysis tool launch commands now do not have automatic quotes to give more control to users.
- fixed plugin filter dependencies (OslcValues moved from "shared")

### v2.6.2
- Split the java shared and domain modules to allow the domain to be reused elsewhere without dependencies
- Modified the dummy tool to make the analysis test suite faster
- The relevant log tailing terminal now stays open when its corresponding components fails to startup
  - There was a bug which caused all the log tailing terminals to close in case of startup failure

### v2.6.1
- GET requests for inProgress Automation Result now recognize a HTTP query string parameter called "enableInProgressOutputs"
  - possible values are true/false
  - default is currently true
- fixed some built-in output filters not being listed
- added more tests for built-in output filters

### v2.6.0
- It is now possible to choose between Powershell and CMD on Windows
  - Use the configuration files (VeriFitAnalysis.properties, VeriFitCompilation.properties) to choose which 
    shell to use by changing the "config_win_shell" property (values "windows_ps1" or "windows_bat").

- run_all.sh now checks whether mvn and curl are available
- build.sh check whether mvn is available

### v2.5.0
- Contents of stdout and stderr of executed analysis can now be polled by clients while the analysis is in progress
- run & test scripts will now not wait forever when one of Unite's components fails to start
  - all scripts now poll the URL (as before) and also check whether the components process is still running (to return an error instead of waiting forever)
- Automation Result updates changed to update Contribution resources explicitly one by one instead of as part of the
  whole A.Result. Otherwise the update would be incorrect and cause the Contributions to have duplicate values for 
  their updated properties. This is likely a bug in lyo.store (or me not understanding SPARQL properly).
- run_all.sh now deletes old logs -- only the 5 most recent will stay
 - "get-date" instead of "date" in run_all script
- bug fix - oneinstanceonly typo
  - the loaded property was matched as OneInstanceOnly instead of oneInstanceOnly (capital first letter) 

### v2.4.0
- renamed to Unite
- now should work in folders with spaces (linux and windows) (bug #1)
- a bit of colored output on linux
- fixed bug #2
- better way of unziping files in the analysis adapter
- added -NoProfile to powershell execution to avoid user profiles messing with the execution (e.g. directory change)

### v2.3.1
- readme and wiki updates for thesis submission
- NO functional changes

### v2.3.0
- valuePrefix for command line parameters
  - allows non-positional command line parameters to be defined for analysis tools
  - e.g. prefix "--arg=" and parameter value "val" will result in the command line string containing "--arg=val"
- confFile supports multiple instances (using the parameter multiple times to create multiple conf files) 
- confDir parameter
  - similar to confFile except its used to create a whole configuration directory in the SUT directory before analysis
  - the directory is transferred as a base64 encoded zip file
  - value format is "path/to/unzip/to\nbase64encodedZip"
- reworked launchSUT and SUTbuildCommand analysis parameters
  - now use fit:commandlinePosition to define the place to put the command
  - and the value is a boolean which says whether to place the command or not
- better automation plan examples
  - complete anaconda interface and tests
  - mostly complete valgrind interface and tests
  - mostly complete infer interface and tests
- outputFileRegex improvements
  - now reports an error if an invalid regex is supplied
  - file matching is now performed on the file path and not just the file name (to enable directory control)
- jSEM dependency changed to Maven
- added envVariable input parameter to the Analysis adapter
  - allows environment variables to be set for execution
  - can be used multiple times to set multiple variables
  - expected value is "variable_name\nvariable_value"
- base64 value check before execution to return an error immediately 
- tutorials folder
  - includes a version of the Wiki, conf_example, and Postman tutorial collection

### v2.2.0
- implemented resource delete
  - delete capabilities for A.Requests, A.Results, Contributions (analysis only), SUT (compilation only)
  - Requests, Results, and SUTs recognize a "cascade" parameter which when set to "true" makes it so that 
    deleting any one of the three will result in all three getting deleted
  - deleting a resource that is connected to a directory or a file will also delete the file/directory (SUT, contribution, A.Result)
  - deleting an Automation Request that is not yet finished results in the request getting canceled first (using desiredState update),
    unfinished Automation Results can not be deleted
- implemented resource update
  - update capabilities for A.Requests, A.Results, Contributions (analysis only), SUT (compilation only)
  - not all properties can be updated
  - properties can not be changed before execution finishes, except for the desiredState property of AutomationRequests
- implemented execution cancel
  - cancel Automation Request execution by updating the A.Request's desiredState property to "canceled"
  - canceling only works on requests that are still state "inProgress"
- reworked resource ID generators
  - now use long instead of int
  - initialization is based on a special bookmarkID resource instead of walking the whole triplestore
- keep_last_n
  - added a configuration option that tells the adapter to automatically delete old AutomationRequests and 
    their associated Results and all other artifacts (SUTs, Contributions, ...)
  - configured in *adapter*.properties using "keep_last_n_enabled" (bool) and "keep_last_n" (>0)
  - added a test suite for keep_last_n (separate test suite due to requiring different adapter conf)
- added a confFile input parameter to the Analysis adapter
  - a file that gets created before the analysis is executed
  - usecase: a tool that accepts a "--conf=path/to/file" parameter (like anaconda)
  - value format is "filename\nfilecontents"
- added a link to the cratedSUT resource as a Contribution as well in the Compilation adapter for standard compliance
- better plugin filter example
- added a beforeCommand and an afterCommand to Analysis adapter
  - commands that can be set to run right before or right after executing analysis
  - if the beforeCommand fails, analysis is not executed, and neither is the afterCommand
  - if analysis fails, the afterCommand is not executed
- added an option to override default values of predefined ParameterDefinitions in the Analysis adapter
  - when defining an analysis tool automation plan, define a ParameterDefinition with the same name as one of the predefined
    parameters (e.g. timeout, zipOutputs, ...; except the SUT parameter) and your default value will be used instead

### v2.1.1
- fixed high cpu usage on Windows
  - run_all.ps1 had a active wait loop that was causing Powershell to use a lot of CPU
  - fixed by adding sleep into the loop

### v2.1.0
- added a ./conf directory and a ./conf_example directory
  - the ./conf directory is where users place all their custom configuration files
  - both the build script and the run script will then load the configuration from
    the ./conf directory and copy it into appropriate places
  - see the ./conf_example to learn what configuration files to place in the ./conf dir
- reworked scripts to used shared resources
- moved the jetty distribution into the ./lib directory and then use jetty base in ./sparql_triplestore
- changed built in tool loading to be loaded the same way as custom tools
- changed powershell exec in the analysis adapter to print exceptions properly
- changed linux exec so that the script contains a /bin/bash -c "$1" and the string to execute gets passed as the $1 to the script

### v2.0.0
- added a new common input parameter "outputFilter" to the analysis adapter
	- features allowedValue properties that list all available parsers
	- parsers can be defined as plugins with configuration located in analysis/PluginParserConfiguration
	  and plugin source files placed in src/main/java/pluginParsers
	- each plugin parser has to implement the IParser interface and the IExtension interface
  - there is a few predefined parsers including an example template parser
- added jSEM as a .jar file to get plugin output parsers/filters (potentially a temporary solution)
- added URIs to Contribution resources
  - **fit:fileURI** renamed to **fit:filePath** and no longer serves as a copy paste method of downloading files.
    Instead, octet-stream GETs or PUTs should be directed at the URI of the contribution itself
  - added a query capability for Contribution resources
- the root directory is now an Eclipse project for hierarchical view

### v1.4.0
- All adapter outputs are now in their own directory ".adapter" to not get in the way of analysis tool outputs.
- Changed rdf:value type to String from XMLLiteral to avoid invalid XMLLiteral warnings for contribution values.
- Added a new parameter to the test script (-l to not launch the adapter)
- renamed maven project model to lyo_designer_model
- typos, refactor, ...
- added a "compile" command to the compilation adapter that controls whether to perform compilation or not
- added a boolean fit:compiled property to SUT resources

### v1.3.1
powershell modifications
- The previous way of executing powershell commands had issues with quotes.
- Command execution was changed from directly running a command using java .exec()
  to writing the command into a script file and then executing that script.
  The command to execute has to be a single line (probably), and the script then 
  exits with the commands return code. Powershell commands can also throw exceptions
  which the script catches and returns a non-zero exit code.

### v1.3.0
configuration
- Moved triplestore conf out of the repository
- build script now creates conf files

Powershell
- The adapter now uses powershell.exe to execute commands on windows (instead of cmd /c)
- created replaced .bat scripts with .ps1 scripts

### v1.2.0
New contributions
- executionTime - Duration of execution in milliseconds
- returnCode - return code of the execution
- statusMessage - status messages from the adapter (replaced messages prefixed with # in the previous versions)

Contribution changes
- renamed "Analysis stdout/stderr" and "Compilation stdout/sdterr" to just "stdout/stderr"
- stdout/stderr is now saved to a file (will be in the zip and in the SUT)

Run script additions
- run\_all.sh now has a -t option to launch terminals with "tail -f log\_files" 

test suite changes
- does not use sleep for synchronization anymore (waiting for automation to finish), uses get requests in a cycle instead

Other
- added CHANGELOG and VERSION
- version now shows on startup

### v1.1.0
Build script and shared module

### v1.0.1
Hotfit for loy store properties

### v1.0.0
Migrated to Lyo 4.0.0

### v0.x.x
Old prototype
