// Start of user code Copyright

/* ## License for manual implementation (enclosed in "// Start of user code ..." and "// End of user code ...") ##
 *
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// ## License for generated code: ##
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

package verifit.compilation;

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
import verifit.compilation.resources.SUT;

// Start of user code imports
// End of user code

// Start of user code pre_class_code
// End of user code

public class VeriFitCompilationResourcesFactory {

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
    

    //methods for SUT resource
    public static SUT createSUT(final String serviceProviderId, final String sUTId)
           throws URISyntaxException
    {
        return new SUT(constructURIForSUT(serviceProviderId, sUTId));
    }
    
    public static URI constructURIForSUT(final String serviceProviderId, final String sUTId)
    {
        String basePath = OSLC4JUtils.getServletURI();
        Map<String, Object> pathParameters = new HashMap<String, Object>();
        pathParameters.put("serviceProviderId", serviceProviderId);
        pathParameters.put("sUTId", sUTId);
        String instanceURI = "serviceProviders/{serviceProviderId}/resources/sUTs/{sUTId}";
    
        final UriBuilder builder = UriBuilder.fromUri(basePath);
        return builder.path(instanceURI).buildFromMap(pathParameters);
    }
    
    public static Link constructLinkForSUT(final String serviceProviderId, final String sUTId , final String label)
    {
        return new Link(constructURIForSUT(serviceProviderId, sUTId), label);
    }
    
    public static Link constructLinkForSUT(final String serviceProviderId, final String sUTId)
    {
        return new Link(constructURIForSUT(serviceProviderId, sUTId));
    }
    

}
