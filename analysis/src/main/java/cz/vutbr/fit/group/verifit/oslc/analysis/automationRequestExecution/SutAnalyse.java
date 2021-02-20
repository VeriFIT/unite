/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.analysis.automationRequestExecution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc4j.core.model.Link;

import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisConstants;
import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisResourcesFactory;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputParser.ParserManager;

import cz.vutbr.fit.group.verifit.oslc.domain.SUT;
import org.eclipse.lyo.oslc.domains.auto.Contribution;

import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;
import cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution.RequestRunner;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.GetModifFilesBySnapshot;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;


/**
 * A thread for executing analysis of an SUT.
 * @author od42
 *
 */
public class SutAnalyse extends RequestRunner
{
	final private String execAutoRequestId;
	private AutomationRequest execAutoRequest;
	final private String resAutoResultId;
	private AutomationResult resAutoResult;
	final private String execSutId;
	private SUT execSut;
	final private Map<String, Pair<String,Integer>> inputParamsMap;

	final private AutomationPlanConfManager.AutomationPlanConf autoPlanConf;
	
	/**
	 * @param serviceProviderId	ID of the service provider
	 * @param execAutoRequest	Executed AutomationRequest resource object
	 * @param resAutoResult		Result AutomationResult resource object
	 * @param execSut			Executed SUT resource object
	 * @param inputParamsMap	Input parameters as a map of "name" => "(value, position)"
	 */
	public SutAnalyse(AutomationRequest execAutoRequest, AutomationResult resAutoResult, SUT execSut, Map<String, Pair<String,Integer>> inputParamsMap) 
	{
		super();
		
		this.inputParamsMap = inputParamsMap;
		this.execAutoRequestId = Utils.getResourceIdFromUri(execAutoRequest.getAbout());
		this.execAutoRequest = execAutoRequest;
		this.resAutoResultId = Utils.getResourceIdFromUri(resAutoResult.getAbout());
		this.resAutoResult = resAutoResult;
		this.execSutId = Utils.getResourceIdFromUri(execSut.getAbout());;
		this.execSut = execSut;

		// load the AutomationPlanConfiguration
		AutomationPlanConfManager autoPlanConfManager = AutomationPlanConfManager.getInstance();
		this.autoPlanConf = autoPlanConfManager.getAutoPlanConf(
				Utils.getResourceIdFromUri(execAutoRequest.getExecutesAutomationPlan().getValue())
			);
	}

