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
 */
// End of user code

package cz.vutbr.fit.group.verifit.oslc.compilation.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.SystemUtils;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.eclipse.lyo.oslc4j.provider.json4j.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.lyo.oslc4j.core.OSLC4JConstants;
import org.eclipse.lyo.oslc4j.core.OSLC4JUtils;
import org.eclipse.lyo.oslc4j.core.annotation.OslcCreationFactory;
import org.eclipse.lyo.oslc4j.core.annotation.OslcDialog;
import org.eclipse.lyo.oslc4j.core.annotation.OslcDialogs;
import org.eclipse.lyo.oslc4j.core.annotation.OslcQueryCapability;
import org.eclipse.lyo.oslc4j.core.annotation.OslcService;
import org.eclipse.lyo.oslc4j.core.model.Compact;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.eclipse.lyo.oslc4j.core.model.Preview;
import org.eclipse.lyo.oslc4j.core.model.ServiceProvider;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;

import cz.vutbr.fit.group.verifit.oslc.compilation.VeriFitCompilationManager;
import cz.vutbr.fit.group.verifit.oslc.compilation.VeriFitCompilationConstants;
import org.eclipse.lyo.oslc.domains.auto.Oslc_autoDomainConstants;
import org.eclipse.lyo.oslc.domains.auto.Oslc_autoDomainConstants;
import org.eclipse.lyo.oslc.domains.auto.Oslc_autoDomainConstants;
import org.eclipse.lyo.oslc.domains.auto.Oslc_autoDomainConstants;
import cz.vutbr.fit.group.verifit.oslc.domain.FitDomainConstants;
import cz.vutbr.fit.group.verifit.oslc.compilation.servlet.ServiceProviderCatalogSingleton;
import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc.domains.auto.Contribution;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc.domains.auto.ParameterInstance;
import org.eclipse.lyo.oslc.domains.Person;
import cz.vutbr.fit.group.verifit.oslc.domain.SUT;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

// Start of user code imports
import cz.vutbr.fit.group.verifit.oslc.shared.exceptions.OslcResourceException;
import org.eclipse.lyo.oslc4j.core.model.Error;
// End of user code

// Start of user code pre_class_code
// End of user code
@OslcService(Oslc_autoDomainConstants.AUTOMATION_DOMAIN)
@Path("resources")
@Api(value = "OSLC Service for {" + Oslc_autoDomainConstants.AUTOMATIONPLAN_LOCALNAME + ", " + Oslc_autoDomainConstants.AUTOMATIONREQUEST_LOCALNAME + ", " + Oslc_autoDomainConstants.AUTOMATIONRESULT_LOCALNAME + ", " + FitDomainConstants.SUT_LOCALNAME + "}")
public class ServiceProviderService1
{
    @Context private HttpServletRequest httpServletRequest;
    @Context private HttpServletResponse httpServletResponse;
    @Context private UriInfo uriInfo;

    private static final Logger log = LoggerFactory.getLogger(ServiceProviderService1.class);

    // Start of user code class_attributes
    // End of user code

    // Start of user code class_methods
    @GET
    @Path("getOS")
    @Produces({MediaType.TEXT_PLAIN})
    @ApiOperation(
        value = "GET the OS (windows or linux)'}",
        produces = MediaType.TEXT_PLAIN
    )
    public Response getOS()
    {
    	String os = SystemUtils.IS_OS_LINUX ? "linux" : "windows"; // TODO assumes that "not linux" means "windows"
		return Response.status(200).entity(os).build();
    }
    // End of user code

    public ServiceProviderService1()
    {
        super();
    }

    private void addCORSHeaders (final HttpServletResponse httpServletResponse) {
        //UI preview can be blocked by CORS policy.
        //add select CORS headers to every response that is embedded in an iframe.
        httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD");
        httpServletResponse.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");
    }

