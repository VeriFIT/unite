// Start of user code Copyright
/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Eclipse Distribution License is available at
 *  http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *  Contributors:
 *
 *     Sam Padgett         - initial API and implementation
 *     Michael Fiedler     - adapted for OSLC4J
 *     Jad El-khoury       - initial implementation of code generator (https://bugs.eclipse.org/bugs/show_bug.cgi?id=422448)
 *     Andrii Berezovskyi  - change URL configuration logic (Bug 509767)
 *
 * This file is generated by org.eclipse.lyo.oslc4j.codegenerator
 *******************************************************************************/
// End of user code

package verifit.analysis.servlet;

import java.net.MalformedURLException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import org.eclipse.lyo.oslc4j.core.OSLC4JUtils;

import verifit.analysis.VeriFitAnalysisManager;

// Start of user code imports
// End of user code

public class ServletListener implements ServletContextListener  {
    private static final String DEFAULT_BASE = "http://localhost:8080";
    private static final String PROPERTY_BASE = servletContextParameterName("baseurl");
    private static final Logger logger = Logger.getLogger(ServletListener.class.getName());

    //If you are using another servletName in your web.xml configuration file, modify this variable early in the method contextInitialized below
    private static String servletName = "JAX-RS Servlet";

    // Start of user code class_attributes
    // End of user code

    public ServletListener() {
        super();
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        // Start of user code contextInitialized_init
        //TODO: override the default servletName to match that defined in your web.xml configuration file.
        //servletName = "Your_Configured_Servlet_Name";
        // End of user code

        String baseUrl = generateBasePath(servletContextEvent);
        String servletUrlPattern = "services/";
        try {
            servletUrlPattern = getServletUrlPattern(servletContextEvent);
        } catch (Exception e1) {
            logger.log(Level.SEVERE, "servletListner encountered problems identifying the servlet URL pattern.", e1);
        }
        try {
            OSLC4JUtils.setPublicURI(baseUrl);
            OSLC4JUtils.setServletPath(servletUrlPattern);
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "servletListner encountered MalformedURLException.", e);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "servletListner encountered IllegalArgumentException.", e);
        }

        logger.log(Level.INFO, "servletListner contextInitialized.");

        // Establish connection to data backbone etc ...
        VeriFitAnalysisManager.contextInitializeServletListener(servletContextEvent);

        // Start of user code contextInitialized_final
        // End of user code
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent)
    {
        // Start of user code contextDestroyed_init
        // End of user code

        // Shutdown connections to data backbone etc...
        VeriFitAnalysisManager.contextDestroyServletListener(servletContextEvent);

        // Start of user code contextDestroyed_final
        // End of user code
    }

    // Start of user code class_methods
    // End of user code

    private static String servletContextParameterName(String parameter) {
        return String.format("%s.%s", ServletListener.class.getPackage().getName(), parameter);
    }

    private static String generateBasePath(final ServletContextEvent servletContextEvent) {
        final ServletContext servletContext = servletContextEvent.getServletContext();
        String base = getInitParameterOrDefault(servletContext, PROPERTY_BASE, DEFAULT_BASE);
        return base + servletContext.getContextPath();
    }

    private static String getInitParameterOrDefault(ServletContext context, String propertyName, String defaultValue) {
        String base = context.getInitParameter(propertyName);
        if (base == null || base.trim().isEmpty()) {
            base = defaultValue;
        }
        return base;
    }

    private static String getServletUrlPattern(final ServletContextEvent servletContextEvent) throws Exception {
        final ServletContext servletContext = servletContextEvent.getServletContext();

        ServletRegistration servletRegistration = servletContext.getServletRegistration(servletName);
        if (servletRegistration == null) {
            throw new NoSuchElementException("no servlet with name \"" + servletName + "\" is found.");
        }
        java.util.Collection<java.lang.String> mappings = servletRegistration.getMappings();
        if (mappings.size() != 1) {
            throw new NoSuchElementException("unable to identify servlet mappings for servlet with name \"" + servletName + "\".");
        }
        String mapping = (String) mappings.toArray()[0];

        //url patterns in  most cases end with '\*'. But a url-pattern with just '\' may be found for exact matches.
        if (mapping.endsWith("*"))
            mapping = mapping.substring(0, mapping.length()-1);
        return mapping;
    }
}

