#!/bin/bash
# a workaround for the oslc package being generated only once and in the core directory

cd "${BASH_SOURCE%/*}"

cp -r ../src ../compilation
cp -r ../src ../analysis
rm -r ../src


# not sure why this is needed -- missing an import 
sed -i "s|// Start of user code imports|// Start of user code imports\\nimport verifit.compilation.resources.TextOut;|" ../compilation/src/main/java/org/eclipse/lyo/oslc/domains/auto/IAutomationResult.java

sed -i "s|// Start of user code imports|// Start of user code imports\\nimport verifit.analysis.resources.TextOut;|" ../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/IAutomationResult.java


# this is because the files get generated for the compilation adapter only -- need to chenge the package for the analysis adapter
sed -i "s|import verifit.compilation.resources.FitDomainConstants;|import verifit.analysis.resources.FitDomainConstants;|" ../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/IAutomationResult.java
sed -i "s|import verifit.compilation.resources.ITextOut;|import verifit.analysis.resources.ITextOut;|" ../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/IAutomationResult.java
sed -i "s|import verifit.compilation.resources.ISUT;|import verifit.analysis.resources.ISUT;|" ../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/IAutomationResult.java

sed -i "s|import verifit.compilation.resources.FitDomainConstants;|import verifit.analysis.resources.FitDomainConstants;|" ../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/AutomationResult.java
sed -i "s|import verifit.compilation.resources.TextOut;|import verifit.analysis.resources.TextOut;|" ../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/AutomationResult.java
sed -i "s|import verifit.compilation.resources.SUT;|import verifit.analysis.resources.SUT;|" ../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/AutomationResult.java

sed -i "s|import verifit.compilation.resources.FitDomainConstants;|import verifit.analysis.resources.FitDomainConstants;|" ../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/IParameterDefinition.java
sed -i "s|import verifit.compilation.resources.FitDomainConstants;|import verifit.analysis.resources.FitDomainConstants;|" ../analysis/src/main/java/org/eclipse/lyo/oslc/domains/auto/ParameterDefinition.java