	/**
	 * Thread main
	 */
	public void run()
	{
		// get non-commandline input parameters (dont have commandline position - getRight() == -1)
		final String outputRegex = this.inputParamsMap.get("outputFileRegex").getLeft();
		final String zipOutputs = this.inputParamsMap.get("zipOutputs").getLeft();
		final String timeout = this.inputParamsMap.get("timeout").getLeft();
		final String toolCommand = this.inputParamsMap.get("toolCommand").getLeft();

		// set the states of the Automation Result and Request to "inProgress" - if they are not that already
		if (!(execAutoRequest.getState().iterator().next().getValue()
				.equals(OslcValues.AUTOMATION_STATE_INPROGRESS.getValue())))
		{
			resAutoResult.replaceState(OslcValues.AUTOMATION_STATE_INPROGRESS);
			VeriFitAnalysisManager.updateAutomationResult(null, resAutoResult, resAutoResultId);
			execAutoRequest.replaceState(OslcValues.AUTOMATION_STATE_INPROGRESS);
			VeriFitAnalysisManager.updateAutomationRequest(null, execAutoRequest, execAutoRequestId);
		}


	    // prepare Contribution resources
		Contribution executionTime = new Contribution();
		executionTime.setDescription("Total execution time of the analysis in milliseconds."); // TODO CHECK really milliseconds?
		executionTime.setTitle("executionTime");
		executionTime.addValueType(OslcValues.OSLC_VAL_TYPE_INTEGER);
	    
		Contribution statusMessage = new Contribution();
		statusMessage.setDescription("Status messages from the adapter about the execution.");
		statusMessage.setTitle("statusMessage");
		statusMessage.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
	    
		Contribution returnCode = new Contribution();
		returnCode.setDescription("Return code of the execution. If non-zero, then the verdict will be #failed.");
		returnCode.setTitle("returnCode");
		returnCode.addValueType(OslcValues.OSLC_VAL_TYPE_INTEGER);	
		
		Contribution analysisStdout = new Contribution();
	    analysisStdout.setDescription("Standard output of the analysis.");
	    analysisStdout.setTitle("stdout");
	    analysisStdout.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
	    
	    Contribution analysisStderr = new Contribution();
	    analysisStderr.setDescription("Error output of the analysis.");
	    analysisStderr.setTitle("stderr");
	    analysisStderr.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
		
	    // take a snapshot of SUT files modification times before executing the analysis
	    GetModifFilesBySnapshot snapshotter = new GetModifFilesBySnapshot(new File(execSut.getSUTdirectoryPath()));
	    snapshotter.takeBeforeSnapshot(outputRegex);
	    
		// execute analysis
	    Link executionVerdict;
	    ExecutionResult analysisRes = null;
		try {
			final String stringToExecute = buildStringToExecFromParams(toolCommand, this.inputParamsMap);
			final Path SUTdirAsPath = FileSystems.getDefault().getPath(execSut.getSUTdirectoryPath());
			
			statusMessage.setValue("Executing: " + stringToExecute + "\n  In dir: " + SUTdirAsPath + "\n");
	    	analysisRes = executeString(SUTdirAsPath, stringToExecute, Integer.parseInt(timeout), this.execAutoRequestId);
	    	
			if (analysisRes.timeouted)
			{
				executionVerdict = OslcValues.AUTOMATION_VERDICT_FAILED;
				statusMessage.setValue(statusMessage.getValue() + "Analysis aborted due to a " + analysisRes.timeoutType + " timeout (" + timeout + " seconds).");
			}
	    	else if (analysisRes.retCode != 0)
	    	{
				executionVerdict = OslcValues.AUTOMATION_VERDICT_FAILED;
				statusMessage.setValue(statusMessage.getValue() + "Analysis failed (returned non-zero: " + analysisRes.retCode + ").");
			}
	    	else
	    	{
	    		executionVerdict = OslcValues.AUTOMATION_VERDICT_PASSED;
	    		statusMessage.setValue(statusMessage.getValue() + "Analysis completed successfully.");
	    	}

		} catch (IOException e) {
			executionVerdict = OslcValues.AUTOMATION_VERDICT_ERROR;
			statusMessage.setValue(statusMessage.getValue() + "Analysis execution error: " + e.getMessage());
		} finally {
			resAutoResult.addContribution(statusMessage); // TODO add infos abou stuff below too
		}

		// only do more processing if there was no exception during execution
		if (executionVerdict != OslcValues.AUTOMATION_VERDICT_ERROR)
		{
			// add general compilation Contributions to the Automation Result
			
			executionTime.setValue(Long.toString(analysisRes.totalTime));
			resAutoResult.addContribution(executionTime);
			returnCode.setValue(Integer.toString(analysisRes.retCode));
			resAutoResult.addContribution(returnCode);	
	    
			// take a snapshot of SUT files after analysis and get a list of modified ones
	    	snapshotter.takeAfterSnapshot();
	    	Collection<File> modifFiles = snapshotter.getModifFiles();
	    	
	    	// remove the stdout and stderr files from the list of modified files from the snapshot (if they are there)
	    	// (will be added manually with special description etc..)
			modifFiles.remove(analysisRes.stdoutFile);
			modifFiles.remove(analysisRes.stderrFile);
		
	    	// add all modified or produced files as contributions to the automation result
	    	Collection<Contribution> fileContributions = createFileContributions(modifFiles);
	    	for (Contribution c : fileContributions) {
	    		resAutoResult.addContribution(c);
	    	}
	    	
	    	// now add them back in there (for zip later)
			modifFiles.add(analysisRes.stdoutFile);
			modifFiles.add(analysisRes.stderrFile);
	    	
	    	// add file URIs to standard output contributions and add them to the automation result
	    	analysisStdout.setFileURI(VeriFitAnalysisResourcesFactory.constructURIForContribution(analysisRes.stdoutFile.getPath()));
	    	resAutoResult.addContribution(analysisStdout);
	    	analysisStderr.setFileURI(VeriFitAnalysisResourcesFactory.constructURIForContribution(analysisRes.stderrFile.getPath()));
			resAutoResult.addContribution(analysisStderr);
	    	
			// create a zip of all file contributions if needed
			if (zipOutputs.equalsIgnoreCase("true"))
			{
				final String zipName = "out" + this.execAutoRequestId + ".zip";
				final Path zipDir = FileSystems.getDefault().getPath(this.execSut.getSUTdirectoryPath());
				
				try {
					Contribution zipContrib = zipAllFileContributions(modifFiles, zipName, zipDir);
					resAutoResult.addContribution(zipContrib);
				} catch (Exception e) {
					System.out.println("ERROR: failed to ZIP outputs: " + e.getMessage()); // TODO
				}
			}
			
			// run the AutoResult contributions through a parser
			ParserManager parserManagerInst = ParserManager.getInstance();
			Set<Contribution> parsedContributions = parserManagerInst.parseContributionsForTool(
					Utils.getResourceIdFromUri(execAutoRequest.getExecutesAutomationPlan().getValue()),
					resAutoResult.getContribution());
			resAutoResult.setContribution(parsedContributions);
		}
		
		// update the AutoResult state and verdict, and AutoRequest state
		resAutoResult.replaceState(OslcValues.AUTOMATION_STATE_COMPLETE);
		resAutoResult.replaceVerdict(executionVerdict);
		VeriFitAnalysisManager.updateAutomationResult(null, resAutoResult, Utils.getResourceIdFromUri(resAutoResult.getAbout()));
		execAutoRequest.setState(new HashSet<Link>());
		execAutoRequest.addState(OslcValues.AUTOMATION_STATE_COMPLETE);
		VeriFitAnalysisManager.updateAutomationRequest(null, execAutoRequest, execAutoRequestId);

		// end the request execution (in case it is part of a request queue)
		VeriFitAnalysisManager.finishedAutomationRequestExecution(execAutoRequest);
	}

	


