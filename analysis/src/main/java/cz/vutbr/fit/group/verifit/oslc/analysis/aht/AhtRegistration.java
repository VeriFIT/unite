package cz.vutbr.fit.group.verifit.oslc.analysis.aht;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

import cz.vutbr.fit.group.verifit.arrowhead.client.ArrowheadClient;
import cz.vutbr.fit.group.verifit.arrowhead.client.ArrowheadClientBuilder;
import cz.vutbr.fit.group.verifit.arrowhead.client.ArrowheadServiceRegistryClient;
import cz.vutbr.fit.group.verifit.arrowhead.dto.ArrowheadServiceRegistrationForm;
import cz.vutbr.fit.group.verifit.arrowhead.dto.ArrowheadServiceRegistryEntry;
import cz.vutbr.fit.group.verifit.oslc.analysis.properties.VeriFitAnalysisProperties;

/**
 * Wrapper class for integrating with the Eclipse Arrowhead Tools framework. 
 *
 */
public class AhtRegistration {

	private boolean registered;
	private ArrowheadClient ahtClient;
	private ArrowheadServiceRegistryClient ahtClientServiceRegistry;
	private ArrowheadServiceRegistrationForm ahtFormServiceRegistration;
	private ArrowheadServiceRegistryEntry ahtEntry;
	
	/**
	 * Register as an AHT service in the AHT service registry.
	 * @param unicConfMapForAht Expects a map of .properties loaded from configuration files for UniC. 
	 * 							Key is the property name/key and value is the property value. 
	 * 							The map may contain a mix of configurations for multiple tools, and each
	 * 							tool has two kinds of properties (one from .config and the other from .tasks)
	 * 							these are differentiated by a prefix of their keys. 
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws FileNotFoundException
	 */
	public void register(Map<String, String> unicConfMapForAht)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
			UnrecoverableKeyException, KeyManagementException, FileNotFoundException
	{
		// build a client to connect to the AHT service registry
		ahtClient = ArrowheadClientBuilder.newBuilder().certificate(
			new FileInputStream(Paths.get(
			VeriFitAnalysisProperties.CERTIFICATES_PATH).resolve(VeriFitAnalysisProperties.AHT_CERTIFICATE).toFile()),	// certificate
			VeriFitAnalysisProperties.AHT_CERTIFICATE_PASSWORD)															// password
			//.defaultLogger() // logger is only for debugging
			.build();
		ahtClientServiceRegistry = new ArrowheadServiceRegistryClient(ahtClient,
				VeriFitAnalysisProperties.AHT_SERVICE_REGISTRY_HOST,							// host
				Integer.parseInt(VeriFitAnalysisProperties.AHT_SERVICE_REGISTRY_PORT));			// port
		
		// build a registration form
		ahtFormServiceRegistration = new ArrowheadServiceRegistrationForm(
				VeriFitAnalysisProperties.AHT_SERVICE_NAME,					// service name
				VeriFitAnalysisProperties.AHT_SYSTEM_NAME,					// system name 
				VeriFitAnalysisProperties.ADAPTER_HOST,						// address
				Integer.parseInt(VeriFitAnalysisProperties.ADAPTER_PORT),	// port
				VeriFitAnalysisProperties.ADAPTER_CONTEXT,					// uri
				unicConfMapForAht);											// metadata

		// unregister and then register
		ahtClientServiceRegistry.unregister(ahtFormServiceRegistration);
		ahtEntry = ahtClientServiceRegistry.register(ahtFormServiceRegistration);
		
		registered = true;
	}
	
	/**
	 * Unregister as an AHT service from the AHT service registry.
	 * @return true/false based on if the un-registration was successful/failed
	 */
	public boolean unregister() {
		// do nothing if not registered
		if (!registered)
			return false;
		
		// otherwise try to unregister
		else
			return ahtClientServiceRegistry.unregister(ahtFormServiceRegistration);
	}
	
}