    @OslcQueryCapability
    (
        title = "QuerySUT",
        label = "QuerySUT",
        resourceShape = OslcConstants.PATH_RESOURCE_SHAPES + "/" + FitDomainConstants.SUT_PATH,
        resourceTypes = {FitDomainConstants.SUT_TYPE},
        usages = {}
    )
    @GET
    @Path("querySUT")
    @Produces({OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_JSON_LD, OslcMediaType.TEXT_TURTLE, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON})
    @ApiOperation(
        value = "Query capability for resources of type {" + FitDomainConstants.SUT_LOCALNAME + "}",
        notes = "Query capability for resources of type {" + "<a href=\"" + FitDomainConstants.SUT_TYPE + "\">" + FitDomainConstants.SUT_LOCALNAME + "</a>" + "}" +
            ", with respective resource shapes {" + "<a href=\"" + "../services/" + OslcConstants.PATH_RESOURCE_SHAPES + "/" + FitDomainConstants.SUT_PATH + "\">" + FitDomainConstants.SUT_LOCALNAME + "</a>" + "}",
        produces = OslcMediaType.APPLICATION_RDF_XML + ", " + OslcMediaType.APPLICATION_XML + ", " + OslcMediaType.APPLICATION_JSON + ", " + OslcMediaType.TEXT_TURTLE + ", " + MediaType.TEXT_HTML
    )
    public SUT[] querySUTs(
                                                    
                                                     @QueryParam("oslc.where") final String where,
                                                     @QueryParam("oslc.prefix") final String prefix,
                                                     @QueryParam("page") final String pageString,
                                                    @QueryParam("oslc.pageSize") final String pageSizeString) throws IOException, ServletException
    {
        int page=0;
        int pageSize=20;
        if (null != pageString) {
            page = Integer.parseInt(pageString);
        }
        if (null != pageSizeString) {
            pageSize = Integer.parseInt(pageSizeString);
        }

        // Start of user code querySUTs
        // Here additional logic can be implemented that complements main action taken in VeriFitCompilationManager
        // End of user code

        final List<SUT> resources = VeriFitCompilationManager.querySUTs(httpServletRequest, where, prefix, page, pageSize);
        httpServletRequest.setAttribute("queryUri",
                uriInfo.getAbsolutePath().toString() + "?oslc.paging=true");
        if (resources.size() > pageSize) {
            resources.remove(resources.size() - 1);
            httpServletRequest.setAttribute(OSLC4JConstants.OSLC4J_NEXT_PAGE,
                    uriInfo.getAbsolutePath().toString() + "?oslc.paging=true&oslc.pageSize=" + pageSize + "&page=" + (page + 1));
        }
        return resources.toArray(new SUT [resources.size()]);
    }

    @GET
    @Path("querySUT")
    @Produces({ MediaType.TEXT_HTML })
    @ApiOperation(
        value = "Query capability for resources of type {" + FitDomainConstants.SUT_LOCALNAME + "}",
        notes = "Query capability for resources of type {" + "<a href=\"" + FitDomainConstants.SUT_TYPE + "\">" + FitDomainConstants.SUT_LOCALNAME + "</a>" + "}" +
            ", with respective resource shapes {" + "<a href=\"" + "../services/" + OslcConstants.PATH_RESOURCE_SHAPES + "/" + FitDomainConstants.SUT_PATH + "\">" + FitDomainConstants.SUT_LOCALNAME + "</a>" + "}",
        produces = OslcMediaType.APPLICATION_RDF_XML + ", " + OslcMediaType.APPLICATION_XML + ", " + OslcMediaType.APPLICATION_JSON + ", " + OslcMediaType.TEXT_TURTLE + ", " + MediaType.TEXT_HTML
    )
    public void querySUTsAsHtml(
                                    
                                       @QueryParam("oslc.where") final String where,
                                       @QueryParam("oslc.prefix") final String prefix,
                                       @QueryParam("page") final String pageString,
                                    @QueryParam("oslc.pageSize") final String pageSizeString) throws ServletException, IOException
    {
        int page=0;
        int pageSize=20;
        if (null != pageString) {
            page = Integer.parseInt(pageString);
        }
        if (null != pageSizeString) {
            pageSize = Integer.parseInt(pageSizeString);
        }

        // Start of user code querySUTsAsHtml
        // End of user code

        final List<SUT> resources = VeriFitCompilationManager.querySUTs(httpServletRequest, where, prefix, page, pageSize);

        if (resources!= null) {
            httpServletRequest.setAttribute("resources", resources);
            // Start of user code querySUTsAsHtml_setAttributes
            // End of user code

            httpServletRequest.setAttribute("queryUri",
                    uriInfo.getAbsolutePath().toString() + "?oslc.paging=true");
            if (resources.size() > pageSize) {
                resources.remove(resources.size() - 1);
                httpServletRequest.setAttribute(OSLC4JConstants.OSLC4J_NEXT_PAGE,
                        uriInfo.getAbsolutePath().toString() + "?oslc.paging=true&oslc.pageSize=" + pageSize + "&page=" + (page + 1));
            }
            RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/sutscollection.jsp");
            rd.forward(httpServletRequest,httpServletResponse);
            return;
        }

        throw new WebApplicationException(Status.NOT_FOUND);
    }

