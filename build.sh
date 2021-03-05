#!/bin/bash

##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################

USRPATH=$PWD                        # get the call directory
ROOTDIR=$(dirname $(realpath $0))   # get the script directory
cd $ROOTDIR                         # move to the script directory

# Checks if a conf file exists. If not, then a default one is created.
# $1 = file to check
# $2 = default file to copy if not found
confFileCheckOrDefault()
{
    echo -n "Checking $1: "
    if [ -f "$1" ]; then
        echo "OK"
    else
        echo "Not found"
        echo "    Creating a default one"
        cp "$2" "$1"
    fi
}


echo
echo "############################################################"
echo "    Checking configuration files"
echo "############################################################"
echo
confFileCheckOrDefault "./analysis/VeriFitAnalysis.properties" "./analysis/VeriFitAnalysisExample.properties"
confFileCheckOrDefault "./compilation/VeriFitCompilation.properties" "./compilation/VeriFitCompilationExample.properties"
confFileCheckOrDefault "./sparql_triplestore/jetty-distribution/start.ini" "./sparql_triplestore/jetty-distribution/startExample.ini"

echo
echo "############################################################"
echo "    Building and Installing shared resources"
echo "############################################################"
echo

mvn install:install-file -Dfile=$ROOTDIR/lib/cz.vutbr.fit.group.verifit.jsem_0.2.0.202103021435.jar -DgroupId=cz.vutbr.fit.group.verifit.jsem -DartifactId=jsem -Dversion=0.2.0.qualifier -Dpackaging=jar || exit "$?"

cd shared
mvn clean install || exit "$?"

echo
echo "############################################################"
echo "    Building and Installing the Compilation adapter"
echo "############################################################"
echo
cd ../compilation
mvn clean install || exit "$?"

echo
echo "############################################################"
echo "    Building and Installing the Analysis adapter"
echo "############################################################"
echo
cd ../analysis
mvn clean install || exit "$?"

echo
echo "##### ALL DONE #############################################"
echo