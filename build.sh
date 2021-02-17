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


echo "#########################################################"
echo "#### Building and Installing shared resources"
echo "#########################################################"
cd shared
mvn clean install
echo "#### Done ####"

echo "#########################################################"
echo "#### Building and Installing the Compilation adapter"
echo "#########################################################"
cd ../compilation
mvn clean install
echo "#### Done ####"


echo "#########################################################"
echo "#### Building and Installing the Analysis adapter"
echo "#########################################################"
cd ../analysis
mvn clean install
echo "#### Done ####"

echo -e "\n#### ALL DONE ####\n"

echo "    Make sure to create configuration files for both adapters"
echo "    by creating analysis/VeriFitAnalysis.properties and "
echo "    by creating compilation/VeriFitCompilation.properties."
echo "    Both directories contian a *Example.properties file that"
echo "    can be used as a template."
echo