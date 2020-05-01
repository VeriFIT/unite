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
	public static void createPredefinedAutomationPlans() throws StoreAccessException
	{
		createAnacondaAutomationPlan();
		createPerrunAutomationPlan();
	}	
	
	/**
	 * Check whether the predefined automation plans exist
	 * @return True if they exist, False if not 
	 */
	public static boolean checkPredefinedAutomationPlans()
	{
    	if (VeriFitAnalysisManager.getAutomationPlan(null, VeriFitAnalysisConstants.AUTOMATION_PROVIDER_ID, "0") == null)
    		return false;
    	else
    		return true;
	}

	/**
	 * TODO
	 * @throws StoreAccessException 
	 */
	private static void createAnacondaAutomationPlan() throws StoreAccessException
	{ 
		try {
			// create parameter definitions
			ParameterDefinition analyser = new ParameterDefinition();
			analyser.setDescription("Specify what analyser to use");
			analyser.setName("analyser");
			analyser.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ONE)));
			analyser.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));
			//Analyser.setRepresentation(new Link(new URI(VeriFitAnalysisConstants.OSLC_REPRESENTATION_EITHER))); //TODO
			analyser.setHidden(false);
			analyser.setReadOnly(false);
			analyser.setCommandlinePosition(1);			
			
			// special paramDefinition specifying that the SUT call command should be placed at this position
			ParameterDefinition callSUT = new ParameterDefinition();
			callSUT.setDescription("This parameter definitions tells the Automation Plan to place the SUT call command at this command line position"); //TODO
			callSUT.setName("callSUT");
			callSUT.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
			callSUT.setHidden(true);
			callSUT.setReadOnly(true);
			callSUT.setCommandlinePosition(2);
			
			ParameterDefinition executionParameters = new ParameterDefinition();
			executionParameters.setDescription("Set the execution parameters for the analyzed program. Write down all parameters as you would in a console.");
			executionParameters.setName("executionParameters");
			executionParameters.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
			executionParameters.setDefaultValue("");
			executionParameters.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));
			executionParameters.setHidden(false);
			executionParameters.setReadOnly(false);
			executionParameters.setCommandlinePosition(3);

			
			// create the autoPlan
			AutomationPlan propertiesPlan = new AutomationPlan();
			propertiesPlan.setTitle("ANaConDA");
			propertiesPlan.setDescription("Analyse an SUT using ANaConDA");
			propertiesPlan.addParameterDefinition(analyser);
			propertiesPlan.addParameterDefinition(executionParameters);
			propertiesPlan.addParameterDefinition(callSUT);
			propertiesPlan.addCreator(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic")));
			propertiesPlan.addUsesExecutionEnvironment(new Link(new URI("https://pajda.fit.vutbr.cz/anaconda/anaconda")));
			VeriFitAnalysisManager.createAutomationPlan(propertiesPlan, VeriFitAnalysisProperties.ANACONDA_PATH);
	
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
	}

	/**
	 * TODO
	 * @throws StoreAccessException 
	 */
	private static void createPerrunAutomationPlan() throws StoreAccessException
	{ 
		try {
			// create parameter definitions
			
			// create parameter definitions
			ParameterDefinition command = new ParameterDefinition();
			command.setDescription("Specify a perun command to run");
			command.setName("command");
			command.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ONE)));
			command.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));	
			command.setHidden(false);
			command.setReadOnly(false);
			command.setCommandlinePosition(2);	// has to be 1+ (zero is taken by tool command)
			
			ParameterDefinition adapterSpecific = new ParameterDefinition();
			adapterSpecific.setDescription("Parameters needed by the adapter in order for the tools output to be xml readable.");
			adapterSpecific.setName("adapterSpecific");
			adapterSpecific.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ONE)));
			adapterSpecific.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));	
			adapterSpecific.setHidden(true);
			adapterSpecific.setReadOnly(true);
			adapterSpecific.setDefaultValue("-nc");
			adapterSpecific.setCommandlinePosition(1);	// has to be 1+ (zero is taken by tool command)
			
			// create the autoPlan
			AutomationPlan propertiesPlan = new AutomationPlan();
			propertiesPlan.setTitle("Perun");
			propertiesPlan.setDescription("Analyse an SUT using Perun");
			propertiesPlan.addParameterDefinition(command);
			propertiesPlan.addParameterDefinition(adapterSpecific);
			propertiesPlan.addCreator(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic")));
			propertiesPlan.addUsesExecutionEnvironment(new Link(new URI("https://github.com/tfiedor/perun")));
			VeriFitAnalysisManager.createAutomationPlan(propertiesPlan, VeriFitAnalysisProperties.PERUN_PATH);
	
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
	}
}