    @OslcQueryCapability
    (
        title = "QueryAutomationResult",
        label = "QueryAutomationResult",
        resourceShape = OslcConstants.PATH_RESOURCE_SHAPES + "/" + Oslc_autoDomainConstants.AUTOMATIONRESULT_PATH,
        resourceTypes = {Oslc_autoDomainConstants.AUTOMATIONRESULT_TYPE},
        usages = {}
    )
    @GET
    @Path("queryAutomationResult")
    @Produces({OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_JSON_LD, OslcMediaType.TEXT_TURTLE, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON})
    @ApiOperation(
        value = "Query capability for resources of type {" + Oslc_autoDomainConstants.AUTOMATIONRESULT_LOCALNAME + "}",
        notes = "Query capability for resources of type {" + "<a href=\"" + Oslc_autoDomainConstants.AUTOMATIONRESULT_TYPE + "\">" + Oslc_autoDomainConstants.AUTOMATIONRESULT_LOCALNAME + "</a>" + "}" +
            ", with respective resource shapes {" + "<a href=\"" + "../services/" + OslcConstants.PATH_RESOURCE_SHAPES + "/" + Oslc_autoDomainConstants.AUTOMATIONRESULT_PATH + "\">" + Oslc_autoDomainConstants.AUTOMATIONRESULT_LOCALNAME + "</a>" + "}",
        produces = OslcMediaType.APPLICATION_RDF_XML + ", " + OslcMediaType.APPLICATION_XML + ", " + OslcMediaType.APPLICATION_JSON + ", " + OslcMediaType.TEXT_TURTLE + ", " + MediaType.TEXT_HTML
    )
    public AutomationResult[] queryAutomationResults(
                                                    
                                                     @QueryParam("oslc.where") final String where,
                                                     @QueryParam("oslc.prefix") final String prefix,
                                                     @QueryParam("page") final String pageString,
                                                    @QueryParam("oslc.pageSize") final String pageSizeString) throws IOException, ServletException
    {
        int page=0;
        int pageSize=20;
        if (null != pageString) {
            page = Integer.parseInt(pageString);
        }
        if (null != pageSizeString) {
            pageSize = Integer.parseInt(pageSizeString);
        }

        // Start of user code queryAutomationResults
        // Here additional logic can be implemented that complements main action taken in VeriFitCompilationManager
        // End of user code

        final List<AutomationResult> resources = VeriFitCompilationManager.queryAutomationResults(httpServletRequest, where, prefix, page, pageSize);
        httpServletRequest.setAttribute("queryUri",
                uriInfo.getAbsolutePath().toString() + "?oslc.paging=true");
        if (resources.size() > pageSize) {
            resources.remove(resources.size() - 1);
            httpServletRequest.setAttribute(OSLC4JConstants.OSLC4J_NEXT_PAGE,
                    uriInfo.getAbsolutePath().toString() + "?oslc.paging=true&oslc.pageSize=" + pageSize + "&page=" + (page + 1));
        }
        return resources.toArray(new AutomationResult [resources.size()]);
    }

    @GET
    @Path("queryAutomationResult")
    @Produces({ MediaType.TEXT_HTML })
    @ApiOperation(
        value = "Query capability for resources of type {" + Oslc_autoDomainConstants.AUTOMATIONRESULT_LOCALNAME + "}",
        notes = "Query capability for resources of type {" + "<a href=\"" + Oslc_autoDomainConstants.AUTOMATIONRESULT_TYPE + "\">" + Oslc_autoDomainConstants.AUTOMATIONRESULT_LOCALNAME + "</a>" + "}" +
            ", with respective resource shapes {" + "<a href=\"" + "../services/" + OslcConstants.PATH_RESOURCE_SHAPES + "/" + Oslc_autoDomainConstants.AUTOMATIONRESULT_PATH + "\">" + Oslc_autoDomainConstants.AUTOMATIONRESULT_LOCALNAME + "</a>" + "}",
        produces = OslcMediaType.APPLICATION_RDF_XML + ", " + OslcMediaType.APPLICATION_XML + ", " + OslcMediaType.APPLICATION_JSON + ", " + OslcMediaType.TEXT_TURTLE + ", " + MediaType.TEXT_HTML
    )
    public void queryAutomationResultsAsHtml(
                                    
                                       @QueryParam("oslc.where") final String where,
                                       @QueryParam("oslc.prefix") final String prefix,
                                       @QueryParam("page") final String pageString,
                                    @QueryParam("oslc.pageSize") final String pageSizeString) throws ServletException, IOException
    {
        int page=0;
        int pageSize=20;
        if (null != pageString) {
            page = Integer.parseInt(pageString);
        }
        if (null != pageSizeString) {
            pageSize = Integer.parseInt(pageSizeString);
        }

        // Start of user code queryAutomationResultsAsHtml
        // End of user code

        final List<AutomationResult> resources = VeriFitCompilationManager.queryAutomationResults(httpServletRequest, where, prefix, page, pageSize);

        if (resources!= null) {
            httpServletRequest.setAttribute("resources", resources);
            // Start of user code queryAutomationResultsAsHtml_setAttributes
            // End of user code

            httpServletRequest.setAttribute("queryUri",
                    uriInfo.getAbsolutePath().toString() + "?oslc.paging=true");
            if (resources.size() > pageSize) {
                resources.remove(resources.size() - 1);
                httpServletRequest.setAttribute(OSLC4JConstants.OSLC4J_NEXT_PAGE,
                        uriInfo.getAbsolutePath().toString() + "?oslc.paging=true&oslc.pageSize=" + pageSize + "&page=" + (page + 1));
            }
            RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/automationresultscollection.jsp");
            rd.forward(httpServletRequest,httpServletResponse);
            return;
        }

        throw new WebApplicationException(Status.NOT_FOUND);
    }

