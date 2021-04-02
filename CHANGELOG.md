### v2.2.0
- implemented resource delete
  - delete capabilities for A.Requests, A.Results, Contributions (analysis only), SUT (compilation only)
  - Requests, Results, and SUTs recognize a "cascade" header which when set to "true" makes it so that 
    deleting any one of the three will result in all three getting deleted
  - deleting a resource that is connected to a directory or a file will also delete the file/directory (SUT, contribution, A.Result)
- implemented resource update
  - update capabilities for A.Requests, A.Results, Contributions (analysis only), SUT (compilation only)
  - not all properties can be updated
  - properties can not be changed before execution finishes, except for the desiredState property of AutomationRequests
- implemented execution cancel
  - cancel Automation Request execution by updating the A.Request's desiredState property to "canceled"
  - canceling only works on requests that are still state "inProgress"


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
