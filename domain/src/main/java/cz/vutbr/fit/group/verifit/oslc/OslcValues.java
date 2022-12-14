/* 
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.lyo.oslc4j.core.model.Link;

public final class OslcValues
{
	/*
	 * OSLC Specification constants
	 */
    public static final Link AUTOMATION_STATE_NEW = makeLink("http://open-services.net/ns/auto#new");
    public static final Link AUTOMATION_STATE_QUEUED = makeLink("http://open-services.net/ns/auto#queued");
    public static final Link AUTOMATION_STATE_INPROGRESS = makeLink("http://open-services.net/ns/auto#inProgress");
    public static final Link AUTOMATION_STATE_CANCELING = makeLink("http://open-services.net/ns/auto#canceling");
    public static final Link AUTOMATION_STATE_CANCELED = makeLink("http://open-services.net/ns/auto#canceled");
    public static final Link AUTOMATION_STATE_COMPLETE = makeLink("http://open-services.net/ns/auto#complete");
    
    public static final Link AUTOMATION_VERDICT_UNAVAILABLE = makeLink("http://open-services.net/ns/auto#unavailable");
    public static final Link AUTOMATION_VERDICT_PASSED = makeLink("http://open-services.net/ns/auto#passed");
    public static final Link AUTOMATION_VERDICT_WARNING = makeLink("http://open-services.net/ns/auto#warning");
    public static final Link AUTOMATION_VERDICT_FAILED = makeLink("http://open-services.net/ns/auto#failed");
    public static final Link AUTOMATION_VERDICT_ERROR = makeLink("http://open-services.net/ns/auto#error");

    public static final Link OSLC_OCCURS_ZEROorONE = makeLink("http://open-services.net/ns/core#Zero-or-One");
    public static final Link OSLC_OCCURS_ONE = makeLink("http://open-services.net/ns/core#Exactly-one");
    public static final Link OSLC_OCCURS_ZEROorMany = makeLink("http://open-services.net/ns/core#Zero-or-many");
    public static final Link OSLC_OCCURS_ONEorMany = makeLink("http://open-services.net/ns/core#One-or-many");
    
    // ADD more value types when needed
    // https://archive.open-services.net/bin/view/Main/OSLCCoreSpecAppendixA.html#Value_type_Property
    public static final Link OSLC_VAL_TYPE_STRING = makeLink("http://www.w3.org/2001/XMLSchema#string");
    public static final Link OSLC_VAL_TYPE_BOOL = makeLink("http://www.w3.org/2001/XMLSchema#boolean");
    public static final Link OSLC_VAL_TYPE_INTEGER = makeLink("http://www.w3.org/2001/XMLSchema#integer");
    public static final Link OSLC_VAL_TYPE_BASE64BINARY = makeLink("http://www.w3.org/2001/XMLSchema#base64binary");
    
    // ADD more value types when needed
    // https://archive.open-services.net/bin/view/Main/OSLCCoreSpecAppendixA.html#Representation_Property
    public static final Link OSLC_REPRESENTATION_EITHER = makeLink("http://open-services.net/ns/core#Either");
    
    
    private static Link makeLink(String uri)
    {
    	try {
			return new Link(new URI(uri));
		} catch (URISyntaxException e) {
			//will never happen
			return null;
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
	
	public static String getResourceIdFromUri(String uri)
	{
		return uri.substring(uri.lastIndexOf('/') + 1);
	}
}
