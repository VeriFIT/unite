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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.lyo.oslc4j.core.model.ServiceProvider;
import org.eclipse.lyo.store.StoreAccessException;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.Link;

import verifit.compilation.servlet.ServiceProviderCatalogSingleton;
import verifit.compilation.ServiceProviderInfo;
import verifit.compilation.automationPlans.AutomationPlanDefinition;
import verifit.compilation.persistance.Persistence;

import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc.domains.auto.ParameterInstance;
import org.apache.commons.io.FileUtils;
import org.eclipse.lyo.oslc.domains.Person;
import verifit.compilation.resources.SUT;
import verifit.compilation.resources.TextOut;


// Start of user code imports
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
	 * @param serviceProviderId	ID of the service provider for the new resource.
	 * @return					The newly created resource. Or null if one of the required properties was missing.
	 * @throws StoreAccessException 
	 */
    public static AutomationPlan createAutomationPlan(final AutomationPlan aResource, final String serviceProviderId) throws StoreAccessException
    {    	
    	AutomationPlan newResource = null;
    	
    	// check that required properties are specified in the input parameter
    	if (aResource == null || aResource.getTitle() == null || aResource.getTitle().isEmpty())
    	{
    		return null;
    	}
        
		try {
			String newID = AutoPlanIdGen.getId();
			newResource = VeriFitCompilationResourcesFactory.createAutomationPlan(serviceProviderId, newID);
			
			// resources set by the service provider
			newResource.setIdentifier(newID);
			Date timestamp = new Date();
			newResource.setCreated(timestamp);
			newResource.setModified(timestamp);
			newResource.setInstanceShape(new URI(VeriFitCompilationProperties.PATH_RESOURCE_SHAPES + "automationPlan"));
			newResource.addServiceProvider(new URI(VeriFitCompilationProperties.PATH_AUTOMATION_SERVICE_PROVIDERS + serviceProviderId));
			newResource.addType(new URI("http://open-services.net/ns/auto#AutomationPlan"));
			
			// resources set by the user
			newResource.setDescription(aResource.getDescription());
			newResource.setSubject(aResource.getSubject());
			newResource.setTitle(aResource.getTitle());
			
			newResource.setContributor(aResource.getContributor());
			newResource.setCreator(aResource.getCreator());
			
			// TODO unknown resources 
			newResource.setUsesExecutionEnvironment(aResource.getUsesExecutionEnvironment());
			newResource.setFutureAction(aResource.getFutureAction());
			
			// persist in the triplestore
			//store.updateResources(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), newResource);

		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		}/* catch (StoreAccessException e) {
			throw new StoreAccessException("AutomationPlan creation failed: " + e.getMessage());
		}*/
		
		return newResource;
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
			newResource = VeriFitCompilationResourcesFactory.createAutomationResult(serviceProviderId, newID);
			
			// resources set by the service provider
			newResource.setIdentifier(newID);
			Date timestamp = new Date();
			newResource.setCreated(timestamp);
			newResource.setModified(timestamp);
			newResource.setInstanceShape(new URI(VeriFitCompilationProperties.PATH_RESOURCE_SHAPES + "automationResult"));
			newResource.addServiceProvider(new URI(VeriFitCompilationProperties.PATH_AUTOMATION_SERVICE_PROVIDERS + serviceProviderId));
			newResource.setDesiredState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_COMPLETE)));
			newResource.addType(new URI("http://open-services.net/ns/auto#AutomationResult"));
			
			// resources set by the user
			newResource.setTitle(aResource.getTitle());
			newResource.setSubject(aResource.getSubject());
			newResource.setState(aResource.getState());
			newResource.setVerdict(aResource.getVerdict());
			newResource.setContribution(aResource.getContribution());
			newResource.setInputParameter(aResource.getInputParameter());
			newResource.setProducedByAutomationRequest(aResource.getProducedByAutomationRequest());
			newResource.setReportsOnAutomationPlan(aResource.getReportsOnAutomationPlan());
			
			newResource.setContributor(aResource.getContributor());
			newResource.setCreator(aResource.getCreator());
			
			// TODO unknown resources
			newResource.setOutputParameter(aResource.getOutputParameter());

			// persist in the triplestore
			//store.updateResources(new URI(VeriFitCompilationProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES), newResource);
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} /*catch (StoreAccessException e) {
			System.out.println("WARNING: AutomationResult creation failed: " + e.getMessage());
		}*/
		
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
    	
		try {
			newResource = new TextOut();
			newResource.setCreated(new Date());
			
			// resources set by the user
			newResource.setDescription(aResource.getDescription());
			newResource.setValue(aResource.getValue());
			newResource.setTitle(aResource.getTitle());
			newResource.setType(aResource.getType());
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
		
		return newResource;
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

    	/*
    	// connect to the triplestore
    	String sparqlQueryEndpoint = AnacondaAdapterConstants.SPARQL_SERVER_QUERY_ENDPOINT;
    	String sparqlUpdateEndpoint = AnacondaAdapterConstants.SPARQL_SERVER_UPDATE_ENDPOINT; 
		try {
			store = new Persistence(sparqlQueryEndpoint, sparqlUpdateEndpoint);
		} catch (IOException e) {
			System.out.println("ERROR: Adapter configuration: " + e.getMessage());
			System.exit(1);
		}
		*/

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
        // TODO Implement code to return a set of resources
        // End of user code
        return resources;
    }
    public static List<AutomationRequest> queryAutomationRequests(HttpServletRequest httpServletRequest, final String serviceProviderId, String where, int page, int limit)
    {
        List<AutomationRequest> resources = null;
        
        // Start of user code queryAutomationRequests
        // TODO Implement code to return a set of resources
        // End of user code
        return resources;
    }
    public static List<AutomationResult> queryAutomationResults(HttpServletRequest httpServletRequest, final String serviceProviderId, String where, int page, int limit)
    {
        List<AutomationResult> resources = null;
        
        // Start of user code queryAutomationResults
        // TODO Implement code to return a set of resources
        // End of user code
        return resources;
    }
    public static List<AutomationPlan> queryAutomationPlans(HttpServletRequest httpServletRequest, final String serviceProviderId, String where, int page, int limit)
    {
        List<AutomationPlan> resources = null;
        
        // Start of user code queryAutomationPlans
        // TODO Implement code to return a set of resources
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
        // TODO Implement code to return a set of resources, based on search criteria 
        // End of user code
        return resources;
    }
    public static List<AutomationResult> AutomationResultSelector(HttpServletRequest httpServletRequest, final String serviceProviderId, String terms)   
    {
        List<AutomationResult> resources = null;
        
        // Start of user code AutomationResultSelector
        // TODO Implement code to return a set of resources, based on search criteria 
        // End of user code
        return resources;
    }
    public static List<AutomationPlan> AutomationPlanSelector(HttpServletRequest httpServletRequest, final String serviceProviderId, String terms)   
    {
        List<AutomationPlan> resources = null;
        
        // Start of user code AutomationPlanSelector
        // TODO Implement code to return a set of resources, based on search criteria 
        // End of user code
        return resources;
    }
    public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String serviceProviderId)
    {
        AutomationRequest newResource = null;
        
        // Start of user code createAutomationRequest
        
        newResource = aResource;
        
        // End of user code
        return newResource;
    }

    public static AutomationRequest createAutomationRequestFromDialog(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String serviceProviderId)
    {
        AutomationRequest newResource = null;
        
        // Start of user code createAutomationRequestFromDialog
        
        newResource = createAutomationRequest(httpServletRequest,aResource, serviceProviderId);
        
        // End of user code
        return newResource;
    }

    public static AutomationPlan getAutomationPlan(HttpServletRequest httpServletRequest, final String serviceProviderId, final String automationPlanId)
    {
        AutomationPlan aResource = null;
        
        // Start of user code getAutomationPlan
        // TODO Implement code to return a resource
        // End of user code
        return aResource;
    }


    public static SUT getSUT(HttpServletRequest httpServletRequest, final String serviceProviderId, final String sUTId)
    {
        SUT aResource = null;
        
        // Start of user code getSUT
        // TODO Implement code to return a resource
        // End of user code
        return aResource;
    }


    public static AutomationRequest getAutomationRequest(HttpServletRequest httpServletRequest, final String serviceProviderId, final String automationRequestId)
    {
        AutomationRequest aResource = null;
        
        // Start of user code getAutomationRequest
        // TODO Implement code to return a resource
        // End of user code
        return aResource;
    }


    public static AutomationResult getAutomationResult(HttpServletRequest httpServletRequest, final String serviceProviderId, final String automationResultId)
    {
        AutomationResult aResource = null;
        
        // Start of user code getAutomationResult
        // TODO Implement code to return a resource
        // End of user code
        return aResource;
    }




    public static String getETagFromAutomationPlan(final AutomationPlan aResource)
    {
        String eTag = null;
        // Start of user code getETagFromAutomationPlan
        // TODO Implement code to return an ETag for a particular resource
        // End of user code
        return eTag;
    }
    public static String getETagFromAutomationRequest(final AutomationRequest aResource)
    {
        String eTag = null;
        // Start of user code getETagFromAutomationRequest
        // TODO Implement code to return an ETag for a particular resource
        // End of user code
        return eTag;
    }
    public static String getETagFromAutomationResult(final AutomationResult aResource)
    {
        String eTag = null;
        // Start of user code getETagFromAutomationResult
        // TODO Implement code to return an ETag for a particular resource
        // End of user code
        return eTag;
    }
    public static String getETagFromSUT(final SUT aResource)
    {
        String eTag = null;
        // Start of user code getETagFromSUT
        // TODO Implement code to return an ETag for a particular resource
        // End of user code
        return eTag;
    }

}
