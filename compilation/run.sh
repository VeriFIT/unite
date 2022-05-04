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


# looks up the adapter_host configuration of the Compilation adapter
# env variable UNITE_COMPILATION_HOST overrides the configuration
# NOTE: duplicit code with shared.sh
# $1 ... root directory of the project
lookupCompilationHost()
{
    compilation_host="$(cat "$1/conf/VeriFitCompilation.properties" | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||")" # removes final slash in case there is one (http://host/ vs http://host)

    if [ -n "$UNITE_COMPILATION_HOST" ]; then
        compilation_host="$UNITE_COMPILATION_HOST"
    fi

    echo "$compilation_host"
}

# looks up the adapter_port configuration of the Compilation adapter
# env variable UNITE_COMPILATION_PORT overrides the configuration
# NOTE: duplicit code with shared.sh
# $1 ... root directory of the project
lookupCompilationPort()
{
    compilation_port="$(cat "$1/conf/VeriFitCompilation.properties" | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")"

    if [ -n "$UNITE_COMPILATION_PORT" ]; then
        compilation_port="$UNITE_COMPILATION_PORT"
    fi

    echo "$compilation_port"
}


# lookup adapter host/port as configuration or environment variables
adapter_host="$(lookupCompilationHost "$ROOTDIR")"
adapter_port="$(lookupCompilationPort "$ROOTDIR")"

cd "$ROOTDIR"
exec mvn -Dadapter_port="$adapter_port" -Dadapter_host="$adapter_host" jetty:run-exploded
cd "$USRPATH"