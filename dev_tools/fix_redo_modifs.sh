#!/bin/bash
# redo changes in places that are not protected agains code generation

##########################
# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
##########################

USRPATH=$PWD                        # get the call directory
ROOTDIR=$(dirname $(realpath $0))   # get the script directory
cd $ROOTDIR                         # move to the script directory

# */resources contains store.properties which we dont use and load properties from elsewhere
rm -rf ../analysis/src/main/resources/
rm -rf ../compilation/src/main/resources/

# both Managers have a Store declared in a different way then we want
sed -i 's|Properties lyoStoreProperties = new Properties();|/* Unwanted generated code\n\t\tProperties lyoStoreProperties = new Properties();|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/VeriFitAnalysisManager.java
sed -i 's|int initialPoolSize = Integer.parseInt(lyoStoreProperties.getProperty("initialPoolSize"));|*/\n\t\tint initialPoolSize = 100; // TODO|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/VeriFitAnalysisManager.java
sed -i 's|defaultNamedGraph = new URI(lyoStoreProperties.getProperty("defaultNamedGraph"));|defaultNamedGraph = new URI(VeriFitAnalysisProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES);|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/VeriFitAnalysisManager.java
sed -i 's|sparqlQueryEndpoint = new URI(lyoStoreProperties.getProperty("sparqlQueryEndpoint"));|sparqlQueryEndpoint = new URI(VeriFitAnalysisProperties.SPARQL_SERVER_QUERY_ENDPOINT);|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/VeriFitAnalysisManager.java
sed -i 's|sparqlUpdateEndpoint = new URI(lyoStoreProperties.getProperty("sparqlUpdateEndpoint"));|sparqlUpdateEndpoint = new URI(VeriFitAnalysisProperties.SPARQL_SERVER_UPDATE_ENDPOINT);|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/VeriFitAnalysisManager.java

sed -i 's|Properties lyoStoreProperties = new Properties();|/* Unwanted generated code\n\t\tProperties lyoStoreProperties = new Properties();|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/VeriFitCompilationManager.java
sed -i 's|int initialPoolSize = Integer.parseInt(lyoStoreProperties.getProperty("initialPoolSize"));|*/\n\t\tint initialPoolSize = 100; // TODO|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/VeriFitCompilationManager.java
sed -i 's|defaultNamedGraph = new URI(lyoStoreProperties.getProperty("defaultNamedGraph"));|defaultNamedGraph = new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES);|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/VeriFitCompilationManager.java
sed -i 's|sparqlQueryEndpoint = new URI(lyoStoreProperties.getProperty("sparqlQueryEndpoint"));|sparqlQueryEndpoint = new URI(VeriFitCompilationProperties.SPARQL_SERVER_QUERY_ENDPOINT);|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/VeriFitCompilationManager.java
sed -i 's|sparqlUpdateEndpoint = new URI(lyoStoreProperties.getProperty("sparqlUpdateEndpoint"));|sparqlUpdateEndpoint = new URI(VeriFitCompilationProperties.SPARQL_SERVER_UPDATE_ENDPOINT);|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/VeriFitCompilationManager.java


# override authentication configuration
sed -i 's|private static final Boolean ignoreResourceProtection = false;|private static final Boolean ignoreResourceProtection = !(VeriFitAnalysisProperties.AUTHENTICATION_ENABLED);|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/servlet/CredentialsFilter.java

sed -i 's|private static final Boolean ignoreResourceProtection = false;|private static final Boolean ignoreResourceProtection = !(VeriFitCompilationProperties.AUTHENTICATION_ENABLED);|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/servlet/CredentialsFilter.java


# my POST handler throws and exception, and modifies aResource
sed -i 's|public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource)|public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, AutomationRequest aResource) throws OslcResourceException|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/VeriFitAnalysisManager.java

sed -i 's|public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource)|public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, AutomationRequest aResource) throws OslcResourceException|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/VeriFitCompilationManager.java

# catch the added exception and send an error response
sed -i 's|AutomationRequest newResource = VeriFitAnalysisManager.createAutomationRequest(httpServletRequest, aResource);|try {\n\t\tAutomationRequest newResource = VeriFitAnalysisManager.createAutomationRequest(httpServletRequest, aResource);|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/services/ServiceProviderService1.java
sed -i 's|return Response.created(newResource.getAbout()).entity(newResource).header(VeriFitAnalysisConstants.HDR_OSLC_VERSION, VeriFitAnalysisConstants.OSLC_VERSION_V2).build();|return Response.created(newResource.getAbout()).entity(newResource).header(VeriFitAnalysisConstants.HDR_OSLC_VERSION, VeriFitAnalysisConstants.OSLC_VERSION_V2).build();\n\t\t} catch (OslcResourceException e) {\n\t\tError errorResource = new Error();\n\t\t\terrorResource.setStatusCode(\"400\");\n\t\t\terrorResource.setMessage(e.getMessage());\n\t\t\treturn Response.status(400).entity(errorResource).build();\n\t\t}|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/services/ServiceProviderService1.java

