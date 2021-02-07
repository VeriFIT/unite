#!/bin/bash
# a workaround for the oslc package being generated only once and in the core directory

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

cp -r ../src ../compilation
cp -r ../src ../analysis
rm -r ../src

# this is because the files get generated for the compilation adapter only -- need to chenge the package for the analysis adapter
sed -i "s|import verifit.compilation.resources.FitDomainConstants;|import verifit.analysis.resources.FitDomainConstants;|" \
../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/IAutomationResult.java
sed -i "s|import verifit.compilation.resources.ISUT;|import verifit.analysis.resources.ISUT;|" \
../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/IAutomationResult.java
sed -i "s|import verifit.compilation.resources.FitDomainConstants;|import verifit.analysis.resources.FitDomainConstants;|" \
../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/AutomationResult.java
sed -i "s|import verifit.compilation.resources.SUT;|import verifit.analysis.resources.SUT;|" \
../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/AutomationResult.java

sed -i "s|import verifit.compilation.resources.FitDomainConstants;|import verifit.analysis.resources.FitDomainConstants;|" \
../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/IContribution.java
sed -i "s|import verifit.compilation.resources.FitDomainConstants;|import verifit.analysis.resources.FitDomainConstants;|" \
../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/Contribution.java

sed -i "s|import verifit.compilation.resources.FitDomainConstants;|import verifit.analysis.resources.FitDomainConstants;|" \
../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/IParameterDefinition.java
sed -i "s|import verifit.compilation.resources.FitDomainConstants;|import verifit.analysis.resources.FitDomainConstants;|" \
../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/ParameterDefinition.java