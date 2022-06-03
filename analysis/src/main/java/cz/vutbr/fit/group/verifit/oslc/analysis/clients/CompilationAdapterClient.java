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
 *
 * This file is generated by Lyo Designer (https://www.eclipse.org/lyo/)
 */
// End of user code

package cz.vutbr.fit.group.verifit.oslc.analysis.clients;

import javax.ws.rs.core.Response;
import org.eclipse.lyo.client.OSLCConstants;
import org.eclipse.lyo.client.OslcClient;
import org.eclipse.lyo.oslc4j.core.model.ServiceProviderCatalog;
import cz.vutbr.fit.group.verifit.oslc.domain.SUT;

// Start of user code imports
import cz.vutbr.fit.group.verifit.oslc.analysis.properties.VeriFitAnalysisProperties;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
// End of user code


// Start of user code pre_class_code
// End of user code

public class CompilationAdapterClient
{

    // Start of user code class_attributes
	static String username = VeriFitAnalysisProperties.AUTHENTICATION_USERNAME;
	static String password = VeriFitAnalysisProperties.AUTHENTICATION_PASSWORD;
    // End of user code
    
    // Start of user code class_methods
    // End of user code

    static String serviceProviderCatalogURI = "http://localhost:8081/compilation/services/catalog/singleton";

    public static ServiceProviderCatalog getServiceProviderCatalog() throws Exception {
        OslcClient client = new OslcClient();
        Response response = null;
        ServiceProviderCatalog catalog = null;

        // Start of user code getServiceProviderCatalog_init
        ClientBuilder builder = ClientBuilder.newBuilder();
        if(VeriFitAnalysisProperties.AUTHENTICATION_ENABLED)
        	builder.register(HttpAuthenticationFeature.basic(username, password.getBytes()));
        client = new OslcClient(builder);
        // End of user code

        response = client.getResource(serviceProviderCatalogURI,OSLCConstants.CT_RDF);
        if (response != null) {
            catalog = response.readEntity(ServiceProviderCatalog.class);
        }
        // Start of user code getServiceProviderCatalog_final
        if (catalog == null)
        	throw new Exception("status code: " + Integer.toString(response.getStatus()));
        // End of user code
        return catalog;
    }

    public static SUT getSUT(String resourceURI) throws Exception {
        OslcClient client = new OslcClient();
        Response response = null;
        SUT resource = null;

        // Start of user code getSUT_init
        ClientBuilder builder = ClientBuilder.newBuilder();
        if(VeriFitAnalysisProperties.AUTHENTICATION_ENABLED)
        	builder.register(HttpAuthenticationFeature.basic(username, password.getBytes()));
        client = new OslcClient(builder);
        // End of user code

        response = client.getResource(resourceURI, OSLCConstants.CT_RDF);
        if (response != null) {
            resource = response.readEntity(SUT.class);
        }
        // Start of user code getSUT_final
        if (resource == null)
        	throw new Exception("status code: " + Integer.toString(response.getStatus()));
        // End of user code
        return resource;
    }
}