    @OslcQueryCapability
    (
        title = "QueryAutomationPlan",
        label = "QueryAutomationPlan",
        resourceShape = OslcConstants.PATH_RESOURCE_SHAPES + "/" + Oslc_autoDomainConstants.AUTOMATIONPLAN_PATH,
        resourceTypes = {Oslc_autoDomainConstants.AUTOMATIONPLAN_TYPE},
        usages = {}
    )
    @GET
    @Path("queryAutomationPlan")
    @Produces({OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_JSON_LD, OslcMediaType.TEXT_TURTLE, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON})
    @ApiOperation(
        value = "Query capability for resources of type {" + Oslc_autoDomainConstants.AUTOMATIONPLAN_LOCALNAME + "}",
        notes = "Query capability for resources of type {" + "<a href=\"" + Oslc_autoDomainConstants.AUTOMATIONPLAN_TYPE + "\">" + Oslc_autoDomainConstants.AUTOMATIONPLAN_LOCALNAME + "</a>" + "}" +
            ", with respective resource shapes {" + "<a href=\"" + "../services/" + OslcConstants.PATH_RESOURCE_SHAPES + "/" + Oslc_autoDomainConstants.AUTOMATIONPLAN_PATH + "\">" + Oslc_autoDomainConstants.AUTOMATIONPLAN_LOCALNAME + "</a>" + "}",
        produces = OslcMediaType.APPLICATION_RDF_XML + ", " + OslcMediaType.APPLICATION_XML + ", " + OslcMediaType.APPLICATION_JSON + ", " + OslcMediaType.TEXT_TURTLE + ", " + MediaType.TEXT_HTML
    )
    public AutomationPlan[] queryAutomationPlans(
                                                    
                                                     @QueryParam("oslc.where") final String where,
                                                     @QueryParam("oslc.prefix") final String prefix,
                                                     @QueryParam("page") final String pageString,
                                                    @QueryParam("oslc.pageSize") final String pageSizeString) throws IOException, ServletException
    {
        int page=0;
        int pageSize=20;
        if (null != pageString) {
            page = Integer.parseInt(pageString);
        }
        if (null != pageSizeString) {
            pageSize = Integer.parseInt(pageSizeString);
        }

        // Start of user code queryAutomationPlans
        // Here additional logic can be implemented that complements main action taken in VeriFitCompilationManager
        // End of user code

        final List<AutomationPlan> resources = VeriFitCompilationManager.queryAutomationPlans(httpServletRequest, where, prefix, page, pageSize);
        httpServletRequest.setAttribute("queryUri",
                uriInfo.getAbsolutePath().toString() + "?oslc.paging=true");
        if (resources.size() > pageSize) {
            resources.remove(resources.size() - 1);
            httpServletRequest.setAttribute(OSLC4JConstants.OSLC4J_NEXT_PAGE,
                    uriInfo.getAbsolutePath().toString() + "?oslc.paging=true&oslc.pageSize=" + pageSize + "&page=" + (page + 1));
        }
        return resources.toArray(new AutomationPlan [resources.size()]);
    }

    @GET
    @Path("queryAutomationPlan")
    @Produces({ MediaType.TEXT_HTML })
    @ApiOperation(
        value = "Query capability for resources of type {" + Oslc_autoDomainConstants.AUTOMATIONPLAN_LOCALNAME + "}",
        notes = "Query capability for resources of type {" + "<a href=\"" + Oslc_autoDomainConstants.AUTOMATIONPLAN_TYPE + "\">" + Oslc_autoDomainConstants.AUTOMATIONPLAN_LOCALNAME + "</a>" + "}" +
            ", with respective resource shapes {" + "<a href=\"" + "../services/" + OslcConstants.PATH_RESOURCE_SHAPES + "/" + Oslc_autoDomainConstants.AUTOMATIONPLAN_PATH + "\">" + Oslc_autoDomainConstants.AUTOMATIONPLAN_LOCALNAME + "</a>" + "}",
        produces = OslcMediaType.APPLICATION_RDF_XML + ", " + OslcMediaType.APPLICATION_XML + ", " + OslcMediaType.APPLICATION_JSON + ", " + OslcMediaType.TEXT_TURTLE + ", " + MediaType.TEXT_HTML
    )
    public void queryAutomationPlansAsHtml(
                                    
                                       @QueryParam("oslc.where") final String where,
                                       @QueryParam("oslc.prefix") final String prefix,
                                       @QueryParam("page") final String pageString,
                                    @QueryParam("oslc.pageSize") final String pageSizeString) throws ServletException, IOException
    {
        int page=0;
        int pageSize=20;
        if (null != pageString) {
            page = Integer.parseInt(pageString);
        }
        if (null != pageSizeString) {
            pageSize = Integer.parseInt(pageSizeString);
        }

        // Start of user code queryAutomationPlansAsHtml
        // End of user code

        final List<AutomationPlan> resources = VeriFitCompilationManager.queryAutomationPlans(httpServletRequest, where, prefix, page, pageSize);

        if (resources!= null) {
            httpServletRequest.setAttribute("resources", resources);
            // Start of user code queryAutomationPlansAsHtml_setAttributes
            // End of user code

            httpServletRequest.setAttribute("queryUri",
                    uriInfo.getAbsolutePath().toString() + "?oslc.paging=true");
            if (resources.size() > pageSize) {
                resources.remove(resources.size() - 1);
                httpServletRequest.setAttribute(OSLC4JConstants.OSLC4J_NEXT_PAGE,
                        uriInfo.getAbsolutePath().toString() + "?oslc.paging=true&oslc.pageSize=" + pageSize + "&page=" + (page + 1));
            }
            RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/automationplanscollection.jsp");
            rd.forward(httpServletRequest,httpServletResponse);
            return;
        }

        throw new WebApplicationException(Status.NOT_FOUND);
    }

