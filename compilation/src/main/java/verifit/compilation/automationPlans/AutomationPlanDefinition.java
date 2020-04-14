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

package verifit.compilation.automationPlans;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.store.StoreAccessException;

import verifit.compilation.VeriFitCompilationConstants;
import verifit.compilation.VeriFitCompilationManager;

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
    	if (VeriFitCompilationManager.getAutomationPlan(null, VeriFitCompilationConstants.AUTOMATION_PROVIDER_ID, "0") == null)
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
			ParameterDefinition GitURL = new ParameterDefinition();
			GitURL.setDescription("The SUT will be retrieved from a Git repository.");
			GitURL.setName("sourceGit");
			//GitURL.setTitle("Definition"); //TODO
			GitURL.setOccurs(new Link(new URI(VeriFitCompilationConstants.OSLC_OCCURS_ZEROorONE)));
			//GitURL.setReadOnly(false);
			GitURL.addValueType(new Link(new URI(VeriFitCompilationConstants.OSLC_VAL_TYPE_STRING))); // TODO change to URI
			//GitURL.setRepresentation(new Link(new URI(VeriFitCompilationConstants.OSLC_REPRESENTATION_EITHER)));
			
			ParameterDefinition sourceFileUrl = new ParameterDefinition();
			sourceFileUrl.setDescription("A single file SUT will be downloaded from a URL. "
					+ "Example: https://pajda.fit.vutbr.cz/xvasic/oslc-generic-analysis.git");
			sourceFileUrl.setName("sourceFileUrl");
			sourceFileUrl.setOccurs(new Link(new URI(VeriFitCompilationConstants.OSLC_OCCURS_ZEROorONE)));
			sourceFileUrl.addValueType(new Link(new URI(VeriFitCompilationConstants.OSLC_VAL_TYPE_STRING)));
			
			ParameterDefinition buildCommand = new ParameterDefinition();
			buildCommand.setDescription("How to build the SUT. "
					+ "The specified command will be launched from the root directory of the downloaded SUT. "
					+ "Examples: make | ./build.sh | gcc -g -o my_sut"); 
			buildCommand.setName("buildCommand");
			buildCommand.setOccurs(new Link(new URI(VeriFitCompilationConstants.OSLC_OCCURS_ONE)));
			buildCommand.addValueType(new Link(new URI(VeriFitCompilationConstants.OSLC_VAL_TYPE_STRING)));

			ParameterDefinition launchCommand = new ParameterDefinition();
			launchCommand.setDescription("How to launch the SUT once its build. "
					+ "The specified command will be launched from the root directory of the downloaded SUT. "
					+ "Examples: make run | ./run.sh | ./my_sut"); 
			launchCommand.setName("launchCommand");
			launchCommand.setOccurs(new Link(new URI(VeriFitCompilationConstants.OSLC_OCCURS_ONE)));
			launchCommand.addValueType(new Link(new URI(VeriFitCompilationConstants.OSLC_VAL_TYPE_STRING)));
						
			// create the autoPlan
			AutomationPlan propertiesPlan = new AutomationPlan();
			propertiesPlan.setTitle("SUT Deploy");
			propertiesPlan.setDescription("Download and compile an SUT on the server so it can be executed later. "
					+ "Use exactly one of the \"source.*\" parameters.");
			propertiesPlan.addParameterDefinition(sourceFileUrl);
			propertiesPlan.addParameterDefinition(GitURL);
			propertiesPlan.addParameterDefinition(buildCommand);
			propertiesPlan.addParameterDefinition(launchCommand);
			propertiesPlan.addCreator(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic")));
			VeriFitCompilationManager.createAutomationPlan(propertiesPlan);
	
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
	}
}
