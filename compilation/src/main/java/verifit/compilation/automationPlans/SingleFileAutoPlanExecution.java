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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Base64.Decoder;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc4j.core.model.Link;

import verifit.compilation.VeriFitCompilationConstants;
import verifit.compilation.VeriFitCompilationManager;
import verifit.compilation.VeriFitCompilationResourcesFactory;
import verifit.compilation.resources.TextOut;
/**
 * A thread for executing the Single File Automation Plan.
 * @author od42
 *
 */
public class SingleFileAutoPlanExecution extends RequestRunner
{
	final private String serviceProviderId;
	final private String execAutoRequestId;
	final private Map<String, String> inputParamsMap;
	private AutomationRequest execAutoRequest;
	
	/**
	 * Creating the thread automatically starts the execution
	 * @param serviceProviderId	ID of the service provider
	 * @param execAutoRequest	Executed AutomationRequest resource object
	 * @param inputParamsMap	Input parameters as a "name" => "value" map
	 */
	public SingleFileAutoPlanExecution(String serviceProviderId, AutomationRequest execAutoRequest, Map<String, String> inputParamsMap) 
	{
		super();
		
		this.serviceProviderId = serviceProviderId;
		this.execAutoRequest = execAutoRequest;
		this.inputParamsMap = inputParamsMap;
		this.execAutoRequestId = VeriFitCompilationManager.getResourceIdFromUri(execAutoRequest.getAbout());
		
		this.start();
	}

