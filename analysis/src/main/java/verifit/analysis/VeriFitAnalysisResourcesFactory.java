// Start of user code Copyright
/*******************************************************************************
 * Copyright (c) 2017 Jad El-khoury.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *
 *     Jad El-khoury        - initial implementation
 *     
 *******************************************************************************/
// End of user code

package verifit.analysis;

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
import verifit.analysis.resources.SUT;

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
    public static AutomationPlan createAutomationPlan(final String serviceProviderId, final String automationPlanId)
           throws URISyntaxException
    {
        return new AutomationPlan(constructURIForAutomationPlan(serviceProviderId, automationPlanId));
    }
    
    public static URI constructURIForAutomationPlan(final String serviceProviderId, final String automationPlanId)
    {
        String basePath = OSLC4JUtils.getServletURI();
        Map<String, Object> pathParameters = new HashMap<String, Object>();
        pathParameters.put("serviceProviderId", serviceProviderId);
        pathParameters.put("automationPlanId", automationPlanId);
        String instanceURI = "serviceProviders/{serviceProviderId}/resources/automationPlans/{automationPlanId}";
    
        final UriBuilder builder = UriBuilder.fromUri(basePath);
        return builder.path(instanceURI).buildFromMap(pathParameters);
    }
    
    public static Link constructLinkForAutomationPlan(final String serviceProviderId, final String automationPlanId , final String label)
    {
        return new Link(constructURIForAutomationPlan(serviceProviderId, automationPlanId), label);
    }
    
    public static Link constructLinkForAutomationPlan(final String serviceProviderId, final String automationPlanId)
    {
        return new Link(constructURIForAutomationPlan(serviceProviderId, automationPlanId));
    }
    

    //methods for AutomationRequest resource
    public static AutomationRequest createAutomationRequest(final String serviceProviderId, final String automationRequestId)
           throws URISyntaxException
    {
        return new AutomationRequest(constructURIForAutomationRequest(serviceProviderId, automationRequestId));
    }
    
    public static URI constructURIForAutomationRequest(final String serviceProviderId, final String automationRequestId)
    {
        String basePath = OSLC4JUtils.getServletURI();
        Map<String, Object> pathParameters = new HashMap<String, Object>();
        pathParameters.put("serviceProviderId", serviceProviderId);
        pathParameters.put("automationRequestId", automationRequestId);
        String instanceURI = "serviceProviders/{serviceProviderId}/resources/automationRequests/{automationRequestId}";
    
        final UriBuilder builder = UriBuilder.fromUri(basePath);
        return builder.path(instanceURI).buildFromMap(pathParameters);
    }
    
    public static Link constructLinkForAutomationRequest(final String serviceProviderId, final String automationRequestId , final String label)
    {
        return new Link(constructURIForAutomationRequest(serviceProviderId, automationRequestId), label);
    }
    
    public static Link constructLinkForAutomationRequest(final String serviceProviderId, final String automationRequestId)
    {
        return new Link(constructURIForAutomationRequest(serviceProviderId, automationRequestId));
    }
    

    //methods for AutomationResult resource
    public static AutomationResult createAutomationResult(final String serviceProviderId, final String automationResultId)
           throws URISyntaxException
    {
        return new AutomationResult(constructURIForAutomationResult(serviceProviderId, automationResultId));
    }
    
    public static URI constructURIForAutomationResult(final String serviceProviderId, final String automationResultId)
    {
        String basePath = OSLC4JUtils.getServletURI();
        Map<String, Object> pathParameters = new HashMap<String, Object>();
        pathParameters.put("serviceProviderId", serviceProviderId);
        pathParameters.put("automationResultId", automationResultId);
        String instanceURI = "serviceProviders/{serviceProviderId}/resources/automationResults/{automationResultId}";
    
        final UriBuilder builder = UriBuilder.fromUri(basePath);
        return builder.path(instanceURI).buildFromMap(pathParameters);
    }
    
    public static Link constructLinkForAutomationResult(final String serviceProviderId, final String automationResultId , final String label)
    {
        return new Link(constructURIForAutomationResult(serviceProviderId, automationResultId), label);
    }
    
    public static Link constructLinkForAutomationResult(final String serviceProviderId, final String automationResultId)
    {
        return new Link(constructURIForAutomationResult(serviceProviderId, automationResultId));
    }
    

    //methods for Contribution resource
    public static Contribution createContribution(final String serviceProviderId, final String contributionId)
           throws URISyntaxException
    {
        return new Contribution(constructURIForContribution(serviceProviderId, contributionId));
    }
    
    public static URI constructURIForContribution(final String serviceProviderId, final String contributionId)
    {
        String basePath = OSLC4JUtils.getServletURI();
        Map<String, Object> pathParameters = new HashMap<String, Object>();
        pathParameters.put("serviceProviderId", serviceProviderId);
        pathParameters.put("contributionId", contributionId);
        String instanceURI = "serviceProviders/{serviceProviderId}/resources/contributions/{contributionId}";
    
        final UriBuilder builder = UriBuilder.fromUri(basePath);
        return builder.path(instanceURI).buildFromMap(pathParameters);
    }
    
    public static Link constructLinkForContribution(final String serviceProviderId, final String contributionId , final String label)
    {
        return new Link(constructURIForContribution(serviceProviderId, contributionId), label);
    }
    
    public static Link constructLinkForContribution(final String serviceProviderId, final String contributionId)
    {
        return new Link(constructURIForContribution(serviceProviderId, contributionId));
    }
    

}