sed -i 's|AutomationRequest newResource = VeriFitCompilationManager.createAutomationRequest(httpServletRequest, aResource);|try {\n\t\tAutomationRequest newResource = VeriFitCompilationManager.createAutomationRequest(httpServletRequest, aResource);|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/services/ServiceProviderService1.java
sed -i 's|return Response.created(newResource.getAbout()).entity(newResource).header(VeriFitCompilationConstants.HDR_OSLC_VERSION, VeriFitCompilationConstants.OSLC_VERSION_V2).build();|return Response.created(newResource.getAbout()).entity(newResource).header(VeriFitCompilationConstants.HDR_OSLC_VERSION, VeriFitCompilationConstants.OSLC_VERSION_V2).build();\n\t\t} catch (OslcResourceException e) {\n\t\tError errorResource = new Error();\n\t\t\terrorResource.setStatusCode(\"400\");\n\t\t\terrorResource.setMessage(e.getMessage());\n\t\t\treturn Response.status(400).entity(errorResource).build();\n\t\t}|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/services/ServiceProviderService1.java


# my update handlers modify aResource
sed -i 's|public static AutomationRequest updateAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String id) {|public static AutomationRequest updateAutomationRequest(HttpServletRequest httpServletRequest, AutomationRequest aResource, final String id) {|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/VeriFitAnalysisManager.java
sed -i 's|public static AutomationResult updateAutomationResult(HttpServletRequest httpServletRequest, final AutomationResult aResource, final String id) {|public static AutomationResult updateAutomationResult(HttpServletRequest httpServletRequest, AutomationResult aResource, final String id) {|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/VeriFitAnalysisManager.java
sed -i 's|public static Contribution updateContribution(HttpServletRequest httpServletRequest, final Contribution aResource, final String id) {|public static Contribution updateContribution(HttpServletRequest httpServletRequest, Contribution aResource, final String id) {|' \
../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/VeriFitAnalysisManager.java


sed -i 's|public static AutomationRequest updateAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String id) {|public static AutomationRequest updateAutomationRequest(HttpServletRequest httpServletRequest, AutomationRequest aResource, final String id) {|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/VeriFitCompilationManager.java
sed -i 's|public static AutomationResult updateAutomationResult(HttpServletRequest httpServletRequest, final AutomationResult aResource, final String id) {|public static AutomationResult updateAutomationResult(HttpServletRequest httpServletRequest, AutomationResult aResource, final String id) {|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/VeriFitCompilationManager.java
sed -i 's|public static SUT updateSUT(HttpServletRequest httpServletRequest, final SUT aResource, final String id) {|public static SUT updateSUT(HttpServletRequest httpServletRequest, SUT aResource, final String id) {|' \
../compilation/src/main/java/cz/vutbr/fit/group/verifit/oslc/compilation/VeriFitCompilationManager.java



# change swager annotations for my custom octet-stream endpoints # DONT KNOW HOW TO MATCH THE RIGHT ONES
#sed -i 's|produces = OslcMediaType.APPLICATION_RDF_XML + ", " + OslcMediaType.APPLICATION_XML + ", " + OslcMediaType.APPLICATION_JSON + ", " + OslcMediaType.TEXT_TURTLE + ", " + MediaType.TEXT_HTML + ", " + OslcMediaType.APPLICATION_X_OSLC_COMPACT_XML\n    )\n    public Contribution getContribution|produces = OslcMediaType.APPLICATION_RDF_XML + ", " + OslcMediaType.APPLICATION_XML + ", " + OslcMediaType.APPLICATION_JSON + ", " + OslcMediaType.TEXT_TURTLE + ", " + MediaType.TEXT_HTML + ", " + OslcMediaType.APPLICATION_X_OSLC_COMPACT_XML + ", " + MediaType.APPLICATION_OCTET_STREAM + ", " + MediaType.TEXT_PLAIN\n    )\n    public Contribution getContribution|' \
#../analysis/src/main/java/cz/vutbr/fit/group/verifit/oslc/analysis/services/Contributions.java
          