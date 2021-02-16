<%--To avoid the overriding of any manual code changes upon subsequent code generations, disable "Generate JSP Files" option in the Adaptor model.--%>
<!DOCTYPE html>
<%--
 Copyright (c) 2020 Contributors to the Eclipse Foundation
 
 See the NOTICE file(s) distributed with this work for additional
 information regarding copyright ownership.
 
 This program and the accompanying materials are made available under the
 terms of the Eclipse Distribution License 1.0 which is available at
 http://www.eclipse.org/org/documents/edl-v10.php.
 
 SPDX-License-Identifier: BSD-3-Simple

 This file is generated by Lyo Designer (https://www.eclipse.org/lyo/)
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@page import="org.eclipse.lyo.oslc4j.core.model.Link" %>
<%@page import="org.eclipse.lyo.oslc4j.core.model.ServiceProvider"%>
<%@page import="org.eclipse.lyo.oslc4j.core.model.OslcConstants"%>
<%@page import="org.eclipse.lyo.oslc4j.core.OSLC4JUtils"%>
<%@page import="org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition"%>
<%@page import="org.eclipse.lyo.oslc4j.core.annotation.OslcName"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="java.net.URI"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="javax.xml.namespace.QName"%>
<%@page import="javax.ws.rs.core.UriBuilder"%>

<%@page import="org.eclipse.lyo.oslc.domains.auto.AutomationResult"%>
<%@page import="org.eclipse.lyo.oslc.domains.auto.Oslc_autoDomainConstants"%>

<%@ page contentType="text/html" language="java" pageEncoding="UTF-8" %>

<%
  AutomationResult aAutomationResult = (AutomationResult) request.getAttribute("aAutomationResult");
%>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <title><%= aAutomationResult.toString() %></title>

  <link href="<c:url value="/static/css/bootstrap-4.0.0-beta.min.css"/>" rel="stylesheet">
  <link href="<c:url value="/static/css/adaptor.css"/>" rel="stylesheet">

  <script src="<c:url value="/static/js/jquery-3.2.1.min.js"/>"></script>
  <script src="<c:url value="/static/js/popper-1.11.0.min.js"/>"></script>
  <script src="<c:url value="/static/js/bootstrap-4.0.0-beta.min.js"/>"></script>
  <script src="<c:url value="/static/js/ui-preview-helper.js"/>"></script>
  <script type="text/javascript">
    $(function () {setupUiPreviewOnPopover($("a.oslc-resource-link"));});
  </script>
</head>

<body>
<!-- Fixed navbar -->
  <nav class="navbar navbar-expand-lg sticky-top navbar-light bg-light">
    <div class="container">
      <ul class="navbar-nav mr-auto">
        <li class="nav-item"><a class="nav-link" href="<c:url value="/"/>"><%= application.getServletContextName() %></a></li>
        <li class="nav-item"><a class="nav-link" href="<c:url value="/services/catalog/singleton"/>">Service Provider Catalog</a></li>
        <li class="nav-item"><a class="nav-link" href="<c:url value="/swagger-ui/index.jsp"/>">Swagger UI</a></li>
      </ul>
    </div>
  </nav>
<!-- Begin page content -->
<div class="container">
    <div class="page-header">
        <h1>AutomationResult: <%= aAutomationResult.toString() %></h1>
        <%
        URI shapeUri = UriBuilder.fromUri(OSLC4JUtils.getServletURI()).path(OslcConstants.PATH_RESOURCE_SHAPES).path(Oslc_autoDomainConstants.AUTOMATIONRESULT_PATH).build();
        Collection<URI> types = aAutomationResult.getTypes();   
        %>
        <p class="lead">Resource URI:&nbsp;
        <jsp:include page="/cz/vutbr/fit/group/verifit/oslc/compilation/automationresulttohtml.jsp"></jsp:include>
        </p>
        <p class="lead">Shape:&nbsp;
        <a href="<%=shapeUri%>"><%=shapeUri%></a>
        </p>
        <p class="lead">rdf:type(s):</p>
        <ul>
        <%for (URI type : aAutomationResult.getTypes()) {%>
        <li><a href="<%=type%>"><%=type%></a></li>
        <%}%>
        </ul>
    </div>
        <h2>Properties</h2>
        <div>
          <% Method method = null; %>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getContributor"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <ul>
            <%
            for(Link next : aAutomationResult.getContributor()) {
                if (next.getValue() == null) {
                    out.write("<li>" + "<em>null</em>" + "</li>");
                }
                else {
                    %>
                    <li>
                    <jsp:include page="/cz/vutbr/fit/group/verifit/oslc/compilation/persontohtml.jsp">
                        <jsp:param name="resourceUri" value="<%=next.getValue()%>"/> 
                        </jsp:include>
                    </li>
                    <%
                }
            }
            %>
            </ul>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getCreated"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <%
            if (aAutomationResult.getCreated() == null) {
                out.write("<em>null</em>");
            }
            else {
                out.write(aAutomationResult.getCreated().toString());
            }
            %>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getCreator"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <ul>
            <%
            for(Link next : aAutomationResult.getCreator()) {
                if (next.getValue() == null) {
                    out.write("<li>" + "<em>null</em>" + "</li>");
                }
                else {
                    %>
                    <li>
                    <jsp:include page="/cz/vutbr/fit/group/verifit/oslc/compilation/persontohtml.jsp">
                        <jsp:param name="resourceUri" value="<%=next.getValue()%>"/> 
                        </jsp:include>
                    </li>
                    <%
                }
            }
            %>
            </ul>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getIdentifier"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <%
            if (aAutomationResult.getIdentifier() == null) {
                out.write("<em>null</em>");
            }
            else {
                out.write(aAutomationResult.getIdentifier().toString());
            }
            %>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getModified"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <%
            if (aAutomationResult.getModified() == null) {
                out.write("<em>null</em>");
            }
            else {
                out.write(aAutomationResult.getModified().toString());
            }
            %>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getType"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <ul>
            <%
            for(Link next : aAutomationResult.getType()) {
                if (next.getValue() == null) {
                    out.write("<li>" + "<em>null</em>" + "</li>");
                }
                else {
                    out.write("<li>" + "<a href=\"" + next.getValue().toString() + "\" class=\"oslc-resource-link\">" + next.getValue().toString() + "</a>" + "</li>");
                }
            }
            %>
            </ul>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getSubject"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <ul>
            <%
            Iterator<String> subjectItr = aAutomationResult.getSubject().iterator();
            while(subjectItr.hasNext()) {
                out.write("<li>" + subjectItr.next().toString() + "</li>");
            }
            %>
            </ul>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getTitle"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <%
            if (aAutomationResult.getTitle() == null) {
                out.write("<em>null</em>");
            }
            else {
                out.write(aAutomationResult.getTitle().toString());
            }
            %>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getInstanceShape"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <ul>
            <%
            for(Link next : aAutomationResult.getInstanceShape()) {
                if (next.getValue() == null) {
                    out.write("<li>" + "<em>null</em>" + "</li>");
                }
                else {
                    out.write("<li>" + "<a href=\"" + next.getValue().toString() + "\" class=\"oslc-resource-link\">" + next.getValue().toString() + "</a>" + "</li>");
                }
            }
            %>
            </ul>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getServiceProvider"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <ul>
            <%
            for(Link next : aAutomationResult.getServiceProvider()) {
                if (next.getValue() == null) {
                    out.write("<li>" + "<em>null</em>" + "</li>");
                }
                else {
                    out.write("<li>" + "<a href=\"" + next.getValue().toString() + "\" class=\"oslc-resource-link\">" + next.getValue().toString() + "</a>" + "</li>");
                }
            }
            %>
            </ul>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getState"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <ul>
            <%
            for(Link next : aAutomationResult.getState()) {
                if (next.getValue() == null) {
                    out.write("<li>" + "<em>null</em>" + "</li>");
                }
                else {
                    out.write("<li>" + "<a href=\"" + next.getValue().toString() + "\" class=\"oslc-resource-link\">" + next.getValue().toString() + "</a>" + "</li>");
                }
            }
            %>
            </ul>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getDesiredState"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <%
            if ((aAutomationResult.getDesiredState() == null) || (aAutomationResult.getDesiredState().getValue() == null)) {
                out.write("<em>null</em>");
            }
            else {
                out.write("<a href=\"" + aAutomationResult.getDesiredState().getValue().toString() + "\" class=\"oslc-resource-link\">" + aAutomationResult.getDesiredState().getValue().toString() + "</a>");
            }
            %>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getVerdict"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <ul>
            <%
            for(Link next : aAutomationResult.getVerdict()) {
                if (next.getValue() == null) {
                    out.write("<li>" + "<em>null</em>" + "</li>");
                }
                else {
                    out.write("<li>" + "<a href=\"" + next.getValue().toString() + "\" class=\"oslc-resource-link\">" + next.getValue().toString() + "</a>" + "</li>");
                }
            }
            %>
            </ul>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getContribution"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <ul>
            <%
            for(Object next : aAutomationResult.getContribution()) {
                %>
                <li> 
                <jsp:include page="/cz/vutbr/fit/group/verifit/oslc/compilation/contributiontohtml.jsp">
                    <jsp:param name="asLocalResource" value="true"/>
                    </jsp:include>
                </li> 
                <%
            }
            %>
            </ul>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getInputParameter"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <ul>
            <%
            for(Object next : aAutomationResult.getInputParameter()) {
                %>
                <li> 
                <jsp:include page="/cz/vutbr/fit/group/verifit/oslc/compilation/parameterinstancetohtml.jsp">
                    <jsp:param name="asLocalResource" value="true"/>
                    </jsp:include>
                </li> 
                <%
            }
            %>
            </ul>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getOutputParameter"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <ul>
            <%
            for(Object next : aAutomationResult.getOutputParameter()) {
                %>
                <li> 
                <jsp:include page="/cz/vutbr/fit/group/verifit/oslc/compilation/parameterinstancetohtml.jsp">
                    <jsp:param name="asLocalResource" value="true"/>
                    </jsp:include>
                </li> 
                <%
            }
            %>
            </ul>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getProducedByAutomationRequest"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <%
            if ((aAutomationResult.getProducedByAutomationRequest() == null) || (aAutomationResult.getProducedByAutomationRequest().getValue() == null)) {
                out.write("<em>null</em>");
            }
            else {
                %>
                <jsp:include page="/cz/vutbr/fit/group/verifit/oslc/compilation/automationrequesttohtml.jsp">
                    <jsp:param name="resourceUri" value="<%=aAutomationResult.getProducedByAutomationRequest().getValue()%>"/> 
                    </jsp:include>
                <%
            }
            %>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getReportsOnAutomationPlan"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <%
            if ((aAutomationResult.getReportsOnAutomationPlan() == null) || (aAutomationResult.getReportsOnAutomationPlan().getValue() == null)) {
                out.write("<em>null</em>");
            }
            else {
                %>
                <jsp:include page="/cz/vutbr/fit/group/verifit/oslc/compilation/automationplantohtml.jsp">
                    <jsp:param name="resourceUri" value="<%=aAutomationResult.getReportsOnAutomationPlan().getValue()%>"/> 
                    </jsp:include>
                <%
            }
            %>
            
            </dd>
          </dl>
          <dl class="row">
            <% method = AutomationResult.class.getMethod("getCreatedSUT"); %>
            <dt  class="col-sm-2 text-right"><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
            <dd class="col-sm-9">
            <%
            if ((aAutomationResult.getCreatedSUT() == null) || (aAutomationResult.getCreatedSUT().getValue() == null)) {
                out.write("<em>null</em>");
            }
            else {
                %>
                <jsp:include page="/cz/vutbr/fit/group/verifit/oslc/compilation/suttohtml.jsp">
                    <jsp:param name="resourceUri" value="<%=aAutomationResult.getCreatedSUT().getValue()%>"/> 
                    </jsp:include>
                <%
            }
            %>
            
            </dd>
          </dl>
        </div>
        <%
        Map<QName, Object> extendedProperties = aAutomationResult.getExtendedProperties();
        if (!extendedProperties.isEmpty()) {
        %>
            <h3>Extended Properties</h3>
            <div>
            <%
            for (Map.Entry<QName, Object> entry : extendedProperties.entrySet()) 
            {
                QName key = entry.getKey();
                Object value = entry.getValue();
            %>
            <dl class="row">
                <dt  class="col-sm-2 text-right"><a href="<%=key.getNamespaceURI() + key.getLocalPart() %>"><%=key.getLocalPart()%></a></dt>
                <dd class="col-sm-9"><%= value.toString()%></dd>
            </dl>
            <%
            }
            %>
            </div>
        <%
        }
        %>
</div>
  <footer class="footer">
    <div class="container">
      <p class="text-muted">
        OSLC Adaptor was generated using <a href="http://eclipse.org/lyo">Eclipse Lyo</a>.
      </p>
    </div>
  </footer>
</body>
</html>