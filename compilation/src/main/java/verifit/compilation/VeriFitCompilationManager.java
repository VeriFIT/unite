// Start of user code Copyright
/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *  
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Eclipse Distribution License is available at
 *  http://www.eclipse.org/org/documents/edl-v10.php.
 *  
 *  Contributors:
 *  
 *	   Sam Padgett	       - initial API and implementation
 *     Michael Fiedler     - adapted for OSLC4J
 *     Jad El-khoury        - initial implementation of code generator (https://bugs.eclipse.org/bugs/show_bug.cgi?id=422448)
 *     Matthieu Helleboid   - Support for multiple Service Providers.
 *     Anass Radouani       - Support for multiple Service Providers.
 *
 * This file is generated by org.eclipse.lyo.oslc4j.codegenerator
 *******************************************************************************/
// End of user code

package verifit.compilation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContextEvent;
import java.util.List;

import org.eclipse.lyo.oslc4j.core.model.ServiceProvider;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import verifit.compilation.servlet.ServiceProviderCatalogSingleton;
import verifit.compilation.ServiceProviderInfo;
import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc.domains.auto.ParameterInstance;
import org.eclipse.lyo.oslc.domains.Person;
import verifit.compilation.resources.SUT;
import verifit.compilation.resources.TextOut;


// Start of user code imports
import verifit.compilation.persistance.Persistence;
import verifit.compilation.automationPlans.AutomationPlanDefinition;
import verifit.compilation.automationPlans.SutDeployAutoPlanExecution;
import verifit.compilation.exceptions.OslcResourceException;

import org.eclipse.lyo.store.StoreAccessException;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.NoSuchElementException;
import java.util.Map;
// End of user code

// Start of user code pre_class_code
// End of user code

public class VeriFitCompilationManager {

    // Start of user code class_attributes	
	static Persistence store;
	
	static ResourceIdGen AutoPlanIdGen;
	static ResourceIdGen AutoRequestIdGen;
	
    // End of user code
    
    
    // Start of user code class_methods
	/**
	 * Creates the tmp directory for saving programs to analyze
	 */
	private static void createTmpDir()
	{
		File programDir = new File("tmp/");
	    if (!programDir.exists())
	    {
	    	programDir.mkdirs();
	    }
	}
	
	/**
	 * Deletes the tmp directory to cleanup
	 * @throws IOException
	 */
	private static void deleteTmpDir() throws IOException
	{
		File programDir = new File("tmp/");
		FileUtils.deleteDirectory(programDir);
	}
	
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
	 * Get the ID of an OSLC resource from its URI (About(), or Link)
	 * @param	uri	OSLC resource uri (eg. from a Link)
	 * @return 		ID of the OSLC resource
	 */
	public static String getResourceIdFromUri(URI uri)
	{
		String uriPath = uri.getPath();
		return uriPath.substring(uriPath.lastIndexOf('/') + 1);
	}
	
	
	/**
	 * Creates an AutomationPlan resource with the specified properties, and stores in the Adapter's catalog.
	 * @param aResource			The new resource will copy properties from the specified aResource.
	 * @return					The newly created resource. Or null if one of the required properties was missing.
	 * @throws StoreAccessException 
	 */
    public static AutomationPlan createAutomationPlan(final AutomationPlan aResource) throws StoreAccessException
    {    	
    	AutomationPlan newResource = null;
    	
    	// check that required properties are specified in the input parameter
    	if (aResource == null || aResource.getTitle() == null || aResource.getTitle().isEmpty())
    	{
    		return null;
    	}
        
		try {
			String newID = AutoPlanIdGen.getId();
			newResource = aResource;
			newResource.setAbout(VeriFitCompilationResourcesFactory.constructURIForAutomationPlan(VeriFitCompilationConstants.AUTOMATION_PROVIDER_ID, newID));
			
			// resources set by the service provider
			newResource.setIdentifier(newID);
			Date timestamp = new Date();
			newResource.setCreated(timestamp);
			newResource.setModified(timestamp);
			//newResource.setInstanceShape(new URI(VeriFitCompilationProperties.PATH_RESOURCE_SHAPES + "automationPlan"));
			//newResource.addServiceProvider(new URI(VeriFitCompilationProperties.PATH_AUTOMATION_SERVICE_PROVIDERS + serviceProviderId));
			//newResource.addType(new URI("http://open-services.net/ns/auto#AutomationPlan"));
			
			// persist in the triplestore
			store.updateResources(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), newResource);

		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (StoreAccessException e) {
			throw new StoreAccessException("AutomationPlan creation failed: " + e.getMessage());
		}
		
		return newResource;
    }
    

    /**
     * Updates an AutomationRequest in the Adapter's catalog. The old resource is replaced with the new one.
     * @param changedResource		This resource will be used as replacement for the old resource.
     * @param serviceProviderId		ID of the service provider for the updated resource.
     * @param automationRequestId	ID of the AutomationRequest to update
     * @return						The updated resource.
     */
    public static AutomationRequest updateAutomationRequest(AutomationRequest changedResource, final String serviceProviderId, final String automationRequestId)
    {
    	AutomationRequest updatedResource = null;

    	changedResource.setModified(new Date());
  
    	try {
    		
			store.updateResources(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), changedResource);
			
		} catch (StoreAccessException e) {
			System.out.println("WARNING: AutomationRequest update failed: " + e.getMessage());
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
    	updatedResource = changedResource;
    	
        return updatedResource;
    }
    
    /**
     * Creates an AutomationResult resource with the specified properties, and stores in the Adapter's catalog.
     * @param aResource			The new resource will copy properties from the specified aResource.
     * @param serviceProviderId	ID of the service provider for the new resource.
     * @param newID				ID to assign to the new Result (meant to be the same as the Request ID)
     * @return					The newly created resource. Or null if one of the required properties was missing.
     */
    public static AutomationResult createAutomationResult(final AutomationResult aResource, final String serviceProviderId, final String newID)
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
			aResource.setAbout(VeriFitCompilationResourcesFactory.constructURIForAutomationResult(serviceProviderId, newID));
			
			// resources set by the service provider
			newResource.setIdentifier(newID);
			Date timestamp = new Date();
			newResource.setCreated(timestamp);
			newResource.setModified(timestamp);
			//newResource.setInstanceShape(new URI(VeriFitCompilationProperties.PATH_RESOURCE_SHAPES + "automationResult"));
			//newResource.addServiceProvider(new URI(VeriFitCompilationProperties.PATH_AUTOMATION_SERVICE_PROVIDERS + serviceProviderId));
			newResource.setDesiredState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_COMPLETE)));
			//newResource.addType(new URI("http://open-services.net/ns/auto#AutomationResult"));
		
			// persist in the triplestore
			store.updateResources(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), newResource);
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (StoreAccessException e) {
			System.out.println("WARNING: AutomationResult creation failed: " + e.getMessage());
		}
		
		return newResource;
    }
    
    /**
	 * Creates a TextOut resource with the specified properties, and stores in the Adapter's catalog.
	 * @param aResource			The new resource will copy properties from the specified aResource.
	 * @param serviceProviderId	ID of the service provider for the new resource.
	 * @param newID				ID for the new resource
	 * @return					The newly created resource. Or null if one of the required properties was missing.
	 */
    public static TextOut createTextOut(final TextOut aResource, final String serviceProviderId, final String newID)
    {
    	TextOut newResource = null;

    	// check that required properties are specified in the input parameter
    	if (aResource == null)
    	{
    		return null;
    	}
    	
		newResource = aResource;
		newResource.setCreated(new Date());
		// TODO set about / id

		return newResource;
    }
    

	/**
	 * Check that the AutomationRequest contains the neccesary input parameters based on its AutoPlan
	 * and return a map of them.
	 * @param serviceProviderId			serviceProviderId passed by the HTTP request
	 * @param execAutoRequest			The AutomationRequest to execute. Needs to have a valid
	 * 									executesAutomationPlan property.
	 * @throws OslcResourceException 	When the executed AutomationRequest properties are invalid or missing
	 */
	private static Map<String, String> getAutoReqInputParams(String serviceProviderId, AutomationRequest execAutoRequest) throws OslcResourceException
	{
		// get the executed AutomationPlan resource			
		String execAutoPlanId = getResourceIdFromUri(execAutoRequest.getExecutesAutomationPlan().getValue());
		AutomationPlan execAutoPlan = getAutomationPlan(null, serviceProviderId, execAutoPlanId);
		if (execAutoPlan == null)
			throw new OslcResourceException("AutomationPlan not found (id: " + execAutoPlanId + ")");
		
		/// check the input parameters and create a map of "name" -> "value"
		Map<String, String> inputParamsMap = new HashMap<String, String>();
		int countMatchedParams = 0;
		
		// loop through autoPlan defined parameters to match them with the input params
		for (ParameterDefinition definedParam : execAutoPlan.getParameterDefinition())
		{
			// set the default value (will be overwritten later), may be null if not set
			inputParamsMap.put(definedParam.getName(), definedParam.getDefaultValue());
				
			// find the corresponding autoRequest input parameter
			for (ParameterInstance submittedParam : execAutoRequest.getInputParameter())
			{				
				if (definedParam.getName().equals(submittedParam.getName()))
				{
					countMatchedParams++;
					
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
					
					inputParamsMap.put(definedParam.getName(), submittedParam.getValue());
				}
			}
			
			// check parameter occurrences
			Boolean paramMissing = false;
			switch (definedParam.getOccurs().getValue().toString())
			{
			case VeriFitCompilationConstants.OSLC_OCCURS_ONE:
				// TODO check for more then one when there should be exactly one
			case VeriFitCompilationConstants.OSLC_OCCURS_ONEorMany:
				if (inputParamsMap.get(definedParam.getName()) == null)
					paramMissing = true;
				break;
				
			case VeriFitCompilationConstants.OSLC_OCCURS_ZEROorONE:
				// TODO check for more then one when there should be max one
				break;

			case VeriFitCompilationConstants.OSLC_OCCURS_ZEROorMany:
				break;
			}
			
			if (paramMissing == true)
				throw new OslcResourceException("'" + definedParam.getName() + "' input parameter missing");
		}
		
		// check that there were no unknown input parameters
		if (countMatchedParams != execAutoRequest.getInputParameter().size())
		{
			throw new OslcResourceException("unrecognized input parameters");
		}
		
		return inputParamsMap;
	}
	
	
    /**
	 * Creates an SUT resource with the specified properties, and stores in the Adapter's catalog.
     * @param aResource			The new resource will copy properties from the specified aResource.
     * @param serviceProviderId	ID of the service provider for the new resource.
     * @param newID				ID to assign to the new SUT (meant to be the same as the Request ID)
     * @return					The newly created resource. Or null if one of the required properties was missing.
     */
    public static SUT createSUT(final SUT aResource, final String serviceProviderId, final String newID) 
    {    	
    	SUT newResource = null;
    	
    	// check that required properties are specified in the input parameter
    	if (aResource == null || aResource.getTitle() == null || aResource.getTitle().isEmpty()) // TODO
    	{
    		return null;
    	}
        
		try {
			newResource = aResource;
			newResource.setAbout(VeriFitCompilationResourcesFactory.constructURIForSUT(serviceProviderId, newID));
			
			// resources set by the service provider
			Date timestamp = new Date();
			newResource.setCreated(timestamp);
			newResource.setModified(timestamp);
			newResource.setIdentifier(newID);
			//TODO
			
			// persist in the triplestore
			store.updateResources(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), newResource);

		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (StoreAccessException e) {
			System.out.println("WARNING: SUT creation failed: " + e.getMessage());
		}
		
		return newResource;
    }
    
    
    /**
	 * Check that the AutomationRequest exactly one "source.*" input parameter.
	 * @param autoRequest			Automation Request with parameters to check
	 * @throws OslcResourceException 	When the executed AutomationRequest "source.*" input params are invalid or missing
	 */
    public static void checkSutDeploySourceInputs (AutomationRequest autoRequest) throws OslcResourceException
    {
		// count the "source.*" input params
		int count = 0;
    	for (ParameterInstance submittedParam : autoRequest.getInputParameter())
		{				
			if (submittedParam.getName().startsWith("source"))
			{
				count++;
			}
		}
    	
    	if (count == 1)
    		return;	// all fine
    	else if (count == 0)
    		throw new OslcResourceException("Source parameter missing. Expected exactly one.");
    	else // (count > 1)
    		throw new OslcResourceException("Too many source parameters. Expected exactly one.");
    }
    
    
    // End of user code

    public static void contextInitializeServletListener(final ServletContextEvent servletContextEvent)
    {
        
        // Start of user code contextInitializeServletListener
    	
    	// load configuration
    	try {
    		VeriFitCompilationProperties.loadProperties();
    	} catch (IOException e) {
			System.out.println("ERROR: Adapter configuration: Failed to load Java properties: " + e.getMessage());
			System.exit(1);
		}    	 
    	
    	// create the tmp directory
    	createTmpDir();

    	// connect to the triplestore
    	String sparqlQueryEndpoint = VeriFitCompilationProperties.SPARQL_SERVER_QUERY_ENDPOINT;
    	String sparqlUpdateEndpoint = VeriFitCompilationProperties.SPARQL_SERVER_UPDATE_ENDPOINT; 
		try {
			store = new Persistence(sparqlQueryEndpoint, sparqlUpdateEndpoint);
		} catch (IOException e) {
			System.out.println("ERROR: Adapter configuration: " + e.getMessage());
			System.exit(1);
		}

    	// create predefined AutomationPlans
		try {
			AutoPlanIdGen = new ResourceIdGen();
			if (!AutomationPlanDefinition.checkPredefinedAutomationPlans())
			{
				AutomationPlanDefinition.createPredefinedAutomationPlans();
			}
		} catch (StoreAccessException e) {
			System.out.println("ERROR: Adapter initialization: Predefined AutomationPlan creation: " + e.getMessage());
			System.exit(1);
		}
		
		// check what the last AutomationRequest ID is
    	// requests have a numerical ID
    	int initReqId = 0;
    	try {
			List<AutomationRequest> listAutoRequests =  store.getResources(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), AutomationRequest.class);
			for (AutomationRequest autoReq : listAutoRequests)
			{
				int reqId = Integer.parseInt(getResourceIdFromUri(autoReq.getAbout()));
				if ( reqId >= initReqId)
				{
					initReqId = reqId + 1;
				}
			}		
		} catch (StoreAccessException e) { 
			System.out.println("ERROR: Adapter initialization: Failed to get latest AutomationRequest ID: " + e.getMessage());
			System.exit(1);
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}		
		AutoRequestIdGen = new ResourceIdGen(initReqId);
        // End of user code
    }

    public static void contextDestroyServletListener(ServletContextEvent servletContextEvent) 
    {
        
        // Start of user code contextDestroyed
    	try {
			deleteTmpDir();
		} catch (IOException e) {
			System.out.println("ERROR: Adapter context destroy: Failed to get delete the TMP folder: " + e.getMessage());
			System.exit(1);
		}
        // End of user code
    }

    public static ServiceProviderInfo[] getServiceProviderInfos(HttpServletRequest httpServletRequest)
    {
        ServiceProviderInfo[] serviceProviderInfos = {};
        
        // Start of user code "ServiceProviderInfo[] getServiceProviderInfos(...)"

        ServiceProviderInfo r1 = new ServiceProviderInfo();
        r1.name = "Verifit Compilation Provider";
        r1.serviceProviderId = VeriFitCompilationConstants.AUTOMATION_PROVIDER_ID;

        serviceProviderInfos = new ServiceProviderInfo[1];
        serviceProviderInfos[0] = r1;

        // End of user code
        return serviceProviderInfos;
    }

    public static List<SUT> querySUTs(HttpServletRequest httpServletRequest, final String serviceProviderId, String where, int page, int limit)
    {
        List<SUT> resources = null;
        
        // Start of user code querySUTs
		resources = new ArrayList<SUT>();
        try {
        	
	    	resources = store.whereQuery(httpServletRequest, new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), SUT.class, where, page, limit);

		} catch (StoreAccessException e) {
			System.out.println("WARNING: AutomationRequest query failed: " + e.getMessage());
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
        // End of user code
        return resources;
    }
    public static List<AutomationRequest> queryAutomationRequests(HttpServletRequest httpServletRequest, final String serviceProviderId, String where, int page, int limit)
    {
        List<AutomationRequest> resources = null;
        
        // Start of user code queryAutomationRequests
		resources = new ArrayList<AutomationRequest>();
        try {
        	
	    	resources = store.whereQuery(httpServletRequest, new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), AutomationRequest.class, where, page, limit);

		} catch (StoreAccessException e) {
			System.out.println("WARNING: AutomationRequest query failed: " + e.getMessage());
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
        // End of user code
        return resources;
    }
    public static List<AutomationResult> queryAutomationResults(HttpServletRequest httpServletRequest, final String serviceProviderId, String where, int page, int limit)
    {
        List<AutomationResult> resources = null;
        
        // Start of user code queryAutomationResults
		resources = new ArrayList<AutomationResult>();
        try {
        	
        	resources = store.whereQuery(httpServletRequest, new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), AutomationResult.class, where, page, limit);
	    	
		} catch (StoreAccessException e) {
			System.out.println("WARNING: AutomationResult query failed: " + e.getMessage());
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
        // End of user code
        return resources;
    }
    public static List<AutomationPlan> queryAutomationPlans(HttpServletRequest httpServletRequest, final String serviceProviderId, String where, int page, int limit)
    {
        List<AutomationPlan> resources = null;
        
        // Start of user code queryAutomationPlans
		resources = new ArrayList<AutomationPlan>();
        try {

	    	resources = store.whereQuery(httpServletRequest, new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), AutomationPlan.class, where, page, limit);

		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (StoreAccessException e) {
			System.out.println("WARNING: AutomationPlan query failed: " + e.getMessage());
		}
        // End of user code
        return resources;
    }
    public static List<SUT> SUTSelector(HttpServletRequest httpServletRequest, final String serviceProviderId, String terms)   
    {
        List<SUT> resources = null;
        
        // Start of user code SUTSelector
        // TODO Implement code to return a set of resources, based on search criteria 
        // End of user code
        return resources;
    }
    public static List<AutomationRequest> AutomationRequestSelector(HttpServletRequest httpServletRequest, final String serviceProviderId, String terms)   
    {
        List<AutomationRequest> resources = null;
        
        // Start of user code AutomationRequestSelector
        
        resources = queryAutomationRequests(httpServletRequest, serviceProviderId, terms, 0, 20);       
        
        // End of user code
        return resources;
    }
    public static List<AutomationResult> AutomationResultSelector(HttpServletRequest httpServletRequest, final String serviceProviderId, String terms)   
    {
        List<AutomationResult> resources = null;
        
        // Start of user code AutomationResultSelector
        
        resources = queryAutomationResults(httpServletRequest, serviceProviderId, terms, 0, 20);
        
        // End of user code
        return resources;
    }
    public static List<AutomationPlan> AutomationPlanSelector(HttpServletRequest httpServletRequest, final String serviceProviderId, String terms)   
    {
        List<AutomationPlan> resources = null;
        
        // Start of user code AutomationPlanSelector
        
        resources = queryAutomationPlans(httpServletRequest, serviceProviderId, terms, 0, 20);
        
        // End of user code
        return resources;
    }
    public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String serviceProviderId) throws OslcResourceException
    {
        AutomationRequest newResource = null;
        
        // Start of user code createAutomationRequest

        /*
         * Check that the predefined AutomationPlans exist
         * If not then the triplestore is not running or is broken 
         */
        if (!AutomationPlanDefinition.checkPredefinedAutomationPlans())
		{
        	throw new OslcResourceException("Failed to get AutomationPlans. Is the triplestore still online?"
        			+ " If yes, then it may be corrupted. Try restarting the Adapter.");
		}
 
		try {
			// error response on empty creation POST
	        if (aResource == null)
				throw new OslcResourceException("empty creation POST");
	        
	        // check for missing required properties
			if (aResource.getExecutesAutomationPlan().getValue() == null)
				throw new OslcResourceException("executesAutomationPlan property missing");
			if (aResource.getTitle() == null || aResource.getTitle().isEmpty())
				throw new OslcResourceException("title property missing");
	        
			// copy the properties specified in the POST request
			String newID = AutoRequestIdGen.getId();
			newResource = aResource;
			newResource.setAbout(VeriFitCompilationResourcesFactory.constructURIForAutomationRequest(serviceProviderId, newID));
			
			// resources set by the service provider
			newResource.setIdentifier(newID);
			Date timestamp = new Date();
			newResource.setCreated(timestamp);
			newResource.setModified(timestamp);
			//newResource.setInstanceShape(new URI(AnacondaAdapterConstants.PATH_RESOURCE_SHAPES + "automationRequest"));
			//newResource.addServiceProvider(new URI(AnacondaAdapterConstants.PATH_AUTOMATION_SERVICE_PROVIDERS + serviceProviderId));
			newResource.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_NEW)));
			newResource.setDesiredState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_COMPLETE)));
			//newResource.addType(new URI("http://open-services.net/ns/auto#AutomationRequest"));

			
			// when a request is created - get its executed autoPlan, check parameters and make an input map
			// throws an exception if the autoRequest is not valid
			Map<String, String> inputParamsMap = getAutoReqInputParams(serviceProviderId, newResource);
			
			// check that the request contains exactly one "source.*" parameter (can not be checked automatically based on the AutoPlan
			// throws an exception if the Inputs are not OK
			checkSutDeploySourceInputs(newResource);
			
			// persist in the triplestore
			store.updateResources(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), newResource);
			
			// create a new thread to execute the automation request // TODO
			new SutDeployAutoPlanExecution(serviceProviderId, newResource.getAbout(), inputParamsMap);	

		} catch (OslcResourceException e) {
			throw new OslcResourceException("AutomationRequest NOT created - " + e.getMessage());
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (Exception e) {
			System.out.println("WARNING: AutomationResquest creation failed: " + e.getMessage());
			throw new OslcResourceException("AutomationRequest NOT created - " + e.getMessage());
		}
		
        // End of user code
        return newResource;
    }

    public static AutomationRequest createAutomationRequestFromDialog(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String serviceProviderId)
    {
        AutomationRequest newResource = null;
        
        // Start of user code createAutomationRequestFromDialog
        
        try {
			newResource = createAutomationRequest(httpServletRequest,aResource, serviceProviderId);
		} catch (OslcResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // End of user code
        return newResource;
    }

    public static AutomationPlan getAutomationPlan(HttpServletRequest httpServletRequest, final String serviceProviderId, final String automationPlanId)
    {
        AutomationPlan aResource = null;
        
        // Start of user code getAutomationPlan

        URI resUri = VeriFitCompilationResourcesFactory.constructURIForAutomationPlan(serviceProviderId, automationPlanId);
        try {

        	aResource = store.getResource(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), resUri, AutomationPlan.class);

        } catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (NoSuchElementException e) {
			aResource = null;
			
		} catch (StoreAccessException e) {
			System.out.println("WARNING: AutomationPlan GET failed: " + e.getMessage());
		}
        
        // End of user code
        return aResource;
    }


    public static SUT getSUT(HttpServletRequest httpServletRequest, final String serviceProviderId, final String sUTId)
    {
        SUT aResource = null;
        
        // Start of user code getSUT

        URI resUri = VeriFitCompilationResourcesFactory.constructURIForSUT(serviceProviderId, sUTId);
        try {
        	
			aResource = store.getResource(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), resUri, SUT.class);

        } catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (NoSuchElementException e) {
			aResource = null;
			
		} catch (StoreAccessException e) {
			System.out.println("WARNING: SUT GET failed: " + e.getMessage());
		}
        
        // End of user code
        return aResource;
    }


    public static AutomationRequest getAutomationRequest(HttpServletRequest httpServletRequest, final String serviceProviderId, final String automationRequestId)
    {
        AutomationRequest aResource = null;
        
        // Start of user code getAutomationRequest

        URI resUri = VeriFitCompilationResourcesFactory.constructURIForAutomationRequest(serviceProviderId, automationRequestId);
        try {
        	
			aResource = store.getResource(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), resUri, AutomationRequest.class);

        } catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (NoSuchElementException e) {
			aResource = null;
			
		} catch (StoreAccessException e) {
			System.out.println("WARNING: AutomationRequest GET failed: " + e.getMessage());
		}
        
        // End of user code
        return aResource;
    }


    public static AutomationResult getAutomationResult(HttpServletRequest httpServletRequest, final String serviceProviderId, final String automationResultId)
    {
        AutomationResult aResource = null;
        
        // Start of user code getAutomationResult

        URI resUri = VeriFitCompilationResourcesFactory.constructURIForAutomationResult(serviceProviderId, automationResultId);
        try {
        	
			aResource = store.getResource(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), resUri, AutomationResult.class);

        } catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (NoSuchElementException e) {
			aResource = null;
			
		} catch (StoreAccessException e) {
			System.out.println("WARNING: AutomationResult GET failed: " + e.getMessage());
		}
        
        // End of user code
        return aResource;
    }


    public static AutomationResult updateAutomationResult(HttpServletRequest httpServletRequest, final AutomationResult aResource, final String serviceProviderId, final String automationResultId) {
        AutomationResult updatedResource = null;
        // Start of user code updateAutomationResult
    	
        AutomationResult changedResource = aResource;
        aResource.setAbout(VeriFitCompilationResourcesFactory.constructURIForAutomationResult(serviceProviderId, automationResultId));
    	changedResource.setModified(new Date());
    	
    	try {
    		
			store.updateResources(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), changedResource);
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (Exception e) {
			System.out.println("WARNING: AutomationResult update failed: " + e.getMessage());
		}

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
    public static String getETagFromSUT(final SUT aResource)
    {
        String eTag = null;
        // Start of user code getETagFromSUT
        if (aResource != null && aResource.getCreated() != null)
        	eTag = Long.toString(aResource.getCreated().getTime());
        // End of user code
        return eTag;
    }

}
