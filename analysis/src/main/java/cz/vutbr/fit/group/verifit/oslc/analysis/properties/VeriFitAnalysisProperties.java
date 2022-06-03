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

import cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution.RequestRunner.ConfigOs;

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
		overridePropertiesWithEnvVariables();
		updateProperties();
		setDummyToolPath();
	}
	
	private static void setDummyToolPath()
	{
		// dummy
		if (VeriFitAnalysisProperties.CONFIG_OS == ConfigOs.LINUX)
		{
			DUMMYTOOL_PATH = Paths.get("tests/resources/dummy_tool.sh").toFile().getAbsolutePath();
			
		}
		else if (VeriFitAnalysisProperties.CONFIG_OS == ConfigOs.WINDOWS_BAT)
		{
			DUMMYTOOL_PATH = Paths.get("tests/resources/dummy_tool.bat").toFile().getAbsolutePath();
		}
		else // if (VeriFitAnalysisProperties.CONFIG_OS == ConfigOs.WINDOWS_PS1)
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
		try { // check if parsable as int
			Integer.parseInt(ADAPTER_PORT);
		} catch (Exception e) {
			throw new IOException("failed to parse adapter_port value - int expected");
		}
		
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
		if (str_AUTHENTICAION_ENABLED == null) {
			AUTHENTICATION_ENABLED = false;	// assume false by default
		} else {
			try {
				AUTHENTICATION_ENABLED = Boolean.parseBoolean(str_AUTHENTICAION_ENABLED);
			} catch (Exception e) {
				throw new IOException("failed to parse enable_authentication value - boolean expected");
			}			
		}
		if (AUTHENTICATION_ENABLED) { // load other relevant props only if needed
			AUTHENTICATION_USERNAME = VeriFitAnalysisProperties.getProperty("username");	
			if (AUTHENTICATION_USERNAME == null)
				throw new IOException("username missing");
			
			AUTHENTICATION_PASSWORD = VeriFitAnalysisProperties.getProperty("password");	
			if (AUTHENTICATION_PASSWORD == null)
				throw new IOException("password missing");
		}
		
		String str_KEEP_LAST_N_ENABLED;
		str_KEEP_LAST_N_ENABLED = VeriFitAnalysisProperties.getProperty("keep_last_n_enabled");	
		if (str_KEEP_LAST_N_ENABLED == null) {
			KEEP_LAST_N_ENABLED = false;	// assume false by default
		} else {
			try {
				KEEP_LAST_N_ENABLED = Boolean.parseBoolean(str_KEEP_LAST_N_ENABLED);
			} catch (Exception e) {
				throw new IOException("failed to parse keep_last_n_enabled value - boolean expected");
			}
		}
		if (KEEP_LAST_N_ENABLED) { // load other relevant props only if needed
			String str_KEEP_LAST_N;
			str_KEEP_LAST_N = VeriFitAnalysisProperties.getProperty("keep_last_n");	
			if (str_KEEP_LAST_N == null)
				throw new IOException("keep_last_n missing");
			try {
				KEEP_LAST_N = Long.parseLong(str_KEEP_LAST_N);
				if (KEEP_LAST_N < 1)
					throw new Exception();
			} catch (Exception e) {
				throw new IOException("failed to parse keep_last_n value - positive non-zero integer expected");
				
			}
		}

		if (SystemUtils.IS_OS_LINUX) {
			CONFIG_OS = ConfigOs.LINUX;
			CONFIG_OS_STR = "linux";
		} else {
			String str_CONFIG_OS;
			str_CONFIG_OS = VeriFitAnalysisProperties.getProperty("config_win_shell");	
			if (str_CONFIG_OS == null)
				throw new IOException("config_win_shell missing");
			try {
				if (str_CONFIG_OS.equalsIgnoreCase("windows_bat"))
					CONFIG_OS = ConfigOs.WINDOWS_BAT;
				else if (str_CONFIG_OS.equalsIgnoreCase("windows_ps1"))
					CONFIG_OS = ConfigOs.WINDOWS_PS1;
				else
					throw new Exception();
			} catch (Exception e) {
				throw new IOException("failed to parse config_win_shell value - options are \"windows_ps1\" or \"windows_bat\"");
			}
			CONFIG_OS_STR = str_CONFIG_OS;
		}
		
		// AHT service registry configuration
		String str_AHT_ENABLED;
		str_AHT_ENABLED = VeriFitAnalysisProperties.getProperty("aht_enabled");	
		if (str_AHT_ENABLED == null) {
			AHT_ENABLED = false;	// assume false by default 
		} else {
			try {
				AHT_ENABLED = Boolean.parseBoolean(str_AHT_ENABLED);
			} catch (Exception e) {
				throw new IOException("failed to parse aht_enabled value - boolean expected");
			}
		}
		if (AHT_ENABLED) { // load other AHT props only if enabled
			AHT_SERVICE_REGISTRY_HOST = VeriFitAnalysisProperties.getProperty("aht_service_registry_host");	
			if (AHT_SERVICE_REGISTRY_HOST == null)
				throw new IOException("aht_service_registry_host missing");
			
			AHT_SERVICE_REGISTRY_PORT = VeriFitAnalysisProperties.getProperty("aht_service_registry_port");	
			if (AHT_SERVICE_REGISTRY_PORT == null)
				throw new IOException("aht_service_registry_port missing");
			try { // check if parsable as int
				Integer.parseInt(AHT_SERVICE_REGISTRY_PORT);
			} catch (Exception e) {
				throw new IOException("failed to parse aht_service_registry_port value - int expected");
			}
			
			AHT_SERVICE_NAME = VeriFitAnalysisProperties.getProperty("aht_service_name");	
			if (AHT_SERVICE_NAME == null)
				throw new IOException("aht_service_name missing");
			
			AHT_SYSTEM_NAME = VeriFitAnalysisProperties.getProperty("aht_system_name");	
			if (AHT_SYSTEM_NAME == null)
				throw new IOException("aht_system_name missing");
			
			AHT_CERTIFICATE = VeriFitAnalysisProperties.getProperty("aht_certificate");
			if (AHT_CERTIFICATE == null)
				throw new IOException("aht_certificate missing");
			
			AHT_CERTIFICATE_PASSWORD = VeriFitAnalysisProperties.getProperty("aht_certificate_password");
			if (AHT_CERTIFICATE_PASSWORD == null)
				throw new IOException("aht_certificate_password missing");
		}
		
		String str_INPROGRESS_OUTPUTS_ENABLED;
		str_INPROGRESS_OUTPUTS_ENABLED = VeriFitAnalysisProperties.getProperty("inprogress_outputs_enabled");	
		if (str_INPROGRESS_OUTPUTS_ENABLED == null) {
			INPROGRESS_OUTPUTS_ENABLED = false;	// assume false by default
		} else {
			try {
				INPROGRESS_OUTPUTS_ENABLED = Boolean.parseBoolean(str_INPROGRESS_OUTPUTS_ENABLED);
			} catch (Exception e) {
				throw new IOException("failed to parse inprogress_outputs_enabled value - boolean expected");
			}
		}
	}

	/**
	 * Checks if any relevant environmental variables are defined and if yes, then 
	 * they are used to override the .properties configuration
	 */
	private static void overridePropertiesWithEnvVariables()
	{
		String envAdapterHost = System.getenv("UNITE_ANALYSIS_HOST");
		if (envAdapterHost != null)
			ADAPTER_HOST = envAdapterHost;

		String envAdapterPort = System.getenv("UNITE_ANALYSIS_PORT");
		if (envAdapterPort != null)
			ADAPTER_PORT = envAdapterPort;
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
	public static final String CERTIFICATES_PATH = "./conf/certificates";
	public static final String UNIC_CONF_PATH = "./conf/unic";

	public static String ADAPTER_HOST;
	public static String ADAPTER_PORT;
	
	public static String DUMMYTOOL_PATH;

    public static String SPARQL_SERVER_NAMED_GRAPH_RESOURCES;
    public static String SPARQL_SERVER_QUERY_ENDPOINT;
    public static String SPARQL_SERVER_UPDATE_ENDPOINT;
    
    public static Boolean AUTHENTICATION_ENABLED;
    public static String AUTHENTICATION_USERNAME;
    public static String AUTHENTICATION_PASSWORD;
    
    public static Boolean KEEP_LAST_N_ENABLED;
    public static long KEEP_LAST_N;

	public static ConfigOs CONFIG_OS;
	public static String CONFIG_OS_STR;    

	public static Boolean AHT_ENABLED;
    public static String AHT_SERVICE_REGISTRY_HOST;
    public static String AHT_SERVICE_REGISTRY_PORT;
    public static String AHT_SERVICE_NAME;
    public static String AHT_SYSTEM_NAME;
    public static String AHT_CERTIFICATE;
    public static String AHT_CERTIFICATE_PASSWORD;
    
    public static Boolean INPROGRESS_OUTPUTS_ENABLED;
    
    /*
     *  Internal constants
     */
	public static final String AUTOMATION_PROVIDER_ID = "A0";
	public static final String ADAPTER_CONTEXT = "/analysis";
	public static String SERVER_URL = ADAPTER_HOST + ":" + ADAPTER_PORT;
    public static String PATH_AUTOMATION_SERVICE_PROVIDERS = SERVER_URL + ADAPTER_CONTEXT + "/services/serviceProviders";
    public static String PATH_RESOURCE_SHAPES = SERVER_URL + ADAPTER_CONTEXT + "/services/resourceShapes";
}