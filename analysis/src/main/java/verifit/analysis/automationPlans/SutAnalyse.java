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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.Base64.Decoder;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc4j.core.model.Link;

import verifit.analysis.VeriFitAnalysisConstants;
import verifit.analysis.VeriFitAnalysisManager;
import verifit.analysis.VeriFitAnalysisProperties;
import verifit.analysis.VeriFitAnalysisResourcesFactory;
import verifit.analysis.resources.SUT;
import verifit.analysis.resources.TextOut;
/**
 * A thread for executing analysis of an SUT.
 * @author od42
 *
 */
public class SutAnalyse extends RequestRunner
{
	final private String serviceProviderId;
	final private String execAutoRequestId;
	final private AutomationRequest execAutoRequest;
	final private String execSutId;
	final private SUT execSut;
	final private Map<String, Pair<String,Integer>> inputParamsMap;
	
	/**
	 * Creating the thread automatically starts the execution
	 * @param serviceProviderId	ID of the service provider
	 * @param execAutoRequest	Executed AutomationRequest resource object
	 * @param execSUT			Executed SUT resource object
	 * @param inputParamsMap	Input parameters as a map of "name" => "(value, position)"
	 */
	public SutAnalyse(String serviceProviderId, AutomationRequest execAutoRequest, SUT execSut, Map<String, Pair<String,Integer>> inputParamsMap) 
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
			final String outputRegex = inputParamsMap.get("addOutputsRegex").getLeft();
			
			// Build the string to execute from the input parameters based on their positions TODO maybe move somewhere else
			String buildStringToExecute = "";
			List<Pair<String,Integer>> inputParamsList = new ArrayList<Pair<String,Integer>>(inputParamsMap.values());
			inputParamsList.sort((Pair<String,Integer> a, Pair<String,Integer> b) -> a.getRight().compareTo(b.getRight()));
			for (Pair<String, Integer> param : inputParamsList)
			{
				if (param.getRight() != -1) // skip the non commandline input params
					buildStringToExecute += param.getLeft() + " ";
				
			}
			final String stringToExecute = buildStringToExecute;
			

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
			
			
		    // prepare stdin & stdout contributions
		    TextOut analysisStdoutLog = new TextOut();
		    analysisStdoutLog.setDescription("Standard output of the analysis. Provider messages are prefixed with #.");
		    analysisStdoutLog.setTitle("Analysis stdout");
		    TextOut analysisStderrLog = new TextOut();
		    analysisStderrLog.setDescription("Error output of the analysis. Provider messages are prefixed with #.");
		    analysisStderrLog.setTitle("Analysis stderr");
			
		    
		    // take a snapshot of SUT files modification times before executing the analysis
		    Map<String, Long> snapshotBeforeAnalysis = takeDirSnapshot(execSut.getSUTdirectoryPath(), outputRegex);
		    
			// analyse SUT
		    String executionVerdict = VeriFitAnalysisConstants.AUTOMATION_VERDICT_PASSED;
			try {
		    	Triple<Integer, String, String> analysisRes = analyseSUT(execSut.getSUTdirectoryPath(), stringToExecute);
		    	
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
	    		
			}

			// create the compilation Contributions and add them to the Automation Result
			analysisStdoutLog = VeriFitAnalysisManager.createTextOut(analysisStdoutLog, serviceProviderId);
			analysisStderrLog = VeriFitAnalysisManager.createTextOut(analysisStderrLog, serviceProviderId);
	    	newAutoResult.addContribution(analysisStdoutLog);
	    	newAutoResult.addContribution(analysisStderrLog);
	    	
		    // take a snapshot of SUT files modification times after executing the analysis
	    	// and add all the new ones / modified ones as contributions
		    Map<String, Long> snapshotAfterAnalysis = takeDirSnapshot(execSut.getSUTdirectoryPath(), outputRegex);
		    for (Map.Entry<String,Long> newFile : snapshotAfterAnalysis.entrySet())
		    {
		    	if (snapshotBeforeAnalysis.containsKey(newFile.getKey()))
		    	{ // if the file existed before the analysis, check if his modif time changed
		    		Long oldTimestamp = snapshotBeforeAnalysis.get(newFile.getKey());	
		    		if (newFile.getValue() <= oldTimestamp)
		    		{ // the file was not modified
		    			continue;
		    		}
		    	}

		    	// the file did not exist before analysis OR was modified --> add it as contribution to the AutoResult
			    File currFile = new File(newFile.getKey());
		    	TextOut newOrModifFile = new TextOut();
			    newOrModifFile.setAbsolutePath(currFile.getAbsolutePath());
			    newOrModifFile.setDescription("This file was modified or created during execution of this Automation Request. "
			    		+ "If you want to modify the file (eg. its a configuration file), then post this fit:TextOut resource to the TextOut creation factory."); // TODO
			    newOrModifFile.setTitle(currFile.getName());
			    try {
					newOrModifFile.setValue(Files.readString(currFile.toPath(), StandardCharsets.US_ASCII));	// TODO
				} catch (IOException e) {
					newOrModifFile.setValue(e.getMessage());
				}
			    newOrModifFile = VeriFitAnalysisManager.createTextOut(newOrModifFile, serviceProviderId);
		    	newAutoResult.addContribution(newOrModifFile);
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