#!/bin/sh

##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################

USRPATH="$PWD"                          # get the call directory
ROOTDIR="$(dirname "$(realpath "$0")")" # get the script directory

# looks up the adapter_host configuration of the Analysis adapter
# env variable UNITE_ANALYSIS_HOST overrides the configuration
# NOTE: duplicit code with shared.sh
# $1 ... root directory of the project
lookupAnalysisHost()
{
    analysis_host="$(cat "$1/conf/VeriFitAnalysis.properties" | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||")" # removes final slash in case there is one (http://host/ vs http://host)

    if [ -n "$UNITE_ANALYSIS_HOST" ]; then
        analysis_host="$UNITE_ANALYSIS_HOST"
    fi

    echo "$analysis_host"
}

# looks up the adapter_port configuration of the Analysis adapter
# env variable UNITE_ANALYSIS_PORT overrides the configuration
# NOTE: duplicit code with shared.sh
# $1 ... root directory of the project
lookupAnalysisPort()
{
    analysis_port="$(cat "$1/conf/VeriFitAnalysis.properties" | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")"

    if [ -n "$UNITE_ANALYSIS_PORT" ]; then
        analysis_port="$UNITE_ANALYSIS_PORT"
    fi

    echo "$analysis_port"
}

# lookup adapter host/port as configuration or environment variables
adapter_host="$(lookupAnalysisHost "$ROOTDIR")"
adapter_port="$(lookupAnalysisPort "$ROOTDIR")"

cd "$ROOTDIR"
exec mvn -Dadapter_port="$adapter_port" -Dadapter_host="$adapter_host" jetty:run-exploded
cd "$USRPATH"