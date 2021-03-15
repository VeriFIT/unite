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

<%@page import="cz.vutbr.fit.group.verifit.oslc.domain.SUT"%>
<%@page import="cz.vutbr.fit.group.verifit.oslc.domain.FitDomainConstants"%>

<%@ page contentType="text/html" language="java" pageEncoding="UTF-8" %>

<%
  SUT aSUT = (SUT) request.getAttribute("aSUT");
%>

<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <title><%= aSUT.toString() %></title>

  <link href="<c:url value="/static/css/bootstrap-4.0.0-beta.min.css"/>" rel="stylesheet">
  <link href="<c:url value="/static/css/adaptor.css"/>" rel="stylesheet">

  <script src="<c:url value="/static/js/jquery-3.2.1.min.js"/>"></script>
  <script src="<c:url value="/static/js/popper-1.11.0.min.js"/>"></script>
  <script src="<c:url value="/static/js/bootstrap-4.0.0-beta.min.js"/>"></script>
</head>

<body>

<!-- Begin page content -->
<div>
    <% Method method = null; %>
    <dl class="dl-horizontal">
        <% method = SUT.class.getMethod("getLaunchCommand"); %>
        <dt><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
        <dd>
        <%
        if (aSUT.getLaunchCommand() == null) {
            out.write("<em>null</em>");
        }
        else {
            out.write(aSUT.getLaunchCommand().toString());
        }
        %>
        
        </dd>
    </dl>
    <dl class="dl-horizontal">
        <% method = SUT.class.getMethod("getBuildCommand"); %>
        <dt><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
        <dd>
        <%
        if (aSUT.getBuildCommand() == null) {
            out.write("<em>null</em>");
        }
        else {
            out.write(aSUT.getBuildCommand().toString());
        }
        %>
        
        </dd>
    </dl>
    <dl class="dl-horizontal">
        <% method = SUT.class.getMethod("getTitle"); %>
        <dt><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
        <dd>
        <%
        if (aSUT.getTitle() == null) {
            out.write("<em>null</em>");
        }
        else {
            out.write(aSUT.getTitle().toString());
        }
        %>
        
        </dd>
    </dl>
    <dl class="dl-horizontal">
        <% method = SUT.class.getMethod("getDescription"); %>
        <dt><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
        <dd>
        <%
        if (aSUT.getDescription() == null) {
            out.write("<em>null</em>");
        }
        else {
            out.write(aSUT.getDescription().toString());
        }
        %>
        
        </dd>
    </dl>
    <dl class="dl-horizontal">
        <% method = SUT.class.getMethod("getCreated"); %>
        <dt><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
        <dd>
        <%
        if (aSUT.getCreated() == null) {
            out.write("<em>null</em>");
        }
        else {
            out.write(aSUT.getCreated().toString());
        }
        %>
        
        </dd>
    </dl>
    <dl class="dl-horizontal">
        <% method = SUT.class.getMethod("getModified"); %>
        <dt><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
        <dd>
        <%
        if (aSUT.getModified() == null) {
            out.write("<em>null</em>");
        }
        else {
            out.write(aSUT.getModified().toString());
        }
        %>
        
        </dd>
    </dl>
    <dl class="dl-horizontal">
        <% method = SUT.class.getMethod("getIdentifier"); %>
        <dt><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
        <dd>
        <%
        if (aSUT.getIdentifier() == null) {
            out.write("<em>null</em>");
        }
        else {
            out.write(aSUT.getIdentifier().toString());
        }
        %>
        
        </dd>
    </dl>
    <dl class="dl-horizontal">
        <% method = SUT.class.getMethod("getSUTdirectoryPath"); %>
        <dt><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
        <dd>
        <%
        if (aSUT.getSUTdirectoryPath() == null) {
            out.write("<em>null</em>");
        }
        else {
            out.write(aSUT.getSUTdirectoryPath().toString());
        }
        %>
        
        </dd>
    </dl>
    <dl class="dl-horizontal">
        <% method = SUT.class.getMethod("getCreator"); %>
        <dt><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
        <dd>
        <ul>
        <%
        for(Link next : aSUT.getCreator()) {
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
    <dl class="dl-horizontal">
        <% method = SUT.class.getMethod("isCompiled"); %>
        <dt><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
        <dd>
        <%
        if (aSUT.isCompiled() == null) {
            out.write("<em>null</em>");
        }
        else {
            out.write(aSUT.isCompiled().toString());
        }
        %>
        
        </dd>
    </dl>
    <dl class="dl-horizontal">
        <% method = SUT.class.getMethod("getProducedByAutomationRequest"); %>
        <dt><a href="<%=method.getAnnotation(OslcPropertyDefinition.class).value() %>"><%=method.getAnnotation(OslcName.class).value()%></a></dt>
        <dd>
        <%
        if ((aSUT.getProducedByAutomationRequest() == null) || (aSUT.getProducedByAutomationRequest().getValue() == null)) {
            out.write("<em>null</em>");
        }
        else {
            %>
            <jsp:include page="/cz/vutbr/fit/group/verifit/oslc/compilation/automationrequesttohtml.jsp">
                <jsp:param name="resourceUri" value="<%=aSUT.getProducedByAutomationRequest().getValue()%>"/> 
                </jsp:include>
            <%
        }
        %>
        
        </dd>
    </dl>
</div>
<%
Map<QName, Object> extendedProperties = aSUT.getExtendedProperties();
if (!extendedProperties.isEmpty()) {
%>
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
</body>
</html>
