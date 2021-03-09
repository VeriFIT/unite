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
$SLEEP=3

$USRPATH=$(pwd)         # get the call directory
$ROOTDIR=$PSScriptRoot  # get the script directory
cd $ROOTDIR             # move to the script directory
cd ..
$ADAPTER_ROOT_DIR=$(pwd) # get the adapter root directory
cd $USRPATH              # move back to the user

$PIDS_TO_KILL=@()

# source shared utils
. $ADAPTER_ROOT_DIR/dev_tools/shared.ps1

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
    echo "Booting up the Universal Analysis Adapter"
    $process = Start-Process -WindowStyle Minimized powershell.exe "(Get-Host).ui.RawUI.WindowTitle='Universal Analysis Adapter'; $ADAPTER_ROOT_DIR/run_all.ps1" -passthru
    $PIDS_TO_KILL = $PIDS_TO_KILL + $process.id
    $ret = curl_poll $analysis_url $SLEEP  # poll the analysis adapter because that one starts last in the run script
    echo "Adapter up and running`n"
} else {
    echo "Skipping Adapter boot. Adapter expected to be running already."

    # make sure configuration files exist
    checkConfFiles "$ADAPTER_ROOT_DIR"
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
killAllWithChildren $PIDS_TO_KILL

# return non-zero if there were failed tests
if ( $compilationRes -ne $true -Or $analysisRes -ne $true -Or $analysisToolsRes -ne $true) {
    echo "`n`n  TESTS FAILED`n"
    exit 1
} else {
    echo "`n`n  TESTS PASSED`n"
    exit 0
}