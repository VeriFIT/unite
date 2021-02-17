/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.shared.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc.domains.auto.ParameterInstance;
import org.eclipse.lyo.oslc4j.provider.jena.JenaModelHelper;
import org.eclipse.lyo.store.Store;
import org.eclipse.lyo.store.StorePool;

import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;
import cz.vutbr.fit.group.verifit.oslc.shared.exceptions.OslcResourceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public class Utils {

	/**
	 * Used to generate IDs for new resources in a synchronized way (datarace free)
	 * @author od42
	 */
	public static class ResourceIdGen {
	    int idCounter; 
	    
	    public ResourceIdGen ()
	    {
	    	idCounter = 0;
	    };
	    
	    public ResourceIdGen (int start)
	    {
	    	idCounter = start;
	    }
	    
	    /**
	     * Get a new ID. This increments the internal ID counter. Synchronized.
	     * @return New ID
	     */
	    public synchronized String getId() {
	    	idCounter++;
	        return Integer.toString(idCounter - 1);
	    }
	}
	
	public static String base64Encode(byte [] bytes)
	{
		Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(bytes);
	}
	
	public static byte [] base64Decode(String base64Str)
	{
		Decoder decoder = Base64.getDecoder();
		return decoder.decode(base64Str);
	}

	/**
	 * Parses resources <T> from an XML file and returns them as an array.
	 * @param pathToFile	Path to the XML file to load from
	 * @param clazz	<T>.class of the resource to be parsed from the XML file
	 * @return An array of <T> resources
	 * @throws FileNotFoundException Error accessing the XML file
	 * @throws LyoJenaModelException Error parsing the XML file
	 */
	public static <T> T[] parseResourcesFromXmlFile(File pathToFile, Class<T> clazz) throws FileNotFoundException, org.apache.jena.riot.RiotException
	{
		InputStream inStream = new FileInputStream(pathToFile);
		Model model = ModelFactory.createDefaultModel();
		model.read(inStream, null);
		return JenaModelHelper.unmarshal(model, clazz);
	}

	/**
	 * Get the ID of an OSLC resource from its URI (About(), or Link)
	 * @param	uri	OSLC resource uri (eg. from a Link)
	 * @return 		ID of the OSLC resource
	 */
	public static String getResourceIdFromUri(URI uri)
	{
		String uriPath = uri.getPath();
		return uriPath.substring(uriPath.lastIndexOf('/') + 1);
	}
	

	/**
	 * Check that the triplestore is running by sending a request. 
	 * @throws Exception
	 */
	public static void checkStoreOnline(StorePool storePool) throws Exception
	{

        Store store = storePool.getStore();
		try {
			// Dont care if the graph exists or not, will be created later if needed
			store.namedGraphExists(new URI("http://random_graph"));
			
		} catch (URISyntaxException e) {
			// TODO never happens
			e.printStackTrace();
	    } finally {
	        storePool.releaseStore(store);
	    }
	}
	
	/**
	 * Check that the AutomationRequest contains the necessary input parameters based on its AutoPlan, fill the AutomationResult with output parameters (default values),
	 * and return a simplified map of parameters (TODO refactor the map)
	 * @param execAutoRequest			The AutomationRequest to execute
	 * @param newAutoResult				The AutomationResult created by the AutoRequest.
	 * @param execAutoPlan				The AutomationPlan refferenced by the executed AutomationRequest
	 * @throws OslcResourceException 	When the executed AutomationRequest properties are invalid or missing
	 * @return	A map of input parameters [ name, Pair(value,position) ]; position -1 means there was no position
	 */
	public static Map<String, Pair<String,Integer>> processAutoReqInputParams(AutomationRequest execAutoRequest, AutomationResult newAutoResult, AutomationPlan execAutoPlan) throws OslcResourceException
	{
		/// check the input parameters and create a map of "name" -> ("value", position)
		Map<String, Pair<String,Integer>> inputParamsMap = new HashMap<String, Pair<String,Integer>>();
		
		// loop through autoPlan defined parameters to match them with the input params
		for (ParameterDefinition definedParam : execAutoPlan.getParameterDefinition())
		{
			Integer paramPosition;
			if (definedParam.getCommandlinePosition() != null)
			{
				paramPosition = definedParam.getCommandlinePosition();
			}
			else
			{
				paramPosition = -1;
			}
				
			// find the corresponding autoRequest input parameter
			boolean matched = false;
			for (ParameterInstance submittedParam : execAutoRequest.getInputParameter())
			{				
				if (definedParam.getName().equals(submittedParam.getName()))
				{
					
					// check if the value is allowed
					Boolean validValue = true;
					if (definedParam.getAllowedValue().size() > 0)
					{
						validValue = false;
						for (String allowedValue : definedParam.getAllowedValue())
						{
							if (allowedValue.equals(submittedParam.getValue()))
							{
								validValue = true;
								break;
							}
						}
					}
					if (!validValue)
					{
						throw new OslcResourceException("value '" + submittedParam.getValue() + "' not allowed for the '" + definedParam.getName() + "' parameter");
					}
					
					inputParamsMap.put(definedParam.getName(), Pair.of(submittedParam.getValue(),paramPosition));
					matched = true;
				}
			}
			// try to use the default value if no matching input param found
			if (!matched && definedParam.getDefaultValue() != null)
			{
				inputParamsMap.put(definedParam.getName(), Pair.of(definedParam.getDefaultValue(), paramPosition));
				matched = true;

				// add the default value as an output parameter to the Automation Result
				ParameterInstance outputParameter = null;
				outputParameter = new ParameterInstance();
				outputParameter.setName(definedParam.getName());
				outputParameter.setValue(definedParam.getDefaultValue());
				newAutoResult.addOutputParameter(outputParameter);
			}
			
			// check parameter occurrences
			Boolean paramMissing = false;
			switch (definedParam.getOccurs().getValue().toString())
			{
			case OslcValues.OSLC_OCCURS_ONE:
				// TODO check for more then one when there should be exactly one
			case OslcValues.OSLC_OCCURS_ONEorMany:
				if (!matched)
					paramMissing = true;
				break;
				
			case OslcValues.OSLC_OCCURS_ZEROorONE:
				// TODO check for more then one when there should be max one
				break;

			case OslcValues.OSLC_OCCURS_ZEROorMany:
				break;
			}
			
			if (paramMissing == true)
				throw new OslcResourceException("'" + definedParam.getName() + "' input parameter missing");
		}
		
		// check that there were no unknown input parameters
		for (ParameterInstance submittedParam : execAutoRequest.getInputParameter())
		{				
			boolean matched = false;
			for (ParameterDefinition definedParam : execAutoPlan.getParameterDefinition())
			{
				if (definedParam.getName().equals(submittedParam.getName()))
					matched = true;
			}
			
			if (!matched)
				throw new OslcResourceException("'" + submittedParam.getName() + "' input parameter not recognized");
		}
		
		return inputParamsMap;
	}
}
