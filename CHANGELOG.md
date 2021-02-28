### v1.4.0
All adapter outputs are now in their own directory ".adapter" to not get in the way of analysis tool outputs.

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
