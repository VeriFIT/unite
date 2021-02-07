#!/bin/bash
# run this after generating a new model from Lyo to make the code compilable (fix generation bugs or invalid config etc..)

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

./fix_move_domains.sh
./fix_getters.sh
./fix_redo_modifs.sh