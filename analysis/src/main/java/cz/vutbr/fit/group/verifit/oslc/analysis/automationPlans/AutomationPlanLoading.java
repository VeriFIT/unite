/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans;

import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.store.StoreAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.group.verifit.oslc.OslcValues;
import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager.AutomationPlanConf;
import cz.vutbr.fit.group.verifit.oslc.shared.exceptions.OslcResourceException;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;
import cz.vutbr.fit.group.verifit.oslc.analysis.properties.VeriFitAnalysisProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AutomationPlanLoading {

	private final Map<String,AutomationPlan> automationPlans; // AutomationPlans indexed by their IDs
	private final Collection<AutomationPlanConf> autoPlanConfs;

	private final Logger log = LoggerFactory.getLogger(VeriFitAnalysisManager.class);
    
    public AutomationPlanLoading() {
		this.automationPlans = new HashMap<String,AutomationPlan>();
		this.autoPlanConfs = new ArrayList<AutomationPlanConf>();
	}

    public Collection<AutomationPlanConf> getAutoPlanConfs() {
		return autoPlanConfs;
	}
    
	/**
	 * Loads all AutomationPlans defined in the "autoPlansDefPath" directory.
	 * Does not persist them in the triplestore! (persistAutomationPlans does that)
     * Each Automation Plan is defined by an .rdf file and one .properties file of the same name.
     * The .rdf file holds the base of the Automation Plan resource and the .properties file specifies other
     * configuration (e.g. path to tool executable etc.)
     * @throws StoreAccessException When there was an error accessing the triplestore
     * @throws Exception When there was a loading error 
     */
    public void loadAutomationPlans(File autoPlansDefPath)
    {
        // load automation plans one by one from the user configuration
        File[] autoPlansDefFiles = findAutomationPlanDefinitionFiles(autoPlansDefPath);
        if (autoPlansDefFiles == null)
        {
            log.warn("Loading AutomationPlans: Did not find any AutomationPlan definitions in "
                + autoPlansDefPath.getPath()
                + "\nAutomationPlans need to be created for your analysis tools."
                + "Have a look at the ExampleTool.rdf and ExampleTool.properties files."); // TODO update
        } else {
            for (File autoPlanDef : autoPlansDefFiles)
            {
        		try {
                	loadOneAutomationPlan(autoPlanDef);
        		} catch (Exception e){
            		System.out.println("ERROR: Can not load analysis tool Automation Plan from file \"" + autoPlanDef.getName() + "\": " + e.getMessage());
        		}
            }
        }
    }

    /**
     * Saves all currently loaded automation plans into the triplestore
     * @param automationPlanConfigurations 
     * @throws StoreAccessException
     * @throws OslcResourceException
     */
    public void persistAutomationPlans(Collection<AutomationPlanConf> automationPlanConfigurations) throws StoreAccessException, OslcResourceException
    {        
    	for (AutomationPlan autoPlan : this.automationPlans.values())
    	{
    		
	        // find conf for the auto plan
    		AutomationPlanConf conf = null;
    		for (AutomationPlanConf c : automationPlanConfigurations)
    		{
    			if (c.getIdentifier().equals(autoPlan.getIdentifier()))
    				conf = c;
    		}
    		
    		if (conf == null)
    			throw new OslcResourceException("this should never happen -- auto plan loading: couldnt find conf for auto plan");
    		
    		// create the Automation Plan
	        try {
	        	autoPlan = VeriFitAnalysisManager.createAutomationPlan(autoPlan, conf);
	        } catch (OslcResourceException e) {
	        	throw new OslcResourceException("Failed to create Automation Plan: " + e.getMessage());
	        }
        }
	}

    /**
     * Loads the .properties and .rdf of an Automation Plan. Saves the automation plan in to a loader variable and returns the auto plan configuration
     * @param autoPlanDef
     * @throws Exception
     */
    private void loadOneAutomationPlan(File autoPlanDef) throws Exception
    {
        // load the matching properties file

        // load the .rdf file
        AutomationPlan newAutoPlan = loadAutomationPlanRdfConfiguration(autoPlanDef);
        if (newAutoPlan.getIdentifier() == null || newAutoPlan.getIdentifier().isEmpty())
        {
        	throw new Exception("Loading AutomationPlan: definition in file \"" + autoPlanDef.getName() + "\" is missing an identifier or identifier is empty");
        }
        
        // add value type string to all param definitions that have none
        for (ParameterDefinition paramDef : newAutoPlan.getParameterDefinition())
        {
        	if (paramDef.getValueType() == null)
        		paramDef.setValueType(new HashSet<Link>());
        	if (paramDef.getValueType().isEmpty())
        		paramDef.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);	// TODO maybe copy instead to avoid accidental modification?
        }
        
        // make sure special parameter definitions are correct
    	for (ParameterDefinition paramDef : newAutoPlan.getParameterDefinition())
    	{
    		// check launchSUT -- needs a command line position and value type bool
    		if (paramDef.getName() != null && paramDef.getName().equals("launchSUT"))
    		{
    			if (paramDef.getCommandlinePosition() == null)
                	throw new Exception("Loading AutomationPlan: definition of Automation Plan \"" + newAutoPlan.getIdentifier() + "\" contains a \"launchSUT\" ParameterDefinition which is missing a fit:commandlinePosition property.");  				   				
    			
    			// correct the type if needed
    			if (!paramDef.getValueType().iterator().next().getValue().equals(
    					OslcValues.OSLC_VAL_TYPE_BOOL.getValue()
					))
    			{
    				paramDef.setValueType(new HashSet<Link>());
    				paramDef.addValueType(OslcValues.OSLC_VAL_TYPE_BOOL);
                	log.warn("Loading AutomationPlan: definition of Automation Plan \"" + newAutoPlan.getIdentifier() + "\" contains a \"launchSUT\" ParameterDefinition with an incorrect valuetype -- autocorrected to http://www.w3.org/2001/XMLSchema#boolean");
    			}
    		}

    		// check SUTbuildCommand -- needs a command line position and value type bool
    		if (paramDef.getName() != null && paramDef.getName().equals("SUTbuildCommand"))
    		{
    			if (paramDef.getCommandlinePosition() == null)
                	throw new Exception("Loading AutomationPlan: definition of Automation Plan \"" + newAutoPlan.getIdentifier() + "\" contains a \"SUTbuildCommand\" ParameterDefinition which is missing a fit:commandlinePosition property.");  				   				
    			
    			// correct the type if needed
    			if (!paramDef.getValueType().iterator().next().getValue().equals(
    					OslcValues.OSLC_VAL_TYPE_BOOL.getValue()
					))
    			{
    				paramDef.setValueType(new HashSet<Link>());
    				paramDef.addValueType(OslcValues.OSLC_VAL_TYPE_BOOL);
                	log.warn("Loading AutomationPlan: definition of Automation Plan \"" + newAutoPlan.getIdentifier() + "\" contains a \"SUTbuildCommand\" ParameterDefinition with an incorrect valuetype -- autocorrected to http://www.w3.org/2001/XMLSchema#boolean");
    			}
    		}
    	}
    	
        
        // check that the automation plan configuration is correct
        checkAutomationPlanConfiguration(newAutoPlan);
        
        // load the .properties file
        String autoPlanPropsName = autoPlanDef.getName().replace(".rdf", ".properties");
        File autoPlanPropsFile = autoPlanDef.toPath().getParent().resolve(autoPlanPropsName).toFile();
        AutomationPlanConf newAutoPlanConf = loadAutomationPlanPropertiesConfiguration(newAutoPlan.getIdentifier(), autoPlanPropsFile);
        
        // the dummy tool needs special treatment to make it portable (win vs linux)
        if (newAutoPlanConf.getIdentifier().equals("dummy")) {
        	newAutoPlanConf.setToolLaunchCommand(VeriFitAnalysisProperties.DUMMYTOOL_PATH);
        }
        
        // check for duplicit Automation Plans
        if (this.automationPlans.containsKey(newAutoPlan.getIdentifier()))
		{
        	throw new Exception("Loading AutomationPlan: Identifier \"" + newAutoPlan.getIdentifier() + "\" is already taken.");
		}
        else
        {
            // save the AutomationPlan in the local Map to persist later
            this.automationPlans.put(newAutoPlan.getIdentifier(), newAutoPlan);            
        	this.autoPlanConfs.add(newAutoPlanConf);
        }       
    }
    
    /**
     * Loads an AutomationPlan out of its configuration .rdf file.
     * @param autoPlanDefFile
     * @return Loaded AutomationPlan
     * @throws Exception If anything goes wrong (file error, parse error)
     */
    private AutomationPlan loadAutomationPlanRdfConfiguration(File autoPlanDefFile) throws Exception
    {
        AutomationPlan[] parsedResources = new AutomationPlan[0];
        try {
            parsedResources = Utils.parseResourcesFromXmlFile(autoPlanDefFile, AutomationPlan.class);	// there should be only one
        } catch (FileNotFoundException e) {
        	throw new Exception("Failed to open definition .rdf file ("
                    + autoPlanDefFile.getName() + "): " + e.getMessage());
        } catch (Exception e) {
        	throw new Exception("Loading AutomationPlan: Failed to parse definition .rdf file: ("
                    + autoPlanDefFile.getName() + "): " + e.getMessage());
        }
        if (parsedResources == null || parsedResources.length != 1) {
        	throw new Exception("Loading AutomationPlan: Parsed definition .rdf file ("
                    + autoPlanDefFile.getName() + ") did not contain exactly one Automation Plan resource");
        }
        
        return parsedResources[0]; // there is only one
    }
    
    /**
     * Loads AutomationPlan properties out of a .properties file
     * @param autoPlanId 
     * @param autoPlanPropsFile
     * @return AutomationPlanConf object with the loaded properties
     * @throws Exception If anything goes wrong (file error, parse error, property missing)
     */
    private AutomationPlanConf loadAutomationPlanPropertiesConfiguration(String autoPlanId, File autoPlanPropsFile) throws Exception
    {
    	// Open .the properties file
        Properties autoPlanProps = new Properties();
        try {
            autoPlanProps.load(new FileInputStream(autoPlanPropsFile));
        } catch (IOException e) {
        	throw new Exception("Failed to open matching .properties file ("
                    + autoPlanPropsFile.getName() + "): " + e.getMessage());
        }
        
        // load the properties
        String toolLaunchCommand = autoPlanProps.getProperty("toolLaunchCommand");
        if (toolLaunchCommand == null)
        {
        	throw new Exception("The matching .properties file ("
                    + autoPlanPropsFile.getName() + ") is missing a toolLaunchCommand");
        }
        String toolSpecificArgs = autoPlanProps.getProperty("toolSpecificArgs");
        toolSpecificArgs = (toolSpecificArgs == null ? "" : toolSpecificArgs); // default value
        Boolean OneInstanceOnly = Boolean.parseBoolean(autoPlanProps.getProperty("oneInstanceOnly"));
        OneInstanceOnly = (OneInstanceOnly == null ? false : OneInstanceOnly); // default value
        
        return new AutomationPlanConf(
        		autoPlanId,
                toolLaunchCommand,
                toolSpecificArgs,
                OneInstanceOnly
        );
    }

    /**
     * Finds all .rdf files in the AutomationPlan definitions directory
     * @param inFolder	Folder to search in
     * @return Collection of .rdf files
     */
    private File[] findAutomationPlanDefinitionFiles(File inFolder)
    {
        File autoPlansDefFolder = inFolder;
        File[] autoPlanDefFiles = autoPlansDefFolder.listFiles(
                (dir, name) -> name.endsWith(".rdf")
            );

        return autoPlanDefFiles;
    }
    

    /**
     * Check that the Automation Plan is well defined
     * @param autoPlan
     * @throws Exception with description if the check fails
     */
    private void checkAutomationPlanConfiguration(AutomationPlan autoPlan) throws Exception
    {

    	/*
    	 * check that each parameter definition has properties: name, one value type, and occurs
    	 */
    	for (ParameterDefinition paramDef : autoPlan.getParameterDefinition())
    	{
    		// check name
    		if (paramDef.getName() == null || paramDef.getName().isEmpty())
    		{
            	throw new Exception("Loading AutomationPlan: definition of Automation Plan \"" + autoPlan.getIdentifier() + "\" contains a ParameterDefinition which is missing a name or its name is empty");
    		}
    		// check value type
    		if (paramDef.getValueType() == null || paramDef.getValueType().size() != 1)
    		{
            	throw new Exception("Loading AutomationPlan: definition of Automation plan \"" + autoPlan.getIdentifier() + "\" contains a ParameterDefinition \"" + paramDef.getName() + "\" which is missing a value type or has multiple value types.");
            	// TODO check for valid value types
    		}
    		// check occurs
    		if (paramDef.getOccurs() == null || (
    				!paramDef.getOccurs().getValue().equals(OslcValues.OSLC_OCCURS_ONE.getValue())        &&
    				!paramDef.getOccurs().getValue().equals(OslcValues.OSLC_OCCURS_ONEorMany.getValue())  &&
    				!paramDef.getOccurs().getValue().equals(OslcValues.OSLC_OCCURS_ZEROorMany.getValue()) &&
    				!paramDef.getOccurs().getValue().equals(OslcValues.OSLC_OCCURS_ZEROorONE.getValue())   )
    			)
    		{
            	throw new Exception("Loading AutomationPlan: definition of Automation plan \"" + autoPlan.getIdentifier() + "\" contains a ParameterDefinition \"" + paramDef.getName() + "\" which is missing a \"occurs\" property or the property has an invalid value.");
    		}
    	}
    	
    	
    	/*
    	 * check that there are no duplicit parameter definitions
    	 */
    	
    	// convert to list for fixed order
    	List<ParameterDefinition> listParams = new ArrayList<ParameterDefinition>(autoPlan.getParameterDefinition());
    	for (int i = 0; i < listParams.size(); i++)
    	{    		
    		// then loop over all other param defs to check duplicate names
    		for(int j = i+1; j < listParams.size(); j++)
    		{
    			if (listParams.get(i).getName().equals(listParams.get(j).getName()))
    	        	throw new Exception("Loading AutomationPlan: definition of Automation plan \"" + autoPlan.getIdentifier() + "\" has ParameterDefinitions with duplicit names \"" + listParams.get(i).getName() + "\"");
    		}
    	}
    	
    	// TODO do other configuration checks
    	
    }
}

