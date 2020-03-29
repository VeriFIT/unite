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
			/*ParameterDefinition Analyser = new ParameterDefinition();
			Analyser.setDescription("Specify what analyser to use");
			Analyser.setName("Analyser");
			Analyser.setTitle("Analyser to Use");
			Analyser.setOccurs(new Link(new URI(VeriFitCompilationConstants.OSLC_OCCURS_ONE)));
			Analyser.setReadOnly(false);
			Analyser.addValueType(new Link(new URI(VeriFitCompilationConstants.OSLC_VAL_TYPE_STRING)));
			Analyser.setRepresentation(new Link(new URI(VeriFitCompilationConstants.OSLC_REPRESENTATION_EITHER)));
			
			ParameterDefinition ProgramDefinition = new ParameterDefinition();
			ProgramDefinition.setDescription("Specify how the value of the Program parameter should be treated. See the allowedValue properties for options.");
			ProgramDefinition.setName("ProgramDefinition");
			ProgramDefinition.setTitle("Definition of the Program Value");
			ProgramDefinition.setOccurs(new Link(new URI(VeriFitCompilationConstants.OSLC_OCCURS_ONE)));
			ProgramDefinition.setReadOnly(false);
			ProgramDefinition.addAllowedValue("url download");
			ProgramDefinition.addAllowedValue("base64 string");
			ProgramDefinition.addAllowedValue("filesystem path");
			ProgramDefinition.addAllowedValue("console command");
			ProgramDefinition.addValueType(new Link(new URI(VeriFitCompilationConstants.OSLC_VAL_TYPE_STRING)));
			ProgramDefinition.setRepresentation(new Link(new URI(VeriFitCompilationConstants.OSLC_REPRESENTATION_EITHER)));
			
			ParameterDefinition Program = new ParameterDefinition();
			Program.setDescription("Specify what program to analyze. The value is proccessed based on the ProgramDefinition parameter.");
			Program.setName("Program");
			Program.setTitle("Program to Analyze");
			Program.setOccurs(new Link(new URI(VeriFitCompilationConstants.OSLC_OCCURS_ONE)));
			Program.setReadOnly(false);
			Program.addValueType(new Link(new URI(VeriFitCompilationConstants.OSLC_VAL_TYPE_STRING)));
			Program.setRepresentation(new Link(new URI(VeriFitCompilationConstants.OSLC_REPRESENTATION_EITHER)));
			
			ParameterDefinition CompilationParameters = new ParameterDefinition();
			CompilationParameters.setDescription("Parameters to use during compilation - gcc source 'compilation_parameters'. Default is '-g' because ANaConDA needs debug information to provide useful reports (line numbers, variable names,...). Do NOT use the '-o' parameter or the adapter will not find the compiled binary.");
			CompilationParameters.setName("CompilationParameters");
			CompilationParameters.setTitle("Parameters for Compilation");
			CompilationParameters.setOccurs(new Link(new URI(VeriFitCompilationConstants.OSLC_OCCURS_ZEROorONE))); //TODO if there is a defaultValue isnt it exactly one?
			CompilationParameters.setReadOnly(false);
			CompilationParameters.setDefaultValue("-g");
			CompilationParameters.addValueType(new Link(new URI(VeriFitCompilationConstants.OSLC_VAL_TYPE_STRING)));
			CompilationParameters.setRepresentation(new Link(new URI(VeriFitCompilationConstants.OSLC_REPRESENTATION_EITHER)));
						
			ParameterDefinition ExecutionParameters = new ParameterDefinition();
			ExecutionParameters.setDescription("Set the execution parameters for the analyzed program. Write down all parameters as you would in a console.");
			ExecutionParameters.setName("ExecutionParameters");
			ExecutionParameters.setTitle("Parameters for Program Execution");
			ExecutionParameters.setOccurs(new Link(new URI(VeriFitCompilationConstants.OSLC_OCCURS_ZEROorONE)));
			ExecutionParameters.setReadOnly(false);
			ExecutionParameters.setDefaultValue("");
			ExecutionParameters.addValueType(new Link(new URI(VeriFitCompilationConstants.OSLC_VAL_TYPE_STRING)));
			ExecutionParameters.setRepresentation(new Link(new URI(VeriFitCompilationConstants.OSLC_REPRESENTATION_EITHER)));*/
						
			// create the autoPlan TODO
			AutomationPlan propertiesPlan = new AutomationPlan();
			propertiesPlan.setTitle("SUT Deploy");
			propertiesPlan.setDescription(
					"Download and compile an SUT on the server so it can be executed later.");
			/*propertiesPlan.addParameterDefinition(Analyser);
			propertiesPlan.addParameterDefinition(Program);
			propertiesPlan.addParameterDefinition(ProgramDefinition);
			propertiesPlan.addParameterDefinition(CompilationParameters);
			propertiesPlan.addParameterDefinition(ExecutionParameters);*/
			propertiesPlan.addCreator(new Link(new URI("https://pajda.fit.vutbr.cz/xvasic")));
			VeriFitCompilationManager.createAutomationPlan(propertiesPlan, VeriFitCompilationConstants.AUTOMATION_PROVIDER_ID);
	
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
	}
}
