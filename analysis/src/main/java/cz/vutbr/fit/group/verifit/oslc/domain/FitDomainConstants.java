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

package cz.vutbr.fit.group.verifit.oslc.domain;

import org.eclipse.lyo.oslc4j.core.model.OslcConstants;


// Start of user code imports
// End of user code

public interface FitDomainConstants
{
    // Start of user code user constants
    // End of user code

    public static String VERIFIT_UNIVERSAL_ANALYSIS_DOMAIN = "http://fit.vutbr.cz/group/verifit/oslc/ns/universal-analysis#";
    public static String VERIFIT_UNIVERSAL_ANALYSIS_NAMSPACE = "http://fit.vutbr.cz/group/verifit/oslc/ns/universal-analysis#";
    public static String VERIFIT_UNIVERSAL_ANALYSIS_NAMSPACE_PREFIX = "fit";

    public static String SUT_PATH = "sUT";
    public static String SUT_NAMESPACE = VERIFIT_UNIVERSAL_ANALYSIS_NAMSPACE; //namespace of the rdfs:class the resource describes
    public static String SUT_LOCALNAME = "SUT"; //localName of the rdfs:class the resource describes
    public static String SUT_TYPE = SUT_NAMESPACE + SUT_LOCALNAME; //fullname of the rdfs:class the resource describes
}