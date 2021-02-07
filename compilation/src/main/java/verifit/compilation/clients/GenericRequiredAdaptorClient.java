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
 * Copyright (c) 2015 Jad El-khoury.
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
 *     Jad El-khoury        - initial implementation of client code
 *     
 *******************************************************************************/
// End of user code

package verifit.compilation.clients;

import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.OslcClient;
import org.eclipse.lyo.oslc4j.core.model.ServiceProviderCatalog;

// Start of user code imports
// End of user code


// Start of user code pre_class_code
// End of user code

public class GenericRequiredAdaptorClient
{

    // Start of user code class_attributes
    // End of user code
    
    // Start of user code class_methods
    // End of user code

    static String serviceProviderCatalogURI = "http://your.host.com/adaptor/services/catalog/singleton";

    public static ServiceProviderCatalog getServiceProviderCatalog() throws Exception {
        OslcClient client = new OslcClient();
        ClientResponse response = null;
        ServiceProviderCatalog catalog = null;

        // Start of user code getServiceProviderCatalog_init
        // End of user code

        response = client.getResource(serviceProviderCatalogURI,OSLCConstants.CT_RDF);
        if (response != null) {
            catalog = response.getEntity(ServiceProviderCatalog.class);
        }
        // Start of user code getServiceProviderCatalog_final
        // End of user code
        return catalog;
    }

}
