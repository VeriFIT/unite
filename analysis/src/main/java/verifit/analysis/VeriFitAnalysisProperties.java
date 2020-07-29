


package verifit.analysis;

// Start of user code imports
import java.util.Set;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
// End of user code
import java.util.Properties;

public class VeriFitAnalysisProperties
{
	
	/**
	 * Loads the Java properties configuration
	 * @throws FileNotFoundException	If the properties file is missing
	 * @throws IOException				If one of the properties is missing
	 */
	public static void loadProperties() throws FileNotFoundException, IOException
	{
		Properties VeriFitAnalysisProperties = new Properties();
		VeriFitAnalysisProperties.load(new FileInputStream(PROPERTIES_PATH));
		
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
		
		// TODO anaconda
		ANACONDA_PATH = VeriFitAnalysisProperties.getProperty("anaconda_path");	
		if (ANACONDA_PATH == null)
			throw new IOException("anaconda_path missing");
		String analysers = VeriFitAnalysisProperties.getProperty("anaconda_analysers");	
		if (analysers == null)
			throw new IOException("anaconda_analysers missing");
		ANACONDA_ANALYSERS = new HashSet<String>(Arrays.asList(analysers.split(",")));

		// perun
		PERUN_PATH = VeriFitAnalysisProperties.getProperty("perun_path");	
		if (PERUN_PATH == null)
			throw new IOException("perun_path missing");
		
		// hilite
		HILITE_PATH = VeriFitAnalysisProperties.getProperty("hilite_path");	
		if (HILITE_PATH == null)
			throw new IOException("hilite_path missing");
		
		
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
	public static final String PROPERTIES_PATH = "./VeriFitAnalysis.properties";

	public static String ADAPTER_HOST;
	public static String ADAPTER_PORT;
	
	public static String ANACONDA_PATH;
    public static Set<String> ANACONDA_ANALYSERS = new HashSet<String>();

	public static String PERUN_PATH;
	
	public static String HILITE_PATH;

    public static String SPARQL_SERVER_NAMED_GRAPH_RESOURCES;
    public static String SPARQL_SERVER_QUERY_ENDPOINT;
    public static String SPARQL_SERVER_UPDATE_ENDPOINT;
    
    
    /*
     *  Internal constants
     */
	public static String SERVER_URL = ADAPTER_HOST + ":" + ADAPTER_PORT + "/";
	public static final String ADAPTER_CONTEXT = "analysis/";
    public static String PATH_AUTOMATION_SERVICE_PROVIDERS = SERVER_URL + ADAPTER_CONTEXT + "services/serviceProviders/";
    public static String PATH_RESOURCE_SHAPES = SERVER_URL + ADAPTER_CONTEXT + "services/resourceShapes/";
}