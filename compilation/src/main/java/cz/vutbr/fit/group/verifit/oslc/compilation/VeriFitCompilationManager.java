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



// Start of user code imports
// End of user code

// Start of user code pre_class_code
// End of user code

public class VeriFitCompilationManager {

    private static final Logger log = LoggerFactory.getLogger(VeriFitCompilationManager.class);

    
    // Start of user code class_attributes
    // End of user code
    
    
    // Start of user code class_methods
    // End of user code

    public static void contextInitializeServletListener(final ServletContextEvent servletContextEvent)
    {
        
        // Start of user code contextInitializeServletListener
        // TODO Implement code to establish connection to data backbone etc ...
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
        // TODO Implement code to return the set of ServiceProviders
        // End of user code
        return serviceProviderInfos;
    }

    public static List<SUT> querySUTs(HttpServletRequest httpServletRequest, String where, String prefix, int page, int limit)
    {
        List<SUT> resources = null;
        
        
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
        
        
        // Start of user code AutomationRequestSelector
        // TODO Implement code to return a set of resources, based on search criteria 
        // An empty List should imply that no resources where found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return resources;
    }
    public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource)
    {
        AutomationRequest newResource = null;
        
        
        // Start of user code createAutomationRequest
        // TODO Implement code to create a resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return newResource;
    }

    public static AutomationRequest createAutomationRequestFromDialog(HttpServletRequest httpServletRequest, final AutomationRequest aResource)
    {
        AutomationRequest newResource = null;
        
        
        // Start of user code createAutomationRequestFromDialog
        // TODO Implement code to create a resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return newResource;
    }



    public static AutomationRequest getAutomationRequest(HttpServletRequest httpServletRequest, final String id)
    {
        AutomationRequest aResource = null;
        
        
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
        
        // Start of user code deleteAutomationRequest
        // TODO Implement code to delete a resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return deleted;
    }

    public static AutomationRequest updateAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String id) {
        AutomationRequest updatedResource = null;
        
        // Start of user code updateAutomationRequest
        // TODO Implement code to update and return a resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return updatedResource;
    }
    public static AutomationPlan getAutomationPlan(HttpServletRequest httpServletRequest, final String id)
    {
        AutomationPlan aResource = null;
        
        
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
        
        
        // Start of user code getAutomationResult
        // TODO Implement code to return a resource
        // return 'null' if the resource was not found.
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return aResource;
    }


    public static SUT getSUT(HttpServletRequest httpServletRequest, final String id)
    {
        SUT aResource = null;
        
        
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
        // TODO Implement code to return an ETag for a particular resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return eTag;
    }
    public static String getETagFromAutomationRequest(final AutomationRequest aResource)
    {
        String eTag = null;
        // Start of user code getETagFromAutomationRequest
        // TODO Implement code to return an ETag for a particular resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return eTag;
    }
    public static String getETagFromAutomationResult(final AutomationResult aResource)
    {
        String eTag = null;
        // Start of user code getETagFromAutomationResult
        // TODO Implement code to return an ETag for a particular resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return eTag;
    }
    public static String getETagFromSUT(final SUT aResource)
    {
        String eTag = null;
        // Start of user code getETagFromSUT
        // TODO Implement code to return an ETag for a particular resource
        // If you encounter problems, consider throwing the runtime exception WebApplicationException(message, cause, final httpStatus)
        // End of user code
        return eTag;
    }

}
