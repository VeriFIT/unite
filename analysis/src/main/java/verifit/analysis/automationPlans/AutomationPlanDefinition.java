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
	public static void createPredefinedAutomationPlans(AutomationPlan [] autoPlans) throws StoreAccessException
	{
		for (AutomationPlan plan : autoPlans)
		{
			VeriFitAnalysisManager.createAutomationPlan(plan);
		}
	}	
//
//	/**
//	 * TODO
//	 * @throws StoreAccessException 
//	 */
//	private static void createDummyAutomationPlan() throws StoreAccessException
//	{ 
//		try {			
//			// create parameter definitions
//			ParameterDefinition arguments = new ParameterDefinition();
//			arguments.setDescription("Specify which arguments should be passed to the command line.");
//			arguments.setName("arguments");
//			arguments.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ONE)));
//			arguments.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));	
//			arguments.setHidden(false);
//			arguments.setReadOnly(false);
//			arguments.setCommandlinePosition(2);	// has to be 2+ (zero is taken by tool command; one by adapterSpecific tool params)
//
//			// special paramDefinition specifying that the SUT call command should be placed at this position
//			ParameterDefinition launchSUT = new ParameterDefinition();
//			launchSUT.setDescription("This parameter definitions tells the Automation Plan to place the SUT launch command at this command line position"); //TODO
//			launchSUT.setName("launchSUT");
//			launchSUT.setDefaultValue("");
//			launchSUT.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
//			launchSUT.setHidden(true);
//			launchSUT.setReadOnly(true);
//			launchSUT.setCommandlinePosition(3);
//			
//			// create the autoPlan
//			AutomationPlan propertiesPlan = new AutomationPlan();
//			propertiesPlan.setTitle("Dummy Tool");
//			propertiesPlan.setDescription("Used for to test the funcionality of this adapter.");
//			propertiesPlan.addParameterDefinition(arguments);
//			propertiesPlan.addParameterDefinition(launchSUT);
//			propertiesPlan.addCreator(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic")));
//			propertiesPlan.addUsesExecutionEnvironment(new Link(new URI("https://dummy_tool"))); // TODO 
//			VeriFitAnalysisManager.createAutomationPlan(propertiesPlan, VeriFitAnalysisProperties.DUMMYTOOL_PATH, "");
//	
//		} catch (URISyntaxException e) {
//			// TODO should never be thrown (URI syntax)
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * TODO
//	 * @throws StoreAccessException 
//	 */
//	private static void createAnacondaAutomationPlan() throws StoreAccessException
//	{ 
//		try {
//			// create parameter definitions
//			ParameterDefinition analyser = new ParameterDefinition();
//			analyser.setDescription("Specify what analyser to use");
//			analyser.setName("analyser");
//			analyser.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ONE)));
//			analyser.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));
//			//Analyser.setRepresentation(new Link(new URI(VeriFitAnalysisConstants.OSLC_REPRESENTATION_EITHER))); //TODO
//			analyser.setHidden(false);
//			analyser.setReadOnly(false);
//			analyser.setCommandlinePosition(2);	// has to be 2+ (zero is taken by tool command; one by adapterSpecific tool params)	
//			
//			// special paramDefinition specifying that the SUT call command should be placed at this position
//			ParameterDefinition launchSUT = new ParameterDefinition();
//			launchSUT.setDescription("This parameter definitions tells the Automation Plan to place the SUT launch command at this command line position"); //TODO
//			launchSUT.setName("launchSUT");
//			launchSUT.setDefaultValue("");
//			launchSUT.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
//			launchSUT.setHidden(true);
//			launchSUT.setReadOnly(true);
//			launchSUT.setCommandlinePosition(3);
//			
//			ParameterDefinition executionParameters = new ParameterDefinition();
//			executionParameters.setDescription("Set the execution parameters for the analyzed program. Write down all parameters as you would in a console.");
//			executionParameters.setName("executionParameters");
//			executionParameters.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
//			executionParameters.setDefaultValue("");
//			executionParameters.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));
//			executionParameters.setHidden(false);
//			executionParameters.setReadOnly(false);
//			executionParameters.setCommandlinePosition(4);
//
//			// create the autoPlan
//			AutomationPlan propertiesPlan = new AutomationPlan();
//			propertiesPlan.setTitle("ANaConDA");
//			propertiesPlan.setDescription("Analyse an SUT using ANaConDA");
//			propertiesPlan.addParameterDefinition(analyser);
//			propertiesPlan.addParameterDefinition(executionParameters);
//			propertiesPlan.addParameterDefinition(launchSUT);
//			propertiesPlan.addCreator(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic")));
//			propertiesPlan.addUsesExecutionEnvironment(new Link(new URI("https://pajda.fit.vutbr.cz/anaconda/anaconda")));
//			VeriFitAnalysisManager.createAutomationPlan(propertiesPlan, VeriFitAnalysisProperties.ANACONDA_PATH, "");
//	
//		} catch (URISyntaxException e) {
//			// TODO should never be thrown (URI syntax)
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * TODO
//	 * @throws StoreAccessException 
//	 */
//	private static void createPerrunAutomationPlan() throws StoreAccessException
//	{ 
//		try {			
//			// create parameter definitions
//			ParameterDefinition command = new ParameterDefinition();
//			command.setDescription("Specify a perun command to run");
//			command.setName("command");
//			command.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ONE)));
//			command.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));	
//			command.setHidden(false);
//			command.setReadOnly(false);
//			command.setCommandlinePosition(2);	// has to be 2+ (zero is taken by tool command; one by adapterSpecific tool params)
//			
//			// create the autoPlan
//			AutomationPlan propertiesPlan = new AutomationPlan();
//			propertiesPlan.setTitle("Perun");
//			propertiesPlan.setDescription("Analyse an SUT using Perun");
//			propertiesPlan.addParameterDefinition(command);
//			propertiesPlan.addCreator(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic")));
//			propertiesPlan.addUsesExecutionEnvironment(new Link(new URI("https://github.com/tfiedor/perun")));
//			VeriFitAnalysisManager.createAutomationPlan(propertiesPlan, VeriFitAnalysisProperties.PERUN_PATH, "-nc --no-pager");
//	
//		} catch (URISyntaxException e) {
//			// TODO should never be thrown (URI syntax)
//			e.printStackTrace();
//		}
//	}
//
//	
//	/**
//	 * TODO
//	 * @throws StoreAccessException 
//	 */
//	private static void createHiliteAutomationPlan() throws StoreAccessException
//	{ 
//		try {			
//			// client parameters
//			ParameterDefinition arguments = new ParameterDefinition();
//			arguments.setDescription("Specify which arguments should be passed to Hilite on the command line.");
//			arguments.setName("arguments");
//			arguments.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
//			arguments.setDefaultValue("");
//			arguments.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));	
//			arguments.setHidden(false);
//			arguments.setReadOnly(false);
//			arguments.setCommandlinePosition(3);	// has to be 2+ (zero is taken by tool command; one by adapterSpecific tool params)
//			
//			// special param. definitions for the adapter
//			ParameterDefinition launchSUT = new ParameterDefinition();
//			launchSUT.setDescription("This parameter definitions tells the Automation Provider to place the SUT launch command at this command line position"); //TODO
//			launchSUT.setName("launchSUT");
//			launchSUT.setDefaultValue("");
//			launchSUT.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
//			launchSUT.setHidden(true);
//			launchSUT.setReadOnly(true);
//			launchSUT.setCommandlinePosition(2); // has to be 2+ (zero is taken by tool command; one by adapterSpecific tool params)
//			
//			ParameterDefinition oneInstanceOnly = new ParameterDefinition();
//			oneInstanceOnly.setDescription("This parameter definitions tells the Automation Provider to only allow one Automation Request at a time to execute this Automation Plan. Requests created while an instance is already running will be placed in a queue.");
//			oneInstanceOnly.setName("oneInstanceOnly");
//			oneInstanceOnly.setDefaultValue("True");
//			oneInstanceOnly.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
//			oneInstanceOnly.setHidden(true);
//			oneInstanceOnly.setReadOnly(true);
//			
//			// create the autoPlan
//			AutomationPlan propertiesPlan = new AutomationPlan();
//			propertiesPlan.setTitle("Hilite");
//			propertiesPlan.setDescription("Analyse an SUT using Hilite");
//			propertiesPlan.addParameterDefinition(arguments);
//			propertiesPlan.addParameterDefinition(launchSUT);
//			propertiesPlan.addParameterDefinition(oneInstanceOnly);
//			propertiesPlan.addCreator(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic")));
//			propertiesPlan.addUsesExecutionEnvironment(new Link(new URI("https://hilite.TODO"))); // TODO hilite url
//			VeriFitAnalysisManager.createAutomationPlan(propertiesPlan, VeriFitAnalysisProperties.HILITE_PATH, "");
//	
//		} catch (URISyntaxException e) {
//			// TODO should never be thrown (URI syntax)
//			e.printStackTrace();
//		}
//	}
}
