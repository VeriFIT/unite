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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	final private AutomationPlanConf autoPlanConf;
	
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
			
			// extract values from parameters
			for (ExecutionParameter param : this.execParameters)
			{ 
				if (param.getName().equals("outputFileRegex")) outputRegex = param.getValue();
				else if (param.getName().equals("zipOutputs")) zipOutputs = param.getValue();
				else if (param.getName().equals("timeout")) timeout = param.getValue();
				else if (param.getName().equals("toolCommand")) toolCommand = param.getValue();
				else if (param.getName().equals("outputFilter")) outputFilter = param.getValue();
				
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
	
		    // prepare Contribution resources
			Contribution executionTime = VeriFitAnalysisResourcesFactory.createContribution("executionTime");
			executionTime.setDescription("Total execution time of the analysis in milliseconds."); // TODO CHECK really milliseconds?
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
			
		    // warning if not compiled
		    if (!execSut.isCompiled())
		    {
	    		statusMessage.setValue(statusMessage.getValue() + "Warning: Analysing an SUT which was not compiled\n");
		    }
		    
		    // take a snapshot of SUT files modification times before executing the analysis
		    GetModifFilesBySnapshot snapshotter = new GetModifFilesBySnapshot(new File(execSut.getSUTdirectoryPath()));
		    snapshotter.takeBeforeSnapshot(outputRegex);
		    
			// execute analysis
		    Link executionVerdict;
			final Path SUTdirAsPath = FileSystems.getDefault().getPath(execSut.getSUTdirectoryPath());
		    ExecutionResult analysisRes = executeString(SUTdirAsPath, stringToExecute, Integer.parseInt(timeout), "_analysis_" + this.execAutoRequestId);
			statusMessage.setValue(statusMessage.getValue() +
					"Executing: " + stringToExecute + "\n   as: " + analysisRes.executedString + "\n   In dir: " + SUTdirAsPath + "\n");
			if (analysisRes.exceptionThrown != null)
			{
				// there was an error
				executionVerdict = OslcValues.AUTOMATION_VERDICT_ERROR;
				statusMessage.setValue(statusMessage.getValue() +  "Analysis execution error: " + analysisRes.exceptionThrown.getMessage());
			}
			else if (analysisRes.timeouted)
			{
				executionVerdict = OslcValues.AUTOMATION_VERDICT_FAILED;
				statusMessage.setValue(statusMessage.getValue() + "Analysis aborted due to a " + analysisRes.timeoutType + " timeout (" + timeout + " seconds)");
			}
	    	else if (analysisRes.retCode != 0)
	    	{
				executionVerdict = OslcValues.AUTOMATION_VERDICT_FAILED;
				statusMessage.setValue(statusMessage.getValue() + "Analysis failed (returned non-zero: " + analysisRes.retCode + ")\n");
			}
	    	else
	    	{
	    		executionVerdict = OslcValues.AUTOMATION_VERDICT_PASSED;
	    		statusMessage.setValue(statusMessage.getValue() + "Analysis completed successfully\n");
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
				statusMessage.setValue(statusMessage.getValue() + "File Contributions added\n");
		    	
		    	// now add them back in there (for zip later)
				modifFiles.add(analysisRes.stdoutFile);
				modifFiles.add(analysisRes.stderrFile);
		    	
		    	// add file URIs to standard output contributions and add them to the automation result
		    	analysisStdout.setFilePath(analysisRes.stdoutFile.getPath());
		    	resAutoResult.addContribution(analysisStdout);
		    	analysisStderr.setFilePath(analysisRes.stderrFile.getPath());
				resAutoResult.addContribution(analysisStderr);
		    	
				// create a zip of all file contributions if needed
				if (zipOutputs.equalsIgnoreCase("true"))
				{
					final String zipName = "out" + this.execAutoRequestId + ".zip";
					final Path zipDir = FileSystems.getDefault().getPath(this.execSut.getSUTdirectoryPath());
					
					try {
						Contribution zipContrib = zipAllFileContributions(modifFiles, zipName, zipDir);
						resAutoResult.addContribution(zipContrib);
						statusMessage.setValue(statusMessage.getValue() + "Added a ZIP contribution\n");
					} catch (Exception e) {
						statusMessage.setValue(statusMessage.getValue() + "Failed to create a ZIP file: " + e.getMessage() + "\n");
						System.out.println("ERROR: failed to ZIP outputs: " + e.getMessage()); // TODO
					}
				}
				
				// run the AutoResult contributions through a filter
				statusMessage.setValue(statusMessage.getValue() + "Applying output filters\n");
				resAutoResult.addContribution(statusMessage);
				Set<Contribution> parsedContributions = FilterManager.filterContributionsForTool(
						autoPlanConf.getFilter(outputFilter),
						resAutoResult.getContribution(),
						this.resAutoResultId + "-"
						);
				resAutoResult.setContribution(parsedContributions);
			}
			
			// update the AutoResult state and verdict, and AutoRequest state
			resAutoResult.replaceState(OslcValues.AUTOMATION_STATE_COMPLETE);
			resAutoResult.replaceVerdict(executionVerdict);
			VeriFitAnalysisManager.internalUpdateAutomationResult(resAutoResult, Utils.getResourceIdFromUri(resAutoResult.getAbout()));
			execAutoRequest.replaceState(OslcValues.AUTOMATION_STATE_COMPLETE);
			VeriFitAnalysisManager.internalUpdateAutomationRequest(execAutoRequest, execAutoRequestId);

		} catch (InterruptedException e) {
			// this automation request execution was canceled
			
		} catch (Exception e) {
			log.error("Unexpected error during request execution!", e);
		} finally {
			// notify the execution manager that this execution finished
			// in a finally clause to make sure it gets called no matter what 
			executionFinishedNotifyManager();
		}
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
			buildStringToExecute += " " + param.getValue();
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
	
}