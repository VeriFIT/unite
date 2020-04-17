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
		createSutDeployAutomationPlan();
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
	private static void createSutDeployAutomationPlan() throws StoreAccessException
	{ 
		try {
			// create parameter definitions
			ParameterDefinition analyser = new ParameterDefinition();
			analyser.setDescription("Specify what analyser to use");
			analyser.setAllowedValue(VeriFitAnalysisProperties.ANACONDA_ANALYSERS);
			analyser.setName("analyser");
			analyser.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ONE)));
			analyser.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));
			//Analyser.setRepresentation(new Link(new URI(VeriFitAnalysisConstants.OSLC_REPRESENTATION_EITHER))); //TODO		
			
			ParameterDefinition executionParameters = new ParameterDefinition();
			executionParameters.setDescription("Set the execution parameters for the analyzed program. Write down all parameters as you would in a console.");
			executionParameters.setName("executionParameters");
			executionParameters.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ZEROorONE)));
			executionParameters.setDefaultValue("");
			executionParameters.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));
			
			ParameterDefinition SUT = new ParameterDefinition();
			SUT.setDescription("Refference to an SUT resource to analyse. SUTs are created using the compilation provider."); //TODO
			SUT.setName("SUT");
			SUT.setOccurs(new Link(new URI(VeriFitAnalysisConstants.OSLC_OCCURS_ONE)));
			SUT.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING))); // TODO change to URI
			
			// create the autoPlan
			AutomationPlan propertiesPlan = new AutomationPlan();
			propertiesPlan.setTitle("ANaConDA");
			propertiesPlan.setDescription("Analyse an SUT using ANaConDA");
			propertiesPlan.addParameterDefinition(analyser);
			propertiesPlan.addParameterDefinition(executionParameters);
			propertiesPlan.addParameterDefinition(SUT);
			propertiesPlan.addCreator(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic")));
			VeriFitAnalysisManager.createAutomationPlan(propertiesPlan);
	
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
	}
}