    @OslcQueryCapability
    (
        title = "QueryAutomationRequest",
        label = "QueryAutomationRequest",
        resourceShape = OslcConstants.PATH_RESOURCE_SHAPES + "/" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_PATH,
        resourceTypes = {Oslc_autoDomainConstants.AUTOMATIONREQUEST_TYPE},
        usages = {}
    )
    @GET
    @Path("queryAutomationRequest")
    @Produces({OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_JSON_LD, OslcMediaType.TEXT_TURTLE, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON})
    @ApiOperation(
        value = "Query capability for resources of type {" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_LOCALNAME + "}",
        notes = "Query capability for resources of type {" + "<a href=\"" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_TYPE + "\">" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_LOCALNAME + "</a>" + "}" +
            ", with respective resource shapes {" + "<a href=\"" + "../services/" + OslcConstants.PATH_RESOURCE_SHAPES + "/" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_PATH + "\">" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_LOCALNAME + "</a>" + "}",
        produces = OslcMediaType.APPLICATION_RDF_XML + ", " + OslcMediaType.APPLICATION_XML + ", " + OslcMediaType.APPLICATION_JSON + ", " + OslcMediaType.TEXT_TURTLE + ", " + MediaType.TEXT_HTML
    )
    public AutomationRequest[] queryAutomationRequests(
                                                    
                                                     @QueryParam("oslc.where") final String where,
                                                     @QueryParam("oslc.prefix") final String prefix,
                                                     @QueryParam("page") final String pageString,
                                                    @QueryParam("oslc.pageSize") final String pageSizeString) throws IOException, ServletException
    {
        int page=0;
        int pageSize=20;
        if (null != pageString) {
            page = Integer.parseInt(pageString);
        }
        if (null != pageSizeString) {
            pageSize = Integer.parseInt(pageSizeString);
        }

        // Start of user code queryAutomationRequests
        // Here additional logic can be implemented that complements main action taken in VeriFitCompilationManager
        // End of user code

        final List<AutomationRequest> resources = VeriFitCompilationManager.queryAutomationRequests(httpServletRequest, where, prefix, page, pageSize);
        httpServletRequest.setAttribute("queryUri",
                uriInfo.getAbsolutePath().toString() + "?oslc.paging=true");
        if (resources.size() > pageSize) {
            resources.remove(resources.size() - 1);
            httpServletRequest.setAttribute(OSLC4JConstants.OSLC4J_NEXT_PAGE,
                    uriInfo.getAbsolutePath().toString() + "?oslc.paging=true&oslc.pageSize=" + pageSize + "&page=" + (page + 1));
        }
        return resources.toArray(new AutomationRequest [resources.size()]);
    }

    @GET
    @Path("queryAutomationRequest")
    @Produces({ MediaType.TEXT_HTML })
    @ApiOperation(
        value = "Query capability for resources of type {" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_LOCALNAME + "}",
        notes = "Query capability for resources of type {" + "<a href=\"" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_TYPE + "\">" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_LOCALNAME + "</a>" + "}" +
            ", with respective resource shapes {" + "<a href=\"" + "../services/" + OslcConstants.PATH_RESOURCE_SHAPES + "/" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_PATH + "\">" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_LOCALNAME + "</a>" + "}",
        produces = OslcMediaType.APPLICATION_RDF_XML + ", " + OslcMediaType.APPLICATION_XML + ", " + OslcMediaType.APPLICATION_JSON + ", " + OslcMediaType.TEXT_TURTLE + ", " + MediaType.TEXT_HTML
    )
    public void queryAutomationRequestsAsHtml(
                                    
                                       @QueryParam("oslc.where") final String where,
                                       @QueryParam("oslc.prefix") final String prefix,
                                       @QueryParam("page") final String pageString,
                                    @QueryParam("oslc.pageSize") final String pageSizeString) throws ServletException, IOException
    {
        int page=0;
        int pageSize=20;
        if (null != pageString) {
            page = Integer.parseInt(pageString);
        }
        if (null != pageSizeString) {
            pageSize = Integer.parseInt(pageSizeString);
        }

        // Start of user code queryAutomationRequestsAsHtml
        // End of user code

        final List<AutomationRequest> resources = VeriFitCompilationManager.queryAutomationRequests(httpServletRequest, where, prefix, page, pageSize);

        if (resources!= null) {
            httpServletRequest.setAttribute("resources", resources);
            // Start of user code queryAutomationRequestsAsHtml_setAttributes
            // End of user code

            httpServletRequest.setAttribute("queryUri",
                    uriInfo.getAbsolutePath().toString() + "?oslc.paging=true");
            if (resources.size() > pageSize) {
                resources.remove(resources.size() - 1);
                httpServletRequest.setAttribute(OSLC4JConstants.OSLC4J_NEXT_PAGE,
                        uriInfo.getAbsolutePath().toString() + "?oslc.paging=true&oslc.pageSize=" + pageSize + "&page=" + (page + 1));
            }
            RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/automationrequestscollection.jsp");
            rd.forward(httpServletRequest,httpServletResponse);
            return;
        }

        throw new WebApplicationException(Status.NOT_FOUND);
    }

