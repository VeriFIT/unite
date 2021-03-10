/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.analysis.properties;

// Start of user code imports

import org.apache.commons.lang3.SystemUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
// End of user code
import java.util.Properties;

public class VeriFitAnalysisProperties
{
	
	/**
	 * Sets up properties for the adapter. Needs to be called on startup.
	 * @throws FileNotFoundException When the configuration file does not exist
	 * @throws IOException When some properties are missing in the conf file 
	 */
	public static void initializeProperties() throws FileNotFoundException, IOException
	{
		loadPropertiesFromFile();
		updateProperties();
		setDummyToolPath();
	}
	
	private static void setDummyToolPath()
	{
		// dummy
		if (SystemUtils.IS_OS_LINUX)
		{
			DUMMYTOOL_PATH = Paths.get("tests/resources/dummy_tool.sh").toFile().getAbsolutePath();
			
		}
		else if (SystemUtils.IS_OS_WINDOWS)
		{
			DUMMYTOOL_PATH = Paths.get("tests/resources/dummy_tool.ps1").toFile().getAbsolutePath();
		}
	}
	
	/**
	 * Loads the Java properties from a configuration file and sets the appropriate internal variables
	 * @throws FileNotFoundException	If the properties file is missing
	 * @throws IOException				If one of the properties is missing
	 */
	private static void loadPropertiesFromFile() throws FileNotFoundException, IOException
	{
		Properties VeriFitAnalysisProperties = new Properties();
		VeriFitAnalysisProperties.load(new FileInputStream(Paths.get(PROPERTIES_PATH).toFile()));
		
		ADAPTER_HOST = VeriFitAnalysisProperties.getProperty("adapter_host");	
		if (ADAPTER_HOST == null)
			throw new IOException("adapter_host missing");
		
		ADAPTER_PORT = VeriFitAnalysisProperties.getProperty("adapter_port");	
		if (ADAPTER_PORT == null)
			throw new IOException("adapter_port missing");
		
		SPARQL_SERVER_QUERY_ENDPOINT = VeriFitAnalysisProperties.getProperty("sparql_query");	
		if (SPARQL_SERVER_QUERY_ENDPOINT == null)
			throw new IOException("sparql_query missing");
		
		SPARQL_SERVER_UPDATE_ENDPOINT = VeriFitAnalysisProperties.getProperty("sparql_update");	
		if (SPARQL_SERVER_UPDATE_ENDPOINT == null)
			throw new IOException("sparql_update missing");
		
		SPARQL_SERVER_NAMED_GRAPH_RESOURCES = VeriFitAnalysisProperties.getProperty("sparql_graph");	
		if (SPARQL_SERVER_NAMED_GRAPH_RESOURCES == null)
			throw new IOException("sparql_graph missing");
		
		String str_AUTHENTICAION_ENABLED;
		str_AUTHENTICAION_ENABLED = VeriFitAnalysisProperties.getProperty("enable_authentication");	
		if (str_AUTHENTICAION_ENABLED == null)
			throw new IOException("enable_authentication missing");
		AUTHENTICATION_ENABLED = Boolean.parseBoolean(str_AUTHENTICAION_ENABLED);
		
		AUTHENTICATION_USERNAME = VeriFitAnalysisProperties.getProperty("username");	
		if (SPARQL_SERVER_NAMED_GRAPH_RESOURCES == null)
			throw new IOException("username missing");
		
		AUTHENTICATION_PASSWORD = VeriFitAnalysisProperties.getProperty("password");	
		if (SPARQL_SERVER_NAMED_GRAPH_RESOURCES == null)
			throw new IOException("password missing");
	}
	
	/**
	 * Update properties which are derived from the configuration file (not directly loaded from it)
	 * A bit messy - SERVER_URL is "null:null" on startup
	 */
	private static void updateProperties()
	{
		SERVER_URL = ADAPTER_HOST + ":" + ADAPTER_PORT + "/";
	    PATH_AUTOMATION_SERVICE_PROVIDERS = SERVER_URL + ADAPTER_CONTEXT + "services/serviceProviders/";
	    PATH_RESOURCE_SHAPES = SERVER_URL + ADAPTER_CONTEXT + "services/resourceShapes/";
	    
	}
	
	/*
	 *  Java properties
	 */
	public static final String PROPERTIES_PATH = "./conf/VeriFitAnalysis.properties";
	public static final String AUTOPLANS_DEF_PATH_BUILTIN = "./conf/BuiltInAnalysisTools";
	public static final String PLUGIN_FILTER_CONF_PATH_BUILTIN = "./conf/BuiltInPluginFiltersConfiguration";
	public static final String AUTOPLANS_DEF_PATH_CUSTOM = "./conf/CustomAnalysisTools";
	public static final String PLUGIN_FILTER_CONF_PATH_CUSTOM = "./conf/CustomPluginFiltersConfiguration";

	public static String ADAPTER_HOST;
	public static String ADAPTER_PORT;
	
	public static String DUMMYTOOL_PATH;

    public static String SPARQL_SERVER_NAMED_GRAPH_RESOURCES;
    public static String SPARQL_SERVER_QUERY_ENDPOINT;
    public static String SPARQL_SERVER_UPDATE_ENDPOINT;
    
    public static Boolean AUTHENTICATION_ENABLED;
    public static String AUTHENTICATION_USERNAME;
    public static String AUTHENTICATION_PASSWORD;
    
    
    /*
     *  Internal constants
     */
	public static final String AUTOMATION_PROVIDER_ID = "A0";
	public static final String ADAPTER_CONTEXT = "analysis/";
	public static String SERVER_URL = ADAPTER_HOST + ":" + ADAPTER_PORT + "/";
    public static String PATH_AUTOMATION_SERVICE_PROVIDERS = SERVER_URL + ADAPTER_CONTEXT + "services/serviceProviders/";
    public static String PATH_RESOURCE_SHAPES = SERVER_URL + ADAPTER_CONTEXT + "services/resourceShapes/";
}