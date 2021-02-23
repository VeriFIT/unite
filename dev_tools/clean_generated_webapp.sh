#!/bin/bash
# deletes all Lyo generated files
# except the AdapterManager as that is the one with manual changes

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

rm -r ../analysis/src/main/webapp/static
rm -r ../analysis/src/main/webapp/cz
rm -r ../analysis/src/main/webapp/swagger-ui
rm -r ../analysis/src/main/webapp/index.jsp

rm -r ../compilation/src/main/webapp/static
rm -r ../compilation/src/main/webapp/cz
rm -r ../compilation/src/main/webapp/swagger-ui
rm -r ../compilation/src/main/webapp/index.jsp