	/**
	 * Thread main
	 */
	public void run()
	{
		
		try {
			
			// get the input parameters
			final String paramAnalyser = inputParamsMap.get("Analyser");
			final String paramProgram = inputParamsMap.get("Program");
			final String paramProgramDefinition = inputParamsMap.get("ProgramDefinition");
			final String paramCompilatinParameters = inputParamsMap.get("CompilationParameters");
			final String paramExecutionParameters = inputParamsMap.get("ExecutionParameters");

			//create the autoResult as inProgress
			AutomationResult propAutoResult = new AutomationResult();
			propAutoResult.setTitle("Result - " + execAutoRequest.getTitle());
			propAutoResult.setReportsOnAutomationPlan(execAutoRequest.getExecutesAutomationPlan());
			propAutoResult.setProducedByAutomationRequest(VeriFitCompilationResourcesFactory.constructLinkForAutomationRequest(serviceProviderId, execAutoRequest.getIdentifier()));
			propAutoResult.setInputParameter(execAutoRequest.getInputParameter());
			propAutoResult.setContributor(execAutoRequest.getContributor());
			propAutoResult.setCreator(execAutoRequest.getCreator());
			propAutoResult.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_INPROGRESS)));
			propAutoResult.addVerdict(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_VERDICT_UNAVAILABLE)));
		    AutomationResult newAutoResult = VeriFitCompilationManager.createAutomationResult(propAutoResult, serviceProviderId, execAutoRequestId);
	    	
		    // prepare result contributions - program fetching, compilation, analysis
		    TextOut fetchLog = new TextOut();
		    fetchLog.setDescription("Output of the program fetching process. Stderr is appended to the end."); // TODO update if changed
		    fetchLog.setTitle("Fetching Log");
		    fetchLog.addType(new Link(new URI("http://purl.org/dc/dcmitype/Text")));
		    
		    TextOut compLog = new TextOut();
		    compLog.setDescription("Output of the compilation. Stderr is appended to the end."); // TODO update if changed
		    compLog.setTitle("Compilation Log");
		    compLog.addType(new Link(new URI("http://purl.org/dc/dcmitype/Text")));
		    
			TextOut analysisLog = new TextOut();
			analysisLog.setDescription("Output of the analysis. Stderr is appended to the end."); // TODO update if changed
			analysisLog.setTitle("Analysis Log");
		    compLog.addType(new Link(new URI("http://purl.org/dc/dcmitype/Text")));
			
			// create the program path and name
			final String folderPath = createTmpDir("singlefile");
			final String programPath = folderPath + "/" + genFileName(execAutoRequestId);
		  
			// flags to disable a part of the execution in case of an error
		    Boolean performCompilation = true;
		    Boolean performAnalysis = true;
			
			String executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_PASSED;
			String programToExecute = null;
		    
			// fetch source file
			try {
			    // get the source file depending on the ProgramDefinition
			    switch (paramProgramDefinition)
			    {
			    case "url download":
			    	downloadFileFromUrl(paramProgram, programPath);
			    	break;
			    	
			    case "base64 string":
			    	createFileFromBase64(paramProgram, programPath);
			    	break;
			    	
			    case "filesystem path":
			    	createFileFromFilesystem(paramProgram, programPath);
			    	break;
			    	
			    case "console command":
			    	performCompilation = false; // console commands dont have to be compiled
			    	programToExecute = paramProgram;
			    	break;
			    }

				fetchLog.setValue(""); // TODO fetchlog is empty when there is no error
				
			} catch (UnknownHostException e) {
				executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_ERROR;
				fetchLog.setValue("Unknown host or host unreachable: " + e.getMessage());
	    		performCompilation = false;
	    		performAnalysis = false;
	    		
			} catch (IOException e) {
				executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_ERROR;
				fetchLog.setValue(e.getMessage());
	    		performCompilation = false;
	    		performAnalysis = false;
	    		
			} finally {
				// create the program fetching result Contribution
				// except for the "console command" version
				if (!paramProgramDefinition.equals("console command"))
				{
					fetchLog = VeriFitCompilationManager.createTextOut(fetchLog, serviceProviderId, execAutoRequestId + "-fetch");
					newAutoResult.addContribution(fetchLog);
				}
			}
			    
			// compile source file
			if (performCompilation)
			{
				try {
	
				    Map<String,String> compOut = compileSourceFile(programPath, paramCompilatinParameters); 
			    	String compOutLog = compOut.get("out");
			    	programToExecute = compOut.get("path");
			    	
			    	compLog.setValue(compOutLog);
					
			    	// check for compilation error
			    	if (programToExecute == null)
			    	{
			    		executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_ERROR;
			    		performAnalysis = false;
			    	}
				    
				} catch (IOException e) {
					executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_ERROR;
					compLog.setValue(e.getMessage());
		    		performAnalysis = false;
		    		
				} finally {
					// create the compilation result Contribution
			    	compLog = VeriFitCompilationManager.createTextOut(compLog, serviceProviderId, execAutoRequestId + "-comp");
			    	newAutoResult.addContribution(compLog);
				}
			}
	    	
			
			// execute analysis
			if (performAnalysis)
			{
				try {
		    		String stringToExecute = "echo" + " " + paramAnalyser + " " + programToExecute + " " + paramExecutionParameters;			
		    		String analysisOutLog = executeTheAnalysis(stringToExecute);
		    		analysisLog.setValue(analysisOutLog);
			    
				} catch (IOException e) {
					executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_ERROR;
					
					// alter the error text for console commands
					if (paramProgramDefinition.equals("console command"))
					{
						analysisLog.setValue(e.getMessage().replace("Internal error. Compiled program binary not found.", "Specified console command not found."));
					}
					else
					{
						analysisLog.setValue(e.getMessage());
					}
					
				} finally {
					// create the analysis result Contribution
					analysisLog = VeriFitCompilationManager.createTextOut(analysisLog, serviceProviderId, execAutoRequestId + "-out");
					newAutoResult.addContribution(analysisLog);
				}
		    }
			
			// update the autoResult state, contribution, verdict
			newAutoResult.setState(new HashSet<Link>());
			newAutoResult.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_COMPLETE)));
			newAutoResult.setVerdict(new HashSet<Link>());
			newAutoResult.addVerdict(new Link(new URI(executionVerdict)));
			VeriFitCompilationManager.updateAutomationResult(null, newAutoResult, serviceProviderId, VeriFitCompilationManager.getResourceIdFromUri(newAutoResult.getAbout()));
			
			// update the autoRequest state
			execAutoRequest.setState(new HashSet<Link>());
			execAutoRequest.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_COMPLETE)));
			VeriFitCompilationManager.updateAutomationRequest(execAutoRequest, serviceProviderId, execAutoRequestId);
				
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
	}
}