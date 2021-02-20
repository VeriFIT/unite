/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.compilation.automationPlans;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.store.StoreAccessException;

import cz.vutbr.fit.group.verifit.oslc.compilation.VeriFitCompilationConstants;
import cz.vutbr.fit.group.verifit.oslc.compilation.VeriFitCompilationManager;
import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;

/**
 * This class defines AutomationPlans predefined for the adapter.
 * @author od42
 *
 */
public class AutomationPlanDefinition {
	
	/**
	 * Creates all the predefined AutomationPlans making them available in the adapter catalog
	 * @throws StoreAccessException 
	 */
	public static void createPredefinedAutomationPlans() throws StoreAccessException
	{
		createSutDeployAutomationPlan();
	}	
	
	/**
	 * Check whether the predefined automation plans exist
	 * @return True if they exist, False if not 
	 */
	public static boolean checkPredefinedAutomationPlans()
	{
		try {
    	if (VeriFitCompilationManager.getAutomationPlan(null, "0") == null)
    		return false;
    	else
    		return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * TODO
	 * @throws StoreAccessException 
	 */
	private static void createSutDeployAutomationPlan() throws StoreAccessException
	{ 
		try {
			// create parameter definitions
			ParameterDefinition sourceGitUrl = new ParameterDefinition();
			sourceGitUrl.setDescription("The SUT will be retrieved from a Git repository.");
			sourceGitUrl.setName("sourceGit");
			//sourceGitUrl.setTitle("Definition"); //TODO
			sourceGitUrl.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			//sourceGitUrl.setReadOnly(false);
			sourceGitUrl.addValueType(OslcValues.OSLC_VAL_TYPE_STRING); // TODO change to URI
			//sourceGitUrl.setRepresentation(new Link(new URI(VeriFitCompilationConstants.OSLC_REPRESENTATION_EITHER)));
			
			
			/*
			 * SUT fetch methods
			 */
			ParameterDefinition sourceUrl = new ParameterDefinition();
			sourceUrl.setDescription("The SUT will be downloaded from a URL. "
					+ "Example: https://pajda.fit.vutbr.cz/xvasic/oslc-generic-analysis.git");
			sourceUrl.setName("sourceUrl");
			sourceUrl.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			sourceUrl.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			
			ParameterDefinition sourceBase64 = new ParameterDefinition();
			sourceBase64.setDescription("The SUT is encoded in base64 as this parameter's value. "
					+ "IMPORTANT the value needs to specify a filename (to match with buildCommand). "
					+ "The filename should be on the first line of the value, then the base64 encoded file as the second line "
					+ "(separated by a \"\\n\"");
			sourceBase64.setName("sourceBase64");
			sourceBase64.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			sourceBase64.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			
			ParameterDefinition sourceFilePath = new ParameterDefinition();
			sourceFilePath.setDescription("The SUT will be copied from a path in the filesystem.");
			sourceFilePath.setName("sourceFilePath");
			sourceFilePath.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			sourceFilePath.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			
			ParameterDefinition unpackZip = new ParameterDefinition();
			unpackZip.setDescription("Set this parameter to \"true\" to have the adapter unpack the SUT using ZIP after fetching it."); 
			unpackZip.setName("unpackZip");
			unpackZip.setDefaultValue("false");
			unpackZip.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			unpackZip.addValueType(OslcValues.OSLC_VAL_TYPE_BOOL);
			
			
			/*
			 * SUT parameters definition
			 */
			ParameterDefinition buildCommand = new ParameterDefinition();
			buildCommand.setDescription("How to build the SUT. "
					+ "The specified command will be launched from the root directory of the downloaded SUT. "
					+ "Examples: make | ./build.sh | gcc -g -o my_sut. "
					+ "If this command is missing or empty then compilation will not be performed (e.g. for static analysis tools)"); //TODO
			buildCommand.setName("buildCommand");
			buildCommand.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			buildCommand.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);

			ParameterDefinition launchCommand = new ParameterDefinition();
			launchCommand.setDescription("How to launch the SUT once its build. "
					+ "The specified command will be launched from the root directory of the downloaded SUT. "
					+ "Examples: make run | ./run.sh | ./my_sut"); 
			launchCommand.setName("launchCommand");
			launchCommand.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			launchCommand.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
						
			
			/*
			 * Utility parameters
			 */
			ParameterDefinition timeout = new ParameterDefinition();
			timeout.setDescription("Timeout for the compilation. Zero means no timeout.");
			timeout.setName("timeout");
			timeout.setOccurs(OslcValues.OSLC_OCCURS_ZEROorONE);
			timeout.addValueType(OslcValues.OSLC_VAL_TYPE_INTEGER);
			timeout.setDefaultValue("0");
			
			
			
			// create the autoPlan
			AutomationPlan propertiesPlan = new AutomationPlan();
			propertiesPlan.setTitle("SUT Deploy");
			propertiesPlan.setDescription("Download and compile an SUT on the server so it can be executed later."
					+ "Use exactly one of the \"source.*\" parameters.");
			propertiesPlan.addParameterDefinition(sourceUrl);
			propertiesPlan.addParameterDefinition(sourceGitUrl);
			propertiesPlan.addParameterDefinition(sourceBase64);
			propertiesPlan.addParameterDefinition(sourceFilePath);			
			propertiesPlan.addParameterDefinition(unpackZip);
			propertiesPlan.addParameterDefinition(buildCommand);
			propertiesPlan.addParameterDefinition(launchCommand);
			propertiesPlan.addParameterDefinition(timeout);
			propertiesPlan.addCreator(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic")));
			VeriFitCompilationManager.createAutomationPlan(propertiesPlan);
	
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
	}
}
