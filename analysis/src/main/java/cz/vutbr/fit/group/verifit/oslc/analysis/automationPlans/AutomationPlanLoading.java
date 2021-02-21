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
import org.eclipse.lyo.store.StoreAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager.AutomationPlanConf;
import cz.vutbr.fit.group.verifit.oslc.shared.exceptions.OslcResourceException;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;
import cz.vutbr.fit.group.verifit.oslc.analysis.properties.VeriFitAnalysisProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import static cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.PredefinedAutomationPlanDefinition.getDummyAutomationPlanConf;
import static cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.PredefinedAutomationPlanDefinition.getDummyAutomationPlanDefinition;

public class AutomationPlanLoading {

    private static final Logger log = LoggerFactory.getLogger(VeriFitAnalysisManager.class);
    
    /**
     * Each Automation Plan is defined by an .rdf file and one .properties file of the same name.
     * The .rdf file holds the base of the Automation Plan resource and the .properties file specifies other
     * configuration (e.g. path to tool executable etc.)
     * @throws StoreAccessException When there was an error accessing the triplestore
     * @throws Exception When there was a loading error 
     */
    public static void loadAutomationPlans() throws StoreAccessException, Exception
    {
        // create the predefined AutomationPlans from the source code
        createPredefinedAutomationPlans();

        // load automation plans one by one from the user configuration
        File[] autoPlansDefFiles = findAutomationPlanDefinitionFiles();
        if (autoPlansDefFiles == null)
        {
            log.warn("Loading AutomationPlans: Did not find any AutomationPlan definitions in "
                + VeriFitAnalysisProperties.AUTOPLANS_DEF_PATH
                + "\nAutomationPlans need to be created for your analysis tools."
                + "Have a look at the ExampleTool.rdf and ExampleTool.properties files.");
  
        }
        else {
            for (File autoPlanDef : autoPlansDefFiles)
            {
                if (autoPlanDef.getName().equals("ExampleTool.rdf"))
                    continue; // skip the example
                else
                	loadOneAutomationPlan(autoPlanDef);
            }
        }
    }
    
    private static void loadOneAutomationPlan(File autoPlanDef) throws StoreAccessException,  Exception
    {
        // load the matching properties file

        // load the .rdf file
        AutomationPlan newAutoPlan = loadAutomationPlanRdfConfiguration(autoPlanDef);
        if ("dummy".equals(newAutoPlan.getIdentifier())) // dummy is taken by the test analysis tool
        {
        	throw new Exception("User defined AutomationPlan with identifier" +
                    " \"dummy\" can not be created. Identifier is reserved for the internal testing tool.");
        }
        
        // load the .properties file
        String autoPlanPropsName = autoPlanDef.getName().replace(".rdf", ".properties");
        File autoPlanPropsFile = autoPlanDef.toPath().getParent().resolve(autoPlanPropsName).toFile();
        AutomationPlanConf newAutoPlanConf = loadAutomationPlanPropertiesConfiguration(autoPlanPropsFile);

        // create the Automation Plan
        try {
            newAutoPlan = VeriFitAnalysisManager.createAutomationPlan(newAutoPlan);	//// commitment point
        } catch (OslcResourceException e) {
        	throw new Exception("Failed to create Automation Plan: " + e.getMessage());
        }
        
        // save the properties
        AutomationPlanConfManager autoPlanConfManager = AutomationPlanConfManager.getInstance();
        autoPlanConfManager.addAutoPlanConf(
                newAutoPlan.getIdentifier(),
                newAutoPlanConf
                );
    }
    
    /**
     * Loads an AutomationPlan out of its configuration .rdf file.
     * @param autoPlanDefFile
     * @return Loaded AutomationPlan
     * @throws Exception If anything goes wrong (file error, parse error)
     */
    private static AutomationPlan loadAutomationPlanRdfConfiguration(File autoPlanDefFile) throws Exception
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
                    + autoPlanDefFile.getName() + ") did not contain exactly on Automation Plan resource");
        }
        
        return parsedResources[0]; // there is only one
    }
    
    /**
     * Loads AutomationPlan properties out of a .properties file
     * @param autoPlanPropsFile
     * @return AutomationPlanConf object with the loaded properties
     * @throws Exception If anything goes wrong (file error, parse error, property missing)
     */
    private static AutomationPlanConfManager.AutomationPlanConf loadAutomationPlanPropertiesConfiguration(File autoPlanPropsFile) throws Exception
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
        Boolean OneInstanceOnly = Boolean.parseBoolean(autoPlanProps.getProperty("OneInstanceOnly"));
        OneInstanceOnly = (OneInstanceOnly == null ? false : OneInstanceOnly); // default value
        
        return new AutomationPlanConfManager.AutomationPlanConf(
                toolLaunchCommand,
                toolSpecificArgs,
                OneInstanceOnly
        );
    }

    /**
     * Creates all the predefined AutomationPlans making them available in the adapter catalog
     * @throws StoreAccessException 
     * @throws Exception When the predefined AutomationPlan was not defined properly (should not happen)
     */
    private static void createPredefinedAutomationPlans() throws StoreAccessException, Exception
    {
        try {
            // create the dummy tool AutoPlan (independent of xml config)
            VeriFitAnalysisManager.createAutomationPlan(PredefinedAutomationPlanDefinition.getDummyAutomationPlanDefinition());

            // save the properties
            AutomationPlanConfManager.getInstance()
                    .addAutoPlanConf(
                        getDummyAutomationPlanDefinition().getIdentifier(),
                        getDummyAutomationPlanConf()
                    );

        } catch (OslcResourceException e) {
            throw new Exception("Failed to create dummy tool automation plan" + e.getMessage());
        }
    }

    /**
     * Finds all .rdf files in the AutomationPlan definitions directory
     * @return Collection of .rdf files
     */
    private static File[] findAutomationPlanDefinitionFiles()
    {
        File autoPlansDefFolder = new File(VeriFitAnalysisProperties.AUTOPLANS_DEF_PATH);
        File[] autoPlanDefFiles = autoPlansDefFolder.listFiles(
                (dir, name) -> name.endsWith(".rdf")
            );

        return autoPlanDefFiles;
    }
}