    @OslcDialog
    (
         title = "SelectorSUT",
         label = "SelectorSUT",
         uri = "resources/selectorSUT",
         hintWidth = "250px",
         hintHeight = "250px",
         resourceTypes = {FitDomainConstants.SUT_TYPE},
         usages = {}
    )
    @GET
    @Path("selectorSUT")
    @Consumes({ MediaType.TEXT_HTML, MediaType.WILDCARD })
    public void SUTSelector(
        @QueryParam("terms") final String terms
        
        ) throws ServletException, IOException
    {
        // Start of user code SUTSelector_init
        // End of user code

        httpServletRequest.setAttribute("selectionUri",UriBuilder.fromUri(OSLC4JUtils.getServletURI()).path(uriInfo.getPath()).build().toString());
        // Start of user code SUTSelector_setAttributes
        // End of user code

        if (terms != null ) {
            httpServletRequest.setAttribute("terms", terms);
            final List<SUT> resources = VeriFitCompilationManager.SUTSelector(httpServletRequest, terms);
            if (resources!= null) {
                        httpServletRequest.setAttribute("resources", resources);
                        RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/sutselectorresults.jsp");
                        rd.forward(httpServletRequest, httpServletResponse);
                        return;
            }
            log.error("A empty search should return an empty list and not NULL!");
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);

        } else {
            RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/sutselector.jsp");
            rd.forward(httpServletRequest, httpServletResponse);
            return;
        }
    }

    @OslcDialog
    (
         title = "SelectorAutomationResult",
         label = "SelectorAutomationResult",
         uri = "resources/selectorAutomationResult",
         hintWidth = "250px",
         hintHeight = "250px",
         resourceTypes = {Oslc_autoDomainConstants.AUTOMATIONRESULT_TYPE},
         usages = {}
    )
    @GET
    @Path("selectorAutomationResult")
    @Consumes({ MediaType.TEXT_HTML, MediaType.WILDCARD })
    public void AutomationResultSelector(
        @QueryParam("terms") final String terms
        
        ) throws ServletException, IOException
    {
        // Start of user code AutomationResultSelector_init
        // End of user code

        httpServletRequest.setAttribute("selectionUri",UriBuilder.fromUri(OSLC4JUtils.getServletURI()).path(uriInfo.getPath()).build().toString());
        // Start of user code AutomationResultSelector_setAttributes
        // End of user code

        if (terms != null ) {
            httpServletRequest.setAttribute("terms", terms);
            final List<AutomationResult> resources = VeriFitCompilationManager.AutomationResultSelector(httpServletRequest, terms);
            if (resources!= null) {
                        httpServletRequest.setAttribute("resources", resources);
                        RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/automationresultselectorresults.jsp");
                        rd.forward(httpServletRequest, httpServletResponse);
                        return;
            }
            log.error("A empty search should return an empty list and not NULL!");
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);

        } else {
            RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/automationresultselector.jsp");
            rd.forward(httpServletRequest, httpServletResponse);
            return;
        }
    }

    @OslcDialog
    (
         title = "SelectorAutomationPlan",
         label = "SelectorAutomationPlan",
         uri = "resources/selectorAutomationPlan",
         hintWidth = "250px",
         hintHeight = "250px",
         resourceTypes = {Oslc_autoDomainConstants.AUTOMATIONPLAN_TYPE},
         usages = {}
    )
    @GET
    @Path("selectorAutomationPlan")
    @Consumes({ MediaType.TEXT_HTML, MediaType.WILDCARD })
    public void AutomationPlanSelector(
        @QueryParam("terms") final String terms
        
        ) throws ServletException, IOException
    {
        // Start of user code AutomationPlanSelector_init
        // End of user code

        httpServletRequest.setAttribute("selectionUri",UriBuilder.fromUri(OSLC4JUtils.getServletURI()).path(uriInfo.getPath()).build().toString());
        // Start of user code AutomationPlanSelector_setAttributes
        // End of user code

        if (terms != null ) {
            httpServletRequest.setAttribute("terms", terms);
            final List<AutomationPlan> resources = VeriFitCompilationManager.AutomationPlanSelector(httpServletRequest, terms);
            if (resources!= null) {
                        httpServletRequest.setAttribute("resources", resources);
                        RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/automationplanselectorresults.jsp");
                        rd.forward(httpServletRequest, httpServletResponse);
                        return;
            }
            log.error("A empty search should return an empty list and not NULL!");
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);

        } else {
            RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/automationplanselector.jsp");
            rd.forward(httpServletRequest, httpServletResponse);
            return;
        }
    }

    @OslcDialog
    (
         title = "SelectorAutomationRequest",
         label = "SelectorAutomationRequest",
         uri = "resources/selectorAutomationRequest",
         hintWidth = "250px",
         hintHeight = "250px",
         resourceTypes = {Oslc_autoDomainConstants.AUTOMATIONREQUEST_TYPE},
         usages = {}
    )
    @GET
    @Path("selectorAutomationRequest")
    @Consumes({ MediaType.TEXT_HTML, MediaType.WILDCARD })
    public void AutomationRequestSelector(
        @QueryParam("terms") final String terms
        
        ) throws ServletException, IOException
    {
        // Start of user code AutomationRequestSelector_init
        // End of user code

        httpServletRequest.setAttribute("selectionUri",UriBuilder.fromUri(OSLC4JUtils.getServletURI()).path(uriInfo.getPath()).build().toString());
        // Start of user code AutomationRequestSelector_setAttributes
        // End of user code

        if (terms != null ) {
            httpServletRequest.setAttribute("terms", terms);
            final List<AutomationRequest> resources = VeriFitCompilationManager.AutomationRequestSelector(httpServletRequest, terms);
            if (resources!= null) {
                        httpServletRequest.setAttribute("resources", resources);
                        RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/automationrequestselectorresults.jsp");
                        rd.forward(httpServletRequest, httpServletResponse);
                        return;
            }
            log.error("A empty search should return an empty list and not NULL!");
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);

        } else {
            RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/automationrequestselector.jsp");
            rd.forward(httpServletRequest, httpServletResponse);
            return;
        }
    }

    /**
     * Create a single AutomationRequest via RDF/XML, XML or JSON POST
     *
     * @throws IOException
     * @throws ServletException
     */
    @OslcCreationFactory
    (
         title = "CreateAutomationRequest",
         label = "CreateAutomationRequest",
         resourceShapes = {OslcConstants.PATH_RESOURCE_SHAPES + "/" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_PATH},
         resourceTypes = {Oslc_autoDomainConstants.AUTOMATIONREQUEST_TYPE},
         usages = {}
    )
    @POST
    @Path("createAutomationRequest")
    @Consumes({OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_JSON_LD, OslcMediaType.TEXT_TURTLE, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
    @Produces({OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_JSON_LD, OslcMediaType.TEXT_TURTLE, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON})
    @ApiOperation(
        value = "Creation factory for resources of type {" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_LOCALNAME + "}",
        notes = "Creation factory for resources of type {" + "<a href=\"" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_TYPE + "\">" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_LOCALNAME + "</a>" + "}" +
            ", with respective resource shapes {" + "<a href=\"" + "../services/" + OslcConstants.PATH_RESOURCE_SHAPES + "/" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_PATH + "\">" + Oslc_autoDomainConstants.AUTOMATIONREQUEST_LOCALNAME + "</a>" + "}",
        produces = OslcMediaType.APPLICATION_RDF_XML + ", " + OslcMediaType.APPLICATION_XML + ", " + OslcMediaType.APPLICATION_JSON + ", " + OslcMediaType.TEXT_TURTLE
    )
    public Response createAutomationRequest(
            
            final AutomationRequest aResource
        ) throws IOException, ServletException
    {
        try {
		AutomationRequest newResource = VeriFitCompilationManager.createAutomationRequest(httpServletRequest, aResource);
        httpServletResponse.setHeader("ETag", VeriFitCompilationManager.getETagFromAutomationRequest(newResource));
        return Response.created(newResource.getAbout()).entity(newResource).header(VeriFitCompilationConstants.HDR_OSLC_VERSION, VeriFitCompilationConstants.OSLC_VERSION_V2).build();
		} catch (OslcResourceException e) {
		Error errorResource = new Error();
			errorResource.setStatusCode("400");
			errorResource.setMessage(e.getMessage());
			return Response.status(400).entity(errorResource).build();
		}
    }

    /**
     * OSLC delegated creation dialog for a single resource
     *
     * @throws IOException
     * @throws ServletException
     */
    @GET
    @Path("creatorAutomationRequest")
    @Consumes({MediaType.WILDCARD})
    public void AutomationRequestCreator(
                
        ) throws IOException, ServletException
    {
        // Start of user code AutomationRequestCreator
        // End of user code

        httpServletRequest.setAttribute("creatorUri", UriBuilder.fromUri(OSLC4JUtils.getServletURI()).path(uriInfo.getPath()).build().toString());

        RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/cz/vutbr/fit/group/verifit/oslc/compilation/automationrequestcreator.jsp");
        rd.forward(httpServletRequest, httpServletResponse);
    }

    /**
     * Backend creator for the OSLC delegated creation dialog.
     *
     * Accepts the input in FormParams and returns a small JSON response
     */
    @OslcDialog
    (
         title = "CreatorAutomationRequest",
         label = "CreatorAutomationRequest",
         uri = "resources/creatorAutomationRequest",
         hintWidth = "250px",
         hintHeight = "250px",
         resourceTypes = {Oslc_autoDomainConstants.AUTOMATIONREQUEST_TYPE},
         usages = {}
    )
    @POST
    @Path("creatorAutomationRequest")
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED})
    public void createAutomationRequestFromDialog(MultivaluedMap<String, String> formParams
            
        ) throws URISyntaxException, ParseException {
        AutomationRequest newResource = null;

        AutomationRequest aResource = new AutomationRequest();

        List<String> paramValues;

        paramValues = formParams.get("contributor");
        if (paramValues != null) {
                for(int i=0; i<paramValues.size(); i++) {
                    aResource.addContributor(new Link(new URI(paramValues.get(i))));
                }
        }
        paramValues = formParams.get("created");
        if (paramValues != null) {
                if (paramValues.size() == 1) {
                    if (paramValues.get(0).length() != 0)
                        aResource.setCreated(new SimpleDateFormat("M/D/y").parse(paramValues.get(0)));
                    // else, there is an empty value for that parameter, and hence ignore since the parameter is not actually set.
                }

        }
        paramValues = formParams.get("creator");
        if (paramValues != null) {
                for(int i=0; i<paramValues.size(); i++) {
                    aResource.addCreator(new Link(new URI(paramValues.get(i))));
                }
        }
        paramValues = formParams.get("description");
        if (paramValues != null) {
                if (paramValues.size() == 1) {
                    if (paramValues.get(0).length() != 0)
                        aResource.setDescription(paramValues.get(0));
                    // else, there is an empty value for that parameter, and hence ignore since the parameter is not actually set.
                }

        }
        paramValues = formParams.get("identifier");
        if (paramValues != null) {
                if (paramValues.size() == 1) {
                    if (paramValues.get(0).length() != 0)
                        aResource.setIdentifier(paramValues.get(0));
                    // else, there is an empty value for that parameter, and hence ignore since the parameter is not actually set.
                }

        }
        paramValues = formParams.get("modified");
        if (paramValues != null) {
                if (paramValues.size() == 1) {
                    if (paramValues.get(0).length() != 0)
                        aResource.setModified(new SimpleDateFormat("M/D/y").parse(paramValues.get(0)));
                    // else, there is an empty value for that parameter, and hence ignore since the parameter is not actually set.
                }

        }
        paramValues = formParams.get("type");
        if (paramValues != null) {
                for(int i=0; i<paramValues.size(); i++) {
                    aResource.addType(new Link(new URI(paramValues.get(i))));
                }
        }
        paramValues = formParams.get("title");
        if (paramValues != null) {
                if (paramValues.size() == 1) {
                    if (paramValues.get(0).length() != 0)
                        aResource.setTitle(paramValues.get(0));
                    // else, there is an empty value for that parameter, and hence ignore since the parameter is not actually set.
                }

        }
        paramValues = formParams.get("instanceShape");
        if (paramValues != null) {
                for(int i=0; i<paramValues.size(); i++) {
                    aResource.addInstanceShape(new Link(new URI(paramValues.get(i))));
                }
        }
        paramValues = formParams.get("serviceProvider");
        if (paramValues != null) {
                for(int i=0; i<paramValues.size(); i++) {
                    aResource.addServiceProvider(new Link(new URI(paramValues.get(i))));
                }
        }
        paramValues = formParams.get("state");
        if (paramValues != null) {
                for(int i=0; i<paramValues.size(); i++) {
                    aResource.addState(new Link(new URI(paramValues.get(i))));
                }
        }
        paramValues = formParams.get("desiredState");
        if (paramValues != null) {
                if (paramValues.size() == 1) {
                    if (paramValues.get(0).length() != 0)
                        aResource.setDesiredState(new Link(new URI(paramValues.get(0))));
                    // else, there is an empty value for that parameter, and hence ignore since the parameter is not actually set.
                }

        }
        paramValues = formParams.get("inputParameter");
        if (paramValues != null) {
                for(int i=0; i<paramValues.size(); i++) {
                    aResource.addInputParameter(new ParameterInstance(new URI(paramValues.get(i))));
                }
        }
        paramValues = formParams.get("executesAutomationPlan");
        if (paramValues != null) {
                if (paramValues.size() == 1) {
                    if (paramValues.get(0).length() != 0)
                        aResource.setExecutesAutomationPlan(new Link(new URI(paramValues.get(0))));
                    // else, there is an empty value for that parameter, and hence ignore since the parameter is not actually set.
                }

        }
        paramValues = formParams.get("producedAutomationResult");
        if (paramValues != null) {
                if (paramValues.size() == 1) {
                    if (paramValues.get(0).length() != 0)
                        aResource.setProducedAutomationResult(new Link(new URI(paramValues.get(0))));
                    // else, there is an empty value for that parameter, and hence ignore since the parameter is not actually set.
                }

        }

        newResource = VeriFitCompilationManager.createAutomationRequestFromDialog(httpServletRequest, aResource);

        if (newResource != null) {
            httpServletRequest.setAttribute("newResource", newResource);
            httpServletRequest.setAttribute("newResourceUri", newResource.getAbout().toString());

            // Send back to the form a small JSON response
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setStatus(Status.CREATED.getStatusCode());
            httpServletResponse.addHeader("Location", newResource.getAbout().toString());
            try {
                PrintWriter out = httpServletResponse.getWriter();
    
                JSONObject oslcResponse = new JSONObject();
                JSONObject newResourceJson = new JSONObject();
                newResourceJson.put("rdf:resource", newResource.getAbout().toString());
                // Start of user code OSLC Resource Label
                newResourceJson.put("oslc:label", newResource.toString());
                // End of user code
                oslcResponse.put("oslc:results", new Object[]{newResourceJson});
    
                out.print(oslcResponse.toString());
                out.close();
            } catch (IOException | JSONException e) {
                throw new WebApplicationException(e);
            }
        }
    }
}
