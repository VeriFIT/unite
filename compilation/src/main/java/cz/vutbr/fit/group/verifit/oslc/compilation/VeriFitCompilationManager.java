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
import java.nio.file.FileSystems;
import java.util.NoSuchElementException;
import org.eclipse.lyo.store.ModelUnmarshallingException;
import org.eclipse.lyo.store.Store;
import org.eclipse.lyo.store.StorePool;
import org.eclipse.lyo.store.StoreAccessException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;




// Start of user code imports
import cz.vutbr.fit.group.verifit.oslc.compilation.automationPlans.AutomationPlanDefinition;
import cz.vutbr.fit.group.verifit.oslc.compilation.automationRequestExecution.SutDeploy;
import cz.vutbr.fit.group.verifit.oslc.compilation.properties.VeriFitCompilationProperties;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils.ResourceIdGen;
import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;
import cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution.ExecutionManager;
import cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution.ExecutionParameter;
import cz.vutbr.fit.group.verifit.oslc.shared.exceptions.OslcResourceException;

import org.eclipse.lyo.oslc4j.core.model.Link;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
// End of user code

// Start of user code pre_class_code
// End of user code

public class VeriFitCompilationManager {

    private static final Logger log = LoggerFactory.getLogger(VeriFitCompilationManager.class);

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
				List<AutomationRequest> listAutoRequests = VeriFitCompilationManager.queryAutomationRequests(null, "", "", page, 100);
				if (listAutoRequests.size() == 0)
				{
					break;	
				}
				else
				{	
					for (AutomationRequest autoReq : listAutoRequests)
					{
						long reqId = Long.parseLong(Utils.getResourceIdFromUri(autoReq.getAbout()));
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
		AutomationRequest bookmarkReq = VeriFitCompilationResourcesFactory.createAutomationRequest("bookmarkID");
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
	 * Creates the SUT directory for saving programs to analyze
	 */
	private static void createSutDir()
	{
		File programDir = FileSystems.getDefault().getPath(VeriFitCompilationProperties.SUT_FOLDER).toFile();
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
		File programDir = new File(VeriFitCompilationProperties.SUT_FOLDER);
		FileDeleteStrategy.FORCE.delete(programDir);
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
			newResource = VeriFitCompilationResourcesFactory.createAutomationPlan(newID);
			newResource.setParameterDefinition(aResource.getParameterDefinition());
			newResource.setUsesExecutionEnvironment(aResource.getUsesExecutionEnvironment());
			newResource.setTitle(aResource.getTitle());
			newResource.setDescription(aResource.getDescription());
			newResource.setCreator(aResource.getCreator());
			newResource.setContributor(aResource.getContributor());
			newResource.setExtendedProperties(aResource.getExtendedProperties());
			
			// persist in the triplestore
	        Store store = storePool.getStore();
	        try {
	            try {
	                store.updateResources(storePool.getDefaultNamedGraphUri(), newResource);
	            } catch (StoreAccessException e) {
	                log.error("Failed to create resource: '" + aResource.getAbout() + "'", e);            
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
    	AutomationResult newAutoResult = VeriFitCompilationResourcesFactory.createAutomationResult(id);
    	
		newAutoResult.setTitle("Result - " + autoRequest.getTitle());
		newAutoResult.setReportsOnAutomationPlan(autoRequest.getExecutesAutomationPlan());
		newAutoResult.setProducedByAutomationRequest(VeriFitCompilationResourcesFactory.constructLinkForAutomationRequest(id));
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
	 * Creates a Contribution resource with the specified properties.
	 * @param aResource			The new resource will copy properties from the specified aResource.
	 * @param newID				ID for the new resource
	 * @return					The newly created resource. Or null if one of the required properties was missing.
	 */
    /*
    public static Contribution createContribution(final Contribution aResource)
    {
    	Contribution newResource = null;

    	// check that required properties are specified in the input parameter
    	if (aResource == null)
    	{
    		return null;
    	}
    	newResource = new Contribution();
    	newResource.setFileURI(aResource.getFileURI());
    	newResource.setValueType(aResource.getValueType());
		newResource.setTitle(aResource.getTitle());
		newResource.setDescription(aResource.getDescription());
		newResource.setCreator(aResource.getCreator());
		newResource.setExtendedProperties(aResource.getExtendedProperties());
    	

		return newResource;
    }
	*/
    
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
			newResource = VeriFitCompilationResourcesFactory.createSUT(newID);
	    	newResource.setTitle(aResource.getTitle());
	    	newResource.setLaunchCommand(aResource.getLaunchCommand());
			newResource.setBuildCommand(aResource.getBuildCommand());
			newResource.setSUTdirectoryPath(aResource.getSUTdirectoryPath());
			newResource.setCreator(aResource.getCreator());
			newResource.setProducedByAutomationRequest(aResource.getProducedByAutomationRequest());
			newResource.setCompiled(aResource.isCompiled());
	    	
			// persist in the triplestore
	        Store store = storePool.getStore();
	        try {
	            URI uri = newResource.getAbout();
	            
	            if (store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
	                log.error("Cannot create a resource that already exists: '" + uri + "'");
	                throw new WebApplicationException("Cannot create a resource that already exists: '" + uri + "'", Status.SEE_OTHER);
	            }
	            try {
	                store.appendResource(storePool.getDefaultNamedGraphUri(), newResource);
	            } catch (StoreAccessException e) {
	                log.error("Failed to create resource: '" + newResource.getAbout() + "'", e);            
	                throw new WebApplicationException("Failed to create resource: '" + newResource.getAbout() + "'", e, Status.INTERNAL_SERVER_ERROR);
	            }
	        } finally {
	            storePool.releaseStore(store);
	        }

		} catch (WebApplicationException e) {
			log.error("SUT creation failed: " + e.getMessage());
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
    
    /**
	 * Check that the build command is defined if the compilation parameter is set to true
	 * @param autoRequest			Automation Request with parameters to check
	 * @throws OslcResourceException 	When the compilation parameter is true and the build command is null
	 */
    public static void checkSutDeployCompilationAndBuildParams (AutomationRequest autoRequest) throws OslcResourceException
    {
		// count the "source.*" input params
		Boolean compileSet = true;
		Boolean buildCmdFound = false;
    	for (ParameterInstance submittedParam : autoRequest.getInputParameter())
		{				
			if (submittedParam.getName().equals("compile"))
			{
				compileSet = Boolean.valueOf(submittedParam.getValue());
			}
			else if (submittedParam.getName().equals("buildCommand"))
			{
				buildCmdFound = true;
			}
		}
    	
    	if (compileSet == true && buildCmdFound == false)
    		throw new OslcResourceException("compilation is enabled, but the buildCommand is missing.");
    	else
    		return;
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
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationRequest(id);
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
  
        Store store = storePool.getStore();
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationResult(id);
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
    
    // End of user code

    public static void contextInitializeServletListener(final ServletContextEvent servletContextEvent)
    {
        
        // Start of user code contextInitializeServletListener

    	// load configuration
    	try {
    		VeriFitCompilationProperties.initializeProperties();
    	} catch (FileNotFoundException e) {
    		log.error("Adapter configuration: Failed to load Java properties: " + e.getMessage()
					+ "\t The adapter needs to be configured to be able to run!\n"
					+ "\tSee the \"VeriFitCompilationExample.properties\" file for instructions and use it as a template.");
			System.exit(1);
  
    	} catch (IOException e) {
			log.error("Adapter configuration: Failed to load Java properties: " + e.getMessage());
			System.exit(1);
		}
    	log.info("Loaded configuration:\n"
    			+ "  ADAPTER_HOST: " + VeriFitCompilationProperties.ADAPTER_HOST + "\n"
    			+ "  ADAPTER_PORT: " + VeriFitCompilationProperties.ADAPTER_PORT + "\n"
    			+ "  SERVER_URL: " + VeriFitCompilationProperties.SERVER_URL + "\n"
    			+ "  SPARQL_SERVER_NAMED_GRAPH_RESOURCES: " + VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES + "\n"
    			+ "  SPARQL_SERVER_QUERY_ENDPOINT: " + VeriFitCompilationProperties.SPARQL_SERVER_QUERY_ENDPOINT + "\n"
    			+ "  SPARQL_SERVER_UPDATE_ENDPOINT: " + VeriFitCompilationProperties.SPARQL_SERVER_UPDATE_ENDPOINT + "\n"
    			+ "  AUTHENTICATION_ENABLED: " + VeriFitCompilationProperties.AUTHENTICATION_ENABLED + "\n"
    			+ "  AUTHENTICATION_USERNAME: " + VeriFitCompilationProperties.AUTHENTICATION_USERNAME + "\n"
    			+ "  AUTHENTICATION_PASSWORD: " + "********" + "\n"
    			+ "  KEEP_LAST_N_ENABLED: " + VeriFitCompilationProperties.KEEP_LAST_N_ENABLED + "\n"
    			+ "  KEEP_LAST_N: " + VeriFitCompilationProperties.KEEP_LAST_N + "\n"
    			+ "  PERSIST_SUT_DIRS: " + VeriFitCompilationProperties.PERSIST_SUT_DIRS);
    	
    	
    	// create the SUT directory
    	if (VeriFitCompilationProperties.PERSIST_SUT_DIRS == false) // make sure it was deleted if not persistent
    	{
    		try {
    			log.info("Initialization: Persistency not enabled in conf. Deleting and recreating the SUT directory");
				deleteTmpDir();
			} catch (IOException e) {
				// ignore
			}
    	}
    	createSutDir();

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
        
    	// create predefined AutomationPlans
        AutoPlanIdGen = new ResourceIdGen();
		try {
			if (!AutomationPlanDefinition.checkPredefinedAutomationPlans())
			{
				AutomationPlanDefinition.createPredefinedAutomationPlans();
			}
		} catch (StoreAccessException e) {
			log.error("Adapter initialization: Predefined AutomationPlan creation: " + e.getMessage());
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
		
        // End of user code
        
    }

    public static void contextDestroyServletListener(ServletContextEvent servletContextEvent) 
    {
        
        // Start of user code contextDestroyed
		log.info("Shutting down");
    	
    	// delete the TMP directory, if persistency is not enabled
    	if (VeriFitCompilationProperties.PERSIST_SUT_DIRS == false)
    	{	try {
				log.info("Shutting down: Persistency not enabled in conf. Deleting the SUT directory");
				deleteTmpDir();
			} catch (IOException e) {
				log.error("Adapter context destroy: Failed to delete the TMP folder: " + e.getMessage());
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
        r1.serviceProviderId = VeriFitCompilationProperties.AUTOMATION_PROVIDER_ID;

        serviceProviderInfos = new ServiceProviderInfo[1];
        serviceProviderInfos[0] = r1;

        // End of user code
        return serviceProviderInfos;
    }

    public static List<SUT> querySUTs(HttpServletRequest httpServletRequest, String where, String prefix, int page, int limit)
    {
        List<SUT> resources = null;
        
        // Start of user code querySUTs_storeInit
        if (httpServletRequest != null)
        {
		    String strLimit = httpServletRequest.getParameter("limit");
		    if (strLimit != null)
		    	limit = Integer.parseInt(strLimit) - 1; // TODO -1 to counteract the +1 generated in the store.getResources(...) below
        }
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
        // End of user code
        return resources;
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
            if (Utils.getResourceIdFromUri(req.getAbout()).equals("bookmarkID"))
            {
            	resources.remove(req);
            	break;
            }
        }
        
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

        /*
         * Check that the predefined AutomationPlans exist
         * If not then the triplestore is not running or is broken 
         */
        if (!AutomationPlanDefinition.checkPredefinedAutomationPlans())
		{
        	throw new OslcResourceException("Failed to get AutomationPlans. Is the triplestore still online?"
        			+ " If yes, then it may be corrupted. Try restarting the Adapter.");
		}
        
        SutDeploy runner = null;
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
			newResource = VeriFitCompilationResourcesFactory.createAutomationRequest(newID);
			newResource.setInputParameter(aResource.getInputParameter());
			newResource.setTitle(aResource.getTitle());
			newResource.setDescription(aResource.getDescription());
			newResource.setExecutesAutomationPlan(aResource.getExecutesAutomationPlan());
			newResource.setCreator(aResource.getCreator());
			newResource.setContributor(aResource.getContributor());
			newResource.setExtendedProperties(aResource.getExtendedProperties());
			newResource.replaceState(OslcValues.AUTOMATION_STATE_INPROGRESS);	// set to inProgress becuase there is no queuing currently (all requests start immediately)
			//newResource.replaceDesiredState(aResource.getDesiredState());	// TODO use this to implement deferred execution later

			// get the executed autoPlan
			String execAutoPlanId = Utils.getResourceIdFromUri(newResource.getExecutesAutomationPlan().getValue());
			AutomationPlan execAutoPlan = null;
			try {
				execAutoPlan = getAutomationPlan(null, execAutoPlanId);
			} catch (Exception e) {
				throw new OslcResourceException("AutomationPlan not found (id: " + execAutoPlanId + ")");			
			}

			// check input parameters, and create output parameters
			Set<ParameterInstance> outputParams = Utils.checkInputParamsAndProduceOuputParams(newResource.getInputParameter(), execAutoPlan.getParameterDefinition());

			// check that the request contains exactly one "source.*" parameter (can not be checked automatically based on the AutoPlan
			// throws an exception if the Inputs are not OK
			checkSutDeploySourceInputs(newResource);
			
			// check that buildCommand is not null, if compilation was set to true
			checkSutDeployCompilationAndBuildParams(newResource);
			
			// create an AutomationResult for this AutoRequest
			AutomationResult newAutoResult = createAutomationResultForAutomationRequest(newResource, outputParams);
			newResource.setProducedAutomationResult(new Link(newAutoResult.getAbout()));

			// create Execution Parameters for the runner
			List<ExecutionParameter> execParams = ExecutionParameter.createExecutionParameters(newAutoResult.getInputParameter(), newAutoResult.getOutputParameter(), execAutoPlan.getParameterDefinition());
			
			// create a new thread to execute the automation request
			runner = new SutDeploy(newResource, newAutoResult, execParams);

		} catch (OslcResourceException e) {
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

        // start the execution 
        AutoRequestExecManager.addRequest(runner);
        
        // End of user code
        
        // Start of user code createAutomationRequest
        
        if (VeriFitCompilationProperties.KEEP_LAST_N_ENABLED)
        {
        	try {
	        	// only keep last N automation requests
	        	// delete the one that is last N+1 when creating a new one 
	        	long currentID = Integer.parseInt(Utils.getResourceIdFromUri(newResource.getAbout()));
	        	if (currentID > VeriFitCompilationProperties.KEEP_LAST_N)
	        	{
		        	String toDeleteID = Long.toString(currentID - VeriFitCompilationProperties.KEEP_LAST_N);
	            	log.info("KEEP_LAST_N is enabled with value of " + VeriFitCompilationProperties.KEEP_LAST_N
	            			+ ". Deleting AutomationRequest \"" + toDeleteID +"\" and all its associated resources.");
		        	
		        	deleteAutomationRequest(null, toDeleteID);
		        	deleteAutomationResult(null, toDeleteID);
		        	deleteSUT(null, toDeleteID);
	        	}
        	} catch (Exception e) {
        		log.error("Failed to delete old AutomationRequest, ResultM or SUT with KEEP_LAST_N enabled", e);
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
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationRequest(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("AutomationRequest delete: Cannot delete a resource that already does not exists: '" + uri + "'");
            throw new WebApplicationException("AutomationRequest delete: Cannot delete a resource that already does not exists: '" + uri + "'", Status.NOT_FOUND);
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
	        		
	        		// only delete the SUT if the request finished (otherwise it does not exist yet)
	        		if (requestToDelete.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_COMPLETE) || requestToDelete.getState().iterator().next().equals(OslcValues.AUTOMATION_STATE_CANCELED)) {
	        			deleteSUT(null, id);
		        	}
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
	    			String resultId = Utils.getResourceIdFromUri(updatedResource.getProducedAutomationResult().getValue());
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
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationRequest(id);
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
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationResult(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("Automation Result DELETE: Cannot delete a resource that already does not exists: '" + uri + "'");
            throw new WebApplicationException("Automation Result DELETE: Cannot delete a resource that already does not exists: '" + uri + "'", Status.NOT_FOUND);
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
	        		
	        		// delete the SUT only if the was already created
	        		if (resultToDelete.getCreatedSUT() != null)
	        			deleteSUT(null, id);				// same ^^^
	        	} catch (Exception e) {
	        		log.warn("AutomationResult delete id \"" + id + "\": Failed to cascade - " + e.getMessage());
	        	}
	        }
        }
        
        // delete contribution files
        if (resultToDelete != null) {
        	String sutPath = null;
	        for (Contribution contrib : resultToDelete.getContribution())
	        {
	        	// delete the actual file
            	String filePath = contrib.getFilePath();
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
            	
	        	// get the sut directory to be able to delete *sut*/.adapter/exec_compilation_N.sh or .ps
	        	if (contrib.getFilePath() != null && contrib.getFilePath().contains(".adapter"))
	        		sutPath = contrib.getFilePath();
	        }
	        
	        // delete *sut*/.adapter/exec_analysis_N.sh or .ps
	        if (sutPath != null)
	        {
		        try {
		        	String fileEnding = SystemUtils.IS_OS_LINUX ? ".sh" : ".ps1";
		    		File execFile = FileSystems.getDefault().getPath(sutPath).getParent().resolve("exec_compilation_" + id + fileEnding).toFile();
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
        
        // get the current version of the Automation Request 
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
        
        // for the generated code below
        aResource = updatedResource;
  
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitCompilationResourcesFactory.constructURIForAutomationResult(id);
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
        // End of user code
        return aResource;
    }

    public static Boolean deleteSUT(HttpServletRequest httpServletRequest, final String id)
    {
        Boolean deleted = false;
        // Start of user code deleteSUT_storeInit
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitCompilationResourcesFactory.constructURIForSUT(id);
        if (!store.resourceExists(storePool.getDefaultNamedGraphUri(), uri)) {
            log.error("SUT DELETE: Cannot delete a resource that already does not exists: '" + uri + "'");
            throw new WebApplicationException("SUT DELETE: Cannot delete a resource that already does not exists: '" + uri + "'", Status.NOT_FOUND);
        }
        store.deleteResources(storePool.getDefaultNamedGraphUri(), uri);
        storePool.releaseStore(store);
        deleted = true;
        // Start of user code deleteSUT_storeFinalize
        // End of user code
        
        // Start of user code deleteSUT
        if (httpServletRequest != null) {
	        String cascade = httpServletRequest.getParameter("cascade");
	        if (cascade != null && cascade.equalsIgnoreCase("true"))
	        {
	        	try {
	        		deleteAutomationResult(null, id);	// TODO relies on result and request IDs being the same
	        		deleteAutomationRequest(null, id);	// same ^^^
	        	} catch (Exception e) {
	        		log.warn("AutomationResult delete id \"" + id + "\": Failed to cascade - " + e.getMessage());
	        	}
	        }
        }
        try {
    		File sutDir = FileSystems.getDefault().getPath(VeriFitCompilationProperties.SUT_FOLDER).resolve(id).toFile();	// TODO relies on the SUT dir being named as the sut ID
    		FileDeleteStrategy.FORCE.delete(sutDir);
		} catch (IOException e) {
			log.error("SUT delete: Failed to delete SUT directory: " + e.getMessage());
		}
        // End of user code
        return deleted;
    }

    public static SUT updateSUT(HttpServletRequest httpServletRequest, SUT aResource, final String id) {
        SUT updatedResource = null;
        // Start of user code updateSUT_storeInit

        if (aResource == null)
        {
            log.error("SUT UPDATE: received an empty request");
            throw new WebApplicationException("SUT UPDATE: received an empty request", Status.BAD_REQUEST);
        }
		if (aResource.getTitle() == null || aResource.getTitle().isEmpty())
			throw new WebApplicationException("SUT UPDATE: title property missing", Status.BAD_REQUEST);
        
        // get the current version of the Automation Request 
        updatedResource = getSUT(null, id);
        
        // only update the properties that we allow to update
        updatedResource.setExtendedProperties(aResource.getExtendedProperties());
        updatedResource.setCreator(aResource.getCreator());
        updatedResource.setDescription(aResource.getDescription());
        updatedResource.setLaunchCommand(aResource.getLaunchCommand());
        updatedResource.setTitle(aResource.getTitle());
        
        // TODO future functionality to rebuild SUT after build command change
        updatedResource.setBuildCommand(aResource.getBuildCommand());
        
        // TODO future functionality to build an SUT which was initially set not to build
        //updatedResource.setCompiled(aResource.isCompiled());

        // for the generated code below
        aResource = updatedResource;
        
        // End of user code
        Store store = storePool.getStore();
        URI uri = VeriFitCompilationResourcesFactory.constructURIForSUT(id);
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
        // Start of user code updateSUT_storeFinalize
        // End of user code
        
        // Start of user code updateSUT
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
