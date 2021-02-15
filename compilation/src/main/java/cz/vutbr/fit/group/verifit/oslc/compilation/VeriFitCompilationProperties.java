/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.compilation;

// Start of user code imports
import java.util.Set;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
// End of user code
import java.util.Properties;

public class VeriFitCompilationProperties
{
	
	/**
	 * Loads the Java properties configuration
	 * @throws FileNotFoundException	If the properties file is missing
	 * @throws IOException				If one of the properties is missing
	 */
	public static void loadProperties() throws FileNotFoundException, IOException
	{
		Properties VeriFitCompilationProperties = new Properties();
		VeriFitCompilationProperties.load(new FileInputStream(PROPERTIES_PATH));
		
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
		PERSIST_SUT_DIRS = Boolean.parseBoolean(str_PERSIST_SUT_DIRS);
		
		updateConstants();
	}
	
	/**
	 * A bit messy - SERVER_URL is "null:null" on startup
	 */
	private static void updateConstants()
	{
		SERVER_URL = ADAPTER_HOST + ":" + ADAPTER_PORT + "/";
	    PATH_AUTOMATION_SERVICE_PROVIDERS = SERVER_URL + ADAPTER_CONTEXT + "services/serviceProviders/";
	    PATH_RESOURCE_SHAPES = SERVER_URL + ADAPTER_CONTEXT + "services/resourceShapes/";
	}
	
	/*
	 *  Java properties
	 */
	public static final String PROPERTIES_PATH = "./VeriFitCompilation.properties";

	public static String ADAPTER_HOST;
	public static String ADAPTER_PORT;

    public static String SPARQL_SERVER_NAMED_GRAPH_RESOURCES;
    public static String SPARQL_SERVER_QUERY_ENDPOINT;
    public static String SPARQL_SERVER_UPDATE_ENDPOINT;
    
    public static Boolean PERSIST_SUT_DIRS;
    
    
    /*
     *  Internal constants
     */
	public static String SERVER_URL = ADAPTER_HOST + ":" + ADAPTER_PORT + "/";
	public static final String ADAPTER_CONTEXT = "compilation/";
    public static String PATH_AUTOMATION_SERVICE_PROVIDERS = SERVER_URL + ADAPTER_CONTEXT + "services/serviceProviders/";
    public static String PATH_RESOURCE_SHAPES = SERVER_URL + ADAPTER_CONTEXT + "services/resourceShapes/";
}