// Start of user code Copyright
/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
 *     Russell Boykin       - initial API and implementation
 *     Alberto Giammaria    - initial API and implementation
 *     Chris Peters         - initial API and implementation
 *     Gianluca Bernardini  - initial API and implementation
 *       Sam Padgett          - initial API and implementation
 *     Michael Fiedler      - adapted for OSLC4J
 *     Jad El-khoury        - initial implementation of code generator (422448)
 *     Matthieu Helleboid   - Support for multiple Service Providers.
 *     Anass Radouani       - Support for multiple Service Providers.
 *
 * This file is generated by org.eclipse.lyo.oslc4j.codegenerator
 *******************************************************************************/
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
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.lyo.oslc4j.core.OSLC4JUtils;
import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.core.annotation.OslcAllowedValue;
import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
import org.eclipse.lyo.oslc4j.core.annotation.OslcMemberProperty;
import org.eclipse.lyo.oslc4j.core.annotation.OslcName;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcOccurs;
import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
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
import org.eclipse.lyo.oslc.domains.RdfDomainConstants;

// Start of user code imports
// End of user code

// Start of user code preClassCode
// End of user code

// Start of user code classAnnotations
// End of user code
@OslcNamespace(Oslc_autoDomainConstants.PARAMETERINSTANCE_NAMESPACE)
@OslcName(Oslc_autoDomainConstants.PARAMETERINSTANCE_LOCALNAME)
@OslcResourceShape(title = "ParameterInstance Resource Shape", describes = Oslc_autoDomainConstants.PARAMETERINSTANCE_TYPE)
public class ParameterInstance
    extends AbstractResource
    implements IParameterInstance
{
    // Start of user code attributeAnnotation:value
    // End of user code
    private String value;
    // Start of user code attributeAnnotation:description
    // End of user code
    private String description;
    // Start of user code attributeAnnotation:type
    // End of user code
    private HashSet<Link> type = new HashSet<Link>();
    // Start of user code attributeAnnotation:instanceShape
    // End of user code
    private URI instanceShape;
    // Start of user code attributeAnnotation:serviceProvider
    // End of user code
    private HashSet<URI> serviceProvider = new HashSet<URI>();
    // Start of user code attributeAnnotation:name
    // End of user code
    private String name;
    
    // Start of user code classAttributes
    // End of user code
    // Start of user code classMethods
    // End of user code
    public ParameterInstance()
           throws URISyntaxException
    {
        super();
    
        // Start of user code constructor1
        // End of user code
    }
    
    public ParameterInstance(final URI about)
           throws URISyntaxException
    {
        super(about);
    
        // Start of user code constructor2
        // End of user code
    }
    
    
    public static ResourceShape createResourceShape() throws OslcCoreApplicationException, URISyntaxException {
        return ResourceShapeFactory.createResourceShape(OSLC4JUtils.getServletURI(),
        OslcConstants.PATH_RESOURCE_SHAPES,
        Oslc_autoDomainConstants.PARAMETERINSTANCE_PATH,
        ParameterInstance.class);
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
            result = result + "{a Local ParameterInstance Resource} - update ParameterInstance.toString() to present resource as desired.";
            // Start of user code toString_bodyForLocalResource
            // End of user code
        }
        else {
            result = getAbout().toString();
        }
    
        // Start of user code toString_finalize
        // End of user code
    
        return result;
    }
    
    @Deprecated
    public String toHtml()
    {
        return toHtml(false);
    }
    
    @Deprecated
    public String toHtml(boolean asLocalResource)
    {
        String result = "";
        // Start of user code toHtml_init
        // End of user code
    
        if (asLocalResource) {
            result = toString(true);
            // Start of user code toHtml_bodyForLocalResource
            // End of user code
        }
        else {
            result = "<a href=\"" + getAbout() + "\" class=\"oslc-resource-link\">" + toString() + "</a>";
        }
    
        // Start of user code toHtml_finalize
        // End of user code
    
        return result;
    }
    
    public void addType(final Link type)
    {
        this.type.add(type);
    }
    
    public void addServiceProvider(final URI serviceProvider)
    {
        this.serviceProvider.add(serviceProvider);
    }
    
    
    // Start of user code getterAnnotation:value
    // End of user code
    @OslcName("value")
    @OslcPropertyDefinition(RdfDomainConstants.RDF_NAMSPACE + "value")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcValueType(ValueType.XMLLiteral)
    @OslcReadOnly(false)
    public String getValue()
    {
        // Start of user code getterInit:value
        // End of user code
        return value;
    }
    
    // Start of user code getterAnnotation:description
    // End of user code
    @OslcName("description")
    @OslcPropertyDefinition(DctermsDomainConstants.DUBLIN_CORE_NAMSPACE + "description")
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
    
    // Start of user code getterAnnotation:type
    // End of user code
    @OslcName("type")
    @OslcPropertyDefinition(RdfDomainConstants.RDF_NAMSPACE + "type")
    @OslcDescription("The resource type URIs")
    @OslcOccurs(Occurs.ZeroOrMany)
    @OslcValueType(ValueType.Resource)
    @OslcReadOnly(false)
    public Set<Link> getType()
    {
        // Start of user code getterInit:type
        // End of user code
        return type;
    }
    
    // Start of user code getterAnnotation:instanceShape
    // End of user code
    @OslcName("instanceShape")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "instanceShape")
    @OslcDescription("The URI of a Resource Shape that describes the possible properties, occurrence, value types, allowed values and labels. This shape information is useful in displaying the subject resource as well as guiding clients in performing modifications. Instance shapes may be specific to the authenticated user associated with the request that retrieved the resource, the current state of the resource and other factors and thus should not be cached.")
    @OslcOccurs(Occurs.ZeroOrOne)
    @OslcReadOnly(false)
    public URI getInstanceShape()
    {
        // Start of user code getterInit:instanceShape
        // End of user code
        return instanceShape;
    }
    
    // Start of user code getterAnnotation:serviceProvider
    // End of user code
    @OslcName("serviceProvider")
    @OslcPropertyDefinition(OslcDomainConstants.OSLC_NAMSPACE + "serviceProvider")
    @OslcDescription("A link to the resource's OSLC Service Provider. There may be cases when the subject resource is available from a service provider that implements multiple domain specifications, which could result in multiple values for this property.")
    @OslcOccurs(Occurs.ZeroOrMany)
    @OslcReadOnly(false)
    public Set<URI> getServiceProvider()
    {
        // Start of user code getterInit:serviceProvider
        // End of user code
        return serviceProvider;
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
    
    
    // Start of user code setterAnnotation:value
    // End of user code
    public void setValue(final String value )
    {
        // Start of user code setterInit:value
        // End of user code
        this.value = value;
    
        // Start of user code setterFinalize:value
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
    
    // Start of user code setterAnnotation:type
    // End of user code
    public void setType(final Set<Link> type )
    {
        // Start of user code setterInit:type
        // End of user code
        this.type.clear();
        if (type != null)
        {
            this.type.addAll(type);
        }
    
        // Start of user code setterFinalize:type
        // End of user code
    }
    
    // Start of user code setterAnnotation:instanceShape
    // End of user code
    public void setInstanceShape(final URI instanceShape )
    {
        // Start of user code setterInit:instanceShape
        // End of user code
        this.instanceShape = instanceShape;
    
        // Start of user code setterFinalize:instanceShape
        // End of user code
    }
    
    // Start of user code setterAnnotation:serviceProvider
    // End of user code
    public void setServiceProvider(final Set<URI> serviceProvider )
    {
        // Start of user code setterInit:serviceProvider
        // End of user code
        this.serviceProvider.clear();
        if (serviceProvider != null)
        {
            this.serviceProvider.addAll(serviceProvider);
        }
    
        // Start of user code setterFinalize:serviceProvider
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
    
    
    @Deprecated
    static public String valueToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:valueToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"value\">value: </LABEL>";
    
        // Start of user code "Mid:valueToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"value\" type=\"text\" style=\"width: 400px\" id=\"value\" >";
        // Start of user code "Finalize:valueToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    @Deprecated
    static public String descriptionToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:descriptionToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"description\">description: </LABEL>";
    
        // Start of user code "Mid:descriptionToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"description\" type=\"text\" style=\"width: 400px\" id=\"description\" >";
        // Start of user code "Finalize:descriptionToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    @Deprecated
    static public String typeToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:typeToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"type\">type: </LABEL>";
    
        // Start of user code "Mid:typeToHtmlForCreation(...)"
        // End of user code
    
        // Start of user code "Finalize:typeToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    @Deprecated
    static public String instanceShapeToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:instanceShapeToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"instanceShape\">instanceShape: </LABEL>";
    
        // Start of user code "Mid:instanceShapeToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"instanceShape\" type=\"text\" style=\"width: 400px\" id=\"instanceShape\" >";
        // Start of user code "Finalize:instanceShapeToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    @Deprecated
    static public String serviceProviderToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:serviceProviderToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"serviceProvider\">serviceProvider: </LABEL>";
    
        // Start of user code "Mid:serviceProviderToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"serviceProvider\" type=\"text\" style=\"width: 400px\" id=\"serviceProvider\" >";
        // Start of user code "Finalize:serviceProviderToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    @Deprecated
    static public String nameToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:nameToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"name\">name: </LABEL>";
    
        // Start of user code "Mid:nameToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"name\" type=\"text\" style=\"width: 400px\" id=\"name\" >";
        // Start of user code "Finalize:nameToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    
    @Deprecated
    public String valueToHtml()
    {
        String s = "";
    
        // Start of user code valuetoHtml_mid
        // End of user code
    
        try {
            if (value == null) {
                s = s + "<em>null</em>";
            }
            else {
                s = s + value.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code valuetoHtml_finalize
        // End of user code
    
        return s;
    }
    
    @Deprecated
    public String descriptionToHtml()
    {
        String s = "";
    
        // Start of user code descriptiontoHtml_mid
        // End of user code
    
        try {
            if (description == null) {
                s = s + "<em>null</em>";
            }
            else {
                s = s + description.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code descriptiontoHtml_finalize
        // End of user code
    
        return s;
    }
    
    @Deprecated
    public String typeToHtml()
    {
        String s = "";
    
        // Start of user code typetoHtml_mid
        // End of user code
    
        try {
            s = s + "<ul>";
            for(Link next : type) {
                s = s + "<li>";
                if (next.getValue() == null) {
                    s= s + "<em>null</em>";
                }
                else {
                    s = s + "<a href=\"" + next.getValue().toString() + "\">" + next.getValue().toString() + "</a>";
                }
                s = s + "</li>";
            }
            s = s + "</ul>";
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code typetoHtml_finalize
        // End of user code
    
        return s;
    }
    
    @Deprecated
    public String instanceShapeToHtml()
    {
        String s = "";
    
        // Start of user code instanceShapetoHtml_mid
        // End of user code
    
        try {
            if (instanceShape == null) {
                s = s + "<em>null</em>";
            }
            else {
                s = s + instanceShape.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code instanceShapetoHtml_finalize
        // End of user code
    
        return s;
    }
    
    @Deprecated
    public String serviceProviderToHtml()
    {
        String s = "";
    
        // Start of user code serviceProvidertoHtml_mid
        // End of user code
    
        try {
            s = s + "<ul>";
            Iterator<URI> itr = serviceProvider.iterator();
            while(itr.hasNext()) {
                s = s + "<li>";
                s= s + itr.next().toString();
                s = s + "</li>";
            }
            s = s + "</ul>";
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code serviceProvidertoHtml_finalize
        // End of user code
    
        return s;
    }
    
    @Deprecated
    public String nameToHtml()
    {
        String s = "";
    
        // Start of user code nametoHtml_mid
        // End of user code
    
        try {
            if (name == null) {
                s = s + "<em>null</em>";
            }
            else {
                s = s + name.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code nametoHtml_finalize
        // End of user code
    
        return s;
    }
    
    
}
