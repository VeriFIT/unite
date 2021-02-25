##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################

$USRPATH=$(pwd)         # get the call directory
$ROOTDIR=$PSScriptRoot  # get the script directory
cd $ROOTDIR             # move to the script directory

# Checks if a conf file exists. If not, then a default one is created.
# $1 = file to check
# $2 = default file to copy if not found
function confFileCheckOrDefault ()
{
    param (
        $1, $2
    )
    Write-Host -NoNewline "Checking ${1}: "
    if ( Test-Path $1 -PathType Leaf) {
        echo "OK"
    } else {
        echo "Not found"
        echo "    Creating a default one"
        cp "$2" "$1"
    }
}

function main ()
{
    echo ""
    echo "############################################################"
    echo "    Checking configuration files"
    echo "############################################################"
    echo ""
    confFileCheckOrDefault "./analysis/VeriFitAnalysis.properties" "./analysis/VeriFitAnalysisExample.properties"
    confFileCheckOrDefault "./compilation/VeriFitCompilation.properties" "./compilation/VeriFitCompilationExample.properties"
    confFileCheckOrDefault "./sparql_triplestore/jetty-distribution/start.ini" "./sparql_triplestore/jetty-distribution/startExample.ini"
    
    echo ""
    echo "############################################################"
    echo "    Building and Installing shared resources"
    echo "############################################################"
    echo ""
    cd shared
    mvn clean install
    
    echo ""
    echo "############################################################"
    echo "    Building and Installing the Compilation adapter"
    echo "############################################################"
    echo ""
    cd ../compilation
    mvn clean install
    
    echo ""
    echo "############################################################"
    echo "    Building and Installing the Analysis adapter"
    echo "############################################################"
    echo ""
    cd ../analysis
    mvn clean install
    
    echo ""
    echo "##### ALL DONE #############################################"
    echo ""

    cd $USRPATH
    exit 0
}

main