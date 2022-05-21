// Start of user code Copyright
/* ## License for manual implementation (enclosed in "Start/End user code ..." tags) ##
 *
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/* ## License for generated code: ## */
/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Simple
 */
// End of user code

package cz.vutbr.fit.group.verifit.oslc.analysis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContextEvent;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.lyo.oslc4j.core.model.ServiceProvider;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import cz.vutbr.fit.group.verifit.oslc.analysis.servlet.ServiceProviderCatalogSingleton;
import cz.vutbr.fit.group.verifit.arrowhead.client.ArrowheadClient;
import cz.vutbr.fit.group.verifit.arrowhead.client.ArrowheadClientBuilder;
import cz.vutbr.fit.group.verifit.arrowhead.client.ArrowheadServiceRegistryClient;
import cz.vutbr.fit.group.verifit.arrowhead.dto.ArrowheadServiceRegistrationForm;
import cz.vutbr.fit.group.verifit.oslc.OslcValues;
import cz.vutbr.fit.group.verifit.oslc.analysis.ServiceProviderInfo;
import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc.domains.auto.Contribution;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc.domains.auto.ParameterInstance;
import org.eclipse.lyo.oslc.domains.Person;
import cz.vutbr.fit.group.verifit.oslc.domain.SUT;
import java.net.URI;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.NoSuchElementException;
import org.eclipse.lyo.store.ModelUnmarshallingException;
import org.eclipse.lyo.store.Store;
import org.eclipse.lyo.store.StorePool;
import org.eclipse.lyo.store.StoreAccessException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;




// Start of user code imports
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils.ResourceIdGen;
import cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution.ExecutionManager;
import cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution.ExecutionParameter;
import cz.vutbr.fit.group.verifit.oslc.shared.exceptions.OslcResourceException;
import cz.vutbr.fit.group.verifit.oslc.shared.queuing.RequestRunnerQueues;

import cz.vutbr.fit.group.verifit.oslc.analysis.properties.VeriFitAnalysisProperties;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager.AutomationPlanConf;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanLoading;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationRequestExecution.SutAnalyse;
import cz.vutbr.fit.group.verifit.oslc.analysis.clients.CompilationAdapterClient;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.IFilter;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.FilterManager;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lyo.oslc4j.core.model.Link;
// End of user code

// Start of user code pre_class_code
// End of user code

public class VeriFitAnalysisManager {

    private static final Logger log = LoggerFactory.getLogger(VeriFitAnalysisManager.class);

    private static StorePool storePool;
    
    // Start of user code class_attributes

	static ResourceIdGen AutoPlanIdGen;
	static ResourceIdGen AutoRequestIdGen;
	
	static ExecutionManager AutoRequestExecManager;

    // End of user code
    
    
    // Start of user code class_methods
	
	private static long getCurrentHighestAutomationRequestId() throws Exception
	{
		// first try to get the next ID from the bookmark resource
		try {
			AutomationRequest bookmarkReq = getAutomationRequest(null, "bookmarkID");
			if (bookmarkReq != null)
			{
				String bookmarkIdentifier = bookmarkReq.getIdentifier();
				if (bookmarkIdentifier != null)
					return Long.parseLong(bookmarkIdentifier);
			}
		} catch (Exception e) {
			
		}
		
		// if the first method failed, fallback to the old slow method
		log.warn("Initializing ID generators: bookmarkID resource not found. Falling back querying the whole database.");
    	long currMaxReqId = 0;
    	try {
    		// loop over all the Automation Requests in the triplestore (TODO potential performance issue for initialization with a large persistent database)
    		for (int page = 0;; page++)
    		{
				List<AutomationRequest> listAutoRequests = VeriFitAnalysisManager.queryAutomationRequests(null, "", "", page, 100);
				if (listAutoRequests.size() == 0)
				{
					break;	
				}
				else
				{	
					for (AutomationRequest autoReq : listAutoRequests)
					{
						long reqId = Long.parseLong(OslcValues.getResourceIdFromUri(autoReq.getAbout()));
						if (reqId > currMaxReqId)
						{
							currMaxReqId = reqId;
						}
					}
				}
    		}
		} catch (WebApplicationException e) { 
			throw new Exception(e.getMessage());
		}
		return currMaxReqId;
	}
	
