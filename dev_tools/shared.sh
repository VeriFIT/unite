##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################


# $1 ... exit code
failed()
{
    echo "Failed!\n"
    exit $1
}

# look for a conf file
#   if found, then copy it over to the destination place
#   otherwise copy over the default conf file
# $1 = custom conf file to copy
# $2 = default conf file to copy if no custom file found
# $3 = conf file destination
confFileCopyCustomOrDefault()
{
    
    if [ -f "$1" ]; then
        echo "    using custom: $1"
        cp "$1" "$3"
    else
        echo "    using default: $2"
        cp "$2" "$3"
    fi
}

# Check if a conf directory exists, if yes then copy its contents over to a destination. Clean the destination first
# $1 ... directory to check
# $2 ... location to copy to
confAnalysisToolsCleanCheckAndCopy()
{
    rm -rf "$2"
    if [ -d "$1" ]; then
        echo "    using custom: $1"
        cp -r "$1" "$2"
    else
        echo "    no custom conf found in $1"
    fi
}

# Check if a conf directory exists, if yes then copy its contents over to destinations. Clean the destinations first
# $1 ... directory to check
# $2 ... location to copy .propertires files to
# $3 ... location to copy .java files to
confOutputFiltersCleanCheckAndCopy()
{
    rm -rf "$2"
    rm -rf "$3"
    if [ -d "$1" ]; then
        echo "    using custom: $1"
        mkdir "$2"
        cp $1/*.properties "$2"
        mkdir "$3"
        cp $1/*.java "$3"
    else
        echo "    no custom conf found in $1"
    fi
}

# $1 ... root directory of the project
processConfFiles()
{    
    # basic conf files (analysis, compilation, triplestore properties)
    echo "Checking VeriFitAnalysis.properties:"
    confFileCopyCustomOrDefault "$1/conf/VeriFitAnalysis.properties"    "$1/analysis/conf/VeriFitAnalysisDefault.properties"       "$1/analysis/conf/VeriFitAnalysis.properties"
    echo "Checking VeriFitCompilation.properties:"
    confFileCopyCustomOrDefault "$1/conf/VeriFitCompilation.properties" "$1/compilation/conf/VeriFitCompilationDefault.properties" "$1/compilation/conf/VeriFitCompilation.properties"
    echo "Checking TriplestoreConf.ini:"
    confFileCopyCustomOrDefault "$1/conf/TriplestoreConf.ini"           "$1/sparql_triplestore/startDefault.ini"                   "$1/sparql_triplestore/start.ini"

    # Analysis adapter advanced conf files (tool definitions, output filters)
    echo "Checking AnalysisTools"
    confAnalysisToolsCleanCheckAndCopy "$1/conf/analysis_advanced/AnalysisTools" "$1/analysis/conf/CustomAnalysisTools"
    echo "Checking PluginFilters"
    confOutputFiltersCleanCheckAndCopy "$1/conf/analysis_advanced/PluginFilters" "$1/analysis/conf/CustomPluginFiltersConfiguration" "$1/analysis/src/main/java/pluginFilters/customPluginFilters"
}



# $1 ... root directory of the project
lookupTriplestoreURL()
{
    triplestore_host=$(cat $1/sparql_triplestore/start.ini | grep "^ *jetty.http.host=" | sed "s/^ *jetty.http.host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
    triplestore_port=$(cat $1/sparql_triplestore/start.ini | grep "^ *jetty.http.port=" | sed "s/^ *jetty.http.port=//")
    triplestore_url="$triplestore_host:$triplestore_port/fuseki/"
    echo "$triplestore_url"
}
    
# $1 ... root directory of the project
lookupCompilationURL()
{
    compilation_host=$(cat $1/compilation/conf/VeriFitCompilation.properties | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
    compilation_port=$(cat $1/compilation/conf/VeriFitCompilation.properties | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")
    compilation_url="$compilation_host:$compilation_port/compilation/"
    echo "$compilation_url"
}

# $1 ... root directory of the project
lookupAnalysisURL()
{
    analysis_host=$(cat $1/analysis/conf/VeriFitAnalysis.properties | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
    analysis_port=$(cat $1/analysis/conf/VeriFitAnalysis.properties | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")
    analysis_url="$analysis_host:$analysis_port/analysis/"
    echo "$analysis_url"
}

# Check that all required configuration files exist.
# Outputs an error message and exits the script if 
# a conf file is missing.
# $1 ... root directory of the project
checkConfFiles()
{
    if [ ! -f "$1/analysis/conf/VeriFitAnalysis.properties" ]; then
        echo -e "ERROR: Configuration file \"$1/analysis/conf/VeriFitAnalysis.properties\" not found."
        echo -e "  The adapter needs to be configured to be able to run!"
        echo    "  Run the build script first or use the -b option."
        exit 1
    fi
    if [ ! -f "$1/compilation/conf/VeriFitCompilation.properties" ]; then
        echo -e "ERROR: Configuration file \"$1/compilation/conf/VeriFitCompilation.properties\" not found."
        echo -e "  The adapter needs to be configured to be able to run!"
        echo    "  Run the build script first or use the -b option."
        exit 1
    fi
    if [ ! -f "$1/sparql_triplestore/start.ini" ]; then
        echo -e "ERROR: Configuration file \"$1/sparql_triplestore/start.ini\" not found."
        echo -e "  The adapter needs to be configured to be able to run!"
        echo    "  Run the build script first or use the -b option."
        exit 1
    fi
}

# polls an address using curl until the request returns True (i.e. the address responds)
# $1 ... URL for curl to poll
# $2 ... sleep duration between polls
# $3 ... timeout in cycles, zero means no timeout
curl_poll()
{
    curl_ret=42
    counter=0
    while [ $curl_ret != 0 ]
    do
        sleep $2
        echo -n "."
        curl $1 &> /dev/null
        curl_ret=$?

        if [ "$3" -ne "0" ] && [ "$counter" -ge "$3" ]; then # only check timout if there is one
            return 1
        fi

        counter=$((counter+1))
    done
    return 0
}

# $1 ... help
# $2 ... usage
print_help()
{
    echo "$1"
    echo "$2"
    exit 0
}

# $1 ... name of the invalid arg
# $2 ... usage
invalid_arg()
{
    echo -e "\n   Invalid argument: ${1} \n"
    echo "$2"
    exit 1
}