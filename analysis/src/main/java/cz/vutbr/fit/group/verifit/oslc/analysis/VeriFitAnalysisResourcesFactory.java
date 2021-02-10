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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.OSLC4JUtils;
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

public class VeriFitAnalysisResourcesFactory {

    // Start of user code class_attributes
    // End of user code
    
    // Start of user code class_methods
    // End of user code

    //methods for AutomationPlan resource
    
    public static AutomationPlan createAutomationPlan(final String id)
    {
        return new AutomationPlan(constructURIForAutomationPlan(id));
    }
    
    public static URI constructURIForAutomationPlan(final String id)
    {
        String basePath = OSLC4JUtils.getServletURI();
        Map<String, Object> pathParameters = new HashMap<String, Object>();
        pathParameters.put("id", id);
        String instanceURI = "resources/automationPlans/{id}";
    
        final UriBuilder builder = UriBuilder.fromUri(basePath);
        return builder.path(instanceURI).buildFromMap(pathParameters);
    }
    
    public static Link constructLinkForAutomationPlan(final String id , final String label)
    {
        return new Link(constructURIForAutomationPlan(id), label);
    }
    
    public static Link constructLinkForAutomationPlan(final String id)
    {
        return new Link(constructURIForAutomationPlan(id));
    }
    

    //methods for AutomationRequest resource
    
    public static AutomationRequest createAutomationRequest(final String id)
    {
        return new AutomationRequest(constructURIForAutomationRequest(id));
    }
    
    public static URI constructURIForAutomationRequest(final String id)
    {
        String basePath = OSLC4JUtils.getServletURI();
        Map<String, Object> pathParameters = new HashMap<String, Object>();
        pathParameters.put("id", id);
        String instanceURI = "resources/automationRequests/{id}";
    
        final UriBuilder builder = UriBuilder.fromUri(basePath);
        return builder.path(instanceURI).buildFromMap(pathParameters);
    }
    
    public static Link constructLinkForAutomationRequest(final String id , final String label)
    {
        return new Link(constructURIForAutomationRequest(id), label);
    }
    
    public static Link constructLinkForAutomationRequest(final String id)
    {
        return new Link(constructURIForAutomationRequest(id));
    }
    

    //methods for AutomationResult resource
    
    public static AutomationResult createAutomationResult(final String id)
    {
        return new AutomationResult(constructURIForAutomationResult(id));
    }
    
    public static URI constructURIForAutomationResult(final String id)
    {
        String basePath = OSLC4JUtils.getServletURI();
        Map<String, Object> pathParameters = new HashMap<String, Object>();
        pathParameters.put("id", id);
        String instanceURI = "resources/automationResults/{id}";
    
        final UriBuilder builder = UriBuilder.fromUri(basePath);
        return builder.path(instanceURI).buildFromMap(pathParameters);
    }
    
    public static Link constructLinkForAutomationResult(final String id , final String label)
    {
        return new Link(constructURIForAutomationResult(id), label);
    }
    
    public static Link constructLinkForAutomationResult(final String id)
    {
        return new Link(constructURIForAutomationResult(id));
    }
    

    //methods for Contribution resource
    
    public static Contribution createContribution(final String id)
    {
        return new Contribution(constructURIForContribution(id));
    }
    
    public static URI constructURIForContribution(final String id)
    {
        String basePath = OSLC4JUtils.getServletURI();
        Map<String, Object> pathParameters = new HashMap<String, Object>();
        pathParameters.put("id", id);
        String instanceURI = "resources/contributions/{id}";
    
        final UriBuilder builder = UriBuilder.fromUri(basePath);
        return builder.path(instanceURI).buildFromMap(pathParameters);
    }
    
    public static Link constructLinkForContribution(final String id , final String label)
    {
        return new Link(constructURIForContribution(id), label);
    }
    
    public static Link constructLinkForContribution(final String id)
    {
        return new Link(constructURIForContribution(id));
    }
    

}
