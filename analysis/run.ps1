##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################

$USRPATH=$(pwd)           # get the call directory
$ROOTDIR="$PSScriptRoot"  # get the script directory

# We need to set env variables UNITE_ANALYSIS_PORT and UNITE_ANALYSIS_HOST
# for maven to configure the webapp correctly.
# If these env variables are already defined by the user, then keep them as is.
# If not defined, then load them from .properties configuration.
$content = Get-Content "$ROOTDIR\conf\VeriFitAnalysis.properties" -raw
$hashTable = ConvertFrom-StringData -StringData $content
if ($null -ne $env:UNITE_ANALYSIS_PORT) { # if not defined
    $adapter_port = $env:UNITE_ANALYSIS_PORT
} else {
    $adapter_port = $hashTable.adapter_port
}
if ($null -ne $env:UNITE_ANALYSIS_HOST) {
    $adapter_host = $env:UNITE_ANALYSIS_HOST
} else {
    $adapter_host = $hashTable.adapter_host
}

mvn -Dadapter_port="$adapter_port" -Dadapter_host="$adapter_host" jetty:run-exploded