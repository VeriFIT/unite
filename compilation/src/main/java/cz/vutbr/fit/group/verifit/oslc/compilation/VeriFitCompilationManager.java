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

package cz.vutbr.fit.group.verifit.oslc.compilation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContextEvent;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.lyo.oslc4j.core.model.ServiceProvider;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import cz.vutbr.fit.group.verifit.oslc.compilation.servlet.ServiceProviderCatalogSingleton;
import cz.vutbr.fit.group.verifit.oslc.compilation.ServiceProviderInfo;
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
import cz.vutbr.fit.group.verifit.oslc.compilation.automationPlans.AutomationPlanDefinition;
import cz.vutbr.fit.group.verifit.oslc.compilation.automationPlans.SutDeployAutoPlanExecution;
import cz.vutbr.fit.group.verifit.oslc.compilation.exceptions.OslcResourceException;

import org.eclipse.lyo.store.StoreAccessException;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.apache.commons.io.FileDeleteStrategy;
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

    private static final Logger log = LoggerFactory.getLogger(VeriFitCompilationManager.class);

    private static StorePool storePool;
    
    // Start of user code class_attributes
	static ResourceIdGen AutoPlanIdGen;
	static ResourceIdGen AutoRequestIdGen;
	
    // End of user code
    
    
    // Start of user code class_methods

	/**
	 * Creates the SUT directory for saving programs to analyze
	 */
	private static void createTmpDir()
	{
		File programDir = new File("SUT/");
	    if (!programDir.exists())
	    {
	    	programDir.mkdirs();
	    }
	}
	
	/**
	 * Deletes the SUT directory to cleanup
	 * @throws IOException
	 */
	private static void deleteTmpDir() throws IOException
	{
		File programDir = new File("SUT");
		FileDeleteStrategy.FORCE.delete(programDir);
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
			newResource.setAbout(VeriFitCompilationResourcesFactory.constructURIForAutomationPlan(newID));
			
			// resources set by the service provider
			newResource.setIdentifier(newID);
			Date timestamp = new Date();
			newResource.setCreated(timestamp);
			newResource.setModified(timestamp);
			//newResource.setInstanceShape(new URI(VeriFitCompilationProperties.PATH_RESOURCE_SHAPES + "automationPlan"));
			//newResource.addServiceProvider(new URI(VeriFitCompilationProperties.PATH_AUTOMATION_SERVICE_PROVIDERS + serviceProviderId));
			//newResource.addType(new URI("http://open-services.net/ns/auto#AutomationPlan"));
			
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
			aResource.setAbout(VeriFitCompilationResourcesFactory.constructURIForAutomationResult(newID));
			
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
	 * Creates a Contribution resource with the specified properties.
	 * @param aResource			The new resource will copy properties from the specified aResource.
	 * @param newID				ID for the new resource
	 * @return					The newly created resource. Or null if one of the required properties was missing.
	 */
    public static Contribution createContribution(final Contribution aResource)
    {
    	Contribution newResource = null;

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
	 * Check that the AutomationRequest contains the necessary input parameters based on its AutoPlan, fill the AutomationResult with output parameters (default values),
	 * and return a simplified map of parameters (TODO refactor the map)
	 * @param execAutoRequest			The AutomationRequest to execute. Needs to have a valid executesAutomationPlan property.
	 * @param newAutoResult				The AutomationResult created by the AutoRequest.
	 * @throws OslcResourceException 	When the executed AutomationRequest properties are invalid or missing
	 * @return	A map of input parameters [ name, value ]
	 */
	private static Map<String, String> processAutoReqInputParams(AutomationRequest execAutoRequest, AutomationResult newAutoResult) throws OslcResourceException
	{
		// get the executed AutomationPlan resource			
		String execAutoPlanId = getResourceIdFromUri(execAutoRequest.getExecutesAutomationPlan().getValue());
		AutomationPlan execAutoPlan = null;
		try {
			execAutoPlan = getAutomationPlan(null, execAutoPlanId);
		} catch (Exception e) {
			throw new OslcResourceException("AutomationPlan not found (id: " + execAutoPlanId + ")");			
		}

		/// check the input parameters and create a map of "name" -> ("value", position)
		Map<String, String> inputParamsMap = new HashMap<String, String>();
		
		// loop through autoPlan defined parameters to match them with the input params
		for (ParameterDefinition definedParam : execAutoPlan.getParameterDefinition())
		{		
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
					
					inputParamsMap.put(definedParam.getName(), submittedParam.getValue());
					matched = true;
				}
			}
			// try to use the default value if no matching input param found
			if (!matched && definedParam.getDefaultValue() != null)
			{
				inputParamsMap.put(definedParam.getName(), definedParam.getDefaultValue());
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
			case VeriFitCompilationConstants.OSLC_OCCURS_ONE:
				// TODO check for more then one when there should be exactly one
			case VeriFitCompilationConstants.OSLC_OCCURS_ONEorMany:
				if (!matched)
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
	 * Creates an SUT resource with the specified properties, and stores in the Adapter's catalog.
     * @param aResource			The new resource will copy properties from the specified aResource.
     * @param newID				ID to assign to the new SUT (meant to be the same as the Request ID)
     * @return					The newly created resource. Or null if one of the required properties was missing.
     */
    public static SUT createSUT(final SUT aResource, final String newID) 
    {    	
    	SUT newResource = null;
    	
    	// check that required properties are specified in the input parameter
    	if (aResource == null || aResource.getTitle() == null || aResource.getTitle().isEmpty()) // TODO
    	{
    		return null;
    	}
        
		try {
			newResource = aResource;
			newResource.setAbout(VeriFitCompilationResourcesFactory.constructURIForSUT(newID));
			
			// resources set by the service provider
			Date timestamp = new Date();
			newResource.setCreated(timestamp);
			newResource.setModified(timestamp);
			newResource.setIdentifier(newID);
			//TODO
			
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

		} catch (WebApplicationException e) {
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
    	
    	// create the SUT directory
    	if (VeriFitCompilationProperties.PERSIST_SUT_DIRS == false) // make sure it was deleted if not persistent
    	{
    		try {
				deleteTmpDir();
			} catch (IOException e) {
				// ignore
			}
    	}
    	createTmpDir();

    	// connect to the triplestore
    	String sSparqlQueryEndpoint = VeriFitCompilationProperties.SPARQL_SERVER_QUERY_ENDPOINT;
    	String sSparqlUpdateEndpoint = VeriFitCompilationProperties.SPARQL_SERVER_UPDATE_ENDPOINT; 
    	
        // End of user code
        // Start of user code StoreInitialise
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
            defaultNamedGraph = new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES);
            sparqlQueryEndpoint = new URI(VeriFitCompilationProperties.SPARQL_SERVER_QUERY_ENDPOINT);
            sparqlUpdateEndpoint = new URI(VeriFitCompilationProperties.SPARQL_SERVER_UPDATE_ENDPOINT);
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
			store.namedGraphExists(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES));
			
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
			List<AutomationRequest> listAutoRequests = VeriFitCompilationManager.queryAutomationRequests(null, "", "", 0, 100); // TODO check that it works -- should get max ID
			for (AutomationRequest autoReq : listAutoRequests)
			{
				int reqId = Integer.parseInt(getResourceIdFromUri(autoReq.getAbout()));
				if ( reqId >= initReqId)
				{
					initReqId = reqId + 1;
				}
			}		
		} catch (WebApplicationException e) { 
			System.out.println("ERROR: Adapter initialization: Failed to get latest AutomationRequest ID: " + e.getMessage());
			System.exit(1);
		}		
		AutoRequestIdGen = new ResourceIdGen(initReqId);
        
        // End of user code
        
    }

    public static void contextDestroyServletListener(ServletContextEvent servletContextEvent) 
    {
        
        // Start of user code contextDestroyed
    	
    	// delete the TMP directory, if persistency is not enabled
    	if (VeriFitCompilationProperties.PERSIST_SUT_DIRS == false)
    	{	try {
				deleteTmpDir();
			} catch (IOException e) {
				System.out.println("WARNING: Adapter context destroy: Failed to delete the TMP folder: " + e.getMessage());
			}
    	}
        // End of user code
    }

    public static ServiceProviderInfo[] getServiceProviderInfos(HttpServletRequest httpServletRequest)
    {
        ServiceProviderInfo[] serviceProviderInfos = {};
        
        // Start of user code "ServiceProviderInfo[] getServiceProviderInfos(...)"

        ServiceProviderInfo r1 = new ServiceProviderInfo();
        r1.name = "VeriFit Compilation Provider";
        r1.serviceProviderId = VeriFitCompilationConstants.AUTOMATION_PROVIDER_ID;

        serviceProviderInfos = new ServiceProviderInfo[1];
        serviceProviderInfos[0] = r1;

        // End of user code
        return serviceProviderInfos;
    }

    public static List<SUT> querySUTs(HttpServletRequest httpServletRequest, String where, String prefix, int page, int limit)
    {
        List<SUT> resources = null;
        
        // Start of user code querySUTs_storeInit
        // End of user code
        Store store = storePool.getStore();
        try {
            resources = new ArrayList<SUT>(store.getResources(storePool.getDefaultNamedGraphUri(), SUT.class, prefix, where, "", limit+1, page*limit));
        } catch (StoreAccessException | ModelUnmarshallingException e) {
            log.error("Failed to query resources, with where-string '" + where + "'", e);
            throw new WebApplicationException("Failed to query resources, with where-string '" + where + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code querySUTs_storeFinalize
        // End of user code
        
        // Start of user code querySUTs
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
    public static List<AutomationPlan> queryAutomationPlans(HttpServletRequest httpServletRequest, String where, String prefix, int page, int limit)
    {
        List<AutomationPlan> resources = null;
        
        // Start of user code queryAutomationPlans_storeInit
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
    public static List<AutomationRequest> queryAutomationRequests(HttpServletRequest httpServletRequest, String where, String prefix, int page, int limit)
    {
        List<AutomationRequest> resources = null;
        
        // Start of user code queryAutomationRequests_storeInit
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
    public static List<SUT> SUTSelector(HttpServletRequest httpServletRequest, String terms)   
    {
        List<SUT> resources = null;
        
        // Start of user code SUTSelector_storeInit
        // End of user code
        Store store = storePool.getStore();
        try {
            resources = new ArrayList<SUT>(store.getResources(storePool.getDefaultNamedGraphUri(), SUT.class, "", "", terms, 20, -1));
        } catch (StoreAccessException | ModelUnmarshallingException e) {
            log.error("Failed to search resources, with search-term '" + terms + "'", e);
            throw new WebApplicationException("Failed to search resources, with search-term '" + terms + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code SUTSelector_storeFinalize
        // End of user code
        
        // Start of user code SUTSelector
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

        /*
         * Check that the predefined AutomationPlans exist
         * If not then the triplestore is not running or is broken 
         */
        if (!AutomationPlanDefinition.checkPredefinedAutomationPlans())
		{
        	throw new OslcResourceException("Failed to get AutomationPlans. Is the triplestore still online?"
        			+ " If yes, then it may be corrupted. Try restarting the Adapter.");
		}
        
        Map<String, String> inputParamsMap = null;
        AutomationResult newAutoResult = null;
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
			newResource.setAbout(VeriFitCompilationResourcesFactory.constructURIForAutomationRequest(newID));
			
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

			// create an AutomationResult for this AutoRequest; output parameters will be set by processAutoReqInputParams()
			AutomationResult propAutoResult = new AutomationResult();
			propAutoResult.setTitle("Result - " + newResource.getTitle());
			propAutoResult.setReportsOnAutomationPlan(newResource.getExecutesAutomationPlan());
			propAutoResult.setProducedByAutomationRequest(VeriFitCompilationResourcesFactory.constructLinkForAutomationRequest(newID));
			propAutoResult.setInputParameter(newResource.getInputParameter());
			propAutoResult.setContributor(newResource.getContributor());
			propAutoResult.setCreator(newResource.getCreator());
			propAutoResult.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_NEW)));
			propAutoResult.addVerdict(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_VERDICT_UNAVAILABLE)));
			
			// get the executed autoPlan, check input parameters, add output parameters to the AutoResult, and make an input map for the runner
			inputParamsMap = processAutoReqInputParams(newResource, propAutoResult);
			
			// check that the request contains exactly one "source.*" parameter (can not be checked automatically based on the AutoPlan
			// throws an exception if the Inputs are not OK
			checkSutDeploySourceInputs(newResource);
			
			// persist the AutomationResult and set the setProducedAutomationResult() link for the AutoRequest
		    newAutoResult = VeriFitCompilationManager.createAutomationResult(propAutoResult, newID);
			newResource.setProducedAutomationResult(new Link(newAutoResult.getAbout())); // TODO
			

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

		// create a new thread to execute the automation request
		new SutDeployAutoPlanExecution(newResource, newAutoResult, inputParamsMap);
		
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
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationRequest(id);
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
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationRequest(id);
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
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationRequest(id);
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
        // End of user code
        return updatedResource;
    }
    public static AutomationPlan getAutomationPlan(HttpServletRequest httpServletRequest, final String id)
    {
        AutomationPlan aResource = null;
        
        // Start of user code getAutomationPlan_storeInit
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationPlan(id);
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


    public static AutomationResult getAutomationResult(HttpServletRequest httpServletRequest, final String id)
    {
        AutomationResult aResource = null;
        
        // Start of user code getAutomationResult_storeInit
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationResult(id);
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
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationResult(id);
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
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationResult(id);
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
        // End of user code
        return updatedResource;
    }
    public static SUT getSUT(HttpServletRequest httpServletRequest, final String id)
    {
        SUT aResource = null;
        
        // Start of user code getSUT_storeInit
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitCompilationResourcesFactory.constructURIForSUT(id);
        try {
            aResource = store.getResource(storePool.getDefaultNamedGraphUri(), uri, SUT.class);
        } catch (NoSuchElementException e) {
            log.error("Resource: '" + uri + "' not found");
            throw new WebApplicationException("Failed to get resource: '" + uri + "'", e, Status.NOT_FOUND);
        } catch (StoreAccessException | ModelUnmarshallingException  e) {
            log.error("Failed to get resource: '" + uri + "'", e);
            throw new WebApplicationException("Failed to get resource: '" + uri + "'", e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            storePool.releaseStore(store);
        }
        // Start of user code getSUT_storeFinalize
        // End of user code
        
        // Start of user code getSUT
        // TODO Implement code to return a resource
        // return 'null' if the resource was not found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return aResource;
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
