/**
 * Author: Ondrej Vasicek
 * VUT-login: xvasic25
 * VUT-email: xvasic25@stud.fit.vutbr.cz
 * 
 * This file is part of my Bachelor's thesis submitted for my IT degree
 * at the Brno University of Technology, Faculty of Information Technology.
 * 
 * This authors header is included only in the files I have personally modified
 * (hopefully did not miss any)
 */

package verifit.analysis.automationPlans;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.store.StoreAccessException;

import verifit.analysis.VeriFitAnalysisConstants;
import verifit.analysis.VeriFitAnalysisManager;
import verifit.analysis.VeriFitAnalysisProperties;
import verifit.analysis.exceptions.OslcResourceException;

/**
 * This class defines AutomationPlans predefined for the adapter.
 * @author od42
 *
 */
public class AutomationPlanDefinition {

	/**
	 * TODO
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

	public static AutomationPlanConfManager.AutomationPlanConf getDummyAutomationPlanConf()
	{
		return new AutomationPlanConfManager.AutomationPlanConf(
						VeriFitAnalysisProperties.DUMMYTOOL_PATH,
						"",
						false);
	}
}
