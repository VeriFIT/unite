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

<%@page import="org.eclipse.lyo.oslc4j.core.OSLC4JUtils"%>
<%@page import="javax.ws.rs.core.UriBuilder"%>
<%@page import="java.net.URI"%>
<%@page import="java.io.File"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<% 
File swaggerFolder = new File(application.getRealPath("/swagger-ui/dist")); 
URI yamlPath = UriBuilder.fromUri(OSLC4JUtils.getServletURI()).path("/swagger.yaml").build();
%>

<html>
<head>
    <meta charset="UTF-8">
    <title>Swagger UI</title>
        <%if(swaggerFolder.exists()) {%>
        <link rel="stylesheet" type="text/css" href="./dist/swagger-ui.css" >
        <link rel="icon" type="image/png" href="./dist/favicon-32x32.png" sizes="32x32" />
        <link rel="icon" type="image/png" href="./dist/favicon-16x16.png" sizes="16x16" />
        <style>
          html
          {
            box-sizing: border-box;
            overflow: -moz-scrollbars-vertical;
            overflow-y: scroll;
          }
    
          *,
          *:before,
          *:after
          {
            box-sizing: inherit;
          }
    
          body
          {
            margin:0;
            background: #fafafa;
          }
        </style>
        <%}else{%>
        <link href="<c:url value="/static/css/bootstrap-4.0.0-beta.min.css"/>" rel="stylesheet">
        <link href="<c:url value="/static/css/adaptor.css"/>" rel="stylesheet">
    
        <script src="<c:url value="/static/js/jquery-3.2.1.min.js"/>"></script>
        <script src="<c:url value="/static/js/bootstrap-4.0.0-beta.min.js"/>"></script>
        <%}%>
</head>
<body>
    <%if(swaggerFolder.exists()) {%>
        <div id="swagger-ui"></div>
        <script src="./dist/swagger-ui-bundle.js"> </script>
        <script src="./dist/swagger-ui-standalone-preset.js"> </script>
        <script>
        window.onload = function() {
          // Begin Swagger UI call region
          const ui = SwaggerUIBundle({
            url: "<%=yamlPath%>",
            dom_id: '#swagger-ui',
            deepLinking: true,
            presets: [
              SwaggerUIBundle.presets.apis,
              SwaggerUIStandalonePreset
            ],
            plugins: [
              SwaggerUIBundle.plugins.DownloadUrl
            ],
            layout: "StandaloneLayout"
          })
          // End Swagger UI call region
    
          window.ui = ui
        }
      </script>
    <%}else{%>
          <nav class="navbar navbar-expand-lg sticky-top navbar-light bg-light">
            <div class="container">
              <ul class="navbar-nav mr-auto">
                <li class="nav-item"><a class="nav-link" href="<c:url value="/"/>"><%= application.getServletContextName() %></a></li>
                <li class="nav-item"><a class="nav-link" href="<c:url value="/services/catalog/singleton"/>">Service Provider Catalog</a></li>
                <li class="nav-item"><a class="nav-link" href="<c:url value="/swagger-ui/index.jsp"/>">Swagger UI</a></li>
              </ul>
            </div>
          </nav>
        <div class="container">
            <div class="alert alert-primary" role="alert">
                <h4 class="alert-heading">Swagger UI</h4>
                <hr>
                <p><strong>You don't seem to have fully configured your project to use a local standalone distribution of Swagger UI!</strong></p>
                <ul>
                    <li>A "dist" folder containing the Swagger UI collection of HTML, Javascript, and CSS assets is missing.</li>
                    <li>Please follow the complete instrunctions under <a href="<%= "https://oslc.github.io/developing-oslc-applications/eclipse_lyo/setup-an-oslc-provider-consumer-application.html#provide-openapi-support" %>">Lyo Instructions for Swagger</a></li>
                    <li>Until then, you can copy <a href="<%=yamlPath%>">this OpenAPI specification document (yaml file)</a> 
                    to a remote <a href="<%= "https://editor.swagger.io" %>">Swagger Editor</a> and
                    <ul>
                        <li>use the remote Swagger UI interface</li>
                        <li>generate client SDK code for a number of languages and platforms.</li>
                    </ul>
                    </li>
                </ul>
            </div>
        </div>
        <footer class="footer">
          <div class="container">
            <p class="text-muted">
              OSLC Adaptor was generated using <a href="http://eclipse.org/lyo">Eclipse Lyo</a>.
            </p>
          </div>
        </footer>
    <%}%>
</body>
</html>