	private Contribution zipAllFileContributions(Collection<File> modifFiles, String zipName, Path zipDir) throws IOException {
		Contribution zipedContribs = new Contribution();
		Path pathToZip = zipDir.resolve(zipName);

		File newZipFile = Utils.zipFiles(modifFiles, zipDir, pathToZip);

		String fileId = Utils.encodeFilePathAsId(newZipFile);
		zipedContribs.setFileURI(VeriFitAnalysisResourcesFactory.constructURIForContribution(fileId));
		zipedContribs.setDescription("This is a ZIP of all other file contributions. "
				+ "To download the file directly send a GET accepting application/octet-stream to the URI in the fit:fileURI property."); // TODO
		zipedContribs.setTitle(zipName);
		zipedContribs.addValueType(OslcValues.OSLC_VAL_TYPE_BASE64BINARY);
		
		return zipedContribs;
	}

	/**
	 * Build the string to execute from input parameters based on their positions
	 * @param inputParamsMap
	 * @return The string to execute
	 */
	private String buildStringToExecFromParams(String toolCommand, Map<String, Pair<String,Integer>> inputParamsMap)
	{
		toolCommand = (toolCommand.equalsIgnoreCase("true") ? this.autoPlanConf.getLaunchCommand() : ""); // insert the analysis tool launch command if the parameter was true
		
		String buildStringToExecute = ""
			+  toolCommand + " "
			+ this.autoPlanConf.getToolSpecificArgs();
		
		// sort all input params based on their command line position and append them to the string to execute
		List<Pair<String,Integer>> inputParamsList = new ArrayList<Pair<String,Integer>>(inputParamsMap.values());
		inputParamsList.sort((Pair<String,Integer> a, Pair<String,Integer> b) -> a.getRight().compareTo(b.getRight()));
		for (Pair<String, Integer> param : inputParamsList)
		{
			if (param.getRight() != -1) // -1 means "not a commandline parameter" -> dont add those
				buildStringToExecute += " " + param.getLeft();

		}
		return buildStringToExecute;
	}
	
	/**
	 * Creates contributions for a list of files
	 * @param modifFiles
	 * @param resAutoResult2
	 */
	private Collection<Contribution> createFileContributions(Collection<File> modifFiles)
	{
		Collection<Contribution> contributions = new HashSet<Contribution>();
		
		for (File currFile : modifFiles)
		{
			Contribution newContrib = new Contribution();
			String fileId = Utils.encodeFilePathAsId(currFile);
		    newContrib.setFileURI(VeriFitAnalysisResourcesFactory.constructURIForContribution(fileId));
		    newContrib.setDescription("File produced or modified during execution of this Automation Request. "
		    		+ "To download the file directly send a GET accepting application/octet-stream to the URI in the fit:fileURI property. "
		    		+ "To modify the file send a regular OSLC update PUT request for a Contribution resource to the URI in fit:fileURI "
		    		+ "with content type application/octet-stream.");
		    newContrib.setTitle(currFile.getName());
		    
	    	try {
	    		// set value type (binary or not)
				if (Utils.isBinaryFile(currFile)) {
					newContrib.addValueType(OslcValues.OSLC_VAL_TYPE_BASE64BINARY);
				} else {
					newContrib.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
				}
			} catch (IOException e) {
				System.out.println("WARNING: failed probe file type of \"" + currFile.getName() + "\": " + e.getMessage()); // TODO
				newContrib.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			}
	    	
		    contributions.add(newContrib);
		}
		
		return contributions;
	}
	
}