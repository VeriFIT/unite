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

HELP="
   Loads configuration files and then builds and installs the adapter
   and its dependencies.
"
USAGE="   Usage: $0 [-h]
      -h ... help
"

USRPATH=$PWD                        # get the call directory
ROOTDIR=$(dirname $(realpath $0))   # get the script directory

# source shared utils
source $ROOTDIR/dev_tools/shared.sh

main () {
    # process arguments
    for arg in "$@"
    do
        case $arg in
            -h) print_help "$HELP" "$USAGE"; shift ;;
            *) invalid_arg "$arg" "$USAGE" ;;
        esac
    done

    echo
    echo "############################################################"
    echo "    Processing configuration files"
    echo "############################################################"
    echo
    processConfFiles "$ROOTDIR"

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