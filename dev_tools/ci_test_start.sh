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


# polls an address using curl until the request returns zero (i.e. the address responds)
# return - 1 if timouted, otherwise 0
# $1 ... address - URL for curl to poll
# $2 ... timeout in cycles
curl_poll() {
    curl_ret=42
    counter=0
    while [ $curl_ret != 0 ]
    do
        sleep 3
        echo -n "."
        curl $1 &> /dev/null
        curl_ret=$?

        if [ "$counter" -ge "$2" ]; then
            return 1
        fi

        counter=$((counter+1))
    done
    echo
    return 0
}

# $1 ... exit code
failed() {
    echo "Failed!\n"
    exit $1
}


main () {
    # build 
    echo -e "Building\n"
    $ADAPTER_ROOT_DIR/build.sh &>/dev/null

    # lookup triplestore config
    triplestore_host=$(cat $ADAPTER_ROOT_DIR/sparql_triplestore/start.ini | grep "^ *jetty.http.host=" | sed "s/^ *jetty.http.host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
    triplestore_port=$(cat $ADAPTER_ROOT_DIR/sparql_triplestore/start.ini | grep "^ *jetty.http.port=" | sed "s/^ *jetty.http.port=//")
    triplestore_url="$triplestore_host:$triplestore_port/fuseki/"
    # lookup compilation adapter config
    compilation_host=$(cat $ADAPTER_ROOT_DIR/compilation/conf/VeriFitCompilation.properties | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
    compilation_port=$(cat $ADAPTER_ROOT_DIR/compilation/conf/VeriFitCompilation.properties | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")
    compilation_url="$compilation_host:$compilation_port/compilation/"
    # lookup analysis adapter config
    analysis_host=$(cat $ADAPTER_ROOT_DIR/analysis/conf/VeriFitAnalysis.properties | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
    analysis_port=$(cat $ADAPTER_ROOT_DIR/analysis/conf/VeriFitAnalysis.properties | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")
    analysis_url="$analysis_host:$analysis_port/analysis/"


    echo -e "Booting\n"
    $ADAPTER_ROOT_DIR/run_all.sh &>/dev/null &

    echo "triplestore"
    curl_poll "$triplestore_url" 40 # 40 * 3 = 120 seconds
    ret=$?
    if [ $ret -ne 0 ]; then
        tail logs/triplestore*
        failed $ret
    fi
    echo -e "running\n"

    echo "compilation"
    curl_poll "$compilation_url" 40
    ret=$?
    if [ $ret -ne 0 ]; then
        tail logs/compilation*
        failed $ret
    fi
    echo -e "running\n"

    echo "analysis"
    curl_poll "$analysis_url" 40
    ret=$?
    if [ $ret -ne 0 ]; then
        tail logs/analysis*
        failed $ret
    fi
    echo -e "running\n"

    echo "Success!"

    exit 0
}

main "$@"