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
 *
 * This file is generated by Lyo Designer (https://www.eclipse.org/lyo/)
 */
// End of user code

package cz.vutbr.fit.group.verifit.oslc.analysis;

import org.eclipse.lyo.oslc4j.core.model.OslcConstants;

// Start of user code imports
// End of user code

public interface VeriFitAnalysisConstants
{
    // Start of user code user constants
	 
    /*
     *  Internal constants
     */
	public static final String AUTOMATION_PROVIDER_ID = "A0";

	/*
	 * OSLC Specification constants
	 */
    public static final String AUTOMATION_STATE_NEW = "http://open-services.net/ns/auto#new";
    public static final String AUTOMATION_STATE_QUEUED = "http://open-services.net/ns/auto#queued";
    public static final String AUTOMATION_STATE_INPROGRESS = "http://open-services.net/ns/auto#inProgress";
    public static final String AUTOMATION_STATE_CANCELING = "http://open-services.net/ns/auto#canceling";
    public static final String AUTOMATION_STATE_CANCELED = "http://open-services.net/ns/auto#canceled";
    public static final String AUTOMATION_STATE_COMPLETE = "http://open-services.net/ns/auto#complete";
    
    public static final String AUTOMATION_VERDICT_UNAVAILABLE = "http://open-services.net/ns/auto#unavailable";
    public static final String AUTOMATION_VERDICT_PASSED = "http://open-services.net/ns/auto#passed";
    public static final String AUTOMATION_VERDICT_WARNING = "http://open-services.net/ns/auto#warning";
    public static final String AUTOMATION_VERDICT_FAILED = "http://open-services.net/ns/auto#failed";
    public static final String AUTOMATION_VERDICT_ERROR = "http://open-services.net/ns/auto#error";

    public static final String OSLC_OCCURS_ZEROorONE = "http://open-services.net/ns/core#Zero-or-One";
    public static final String OSLC_OCCURS_ONE = "http://open-services.net/ns/core#Exactly-one";
    public static final String OSLC_OCCURS_ZEROorMany = "http://open-services.net/ns/core#Zero-or-many";
    public static final String OSLC_OCCURS_ONEorMany = "http://open-services.net/ns/core#One-or-many";
    
    // ADD more value types when needed
    // https://archive.open-services.net/bin/view/Main/OSLCCoreSpecAppendixA.html#Value_type_Property
    public static final String OSLC_VAL_TYPE_STRING = "http://www.w3.org/2001/XMLSchema#string";
    public static final String OSLC_VAL_TYPE_BOOL = "http://www.w3.org/2001/XMLSchema#boolean";
    public static final String OSLC_VAL_TYPE_INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
    public static final String OSLC_VAL_TYPE_BASE64BINARY = "http://www.w3.org/2001/XMLSchema#base64binary";
    
    // ADD more value types when needed
    // https://archive.open-services.net/bin/view/Main/OSLCCoreSpecAppendixA.html#Representation_Property
    public static final String OSLC_REPRESENTATION_EITHER = "http://open-services.net/ns/core#Either";

    // End of user code

    public static final String HDR_OSLC_VERSION = "OSLC-Core-Version";
    public static final String OSLC_VERSION_V2 = "2.0";
}