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

# look for a conf file
#   if found, then copy it over to the destination place
#   otherwise copy over the default conf file
# $1 = custom conf file to copy
# $2 = default conf file to copy if no custom file found
# $3 = conf file destination

function confFileCopyCustomOrDefault ()
{
    param (
        $1, $2, $3
    )
    if (Test-Path $1 -PathType Leaf) {
        echo "    using custom: $1"
        cp "$1" "$3"
    } else {
        echo "    using default: $2"
        cp "$2" "$3"
    }
}

# Check if a conf directory exists, if yes then copy its contents over to a destination. Clean the destination first
# $1 ... directory to check
# $2 ... location to copy to
function confAnalysisToolsCleanCheckAndCopy ()
{
    param (
        $1, $2
    )
    rm "$2/*"
    if (Test-Path $1) {
        echo "    copying custom: $1"
        cp "$1/*" "$2"
    } else {
        echo "    not found: $1"
    }
}

# Check if a conf directory exists, if yes then copy its contents over to destinations. Clean the destinations first
# $1 ... directory to check
# $2 ... location to copy .propertires files to
# $3 ... location to copy .java files to
function confOutputFiltersCleanCheckAndCopy ()
{
    param (
        $1, $2, $3
    )
    rm "$2/*"
    rm "$3/*"
    if (Test-Path $1) {
        echo "    copying custom: $1"
        cp "$1/*.properties" "$2"
        cp "$1/*.java" "$2"
    } else {
        echo "    not found: $1"
    }
}

function processConfFiles()
{
    # basic conf files (analysis, compilation, triplestore properties)
    echo "Checking VeriFitAnalysis.properties:"
    confFileCopyCustomOrDefault "$ROOTDIR/conf/VeriFitAnalysis.properties"    "$ROOTDIR/analysis/conf/VeriFitAnalysisDefault.properties"       "$ROOTDIR/analysis/conf/VeriFitAnalysis.properties"
    echo "Checking VeriFitCompilation.properties:"
    confFileCopyCustomOrDefault "$ROOTDIR/conf/VeriFitCompilation.properties" "$ROOTDIR/compilation/conf/VeriFitCompilationDefault.properties" "$ROOTDIR/compilation/conf/VeriFitCompilation.properties"
    echo "Checking TriplestoreConf.ini:"
    confFileCopyCustomOrDefault "$ROOTDIR/conf/TriplestoreConf.ini"           "$ROOTDIR/sparql_triplestore/startDefault.ini"                   "$ROOTDIR/sparql_triplestore/start.ini"

    # Analysis adapter advanced conf files (tool definitions, output filters)
    echo "Checking AnalysisTools"
    confAnalysisToolsCleanCheckAndCopy "$ROOTDIR/conf/analysis_advanced/AnalysisTools" "$ROOTDIR/analysis/conf/CustomAnalysisTools"
    echo "Checking PluginFilters"
    confOutputFiltersCleanCheckAndCopy "$ROOTDIR/conf/analysis_advanced/PluginFilters" "$ROOTDIR/analysis/conf/CustomPluginFiltersConfiguration" "$ROOTDIR/analysis/src/main/java/pluginFilters/customPluginFilters"
}

function main ()
{
    echo ""
    echo "############################################################"
    echo "    Processing configuration files"
    echo "############################################################"
    echo ""
    processConfFiles

    echo ""
    echo "############################################################"
    echo "    Building and Installing shared resources"
    echo "############################################################"
    echo ""

    mvn install:install-file -Dfile="$ROOTDIR\lib\cz.vutbr.fit.group.verifit.jsem_0.2.0.202103021435.jar" -DgroupId='cz.vutbr.fit.group.verifit.jsem' -DartifactId='jsem' -Dversion='0.2.0.qualifier' -Dpackaging='jar'
    if ( ! $? ) {
        exit $LastExitCode
    }

    mvn -f $ROOTDIR/shared/pom.xml clean install
    
    echo ""
    echo "############################################################"
    echo "    Building and Installing the Compilation adapter"
    echo "############################################################"
    echo ""
    mvn -f $ROOTDIR/compilation/pom.xml clean install
    if ( ! $? ) {
        exit $LastExitCode
    }
    
    echo ""
    echo "############################################################"
    echo "    Building and Installing the Analysis adapter"
    echo "############################################################"
    echo ""
    mvn -f $ROOTDIR/analysis/pom.xml clean install
    if ( ! $? ) {
        exit $LastExitCode
    }
    
    echo ""
    echo "##### ALL DONE #############################################"
    echo ""

    exit 0
}

main