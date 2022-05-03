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

# TODO env variables and load config

exec mvn -Dadapter_port="$adapter_port" -Dadapter_host="$adapter_host" jetty:run-exploded
