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

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.store.StoreAccessException;

import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisConstants;
import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.exceptions.OslcResourceException;
import cz.vutbr.fit.group.verifit.oslc.analysis.properties.VeriFitAnalysisProperties;

/**
 * This class defines AutomationPlans predefined for the adapter.
 * @author od42
 *
 */
public class PredefinedAutomationPlanDefinition {

	/**
	 * Automation Plan for a Dummy tool which is used to test the adapter.
	 */
	public static AutomationPlan getDummyAutomationPlanDefinition()
	{ 
		try {			
			// create parameter definitions
			ParameterDefinition arguments = new ParameterDefinition();
			arguments.setDescription("Specify which arguments should be passed to the command line.");
			arguments.setName("arguments");
			arguments.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ONE)));
			arguments.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));	
			arguments.setCommandlinePosition(1);
	
			// special paramDefinition specifying that the SUT call command should be placed at this position
			ParameterDefinition launchSUT = new ParameterDefinition();
			launchSUT.setDescription("This parameter definitions tells the Automation Plan to place the SUT launch command at this command line position"); //TODO
			launchSUT.setName("launchSUT");
			launchSUT.setDefaultValue("2");
			launchSUT.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
			
			// special paramDefinition specifying that the SUT call command should be placed at this position
			ParameterDefinition SUTbuildCommand = new ParameterDefinition();
			SUTbuildCommand.setDescription("This parameter definitions tells the Automation Plan to place the SUT launch command at this command line position"); //TODO
			SUTbuildCommand.setName("SUTbuildCommand");
			SUTbuildCommand.setDefaultValue("3");
			SUTbuildCommand.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
	
			// create the autoPlan
			AutomationPlan propertiesPlan = new AutomationPlan();
			propertiesPlan.setIdentifier("dummy");
			propertiesPlan.setTitle("Dummy Tool");
			propertiesPlan.setDescription("Used for to test the funcionality of this adapter.");
			propertiesPlan.addParameterDefinition(arguments);
			propertiesPlan.addParameterDefinition(launchSUT);
			propertiesPlan.addParameterDefinition(SUTbuildCommand);
			propertiesPlan.addCreator(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic")));
			propertiesPlan.addUsesExecutionEnvironment(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic/oslc-generic-analysis/-/blob/master/analysis/tests/dummy_tool.sh"))); // TODO

			return propertiesPlan;
	
		} catch (URISyntaxException e) {
			// TODO should never be thrown
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * AutomationPlan configuration for the Dummy tool (would be in a .properties file for normal tools).
	 * @return
	 */
	public static AutomationPlanConfManager.AutomationPlanConf getDummyAutomationPlanConf()
	{
		return new AutomationPlanConfManager.AutomationPlanConf(
						VeriFitAnalysisProperties.DUMMYTOOL_PATH,
						"",
						false);
	}
}
