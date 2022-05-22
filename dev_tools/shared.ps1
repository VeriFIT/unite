##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################

# look for a conf file
#   if found, then copy it over to the destination place
#   otherwise copy over the default conf file
# $1 = custom conf file to copy
# $2 = default conf file to copy if no custom file found
# $3 = conf file destination
function confFileCopyCustomOrDefault ()
{
    param (
        $1, $2, $3
    )
    if (Test-Path "$1" -PathType Leaf) {
        echo "    using custom: $1"
        cp "$1" "$3"
    } else {
        echo "    using default: $2"
        cp "$2" "$3"
    }
}

# Check if a conf contains any conf files, if yes then copy its contents over to a destination. Clean the destination first
# $1 ... directory to check
# $2 ... location to copy to
function confAnalysisToolsCleanCheckAndCopy ()
{
    param (
        $1, $2
    )

    if (test-path "$2") {
        rm -r "$2"
    }
    mkdir "$2" > $null

    if (Test-Path "$1") {
        $confFilesInfo = ls "$1" | Where-Object {$_.name -match ".*[.]rdf|.*[.]properties"} | Measure-Object
        if ($confFilesInfo.count -ne 0) {
            echo "    using custom: $1"
            cp "$1\*" "$2"
        } else {
            echo "    no analysis tools found in $1"
        }
    } else {
        echo "    no analysis tools found in $1"
    }
}

# Check if a conf contains any conf files, if yes then copy its contents over to destinations. Clean the destinations first
# $1 ... directory to check
# $2 ... location to copy .propertires files to
# $3 ... location to copy .java files to
function confOutputFiltersCleanCheckAndCopy ()
{
    param (
        $1, $2, $3
    )
    if (test-path "$2") {
        rm -r "$2"
    }
    mkdir "$2" > $null
    
    if (test-path "$3") {
        rm -r "$3"
    }
    mkdir "$3" > $null
    
    if (Test-Path "$1") {
        $confFilesInfo = ls "$1" | Where-Object {$_.name -match ".*[.]java|.*[.]properties"} | Measure-Object
        if ($confFilesInfo.count -ne 0) {
            echo "    using custom: $1"
            cp "$1\*.properties" "$2"
            cp "$1\*.java" "$3"
        } else {
            echo "    no filters found in $1"
        }
    } else {
        echo "    no filters found in $1"
    }
}

# Check if a directory exists, if yes then copy its contents over to a destination.
# Clean the destination first
# $1 ... directory to check
# $2 ... location to copy to
function confDirectoryCleanCheckAndCopy ()
{
    param (
        $1, $2
    )

    if (test-path "$2") {
        rm -r "$2"
    }
    mkdir "$2" > $null

    if (Test-Path "$1") {
        cp "$1\*" "$2"
    }
}

# $1 ... root directory of the project
function processConfFiles()
{
    param (
        $1
    )
    
    # basic conf files (analysis, compilation, triplestore properties)
    echo "Checking Triplestore:"
    confFileCopyCustomOrDefault "$1\conf\TriplestoreConf.ini"           "$1\sparql_triplestore\startDefault.ini"                   "$1\sparql_triplestore\start.ini"
    echo "Checking Compilation Adapter:"
    confFileCopyCustomOrDefault "$1\conf\VeriFitCompilation.properties" "$1\compilation\conf\VeriFitCompilationDefault.properties" "$1\compilation\conf\VeriFitCompilation.properties"
    echo "Checking Analysis Adapter:"
    confFileCopyCustomOrDefault "$1\conf\VeriFitAnalysis.properties"    "$1\analysis\conf\VeriFitAnalysisDefault.properties"       "$1\analysis\conf\VeriFitAnalysis.properties"
    
    # Analysis adapter advanced conf files (tool definitions, output filters)
    echo "Checking Analysis Tools:"
    confAnalysisToolsCleanCheckAndCopy "$1\conf\analysis_advanced\AnalysisTools" "$1\analysis\conf\CustomAnalysisTools"
    echo "Checking Plugin Filters:"
    confOutputFiltersCleanCheckAndCopy "$1\conf\analysis_advanced\PluginFilters" "$1\analysis\conf\CustomPluginFiltersConfiguration" "$1\analysis\src\main\java\pluginFilters\customPluginFilters"

    # Certificates
    echo "Checking certificates."
    confDirectoryCleanCheckAndCopy "$1\conf\certificates" "$1\analysis\conf\certificates"
    confDirectoryCleanCheckAndCopy "$1\conf\certificates" "$1\compilation\conf\certificates"
}



