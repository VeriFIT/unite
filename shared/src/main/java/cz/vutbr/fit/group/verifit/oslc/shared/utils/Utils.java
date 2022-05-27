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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.regex.Pattern;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
//import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.ZipFile;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc.domains.auto.ParameterInstance;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.provider.jena.JenaModelHelper;
import org.eclipse.lyo.store.Store;
import org.eclipse.lyo.store.StorePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.group.verifit.oslc.OslcValues;
import cz.vutbr.fit.group.verifit.oslc.shared.exceptions.OslcResourceException;

public class Utils {

    
	/**
	 * Used to generate IDs for new resources in a synchronized way (datarace free)
	 * @author od42
	 */
	public static class ResourceIdGen {
	    private static final Logger log = LoggerFactory.getLogger(ResourceIdGen.class);
	    long idCounter; 
	    
	    public ResourceIdGen ()
	    {
	    	idCounter = 0;
	    };
	    
	    public ResourceIdGen (long start)
	    {
	    	idCounter = start;
	    }
	    
	    /**
	     * Get a new ID. This increments the internal ID counter. Synchronized.
	     * @return New ID
	     */
	    public synchronized String getId() {
	    	if (idCounter > Long.MAX_VALUE - 10000)
	    	{
	    		log.warn("################################################################################\n"
	    				+ "    ID GENERATOR IS NEARING THE LONG MAX VALUE (only 10,000 IDs left)"
	    				+ "################################################################################");
	    	}
	    	else if (idCounter == Long.MAX_VALUE)
	    	{
	    		log.error("################################################################################\n"
	    				+ "    ID GENERATOR JUST REACHED THE LONG MAX VALUE AND WILL START FROM ZERO"
	    				+ "################################################################################");

		    	idCounter = 0; // reset the counter
	    	}
	    	
	    	idCounter++;    	
	        return Long.toString(idCounter - 1);
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
	
	public static boolean base64IsEncoded(String base64)
	{
    	if (Pattern.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$", base64))  
	    	return true;
    	else
    		return false;
	}
	
	/**
	 * @param twoLines	String containing a \n and a base64 encoded string after the \n
	 * @return	True if the input string is well formed (has \n and valid base64 chars after)
	 */
	public static boolean base64IsValueOnSecondLineValid(String twoLines)
	{
		int idxSplit = twoLines.indexOf('\n');
		if (idxSplit == -1)
			return false;
		
		String base64 = twoLines.substring(twoLines.indexOf('\n') + 1);
		return base64IsEncoded(base64);
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
	 * @author https://stackoverflow.com/a/39903784
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public static boolean isBinaryFile(File f) throws IOException {
        String type = Files.probeContentType(f.toPath());
        if (type == null) {
            //type couldn't be determined, assume binary
            return true;
        } else if (type.startsWith("text")
        		|| type.contains("xml")	// TODO text xml inside of an xml property?
        		|| type.contains("json")
        		|| type.contains("html")) {
            return false;
        } else {
            //type isn't text
            return true;
        }
    }
	

	/**
	 * ZIPs all specified files into a new ZIP file from the perspective of a directory.
	 * All files have to be inside of that directory.
	 * 
	 * @param modifFiles List of files to zip (no directories)
	 * @param dirToZipFrom  This path will be used to make the filesToZip paths relative
	 * @param pathToZip  Path where to place the new ZIP file
	 * @return The zip file
	 * @throws IOException
	 */
	public static File zipFiles(Collection<File> modifFiles, Path dirToZipFrom, Path pathToZip) throws IOException
	{
		FileOutputStream fileOutStream = new FileOutputStream(pathToZip.toString());
		ZipOutputStream zipOutStream = new ZipOutputStream(fileOutStream);

		for (File currFile : modifFiles)
		{
			// relativize the file path to the dir path
			String relativeFilePath = new File(dirToZipFrom.toString()).toURI().relativize(new File(currFile.getPath()).toURI()).getPath();

			byte[] buffer = new byte[1024];
			FileInputStream fileInStream = new FileInputStream(currFile);
			zipOutStream.putNextEntry(new ZipEntry(relativeFilePath));
			int length;
			while ((length = fileInStream.read(buffer)) > 0) {
				zipOutStream.write(buffer, 0, length);
			}
			zipOutStream.closeEntry();
			fileInStream.close();
		}
		
		zipOutStream.flush();
		fileOutStream.flush();
		zipOutStream.close();
		fileOutStream.close();
		
		return pathToZip.toFile();
	}

	/**
	 * Unzips a given ZIP file.
	 * @param dirToUnzipTo 
	 * @param zipFile
	 * @throws IOException
	 */
	public static void unzipFile(Path dirToUnzipTo, File zipFile) throws IOException
	{
		try {
			String source = zipFile.getAbsolutePath();
			String destination = dirToUnzipTo.toAbsolutePath().toString();
			ZipFile zf = new ZipFile(source);
			zf.extractAll(destination);
		} catch (ZipException e) {
			throw new IOException("Unzip failed: " + e.getMessage());
		}
	}

	/**
	 * Decode slashes from an URI into a file.
	 * @param id
	 * @return
	 */
	public static String decodeFilePathFromId(String id)
	{
		return id.replaceAll("%2F", "/")
				.replaceAll("%5C", "\\\\"); 
	}
	
	/**
	 * TODO make into a class
	 * Checks that all input parameters are valid (occurs, allowed value) that there are no unknown parameters and creates output parameters using default values.
	 * @param inputParameters
	 * @param parameterDefinitions
	 * @return Output parameters for an AutomationResult
	 * @throws OslcResourceException
	 */
	public static Set<ParameterInstance> checkInputParamsAndProduceOuputParams(final Set<ParameterInstance> inputParameters, final Set<ParameterDefinition> parameterDefinitions) throws OslcResourceException
	{
		Set<ParameterInstance> outputParams = new HashSet<ParameterInstance>();
		
		// loop through autoPlan defined parameters to match them with the input params
		for (ParameterDefinition definedParam : parameterDefinitions)
		{				
			// find the corresponding autoRequest input parameter to check allowed values
			boolean matched = false;
			for (ParameterInstance submittedParam : inputParameters)
			{				
				if (definedParam.getName().equals(submittedParam.getName()))
				{		
					// check if the value is allowed, and check for empty value
					Boolean validValue = true;
					Boolean emptyValueAllowed = false;
					if (definedParam.getAllowedValue() != null && definedParam.getAllowedValue().size() > 0)
					{
						validValue = false;
						for (String allowedValue : definedParam.getAllowedValue())
						{
							if (allowedValue.isEmpty())
							{
								emptyValueAllowed = true;
							}		
							
							if (allowedValue.isEmpty() && submittedParam.getValue() == null)
							{
								validValue = true;
								break;
							}
							else if (allowedValue.equals(submittedParam.getValue()))
							{
								validValue = true;
								break;
							}
						}
					}
					if (submittedParam.getValue() == null && !emptyValueAllowed)
					{
						throw new OslcResourceException("parameter " + submittedParam.getName() + " is missing a value");
					}
					if (!validValue)
					{
						throw new OslcResourceException("value '" + submittedParam.getValue() + "' not allowed for the '" + definedParam.getName() + "' parameter");
					}
					
					matched = true;
				}
			}
			// try to use the default value if no matching input param found
			if (!matched && definedParam.getDefaultValue() != null)
			{
				matched = true;

				// add the default value as an output parameter to the Automation Result
				ParameterInstance outputParameter = new ParameterInstance();
				outputParameter.setName(definedParam.getName());
				outputParameter.setValue(definedParam.getDefaultValue());
				outputParams.add(outputParameter);
			}
			
			// check parameter occurrences
			Boolean paramMissing = false;
			Link paramOccurs = definedParam.getOccurs();
			if (paramOccurs.equals(OslcValues.OSLC_OCCURS_ONE)) {
				// TODO check for more then one when there should be exactly one
				if (!matched)
					paramMissing = true;
			} else if (paramOccurs.equals(OslcValues.OSLC_OCCURS_ONEorMany)) {
				if (!matched)
					paramMissing = true;				
			} else if (paramOccurs.equals(OslcValues.OSLC_OCCURS_ZEROorONE)) {
				// TODO check for more then one when there should be max one
			} else if (paramOccurs.equals(OslcValues.OSLC_OCCURS_ZEROorMany)) {
				// not relevant
			}
			
			if (paramMissing == true)
				throw new OslcResourceException("'" + definedParam.getName() + "' input parameter missing");
		}
		
		// check that there were no unknown input parameters
		for (ParameterInstance submittedParam : inputParameters)
		{				
			boolean matched = false;
			for (ParameterDefinition definedParam : parameterDefinitions)
			{
				if (definedParam.getName().equals(submittedParam.getName()))
					matched = true;
			}
			
			if (!matched)
				throw new OslcResourceException("'" + submittedParam.getName() + "' input parameter not recognized");
		}
		
		return outputParams;
	}
	
	/**
	 * Remove all non-XML v1.0 characters from a string
	 * https://stackoverflow.com/questions/4237625/removing-invalid-xml-characters-from-a-string-in-java
	 * @param strToProcess
	 * @return String with non-XML 1.0 characters removed
	 */
	public static String removeNonXML10Chars(String strToProcess)
	{
		// XML 1.0
		// #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
		String xml10pattern = "[^"
		                    + "\u0009\r\n"
		                    + "\u0020-\uD7FF"
		                    + "\uE000-\uFFFD"
		                    + "\ud800\udc00-\udbff\udfff"
		                    + "]";
		
		return strToProcess.replace(xml10pattern,"");
	}
	
	/**
	 * Remove all non-XML v1.0 characters from a string
	 * https://stackoverflow.com/questions/25245716/remove-all-ansi-colors-styles-from-strings
	 * @param strToProcess
	 * @return String with ANSI sequences removed
	 */
	public static String removeAnsiSequences(String strToProcess)
	{
		return strToProcess.replace("/[\u001b\u009b][[()#;?]*(?:[0-9]{1,4}(?:;[0-9]{0,4})*)?[0-9A-ORZcf-nqry=><]/g","");
	}
	
	/**
	 * shortcut for calling both removeAnsiSequences and removeNonXML10Chars
	 * @param strToProcess
	 * @return
	 */
	public static String removeAnsiAndNonXML10Chars(String strToProcess)
	{
		String properString = Utils.removeAnsiSequences(strToProcess);
		properString = Utils.removeNonXML10Chars(properString);
		return properString;
	}
	
	
	
	
}
