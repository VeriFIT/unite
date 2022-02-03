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

$HELP="
   Launches the sparql triplestore, the analysis adapter and
   the compilation adapter. The triplestore needs to finish
   startup before both adapters, which is controled by polling
   the triplestore using curl until it responds.

   Also reloads all configuration files.
"
$USAGE="   Usage: $PSCommandPath [-t|-h|-b]
      -t ... tail - Opens tail -f for each output log in new gnome-terminals.
      -b ... build - Runs the build script first.
      -h ... help
"

$USRPATH=$(pwd)         # get the call directory
$ROOTDIR="$PSScriptRoot"  # get the script directory
$PIDS_TO_KILL=@()

# duration for polling (via curl)
$SLEEP=3

# source shared utils
. "$ROOTDIR\dev_tools\shared.ps1"

# exits this scripts while killing all the subprocesses
# used when ctrl+c is received
function exit_killall ()
{
    # try to kill all log tailing terminals regardless of the current startup state 
    $PIDS_TO_KILL = $PIDS_TO_KILL + $PID_TRIPLESTORE_TAIL
    $PIDS_TO_KILL = $PIDS_TO_KILL + $PID_ANALYSIS_TAIL
    $PIDS_TO_KILL = $PIDS_TO_KILL + $PID_COMPILATION_TAIL

    abortAll
}

# used by the script in case of failure
function abortAll ()
{
    echo "`nShutting down..."
    killAllWithChildren $PIDS_TO_KILL
    echo "All done."
    [Console]::TreatControlCAsInput = $False
    exit 0
}

#
# main
#

# check args
if ($args.length -ne 0) {
    invalid_arg $args[0] "$USAGE"
}
if ($h) {
    print_help "$HELP" "$USAGE"
}

# build first if requested by args
if ($b)
{
    echo "Running build.sh first"
    & "${ROOTDIR}\build.ps1"
    if ( ! $? ) {
        echo "Build failed. Aborting start.`n"
        exit $LastExitCode
    }
}
# otherwise just update conf
else {
    processConfFiles "$ROOTDIR"
}

# get and output version
$VERSION=$(cat "$ROOTDIR\VERSION.md" 2> $null)
echo ""
echo "########################################################"
echo "    Unite $VERSION"
echo "########################################################"
echo ""

# lookup config URLs
$triplestore_url = lookupTriplestoreURL "$ROOTDIR"
$compilation_url = lookupCompilationURL "$ROOTDIR"
$analysis_url = lookupAnalysisURL "$ROOTDIR"

# create log files and append headings
mkdir -Force "$ROOTDIR\logs" > $null
$CURTIME=$(Get-Date).ToString("yyyy-MM-dd_HH.mm.ss")
$CURTIME_FORLOG=$(Get-Date).ToString("yyyy-MM-dd_HH:mm:ss")
echo "########################################################`r`n    Running version: $VERSION`r`n    Started at: $CURTIME_FORLOG`r`n########################################################`r`n" > "$ROOTDIR\logs\triplestore_$CURTIME.log"
echo "########################################################`r`n    Running version: $VERSION`r`n    Started at: $CURTIME_FORLOG`r`n########################################################`r`n" > "$ROOTDIR\logs\compilation_$CURTIME.log"
echo "########################################################`r`n    Running version: $VERSION`r`n    Started at: $CURTIME_FORLOG`r`n########################################################`r`n" > "$ROOTDIR\logs\analysis_$CURTIME.log"


# rotate log files (delete all but last 5)
cd "$ROOTDIR/logs"
foreach( $log in ls -name | Sort-Object | out-string -stream | Select-String -Pattern triplestore_.*\.log | select -SkipLast 5) { rm "$log"}
foreach( $log in ls -name | Sort-Object | out-string -stream | Select-String -Pattern compilation_.*\.log | select -SkipLast 5) { rm "$log"}
foreach( $log in ls -name | Sort-Object | out-string -stream | Select-String -Pattern analysis_.*\.log | select -SkipLast 5) { rm "$log"}
cd $USRPATH

# treat ctrl+c as input so that we can kill subprocesses (sleep and flush to properly flush input)
[Console]::TreatControlCAsInput = $True
Start-Sleep -Seconds 1
$Host.UI.RawUI.FlushInputBuffer()

############################################################################################################
#  Triplestore
############################################################################################################

# open new terminal that tails the log file and record its PID to kill later
if ($t)
{
    $process = Start-Process powershell.exe "(Get-Host).ui.RawUI.WindowTitle='tail: Triplestore Log (feel free to close this window)'; Get-Content '$ROOTDIR\logs\..\logs\..\logs\triplestore_$CURTIME.log' -Wait -Tail 10; pause" -passthru
    $PID_TRIPLESTORE_TAIL = $process.id
}

