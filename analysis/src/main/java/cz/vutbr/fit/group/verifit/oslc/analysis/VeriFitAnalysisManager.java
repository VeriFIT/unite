// Start of user code Copyright
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
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.RequestRunner;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.SutAnalyse;
import cz.vutbr.fit.group.verifit.oslc.analysis.clients.CompilationAdapterClient;
import cz.vutbr.fit.group.verifit.oslc.analysis.exceptions.OslcResourceException;
import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisProperties;

import org.eclipse.lyo.store.StoreAccessException;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.NoSuchElementException;
import java.util.Map;
import org.eclipse.lyo.client.OslcClient;	//TODO REMOVE LATER
import javax.ws.rs.core.Response;	//TODO REMOVE LATER

import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanLoading;

import static cz.vutbr.fit.group.verifit.oslc.analysis.utils.getResourceIdFromUri;
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
	
	/**
	 * Used to generate IDs for new resources in a synchronized way (datarace free)
	 * @author od42
	 */
	private static class ResourceIdGen {
	    int idCounter; 
	    
	    public ResourceIdGen ()
	    {
	    	idCounter = 0;
	    };
	    
	    public ResourceIdGen (int start)
	    {
	    	idCounter = start;
	    }
	    
	    /**
	     * Get a new ID. This increments the internal ID counter. Synchronized.
	     * @return New ID
	     */
	    public synchronized String getId() {
	    	idCounter++;
	        return Integer.toString(idCounter - 1);
	    }
	}
	
	/**
	 * Check if a string is a console command by finding it in the system PATH
	 * source - https://stackoverflow.com/a/23539220
	 * @param command Command to look for
	 * @return	Whether the command is in the system PATH
	 */
	private static Boolean isInPath(String command)
	{
		return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator))).map(Paths::get).anyMatch(path -> Files.exists(path.resolve(command)));
	}

	/**
	 * Creates an AutomationPlan resource with the specified properties, and stores in the Adapter's catalog.
	 * @param aResource			The new resource will copy properties from the specified aResource.
	 * @return					The newly created resource. Or null if one of the required properties was missing.
	 * @throws StoreAccessException If the triplestore is not accessible
	 * @throws OslcResourceException If the AutomationPlan to be created is missing some properties or is invalid
	 */
    public static AutomationPlan createAutomationPlan(final AutomationPlan aResource) throws StoreAccessException, OslcResourceException
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
			newResource = aResource;
			newResource.setAbout(VeriFitAnalysisResourcesFactory.constructURIForAutomationPlan(newID));
			
			// resources set by the service provider
			newResource.setIdentifier(newID);
			Date timestamp = new Date();
			newResource.setCreated(timestamp);
			newResource.setModified(timestamp);
			//newResource.setInstanceShape(new URI(VeriFitAnalysisProperties.PATH_RESOURCE_SHAPES + "automationPlan"));
			//newResource.addServiceProvider(new URI(VeriFitAnalysisProperties.PATH_AUTOMATION_SERVICE_PROVIDERS + serviceProviderId));
			//newResource.addType(new URI("http://open-services.net/ns/auto#AutomationPlan"));
			
			ParameterDefinition SUT = new ParameterDefinition();
			SUT.setDescription("Refference to an SUT resource to analyse. SUTs are created using the compilation provider."); //TODO
			SUT.setName("SUT");
			SUT.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ONE)));
			SUT.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING))); // TODO change to URI
			newResource.addParameterDefinition(SUT);

			ParameterDefinition outputFileRegex = new ParameterDefinition();
			outputFileRegex.setDescription("Files that change during execution and match this regex will "
					+ "be added as contributions to the Automation Result. The regex needs to match the "
					+ "whole filename."); //TODO
			outputFileRegex.setName("outputFileRegex");
			outputFileRegex.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
			outputFileRegex.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));
			outputFileRegex.setDefaultValue(".^");
			newResource.addParameterDefinition(outputFileRegex);

			ParameterDefinition zipOutputs = new ParameterDefinition();
			zipOutputs.setDescription("If set to true, then all file contributions will be ZIPed and provided as a single zip contribution");
			zipOutputs.setName("zipOutputs");
			zipOutputs.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
			zipOutputs.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_BOOL)));
			zipOutputs.setDefaultValue("false");
			newResource.addParameterDefinition(zipOutputs);

			ParameterDefinition timeout = new ParameterDefinition();
			timeout.setDescription("Timeout for the analysis. Zero means no timeout.");
			timeout.setName("timeout");
			timeout.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
			timeout.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_INTEGER)));
			timeout.setDefaultValue("0");
			newResource.addParameterDefinition(timeout);

			ParameterDefinition toolCommand = new ParameterDefinition();
			toolCommand.setDescription("Used to omit the analysis tool launch command while executing analysis. True means the tool " + 
			"will be used and False means the tool command will not be used. (eg. \"./tool ./sut args\" vs \"/sut args\").");
			toolCommand.setName("toolCommand");
			toolCommand.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
			toolCommand.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_BOOL)));
			toolCommand.setDefaultValue("true");
			newResource.addParameterDefinition(toolCommand);

			// persist in the triplestore
	        Store store = storePool.getStore();
	        try {
	            URI uri = newResource.getAbout();
	            
	            aResource.setAbout(uri);
	            try {
	                store.updateResources(storePool.getDefaultNamedGraphUri(), aResource);
	            } catch (StoreAccessException e) {
	                log.error("Failed to create resource: '" + aResource.getAbout() + "'", e);            
	                throw new WebApplicationException("Failed to create resource: '" + aResource.getAbout() + "'", e, Status.INTERNAL_SERVER_ERROR);
	            }
	        } finally {
	            storePool.releaseStore(store);
	        }
	        newResource = aResource;

		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (WebApplicationException e) {
			throw new StoreAccessException("AutomationPlan creation failed: " + e.getMessage());
		}
		
		return newResource;
    }
    
    /**
     * Creates an AutomationResult resource with the specified properties, and stores in the Adapter's catalog.
     * @param aResource			The new resource will copy properties from the specified aResource.
     * @param newID				ID to assign to the new Result (meant to be the same as the Request ID)
     * @return					The newly created resource. Or null if one of the required properties was missing.
     */
    public static AutomationResult createAutomationResult(final AutomationResult aResource, final String newID)
    {
    	AutomationResult newResource = null;
    	
    	// check that required properties are specified in the input parameter
    	if (aResource == null || aResource.getTitle() == null || aResource.getTitle().isEmpty() ||
    		aResource.getState() == null || aResource.getState().isEmpty() ||
    		aResource.getVerdict() == null || aResource.getVerdict().isEmpty() ||
    		aResource.getReportsOnAutomationPlan().getValue() == null)
    	{
    		return null;
    	}
        
		try {
			newResource = aResource;
			aResource.setAbout(VeriFitAnalysisResourcesFactory.constructURIForAutomationResult(newID));
			
			// resources set by the service provider
			newResource.setIdentifier(newID);
			Date timestamp = new Date();
			newResource.setCreated(timestamp);
			newResource.setModified(timestamp);
			//newResource.setInstanceShape(new URI(VeriFitAnalysisProperties.PATH_RESOURCE_SHAPES + "automationResult"));
			//newResource.addServiceProvider(new URI(VeriFitAnalysisProperties.PATH_AUTOMATION_SERVICE_PROVIDERS + serviceProviderId));
			newResource.setDesiredState(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_STATE_COMPLETE)));
			//newResource.addType(new URI("http://open-services.net/ns/auto#AutomationResult"));

			// persist in the triplestore
	        Store store = storePool.getStore();
	        try {
	            URI uri = newResource.getAbout();
	            
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

		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (WebApplicationException e) {
			System.out.println("WARNING: AutomationResult creation failed: " + e.getMessage());
		}
		
		return newResource;
    }
    
	/**
	 * Check that the AutomationRequest contains the necessary input parameters based on its AutoPlan, fill the AutomationResult with output parameters (default values),
	 * and return a simplified map of parameters (TODO refactor the map)
	 * @param execAutoRequest			The AutomationRequest to execute. Needs to have a valid executesAutomationPlan property.
	 * @param newAutoResult				The AutomationResult created by the AutoRequest.
	 * @throws OslcResourceException 	When the executed AutomationRequest properties are invalid or missing
	 * @return	A map of input parameters [ name, Pair(value,position) ]; position -1 means there was no position
	 */
	private static Map<String, Pair<String,Integer>> processAutoReqInputParams(AutomationRequest execAutoRequest, AutomationResult newAutoResult) throws OslcResourceException
	{
		// get the executed AutomationPlan resource			
		String execAutoPlanId = utils.getResourceIdFromUri(execAutoRequest.getExecutesAutomationPlan().getValue());
		AutomationPlan execAutoPlan = null;
		try {
			execAutoPlan = getAutomationPlan(null, execAutoPlanId);
		} catch (Exception e) {
			throw new OslcResourceException("AutomationPlan not found (id: " + execAutoPlanId + ")");			
		}
		
		/// check the input parameters and create a map of "name" -> ("value", position)
		Map<String, Pair<String,Integer>> inputParamsMap = new HashMap<String, Pair<String,Integer>>();
		
		// loop through autoPlan defined parameters to match them with the input params
		for (ParameterDefinition definedParam : execAutoPlan.getParameterDefinition())
		{
			Integer paramPosition;
			if (definedParam.getCommandlinePosition() != null)
			{
				paramPosition = definedParam.getCommandlinePosition();
			}
			else
			{
				paramPosition = -1;
			}
				
			// find the corresponding autoRequest input parameter
			boolean matched = false;
			for (ParameterInstance submittedParam : execAutoRequest.getInputParameter())
			{				
				if (definedParam.getName().equals(submittedParam.getName()))
				{
					
					// check if the value is allowed
					Boolean validValue = true;
					if (definedParam.getAllowedValue().size() > 0)
					{
						validValue = false;
						for (String allowedValue : definedParam.getAllowedValue())
						{
							if (allowedValue.equals(submittedParam.getValue()))
							{
								validValue = true;
								break;
							}
						}
					}
					if (!validValue)
					{
						throw new OslcResourceException("value '" + submittedParam.getValue() + "' not allowed for the '" + definedParam.getName() + "' parameter");
					}
					
					inputParamsMap.put(definedParam.getName(), Pair.of(submittedParam.getValue(),paramPosition));
					matched = true;
				}
			}
			// try to use the default value if no matching input param found
			if (!matched && definedParam.getDefaultValue() != null)
			{
				inputParamsMap.put(definedParam.getName(), Pair.of(definedParam.getDefaultValue(), paramPosition));
				matched = true;

				// add the default value as an output parameter to the Automation Result
				ParameterInstance outputParameter = null;
				outputParameter = new ParameterInstance();
				outputParameter.setName(definedParam.getName());
				outputParameter.setValue(definedParam.getDefaultValue());
				newAutoResult.addOutputParameter(outputParameter);
			}
			
			// check parameter occurrences
			Boolean paramMissing = false;
			switch (definedParam.getOccurs().getValue().toString())
			{
			case VeriFitAnalysisConstants.OSLC_OCCURS_ONE:
				// TODO check for more then one when there should be exactly one
			case VeriFitAnalysisConstants.OSLC_OCCURS_ONEorMany:
				if (!matched)
					paramMissing = true;
				break;
				
			case VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE:
				// TODO check for more then one when there should be max one
				break;

			case VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorMany:
				break;
			}
			
			if (paramMissing == true)
				throw new OslcResourceException("'" + definedParam.getName() + "' input parameter missing");
		}
		
		// check that there were no unknown input parameters
		for (ParameterInstance submittedParam : execAutoRequest.getInputParameter())
		{				
			boolean matched = false;
			for (ParameterDefinition definedParam : execAutoPlan.getParameterDefinition())
			{
				if (definedParam.getName().equals(submittedParam.getName()))
					matched = true;
			}
			
			if (!matched)
				throw new OslcResourceException("'" + submittedParam.getName() + "' input parameter not recognized");
		}
		
		return inputParamsMap;
	}    
    
	/**
     * TODO handmade because its abnormal (not as intended by lyo)
	 * @param httpServletRequest
	 * @param serviceProviderId
	 * @param contributionId
	 * @return
	 */
    public static File getContribution(HttpServletRequest httpServletRequest, final String serviceProviderId, final String contributionId)
    {
    	File aResource = null;

    	// decode slashes in the file path
		String filePath = contributionId.replaceAll("%2F", "/");
		
    	aResource = new File(filePath);

        return aResource;
    }
    
    /**
     * TODO handmade because its abnormal (not as intended by lyo)
     * @param httpServletRequest
     * @param aResource
     * @param serviceProviderId
     * @param contributionId
     * @return
     * @throws OslcResourceException
     */
    public static Contribution updateContribution(HttpServletRequest httpServletRequest, final Contribution aResource, final String serviceProviderId, final String contributionId) throws OslcResourceException {
        Contribution updatedResource = null;
        
        // Check that all the required properties are set
        if (aResource == null || aResource.getValue() == null)
        {
        	throw new OslcResourceException("Failed to write a Contribution file - Value missing");        	
        }

    	// decode slashes in the file path
		String filePath = contributionId.replaceAll("%2F", "/");
		
        // write the file - path is getPath and content is getValue
		try (PrintWriter out = new PrintWriter(filePath))
		{
		    out.print(aResource.getValue());
		} catch (IOException e)
		{
			throw new OslcResourceException("WARNING: Failed to write a Contribution file: " + e.getMessage());
		}
		
		updatedResource = aResource;	// TODO not changed and has no URI because its not GETable
		
        return updatedResource;
    }
    
    /**
     * A map of queues of Automation Request runners. Each queue is meant to be used for a different Automation Plan (key to the map).
     * The first runner in each queue will be started. 
     */
    public static class RequestRunnerQueues {
    	
    	private Map<String,List<RequestRunner>> queueMap;
    	
    	public RequestRunnerQueues()
    	{
    		queueMap = new HashMap<String,List<RequestRunner>>();
    	}

    	public synchronized Boolean queueExists(String queueName) 
    	{
    		return (queueMap.get(queueName) != null);
    	}
    	
    	public synchronized Boolean empty(String queueName) 
    	{
    		List<RequestRunner> queue = queueMap.get(queueName);
    		if(queue == null)
    			return true;
    		else
    			return queue.isEmpty();
    	}
    	
    	/**
    	 * Adds a runner into a named queue. The runner will be started when it reaches the front of the queue (immediately if the queue is empty).
    	 * @param queueName
    	 * @param runner
    	 */
    	public synchronized void queueUp(String queueName, RequestRunner runner)
    	{	
    		// check if the queue exists - create it if it doesnt
    		if (queueMap.get(queueName) == null)
    		{
    			queueMap.put(queueName, new LinkedList<RequestRunner>());
    		}
    		
    		// queue up
    		queueMap.get(queueName).add(runner);
    		
    		// start execution if first in line
    		if (queueMap.get(queueName).size() == 1)
    			runner.start();
    	}
    	
    	/**
    	 * Removes the first runner from a named queue. Meant to be called after the runner has finished execution. Will start the next runner in line.
    	 * @param queueName
    	 */
    	public synchronized void popFirst(String queueName)
    	{
    		List<RequestRunner> queue = queueMap.get(queueName);
    		if (!(queue == null)) // make sure the queue exists
    		{
    			queue.remove(0);
    			if (!(queue.isEmpty()))	// start the next runner if there is one
    				queue.get(0).start();
    		}
    	}
    };
    
    /**
     * Call this function at the end of an Automation Requests execution. If the request is part of a queue, then it will be removed from it and the next requets will start its execution.
     * @param req
     */
    public static void finishedAutomationRequestExecution(AutomationRequest req)
    {
    	String autoPlanId = utils.getResourceIdFromUri(req.getExecutesAutomationPlan().getValue());
    	
    	// if there is a request queue for this requests Automation Plan, then it means this request execution is currently
    	// at the front of the queue and needs to be removed
    	if (AutoRequestQueues.queueExists(autoPlanId))
    	{
    		AutoRequestQueues.popFirst(autoPlanId);
    	}
    }

	// End of user code

    public static void contextInitializeServletListener(final ServletContextEvent servletContextEvent)
    {
        
        // Start of user code contextInitializeServletListener
	
    	// load configuration
    	try {
    		VeriFitAnalysisProperties.loadProperties();
    	} catch (IOException e) {
			System.out.println("ERROR: Adapter configuration: Failed to load Java properties: " + e.getMessage());
			System.exit(1);
		}

        // End of user code
        // Start of user code StoreInitialise
    	// connect to the triplestore
        // End of user code
        Properties lyoStoreProperties = new Properties();
        String lyoStorePropertiesFile = StorePool.class.getResource("/store.properties").getFile();
        try {
            lyoStoreProperties.load(new FileInputStream(lyoStorePropertiesFile));
        } catch (IOException e) {
            log.error("Failed to initialize Store. properties file for Store configuration could not be loaded.", e);
            throw new RuntimeException(e);
        }
        
        int initialPoolSize = Integer.parseInt(lyoStoreProperties.getProperty("initialPoolSize"));
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
        Store store = storePool.getStore();
		try {
			// Check the triplestore connection. Dont care if the graph exists or not, will be created later if needed
			store.namedGraphExists(new URI(VeriFitAnalysisProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES));
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (Exception e) {
			String hint = "Is the triplestore running?";
			if (e.getMessage() == null)
				hint = "Are the endpoints correct? (server context, data set, endpoint)";
	
			System.out.println("ERROR: Adapter configuration: SPARQL triplestore initialization failed: " + e.getMessage()
			+ "\n  Current configuration:\n    - query endpoint: " + sparqlQueryEndpoint + "\n    - update endpoint: " + sparqlUpdateEndpoint
			+ "\n\n  " + hint);
			System.exit(1);
		}
        
    	// create AutomationPlans
		try {
			AutomationPlanLoading.loadAutomationPlans();
		} catch (StoreAccessException e) {
			System.out.println("ERROR: Adapter initialization: AutomationPlan creation: " + e.getMessage());
			System.exit(1);
		}

		// check what the last AutomationRequest ID is
    	// requests have a numerical ID
    	int initReqId = 0;
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
						int reqId = Integer.parseInt(getResourceIdFromUri(autoReq.getAbout()));
						if (reqId > currMaxReqId)
						{
							currMaxReqId = reqId;
						}
					}
				}
    		}
		} catch (WebApplicationException e) { 
			System.out.println("ERROR: Adapter initialization: Failed to get latest AutomationRequest ID: " + e.getMessage());
			System.exit(1);
		}		
    	initReqId = currMaxReqId + 1;
		AutoRequestIdGen = new ResourceIdGen(initReqId);
        
        // End of user code
        
    }

    public static void contextDestroyServletListener(ServletContextEvent servletContextEvent) 
    {
        
        // Start of user code contextDestroyed
        // TODO Implement code to shutdown connections to data backbone etc...
        // End of user code
    }

    public static ServiceProviderInfo[] getServiceProviderInfos(HttpServletRequest httpServletRequest)
    {
        ServiceProviderInfo[] serviceProviderInfos = {};
        
        // Start of user code "ServiceProviderInfo[] getServiceProviderInfos(...)"

        ServiceProviderInfo r1 = new ServiceProviderInfo();
        r1.name = "VeriFit Analysis Provider";
        r1.serviceProviderId = VeriFitAnalysisConstants.AUTOMATION_PROVIDER_ID;

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
        // TODO Implement code to return a set of resources.
        // An empty List should imply that no resources where found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
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
        // End of user code
        
        // Start of user code queryAutomationResults
        // TODO Implement code to return a set of resources.
        // An empty List should imply that no resources where found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
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
        // TODO Implement code to return a set of resources, based on search criteria 
        // An empty List should imply that no resources where found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
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
        // TODO Implement code to return a set of resources, based on search criteria 
        // An empty List should imply that no resources where found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
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
        // TODO Implement code to return a set of resources, based on search criteria 
        // An empty List should imply that no resources where found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return resources;
    }
    public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource) throws OslcResourceException
    {
        AutomationRequest newResource = null;
        
        // Start of user code createAutomationRequest_storeInit
        
        // TODO check triplestore online
        // ......
        String requestState = null;
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
	        
			// copy the properties specified in the POST request
			String newID = AutoRequestIdGen.getId();
			newResource = aResource;
			newResource.setAbout(VeriFitAnalysisResourcesFactory.constructURIForAutomationRequest(newID));
			
			// resources set by the service provider
			newResource.setIdentifier(newID);
			Date timestamp = new Date();
			newResource.setCreated(timestamp);
			newResource.setModified(timestamp);
			//newResource.setInstanceShape(new URI(AnacondaAdapterConstants.PATH_RESOURCE_SHAPES + "automationRequest"));
			//newResource.addServiceProvider(new URI(AnacondaAdapterConstants.PATH_AUTOMATION_SERVICE_PROVIDERS + serviceProviderId));
			newResource.setDesiredState(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_STATE_COMPLETE)));
			//newResource.addType(new URI("http://open-services.net/ns/auto#AutomationRequest"));
			

			// create an AutomationResult for this AutoRequest; output parameters will be set by processAutoReqInputParams()
			AutomationResult propAutoResult = new AutomationResult();
			propAutoResult.setTitle("Result - " + newResource.getTitle());
			propAutoResult.setReportsOnAutomationPlan(newResource.getExecutesAutomationPlan());
			propAutoResult.setProducedByAutomationRequest(VeriFitAnalysisResourcesFactory.constructLinkForAutomationRequest(newID));
			propAutoResult.setInputParameter(newResource.getInputParameter());
			propAutoResult.setContributor(newResource.getContributor());
			propAutoResult.setCreator(newResource.getCreator());
			propAutoResult.addVerdict(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_VERDICT_UNAVAILABLE)));
			
			// get the executed autoPlan, check input parameters, add output parameters to the AutoResult, and make an input map for the runner
			Map<String, Pair<String,Integer>> inputParamsMap = processAutoReqInputParams(newResource, propAutoResult);
			
			// check that the SUT to be executed exists
			SUT executedSUT = null;
			try {
				String sutURI = inputParamsMap.get("SUT").getLeft();
				executedSUT = CompilationAdapterClient.getSUT(sutURI);
			} catch (Exception e) {
				throw new IOException("Failed to get refferenced SUT - make sure the compilation provider is running.\nClient response: " + e.getMessage());
			}
			
			// check if the SUT launch command or the SUT build command should be used and get it from the SUT
			Pair<String,Integer> launchSUT = inputParamsMap.get("launchSUT");
			Pair<String,Integer> SUTbuildCommand = inputParamsMap.get("SUTbuildCommand");
			if (launchSUT != null)
			{
				String launchCmd = executedSUT.getLaunchCommand();
				if (launchCmd == null)
					throw new OslcResourceException("paramer launchSUT - referenced SUT is missing a launchCommand");
				else	
					inputParamsMap.put("launchSUT", Pair.of(launchCmd, Integer.parseInt(launchSUT.getLeft())));
			}
			if (SUTbuildCommand != null)
			{
				String buildCmd = executedSUT.getBuildCommand();
				if (buildCmd == null)
					throw new OslcResourceException("paramer SUTbuildCommand - referenced SUT is missing a buildCommand");
				else	
					inputParamsMap.put("SUTbuildCommand", Pair.of(buildCmd, Integer.parseInt(SUTbuildCommand.getLeft())));
			}

			if (launchSUT != null || SUTbuildCommand != null)
			{
				// modify the output parameters of the automation result (was added by processAutoReqInputParams)
				for (ParameterInstance param : propAutoResult.getOutputParameter())
				{
					if (param.getName().equals("launchSUT"))
					{
						param.setValue(executedSUT.getLaunchCommand());
					}
					else if (param.getName().equals("SUTbuildCommand"))
					{
						param.setValue(executedSUT.getBuildCommand());
					}
				}
			}

			// load the automation plan's configuration from the conf. manager
			Boolean oneInstanceOnly = AutomationPlanConfManager.getInstance()
					.getAutoPlanConf(getResourceIdFromUri(newResource.getExecutesAutomationPlan().getValue()))
					.getOneInstanceOnly();

			// check whether the new Auto Request can start execution immediately or needs to wait in a queue
			// and set its state accordingly (same for the Auto Result)
			if (oneInstanceOnly == false) // no instance restrictions --> can run
			{
				requestState = VeriFitAnalysisConstants.AUTOMATION_STATE_INPROGRESS;
			}
			else	// only one instance allowed at a time --> check queue
			{
				requestState = VeriFitAnalysisConstants.AUTOMATION_STATE_QUEUED;
			}			
			newResource.addState(new Link(new URI(requestState)));
			propAutoResult.addState(new Link(new URI(requestState)));
			
			// persist the AutomationResult and set the setProducedAutomationResult() link for the AutoRequest
		    AutomationResult newAutoResult = VeriFitAnalysisManager.createAutomationResult(propAutoResult, newID);
			newResource.setProducedAutomationResult(new Link(newAutoResult.getAbout())); // TODO

			// create a new thread to execute the automation request - and start it OR queue it up
			runner = new SutAnalyse(newResource, newAutoResult, executedSUT, inputParamsMap);
			
			
		} catch (OslcResourceException | RuntimeException | IOException e) {
			throw new OslcResourceException("AutomationRequest NOT created - " + e.getMessage());
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (Exception e) {
			System.out.println("WARNING: AutomationResquest creation failed: " + e.getMessage());
			throw new OslcResourceException("AutomationRequest NOT created - " + e.getMessage());
		}
		
        
        // End of user code
        Store store = storePool.getStore();
        try {
            URI uri = null;
            // Start of user code createAutomationRequest_storeSetUri
            //TODO: Set the uri of the resource to be created. Replace this code within the protected user code.
            
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
        
        if (requestState.equals(VeriFitAnalysisConstants.AUTOMATION_STATE_INPROGRESS))
		{
			runner.start();
		}
		else	
		{
			AutoRequestQueues.queueUp(
					utils.getResourceIdFromUri(newResource.getExecutesAutomationPlan().getValue()),
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
        // TODO Implement code to return a resource
        // return 'null' if the resource was not found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
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
        // End of user code
        
        // Start of user code getAutomationResult
        // TODO Implement code to return a resource
        // return 'null' if the resource was not found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
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
        // TODO Implement code to return a resource
        // return 'null' if the resource was not found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
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
        // TODO Implement code to return a resource
        // return 'null' if the resource was not found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
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