# $1 ... root directory of the project
function lookupTriplestoreURL ()
{
    param (
        $1
    )
    $triplestore_host=$(cat "$1\sparql_triplestore\start.ini" | Select-String -Pattern "^ *jetty.http.host=") -replace "^ *jetty.http.host=", "" -replace "/$", "" # removes final slash in case there is one (http://host/ vs http://host)
    $triplestore_port=$(cat "$1\sparql_triplestore\start.ini" | Select-String -Pattern "^ *jetty.http.port=") -replace "^ *jetty.http.port=", ""
    $triplestore_url="http://${triplestore_host}:${triplestore_port}/fuseki/" # TODO prefix needs to be configurable for https
    return $triplestore_url
}
    
# $1 ... root directory of the project
function lookupCompilationURL ()
{
    param (
        $1
    )
    $compilation_host=$(cat "$1\compilation\conf\VeriFitCompilation.properties" | Select-String -Pattern "^ *adapter_host=") -replace "^ *adapter_host=", "" -replace "/$", ""
    $compilation_port=$(cat "$1\compilation\conf\VeriFitCompilation.properties" | Select-String -Pattern "^ *adapter_port=") -replace "^ *adapter_port=", ""
    
    # configuration can be overridden by env variables
    if ($null -ne $env:UNITE_COMPILATION_PORT) { # if not defined
        $compilation_port = $env:UNITE_COMPILATION_PORT
    }
    if ($null -ne $env:UNITE_COMPILATION_HOST) {
        $compilation_host = $env:UNITE_COMPILATION_HOST
    }

    $compilation_url="${compilation_host}:${compilation_port}/compilation/"
    return $compilation_url
}


# $1 ... root directory of the project
function lookupAnalysisURL ()
{
    param (
        $1
    )
    $analysis_host=$(cat "$1\analysis\conf\VeriFitAnalysis.properties" | Select-String -Pattern "^ *adapter_host=") -replace "^ *adapter_host=", "" -replace "/$", ""
    $analysis_port=$(cat "$1\analysis\conf\VeriFitAnalysis.properties" | Select-String -Pattern "^ *adapter_port=") -replace "^ *adapter_port=", ""
    
    # configuration can be overridden by env variables
    if ($null -ne $env:UNITE_ANALYSIS_PORT) { # if not defined
        $analysis_port = $env:UNITE_ANALYSIS_PORT
    }
    if ($null -ne $env:UNITE_ANALYSIS_HOST) {
        $analysis_host = $env:UNITE_ANALYSIS_HOST
    }
    
    $analysis_url="${analysis_host}:${analysis_port}/analysis/"
    return $analysis_url
}


# Check that all required configuration files exist.
# Outputs an error message and exits the script if 
# a conf file is missing.
# $1 ... root directory of the project
function checkConfFiles()
{
    param (
        $1
    )

    if (!(Test-Path "$1\analysis\conf\VeriFitAnalysis.properties" -PathType Leaf)) {
        echo 'ERROR: Configuration file "'$1'"\analysis\conf\VeriFitAnalysis.properties" not found.'
        echo "  The adapter needs to be configured to be able to run!"
        echo '  Run the build script first or use the -b option.'
        exit 1
    }
    if (!(Test-Path "$1\compilation\conf\VeriFitCompilation.properties" -PathType Leaf)) {
        echo 'ERROR: Configuration file "'$1'"\compilation\conf\VeriFitCompilation.properties" not found.'
        echo "  The adapter needs to be configured to be able to run!"
        echo '  Run the build script first or use the -b option.'
        exit 1
    }
    if (!(Test-Path "$1\sparql_triplestore\start.ini" -PathType Leaf)) {
        echo 'ERROR: Configuration file "'$1'"\sparql_triplestore\start.ini" not found.'
        echo "  The adapter needs to be configured to be able to run!"
        echo '  Run the build script first or use the -b option.'
        exit 1
    }
}

