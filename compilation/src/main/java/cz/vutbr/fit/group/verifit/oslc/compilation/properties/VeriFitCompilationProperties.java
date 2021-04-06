/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.compilation.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
// End of user code
import java.util.Properties;

public class VeriFitCompilationProperties
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
	}

	/**
	 * Loads the Java properties from a configuration file and sets the appropriate internal variables
	 * @throws FileNotFoundException	If the properties file is missing
	 * @throws IOException				If one of the properties is missing
	 */
	private static void loadPropertiesFromFile() throws FileNotFoundException, IOException
	{
		Properties VeriFitCompilationProperties = new Properties();
		VeriFitCompilationProperties.load(new FileInputStream(Paths.get(PROPERTIES_PATH).toFile()));
		
		ADAPTER_HOST = VeriFitCompilationProperties.getProperty("adapter_host");	
		if (ADAPTER_HOST == null)
			throw new IOException("adapter_host missing");
		
		ADAPTER_PORT = VeriFitCompilationProperties.getProperty("adapter_port");	
		if (ADAPTER_PORT == null)
			throw new IOException("adapter_port missing");
		
		SPARQL_SERVER_QUERY_ENDPOINT = VeriFitCompilationProperties.getProperty("sparql_query");	
		if (SPARQL_SERVER_QUERY_ENDPOINT == null)
			throw new IOException("sparql_query missing");
		
		SPARQL_SERVER_UPDATE_ENDPOINT = VeriFitCompilationProperties.getProperty("sparql_update");	
		if (SPARQL_SERVER_UPDATE_ENDPOINT == null)
			throw new IOException("sparql_update missing");
		
		SPARQL_SERVER_NAMED_GRAPH_RESOURCES = VeriFitCompilationProperties.getProperty("sparql_graph");	
		if (SPARQL_SERVER_NAMED_GRAPH_RESOURCES == null)
			throw new IOException("sparql_graph missing");
		
		String str_PERSIST_SUT_DIRS;
		str_PERSIST_SUT_DIRS = VeriFitCompilationProperties.getProperty("persist_sut_dirs");	
		if (str_PERSIST_SUT_DIRS == null)
			throw new IOException("persist_sut_dirs missing");
		try {
			PERSIST_SUT_DIRS = Boolean.parseBoolean(str_PERSIST_SUT_DIRS);
		} catch (Exception e) {
			throw new IOException("failed to parse enable_authentication value - boolean expected");
		}

		String str_AUTHENTICAION_ENABLED;
		str_AUTHENTICAION_ENABLED = VeriFitCompilationProperties.getProperty("enable_authentication");	
		if (str_AUTHENTICAION_ENABLED == null)
			throw new IOException("enable_authentication missing");
		AUTHENTICATION_ENABLED = Boolean.parseBoolean(str_AUTHENTICAION_ENABLED);
		
		AUTHENTICATION_USERNAME = VeriFitCompilationProperties.getProperty("username");	
		if (SPARQL_SERVER_NAMED_GRAPH_RESOURCES == null)
			throw new IOException("username missing");
		
		AUTHENTICATION_PASSWORD = VeriFitCompilationProperties.getProperty("password");	
		if (SPARQL_SERVER_NAMED_GRAPH_RESOURCES == null)
			throw new IOException("password missing");

		
		String str_KEEP_LAST_N_ENABLED;
		str_KEEP_LAST_N_ENABLED = VeriFitCompilationProperties.getProperty("keep_last_n_enabled");	
		if (str_KEEP_LAST_N_ENABLED == null)
			throw new IOException("keep_last_n_enabled missing");
		try {
			KEEP_LAST_N_ENABLED = Boolean.parseBoolean(str_KEEP_LAST_N_ENABLED);
		} catch (Exception e) {
			throw new IOException("failed to parse keep_last_n_enabled value - boolean expected");
			
		}
		
		String str_KEEP_LAST_N;
		str_KEEP_LAST_N = VeriFitCompilationProperties.getProperty("keep_last_n");	
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
	public static final String PROPERTIES_PATH = "./conf/VeriFitCompilation.properties";

	public static String ADAPTER_HOST;
	public static String ADAPTER_PORT;

    public static String SPARQL_SERVER_NAMED_GRAPH_RESOURCES;
    public static String SPARQL_SERVER_QUERY_ENDPOINT;
    public static String SPARQL_SERVER_UPDATE_ENDPOINT;
    
    public static Boolean PERSIST_SUT_DIRS;
    public static Boolean AUTHENTICATION_ENABLED;
    public static String AUTHENTICATION_USERNAME;
    public static String AUTHENTICATION_PASSWORD;
    
    public static Boolean KEEP_LAST_N_ENABLED;
    public static long KEEP_LAST_N;
    
    
    
    /*
     *  Internal constants
     */
	public static final String AUTOMATION_PROVIDER_ID = "A0";
	public static String SERVER_URL = ADAPTER_HOST + ":" + ADAPTER_PORT + "/";
	public static final String ADAPTER_CONTEXT = "compilation/";
    public static String PATH_AUTOMATION_SERVICE_PROVIDERS = SERVER_URL + ADAPTER_CONTEXT + "services/serviceProviders/";
    public static String PATH_RESOURCE_SHAPES = SERVER_URL + ADAPTER_CONTEXT + "services/resourceShapes/";
    
    public static String SUT_FOLDER = "SUT";	// TODO if this changes, there might be changes required in AnalysisManager->deleteContribution()
}