	private static void updateAutomationRequestIDbookmarkResource(String id)
	{
		AutomationRequest bookmarkReq = VeriFitAnalysisResourcesFactory.createAutomationRequest("bookmarkID");
		bookmarkReq.setIdentifier(id);
		bookmarkReq.setDescription("This resource is used by the adapter to rememeber what is the current maximum AutomationRequest ID. "
				+ "Determines where new ID's start after adapter restart with a persistent triplestore.");
		
        Store store = storePool.getStore();
        try {
            store.updateResources(storePool.getDefaultNamedGraphUri(), bookmarkReq);
        } catch (StoreAccessException e) {
            log.error("Failed to create resource: '" + bookmarkReq.getAbout() + "'", e);            
            throw new WebApplicationException("Failed to create resource: '" + bookmarkReq.getAbout() + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
	}
	
	/**
	 * Creates an AutomationPlan resource with the specified properties, and stores in the Adapter's catalog.
	 * @param aResource			The new resource will copy properties from the specified aResource.
	 * @param automationPlanConf AutomationPlan configuration loaded from .properties files and with available filters.
	 * @return					The newly created resource. Or null if one of the required properties was missing.
	 * @throws StoreAccessException If the triplestore is not accessible
	 * @throws OslcResourceException If the AutomationPlan to be created is missing some properties or is invalid
	 */
    public static AutomationPlan createAutomationPlan(final AutomationPlan aResource, AutomationPlanConf automationPlanConf) throws StoreAccessException, OslcResourceException
    {    	
    	AutomationPlan newResource = null;
    	
    	// check that required properties are specified
    	if (aResource == null)
    		throw new OslcResourceException("AutomationPlan creation failed: aResource was null");
    	
    	if (aResource.getTitle() == null || aResource.getTitle().isEmpty())
    	{
    		throw new OslcResourceException("AutomationPlan creation failed: dcterms:title missing or empty.");
    	}
    	if (aResource.getIdentifier() == null || aResource.getIdentifier().isEmpty())
    	{
    		throw new OslcResourceException("AutomationPlan creation failed: Title \"" + aResource.getTitle() + "\"- dcterms:identifier missing or empty.");
    	}
        
		try {

			String newID = aResource.getIdentifier();
			newResource = VeriFitAnalysisResourcesFactory.createAutomationPlan(newID);
			newResource.setUsesExecutionEnvironment(aResource.getUsesExecutionEnvironment());
			newResource.setTitle(aResource.getTitle());
			newResource.setDescription(aResource.getDescription());
			newResource.setCreator(aResource.getCreator());
			newResource.setContributor(aResource.getContributor());
			newResource.setExtendedProperties(aResource.getExtendedProperties());
			
			ParameterDefinition SUT = new ParameterDefinition();
			SUT.setDescription("Refference to an SUT resource to analyse. SUTs are created using the compilation provider.");
			SUT.setName("SUT");
			SUT.setOccurs(OslcValues.OSLC_OCCURS_ONE);
			SUT.addValueType(OslcValues.OSLC_VAL_TYPE_STRING); // TODO change to URI
			newResource.addParameterDefinition(SUT);

			ParameterDefinition outputFileRegex = new ParameterDefinition();
			outputFileRegex.setDescription("Files that change during execution and match this regex will "
					+ "be added as contributions to the Automation Result. The regex needs to match the "
					+ "whole filename.");
			outputFileRegex.setName("outputFileRegex");
			outputFileRegex.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			outputFileRegex.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			outputFileRegex.setDefaultValue(".^");
			newResource.addParameterDefinition(outputFileRegex);

			ParameterDefinition zipOutputs = new ParameterDefinition();
			zipOutputs.setDescription("If set to true, then all file contributions will be ZIPed and provided as a single zip contribution");
			zipOutputs.setName("zipOutputs");
			zipOutputs.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			zipOutputs.addValueType(OslcValues.OSLC_VAL_TYPE_BOOL);
			zipOutputs.setDefaultValue("false");
			newResource.addParameterDefinition(zipOutputs);

			ParameterDefinition timeout = new ParameterDefinition();
			timeout.setDescription("Timeout for the analysis. Zero means no timeout.");
			timeout.setName("timeout");
			timeout.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			timeout.addValueType(OslcValues.OSLC_VAL_TYPE_INTEGER);
			timeout.setDefaultValue("0");
			newResource.addParameterDefinition(timeout);

			ParameterDefinition toolCommand = new ParameterDefinition();
			toolCommand.setDescription("Used to omit the analysis tool launch command while executing analysis. True means the tool " + 
			"will be used and False means the tool command will not be used. (eg. \"./tool ./sut args\" vs \"/sut args\").");
			toolCommand.setName("toolCommand");
			toolCommand.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			toolCommand.addValueType(OslcValues.OSLC_VAL_TYPE_BOOL);
			toolCommand.setDefaultValue("true");
			newResource.addParameterDefinition(toolCommand);
			
			ParameterDefinition outputFilter = new ParameterDefinition();
			outputFilter.setDescription("Use this parameter to select which output filter should be used to process"
					+ "Contributions of this Automation Request. AllowedValues are loaded based on defined PluginFilters.");
			outputFilter.setName("outputFilter");
			outputFilter.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			outputFilter.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			outputFilter.setDefaultValue("default");
			for (String filterName : automationPlanConf.getFilters().keySet()) {	// set alloweValues based on defined filters
				outputFilter.addAllowedValue(filterName);
			}
			newResource.addParameterDefinition(outputFilter);
			
			ParameterDefinition confFile = new ParameterDefinition();
			confFile.setDescription("Creates a configuration file inside of the SUT directory before running analysis. "
					+ "Can be used multiple times create multiple conf files."
					+ "Format for this parameter: \"conf_file_name\\nconf_file_txt_contents\"");
			confFile.setName("confFile");
			confFile.setOccurs(OslcValues.OSLC_OCCURS_ZEROorMany);
			confFile.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			newResource.addParameterDefinition(confFile);
			
			ParameterDefinition confDir = new ParameterDefinition();
			confDir.setDescription("Creates a configuration directory inside of the SUT directory before running analysis"
					+ "from a base64 encoded string."
					+ "Format for this parameter: \"path_to_unzip_to\\nbase64_encoded_zip_file\"");
			confDir.setName("confDir");
			confDir.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			confDir.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			newResource.addParameterDefinition(confDir);
			
			ParameterDefinition beforeCommand = new ParameterDefinition();
			beforeCommand.setDescription("A command to run just before analysis is executed.");
			beforeCommand.setName("beforeCommand");
			beforeCommand.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			beforeCommand.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			newResource.addParameterDefinition(beforeCommand);

			ParameterDefinition afterCommand = new ParameterDefinition();
			afterCommand.setDescription("A command to run just after analysis is executed.");
			afterCommand.setName("afterCommand");
			afterCommand.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			afterCommand.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			newResource.addParameterDefinition(afterCommand);
			
			ParameterDefinition envVariable = new ParameterDefinition();
			envVariable.setDescription("Environment variable to be set for execution. "
					+ "Can be used multiple times for multiple variables."
					+ "Expected value format: \"variable_name\\nvariable_value\"");
			envVariable.setName("envVariable");
			envVariable.setOccurs(OslcValues.OSLC_OCCURS_ZEROorMany);
			envVariable.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			newResource.addParameterDefinition(envVariable);
			

			// add all user defined parameter definitions, but check for any conflicting names
			// conflicting names allow users to redefine default values (except for the SUT property)
			for (ParameterDefinition userParam : aResource.getParameterDefinition())
			{
				if (userParam.getName().equals(SUT.getName()))
				{
		    		throw new OslcResourceException("AutomationPlan creation failed: Parameter \"" + SUT.getName() + "\" can not be redefined by user configuration.");
				}
				
				boolean conflictingUserParam = false;
				for (ParameterDefinition commonParam : newResource.getParameterDefinition())
				{
					if (userParam.getName().equals(commonParam.getName()))
					{
						if (userParam.getDefaultValue() != null)
							commonParam.setDefaultValue(userParam.getDefaultValue());
						else
							log.warn("AutomationPlan creation: Automation Plan \"" + aResource.getIdentifier()
							+ "\": Parameter Definition \"" + userParam.getName() + "\" ignored because it conflicts with a common adapter Parameter Definition"
									+ " and does not redefine its defaultValue.");
							
						conflictingUserParam = true;
						break;
					}
				}
				
				// non conflicting parameter, add as a new parameter
				if (!conflictingUserParam)	
					newResource.addParameterDefinition(userParam);
			}
			
			// persist in the triplestore
	        Store store = storePool.getStore();
	        try {
	            try {
	                store.updateResources(storePool.getDefaultNamedGraphUri(), newResource);
	            } catch (StoreAccessException e) {
	                log.error("Failed to create resource: '" + newResource.getAbout() + "'", e);            
	                throw new WebApplicationException("Failed to create resource: '" + newResource.getAbout() + "'", e, Status.INTERNAL_SERVER_ERROR);
	            }
	        } finally {
	            storePool.releaseStore(store);
	        }

		} catch (WebApplicationException e) {
			throw new StoreAccessException("AutomationPlan creation failed: " + e.getMessage());
		}
		
		return newResource;
    }
    
    

	/**
	 * 
	 * @param autoRequest
	 * @param outputParams
	 * @return
	 * @throws OslcResourceException
	 */
    public static AutomationResult createAutomationResultForAutomationRequest(final AutomationRequest autoRequest, final Set<ParameterInstance> outputParams) throws OslcResourceException
    {
    	String id = autoRequest.getIdentifier();
    	AutomationResult newAutoResult = VeriFitAnalysisResourcesFactory.createAutomationResult(id);
    	
		newAutoResult.setTitle("Result - " + autoRequest.getTitle());
		newAutoResult.setReportsOnAutomationPlan(autoRequest.getExecutesAutomationPlan());
		newAutoResult.setProducedByAutomationRequest(VeriFitAnalysisResourcesFactory.constructLinkForAutomationRequest(id));
		newAutoResult.setInputParameter(autoRequest.getInputParameter());
		newAutoResult.setOutputParameter(outputParams);
		newAutoResult.setContributor(autoRequest.getContributor());
		newAutoResult.setCreator(autoRequest.getCreator());
		newAutoResult.setState(autoRequest.getState());
		newAutoResult.setDesiredState(autoRequest.getDesiredState());
		
    	// check that required properties are specified in the input parameter
    	if (newAutoResult == null || newAutoResult.getTitle() == null || newAutoResult.getTitle().isEmpty() ||
			newAutoResult.getState() == null || newAutoResult.getState().isEmpty() ||
			newAutoResult.getVerdict() == null || newAutoResult.getVerdict().isEmpty() ||
			newAutoResult.getReportsOnAutomationPlan().getValue() == null)
    	{
    		throw new OslcResourceException("Failed to create Automation Result - Missing properties");
    	}
		

		try {
			// persist in the triplestore
	        Store store = storePool.getStore();
	        try {
	            URI uri = newAutoResult.getAbout();
	            
	            if (store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
	                log.error("Cannot create a resource that already exists: '" + uri + "'");
	                throw new WebApplicationException("Cannot create a resource that already exists: '" + uri + "'", Status.SEE_OTHER);
	            }
	            try {
	                store.appendResource(storePool.getDefaultNamedGraphUri(), newAutoResult);
	            } catch (StoreAccessException e) {
	                log.error("Failed to create resource: '" + newAutoResult.getAbout() + "'", e);            
	                throw new WebApplicationException("Failed to create resource: '" + newAutoResult.getAbout() + "'", e, Status.INTERNAL_SERVER_ERROR);
	            }
	        } finally {
	            storePool.releaseStore(store);
	        }

		} catch (WebApplicationException e) {
			log.error("AutomationResult creation failed: " + e.getMessage());
		}
		
		return newAutoResult;
    }
    
	/**
     * Handles GET requests to Contribution resources with octet-stream response by directly returning the contents of the file
     * that corresponds to the Contribution. The Contribution ID is a path to the file to be updated. 
	 * @param contributionId
	 * @return Contents of the file corresponding to the Contribution resource
	 */
    public static File getContributionFile(String contributionId)
    {
		Contribution c = getContribution(null, contributionId);
		
		String filePath = c.getFilePath();
		if (filePath == null)
			return null;
		
		return new File(filePath);
    }
    
    /**
     * Handles PUT requests to Contribution resources with octet-stream data by directly replacing the contents of the file
     * that corresponds to the Contribution. The Contribution ID is a path to the file to be updated. 
     * @param fileInputStream
     * @param contributionId
     * @throws Exception 
     */
    public static void updateContributionFile(InputStream fileInputStream, String contributionId) throws Exception
    {  
		Contribution c = getContribution(null, contributionId);

		String filePath = c.getFilePath();
		if (filePath == null)
			throw new Exception("ERROR: Failed to upload Contribution file: This resource does not represent a file which could be directly uploaded");
		
        // write the file - path is getPath and content is getValue
		try {
			FileUtils.copyInputStreamToFile(fileInputStream, new File(filePath));
		}
		catch (IOException e)
		{
			throw new OslcResourceException("WARNING: Failed to write a Contribution file: " + e.getMessage());
		}
    }

	/**
	 * Processes special input parameters that require the adapter to fetch infos from other places and then fill it in.
	 * Commands like "launchSUT" or "SUTbuildCommand" instruct the adapter to fetch the SUT launch command or build command
	 * and insert it to the string to be executed on the commandline.
	 * @param executedSUT	SUT resource to fetch info out of
	 * @param newAutoResult	AutomationResult holding input and output parameters
	 * @param execParams	List of execution parameters for the request runner. This is an output parameter.
	 * @throws OslcResourceException	If some of the infos to be fetched are missing
	 */
	private static void processSpecialInputParams(final SUT executedSUT, final AutomationResult newAutoResult, List<ExecutionParameter> execParams) throws OslcResourceException {
		for ( ParameterInstance param : newAutoResult.getInputParameter())
		{ 
			if (param.getName().equals("launchSUT"))
			{
				String launchCmd = "";
				if (param.getValue().equalsIgnoreCase("true")) // only check the SUT property if parameter value was true
				{
					// get property from SUT
					launchCmd = executedSUT.getLaunchCommand();
					if (launchCmd == null)
						throw new OslcResourceException("paramer launchSUT - referenced SUT is missing a launchCommand");
				}

				// replace execution parameter value (from true to SUT property, or from false to "")
				for (ExecutionParameter execParam : execParams)
					if (execParam.getName().equals("launchSUT"))
						execParam.setValue(launchCmd);
			}
			else if (param.getName().equals("SUTbuildCommand"))
			{
				String buildCmd = "";
				if (param.getValue().equalsIgnoreCase("true")) // only check the SUT property if parameter value was true
				{
					// get property from SUT
					buildCmd = executedSUT.getBuildCommand();
					if (buildCmd == null)
						throw new OslcResourceException("paramer SUTbuildCommand - referenced SUT is missing a buildCommand");
				}

				// replace execution command value (from true to SUT property)
				for (ExecutionParameter execParam : execParams)
					if (execParam.getName().equals("SUTbuildCommand"))
						execParam.setValue(buildCmd);
			}
		}
		
		// same thing copy pasted for output parameters (will be used if one of the parameters was not supplied by the client and a default value was used instead)
		for ( ParameterInstance param : newAutoResult.getOutputParameter())
		{ 
			if (param.getName().equals("launchSUT"))
			{
				String launchCmd = "";
				if (param.getValue().equalsIgnoreCase("true")) // only check the SUT property if parameter value was true
				{
					// get property from SUT
					launchCmd = executedSUT.getLaunchCommand();
					if (launchCmd == null)
						throw new OslcResourceException("paramer launchSUT - referenced SUT is missing a launchCommand");
				}

				// replace execution parameter value (from true to SUT property, or from false to "")
				for (ExecutionParameter execParam : execParams)
					if (execParam.getName().equals("launchSUT"))
						execParam.setValue(launchCmd);
			}
			else if (param.getName().equals("SUTbuildCommand"))
			{
				String buildCmd = "";
				if (param.getValue().equalsIgnoreCase("true")) // only check the SUT property if parameter value was true
				{
					// get property from SUT
					buildCmd = executedSUT.getBuildCommand();
					if (buildCmd == null)
						throw new OslcResourceException("paramer SUTbuildCommand - referenced SUT is missing a buildCommand");
				}

				// replace execution command value (from true to SUT property)
				for (ExecutionParameter execParam : execParams)
					if (execParam.getName().equals("SUTbuildCommand"))
						execParam.setValue(buildCmd);
			}
		}
	}
	
	/**
	 * Fetches properties for Automation Result Contributions
	 * @param httpServletRequest
	 * @param aResource
	 */
	private static void getAutomationResultLocalContributions(HttpServletRequest httpServletRequest, AutomationResult aResource)
	{
		Set<Contribution> contribs = aResource.getContribution();
        aResource.clearContribution();
        
    	for (Contribution contrib : contribs)
    	{
    		try {
	    		Contribution fullContrib = getContribution(httpServletRequest, OslcValues.getResourceIdFromUri(contrib.getAbout()));
	    		aResource.addContribution(fullContrib);
    		} catch (Exception e) {
    			log.warn("AutomationResult GET: Error while flattening Contributions as local - Contribution was probably deleted: " + e.getMessage());
    		}
    	}
	}
	
	/**
	 * Loads the file contents of "stdout" and "stderr" contributions for an Automation Result. 
	 * Expects that these contributions have a filePath but do not have a value (or not an up to date one).
	 * @param autoResult 
	 * @throws IOException When the file read failed
	 */
	private static void getAutomationResultInProgressStdOutputs(AutomationResult autoResult) throws IOException
	{
		Set<Contribution> contribs = autoResult.getContribution();    
    	for (Contribution contrib : contribs)
    	{
    		// load the file contents of "stdout" and "stderr" contributions
    		if (contrib.getTitle().equals("stdout") || contrib.getTitle().equals("stderr"))
    		{
    			Path f = Paths.get(contrib.getFilePath());
    			byte [] fileContents = Files.readAllBytes(f);
    			contrib.setValue(new String(fileContents));
    		}
    	}
	}
	

	/**
	 * Version of the updateAutomationRequest API function that is called by the adapter itself.
	 * Differs in having no restrictions on modifications.
	 * @param aResource
	 * @param id
	 * @return
	 */
    public static AutomationRequest internalUpdateAutomationRequest(final AutomationRequest aResource, final String id) {
        AutomationRequest updatedResource = null;
        
        aResource.setModified(new Date());
  
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationRequest(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Cannot update a resource that already does not exists: '" + uri + "'");
            throw new WebApplicationException("Cannot update a resource that already does not exists: '" + uri + "'", Status.NOT_FOUND);
        }
        aResource.setAbout(uri);
        try {
            store.updateResources(storePool.getDefaultNamedGraphUri(), aResource);
        } catch (StoreAccessException e) {
            log.error("Failed to update resource: '" + uri + "'", e);
            throw new WebApplicationException("Failed to update resource: '" + uri + "'", e);
        } finally {
            storePool.releaseStore(store);
        }
        updatedResource = aResource;
        return updatedResource;
    }

	/**
	 * Version of the updateAutomationResult API function that is called by the adapter itself.
	 * Differs in having no restrictions on modifications.
	 * @param aResource
	 * @param id
	 * @return
	 */
    public static AutomationResult internalUpdateAutomationResult(final AutomationResult aResource, final String id) {
        AutomationResult updatedResource = null;
        aResource.setModified(new Date());
  

        // Contribution resources need to be updated separately (not as part of the A.Result)
        // to do that we need to remove all their attributes except "about" for the initial update
        // and then update them one by one explicitly
        // (this might be caused by a bug in lyo.store)
		Set<Contribution> fullContributions = aResource.getContribution();
        aResource.clearContribution();
    	for (Contribution contrib : fullContributions)
    	{
    		Contribution contribWithAboutOnly = new Contribution();
    		contribWithAboutOnly.setAbout(contrib.getAbout());
    		aResource.addContribution(contribWithAboutOnly);
    	}
    	
    	// now update the A.Result
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationResult(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Cannot update a resource that already does not exists: '" + uri + "'");
            throw new WebApplicationException("Cannot update a resource that already does not exists: '" + uri + "'", Status.NOT_FOUND);
        }
        aResource.setAbout(uri);
        try {
            store.updateResources(storePool.getDefaultNamedGraphUri(), aResource);
        } catch (StoreAccessException e) {
            log.error("Failed to update resource: '" + uri + "'", e);
            throw new WebApplicationException("Failed to update resource: '" + uri + "'", e);
        } finally {
            storePool.releaseStore(store);
        }
        
        // update the individual Contributions
    	for (Contribution contrib : fullContributions)
    	{
    		VeriFitAnalysisManager.updateContribution(null, contrib, OslcValues.getResourceIdFromUri(contrib.getAbout()));
    	}
    	
    	// restore the A.Result's contributions to their original versions 
    	aResource.setContribution(fullContributions);
        
        updatedResource = aResource;
        return updatedResource;
    }

    private static void checkInputParamValues(Set<ParameterInstance> inputParameter) throws OslcResourceException {

    	for (ParameterInstance param : inputParameter)
    	{
    		if (param.getName().equals("confFile"))
    		{
    			String paramValue = param.getValue();
    			int idxSplit = paramValue.indexOf('\n');
    			if (idxSplit == -1)
    				throw new OslcResourceException("Invalid format of confFile value. No \"\\n\" delimiter found. Expected format: filename\\nfile_contents");
    		}
    		if (param.getName().equals("confDir"))
    		{
    			if (!Utils.base64IsValueOnSecondLineValid(param.getValue()))
    				throw new OslcResourceException("Invalid format of confDir value. No \"\\n\" delimiter found or invalid base64 encoding. Expected format: dir_name\\nbase64_encoded_zip");
    		}
    		if (param.getName().equals("outputFileRegex"))
    		{
    			String paramValue = param.getValue();
    			try {
    	            Pattern.compile(paramValue);
    	        } catch (PatternSyntaxException e) {
    				throw new OslcResourceException("Invalid format of outputFileRegex value: " + e);
    	        }
       		}
    		if (param.getName().equals("envVariable"))
    		{
    			String paramValue = param.getValue();
    			int idxSplit = paramValue.indexOf('\n');
    			if (idxSplit == -1)
    				throw new OslcResourceException("Invalid format of envVariable value. No \"\\n\" delimiter found. Expected format: variable_name\\nvariable_value");
       		}
    	}
	}
    
	// End of user code

    public static void contextInitializeServletListener(final ServletContextEvent servletContextEvent)
    {
        
        // Start of user code contextInitializeServletListener
	
    	// load configuration
    	try {
    		VeriFitAnalysisProperties.initializeProperties();
    	} catch (FileNotFoundException e) {
    		log.error("Adapter configuration: Failed to load Java properties: " + e.getMessage()
					+ "\t The adapter needs to be configured to be able to run!\n"
					+ "\tSee the \"VeriFitAnalysisExample.properties\" file for instructions and use it as a template.");
			System.exit(1);
  
    	} catch (IOException e) {
			log.error("Adapter configuration: Failed to load Java properties: " + e.getMessage());
			System.exit(1);
		}
    	log.info("Loaded configuration:\n"
    			+ "  ADAPTER_HOST: " + VeriFitAnalysisProperties.ADAPTER_HOST + "\n"
    			+ "  ADAPTER_PORT: " + VeriFitAnalysisProperties.ADAPTER_PORT + "\n"
    			+ "  SERVER_URL: " + VeriFitAnalysisProperties.SERVER_URL + "\n"
    			+ "  SPARQL_SERVER_NAMED_GRAPH_RESOURCES: " + VeriFitAnalysisProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES + "\n"
    			+ "  SPARQL_SERVER_QUERY_ENDPOINT: " + VeriFitAnalysisProperties.SPARQL_SERVER_QUERY_ENDPOINT + "\n"
    			+ "  SPARQL_SERVER_UPDATE_ENDPOINT: " + VeriFitAnalysisProperties.SPARQL_SERVER_UPDATE_ENDPOINT + "\n"
    			+ "  AUTHENTICATION_ENABLED: " + VeriFitAnalysisProperties.AUTHENTICATION_ENABLED + "\n"
    			+ "  AUTHENTICATION_USERNAME: " + VeriFitAnalysisProperties.AUTHENTICATION_USERNAME + "\n"
    			+ "  AUTHENTICATION_PASSWORD: " + "********" + "\n"
    			+ "  KEEP_LAST_N_ENABLED: " + VeriFitAnalysisProperties.KEEP_LAST_N_ENABLED + "\n"
    			+ "  KEEP_LAST_N: " + VeriFitAnalysisProperties.KEEP_LAST_N + "\n"
    			+ "  DUMMYTOOL_PATH: " + VeriFitAnalysisProperties.DUMMYTOOL_PATH);
    	
        // End of user code
        // Start of user code StoreInitialise
    	// connect to the triplestore
        // End of user code
        /* Unwanted generated code
		Properties lyoStoreProperties = new Properties();
        String lyoStorePropertiesFile = StorePool.class.getResource("/store.properties").getFile();
        try {
            lyoStoreProperties.load(new FileInputStream(lyoStorePropertiesFile));
        } catch (IOException e) {
            log.error("Failed to initialize Store. properties file for Store configuration could not be loaded.", e);
            throw new RuntimeException(e);
        }
        
        */
		int initialPoolSize = 100; // TODO
        URI defaultNamedGraph;
        URI sparqlQueryEndpoint;
        URI sparqlUpdateEndpoint;
        try {
            defaultNamedGraph = new URI(VeriFitAnalysisProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES);
            sparqlQueryEndpoint = new URI(VeriFitAnalysisProperties.SPARQL_SERVER_QUERY_ENDPOINT);
            sparqlUpdateEndpoint = new URI(VeriFitAnalysisProperties.SPARQL_SERVER_UPDATE_ENDPOINT);
        } catch (URISyntaxException e) {
            log.error("Failed to initialize Store. One of the configuration property ('defaultNamedGraph' or 'sparqlQueryEndpoint' or 'sparqlUpdateEndpoint') is not a valid URI.", e);
            throw new RuntimeException(e);
        }
        String userName = null;
        String password = null;
        storePool = new StorePool(initialPoolSize, defaultNamedGraph, sparqlQueryEndpoint, sparqlUpdateEndpoint, userName, password);
        // Start of user code StoreFinalize


        // check that the store is online
        try {
        	Utils.checkStoreOnline(storePool);
		} catch (Exception e) {
			String hint = "Is the triplestore running?\n";
			if (e.getMessage() == null)
				hint = "Are the endpoints correct? (server context, data set, endpoint)";
	
			log.error("Adapter configuration: SPARQL triplestore initialization failed: " + e.getMessage()
			+ "\n\n  Current Triplestore configuration:\n    - query endpoint: " + sparqlQueryEndpoint + "\n    - update endpoint: " + sparqlUpdateEndpoint
			+ "\n\n  " + hint);
			System.exit(1);
		}
        
    	// create AutomationPlans
		AutomationPlanConfManager autoPlanManager = AutomationPlanConfManager.getInstance();
        try {
        	autoPlanManager.initializeAutomationPlans();
		} catch (Exception e) {
			log.error("Adapter initialization: Loading AutomationPlans: " + e.getMessage());
			System.exit(1);
		}

		// check what the last AutomationRequest ID is (requests have a numerical ID) and initialize the ID generator to one higher
    	long initReqId = 0;
		try {
			initReqId = getCurrentHighestAutomationRequestId() + 1;
		} catch (Exception e) {
			log.error("Adapter initialization: Failed to get latest AutomationRequest ID: " + e.getMessage());
			System.exit(1);
		}
		AutoRequestIdGen = new ResourceIdGen(initReqId);
		log.info("Initialization: resource ID gen starting at " + initReqId);

		// initialize execution manager
		AutoRequestExecManager = new ExecutionManager();
		
		
		// AHT client
		System.out.println("XXXXXXXXXXXXXXXXXXXXXX AHT start XXXXXXXXXXXXXXXXXXXXXX");
		ArrowheadClient fitClient1;
		try {
			fitClient1 = ArrowheadClientBuilder.newBuilder().certificate(
				      new FileInputStream(Paths.get(
				        "./conf/fit-client1.p12").toFile()),
				      "123456")
				      .defaultLogger()
				      .build();
			System.out.println("XXXXXXXXXXXXXXXXXXXXXX AHT after client XXXXXXXXXXXXXXXXXXXXXX");

		    ArrowheadServiceRegistryClient srFitClient1
		      = new ArrowheadServiceRegistryClient(fitClient1, "teta.fit.vutbr.cz", 8443);

			System.out.println("XXXXXXXXXXXXXXXXXXXXXX AHT after registry XXXXXXXXXXXXXXXXXXXXXX");
			
		    ArrowheadServiceRegistrationForm registrationForm
		      = new ArrowheadServiceRegistrationForm("verify-oslc", "fit-client1",
		        "147.229.12.81", 8080, "/oslc");
		    
			System.out.println("XXXXXXXXXXXXXXXXXXXXXX AHT after form XXXXXXXXXXXXXXXXXXXXXX");

		    System.out.println(srFitClient1.unregister(registrationForm));
		    
			System.out.println("XXXXXXXXXXXXXXXXXXXXXX AHT after unregister XXXXXXXXXXXXXXXXXXXXXX");

		    System.out.println(srFitClient1.register(registrationForm));
		    
			System.out.println("XXXXXXXXXXXXXXXXXXXXXX AHT after register XXXXXXXXXXXXXXXXXXXXXX");
		    
		} catch (UnrecoverableKeyException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException
				| CertificateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
        // End of user code
        
    }

    public static void contextDestroyServletListener(ServletContextEvent servletContextEvent) 
    {
        
        // Start of user code contextDestroyed
		log.info("Shutting down");
        // End of user code
    }

    public static ServiceProviderInfo[] getServiceProviderInfos(HttpServletRequest httpServletRequest)
    {
        ServiceProviderInfo[] serviceProviderInfos = {};
        
        // Start of user code "ServiceProviderInfo[] getServiceProviderInfos(...)"

        ServiceProviderInfo r1 = new ServiceProviderInfo();
        r1.name = "VeriFit Analysis Provider";
        r1.serviceProviderId = VeriFitAnalysisProperties.AUTOMATION_PROVIDER_ID;

        serviceProviderInfos = new ServiceProviderInfo[1];
        serviceProviderInfos[0] = r1;

        // End of user code
        return serviceProviderInfos;
    }

    public static List<AutomationPlan> queryAutomationPlans(HttpServletRequest httpServletRequest, String where, String prefix, int page, int limit)
    {
        List<AutomationPlan> resources = null;
        
        // Start of user code queryAutomationPlans_storeInit
        if (httpServletRequest != null)
        {
		    String strLimit = httpServletRequest.getParameter("limit");
		    if (strLimit != null)
		    	limit = Integer.parseInt(strLimit) - 1; // TODO -1 to counteract the +1 generated in the store.getResources(...) below
        }
        // End of user code
        Store store = storePool.getStore();
        try {
            resources = new ArrayList<AutomationPlan>(store.getResources(storePool.getDefaultNamedGraphUri(), AutomationPlan.class, prefix, where, "", limit+1, page*limit));
        } catch (StoreAccessException | ModelUnmarshallingException e) {
            log.error("Failed to query resources, with where-string '" + where + "'", e);
            throw new WebApplicationException("Failed to query resources, with where-string '" + where + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code queryAutomationPlans_storeFinalize
        // End of user code
        
        // Start of user code queryAutomationPlans
        // End of user code
        return resources;
    }
    public static List<AutomationResult> queryAutomationResults(HttpServletRequest httpServletRequest, String where, String prefix, int page, int limit)
    {
        List<AutomationResult> resources = null;
        
        // Start of user code queryAutomationResults_storeInit
        if (httpServletRequest != null)
        {
		    String strLimit = httpServletRequest.getParameter("limit");
		    if (strLimit != null)
		    	limit = Integer.parseInt(strLimit) - 1; // TODO -1 to counteract the +1 generated in the store.getResources(...) below
        }
        // End of user code
        Store store = storePool.getStore();
        try {
            resources = new ArrayList<AutomationResult>(store.getResources(storePool.getDefaultNamedGraphUri(), AutomationResult.class, prefix, where, "", limit+1, page*limit));
        } catch (StoreAccessException | ModelUnmarshallingException e) {
            log.error("Failed to query resources, with where-string '" + where + "'", e);
            throw new WebApplicationException("Failed to query resources, with where-string '" + where + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code queryAutomationResults_storeFinalize
        
        for (AutomationResult autoRes : resources)
        {
        	// the triple store only returns the resource as a link not as an inlined one -- fetch their contents
        	getAutomationResultLocalContributions(httpServletRequest, autoRes);
        }
    	
        // End of user code
        
        // Start of user code queryAutomationResults
        // End of user code
        return resources;
    }
    public static List<AutomationRequest> queryAutomationRequests(HttpServletRequest httpServletRequest, String where, String prefix, int page, int limit)
    {
        List<AutomationRequest> resources = null;
        
        // Start of user code queryAutomationRequests_storeInit
        if (httpServletRequest != null)
        {
		    String strLimit = httpServletRequest.getParameter("limit");
		    if (strLimit != null)
		    	limit = Integer.parseInt(strLimit) - 1; // TODO -1 to counteract the +1 generated in the store.getResources(...) below
        }
        // End of user code
        Store store = storePool.getStore();
        try {
            resources = new ArrayList<AutomationRequest>(store.getResources(storePool.getDefaultNamedGraphUri(), AutomationRequest.class, prefix, where, "", limit+1, page*limit));
        } catch (StoreAccessException | ModelUnmarshallingException e) {
            log.error("Failed to query resources, with where-string '" + where + "'", e);
            throw new WebApplicationException("Failed to query resources, with where-string '" + where + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code queryAutomationRequests_storeFinalize
        // End of user code
        
        // Start of user code queryAutomationRequests
        
        for (AutomationRequest req : resources)
        {
            // dont show the bookmark resource in query responses
            if (OslcValues.getResourceIdFromUri(req.getAbout()).equals("bookmarkID"))
            {
            	resources.remove(req);
            	break;
            }
        }
        
        // End of user code
        return resources;
    }
    public static List<Contribution> queryContributions(HttpServletRequest httpServletRequest, String where, String prefix, int page, int limit)
    {
        List<Contribution> resources = null;
        
        // Start of user code queryContributions_storeInit
        // End of user code
        Store store = storePool.getStore();
        try {
            resources = new ArrayList<Contribution>(store.getResources(storePool.getDefaultNamedGraphUri(), Contribution.class, prefix, where, "", limit+1, page*limit));
        } catch (StoreAccessException | ModelUnmarshallingException e) {
            log.error("Failed to query resources, with where-string '" + where + "'", e);
            throw new WebApplicationException("Failed to query resources, with where-string '" + where + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code queryContributions_storeFinalize
        // End of user code
        
        // Start of user code queryContributions
        // TODO Implement code to return a set of resources.
        // An empty List should imply that no resources where found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return resources;
    }
    public static List<AutomationPlan> AutomationPlanSelector(HttpServletRequest httpServletRequest, String terms)   
    {
        List<AutomationPlan> resources = null;
        
        // Start of user code AutomationPlanSelector_storeInit
        // End of user code
        Store store = storePool.getStore();
        try {
            resources = new ArrayList<AutomationPlan>(store.getResources(storePool.getDefaultNamedGraphUri(), AutomationPlan.class, "", "", terms, 20, -1));
        } catch (StoreAccessException | ModelUnmarshallingException e) {
            log.error("Failed to search resources, with search-term '" + terms + "'", e);
            throw new WebApplicationException("Failed to search resources, with search-term '" + terms + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code AutomationPlanSelector_storeFinalize
        // End of user code
        
        // Start of user code AutomationPlanSelector
        // End of user code
        return resources;
    }
    public static List<AutomationResult> AutomationResultSelector(HttpServletRequest httpServletRequest, String terms)   
    {
        List<AutomationResult> resources = null;
        
        // Start of user code AutomationResultSelector_storeInit
        // End of user code
        Store store = storePool.getStore();
        try {
            resources = new ArrayList<AutomationResult>(store.getResources(storePool.getDefaultNamedGraphUri(), AutomationResult.class, "", "", terms, 20, -1));
        } catch (StoreAccessException | ModelUnmarshallingException e) {
            log.error("Failed to search resources, with search-term '" + terms + "'", e);
            throw new WebApplicationException("Failed to search resources, with search-term '" + terms + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code AutomationResultSelector_storeFinalize
        // End of user code
        
        // Start of user code AutomationResultSelector
        // End of user code
        return resources;
    }
    public static List<AutomationRequest> AutomationRequestSelector(HttpServletRequest httpServletRequest, String terms)   
    {
        List<AutomationRequest> resources = null;
        
        // Start of user code AutomationRequestSelector_storeInit
        // End of user code
        Store store = storePool.getStore();
        try {
            resources = new ArrayList<AutomationRequest>(store.getResources(storePool.getDefaultNamedGraphUri(), AutomationRequest.class, "", "", terms, 20, -1));
        } catch (StoreAccessException | ModelUnmarshallingException e) {
            log.error("Failed to search resources, with search-term '" + terms + "'", e);
            throw new WebApplicationException("Failed to search resources, with search-term '" + terms + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code AutomationRequestSelector_storeFinalize
        // End of user code
        
        // Start of user code AutomationRequestSelector
        // End of user code
        return resources;
    }
    public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, AutomationRequest aResource) throws OslcResourceException
    {
        AutomationRequest newResource = null;
        
        // Start of user code createAutomationRequest_storeInit
        
        SutAnalyse runner = null;
        try {
			// error response on empty creation POST
	        if (aResource == null)
				throw new OslcResourceException("empty creation POST");
	        
	        // check for missing required properties
			if (aResource.getExecutesAutomationPlan() == null || aResource.getExecutesAutomationPlan().getValue() == null)
				throw new OslcResourceException("executesAutomationPlan property missing");
			if (aResource.getTitle() == null || aResource.getTitle().isEmpty())
				throw new OslcResourceException("title property missing");
			
			
			// generate a new ID and update the ID bookmark resource 
			String newID = AutoRequestIdGen.getId();
			updateAutomationRequestIDbookmarkResource(newID);
			
			
			// create a basic Automation Request (with core properties like creation time, identifier, ...)
			// and set relevant properties from the Automation Request received from the client
			newResource = VeriFitAnalysisResourcesFactory.createAutomationRequest(newID);
			newResource.setInputParameter(aResource.getInputParameter());
			newResource.setTitle(aResource.getTitle());
			newResource.setDescription(aResource.getDescription());
			newResource.setExecutesAutomationPlan(aResource.getExecutesAutomationPlan());
			newResource.setCreator(aResource.getCreator());
			newResource.setContributor(aResource.getContributor());
			newResource.setExtendedProperties(aResource.getExtendedProperties());
			//newResource.replaceDesiredState(aResource.getDesiredState());	// TODO use this to implement deferred execution later
			
			// get the executed autoPlan and load its configuration
			AutomationPlan execAutoPlan = null;
			String execAutoPlanId = null;
			try {
				execAutoPlanId = OslcValues.getResourceIdFromUri(newResource.getExecutesAutomationPlan().getValue());
				execAutoPlan = getAutomationPlan(null, execAutoPlanId);
			} catch (Exception e) {
				throw new OslcResourceException("AutomationPlan not found (" + newResource.getExecutesAutomationPlan().getValue() + ")");			
			}
			AutomationPlanConf execAutoPlanConf = AutomationPlanConfManager.getInstance().getAutoPlanConf(execAutoPlanId);

			// check whether the new Auto Request can start execution immediately or needs to wait in a queue
			// and set its state accordingly
			if (execAutoPlanConf.getOneInstanceOnly() == false) {	// no instance restrictions --> can run
				newResource.replaceState(OslcValues.AUTOMATION_STATE_INPROGRESS);
			} else {	// only one instance allowed at a time --> check queue
				newResource.replaceState(OslcValues.AUTOMATION_STATE_QUEUED);
			}			
			
			
			// check input parameters based on parameter definitions, and create output parameters
			Set<ParameterInstance> outputParams = Utils.checkInputParamsAndProduceOuputParams(newResource.getInputParameter(), execAutoPlan.getParameterDefinition());

			// check well formed parameter values
			checkInputParamValues(newResource.getInputParameter());				
			
			// get some values from input parameters
			String executedSutId = null;
			for ( ParameterInstance param : newResource.getInputParameter())
			{ 
				if (param.getName().equals("SUT"))
				{
					executedSutId = param.getValue();
				}
			}
			
			// check that the SUT to be executed exists
			SUT executedSUT = null;
			try {
				executedSUT = CompilationAdapterClient.getSUT(executedSutId);
			} catch (Exception e) {
				throw new IOException("Failed to get refferenced SUT - make sure the compilation provider is running.\nClient response: " + e.getMessage());
			}
			
			// create an AutomationResult for this AutoRequest
			AutomationResult newAutoResult = createAutomationResultForAutomationRequest(newResource, outputParams);
			newResource.setProducedAutomationResult(new Link(newAutoResult.getAbout()));
			
			// create Execution Parameters for the runner
			List<ExecutionParameter> execParams = ExecutionParameter.createExecutionParameters(newAutoResult.getInputParameter(), newAutoResult.getOutputParameter(), execAutoPlan.getParameterDefinition());
			
			// process special input parameters
			processSpecialInputParams(executedSUT, newAutoResult, execParams);
			
			// create a new thread to execute the automation request - and start it OR queue it up
			runner = new SutAnalyse(newResource, newAutoResult, executedSUT, execParams);
			
		} catch (OslcResourceException | RuntimeException | IOException e) {
			throw new OslcResourceException("AutomationRequest NOT created - " + e.getMessage());
			
		} catch (Exception e) {
			log.error("AutomationResquest creation failed: " + e.getMessage());
			throw new OslcResourceException("AutomationRequest NOT created - " + e.getMessage());
		}
		

		// hack for the generated code below
		aResource = newResource;
        // End of user code
        Store store = storePool.getStore();
        try {
            URI uri = null;
            // Start of user code createAutomationRequest_storeSetUri
            
            uri = newResource.getAbout();
            
            // End of user code
            if (store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
                log.error("Cannot create a resource that already exists: '" + uri + "'");
                throw new WebApplicationException("Cannot create a resource that already exists: '" + uri + "'", Status.SEE_OTHER);
            }
            aResource.setAbout(uri);
            try {
                store.appendResource(storePool.getDefaultNamedGraphUri(), aResource);
            } catch (StoreAccessException e) {
                log.error("Failed to create resource: '" + aResource.getAbout() + "'", e);            
                throw new WebApplicationException("Failed to create resource: '" + aResource.getAbout() + "'", e, Status.INTERNAL_SERVER_ERROR);
            }
        } finally {
            storePool.releaseStore(store);
        }
        newResource = aResource;
        // Start of user code createAutomationRequest_storeFinalize
        
        // start the execution (or queue up, based on desired state)
        AutoRequestExecManager.addRequest(runner);
        
        // End of user code
        
        // Start of user code createAutomationRequest
        
        if (VeriFitAnalysisProperties.KEEP_LAST_N_ENABLED)
        {
        	try {
	        	// only keep last N automation requests
	        	// delete the one that is last N+1 when creating a new one 
	        	long currentID = Integer.parseInt(OslcValues.getResourceIdFromUri(newResource.getAbout()));
	        	if (currentID > VeriFitAnalysisProperties.KEEP_LAST_N)
	        	{
		        	String toDeleteID = Long.toString(currentID - VeriFitAnalysisProperties.KEEP_LAST_N);
	            	log.info("KEEP_LAST_N is enabled with value of " + VeriFitAnalysisProperties.KEEP_LAST_N
	            			+ ". Deleting AutomationRequest \"" + toDeleteID +"\" and all its associated resources.");
	            	
		        	deleteAutomationRequest(null, toDeleteID);
		        	deleteAutomationResult(null, toDeleteID);
	        	}
        	} catch (Exception e) {
        		log.error("Failed to delete old AutomationRequest or Result with KEEP_LAST_N enabled");
        	}
        }
        
        // End of user code
        return newResource;
    }

    public static AutomationRequest createAutomationRequestFromDialog(HttpServletRequest httpServletRequest, final AutomationRequest aResource)
    {
        AutomationRequest newResource = null;
        
        
        // Start of user code createAutomationRequestFromDialog
		try {
			newResource = createAutomationRequest(httpServletRequest,aResource);
		} catch (OslcResourceException e) {	// TODO
            log.error("Failed to create resource: '" + aResource.getAbout() + "'", e); 
			throw new WebApplicationException("Failed to create resource: '" + aResource.getAbout() + "'", e, Status.BAD_REQUEST);
		}
        // End of user code
        return newResource;
    }



    public static AutomationRequest getAutomationRequest(HttpServletRequest httpServletRequest, final String id)
    {
        AutomationRequest aResource = null;
        
        // Start of user code getAutomationRequest_storeInit
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationRequest(id);
        try {
            aResource = store.getResource(storePool.getDefaultNamedGraphUri(), uri, AutomationRequest.class);
        } catch (NoSuchElementException e) {
            log.error("Resource: '" + uri + "' not found");
            throw new WebApplicationException("Failed to get resource: '" + uri + "'", e, Status.NOT_FOUND);
        } catch (StoreAccessException | ModelUnmarshallingException  e) {
            log.error("Failed to get resource: '" + uri + "'", e);
            throw new WebApplicationException("Failed to get resource: '" + uri + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code getAutomationRequest_storeFinalize
        // End of user code
        
        // Start of user code getAutomationRequest
        // End of user code
        return aResource;
    }

    public static Boolean deleteAutomationRequest(HttpServletRequest httpServletRequest, final String id)
    {
        Boolean deleted = false;
        // Start of user code deleteAutomationRequest_storeInit

        // dont allow clients to delete the bookmark resource
        if (id.equals("bookmarkID"))
        {
            log.error("Automation Request DELETE: bookmarkID resource is not allowed to be updated");
            throw new WebApplicationException("Automation Request DELETE: bookmarkID resource is not allowed to be deleted", Status.FORBIDDEN);
        }
        

        AutomationRequest requestToDelete = null;
        try {
        	requestToDelete = getAutomationRequest(null, id);
            
        	// if the request is still running, cancel execution first
            if (! (requestToDelete.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_CANCELED)
            	|| requestToDelete.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_COMPLETE))) {

            	// set desired state to cancel and update the request --> will cancel it
            	requestToDelete.setDesiredState(OslcValues.AUTOMATION_STATE_CANCELED);
            	updateAutomationRequest(null, requestToDelete, id);
            }
        
        } catch (Exception e) {
        	// means not found; let it fail on the generated "resourceExists()" below
        	// or failed to cancel, so it finished already
        }
        
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationRequest(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("AutomationRequest DELETE: Cannot delete a resource that already does not exists: '" + uri + "'");
            throw new WebApplicationException("AutomationRequest DELETE: Cannot delete a resource that already does not exists: '" + uri + "'", Status.NOT_FOUND);
        }
        store.deleteResources(storePool.getDefaultNamedGraphUri(), uri);
        storePool.releaseStore(store);
        deleted = true;
        // Start of user code deleteAutomationRequest_storeFinalize
        // End of user code
        
        // Start of user code deleteAutomationRequest
        if (httpServletRequest != null) {
	        String cascade = httpServletRequest.getParameter("cascade");
	        if (cascade != null && cascade.equalsIgnoreCase("true"))
	        {
	        	try {
	        		deleteAutomationResult(null, id);	// TODO relies on result and request IDs being the same
	        	} catch (Exception e) {
	        		log.warn("AutomationRequest delete id \"" + id + "\": Failed to cascade - " + e.getMessage());
	        	}
	        }
        }
        // End of user code
        return deleted;
    }

    public static AutomationRequest updateAutomationRequest(HttpServletRequest httpServletRequest, AutomationRequest aResource, final String id) {
        AutomationRequest updatedResource = null;
        // Start of user code updateAutomationRequest_storeInit

        // dont allow clients to update the bookmark resource
        if (id.equals("bookmarkID"))
        {
            log.error("Automation Request UPDATE: bookmarkID resource is not allowed to be updated");
            throw new WebApplicationException("Automation Request UPDATE: bookmarkID resource is not allowed to be updated", Status.FORBIDDEN);
        }
        
        // check that the request has all the required properties
        if (aResource == null)
        {
            log.error("Automation Request UPDATE: received an empty request");
            throw new WebApplicationException("Automation Request UPDATE: received an empty request", Status.BAD_REQUEST);
        }
		if (aResource.getTitle() == null || aResource.getTitle().isEmpty())
			throw new WebApplicationException("Automation Request UPDATE: title property missing", Status.BAD_REQUEST);
        
        // get the current version of the Automation Request 
        updatedResource = getAutomationRequest(null, id);
        
        // check if cancellation was requested
        if (	aResource.getDesiredState() != null && aResource.getDesiredState().equals(OslcValues.AUTOMATION_STATE_CANCELED)	// incoming update says cancel
        		&& !updatedResource.getDesiredState().equals(OslcValues.AUTOMATION_STATE_CANCELED))								// current desired state was not cancel
		{
        	// check that the request has not finished yet, otherwise error
        	if (updatedResource.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_COMPLETE)) {
        		log.error("Automation Request UPDATE: Failed to cancel execution becuase it has already finished.");
	            throw new WebApplicationException("Automation Request UPDATE: Failed to cancel execution becuase it has already finished.", 500);
        	} else {
	    		try {
					// cancel the request
	    			AutoRequestExecManager.cancelRequest(id);
	    			
	    			// update this requests state and desired state
	    			updatedResource.replaceState(OslcValues.AUTOMATION_STATE_CANCELED);
	    			updatedResource.setDesiredState(OslcValues.AUTOMATION_STATE_CANCELED);
					
					// update the associated automation result
	    			String resultId = OslcValues.getResourceIdFromUri(updatedResource.getProducedAutomationResult().getValue());
	    			AutomationResult associatedResult = getAutomationResult(null, resultId);
	    			associatedResult.replaceState(OslcValues.AUTOMATION_STATE_CANCELED);
	    			associatedResult.replaceVerdict(OslcValues.AUTOMATION_VERDICT_UNAVAILABLE);
	    			associatedResult.setDesiredState(OslcValues.AUTOMATION_STATE_CANCELED);
					internalUpdateAutomationResult(associatedResult, resultId);
					
				} catch (Exception e) {
					// the Automation Request is not running so it can not be canceled
	        		log.error("Automation Request UPDATE: This should never happen! ExecutionManager does not have a request even though it should still be running");
		            throw new WebApplicationException("Automation Request UPDATE: This should never happen! ExecutionManager does not have a request even though it should still be running", 500);
				}
        	}
        }

        // only allow other updates once the request has finished execution (otherwise the updates would be overwritten after)
        // TODO maybe changed with future functionality
        if (! (updatedResource.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_CANCELED)
        	|| updatedResource.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_COMPLETE))) {

    		log.error("Automation Request UPDATE: updating Automation Requests that have not yet finished execution is currently not allowed, "
					+ "except when canceling the execution using the desiredState property.");
            throw new WebApplicationException("Automation Request UPDATE: updating Automation Requests that have not yet finished execution is currently not allowed, "
					+ "except when canceling the execution using the desiredState property.", 500);
        }
        
        // only update the properties that we allow to update
        updatedResource.setModified(new Date());
        updatedResource.setTitle(aResource.getTitle());
        updatedResource.setDescription(aResource.getDescription());
        updatedResource.setCreator(aResource.getCreator());
        updatedResource.setContributor(aResource.getContributor());
        updatedResource.setExtendedProperties(aResource.getExtendedProperties());
    
        // for the generated code below
        aResource = updatedResource;
	    
	        
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationRequest(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Cannot update a resource that already does not exists: '" + uri + "'");
            throw new WebApplicationException("Cannot update a resource that already does not exists: '" + uri + "'", Status.NOT_FOUND);
        }
        aResource.setAbout(uri);
        try {
            store.updateResources(storePool.getDefaultNamedGraphUri(), aResource);
        } catch (StoreAccessException e) {
            log.error("Failed to update resource: '" + uri + "'", e);
            throw new WebApplicationException("Failed to update resource: '" + uri + "'", e);
        } finally {
            storePool.releaseStore(store);
        }
        updatedResource = aResource;
        // Start of user code updateAutomationRequest_storeFinalize
        // End of user code
        
        // Start of user code updateAutomationRequest
        // End of user code
        return updatedResource;
    }
    public static AutomationResult getAutomationResult(HttpServletRequest httpServletRequest, final String id)
    {
        AutomationResult aResource = null;
        
        // Start of user code getAutomationResult_storeInit
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationResult(id);
        try {
            aResource = store.getResource(storePool.getDefaultNamedGraphUri(), uri, AutomationResult.class);
        } catch (NoSuchElementException e) {
            log.error("Resource: '" + uri + "' not found");
            throw new WebApplicationException("Failed to get resource: '" + uri + "'", e, Status.NOT_FOUND);
        } catch (StoreAccessException | ModelUnmarshallingException  e) {
            log.error("Failed to get resource: '" + uri + "'", e);
            throw new WebApplicationException("Failed to get resource: '" + uri + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code getAutomationResult_storeFinalize

        // the triple store only returns the resource as a link not as an inlined one -- fetch their contents
        getAutomationResultLocalContributions(httpServletRequest, aResource);
                
        // End of user code
        
        // Start of user code getAutomationResult
        
        

        // if the automation result is still in progress, provide some infos about the run (such as stdout and stderr)
        // TODO currently the database is only updated after the result execution finishes to avoid too much database communication
        // can be disabled using a request parameter (query string)
        if (httpServletRequest != null)
        {
        	String inprogressOutputsParam = httpServletRequest.getParameter("enableInProgressOutputs");
        	boolean inProgressOutputsEnabled = true;
        	if (inprogressOutputsParam != null && inprogressOutputsParam.equalsIgnoreCase("false"))
        		inProgressOutputsEnabled = false;
        	
        	if (inProgressOutputsEnabled && aResource.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_INPROGRESS))
            {
            	// load the current contents of stdout and stderr
            	try {
    				getAutomationResultInProgressStdOutputs(aResource);
    			} catch (IOException e) {
    				log.warn("Automation Result GET: Failed to get contents of stdout or stderr of the execution", e);
    			}
            }
        	
        }
        
        // End of user code
        return aResource;
    }

    public static Boolean deleteAutomationResult(HttpServletRequest httpServletRequest, final String id)
    {
        Boolean deleted = false;
        // Start of user code deleteAutomationResult_storeInit
        AutomationResult resultToDelete = null;
        try {
        	resultToDelete = getAutomationResult(null, id);      
        } catch (Exception e) {
        	// means not found; let it fail on the generated "resourceExists()" below
        }
        if (resultToDelete != null)
        {
        	// deleting results is not allowed before they finish
            if (! (resultToDelete.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_CANCELED)
            	|| resultToDelete.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_COMPLETE))) {

        		log.error("Automation Result DELETE: deleting Automation Results that have not yet finished execution is not allowed");
                throw new WebApplicationException("Automation Result DELETE: deleting Automation Results that have not yet finished execution is not allowed", 500);
            }
        }
        
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationResult(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("AutomationResult DELETE: Cannot delete a resource that already does not exists: '" + uri + "'");
            throw new WebApplicationException("AutomationResult DELETE: Cannot delete a resource that already does not exists: '" + uri + "'", Status.NOT_FOUND);
        }
        store.deleteResources(storePool.getDefaultNamedGraphUri(), uri);
        storePool.releaseStore(store);
        deleted = true;
        // Start of user code deleteAutomationResult_storeFinalize
        // End of user code
        
        // Start of user code deleteAutomationResult
        if (httpServletRequest != null) {
	        String cascade = httpServletRequest.getParameter("cascade");
	        if (cascade != null && cascade.equalsIgnoreCase("true"))
	        {
	        	try {
	        		deleteAutomationRequest(null, id);	// TODO relies on result and request IDs being the same
	        	} catch (Exception e) {
	        		log.warn("AutomationResult delete id \"" + id + "\": Failed to cascade - " + e.getMessage());
	        	}
	        }
        }
        
        // cascade delete all contributions
        if (resultToDelete != null) {
        	String sutPath = null;
	        for (Contribution contrib : resultToDelete.getContribution())
	        {
	        	deleteContribution(null, OslcValues.getResourceIdFromUri(contrib.getAbout()));
	        	
	        	// get the sut directory to be able to delete *sut*/.adapter/exec_analysis_N.sh or .ps
	        	if (contrib.getFilePath() != null && contrib.getFilePath().contains(".adapter"))
	        		sutPath = contrib.getFilePath();
	        }
	        
	        // delete *sut*/.adapter/exec_analysis_N.sh or .ps
	        if (sutPath != null)
	        {
		        try {
		        	String fileEnding = SystemUtils.IS_OS_LINUX ? ".sh" : ".ps1";
		    		File execFile = FileSystems.getDefault().getPath(sutPath).getParent().resolve("exec_analysis_" + id + fileEnding).toFile();
		    		FileDeleteStrategy.FORCE.delete(execFile);
				} catch (IOException e) {
					log.error("Contribution delete: Failed to delete associated file: " + e.getMessage());
				}
	        }
        }
        // End of user code
        return deleted;
    }

    public static AutomationResult updateAutomationResult(HttpServletRequest httpServletRequest, AutomationResult aResource, final String id) {
        AutomationResult updatedResource = null;
        // Start of user code updateAutomationResult_storeInit

        if (aResource == null)
        {
            log.error("Automation Result UPDATE: received an empty request");
            throw new WebApplicationException("Automation Result UPDATE: received an empty request", Status.BAD_REQUEST);
        }
		if (aResource.getTitle() == null || aResource.getTitle().isEmpty())
			throw new WebApplicationException("Automation Result UPDATE: title property missing", Status.BAD_REQUEST);
        
        // get the current version of the Automation Result 
        updatedResource = getAutomationResult(null, id);
        
        // TODO allow async updates of the Automation Result if other agents participate in the execution in the future
        //updatedResource.setOutputParameter(...);
        //updatedResource.setContribution(...);
        
        // only allow other updates once the result has finished execution (otherwise the updates would be overwritten after)
        // TODO maybe changed with future functionality
        if (! (updatedResource.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_CANCELED)
        	|| updatedResource.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_COMPLETE))) {

        	// check if the client tried to cancel this Automation Result, and give him advice on how to do it instead
	        if (	aResource.getDesiredState() != null && aResource.getDesiredState().equals(OslcValues.AUTOMATION_STATE_CANCELED)	// incoming update says cancel
	        		&& !updatedResource.getDesiredState().equals(OslcValues.AUTOMATION_STATE_CANCELED))								// current desired state was not cancel
			{
				// execution can only be cancelled by updating the automation request
				log.error("Automation Result UPDATE: Canceling execution is only allowed by updating the Automation Request, not the result.\n"
						+ "Update this A. Request instead: " + updatedResource.getProducedByAutomationRequest().getValue().toString());
				throw new WebApplicationException("Automation Result UPDATE: Canceling execution is only allowed by updating the Automation Request, not the result.\n"
						+ "Update this A. Request instead: " + updatedResource.getProducedByAutomationRequest().getValue().toString(), 500);
	        }
            
			log.error("Automation Result UPDATE: updating Automation Results that have not yet finished execution is currently not allowed");
			throw new WebApplicationException("Automation Result UPDATE: updating Automation Results that have not yet finished execution is currently not allowed", 500);
        }
	        
        // only update the properties that we allow to update
        updatedResource.setModified(new Date());
        updatedResource.setTitle(aResource.getTitle());
        updatedResource.setCreator(aResource.getCreator());
        updatedResource.setContributor(aResource.getContributor());
        updatedResource.setExtendedProperties(aResource.getExtendedProperties());
        // caution updating Contribution resources! -- seems like lyo.store doesnt update those properly as local resources (they would have duplicate attribute values)
        
        
        // for the generated code below
        aResource = updatedResource;
	    
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationResult(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Cannot update a resource that already does not exists: '" + uri + "'");
            throw new WebApplicationException("Cannot update a resource that already does not exists: '" + uri + "'", Status.NOT_FOUND);
        }
        aResource.setAbout(uri);
        try {
            store.updateResources(storePool.getDefaultNamedGraphUri(), aResource);
        } catch (StoreAccessException e) {
            log.error("Failed to update resource: '" + uri + "'", e);
            throw new WebApplicationException("Failed to update resource: '" + uri + "'", e);
        } finally {
            storePool.releaseStore(store);
        }
        updatedResource = aResource;
        // End of user code
        
        // Start of user code updateAutomationResult
        // End of user code
        return updatedResource;
    }
    public static AutomationPlan getAutomationPlan(HttpServletRequest httpServletRequest, final String id)
    {
        AutomationPlan aResource = null;
        
        // Start of user code getAutomationPlan_storeInit
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationPlan(id);
        try {
            aResource = store.getResource(storePool.getDefaultNamedGraphUri(), uri, AutomationPlan.class);
        } catch (NoSuchElementException e) {
            log.error("Resource: '" + uri + "' not found");
            throw new WebApplicationException("Failed to get resource: '" + uri + "'", e, Status.NOT_FOUND);
        } catch (StoreAccessException | ModelUnmarshallingException  e) {
            log.error("Failed to get resource: '" + uri + "'", e);
            throw new WebApplicationException("Failed to get resource: '" + uri + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code getAutomationPlan_storeFinalize
        // End of user code
        
        // Start of user code getAutomationPlan
        // End of user code
        return aResource;
    }


    public static Contribution getContribution(HttpServletRequest httpServletRequest, final String id)
    {
        Contribution aResource = null;
        
        // Start of user code getContribution_storeInit
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForContribution(id);
        try {
            aResource = store.getResource(storePool.getDefaultNamedGraphUri(), uri, Contribution.class);
        } catch (NoSuchElementException e) {
            log.error("Resource: '" + uri + "' not found");
            throw new WebApplicationException("Failed to get resource: '" + uri + "'", e, Status.NOT_FOUND);
        } catch (StoreAccessException | ModelUnmarshallingException  e) {
            log.error("Failed to get resource: '" + uri + "'", e);
            throw new WebApplicationException("Failed to get resource: '" + uri + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code getContribution_storeFinalize
        // End of user code
        
        // Start of user code getContribution
        // End of user code
        return aResource;
    }

    public static Boolean deleteContribution(HttpServletRequest httpServletRequest, final String id)
    {
        Boolean deleted = false;
        // Start of user code deleteContribution_storeInit
        Contribution contribToDelete = null;
        try {
        	contribToDelete = getContribution(null, id);
        } catch (Exception e) {
        	// means not found; let it fail on the generated "resourceExists()" below
        }
        
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForContribution(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Contribution DELETE: Cannot delete a resource that already does not exists: '" + uri + "'");
            throw new WebApplicationException("Contribution DELETE: Cannot delete a resource that already does not exists: '" + uri + "'", Status.NOT_FOUND);
        }
        store.deleteResources(storePool.getDefaultNamedGraphUri(), uri);
        storePool.releaseStore(store);
        deleted = true;
        // Start of user code deleteContribution_storeFinalize
        // End of user code
        
        // Start of user code deleteContribution

        // delete the associated file, if there is one
        if (contribToDelete != null) {
        	String filePath = contribToDelete.getFilePath();
        	if (filePath != null)
        	{
        		// TODO security issue - what if someone updates the contribution filePath to e.g. "/" --> rm -rf "/"
        		if (! (filePath.contains("compilation/SUT/") || filePath.contains("compilation\\SUT\\")) )
        		{
        			log.error("Contribution delete: Failed to delete associated file: SECURITY MEASURE - the path to the file seems to be outside of the expected SUT directory");
        		}
        		
		        try {
		    		File contribFile = FileSystems.getDefault().getPath(filePath).toFile();
		    		FileDeleteStrategy.FORCE.delete(contribFile);
				} catch (IOException e) {
					log.error("Contribution delete: Failed to delete associated file: " + e.getMessage());
				}
        	}
        }
        // End of user code
        return deleted;
    }

    public static Contribution updateContribution(HttpServletRequest httpServletRequest, Contribution aResource, final String id) {
        Contribution updatedResource = null;
        // Start of user code updateContribution_storeInit
        if (aResource == null)
        {
            log.error("Contribution UPDATE: received an empty request");
            throw new WebApplicationException("Contribution UPDATE: received an empty request", Status.BAD_REQUEST);
        }
		if (aResource.getTitle() == null || aResource.getTitle().isEmpty())
			throw new WebApplicationException("Contribution UPDATE: title property missing", Status.BAD_REQUEST);
        

        // get the current version of the Contribution 
        updatedResource = getContribution(null, id);
		
		// only update the properties we allow to update
        updatedResource.setModified(new Date());
        updatedResource.setTitle(aResource.getTitle());
        updatedResource.setDescription(aResource.getDescription());
        updatedResource.setCreator(aResource.getCreator());
        updatedResource.setExtendedProperties(aResource.getExtendedProperties());
        
        updatedResource.setValue(aResource.getValue());
        updatedResource.setValueType(aResource.getValueType());
        
        updatedResource.setFilePath(aResource.getFilePath());
        String filePath = aResource.getFilePath();
    	if (filePath != null)
    	{
			// TODO security issue - do not allow the filePath to be updated to outside the SUT directory, e.g. "/" --> danger of 'rm -rf "/"' on delete
			if (! (filePath.contains("compilation/SUT/") || filePath.contains("compilation\\SUT\\")) )
			{
				log.error("Contribution UPDATE: Failed to update filePath: SECURITY MEASURE - the path to the file seems to be outside of the expected SUT directory");
	            throw new WebApplicationException("Contribution UPDATE: Failed to update filePath: SECURITY MEASURE - the path to the file seems to be outside of the expected SUT directory", Status.INTERNAL_SERVER_ERROR);
			}
    	}
        
        // for the generated code below
        aResource = updatedResource;
        
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForContribution(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Cannot update a resource that already does not exists: '" + uri + "'");
            throw new WebApplicationException("Cannot update a resource that already does not exists: '" + uri + "'", Status.NOT_FOUND);
        }
        aResource.setAbout(uri);
        try {
            store.updateResources(storePool.getDefaultNamedGraphUri(), aResource);
        } catch (StoreAccessException e) {
            log.error("Failed to update resource: '" + uri + "'", e);
            throw new WebApplicationException("Failed to update resource: '" + uri + "'", e);
        } finally {
            storePool.releaseStore(store);
        }
        updatedResource = aResource;
        // Start of user code updateContribution_storeFinalize
        // End of user code
        
        // Start of user code updateContribution
        // End of user code
        return updatedResource;
    }

    public static String getETagFromAutomationPlan(final AutomationPlan aResource)
    {
        String eTag = null;
        // Start of user code getETagFromAutomationPlan
        
        if (aResource != null && aResource.getModified() != null)
        	eTag = Long.toString(aResource.getModified().getTime());
        
        // End of user code
        return eTag;
    }
    public static String getETagFromAutomationRequest(final AutomationRequest aResource)
    {
        String eTag = null;
        // Start of user code getETagFromAutomationRequest
        
        if (aResource != null && aResource.getModified() != null)
        	eTag = Long.toString(aResource.getModified().getTime());
        
        // End of user code
        return eTag;
    }
    public static String getETagFromAutomationResult(final AutomationResult aResource)
    {
        String eTag = null;
        // Start of user code getETagFromAutomationResult
        
        if (aResource != null && aResource.getModified() != null)
        	eTag = Long.toString(aResource.getModified().getTime());
        
        // End of user code
        return eTag;
    }
    public static String getETagFromContribution(final Contribution aResource)
    {
        String eTag = null;
        // Start of user code getETagFromContribution
        
        if (aResource != null && aResource.getCreated() != null)
        	eTag = Long.toString(aResource.getCreated().getTime());
        
        // End of user code
        return eTag;
    }

}