# Polls an address using curl until the request returns True (i.e. the address responds)
# Checks whether ctrl+C was pressed in every loop and terminates if it was.
# $1 ... URL for curl to poll
# $2 ... sleep duration between polls
function curl_poll
{
    param (
        $1, $2
    )
    $curl_ret=42
    while ( $curl_ret -ne $true )
    {
        Start-Sleep -Seconds $2
        $curl_ret = $false
        Write-Host -NoNewline  "."
        try{
            Invoke-WebRequest -UseBasicParsing -Uri $1 -ErrorAction Ignore 2> $null > $null
        } catch {
        }
        $curl_ret=$?
        
        $ret = checkForCtrlC    # check if the script should be killed
        if ($ret -eq 1) {
            return 1
        }
    }
    return 0
}

# Waits for an Url to go online.
# Also checks whether the associated process is still running (in case it failed and exited).
# Will kill all child processes if curl gets interrupted using ctrl+C.
# $1 ... url to poll
# $2 ... pid of the associated process
# $3 ... sleep in second (between polls)
# return ... 0 URL online, 1 the process stopped, 2 interrupted by ctrl+c
function waitForUrlOnline ()
{
    Param(
        $1, $2, $3
    )

    # poll the url using curl / Invoke-WebRequest
    # loop until URL up, interrupted, or process exited
    $curl_ret=$false
    while ( $curl_ret -ne $true )
    {
        # wait a while
        Start-Sleep -Seconds $3
        
        # check if URL online
        Write-Host -NoNewline  "."
        try{
            Invoke-WebRequest -UseBasicParsing -Uri $1 -ErrorAction Ignore 2> $null > $null
        } catch {
            # TODO
        }
        $curl_ret=$?
        
        # check if the process is still running
        $process = Get-Process -id $2 -ErrorAction SilentlyContinue
        if ($process) {
            # OK still running, lets wait a bit more (next loop)
        } else {
            # process not running --> must have encountered an error
            return 1  
        }

        # check if the script should be killed (ctrl+C pressed)
        $ret = checkForCtrlC    
        if ($ret -eq 1) {
            return 2
        }
    }
    return 0
}

# Check if ctrl+c was pressed
# return ... 0 if not pressed, 1 if pressed
function checkForCtrlC () {
    If ($Host.UI.RawUI.KeyAvailable -and ($Key = $Host.UI.RawUI.ReadKey("AllowCtrlC,NoEcho,IncludeKeyUp"))) {
        If ([Int]$Key.Character -eq 3) {
            return 1
        }
        # Flush the key buffer again for the next loop.
        $Host.UI.RawUI.FlushInputBuffer()
    }
    return 0
}

# Kills a process and its children recursively
# $1 ... pid of the process
function KillWithChildren {
    Param(
        $1
    )
    Get-WmiObject win32_process | where {$_.ParentProcessId -eq $1} | ForEach { KillWithChildren $_.ProcessId }
    try {
        Stop-Process $1 2> $null
    } catch {}
}

# kills all listed pids
# $1 ... pids to kill
function killAllWithChildren {
    Param(
        $1
    )
    foreach ($i in $1) {
        KillWithChildren $i
    }
}

# $1 ... help
# $2 ... usage
function print_help() {
    Param(
        $1, $2
    )
    echo "$1"
    echo "$2"
    exit 0
}

# $1 ... name of the invalid arg
# $2 ... usage
function invalid_arg() {
    Param(
        $1, $2
    )
    echo "`n   Invalid argument: ${1} `n"
    echo "$2"
    exit 1
}