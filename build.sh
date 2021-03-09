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

$HELP="
   Loads configuration files and then builds and installs the adapter
   and its dependencies.
"
$USAGE="   Usage: $0 [-h]
      -h ... help
"

USRPATH=$PWD                        # get the call directory
ROOTDIR=$(dirname $(realpath $0))   # get the script directory


print_help() {
    echo "$HELP"
    echo "$USAGE"
    exit 0
}

# $1 ... name of the invalid arg
invalid_arg() {
    echo -e "\n   Invalid argument: $1\n"
    echo "$USAGE"
    exit 1
}

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



main () {
    # process arguments
    for arg in "$@"
    do
        case $arg in
            -h) print_help ; shift ;;
            *) invalid_arg "$arg" ;;
        esac
    done

    echo
    echo "############################################################"
    echo "    Checking configuration files"
    echo "############################################################"
    echo
    confFileCheckOrDefault "$ROOTDIR/analysis/conf/VeriFitAnalysis.properties" "$ROOTDIR/analysis/conf/VeriFitAnalysisDefault.properties"
    confFileCheckOrDefault "$ROOTDIR/compilation/conf/VeriFitCompilation.properties" "$ROOTDIR/compilation/conf/VeriFitCompilationDefault.properties"
    confFileCheckOrDefault "$ROOTDIR/sparql_triplestore/start.ini" "$ROOTDIR/sparql_triplestore/startDefault.ini"

    echo
    echo "############################################################"
    echo "    Building and Installing shared resources"
    echo "############################################################"
    echo

    mvn install:install-file -Dfile=$ROOTDIR/lib/cz.vutbr.fit.group.verifit.jsem_0.2.0.202103021435.jar -DgroupId=cz.vutbr.fit.group.verifit.jsem -DartifactId=jsem -Dversion=0.2.0.qualifier -Dpackaging=jar || exit "$?"

    mvn -f $ROOTDIR/shared/pom.xml clean install || exit "$?"

    echo
    echo "############################################################"
    echo "    Building and Installing the Compilation adapter"
    echo "############################################################"
    echo
    mvn -f $ROOTDIR/compilation/pom.xml clean install || exit "$?"

    echo
    echo "############################################################"
    echo "    Building and Installing the Analysis adapter"
    echo "############################################################"
    echo
    mvn -f $ROOTDIR/analysis/pom.xml clean install || exit "$?"

    echo
    echo "##### ALL DONE #############################################"
    echo

    exit 0
}

main "$@"