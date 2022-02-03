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
    [switch]$h
)

$HELP="
   Loads configuration files and then builds and installs the adapter
   and its dependencies.
"
$USAGE="   Usage: $PSCommandPath [-h]
      -h ... help
"

$USRPATH=$(pwd)         # get the call directory
$ROOTDIR="$PSScriptRoot"  # get the script directory

# source shared utils
. "$ROOTDIR\dev_tools\shared.ps1"


#
# main
#
if ($args.length -ne 0) {
    invalid_arg $args[0] $USAGE
}
if ($h) {
    print_help $HELP $USAGE
}

echo ""
echo "############################################################"
echo "    Processing configuration files"
echo "############################################################"
echo ""
processConfFiles "$ROOTDIR"

echo ""
echo "############################################################"
echo "    Building and Installing shared resources"
echo "############################################################"
echo ""

mvn -f "$ROOTDIR\domain\pom.xml" clean install
if ( ! $? ) {
    exit $LastExitCode
}
mvn -f "$ROOTDIR\shared\pom.xml" clean install
if ( ! $? ) {
    exit $LastExitCode
}

echo ""
echo "############################################################"
echo "    Building and Installing the Compilation adapter"
echo "############################################################"
echo ""
mvn -f "$ROOTDIR\compilation\pom.xml" clean install
if ( ! $? ) {
    exit $LastExitCode
}

echo ""
echo "############################################################"
echo "    Building and Installing the Analysis adapter"
echo "############################################################"
echo ""
mvn -f "$ROOTDIR\analysis\pom.xml" clean install
if ( ! $? ) {
    exit $LastExitCode
}

echo ""
echo "##### BUILD DONE ###########################################"
echo ""

exit 0