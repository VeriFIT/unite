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

package verifit.compilation.resources;

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

import verifit.compilation.resources.FitDomainConstants;


import org.eclipse.lyo.oslc.domains.DctermsDomainConstants;
import org.eclipse.lyo.oslc.domains.FoafDomainConstants;
import verifit.compilation.resources.FitDomainConstants;
import org.eclipse.lyo.oslc.domains.Person;

// Start of user code imports
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
    // Start of user code attributeAnnotation:title
    // End of user code
    private String title;
    // Start of user code attributeAnnotation:description
    // End of user code
    private String description;
    // Start of user code attributeAnnotation:created
    // End of user code
    private Date created;
    // Start of user code attributeAnnotation:creator
    // End of user code
    private HashSet<Link> creator = new HashSet<Link>();
    // Start of user code attributeAnnotation:modified
    // End of user code
    private Date modified;
    // Start of user code attributeAnnotation:identifier
    // End of user code
    private String identifier;
    // Start of user code attributeAnnotation:launchCommand
    // End of user code
    private String launchCommand;
    // Start of user code attributeAnnotation:directoryPath
    // End of user code
    private String directoryPath;
    // Start of user code attributeAnnotation:buildCommand
    // End of user code
    private String buildCommand;
    
    // Start of user code classAttributes
    // End of user code
    // Start of user code classMethods
    // End of user code
    public SUT()
           throws URISyntaxException
    {
        super();
    
        // Start of user code constructor1
        // End of user code
    }
    
    public SUT(final URI about)
           throws URISyntaxException
    {
        super(about);
    
        // Start of user code constructor2
        // End of user code
    }
    
    /**
    * @deprecated Use the methods in class {@link verifit.compilation.VeriFitCompilationResourcesFactory} instead.
    */
    @Deprecated
    public SUT(final String serviceProviderId, final String sUTId)
           throws URISyntaxException
    {
        this (constructURI(serviceProviderId, sUTId));
        // Start of user code constructor3
        // End of user code
    }
    
    /**
    * @deprecated Use the methods in class {@link verifit.compilation.VeriFitCompilationResourcesFactory} instead.
    */
    @Deprecated
    public static URI constructURI(final String serviceProviderId, final String sUTId)
    {
        String basePath = OSLC4JUtils.getServletURI();
        Map<String, Object> pathParameters = new HashMap<String, Object>();
        pathParameters.put("serviceProviderId", serviceProviderId);
        pathParameters.put("sUTId", sUTId);
        String instanceURI = "serviceProviders/{serviceProviderId}/resources/sUTs/{sUTId}";
    
        final UriBuilder builder = UriBuilder.fromUri(basePath);
        return builder.path(instanceURI).buildFromMap(pathParameters);
    }
    
    /**
    * @deprecated Use the methods in class {@link verifit.compilation.VeriFitCompilationResourcesFactory} instead.
    */
    @Deprecated
    public static Link constructLink(final String serviceProviderId, final String sUTId , final String label)
    {
        return new Link(constructURI(serviceProviderId, sUTId), label);
    }
    
    /**
    * @deprecated Use the methods in class {@link verifit.compilation.VeriFitCompilationResourcesFactory} instead.
    */
    @Deprecated
    public static Link constructLink(final String serviceProviderId, final String sUTId)
    {
        return new Link(constructURI(serviceProviderId, sUTId));
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
    
    public void addCreator(final Link creator)
    {
        this.creator.add(creator);
    }
    
    
    // Start of user code getterAnnotation:title
    // End of user code
    @OslcName("title")
    @OslcPropertyDefinition(DctermsDomainConstants.DUBLIN_CORE_NAMSPACE + "title")
    @OslcDescription("Title of the resource represented as rich text in XHTML content. SHOULD include only content that is valid inside an XHTML <span> element.")
    @OslcOccurs(Occurs.ZeroOrOne)
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
    
    // Start of user code getterAnnotation:created
    // End of user code
    @OslcName("created")
    @OslcPropertyDefinition(DctermsDomainConstants.DUBLIN_CORE_NAMSPACE + "created")
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
    
    // Start of user code getterAnnotation:creator
    // End of user code
    @OslcName("creator")
    @OslcPropertyDefinition(DctermsDomainConstants.DUBLIN_CORE_NAMSPACE + "creator")
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
    
    // Start of user code getterAnnotation:modified
    // End of user code
    @OslcName("modified")
    @OslcPropertyDefinition(DctermsDomainConstants.DUBLIN_CORE_NAMSPACE + "modified")
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
    @OslcPropertyDefinition(DctermsDomainConstants.DUBLIN_CORE_NAMSPACE + "identifier")
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
    
    // Start of user code getterAnnotation:launchCommand
    // End of user code
    @OslcName("launchCommand")
    @OslcPropertyDefinition(FitDomainConstants.VERIFIT_NAMSPACE + "launchCommand")
    @OslcDescription("How to launch the SUT. The specified command will be launched from the root directory of the downloaded SUT. Examples: make run | ./run.sh | ./my_sut")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcValueType(ValueType.XMLLiteral)
    @OslcReadOnly(false)
    @OslcTitle("")
    public String getLaunchCommand()
    {
        // Start of user code getterInit:launchCommand
        // End of user code
        return launchCommand;
    }
    
    // Start of user code getterAnnotation:directoryPath
    // End of user code
    @OslcName("directoryPath")
    @OslcPropertyDefinition(FitDomainConstants.VERIFIT_NAMSPACE + "directoryPath")
    @OslcDescription("Path to the root directory of the SUT. Used to launch and compile the SUT.")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcValueType(ValueType.XMLLiteral)
    @OslcReadOnly(true)
    public String getDirectoryPath()
    {
        // Start of user code getterInit:directoryPath
        // End of user code
        return directoryPath;
    }
    
    // Start of user code getterAnnotation:buildCommand
    // End of user code
    @OslcName("buildCommand")
    @OslcPropertyDefinition(FitDomainConstants.VERIFIT_NAMSPACE + "buildCommand")
    @OslcDescription("How to build the SUT. The specified command will be launched from the root directory of the downloaded SUT. Examples: make | ./build.sh | gcc -g -o my_sut")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcValueType(ValueType.XMLLiteral)
    @OslcReadOnly(false)
    public String getBuildCommand()
    {
        // Start of user code getterInit:buildCommand
        // End of user code
        return buildCommand;
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
    
    // Start of user code setterAnnotation:directoryPath
    // End of user code
    public void setDirectoryPath(final String directoryPath )
    {
        // Start of user code setterInit:directoryPath
        // End of user code
        this.directoryPath = directoryPath;
    
        // Start of user code setterFinalize:directoryPath
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
    
    
    @Deprecated
    static public String titleToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:titleToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"title\">title: </LABEL>";
    
        // Start of user code "Mid:titleToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"title\" type=\"text\" style=\"width: 400px\" id=\"title\" >";
        // Start of user code "Finalize:titleToHtmlForCreation(...)"
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
    static public String createdToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:createdToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"created\">created: </LABEL>";
    
        // Start of user code "Mid:createdToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"created\" type=\"text\" style=\"width: 400px\" id=\"created\" >";
        // Start of user code "Finalize:createdToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    @Deprecated
    static public String creatorToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:creatorToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"creator\">creator: </LABEL>";
    
        // Start of user code "Mid:creatorToHtmlForCreation(...)"
        // End of user code
    
        // Start of user code "Finalize:creatorToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    @Deprecated
    static public String modifiedToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:modifiedToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"modified\">modified: </LABEL>";
    
        // Start of user code "Mid:modifiedToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"modified\" type=\"text\" style=\"width: 400px\" id=\"modified\" >";
        // Start of user code "Finalize:modifiedToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    @Deprecated
    static public String identifierToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:identifierToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"identifier\">identifier: </LABEL>";
    
        // Start of user code "Mid:identifierToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"identifier\" type=\"text\" style=\"width: 400px\" id=\"identifier\" >";
        // Start of user code "Finalize:identifierToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    @Deprecated
    static public String launchCommandToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:launchCommandToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"launchCommand\">launchCommand: </LABEL>";
    
        // Start of user code "Mid:launchCommandToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"launchCommand\" type=\"text\" style=\"width: 400px\" id=\"launchCommand\" >";
        // Start of user code "Finalize:launchCommandToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    @Deprecated
    static public String directoryPathToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:directoryPathToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"directoryPath\">directoryPath: </LABEL>";
    
        // Start of user code "Mid:directoryPathToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"directoryPath\" type=\"text\" style=\"width: 400px\" id=\"directoryPath\" >";
        // Start of user code "Finalize:directoryPathToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    @Deprecated
    static public String buildCommandToHtmlForCreation (final HttpServletRequest httpServletRequest)
    {
        String s = "";
    
        // Start of user code "Init:buildCommandToHtmlForCreation(...)"
        // End of user code
    
        s = s + "<label for=\"buildCommand\">buildCommand: </LABEL>";
    
        // Start of user code "Mid:buildCommandToHtmlForCreation(...)"
        // End of user code
    
        s= s + "<input name=\"buildCommand\" type=\"text\" style=\"width: 400px\" id=\"buildCommand\" >";
        // Start of user code "Finalize:buildCommandToHtmlForCreation(...)"
        // End of user code
    
        return s;
    }
    
    
    @Deprecated
    public String titleToHtml()
    {
        String s = "";
    
        // Start of user code titletoHtml_mid
        // End of user code
    
        try {
            if (title == null) {
                s = s + "<em>null</em>";
            }
            else {
                s = s + title.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code titletoHtml_finalize
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
    public String createdToHtml()
    {
        String s = "";
    
        // Start of user code createdtoHtml_mid
        // End of user code
    
        try {
            if (created == null) {
                s = s + "<em>null</em>";
            }
            else {
                s = s + created.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code createdtoHtml_finalize
        // End of user code
    
        return s;
    }
    
    @Deprecated
    public String creatorToHtml()
    {
        String s = "";
    
        // Start of user code creatortoHtml_mid
        // End of user code
    
        try {
            s = s + "<ul>";
            for(Link next : creator) {
                s = s + "<li>";
                s = s + (new Person (next.getValue())).toHtml(false);
                s = s + "</li>";
            }
            s = s + "</ul>";
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code creatortoHtml_finalize
        // End of user code
    
        return s;
    }
    
    @Deprecated
    public String modifiedToHtml()
    {
        String s = "";
    
        // Start of user code modifiedtoHtml_mid
        // End of user code
    
        try {
            if (modified == null) {
                s = s + "<em>null</em>";
            }
            else {
                s = s + modified.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code modifiedtoHtml_finalize
        // End of user code
    
        return s;
    }
    
    @Deprecated
    public String identifierToHtml()
    {
        String s = "";
    
        // Start of user code identifiertoHtml_mid
        // End of user code
    
        try {
            if (identifier == null) {
                s = s + "<em>null</em>";
            }
            else {
                s = s + identifier.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code identifiertoHtml_finalize
        // End of user code
    
        return s;
    }
    
    @Deprecated
    public String launchCommandToHtml()
    {
        String s = "";
    
        // Start of user code launchCommandtoHtml_mid
        // End of user code
    
        try {
            if (launchCommand == null) {
                s = s + "<em>null</em>";
            }
            else {
                s = s + launchCommand.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code launchCommandtoHtml_finalize
        // End of user code
    
        return s;
    }
    
    @Deprecated
    public String directoryPathToHtml()
    {
        String s = "";
    
        // Start of user code directoryPathtoHtml_mid
        // End of user code
    
        try {
            if (directoryPath == null) {
                s = s + "<em>null</em>";
            }
            else {
                s = s + directoryPath.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code directoryPathtoHtml_finalize
        // End of user code
    
        return s;
    }
    
    @Deprecated
    public String buildCommandToHtml()
    {
        String s = "";
    
        // Start of user code buildCommandtoHtml_mid
        // End of user code
    
        try {
            if (buildCommand == null) {
                s = s + "<em>null</em>";
            }
            else {
                s = s + buildCommand.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Start of user code buildCommandtoHtml_finalize
        // End of user code
    
        return s;
    }
    
    
}
