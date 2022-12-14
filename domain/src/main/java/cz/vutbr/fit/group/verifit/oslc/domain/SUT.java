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

package cz.vutbr.fit.group.verifit.oslc.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;

import org.eclipse.lyo.oslc4j.core.OSLC4JUtils;
import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.core.annotation.OslcAllowedValue;
import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
import org.eclipse.lyo.oslc4j.core.annotation.OslcMemberProperty;
import org.eclipse.lyo.oslc4j.core.annotation.OslcName;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcOccurs;
import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
import org.eclipse.lyo.oslc4j.core.annotation.OslcRdfCollectionType;
import org.eclipse.lyo.oslc4j.core.annotation.OslcRange;
import org.eclipse.lyo.oslc4j.core.annotation.OslcReadOnly;
import org.eclipse.lyo.oslc4j.core.annotation.OslcRepresentation;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
import org.eclipse.lyo.oslc4j.core.annotation.OslcTitle;
import org.eclipse.lyo.oslc4j.core.annotation.OslcValueType;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.Occurs;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.Representation;
import org.eclipse.lyo.oslc4j.core.model.ValueType;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;
import org.eclipse.lyo.oslc4j.core.model.ResourceShapeFactory;

import cz.vutbr.fit.group.verifit.oslc.domain.FitDomainConstants;


import org.eclipse.lyo.oslc.domains.auto.Oslc_autoDomainConstants;
import org.eclipse.lyo.oslc.domains.DctermsDomainConstants;
import org.eclipse.lyo.oslc.domains.FoafDomainConstants;
import cz.vutbr.fit.group.verifit.oslc.domain.FitDomainConstants;
import org.eclipse.lyo.oslc.domains.DctermsVocabularyConstants;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.Person;
// Start of user code imports
import cz.vutbr.fit.group.verifit.oslc.OslcValues;
// End of user code

// Start of user code preClassCode
// End of user code

