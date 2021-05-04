/*
 * Copyright (C) 2020 OndÅ™ej VaÅ¡Ã­Ä�ek <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.analysis.automationRequestExecution;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Base64.Decoder;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc.domains.auto.Contribution;
import org.eclipse.lyo.oslc.domains.auto.ParameterInstance;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisResourcesFactory;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager.AutomationPlanConf;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.FilterManager;
import cz.vutbr.fit.group.verifit.oslc.domain.SUT;
import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;
import cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution.ExecutionParameter;
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
    private static final Logger log = LoggerFactory.getLogger(SutAnalyse.class);
    		
	final private List<ExecutionParameter> execParameters;
	final private String execAutoRequestId;
	private AutomationRequest execAutoRequest;
	final private String resAutoResultId;
	private AutomationResult resAutoResult;
	final private String execSutId;
	private SUT execSut;
	
	final private String scriptFileEnding;
	
	final private AutomationPlanConf autoPlanConf;
	
	private Collection<File> filesToDeleteIfInterrupted = new ArrayList<File>();
	
	/**
	 * @param serviceProviderId	ID of the service provider
	 * @param execAutoRequest	Executed AutomationRequest resource object
	 * @param resAutoResult		Result AutomationResult resource object
	 * @param execSut			Executed SUT resource object
	 * @param execParameters	Execution parameters
	 * @param inputParamsMap	Input parameters as a map of "name" => "(value, position)"
	 */
	public SutAnalyse(AutomationRequest execAutoRequest, AutomationResult resAutoResult, SUT execSut, List<ExecutionParameter> execParameters) 
	{
		super(  Utils.getResourceIdFromUri(execAutoRequest.getAbout()),
				Utils.getResourceIdFromUri(execAutoRequest.getExecutesAutomationPlan().getValue()),
				execAutoRequest.getState().iterator().next(),
				execAutoRequest.getDesiredState()
		);

		this.execParameters = execParameters;
		this.execAutoRequestId = Utils.getResourceIdFromUri(execAutoRequest.getAbout());
		this.execAutoRequest = execAutoRequest;
		this.resAutoResultId = Utils.getResourceIdFromUri(resAutoResult.getAbout());
		this.resAutoResult = resAutoResult;
		this.execSutId = Utils.getResourceIdFromUri(execSut.getAbout());;
		this.execSut = execSut;

		if (SystemUtils.IS_OS_LINUX) {
			scriptFileEnding = ".sh";
		} else {
			scriptFileEnding = ".ps1";
		}
		
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
		try {
			// input parameters
			String outputRegex = null;
			String zipOutputs = null;
			String timeout = null;
			String toolCommand = null;
			String outputFilter = null;
			List<String> confFiles = new ArrayList<String>();		// there can be multiple conf files
			String beforeCommand = null;
			String afterCommand = null;
			String confDir = null;
			List<String> paramEnvVariables = new ArrayList<String>();	// there can be multiple env variables
			
			// extract values from parameters
			for (ExecutionParameter param : this.execParameters)
			{ 
				if (param.getName().equals("outputFileRegex")) outputRegex = param.getValue();
				else if (param.getName().equals("zipOutputs")) zipOutputs = param.getValue();
				else if (param.getName().equals("timeout")) timeout = param.getValue();
				else if (param.getName().equals("toolCommand")) toolCommand = param.getValue();
				else if (param.getName().equals("outputFilter")) outputFilter = param.getValue();
				else if (param.getName().equals("confFile")) confFiles.add(param.getValue());
				else if (param.getName().equals("beforeCommand")) beforeCommand = param.getValue();
				else if (param.getName().equals("afterCommand")) afterCommand = param.getValue();
				else if (param.getName().equals("confDir")) confDir = param.getValue();
				else if (param.getName().equals("envVariable")) paramEnvVariables.add(param.getValue());
			}
	
			// build the string to execute later from command line input parameters (those that have a commandline position)
			final String stringToExecute = buildStringToExecFromParams(toolCommand, this.execParameters);
	
			// set the states of the Automation Result and Request to "inProgress" - if they are not that already
			if (!(execAutoRequest.getState().iterator().next().getValue()
					.equals(OslcValues.AUTOMATION_STATE_INPROGRESS.getValue())))
			{
				resAutoResult.replaceState(OslcValues.AUTOMATION_STATE_INPROGRESS);
				VeriFitAnalysisManager.internalUpdateAutomationResult(resAutoResult, resAutoResultId);
				execAutoRequest.replaceState(OslcValues.AUTOMATION_STATE_INPROGRESS);
				VeriFitAnalysisManager.internalUpdateAutomationRequest(execAutoRequest, execAutoRequestId);
			}
			
			// transform parameEnvVariables to a set of name:value
			Map<String,String> envVariables = prepareEvnVariableMapFromPrams(paramEnvVariables);
			
	
		    // prepare Contribution resources
			Contribution executionTime = VeriFitAnalysisResourcesFactory.createContribution("executionTime");
			executionTime.setDescription("Total execution time of the analysis in milliseconds.");
			executionTime.setTitle("executionTime");
			executionTime.addValueType(OslcValues.OSLC_VAL_TYPE_INTEGER);
		    
			Contribution statusMessage = VeriFitAnalysisResourcesFactory.createContribution("statusMessage");
			statusMessage.setDescription("Status messages from the adapter about the execution.");
			statusMessage.setTitle("statusMessage");
			statusMessage.setValue("");
			statusMessage.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
		    
			Contribution returnCode = VeriFitAnalysisResourcesFactory.createContribution("returnCode");
			returnCode.setDescription("Return code of the execution. If non-zero, then the verdict will be #failed.");
			returnCode.setTitle("returnCode");
			returnCode.addValueType(OslcValues.OSLC_VAL_TYPE_INTEGER);	
			
			Contribution analysisStdout = VeriFitAnalysisResourcesFactory.createContribution("stdout");
		    analysisStdout.setDescription("Standard output of the analysis.");
		    analysisStdout.setTitle("stdout");
		    analysisStdout.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
		    
		    Contribution analysisStderr = VeriFitAnalysisResourcesFactory.createContribution("stderr");
		    analysisStderr.setDescription("Error output of the analysis.");
		    analysisStderr.setTitle("stderr");
		    analysisStderr.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
		    
			Contribution beforeCmdStdout = VeriFitAnalysisResourcesFactory.createContribution("beforeCommandStdout");
			beforeCmdStdout.setDescription("Standard output of the beforeCommand.");
			beforeCmdStdout.setTitle("beforeCommandStdout");
			beforeCmdStdout.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
		    
		    Contribution beforeCmdStderr = VeriFitAnalysisResourcesFactory.createContribution("beforeCommandStderr");
		    beforeCmdStderr.setDescription("Error output of the beforeCommand.");
		    beforeCmdStderr.setTitle("beforeCommandStderr");
		    beforeCmdStderr.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
		    
			Contribution afterCmdStdout = VeriFitAnalysisResourcesFactory.createContribution("afterCommandStdout");
			afterCmdStdout.setDescription("Standard output of the afterCommand.");
			afterCmdStdout.setTitle("afterCommandStdout");
			afterCmdStdout.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
		    
		    Contribution afterCmdStderr = VeriFitAnalysisResourcesFactory.createContribution("afterCommandStderr");
		    afterCmdStderr.setDescription("Error output of the afterCommand.");
		    afterCmdStderr.setTitle("afterCommandStderr");
		    afterCmdStderr.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			
		    // warning if not compiled
		    if (!execSut.isCompiled())
		    {
	    		statusMessage.appendValue("Warning: Analysing an SUT which was not compiled\n");
		    }
		    
		    // take a snapshot of SUT files modification times before executing the analysis
		    GetModifFilesBySnapshot snapshotter = new GetModifFilesBySnapshot(new File(execSut.getSUTdirectoryPath()));
		    snapshotter.takeBeforeSnapshot(outputRegex);
		    
		    // get the SUT path
			final Path SUTdirAsPath = FileSystems.getDefault().getPath(execSut.getSUTdirectoryPath());
		    
			// create a conf directory in the SUT directory if confDir parameter was specified
			if (confDir != null)
			{
				try {
					createConfDir(SUTdirAsPath, confDir);
					statusMessage.appendValue("Creating conf directory \"" + confDir.substring(0, confDir.indexOf('\n')) + "\"\n");
				} catch (Exception e) {
					statusMessage.appendValue("Failed to create conf directory \"" + confDir.substring(0, confDir.indexOf('\n')) + "\"\n");
					// TODO fail the whole execution or set a warning?
				}
			}
			
			// create a conf file in the SUT directory if confFile parameter was specified
			if (!confFiles.isEmpty())
			{
				for (String cFile : confFiles)
				{
					try {
						createConfFile(SUTdirAsPath, cFile);
						statusMessage.appendValue("Creating conf file \"" + cFile.substring(0, cFile.indexOf('\n')) + "\"\n");
					} catch (Exception e) {
						statusMessage.appendValue("Failed to create conf file \"" + cFile.substring(0, cFile.indexOf('\n')) + "\"\n");
						// TODO fail the whole execution or set a warning?
					}
				}
			}
			
			/* Initialize the verdict (result of execution) as passed. If any of the executed commands (before, analysis, after) sets it to error or failed,
			 * then the commands after the failed one will not be executed.
			 */ 
		    Link executionVerdict = OslcValues.AUTOMATION_VERDICT_PASSED;
			
			// execute beforeCommand just before analysis
		    ExecutionResult beforeCmdRes = null;
		    if (beforeCommand != null)
		    {
		    	beforeCmdRes = executeString(SUTdirAsPath, beforeCommand, 0, "_analysis_" + this.execAutoRequestId + "_beforeCmd", this.filesToDeleteIfInterrupted, envVariables);
				statusMessage.appendValue("Executing beforeCommand: " + beforeCommand + "\n   as: " + beforeCmdRes.executedString + "\n   In dir: " + SUTdirAsPath + "\n");
		    
				if (beforeCmdRes.exceptionThrown != null)
				{
					// there was an error
					executionVerdict = OslcValues.AUTOMATION_VERDICT_ERROR;
					statusMessage.appendValue("BeforeCommand execution error: " + beforeCmdRes.exceptionThrown.getMessage() + "\n");
				}
		    	else if (beforeCmdRes.retCode != 0)
		    	{
		    		executionVerdict = OslcValues.AUTOMATION_VERDICT_FAILED;
					statusMessage.appendValue("BeforeCommand failed (returned non-zero: " + beforeCmdRes.retCode + ")\n");
				}

		    	// add file URIs to standard output contributions and add them to the automation result
	    		beforeCmdStdout.setFilePath(beforeCmdRes.stdoutFile.getAbsolutePath());
		    	resAutoResult.addContribution(beforeCmdStdout);
		    	beforeCmdStderr.setFilePath(beforeCmdRes.stderrFile.getAbsolutePath());
				resAutoResult.addContribution(beforeCmdStderr);
		    }
		    
			// execute analysis
		    ExecutionResult analysisRes = null;
		    if (executionVerdict.equals(OslcValues.AUTOMATION_VERDICT_PASSED)) 
		    {
			    analysisRes = executeString(SUTdirAsPath, stringToExecute, Integer.parseInt(timeout), "_analysis_" + this.execAutoRequestId, this.filesToDeleteIfInterrupted, envVariables);
				statusMessage.appendValue("Executing analysis: " + stringToExecute + "\n   as: " + analysisRes.executedString + "\n   In dir: " + SUTdirAsPath + "\n");
				if (analysisRes.exceptionThrown != null)
				{
					// there was an error
					executionVerdict = OslcValues.AUTOMATION_VERDICT_ERROR;
					statusMessage.appendValue("Analysis execution error: " + analysisRes.exceptionThrown.getMessage() + "\n");
				}
				else if (analysisRes.timeouted)
				{
					executionVerdict = OslcValues.AUTOMATION_VERDICT_FAILED;
					statusMessage.appendValue("Analysis aborted due to a " + analysisRes.timeoutType + " timeout (" + timeout + " seconds)");
				}
		    	else if (analysisRes.retCode != 0)
		    	{
					executionVerdict = OslcValues.AUTOMATION_VERDICT_FAILED;
					statusMessage.appendValue("Analysis failed (returned non-zero: " + analysisRes.retCode + ")\n");
				}
		    	else
		    	{
		    		executionVerdict = OslcValues.AUTOMATION_VERDICT_PASSED;
		    		statusMessage.appendValue("Analysis completed successfully\n");
		    	}

		    	// add file URIs to standard output contributions and add them to the automation result
		    	analysisStdout.setFilePath(analysisRes.stdoutFile.getAbsolutePath());
		    	resAutoResult.addContribution(analysisStdout);
		    	analysisStderr.setFilePath(analysisRes.stderrFile.getAbsolutePath());
				resAutoResult.addContribution(analysisStderr);

				// add general compilation Contributions to the Automation Result
				executionTime.setValue(Long.toString(analysisRes.totalTime));
				resAutoResult.addContribution(executionTime);
				returnCode.setValue(Integer.toString(analysisRes.retCode));
				resAutoResult.addContribution(returnCode);	
		    }
		    else
		    {
				statusMessage.appendValue("Skipping analysis due to previous failures\n");
		    }
		    
			// execute afterCommand just after analysis
		    ExecutionResult afterCmdRes = null;
		    if (afterCommand != null)
		    {
			    if (executionVerdict.equals(OslcValues.AUTOMATION_VERDICT_PASSED))
			    {
			    	afterCmdRes = executeString(SUTdirAsPath, afterCommand, 0, "_analysis_" + this.execAutoRequestId + "_afterCmd", this.filesToDeleteIfInterrupted, envVariables);
					statusMessage.appendValue("Executing afterCommand: " + afterCommand + "\n   as: " + afterCmdRes.executedString + "\n   In dir: " + SUTdirAsPath + "\n");
	
					if (afterCmdRes.exceptionThrown != null)
					{
						// there was an error
						executionVerdict = OslcValues.AUTOMATION_VERDICT_ERROR;
						statusMessage.appendValue("AfterCommand execution error: " + afterCmdRes.exceptionThrown.getMessage() + "\n");
					}
			    	else if (afterCmdRes.retCode != 0)
			    	{
			    		executionVerdict = OslcValues.AUTOMATION_VERDICT_FAILED;
						statusMessage.appendValue("AfterCommand failed (returned non-zero: " + afterCmdRes.retCode + ")\n");
					}
					
			    	// add file URIs to standard output contributions and add them to the automation result
		    		afterCmdStdout.setFilePath(afterCmdRes.stdoutFile.getAbsolutePath());
			    	resAutoResult.addContribution(afterCmdStdout);
			    	afterCmdStderr.setFilePath(afterCmdRes.stderrFile.getAbsolutePath());
					resAutoResult.addContribution(afterCmdStderr);
			    }
			    else
			    {
					statusMessage.appendValue("Skipping afterCommand due to previous failures\n");
			    }
		    }
			
			
			// only do more processing if there was no exception during execution
			if (executionVerdict != OslcValues.AUTOMATION_VERDICT_ERROR)
			{		    
				// take a snapshot of SUT files after analysis and get a list of modified ones
		    	snapshotter.takeAfterSnapshot();
		    	Collection<File> modifFiles = snapshotter.getModifFiles();
		    	
		    	// remove the stdout and stderr files from the list of modified files from the snapshot (if they are there)
		    	// (will be added manually with special description etc..)	[hack]
		    	if (analysisRes != null)  modifFiles.remove(analysisRes.stdoutFile);
		    	if (analysisRes != null)  modifFiles.remove(analysisRes.stderrFile);
				if (beforeCmdRes != null) modifFiles.remove(beforeCmdRes.stdoutFile);
				if (beforeCmdRes != null) modifFiles.remove(beforeCmdRes.stderrFile);
				if (afterCmdRes  != null) modifFiles.remove(afterCmdRes.stdoutFile);
				if (afterCmdRes  != null) modifFiles.remove(afterCmdRes.stderrFile);
			
		    	// add all modified or produced files as contributions to the automation result
		    	Collection<Contribution> fileContributions = createFileContributions(modifFiles);
		    	for (Contribution c : fileContributions) {
		    		resAutoResult.addContribution(c);
		    	}
				statusMessage.appendValue("File Contributions added\n");
		    	
		    	// now add them back in there (for zip later)	[hack]
				if (analysisRes != null)  modifFiles.add(analysisRes.stdoutFile);
				if (analysisRes != null)  modifFiles.add(analysisRes.stderrFile);
				if (beforeCmdRes != null) modifFiles.add(beforeCmdRes.stdoutFile);
				if (beforeCmdRes != null) modifFiles.add(beforeCmdRes.stderrFile);
				if (afterCmdRes  != null) modifFiles.add(afterCmdRes.stdoutFile);
				if (afterCmdRes  != null) modifFiles.add(afterCmdRes.stderrFile);
				
				// create a zip of all file contributions if needed
				if (zipOutputs.equalsIgnoreCase("true"))
				{
					final String zipName = "out" + this.execAutoRequestId + ".zip";
					final Path zipDir = FileSystems.getDefault().getPath(this.execSut.getSUTdirectoryPath());
					
					try {
						Contribution zipContrib = zipAllFileContributions(modifFiles, zipName, zipDir);
						resAutoResult.addContribution(zipContrib);
						statusMessage.appendValue("Added a ZIP contribution\n");
					} catch (Exception e) {
						statusMessage.appendValue("Failed to create a ZIP file: " + e.getMessage() + "\n");
						System.out.println("ERROR: failed to ZIP outputs: " + e.getMessage()); // TODO
					}
				}

				resAutoResult.addContribution(statusMessage);
				
				// run the AutoResult contributions through a filter
				try {
					Set<Contribution> parsedContributions = FilterManager.filterContributionsForTool(
							autoPlanConf.getFilter(outputFilter),
							resAutoResult.getContribution(),
							this.resAutoResultId + "-"
							);
					resAutoResult.setContribution(parsedContributions);
					statusMessage.appendValue("Applying output filters\n");
				} catch (Exception e) {
					statusMessage.appendValue("Failed to apply filters - " + e.getMessage() + "\n");
				}

			}
			
			// update the AutoResult state and verdict, and AutoRequest state
			resAutoResult.replaceState(OslcValues.AUTOMATION_STATE_COMPLETE);
			resAutoResult.replaceVerdict(executionVerdict);
			VeriFitAnalysisManager.internalUpdateAutomationResult(resAutoResult, Utils.getResourceIdFromUri(resAutoResult.getAbout()));
			execAutoRequest.replaceState(OslcValues.AUTOMATION_STATE_COMPLETE);
			VeriFitAnalysisManager.internalUpdateAutomationRequest(execAutoRequest, execAutoRequestId);

		} catch (InterruptedException e) {
			// this automation request execution was canceled
			try {
				Thread.sleep(500);	// TODO this sleep is needed otherwise the delete below fails (not sure why)
			} catch (InterruptedException e2) {
				// should not happen
			}
			
			for (File f : this.filesToDeleteIfInterrupted) {
				try {
					FileDeleteStrategy.FORCE.delete(f);
				} catch (IOException e1) {
					log.error("Failed to delete a file during cleanup after a runner was interrupted", e1);
				}
			}
			
			// TODO currently does not delete any files outside of the .adapter directory! Those will just stay there lying around
			
		} catch (Exception e) {
			log.error("Unexpected error during request execution!", e);
		} finally {
			// notify the execution manager that this execution finished
			// in a finally clause to make sure it gets called no matter what 
			executionFinishedNotifyManager();
		}
	}
	
	private void createConfFile(Path sUTdirAsPath, String confFileParam) throws IOException {
		int idxSplit = confFileParam.indexOf('\n');
		if (idxSplit == -1)
			throw new IllegalArgumentException("Invalid format of confFile value. No \"\\n\" delimiter found. Expected format: filename\\nfile_contents");
		
		String filename = confFileParam.substring(0, confFileParam.indexOf('\n'));
		String file_contents = confFileParam.substring(confFileParam.indexOf('\n') + 1);
		
		File confFile = sUTdirAsPath.resolve(filename).toAbsolutePath().toFile();
		InputStream sFileContents = new ByteArrayInputStream(file_contents.getBytes());
	    if (!confFile.exists())
	    {
	    	FileUtils.forceMkdirParent(confFile);
	    }
		FileUtils.copyInputStreamToFile(sFileContents, confFile);
		
		this.filesToDeleteIfInterrupted.add(confFile);
	}

	private void createConfDir(Path sUTdirAsPath, String confDirParam) throws IOException {
		

		int idxSplit = confDirParam.indexOf('\n');
		if (idxSplit == -1)
			throw new IllegalArgumentException("Invalid format of confDir value. No \"\\n\" delimiter found. Expected format: filename\\nbase64");	// should never happen because this gets checked earlier
		
		String pathToUnzipTo = confDirParam.substring(0, confDirParam.indexOf('\n'));
		String base64 = confDirParam.substring(confDirParam.indexOf('\n') + 1);
		
		Decoder decoder = Base64.getDecoder();
		byte[] decodedDir = decoder.decode(base64);
		File unzipTo = sUTdirAsPath.resolve(pathToUnzipTo).toAbsolutePath().toFile();
		File zipFile = sUTdirAsPath.resolve(".adapter").resolve("confDir.zip").toAbsolutePath().toFile();
		InputStream sFileContents = new ByteArrayInputStream(decodedDir);
	    if (!unzipTo.exists())
	    {
	    	FileUtils.forceMkdirParent(unzipTo);
	    }
		FileUtils.copyInputStreamToFile(sFileContents, zipFile);
		
    	Utils.unzipFile(unzipTo.toPath(), zipFile);
		
		this.filesToDeleteIfInterrupted.add(unzipTo);
	}

	private Contribution zipAllFileContributions(Collection<File> modifFiles, String zipName, Path zipDir) throws IOException {
		Contribution zipedContribs = VeriFitAnalysisResourcesFactory.createContribution("zipedOutputs");
		Path pathToZip = zipDir.resolve(zipName);

		File newZipFile = Utils.zipFiles(modifFiles, zipDir, pathToZip);

		zipedContribs.setFilePath(newZipFile.getPath());
		zipedContribs.setDescription("This is a ZIP of all other file contributions. "
				+ "To download the file directly send a GET accepting application/octet-stream to the URI of this resource");
		zipedContribs.setTitle(zipName);
		zipedContribs.addValueType(OslcValues.OSLC_VAL_TYPE_BASE64BINARY);
		
		return zipedContribs;
	}

	/**
	 * Build the string to execute from input parameters based on their positions
	 * @param execParams
	 * @return The string to execute
	 */
	private String buildStringToExecFromParams(String toolCommand, List<ExecutionParameter> execParams)
	{
		toolCommand = (toolCommand.equalsIgnoreCase("true") ? this.autoPlanConf.getLaunchCommand() : ""); // insert the analysis tool launch command if the parameter was true
		
		String buildStringToExecute = ""
			+  toolCommand + " "
			+ this.autoPlanConf.getToolSpecificArgs();
		
		List<ExecutionParameter> cmdLineParams = new ArrayList<ExecutionParameter>();
		for (ExecutionParameter param : execParams)
		{
			if (param.isCmdLineParameter())
				cmdLineParams.add(param);
		}
		
		// sort all input params based on their command line position and append them to the string to execute
		cmdLineParams.sort((ExecutionParameter a, ExecutionParameter b) -> a.getCmdPosition().compareTo(b.getCmdPosition()));
		for (ExecutionParameter param : cmdLineParams)
		{
			buildStringToExecute += " " + param.getValuePrefix() + param.getValue();
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
		
		int id_counter = 0;
		for (File currFile : modifFiles)
		{
			Contribution newContrib = VeriFitAnalysisResourcesFactory.createContribution("file" + id_counter);
		    newContrib.setFilePath(currFile.getPath());
		    newContrib.setDescription("File produced or modified during execution of this Automation Request. "
		    		+ "To download the file directly send a GET accepting application/octet-stream to the URI of this resource. "
		    		+ "To modify the file send a regular OSLC update PUT request for a Contribution resource to the URI of this resource"
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
		    id_counter++;
		}
		
		return contributions;
	}
	
	private Map<String,String> prepareEvnVariableMapFromPrams(Collection<String> paramEnvVariables)
	{
		Map<String,String> res = new HashMap<String,String>();
		for (String envVarTwoLines : paramEnvVariables)
		{
			int idxSplit = envVarTwoLines.indexOf('\n');
			if (idxSplit == -1)
				throw new IllegalArgumentException("Invalid format of envVariable value. No \"\\n\" delimiter found. Expected format: variable_name\\nvariable_value"); // should never happen, gets checked earlier
			
			String varName = envVarTwoLines.substring(0, envVarTwoLines.indexOf('\n'));
			String varValue = envVarTwoLines.substring(envVarTwoLines.indexOf('\n') + 1);
			
			res.put(varName, varValue);
		}
		
		return res;
	}
	
}