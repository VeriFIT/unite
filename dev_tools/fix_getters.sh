#!/bin/bash
# fixes a Lyo bug (probably a bug) - getters return a HashSet instead of a Set

##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################

cd "${BASH_SOURCE%/*}"

cd ../analysis/src/main/java/org/eclipse/lyo/oslc/domains
find . -type f -exec sed -i "s/public HashSet/public Set/g" {} \;
cd - 1> /dev/null
cd ../analysis/src/main/java/verifit/analysis/resources
find . -type f -exec sed -i "s/public HashSet/public Set/g" {} \;
cd - 1> /dev/null
cd ../compilation/src/main/java/org/eclipse/lyo/oslc/domains
find . -type f -exec sed -i "s/public HashSet/public Set/g" {} \;
cd - 1> /dev/null
cd ../compilation/src/main/java/verifit/compilation/resources
find . -type f -exec sed -i "s/public HashSet/public Set/g" {} \;
