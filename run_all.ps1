##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################

param (
    [switch]$t,
    [switch]$h,
    [switch]$b
)

# duration for polling (via curl)
$SLEEP=1

$HELP="
   Launches the sparql triplestore, the analysis adapter and
   the compilation adapter. The triplestore needs to finish
   startup before both adapters, which is controled by polling
   the triplestore using curl until it responds.
"
$USAGE="   Usage: $PSCommandPath [-t|-h|-b]
      -t ... tail - Opens tail -f for each output log in new gnome-terminals.
      -b ... build - Runs the build script first.
      -h ... help
"

$USRPATH=$(pwd)         # get the call directory
$ROOTDIR=$PSScriptRoot  # get the script directory
cd $ROOTDIR             # move to the script directory

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
            Invoke-WebRequest -UseBasicParsing -Uri $1 -ErrorAction Ignore 2> $null > $null
        } catch {
        }
        $curl_ret=$?
        checkForCtrlC # check if the script should be killed
    }
    echo ""
}

# Check that all required configuration files exist.
# Outputs an error message and exits the script if 
# a conf file is missing.
function checkConfFiles()
{
    if (!(Test-Path "./analysis/VeriFitAnalysis.properties" -PathType Leaf)) {
        echo 'ERROR: Configuration file "'$ROOTDIR'"/analysis/VeriFitAnalysis.properties" not found.'
        echo "  The adapter needs to be configured to be able to run!"
        echo '  See the "VeriFitAnalysisExample.properties" file for instructions and use it as a template.'
        exit 1
    }
    if (!(Test-Path "./compilation/VeriFitCompilation.properties" -PathType Leaf)) {
        echo 'ERROR: Configuration file "'$ROOTDIR'"/compilation/VeriFitCompilation.properties" not found.'
        echo "  The adapter needs to be configured to be able to run!"
        echo '  See the "VeriFitCompilationExample.properties" file for instructions and use it as a template.'
        exit 1
    }
    if (!(Test-Path "./sparql_triplestore/jetty-distribution/start.ini" -PathType Leaf)) {
        echo 'ERROR: Configuration file "'$ROOTDIR'"/sparql_triplestore/jetty-distribution/start.ini" not found.'
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

# If a key was pressed during the loop execution, check to see if it was CTRL-C (aka "3"), and if so exit the script after clearing
# out any running jobs and setting CTRL-C back to normal.
# Based on: https://docs.microsoft.com/en-us/archive/blogs/dsheehan/powershell-taking-control-over-ctrl-c
function checkForCtrlC () {
    If ($Host.UI.RawUI.KeyAvailable -and ($Key = $Host.UI.RawUI.ReadKey("AllowCtrlC,NoEcho,IncludeKeyUp"))) {
        If ([Int]$Key.Character -eq 3) {
            killChildren
            [Console]::TreatControlCAsInput = $False
            cd $USRPATH
            exit 0
        }
        # Flush the key buffer again for the next loop.
        $Host.UI.RawUI.FlushInputBuffer()
    }
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

# build first if requested by args
if ($b)
{
    echo "Running build.sh first"
    .\build.ps1
    if ( $LastExitCode -ne 0) {
        echo "Build failed. Aborting start.`n"
        exit $LastExitCode
    }
}

# get and output version
$VERSION=$(cat .\VERSION.md 2> $null)
echo ""
echo "########################################################"
echo "    OSLC Universal Analysis, $VERSION"
echo "########################################################"
echo ""

# lookup triplestore config
$triplestore_host=$(cat sparql_triplestore/jetty-distribution/start.ini | Select-String -Pattern "^ *jetty.http.host=") -replace "^ *jetty.http.host=", "" -replace "/$", "" # removes final slash in case there is one (http://host/ vs http://host)
$triplestore_port=$(cat sparql_triplestore/jetty-distribution/start.ini | Select-String -Pattern "^ *jetty.http.port=") -replace "^ *jetty.http.port=", ""
$triplestore_url="http://${triplestore_host}:${triplestore_port}/fuseki/" # TODO prefix needs to be configurable for https
# lookup compilation adapter config
$compilation_host=$(cat compilation/VeriFitCompilation.properties | Select-String -Pattern "^ *adapter_host=") -replace "^ *adapter_host=", "" -replace "/$", ""
$compilation_port=$(cat compilation/VeriFitCompilation.properties | Select-String -Pattern "^ *adapter_port=") -replace "^ *adapter_port=", ""
$compilation_url="${compilation_host}:${compilation_port}/compilation/"
## lookup analysis adapter config
$analysis_host=$(cat analysis/VeriFitAnalysis.properties | Select-String -Pattern "^ *adapter_host=") -replace "^ *adapter_host=", "" -replace "/$", ""
$analysis_port=$(cat analysis/VeriFitAnalysis.properties | Select-String -Pattern "^ *adapter_port=") -replace "^ *adapter_port=", ""
$analysis_url="${analysis_host}:${analysis_port}/analysis/"

## create log files and append headings
mkdir -Force $ROOTDIR/logs > $null
$CURTIME=$(date).ToString("yyyy-MM-dd_HH.mm.ss")
$CURTIME_FORLOG=$(date).ToString("yyyy-MM-dd_HH:mm:ss")
echo "########################################################`r`n    Running version: $VERSION`r`n    Started at: $CURTIME_FORLOG`r`n########################################################`r`n" > "$ROOTDIR/logs/triplestore_$CURTIME.log"
echo "########################################################`r`n    Running version: $VERSION`r`n    Started at: $CURTIME_FORLOG`r`n########################################################`r`n" > "$ROOTDIR/logs/compilation_$CURTIME.log"
echo "########################################################`r`n    Running version: $VERSION`r`n    Started at: $CURTIME_FORLOG`r`n########################################################`r`n" > "$ROOTDIR/logs/analysis_$CURTIME.log"

# treat ctrl+c as input so that we can kill subprocesses (sleep and flush to properly flush input)
[Console]::TreatControlCAsInput = $True
Start-Sleep -Seconds 1
$Host.UI.RawUI.FlushInputBuffer()

# open new terminals that tail the log files and record their PIDs to kill later
if ($t)
{
    $process = Start-Process powershell.exe "(Get-Host).ui.RawUI.WindowTitle='tail: Triplestore Log (feel free to close this window)'; Get-Content $ROOTDIR\logs\..\logs\..\logs\triplestore_$CURTIME.log -Wait -Tail 10" -passthru
    $PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
    $process = Start-Process powershell.exe "(Get-Host).ui.RawUI.WindowTitle='tail: Compilation Log (feel free to close this window)'; Get-Content $ROOTDIR\logs\..\logs\..\logs\compilation_$CURTIME.log -Wait -Tail 10" -passthru
    $PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
    $process = Start-Process powershell.exe "(Get-Host).ui.RawUI.WindowTitle='tail: Analysis Log (feel free to close this window)'; Get-Content $ROOTDIR\logs\..\logs\..\logs\analysis_$CURTIME.log -Wait -Tail 10" -passthru
    $PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
}

# start the triplestore
echo "Starting the Triplestore"
cd sparql_triplestore
$process = Start-Process -WindowStyle Minimized powershell.exe "(Get-Host).ui.RawUI.WindowTitle='Triplestore'; ./run.ps1 >> $ROOTDIR/logs/triplestore_$CURTIME.log 2>&1" -passthru
$PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
echo "Waiting for the Triplestore to finish startup"
curl_poll $triplestore_url
echo "Triplestore running`n"

## start the compilation adapter
echo "Starting the Compilation adapter"
cd ../compilation
$process = Start-Process -WindowStyle Minimized powershell.exe "(Get-Host).ui.RawUI.WindowTitle='Compilation Adapter'; mvn jetty:run-exploded >> $ROOTDIR/logs/compilation_$CURTIME.log 2>&1" -passthru
$PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
echo "Waiting for the Compilation adapter to finish startup"
curl_poll $compilation_url
echo "Compilation adapter running`n"

# start the analysis adapter
echo "Starting the Analysis adapter"
cd ../analysis
$process = Start-Process -WindowStyle Minimized powershell.exe "(Get-Host).ui.RawUI.WindowTitle='Analysis Adapter'; mvn jetty:run-exploded >> $ROOTDIR/logs/analysis_$CURTIME.log 2>&1" -passthru
$PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
echo "Waiting for the Analysis adapter to finish startup"
curl_poll $analysis_url
echo "Analysis adapter running`n"
cd ..

echo "Ready to go!"
echo "Use ctrl+c to exit..."

# loop to catch ctrl+c
While ($true) {
    checkForCtrlC
}