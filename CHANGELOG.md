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