# start the triplestore 
Write-Host -NoNewline "Starting the Triplestore"
$process = Start-Process -WindowStyle Minimized powershell.exe "(Get-Host).ui.RawUI.WindowTitle='Triplestore'; cd '$ROOTDIR\sparql_triplestore'; .\run.ps1 >> '$ROOTDIR\logs\triplestore_$CURTIME.log' 2>&1" -passthru
$PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
echo " ($process.id)"
echo "Waiting for the Triplestore to finish startup"
$ret = waitForUrlOnline $triplestore_url $process.id $SLEEP
if ($ret -eq 0) {       # OK
    echo "Triplestore running (1/3)`n"
    $PIDS_TO_KILL = $PIDS_TO_KILL + $PID_TRIPLESTORE_TAIL
} elseif ($ret -eq 1) { # startup fail
    echo "Triplestore ${RED}failed${NC} to start!"
    if ($t)
    {
        echo "See the `"tail: Triplestore Log`" powershell window for the startup log."
        echo "Or try checking the logs: `"$ROOTDIR\logs\triplestore_$CURTIME.log`""
    } else {
        echo "Try checking the logs: `"$ROOTDIR\logs\triplestore_$CURTIME.log`""
        echo "Or run again with '-t' to see the logs during startup."
    }
    abortAll
} else {                # ctrl+c pressed
    exit_killall
}


############################################################################################################
#  Compilation
############################################################################################################

# open new terminal that tails the log file and record its PID to kill later
if ($t)
{
    $process = Start-Process powershell.exe "(Get-Host).ui.RawUI.WindowTitle='tail: Compilation Log (feel free to close this window)'; Get-Content '$ROOTDIR\logs\..\logs\..\logs\compilation_$CURTIME.log' -Wait -Tail 10; pause" -passthru
    $PID_COMPILATION_TAIL = $process.id
}

# start the compilation adapter
Write-Host -NoNewline "Starting the Compilation adapter"
$process = Start-Process -WindowStyle Minimized powershell.exe "(Get-Host).ui.RawUI.WindowTitle='Compilation Adapter'; cd '$ROOTDIR\compilation' ; mvn jetty:run-exploded >> '$ROOTDIR\logs\compilation_$CURTIME.log' 2>&1" -passthru
$PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
echo " ($process.id)"
echo "Waiting for the Compilation adapter to finish startup"
$ret = waitForUrlOnline $compilation_url $process.id $SLEEP
if ($ret -eq 0) {
    echo "Compilation adapter running (2/3)`n"
    $PIDS_TO_KILL = $PIDS_TO_KILL + $PID_COMPILATION_TAIL
} elseif ($ret -eq 1) {
    echo "Compilation adapter failed to start!"
    if ($t)
    {
        echo "See the `"tail: Compilation Log`" powershell window for the startup log."
        echo "Or try checking the logs: `"$ROOTDIR\logs\compilation_$CURTIME.log`""
    } else {
        echo "Try checking the logs: `"$ROOTDIR\logs\compilation_$CURTIME.log`""
        echo "Or run again with '-t' to see the logs during startup."
    }
    abortAll
} else {
    exit_killall
}


############################################################################################################
#  Analysis
############################################################################################################

# open new terminal that tails the log file and record its PID to kill later
if ($t)
{
    $process = Start-Process powershell.exe "(Get-Host).ui.RawUI.WindowTitle='tail: Analysis Log (feel free to close this window)'; Get-Content '$ROOTDIR\logs\..\logs\..\logs\analysis_$CURTIME.log' -Wait -Tail 10; pause" -passthru
    $PID_ANALYSIS_TAIL = $process.id
}

# start the analysis adapter
Write-Host -NoNewline "Starting the Analysis adapter"
$process = Start-Process -WindowStyle Minimized powershell.exe "(Get-Host).ui.RawUI.WindowTitle='Analysis Adapter'; cd '$ROOTDIR\analysis' ; mvn jetty:run-exploded >> '$ROOTDIR\logs\analysis_$CURTIME.log' 2>&1" -passthru
$PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
echo " ($process.id)"
echo "Waiting for the Analysis adapter to finish startup"
$ret = waitForUrlOnline $analysis_url $process.id $SLEEP
if ($ret -eq 0) {
    echo "Analysis adapter running (3/3)`n"
    $PIDS_TO_KILL = $PIDS_TO_KILL + $PID_ANALYSIS_TAIL
} elseif ($ret -eq 1) {
    echo "Analysis adapter failed to start!"
    if ($t)
    {
        echo "See the `"tail: Analysis Log`" powershell window for the startup log."
        echo "Or try checking the logs: `"$ROOTDIR\logs\analysis_$CURTIME.log`""
    } else {
        echo "Try checking the logs: `"$ROOTDIR\logs\analysis_$CURTIME.log`""
        echo "Or run again with '-t' to see the logs during startup."
    }
    abortAll
} else {
    exit_killall
}

echo "Ready to go!"
echo "Use ctrl+c to exit..."

# loop to catch ctrl+c
While ($true) {
    Start-Sleep -s 1 # sleep for 1 second
    $ret = checkForCtrlC
    if ($ret -eq 1) {
        exit_killall
    }
}