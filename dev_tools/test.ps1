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
    [switch]$l,
    [switch]$n,
    [switch]$h
)

$HELP="
   Runs the run_all script and then Postman testsuites for the adapter using Newman.
"
$USAGE="   Usage: $PSCommandPath [-h|-t|-n|-l]
      -t ... tools - Runs an additional testsuite which requires some analysis
                         tools to be installed (i.e. will not work on all machines)
      -n ... keep last N - Only runs keep_last_N test suites for both adapters. Does NOT
                           run any other test suites. Requires the adapter to be configured
                           to have keep_last_n enabled with a value of 10.
      -l ... live - Only runs the test suites without launching the adapter. Expects
                    the adapter to be running already.
      -h ... help
"

# duration for polling (via curl)
$SLEEP=3

$USRPATH=$(pwd)         # get the call directory
$ROOTDIR="$PSScriptRoot"# get the script directory
cd "$ROOTDIR"             # move to the script directory
cd ..
$ADAPTER_ROOT_DIR=$(pwd) # get the adapter root directory
cd "$USRPATH"            # move back to the user

$PIDS_TO_KILL=@()

# source shared utils
. "$ADAPTER_ROOT_DIR\dev_tools\shared.ps1"


# exits this scripts while killing all the subprocesses 
function exit_killall ()
{
    echo "`Aborting..."
    killAllWithChildren $PIDS_TO_KILL
    echo "All done."
    [Console]::TreatControlCAsInput = $False
    exit 0
}

#
# main
#
if ($args.length -ne 0) {
    invalid_arg $args[0] $USAGE
}
if ($h) {
    print_help $HELP $USAGE
}


echo "`nNOTE:`n  MAKE SURE THAT THE ADAPTER IS RUNNING ON THE DEFAULT PORTS!`n  THE TESTSUITE CURRENTLY HAS PORTS HARDCODED INSIDE IT`n" # TODO

echo "`nNOTE2:`n  If this script gets killed prematurely, then multiple minimized`n  powershell windows stay open and need to be closed by hand`n" # TODO

# lookup analysis adapter config
$analysis_url = lookupAnalysisURL "$ADAPTER_ROOT_DIR"



if ( ! $l ) {
    
    # treat ctrl+c as input so that we can kill subprocesses (sleep and flush to properly flush input)
    # used to catch it and do our own handeling
    [Console]::TreatControlCAsInput = $True
    Start-Sleep -Seconds 1
    $Host.UI.RawUI.FlushInputBuffer()

    echo "Booting up Unite"
    $process = Start-Process powershell.exe "(Get-Host).ui.RawUI.WindowTitle='Unite'; & '$ADAPTER_ROOT_DIR\run_all.ps1' ; pause" -passthru
    $PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
    $ret = waitForUrlOnline $analysis_url $process.id $SLEEP  # poll the analysis adapter because that one starts last in the run script
    if ($ret -eq 0) {
        echo "Adapter up and running`n"
    } elseif ($ret -eq 1) {
        echo "Adapter failed to start!\n" # TODO give more details?
        echo "`n`n  TESTS FAILED\n"
        exit 1
    } else {
        # interrupted by ctrl+c
        exit_killall
    }

    # dont treat ctrl+c as input anymore
    [Console]::TreatControlCAsInput = $False # TODO the adapter will stay hanging if the test suite is killed

} else {
    echo "Skipping Adapter boot. Adapter expected to be running already."

    # make sure configuration files exist
    checkConfFiles "$ADAPTER_ROOT_DIR"
}


$compilationRes=$true
$compilationKeepLastNRes=$true

$analysisRes=$true
$analysisToolsRes=$true
$analysisKeepLastNRes=$true


if ( ! $n) {
    echo ""
    echo "Running Compilation adapter test suite" 
    $clock = [Diagnostics.Stopwatch]::StartNew()
    newman run "$ADAPTER_ROOT_DIR\compilation\tests\TestSuite.postman_collection"
    $compilationRes=$?
    $clock.Stop()
    echo $clock.Elapsed

    echo ""
    echo "Running Analysis adapter test suite" 
    $clock = [Diagnostics.Stopwatch]::StartNew()
    newman run "$ADAPTER_ROOT_DIR\analysis\tests\TestSuite.postman_collection"
    $analysisRes=$?
    $clock.Stop()
    echo $clock.Elapsed

    echo ""
    if ($t) {
        echo "Running Analysis adapter Tested Tools test suite" 
        $clock = [Diagnostics.Stopwatch]::StartNew()
        newman run "$ADAPTER_ROOT_DIR\analysis\tests\TestSuite_TestedTools.postman_collection"
        $analysisToolsRes=$?
        $clock.Stop()
        echo $clock.Elapsed
    } else {
        echo "Skipping Analysis adapter Tested Tools test suite" 
    }   

    echo ""
    echo "Skipping keep_last_n test suites." 

} else {
    echo ""
    echo "Skipping all test suites except the keep_last_n test suites" 

    echo ""
    echo "Running Compilation adapter keep_last_n test suite" 
    $clock = [Diagnostics.Stopwatch]::StartNew()
    newman run "$ADAPTER_ROOT_DIR\compilation\tests\TestSuite_KeepLastN.postman_collection"
    $compilationKeepLastNRes=$?
    $clock.Stop()
    echo $clock.Elapsed

    echo ""
    echo "Running Analysis adapter keep_last_n test suite" 
    $clock = [Diagnostics.Stopwatch]::StartNew()
    newman run "$ADAPTER_ROOT_DIR\analysis\tests\TestSuite_KeepLastN.postman_collection"
    $analysisKeepLastNRes=$?
    $clock.Stop()
    echo $clock.Elapsed
}

# shutdown the adapter
killAllWithChildren $PIDS_TO_KILL

# return non-zero if there were failed tests
if ( $compilationRes -ne $true -Or $analysisRes -ne $true -Or $analysisToolsRes -ne $true -Or $compilationKeepLastNRes -ne $true -Or $analysisKeepLastNRes -ne $true) {
    echo "`n`n  TESTS FAILED`n"
    exit 1
} else {
    echo "`n`n  TESTS PASSED`n"
    exit 0
}