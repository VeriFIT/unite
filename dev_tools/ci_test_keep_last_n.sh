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
cd ..
ADAPTER_ROOT_DIR=$PWD               # get the adapter root directory
cd $ROOTDIR                         # move back to the script directory


main () {
    # build 
    echo -e "Building\n"
    $ADAPTER_ROOT_DIR/build.sh &>/dev/null
    ret=$?
    if [ $ret -ne 0 ]; then
        failed $ret
    fi
    echo -e "done\n"

    # change configuration to enable keep_last_n
    echo -e "Changing configuration to keep_last_n 10\n"
    cp $ADAPTER_ROOT_DIR/analysis/conf/VeriFitAnalysisDefault.properties $ADAPTER_ROOT_DIR/conf/VeriFitAnalysis.properties    
    sed -i 's|keep_last_n_enabled=false|keep_last_n_enabled=true|' $ADAPTER_ROOT_DIR/conf/VeriFitAnalysis.properties
    sed -i 's|keep_last_n=100|keep_last_n=10|' $ADAPTER_ROOT_DIR/conf/VeriFitAnalysis.properties

    cp $ADAPTER_ROOT_DIR/compilation/conf/VeriFitCompilationDefault.properties $ADAPTER_ROOT_DIR/conf/VeriFitCompilation.properties 
    sed -i 's|keep_last_n_enabled=false|keep_last_n_enabled=true|' $ADAPTER_ROOT_DIR/conf/VeriFitCompilation.properties
    sed -i 's|keep_last_n=100|keep_last_n=10|' $ADAPTER_ROOT_DIR/conf/VeriFitCompilation.properties

    # run tests
    echo -e "Running test script\n"
    $ADAPTER_ROOT_DIR/dev_tools/test.sh -n -ci
    exit $?
}

main "$@"