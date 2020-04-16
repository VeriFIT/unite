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
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc4j.core.model.Link;

import verifit.compilation.VeriFitCompilationConstants;
import verifit.compilation.VeriFitCompilationManager;
import verifit.compilation.VeriFitCompilationResourcesFactory;
import verifit.compilation.resources.SUT;
import verifit.compilation.resources.TextOut;
/**
 * A thread for executing the Deploy SUT Automation Plan.
 * @author od42
 *
 */
public class SutDeployAutoPlanExecution extends RequestRunner
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
	public SutDeployAutoPlanExecution(String serviceProviderId, URI execAutoRequestURI, Map<String, String> inputParamsMap) 
	{
		super();
		
		this.serviceProviderId = serviceProviderId;
		this.inputParamsMap = inputParamsMap;
		this.execAutoRequestId = VeriFitCompilationManager.getResourceIdFromUri(execAutoRequestURI);
		this.execAutoRequest = VeriFitCompilationManager.getAutomationRequest(null, serviceProviderId, execAutoRequestId); // TODO added this to avoid copy by refference -- might be slow
		
		this.start();
	}

	/**
	 * Thread main
	 */
	public void run()
	{
		
		try {
			
			// get the input parameters
			final String paramSourceGit = inputParamsMap.get("sourceGit");
			final String paramSourceFileUrl = inputParamsMap.get("sourceFileUrl");
			final String paramBuildCommand = inputParamsMap.get("buildCommand");
			final String paramLaunchCommand = inputParamsMap.get("launchCommand");

			// check wich one of the source parameters was used 
			String ProgramDefinition = "";	//TODO use something else than a string (speed)
			String ProgramSource = "";
			if (paramSourceGit != null)
			{
				ProgramDefinition = "sourceGit";
				ProgramSource = paramSourceGit;
			}
			else if (paramSourceFileUrl != null)
			{
				ProgramDefinition = "sourceFileUrl";
				ProgramSource = paramSourceFileUrl;
			}
				
			
			//create the autoResult as inProgress
			AutomationResult propAutoResult = new AutomationResult();
			propAutoResult.setTitle("Result - " + execAutoRequest.getTitle());
			propAutoResult.setReportsOnAutomationPlan(execAutoRequest.getExecutesAutomationPlan());
			propAutoResult.setProducedByAutomationRequest(VeriFitCompilationResourcesFactory.constructLinkForAutomationRequest(serviceProviderId, execAutoRequestId));
			propAutoResult.setInputParameter(execAutoRequest.getInputParameter());
			propAutoResult.setContributor(execAutoRequest.getContributor());
			propAutoResult.setCreator(execAutoRequest.getCreator());
			propAutoResult.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_INPROGRESS)));
			propAutoResult.addVerdict(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_VERDICT_UNAVAILABLE)));
		    AutomationResult newAutoResult = VeriFitCompilationManager.createAutomationResult(propAutoResult, serviceProviderId, execAutoRequestId);
	    	
			// set the autoRequest's state to in progress and link the autoResult
			execAutoRequest.setState(new HashSet<Link>());
			execAutoRequest.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_INPROGRESS)));
			execAutoRequest.setProducedAutomationResult(new Link(newAutoResult.getAbout()));
			VeriFitCompilationManager.updateAutomationRequest(execAutoRequest, serviceProviderId, execAutoRequestId);
			
			
		    // prepare result contributions - program fetching, compilation
		    TextOut fetchLog = new TextOut();
		    fetchLog.setDescription("Output of the program fetching process. Provider messages are prefixed with #.");
		    fetchLog.setTitle("Fetching Output");
		    //fetchLog.addType(new Link(new URI("http://purl.org/dc/dcmitype/Text"))); //TODO
		    
		    TextOut compStdoutLog = new TextOut();
		    compStdoutLog.setDescription("Standard output of the compilation. Provider messages are prefixed with #.");
		    compStdoutLog.setTitle("Compilation stdout");
		    TextOut compStderrLog = new TextOut();
		    compStderrLog.setDescription("Error output of the compilation. Provider messages are prefixed with #.");
		    compStderrLog.setTitle("Compilation stderr");
			
		    
			// create the program path and name
			final File folderPath = createTmpDir(execAutoRequestId);
		  
		    Boolean performCompilation = true;	// flag to disable a part of the execution in case of an error
			String executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_PASSED;
			String programToExecute = null;
		    
			// fetch source file
			try {
			    // get the source file depending on the ProgramDefinition
			    switch (ProgramDefinition)
			    {
			    case "sourceGit":
			    	gitClonePublic(ProgramSource, folderPath);
			    	break;
			    	
			    case "sourceFileUrl":
			    	downloadFileFromUrl(ProgramSource, folderPath);
			    	break;
			    }

				fetchLog.setValue("# SUT fetch successful\n");
				
			} catch (UnknownHostException e) {	// TODO
				executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_ERROR;
				fetchLog.setValue("# SUT fetch failed\nUnknown host or host unreachable: " + e.getMessage());
	    		performCompilation = false;
	    		
			} catch (IOException e) {
				executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_ERROR;
				fetchLog.setValue("# SUT fetch failed\n" + e.getMessage());
	    		performCompilation = false;
	    		
			} finally {
				// create the fetching log Contribution and add it to the AutomationResult
				fetchLog = VeriFitCompilationManager.createTextOut(fetchLog, serviceProviderId);
		    	newAutoResult.addContribution(fetchLog);
			}
			    
			// compile source file if the fetching did not fail
			if (performCompilation)
			{
				try {
			    	Triple<Integer, String, String> compRes = compileSUT(folderPath, paramBuildCommand);
			    	
			    	if (compRes.getLeft() != 0) // get return code
			    	{	// if the compilation returned non zero, set the verdict as failed
						executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_FAILED;
				    	compStdoutLog.setValue("# Compilation failed (returned non-zero: " + compRes.getLeft() + ")\n"
				    							+ compRes.getMiddle());	// get stdout
			    	}
			    	else
			    	{
				    	compStdoutLog.setValue("# Compilation completed successfully\n" + compRes.getMiddle());	// get stdout
			    	}
			    	compStderrLog.setValue(compRes.getRight());		// get stderr
				    
				} catch (IOException e) {
					// there was an error
					executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_ERROR;
					compStdoutLog.setValue("# Compilation error");	// get stdout
			    	compStderrLog.setValue(e.getMessage());	// get stderr
		    		
				} finally {
					// create the compilation Contributions and add them to the Automation Result
					compStdoutLog = VeriFitCompilationManager.createTextOut(compStdoutLog, serviceProviderId);
					compStderrLog = VeriFitCompilationManager.createTextOut(compStderrLog, serviceProviderId);
			    	newAutoResult.addContribution(compStdoutLog);
			    	newAutoResult.addContribution(compStderrLog);
				}
			}
	    	
			// create the SUT resource
			SUT newSut = new SUT();
			newSut.setTitle("SUT - " + execAutoRequest.getTitle());
			newSut.setLaunchCommand(paramLaunchCommand);
			newSut.setBuildCommand(paramBuildCommand);
			newSut.setDirectoryPath(folderPath.getAbsolutePath().toString());
			newSut.setCreator(execAutoRequest.getCreator());
			newSut.setProducedByAutomationRequest(VeriFitCompilationResourcesFactory.constructLinkForAutomationRequest(serviceProviderId, execAutoRequestId));
			VeriFitCompilationManager.createSUT(newSut, serviceProviderId, execAutoRequestId); // TODO
			
			// update the autoResult state, contribution, verdict
			newAutoResult.setState(new HashSet<Link>());
			newAutoResult.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_COMPLETE)));
			newAutoResult.setVerdict(new HashSet<Link>());
			newAutoResult.addVerdict(new Link(new URI(executionVerdict)));
			newAutoResult.setCreatedSUT(VeriFitCompilationResourcesFactory.constructLinkForSUT(serviceProviderId, VeriFitCompilationManager.getResourceIdFromUri(newSut.getAbout()))); // TODO
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