// Start of user code classAnnotations
// End of user code
@OslcNamespace(FitDomainConstants.SUT_NAMESPACE)
@OslcName(FitDomainConstants.SUT_LOCALNAME)
@OslcResourceShape(title = "SUT Resource Shape", describes = FitDomainConstants.SUT_TYPE)
public class SUT
    extends AbstractResource
    implements ISUT
{
    // Start of user code attributeAnnotation:launchCommand
    // End of user code
    private String launchCommand;
    // Start of user code attributeAnnotation:buildCommand
    // End of user code
    private String buildCommand;
    // Start of user code attributeAnnotation:title
    // End of user code
    private String title;
    // Start of user code attributeAnnotation:description
    // End of user code
    private String description;
    // Start of user code attributeAnnotation:created
    // End of user code
    private Date created;
    // Start of user code attributeAnnotation:modified
    // End of user code
    private Date modified;
    // Start of user code attributeAnnotation:identifier
    // End of user code
    private String identifier;
    // Start of user code attributeAnnotation:sUTdirectoryPath
    // End of user code
    private String sUTdirectoryPath;
    // Start of user code attributeAnnotation:creator
    // End of user code
    private Set<Link> creator = new HashSet<Link>();
    // Start of user code attributeAnnotation:compiled
    // End of user code
    private Boolean compiled;
    // Start of user code attributeAnnotation:producedByAutomationRequest
    // End of user code
    private Link producedByAutomationRequest;
    
    // Start of user code classAttributes
    // End of user code
    // Start of user code classMethods
    // End of user code
    public SUT()
    {
        super();
    
        // Start of user code constructor1
        // End of user code
    }
    
    public SUT(final URI about)
    {
        super(about);
    
        // Start of user code constructor2
		this.setIdentifier(OslcValues.getResourceIdFromUri(about));
		Date timestamp = new Date();
		this.setCreated(timestamp);
		this.setModified(timestamp);
        // End of user code
    }
    
    public static ResourceShape createResourceShape() throws OslcCoreApplicationException, URISyntaxException {
        return ResourceShapeFactory.createResourceShape(OSLC4JUtils.getServletURI(),
        OslcConstants.PATH_RESOURCE_SHAPES,
        FitDomainConstants.SUT_PATH,
        SUT.class);
    }
    
    
    public String toString()
    {
        return toString(false);
    }
    
    public String toString(boolean asLocalResource)
    {
        String result = "";
        // Start of user code toString_init
        // End of user code
    
        if (asLocalResource) {
            result = result + "{a Local SUT Resource} - update SUT.toString() to present resource as desired.";
            // Start of user code toString_bodyForLocalResource
            result = String.valueOf(getAbout());
            // End of user code
        }
        else {
            result = String.valueOf(getAbout());
        }
    
        // Start of user code toString_finalize
        // End of user code
    
        return result;
    }
    
    public void addCreator(final Link creator)
    {
        this.creator.add(creator);
    }
    
    
    // Start of user code getterAnnotation:launchCommand
    // End of user code
    @OslcName("launchCommand")
    @OslcPropertyDefinition(FitDomainConstants.VERIFIT_UNIVERSAL_ANALYSIS_NAMSPACE + "launchCommand")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.XMLLiteral)
    @OslcReadOnly(false)
    public String getLaunchCommand()
    {
        // Start of user code getterInit:launchCommand
        // End of user code
        return launchCommand;
    }
    
    // Start of user code getterAnnotation:buildCommand
    // End of user code
    @OslcName("buildCommand")
    @OslcPropertyDefinition(FitDomainConstants.VERIFIT_UNIVERSAL_ANALYSIS_NAMSPACE + "buildCommand")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.XMLLiteral)
    @OslcReadOnly(false)
    public String getBuildCommand()
    {
        // Start of user code getterInit:buildCommand
        // End of user code
        return buildCommand;
    }
    
    // Start of user code getterAnnotation:title
    // End of user code
    @OslcName("title")
    @OslcPropertyDefinition(DctermsVocabularyConstants.DUBLIN_CORE_NAMSPACE + "title")
    @OslcDescription("Title of the resource represented as rich text in XHTML content. SHOULD include only content that is valid inside an XHTML <span> element.")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcValueType(ValueType.XMLLiteral)
    @OslcReadOnly(false)
    public String getTitle()
    {
        // Start of user code getterInit:title
        // End of user code
        return title;
    }
    
    // Start of user code getterAnnotation:description
    // End of user code
    @OslcName("description")
    @OslcPropertyDefinition(DctermsVocabularyConstants.DUBLIN_CORE_NAMSPACE + "description")
    @OslcDescription("Descriptive text about resource represented as rich text in XHTML content. SHOULD include only content that is valid and suitable inside an XHTML <div> element.")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.XMLLiteral)
    @OslcReadOnly(false)
    public String getDescription()
    {
        // Start of user code getterInit:description
        // End of user code
        return description;
    }
    
    // Start of user code getterAnnotation:created
    // End of user code
    @OslcName("created")
    @OslcPropertyDefinition(DctermsVocabularyConstants.DUBLIN_CORE_NAMSPACE + "created")
    @OslcDescription("Timestamp of resource creation")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.DateTime)
    @OslcReadOnly(false)
    public Date getCreated()
    {
        // Start of user code getterInit:created
        // End of user code
        return created;
    }
    
    // Start of user code getterAnnotation:modified
    // End of user code
    @OslcName("modified")
    @OslcPropertyDefinition(DctermsVocabularyConstants.DUBLIN_CORE_NAMSPACE + "modified")
    @OslcDescription("Timestamp of latest resource modification")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.DateTime)
    @OslcReadOnly(false)
    public Date getModified()
    {
        // Start of user code getterInit:modified
        // End of user code
        return modified;
    }
    
    // Start of user code getterAnnotation:identifier
    // End of user code
    @OslcName("identifier")
    @OslcPropertyDefinition(DctermsVocabularyConstants.DUBLIN_CORE_NAMSPACE + "identifier")
    @OslcDescription("A unique identifier for a resource. Typically read-only and assigned by the service provider when a resource is created. Not typically intended for end-user display.")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcValueType(ValueType.String)
    @OslcReadOnly(false)
    public String getIdentifier()
    {
        // Start of user code getterInit:identifier
        // End of user code
        return identifier;
    }
    
    // Start of user code getterAnnotation:sUTdirectoryPath
    // End of user code
    @OslcName("SUTdirectoryPath")
    @OslcPropertyDefinition(FitDomainConstants.VERIFIT_UNIVERSAL_ANALYSIS_NAMSPACE + "SUTdirectoryPath")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcValueType(ValueType.XMLLiteral)
    @OslcReadOnly(false)
    public String getSUTdirectoryPath()
    {
        // Start of user code getterInit:sUTdirectoryPath
        // End of user code
        return sUTdirectoryPath;
    }
    
    // Start of user code getterAnnotation:creator
    // End of user code
    @OslcName("creator")
    @OslcPropertyDefinition(DctermsVocabularyConstants.DUBLIN_CORE_NAMSPACE + "creator")
    @OslcDescription("Creator or creators of the resource. It is likely that the target resource will be a foaf:Person but that is not necessarily the case.")
    @OslcOccurs(Occurs.ZeroOrMany)
    @OslcValueType(ValueType.Resource)
    @OslcRange({FoafDomainConstants.PERSON_TYPE})
    @OslcReadOnly(false)
    public Set<Link> getCreator()
    {
        // Start of user code getterInit:creator
        // End of user code
        return creator;
    }
    
    // Start of user code getterAnnotation:compiled
    // End of user code
    @OslcName("compiled")
    @OslcPropertyDefinition(FitDomainConstants.VERIFIT_UNIVERSAL_ANALYSIS_NAMSPACE + "compiled")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcValueType(ValueType.Boolean)
    @OslcReadOnly(false)
    public Boolean isCompiled()
    {
        // Start of user code getterInit:compiled
        // End of user code
        return compiled;
    }
    
    // Start of user code getterAnnotation:producedByAutomationRequest
    // End of user code
    @OslcName("producedByAutomationRequest")
    @OslcPropertyDefinition(Oslc_autoDomainConstants.AUTOMATION_NAMSPACE + "producedByAutomationRequest")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.Resource)
    @OslcRange({Oslc_autoDomainConstants.AUTOMATIONREQUEST_TYPE})
    @OslcReadOnly(false)
    public Link getProducedByAutomationRequest()
    {
        // Start of user code getterInit:producedByAutomationRequest
        // End of user code
        return producedByAutomationRequest;
    }
    
    
    // Start of user code setterAnnotation:launchCommand
    // End of user code
    public void setLaunchCommand(final String launchCommand )
    {
        // Start of user code setterInit:launchCommand
        // End of user code
        this.launchCommand = launchCommand;
    
        // Start of user code setterFinalize:launchCommand
        // End of user code
    }
    
    // Start of user code setterAnnotation:buildCommand
    // End of user code
    public void setBuildCommand(final String buildCommand )
    {
        // Start of user code setterInit:buildCommand
        // End of user code
        this.buildCommand = buildCommand;
    
        // Start of user code setterFinalize:buildCommand
        // End of user code
    }
    
    // Start of user code setterAnnotation:title
    // End of user code
    public void setTitle(final String title )
    {
        // Start of user code setterInit:title
        // End of user code
        this.title = title;
    
        // Start of user code setterFinalize:title
        // End of user code
    }
    
    // Start of user code setterAnnotation:description
    // End of user code
    public void setDescription(final String description )
    {
        // Start of user code setterInit:description
        // End of user code
        this.description = description;
    
        // Start of user code setterFinalize:description
        // End of user code
    }
    
    // Start of user code setterAnnotation:created
    // End of user code
    public void setCreated(final Date created )
    {
        // Start of user code setterInit:created
        // End of user code
        this.created = created;
    
        // Start of user code setterFinalize:created
        // End of user code
    }
    
    // Start of user code setterAnnotation:modified
    // End of user code
    public void setModified(final Date modified )
    {
        // Start of user code setterInit:modified
        // End of user code
        this.modified = modified;
    
        // Start of user code setterFinalize:modified
        // End of user code
    }
    
    // Start of user code setterAnnotation:identifier
    // End of user code
    public void setIdentifier(final String identifier )
    {
        // Start of user code setterInit:identifier
        // End of user code
        this.identifier = identifier;
    
        // Start of user code setterFinalize:identifier
        // End of user code
    }
    
    // Start of user code setterAnnotation:sUTdirectoryPath
    // End of user code
    public void setSUTdirectoryPath(final String sUTdirectoryPath )
    {
        // Start of user code setterInit:sUTdirectoryPath
        // End of user code
        this.sUTdirectoryPath = sUTdirectoryPath;
    
        // Start of user code setterFinalize:sUTdirectoryPath
        // End of user code
    }
    
    // Start of user code setterAnnotation:creator
    // End of user code
    public void setCreator(final Set<Link> creator )
    {
        // Start of user code setterInit:creator
        // End of user code
        this.creator.clear();
        if (creator != null)
        {
            this.creator.addAll(creator);
        }
    
        // Start of user code setterFinalize:creator
        // End of user code
    }
    
    // Start of user code setterAnnotation:compiled
    // End of user code
    public void setCompiled(final Boolean compiled )
    {
        // Start of user code setterInit:compiled
        // End of user code
        this.compiled = compiled;
    
        // Start of user code setterFinalize:compiled
        // End of user code
    }
    
    // Start of user code setterAnnotation:producedByAutomationRequest
    // End of user code
    public void setProducedByAutomationRequest(final Link producedByAutomationRequest )
    {
        // Start of user code setterInit:producedByAutomationRequest
        // End of user code
        this.producedByAutomationRequest = producedByAutomationRequest;
    
        // Start of user code setterFinalize:producedByAutomationRequest
        // End of user code
    }
    
    
}
