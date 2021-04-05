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

package org.eclipse.lyo.oslc.domains.auto;

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

import org.eclipse.lyo.oslc.domains.auto.Oslc_autoDomainConstants;


import org.eclipse.lyo.oslc.domains.DctermsDomainConstants;
import org.eclipse.lyo.oslc4j.core.model.OslcDomainConstants;
import cz.vutbr.fit.group.verifit.oslc.domain.FitDomainConstants;
import org.eclipse.lyo.oslc.domains.DctermsVocabularyConstants;

// Start of user code imports
// End of user code

// Start of user code preClassCode
// End of user code

// Start of user code classAnnotations
// End of user code
@OslcNamespace(Oslc_autoDomainConstants.PARAMETERDEFINITION_NAMESPACE)
@OslcName(Oslc_autoDomainConstants.PARAMETERDEFINITION_LOCALNAME)
@OslcResourceShape(title = "ParameterDefinition Resource Shape", describes = Oslc_autoDomainConstants.PARAMETERDEFINITION_TYPE)
public class ParameterDefinition
    extends AbstractResource
    implements IParameterDefinition
{
    // Start of user code attributeAnnotation:description
    // End of user code
    private String description;
    // Start of user code attributeAnnotation:title
    // End of user code
    private String title;
    // Start of user code attributeAnnotation:allowedValue
    // End of user code
    private Set<String> allowedValue = new HashSet<String>();
    // Start of user code attributeAnnotation:defaultValue
    // End of user code
    private String defaultValue;
    // Start of user code attributeAnnotation:allowedValues
    // End of user code
    private Link allowedValues;
    // Start of user code attributeAnnotation:hidden
    // End of user code
    private Boolean hidden;
    // Start of user code attributeAnnotation:isMemberProperty
    // End of user code
    private Boolean isMemberProperty;
    // Start of user code attributeAnnotation:name
    // End of user code
    private String name;
    // Start of user code attributeAnnotation:maxSize
    // End of user code
    private Integer maxSize;
    // Start of user code attributeAnnotation:occurs
    // End of user code
    private Link occurs;
    // Start of user code attributeAnnotation:range
    // End of user code
    private Set<Link> range = new HashSet<Link>();
    // Start of user code attributeAnnotation:readOnly
    // End of user code
    private Boolean readOnly;
    // Start of user code attributeAnnotation:representation
    // End of user code
    private Link representation;
    // Start of user code attributeAnnotation:valueType
    // End of user code
    private Set<Link> valueType = new HashSet<Link>();
    // Start of user code attributeAnnotation:valueShape
    // End of user code
    private Link valueShape;
    // Start of user code attributeAnnotation:commandlinePosition
    // End of user code
    private Integer commandlinePosition;
    // Start of user code attributeAnnotation:propertyDefinition
    // End of user code
    private Link propertyDefinition;
    
    // Start of user code classAttributes
    // End of user code
    // Start of user code classMethods
    // End of user code
    public ParameterDefinition()
    {
        super();
    
        // Start of user code constructor1
        // End of user code
    }
    
    public ParameterDefinition(final URI about)
    {
        super(about);
    
        // Start of user code constructor2
        // End of user code
    }
    
    public static ResourceShape createResourceShape() throws OslcCoreApplicationException, URISyntaxException {
        return ResourceShapeFactory.createResourceShape(OSLC4JUtils.getServletURI(),
        OslcConstants.PATH_RESOURCE_SHAPES,
        Oslc_autoDomainConstants.PARAMETERDEFINITION_PATH,
        ParameterDefinition.class);
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
            result = result + "{a Local ParameterDefinition Resource} - update ParameterDefinition.toString() to present resource as desired.";
            // Start of user code toString_bodyForLocalResource
            result = (name != null ? "--name=" + name + "<br>" : "") + (maxSize != null ? "maxSize=" + maxSize + "<br>" : "")
            		+ (title != null ? "--title=" + title + "<br>" : "")
            		+ (description != null ? "--description=" + description + "<br>" : "")
            		+ (occurs != null ? "--occurs=" + occurs.getValue() + "<br>" : "")
            		+ (defaultValue != null ? "--defaultValue=" + defaultValue + "<br>" : "")
    				+ (commandlinePosition != null ? "--commandlinePosition=" + commandlinePosition + "<br>" : "")
    				+ (valueType != null && !valueType.isEmpty()? "--valueType=" + valueType.iterator().next().getValue() + "<br>" : "")
    				+ (allowedValues != null && allowedValues.getValue() != null ? "--allowedValues=" + allowedValues.getValue() + "<br>" : "")
    				+ (allowedValue != null && !allowedValue.isEmpty() ? "--allowedValue=" + allowedValue + "<br>" : "")
    				+ (hidden != null ? "--hidden=" + hidden + "<br>" : "")
    				+ (readOnly != null ? "--readOnly=" + readOnly + "<br>" : "")
    				+ (isMemberProperty != null ? "--isMemberProperty=" + isMemberProperty + "<br>" : "")
    				+ (propertyDefinition != null && propertyDefinition.getValue() != null ? "--propertyDefinition=" + propertyDefinition.getValue() + "<br>" : "")
    				+ (range != null && !range.isEmpty() ? "--range=" + range + "<br>" : "")
    				+ (representation != null && representation.getValue() != null ? "--representation=" + representation.getValue() + "<br>" : "")
    				+ (valueShape != null && valueShape.getValue() != null ? "--valueShape=" + valueShape.getValue() : "");
            // End of user code
        }
        else {
            result = String.valueOf(getAbout());
        }
    
        // Start of user code toString_finalize
        // End of user code
    
        return result;
    }
    
    public void addAllowedValue(final String allowedValue)
    {
        this.allowedValue.add(allowedValue);
    }
    
    public void addRange(final Link range)
    {
        this.range.add(range);
    }
    
    public void addValueType(final Link valueType)
    {
        this.valueType.add(valueType);
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
    
    // Start of user code getterAnnotation:allowedValue
    // End of user code
    @OslcName("allowedValue")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "allowedValue")
    @OslcDescription("value allowed for a property")
    @OslcOccurs(Occurs.OneOrMany)
    @OslcValueType(ValueType.String)
    @OslcReadOnly(false)
    public Set<String> getAllowedValue()
    {
        // Start of user code getterInit:allowedValue
        // End of user code
        return allowedValue;
    }
    
    // Start of user code getterAnnotation:defaultValue
    // End of user code
    @OslcName("defaultValue")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "defaultValue")
    @OslcDescription("A default value for property, inlined into property definition. ")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.String)
    @OslcReadOnly(false)
    public String getDefaultValue()
    {
        // Start of user code getterInit:defaultValue
        // End of user code
        return defaultValue;
    }
    
    // Start of user code getterAnnotation:allowedValues
    // End of user code
    @OslcName("allowedValues")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "allowedValues")
    @OslcDescription("Resource with allowed values for the property being defined. ")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcValueType(ValueType.Resource)
    @OslcReadOnly(false)
    public Link getAllowedValues()
    {
        // Start of user code getterInit:allowedValues
        // End of user code
        return allowedValues;
    }
    
    // Start of user code getterAnnotation:hidden
    // End of user code
    @OslcName("hidden")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "hidden")
    @OslcDescription("A hint that indicates that property MAY be hidden when presented in a user interface ")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.Boolean)
    @OslcReadOnly(false)
    public Boolean isHidden()
    {
        // Start of user code getterInit:hidden
        // End of user code
        return hidden;
    }
    
    // Start of user code getterAnnotation:isMemberProperty
    // End of user code
    @OslcName("isMemberProperty")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "isMemberProperty")
    @OslcDescription("If set to true, this indicates that the property is a membership property, as described in the Query Syntax Specification: Member List Patterns. This is useful when the resource whose shape is being defined is viewed as a container of other resources. For example, look at the last example in Appendix B's RDF/XML Representation Examples: Specifying the shape of a query result, where blog:comment is defined as a membership property and comment that matches the query is returned as value of that property.")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.Boolean)
    @OslcReadOnly(false)
    public Boolean isIsMemberProperty()
    {
        // Start of user code getterInit:isMemberProperty
        // End of user code
        return isMemberProperty;
    }
    
    // Start of user code getterAnnotation:name
    // End of user code
    @OslcName("name")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "name")
    @OslcDescription("Name of property being defined, i.e. second part of property's Prefixed Name")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcValueType(ValueType.String)
    @OslcReadOnly(false)
    public String getName()
    {
        // Start of user code getterInit:name
        // End of user code
        return name;
    }
    
    // Start of user code getterAnnotation:maxSize
    // End of user code
    @OslcName("maxSize")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "maxSize")
    @OslcDescription("For String properties only, specifies maximum characters allowed. If not set, then there is no maximum or maximum is specified elsewhere.")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.Integer)
    @OslcReadOnly(false)
    public Integer getMaxSize()
    {
        // Start of user code getterInit:maxSize
        // End of user code
        return maxSize;
    }
    
    // Start of user code getterAnnotation:occurs
    // End of user code
    @OslcName("occurs")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "occurs")
    @OslcDescription("MUST be either http://open-services.net/ns/core#Exactly-one (property is required), http://open-services.net/ns/core#Zero-or-one (property is optional), http://open-services.net/ns/core#zeroOrMany (property is optional), or http://open-services.net/ns/core#One-or-many (property is required)")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcValueType(ValueType.Resource)
    @OslcReadOnly(false)
    public Link getOccurs()
    {
        // Start of user code getterInit:occurs
        // End of user code
        return occurs;
    }
    
    // Start of user code getterAnnotation:range
    // End of user code
    @OslcName("range")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "range")
    @OslcDescription("For properties with a resource value-type, Providers MAY also specify the range of possible resource types allowed, each specified by URI. The default range is http://open-services.net/ns/core#Any.")
    @OslcOccurs(Occurs.ZeroOrMany)
    @OslcValueType(ValueType.Resource)
    @OslcReadOnly(false)
    public Set<Link> getRange()
    {
        // Start of user code getterInit:range
        // End of user code
        return range;
    }
    
    // Start of user code getterAnnotation:readOnly
    // End of user code
    @OslcName("readOnly")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "readOnly")
    @OslcDescription("true if the property is read-only. If omitted, or set to false, then the property is writable. Providers SHOULD declare a property read-only when changes to the value of that property will not be accepted after the resource has been created, e.g. on PUT/PATCH requests. Consumers should note that the converse does not apply: Providers MAY reject a change to the value of a writable property.")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.Boolean)
    @OslcReadOnly(false)
    public Boolean isReadOnly()
    {
        // Start of user code getterInit:readOnly
        // End of user code
        return readOnly;
    }
    
    // Start of user code getterAnnotation:representation
    // End of user code
    @OslcName("representation")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "representation")
    @OslcDescription("Should be http://open-services.net/ns/core#Reference, http://open-services.net/ns/core#Inline or http://open-services.net/ns/core#Either")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.Resource)
    @OslcReadOnly(false)
    public Link getRepresentation()
    {
        // Start of user code getterInit:representation
        // End of user code
        return representation;
    }
    
    // Start of user code getterAnnotation:valueType
    // End of user code
    @OslcName("valueType")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "valueType")
    @OslcDescription("See below for list of allowed values for oslc:valueType. If this property is omitted, then the value type is unconstrained.")
    @OslcOccurs(Occurs.ZeroOrMany)
    @OslcValueType(ValueType.Resource)
    @OslcReadOnly(false)
    public Set<Link> getValueType()
    {
        // Start of user code getterInit:valueType
        // End of user code
        return valueType;
    }
    
    // Start of user code getterAnnotation:valueShape
    // End of user code
    @OslcName("valueShape")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "valueShape")
    @OslcDescription("if the value-type is a resource type, then Property MAY provide a shape value to indicate the Resource Shape that applies to the resource.")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcValueType(ValueType.Resource)
    @OslcReadOnly(false)
    public Link getValueShape()
    {
        // Start of user code getterInit:valueShape
        // End of user code
        return valueShape;
    }
    
    // Start of user code getterAnnotation:commandlinePosition
    // End of user code
    @OslcName("commandlinePosition")
    @OslcPropertyDefinition(FitDomainConstants.VERIFIT_UNIVERSAL_ANALYSIS_NAMSPACE + "commandlinePosition")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.Integer)
    @OslcReadOnly(false)
    public Integer getCommandlinePosition()
    {
        // Start of user code getterInit:commandlinePosition
        // End of user code
        return commandlinePosition;
    }
    
    // Start of user code getterAnnotation:propertyDefinition
    // End of user code
    @OslcName("propertyDefinition")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "propertyDefinition")
    @OslcDescription("URI of the property whose usage is being described.")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.Resource)
    @OslcReadOnly(false)
    public Link getPropertyDefinition()
    {
        // Start of user code getterInit:propertyDefinition
        // End of user code
        return propertyDefinition;
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
    
    // Start of user code setterAnnotation:allowedValue
    // End of user code
    public void setAllowedValue(final Set<String> allowedValue )
    {
        // Start of user code setterInit:allowedValue
        // End of user code
        this.allowedValue.clear();
        if (allowedValue != null)
        {
            this.allowedValue.addAll(allowedValue);
        }
    
        // Start of user code setterFinalize:allowedValue
        // End of user code
    }
    
    // Start of user code setterAnnotation:defaultValue
    // End of user code
    public void setDefaultValue(final String defaultValue )
    {
        // Start of user code setterInit:defaultValue
        // End of user code
        this.defaultValue = defaultValue;
    
        // Start of user code setterFinalize:defaultValue
        // End of user code
    }
    
    // Start of user code setterAnnotation:allowedValues
    // End of user code
    public void setAllowedValues(final Link allowedValues )
    {
        // Start of user code setterInit:allowedValues
        // End of user code
        this.allowedValues = allowedValues;
    
        // Start of user code setterFinalize:allowedValues
        // End of user code
    }
    
    // Start of user code setterAnnotation:hidden
    // End of user code
    public void setHidden(final Boolean hidden )
    {
        // Start of user code setterInit:hidden
        // End of user code
        this.hidden = hidden;
    
        // Start of user code setterFinalize:hidden
        // End of user code
    }
    
    // Start of user code setterAnnotation:isMemberProperty
    // End of user code
    public void setIsMemberProperty(final Boolean isMemberProperty )
    {
        // Start of user code setterInit:isMemberProperty
        // End of user code
        this.isMemberProperty = isMemberProperty;
    
        // Start of user code setterFinalize:isMemberProperty
        // End of user code
    }
    
    // Start of user code setterAnnotation:name
    // End of user code
    public void setName(final String name )
    {
        // Start of user code setterInit:name
        // End of user code
        this.name = name;
    
        // Start of user code setterFinalize:name
        // End of user code
    }
    
    // Start of user code setterAnnotation:maxSize
    // End of user code
    public void setMaxSize(final Integer maxSize )
    {
        // Start of user code setterInit:maxSize
        // End of user code
        this.maxSize = maxSize;
    
        // Start of user code setterFinalize:maxSize
        // End of user code
    }
    
    // Start of user code setterAnnotation:occurs
    // End of user code
    public void setOccurs(final Link occurs )
    {
        // Start of user code setterInit:occurs
        // End of user code
        this.occurs = occurs;
    
        // Start of user code setterFinalize:occurs
        // End of user code
    }
    
    // Start of user code setterAnnotation:range
    // End of user code
    public void setRange(final Set<Link> range )
    {
        // Start of user code setterInit:range
        // End of user code
        this.range.clear();
        if (range != null)
        {
            this.range.addAll(range);
        }
    
        // Start of user code setterFinalize:range
        // End of user code
    }
    
    // Start of user code setterAnnotation:readOnly
    // End of user code
    public void setReadOnly(final Boolean readOnly )
    {
        // Start of user code setterInit:readOnly
        // End of user code
        this.readOnly = readOnly;
    
        // Start of user code setterFinalize:readOnly
        // End of user code
    }
    
    // Start of user code setterAnnotation:representation
    // End of user code
    public void setRepresentation(final Link representation )
    {
        // Start of user code setterInit:representation
        // End of user code
        this.representation = representation;
    
        // Start of user code setterFinalize:representation
        // End of user code
    }
    
    // Start of user code setterAnnotation:valueType
    // End of user code
    public void setValueType(final Set<Link> valueType )
    {
        // Start of user code setterInit:valueType
        // End of user code
        this.valueType.clear();
        if (valueType != null)
        {
            this.valueType.addAll(valueType);
        }
    
        // Start of user code setterFinalize:valueType
        // End of user code
    }
    
    // Start of user code setterAnnotation:valueShape
    // End of user code
    public void setValueShape(final Link valueShape )
    {
        // Start of user code setterInit:valueShape
        // End of user code
        this.valueShape = valueShape;
    
        // Start of user code setterFinalize:valueShape
        // End of user code
    }
    
    // Start of user code setterAnnotation:commandlinePosition
    // End of user code
    public void setCommandlinePosition(final Integer commandlinePosition )
    {
        // Start of user code setterInit:commandlinePosition
        // End of user code
        this.commandlinePosition = commandlinePosition;
    
        // Start of user code setterFinalize:commandlinePosition
        // End of user code
    }
    
    // Start of user code setterAnnotation:propertyDefinition
    // End of user code
    public void setPropertyDefinition(final Link propertyDefinition )
    {
        // Start of user code setterInit:propertyDefinition
        // End of user code
        this.propertyDefinition = propertyDefinition;
    
        // Start of user code setterFinalize:propertyDefinition
        // End of user code
    }
    
    
}
