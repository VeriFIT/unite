##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################

$USRPATH=$(pwd)         # get the call directory
$ROOTDIR=$PSScriptRoot  # get the script directory
cd $ROOTDIR             # move to the script directory

cd jetty-distribution
java -DFUSEKI_BASE="../triplestore" -jar start.jar