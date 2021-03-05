##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################

# Launch the triplestore
# Then install, lanuch and test the adapter


param (
    [switch]$t,
    [switch]$l,
    [switch]$h
)

$HELP="
   Runs the run_all script and then Postman testsuites for the adapter using Newman.
"
$USAGE="   Usage: $PSCommandPath [-t|-h|-l]
      -t ... tools - Runs an additional testsuite which requires some analysis
                         tools to be installed (i.e. will not work on all machines)
      -l ... live - Only runs the test suites without launching the adapter. Expects
                    the adapter to be running already.
      -h ... help
"

# duration for polling (via curl)
$SLEEP=1

$USRPATH=$(pwd)         # get the call directory
$ROOTDIR=$PSScriptRoot  # get the script directory
cd $ROOTDIR             # move to the script directory
cd ..
$ADAPTER_ROOT_DIR=$(pwd) # get the adapter root directory
cd $ROOTDIR             # move back to the script directory

$PIDS_TO_KILL=@()

function print_help() {
    echo "$HELP"
    echo "$USAGE"
    exit 0
}

# $1 name of the invalid arg
function invalid_arg() {
    param (
        $1
    )
    echo "`n   Invalid argument: ${1} `n"
    echo "$USAGE"
    exit 1
}

# polls an address using curl until the request returns True (i.e. the address responds)
# $1 ... URL for curl to poll
function curl_poll
{
    param (
        $1
    )
    $curl_ret=42
    while ( $curl_ret -ne $true )
    {
        Start-Sleep -Seconds $SLEEP
        $curl_ret = $false
        Write-Host -NoNewline  "."
        try{
            Invoke-WebRequest -Uri $1 -ErrorAction Ignore 2> $null > $null
        } catch {
        }
        $curl_ret=$?
    }
    echo ""
}


# Check that all required configuration files exist.
# Outputs an error message and exits the script if 
# a conf file is missing.
function checkConfFiles()
{
    if (!(Test-Path "$ADAPTER_ROOT_DIR/analysis/VeriFitAnalysis.properties" -PathType Leaf)) {
        echo 'ERROR: Configuration file "'$ADAPTER_ROOT_DIR'"/analysis/VeriFitAnalysis.properties" not found.'
        echo "  The adapter needs to be configured to be able to run!"
        echo '  See the "VeriFitAnalysisExample.properties" file for instructions and use it as a template.'
        exit 1
    }
    if (!(Test-Path "$ADAPTER_ROOT_DIR/compilation/VeriFitCompilation.properties" -PathType Leaf)) {
        echo 'ERROR: Configuration file "'$ADAPTER_ROOT_DIR'"/compilation/VeriFitCompilation.properties" not found.'
        echo "  The adapter needs to be configured to be able to run!"
        echo '  See the "VeriFitCompilationExample.properties" file for instructions and use it as a template.'
        exit 1
    }
    if (!(Test-Path "$ADAPTER_ROOT_DIR/sparql_triplestore/jetty-distribution/start.ini" -PathType Leaf)) {
        echo 'ERROR: Configuration file "'$ADAPTER_ROOT_DIR'"/sparql_triplestore/jetty-distribution/start.ini" not found.'
        echo "  The adapter needs to be configured to be able to run!"
        echo '  See the "startExample.ini" file for instructions and use it as a template.'
        exit 1
    }
}

# Kills a process and its children recursively
# $1 ... pid of the process
function KillWithChildren {
    Param(
        $1
    )
    Get-WmiObject win32_process | where {$_.ParentProcessId -eq $1} | ForEach { KillWithChildren $_.ProcessId }
    Stop-Process $1 2> $null
}

# kills all children of this process using the $PIDS_TO_KILL variable
function killChildren {
    echo "`nShutting down..."
    foreach ($i in $PIDS_TO_KILL) {
        KillWithChildren $i
    }
    echo "All done."
}

#
# main
#
if ($args.length -ne 0) {
    invalid_arg $args[0]
}
if ($h) {
    print_help
}

# make sure configuration files exist
checkConfFiles

# lookup analysis adapter config
$analysis_host=$(cat $ADAPTER_ROOT_DIR/analysis/VeriFitAnalysis.properties | Select-String -Pattern "^ *adapter_host=") -replace "^ *adapter_host=", "" -replace "/$", ""
$analysis_port=$(cat $ADAPTER_ROOT_DIR/analysis/VeriFitAnalysis.properties | Select-String -Pattern "^ *adapter_port=") -replace "^ *adapter_port=", ""
$analysis_url="${analysis_host}:${analysis_port}/analysis/"


if ( ! $l ) {
    echo "Booting up the Universal Analysis Adapter"
    $process = Start-Process -WindowStyle Minimized powershell.exe "(Get-Host).ui.RawUI.WindowTitle='Universal Analysis Adapter'; $ADAPTER_ROOT_DIR/run_all.ps1" -passthru
    $PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
    curl_poll $analysis_url       # poll the analysis adapter because that one starts last in the run script
    echo "Adapter up and running`n"
} else {
    echo "Skipping Adapter boot. Adapter expected to be running already." 
}


$compilationRes=$true
$analysisRes=$true
$analysisToolsRes=$true


echo ""
echo "Running Compilation adapter test suite" 
$clock = [Diagnostics.Stopwatch]::StartNew()
newman run $ADAPTER_ROOT_DIR/compilation/tests/TestSuite.postman_collection
$compilationRes=$?
$clock.Stop()
echo $clock.Elapsed

echo ""
echo "Running Analysis adapter test suite" 
$clock = [Diagnostics.Stopwatch]::StartNew()
newman run $ADAPTER_ROOT_DIR/analysis/tests/TestSuite.postman_collection
$analysisRes=$?
$clock.Stop()
echo $clock.Elapsed

echo ""
if ($t) {
    echo "Running Analysis adapter Tested Tools test suite" 
    $clock = [Diagnostics.Stopwatch]::StartNew()
    newman run $ADAPTER_ROOT_DIR/analysis/tests/TestSuite_TestedTools.postman_collection
    $analysisToolsRes=$?
    $clock.Stop()
    echo $clock.Elapsed
} else {
    echo "Skipping Analysis adapter Tested Tools test suite" 
}

# shutdown the adapter
killChildren

cd $USRPATH

# return non-zero if there were failed tests
if ( $compilationRes -ne $true -Or $analysisRes -ne $true -Or $analysisToolsRes -ne $true) {
    echo "`n`n  TESTS FAILED`n"
    exit 1
} else {
    echo "`n`n  TESTS PASSED`n"
    exit 0
}