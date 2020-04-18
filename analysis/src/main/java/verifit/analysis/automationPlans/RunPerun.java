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

import verifit.analysis.VeriFitAnalysisConstants;
import verifit.analysis.VeriFitAnalysisManager;
import verifit.analysis.VeriFitAnalysisProperties;
import verifit.analysis.VeriFitAnalysisResourcesFactory;
import verifit.analysis.resources.SUT;
import verifit.analysis.resources.TextOut;
/**
 * A thread for executing running perun.
 * @author od42
 *
 */
public class RunPerun extends RequestRunner
{
	final private String serviceProviderId;
	final private String execAutoRequestId;
	final private AutomationRequest execAutoRequest;
	final private String execSutId;
	final private SUT execSut;
	final private Map<String, String> inputParamsMap;
	
	/**
	 * Creating the thread automatically starts the execution
	 * @param serviceProviderId	ID of the service provider
	 * @param execAutoRequest	Executed AutomationRequest resource object
	 * @param execSUT			Executed SUT resource object
	 * @param inputParamsMap	Input parameters as a "name" => "value" map
	 */
	public RunPerun(String serviceProviderId, AutomationRequest execAutoRequest, SUT execSut, Map<String, String> inputParamsMap) 
	{
		super();
		
		this.serviceProviderId = serviceProviderId;
		this.inputParamsMap = inputParamsMap;
		this.execAutoRequestId = VeriFitAnalysisManager.getResourceIdFromUri(execAutoRequest.getAbout());
		this.execAutoRequest = execAutoRequest;
		this.execSutId = VeriFitAnalysisManager.getResourceIdFromUri(execSut.getAbout());;
		this.execSut = execSut;
		
		this.start();
	}

	/**
	 * Thread main
	 */
	public void run()
	{
		
		try {
			
			// get the input parameters
			final String paramCommand = inputParamsMap.get("command");
			
			//create the autoResult as inProgress
			AutomationResult propAutoResult = new AutomationResult();
			propAutoResult.setTitle("Result - " + execAutoRequest.getTitle());
			propAutoResult.setReportsOnAutomationPlan(execAutoRequest.getExecutesAutomationPlan());
			propAutoResult.setProducedByAutomationRequest(VeriFitAnalysisResourcesFactory.constructLinkForAutomationRequest(serviceProviderId, execAutoRequestId));
			propAutoResult.setInputParameter(execAutoRequest.getInputParameter());
			propAutoResult.setContributor(execAutoRequest.getContributor());
			propAutoResult.setCreator(execAutoRequest.getCreator());
			propAutoResult.addState(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_STATE_INPROGRESS)));
			propAutoResult.addVerdict(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_VERDICT_UNAVAILABLE)));
		    AutomationResult newAutoResult = VeriFitAnalysisManager.createAutomationResult(propAutoResult, serviceProviderId, execAutoRequestId);
	    	
			// set the autoRequest's state to in progress and link the autoResult
			execAutoRequest.setState(new HashSet<Link>());
			execAutoRequest.addState(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_STATE_INPROGRESS)));
			execAutoRequest.setProducedAutomationResult(new Link(newAutoResult.getAbout()));
			VeriFitAnalysisManager.updateAutomationRequest(execAutoRequest, serviceProviderId, execAutoRequestId);
			
			
		    // prepare result contributions
		    TextOut analysisStdoutLog = new TextOut();
		    analysisStdoutLog.setDescription("Standard output of the analysis. Provider messages are prefixed with #.");
		    analysisStdoutLog.setTitle("Analysis stdout");
		    TextOut analysisStderrLog = new TextOut();
		    analysisStderrLog.setDescription("Error output of the analysis. Provider messages are prefixed with #.");
		    analysisStderrLog.setTitle("Analysis stderr");
			
		    
			// analyse SUT
		    String executionVerdict = VeriFitAnalysisConstants.AUTOMATION_VERDICT_PASSED;
			try {
				File sutDirectory = new File(execSut.getDirectoryPath());
				String sutLaunchCommand = execSut.getLaunchCommand();
		    	Triple<Integer, String, String> analysisRes = analyseSUT(sutDirectory, VeriFitAnalysisProperties.PERUN_PATH, paramCommand, "", "");
		    	
		    	if (analysisRes.getLeft() != 0) // get return code
		    	{
					executionVerdict = VeriFitAnalysisConstants.AUTOMATION_VERDICT_ERROR;
			    	analysisStdoutLog.setValue("# Analysis failed (returned non-zero: " + analysisRes.getLeft() + ")\n"
			    							+ analysisRes.getMiddle());	// get stdout
		    	}
		    	else
		    	{
		    		analysisStdoutLog.setValue("# Analysis completed successfully\n" + analysisRes.getMiddle());	// get stdout
		    	}
		    	analysisStderrLog.setValue(analysisRes.getRight());		// get stderr
			    
			} catch (IOException e) {
				// there was an error
				executionVerdict = VeriFitAnalysisConstants.AUTOMATION_VERDICT_ERROR;
				analysisStdoutLog.setValue("# Analysis error");	// get stdout
				analysisStderrLog.setValue(e.getMessage());	// get stderr
	    		
			} finally {
				// create the compilation Contributions and add them to the Automation Result
				analysisStdoutLog = VeriFitAnalysisManager.createTextOut(analysisStdoutLog, serviceProviderId);
				analysisStderrLog = VeriFitAnalysisManager.createTextOut(analysisStderrLog, serviceProviderId);
		    	newAutoResult.addContribution(analysisStdoutLog);
		    	newAutoResult.addContribution(analysisStderrLog);
			}
			
			// update the autoResult state, contribution, verdict
			newAutoResult.setState(new HashSet<Link>());
			newAutoResult.addState(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_STATE_COMPLETE)));
			newAutoResult.setVerdict(new HashSet<Link>());
			newAutoResult.addVerdict(new Link(new URI(executionVerdict)));
			VeriFitAnalysisManager.updateAutomationResult(null, newAutoResult, serviceProviderId, VeriFitAnalysisManager.getResourceIdFromUri(newAutoResult.getAbout()));
			
			// update the autoRequest state
			execAutoRequest.setState(new HashSet<Link>());
			execAutoRequest.addState(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_STATE_COMPLETE)));
			VeriFitAnalysisManager.updateAutomationRequest(execAutoRequest, serviceProviderId, execAutoRequestId);
				
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
	}
}