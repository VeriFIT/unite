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
import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;
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
import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException;
import org.apache.commons.io.FileUtils;
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
	
	static RequestRunnerQueues AutoRequestQueues = new RequestRunnerQueues();

    // End of user code
    
    
    // Start of user code class_methods
	
	private static int getCurrentHighestAutomationRequestId() throws Exception
	{
    	int currMaxReqId = 0;
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
						int reqId = Integer.parseInt(Utils.getResourceIdFromUri(autoReq.getAbout()));
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
			newResource.setParameterDefinition(aResource.getParameterDefinition());
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
     * Call this function at the end of an Automation Requests execution. If the request is part of a queue, then it will be removed from it and the next requets will start its execution.
     * @param req
     */
    public static void finishedAutomationRequestExecution(AutomationRequest req)
    {
    	String autoPlanId = Utils.getResourceIdFromUri(req.getExecutesAutomationPlan().getValue());
    	
    	// if there is a request queue for this requests Automation Plan, then it means this request execution is currently
    	// at the front of the queue and needs to be removed
    	if (AutoRequestQueues.queueExists(autoPlanId))
    	{
    		AutoRequestQueues.popFirst(autoPlanId);
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
			if (param.getName().equals("launchSUT") || param.getName().equals("SUTbuildCommand"))
			{
				// add the value as an execution parameter
				ExecutionParameter newSpecialExecParam = null;
				
				if (param.getName().equals("launchSUT"))
				{
					String launchCmd = executedSUT.getLaunchCommand();
					if (launchCmd == null)
						throw new OslcResourceException("paramer launchSUT - referenced SUT is missing a launchCommand");
					if (Integer.valueOf(param.getValue()) >= 0) // only create the execution parameter if value was non-negative
						newSpecialExecParam = new ExecutionParameter(
							param.getName(),
							launchCmd,
							Integer.parseInt(param.getValue())
						);
				}
				else if (param.getName().equals("SUTbuildCommand"))
				{
					String buildCmd = executedSUT.getBuildCommand();
					if (buildCmd == null)
						throw new OslcResourceException("paramer SUTbuildCommand - referenced SUT is missing a buildCommand");
					if (Integer.valueOf(param.getValue()) >= 0) // only create the execution parameter if value was non-negative
						newSpecialExecParam = new ExecutionParameter(
							param.getName(),
							buildCmd,
							Integer.parseInt(param.getValue())
						);
				}
				execParams.add(newSpecialExecParam);
			}
		}
		// same thing for output parameters
		for ( ParameterInstance param : newAutoResult.getOutputParameter())
		{ 
			if (param.getName().equals("launchSUT") || param.getName().equals("SUTbuildCommand"))
			{
				// add the value as an execution parameter
				ExecutionParameter newSpecialExecParam = null;
				
				if (param.getName().equals("launchSUT"))
				{
					String launchCmd = executedSUT.getLaunchCommand();
					if (launchCmd == null)
						throw new OslcResourceException("paramer launchSUT - referenced SUT is missing a launchCommand");
					if (Integer.valueOf(param.getValue()) >= 0) // only create the execution parameter if value was non-negative
						newSpecialExecParam = new ExecutionParameter(
							param.getName(),
							launchCmd,
							Integer.parseInt(param.getValue())
						);
				}
				else if (param.getName().equals("SUTbuildCommand"))
				{
					String buildCmd = executedSUT.getBuildCommand();
					if (buildCmd == null)
						throw new OslcResourceException("paramer SUTbuildCommand - referenced SUT is missing a buildCommand");
					if (Integer.valueOf(param.getValue()) >= 0) // only create the execution parameter if value was non-negative
						newSpecialExecParam = new ExecutionParameter(
							param.getName(),
							buildCmd,
							Integer.parseInt(param.getValue())
						);
				}
				execParams.add(newSpecialExecParam);
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
    		Contribution fullContrib = getContribution(httpServletRequest, Utils.getResourceIdFromUri(contrib.getAbout()));
    		aResource.addContribution(fullContrib);
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
    	int initReqId = 0;
		try {
			initReqId = getCurrentHighestAutomationRequestId() + 1;
		} catch (Exception e) {
			log.error("Adapter initialization: Failed to get latest AutomationRequest ID: " + e.getMessage());
			System.exit(1);
		}
		AutoRequestIdGen = new ResourceIdGen(initReqId);
        
        // End of user code
        
    }

    public static void contextDestroyServletListener(ServletContextEvent servletContextEvent) 
    {
        
        // Start of user code contextDestroyed
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
			
			
			// create a basic Automation Request (with core properties like creation time, identifier, ...)
			// and set relevant properties from the Automation Request received from the client
			String newID = AutoRequestIdGen.getId();
			newResource = VeriFitAnalysisResourcesFactory.createAutomationRequest(newID);
			newResource.setInputParameter(aResource.getInputParameter());
			newResource.setTitle(aResource.getTitle());
			newResource.setDescription(aResource.getDescription());
			newResource.setExecutesAutomationPlan(aResource.getExecutesAutomationPlan());
			newResource.setCreator(aResource.getCreator());
			newResource.setContributor(aResource.getContributor());
			newResource.setExtendedProperties(aResource.getExtendedProperties());
			
			
			// get the executed autoPlan and load its configuration
			String execAutoPlanId = Utils.getResourceIdFromUri(newResource.getExecutesAutomationPlan().getValue());
			AutomationPlan execAutoPlan = null;
			try {
				execAutoPlan = getAutomationPlan(null, execAutoPlanId);
			} catch (Exception e) {
				throw new OslcResourceException("AutomationPlan not found (id: " + execAutoPlanId + ")");			
			}
			AutomationPlanConf execAutoPlanConf = AutomationPlanConfManager.getInstance().getAutoPlanConf(execAutoPlanId);

			// check whether the new Auto Request can start execution immediately or needs to wait in a queue
			// and set its state accordingly
			if (execAutoPlanConf.getOneInstanceOnly() == false) {	// no instance restrictions --> can run
				newResource.replaceState(OslcValues.AUTOMATION_STATE_INPROGRESS);
			} else {	// only one instance allowed at a time --> check queue
				newResource.replaceState(OslcValues.AUTOMATION_STATE_QUEUED);
			}			
			
			
			// check input parameters, and create output parameters
			Set<ParameterInstance> outputParams = Utils.checkInputParamsAndProduceOuputParams(newResource.getInputParameter(), execAutoPlan.getParameterDefinition());

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
        Link requestState = newResource.getState().iterator().next(); // TODO will always contain one state
        if (requestState.equals(OslcValues.AUTOMATION_STATE_INPROGRESS))
        {
			runner.start();
		}
        else
		{
			AutoRequestQueues.queueUp(
					Utils.getResourceIdFromUri(newResource.getExecutesAutomationPlan().getValue()),
					runner);
		}
        
        // End of user code
        
        // Start of user code createAutomationRequest
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
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationRequest(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Cannot delete a resource that does not already exists: '" + uri + "'");
            throw new WebApplicationException("Cannot delete a resource that does not already exists: '" + uri + "'", Status.NOT_FOUND);
        }
        store.deleteResources(storePool.getDefaultNamedGraphUri(), uri);
        storePool.releaseStore(store);
        deleted = true;
        // Start of user code deleteAutomationRequest_storeFinalize
        // End of user code
        
        // Start of user code deleteAutomationRequest
        // TODO Implement code to delete a resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return deleted;
    }

    public static AutomationRequest updateAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String id) {
        AutomationRequest updatedResource = null;
        // Start of user code updateAutomationRequest_storeInit

        aResource.setModified(new Date());
  
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationRequest(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Cannot update a resource that does not already exists: '" + uri + "'");
            throw new WebApplicationException("Cannot update a resource that does not already exists: '" + uri + "'", Status.NOT_FOUND);
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
        // TODO Implement code to update and return a resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
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
        // End of user code
        return aResource;
    }

    public static Boolean deleteAutomationResult(HttpServletRequest httpServletRequest, final String id)
    {
        Boolean deleted = false;
        // Start of user code deleteAutomationResult_storeInit
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationResult(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Cannot delete a resource that does not already exists: '" + uri + "'");
            throw new WebApplicationException("Cannot delete a resource that does not already exists: '" + uri + "'", Status.NOT_FOUND);
        }
        store.deleteResources(storePool.getDefaultNamedGraphUri(), uri);
        storePool.releaseStore(store);
        deleted = true;
        // Start of user code deleteAutomationResult_storeFinalize
        // End of user code
        
        // Start of user code deleteAutomationResult
        // TODO Implement code to delete a resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return deleted;
    }

    public static AutomationResult updateAutomationResult(HttpServletRequest httpServletRequest, final AutomationResult aResource, final String id) {
        AutomationResult updatedResource = null;
        // Start of user code updateAutomationResult_storeInit

        aResource.setModified(new Date());
  
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForAutomationResult(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Cannot update a resource that does not already exists: '" + uri + "'");
            throw new WebApplicationException("Cannot update a resource that does not already exists: '" + uri + "'", Status.NOT_FOUND);
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
        // Start of user code updateAutomationResult_storeFinalize
        // End of user code
        
        // Start of user code updateAutomationResult
        // TODO Implement code to update and return a resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
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


    public static Contribution updateContribution(HttpServletRequest httpServletRequest, final Contribution aResource, final String id) {
        Contribution updatedResource = null;
        // Start of user code updateContribution_storeInit
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitAnalysisResourcesFactory.constructURIForContribution(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Cannot update a resource that does not already exists: '" + uri + "'");
            throw new WebApplicationException("Cannot update a resource that does not already exists: '" + uri + "'", Status.NOT_FOUND);
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
        // TODO Implement code to update and return a resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
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
