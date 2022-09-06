/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.compilation.automationRequestExecution;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc.domains.auto.Contribution;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.group.verifit.oslc.OslcValues;
import cz.vutbr.fit.group.verifit.oslc.compilation.VeriFitCompilationConstants;
import cz.vutbr.fit.group.verifit.oslc.compilation.VeriFitCompilationManager;
import cz.vutbr.fit.group.verifit.oslc.compilation.VeriFitCompilationResourcesFactory;
import cz.vutbr.fit.group.verifit.oslc.compilation.automationRequestExecution.sutFetcher.SutFetchBase64;
import cz.vutbr.fit.group.verifit.oslc.compilation.automationRequestExecution.sutFetcher.SutFetchFileSystem;
import cz.vutbr.fit.group.verifit.oslc.compilation.automationRequestExecution.sutFetcher.SutFetchGit;
import cz.vutbr.fit.group.verifit.oslc.compilation.automationRequestExecution.sutFetcher.SutFetchUrl;
import cz.vutbr.fit.group.verifit.oslc.compilation.automationRequestExecution.sutFetcher.SutFetcher;
import cz.vutbr.fit.group.verifit.oslc.compilation.properties.VeriFitCompilationProperties;
import cz.vutbr.fit.group.verifit.oslc.domain.SUT;
import cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution.ExecutionParameter;
import cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution.RequestRunner;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;

/**
 * A thread for executing the Deploy SUT Automation Plan.
 * @author od42
 *
 */
public class SutDeploy extends RequestRunner
{
    private static final Logger log = LoggerFactory.getLogger(SutDeploy.class);
    
	final private String execAutoRequestId;
	private AutomationRequest execAutoRequest;
	final private String resAutoResultId;
	private AutomationResult resAutoResult;
	final private List<ExecutionParameter> execParameters;
	
	private Collection<File> filesToDeleteIfInterrupted = new ArrayList<File>();
	
	/**
	 * @param execAutoRequest	Executed AutomationRequest resource object
	 * @param execAutoResult	Result AutomationResult resource object
	 * @param inputParamsMap	Input parameters as a "name" => "value" map
	 */
	public SutDeploy(AutomationRequest execAutoRequest, AutomationResult resAutoResult, List<ExecutionParameter> execParameters) 
	{
		super(  OslcValues.getResourceIdFromUri(execAutoRequest.getAbout()),
				OslcValues.getResourceIdFromUri(execAutoRequest.getExecutesAutomationPlan().getValue()),
				execAutoRequest.getState().iterator().next(),
				execAutoRequest.getDesiredState()
		);
		
		this.execParameters = execParameters;
		this.execAutoRequestId = OslcValues.getResourceIdFromUri(execAutoRequest.getAbout());
		this.execAutoRequest = execAutoRequest;
		this.resAutoResultId = OslcValues.getResourceIdFromUri(resAutoResult.getAbout());
		this.resAutoResult = resAutoResult;
		
	}

	/** 
	 * Thread main
	 */
	public void run()
	{
		try {
			// input parameters
			String paramSourceGit = null;
			String paramSourceUrl =  null;
			String paramSourceBase64 =  null;
			String paramSourceFilePath =  null;
			String paramBuildCommand =  null;
			String paramLaunchCommand =  null;
			String paramUnpackZip =  null;
			String paramNoCompilation =  null;
			
			// extract values from parameters
			for (ExecutionParameter param : this.execParameters)
			{ 
				if (param.getName().equals("sourceGit")) paramSourceGit = param.getValue();
				else if (param.getName().equals("sourceUrl")) paramSourceUrl = param.getValue();
				else if (param.getName().equals("sourceBase64")) paramSourceBase64 = param.getValue();
				else if (param.getName().equals("sourceFilePath")) paramSourceFilePath = param.getValue();
				else if (param.getName().equals("buildCommand")) paramBuildCommand = param.getValue();
				else if (param.getName().equals("launchCommand")) paramLaunchCommand = param.getValue();
				else if (param.getName().equals("unpackZip")) paramUnpackZip = param.getValue();
				else if (param.getName().equals("noCompilation")) paramNoCompilation = param.getValue();
				
			}
			
			// check wich one of the source parameters was used 
			SutFetcher sutFetcher = null;
			String ProgramSource = "";
			if (paramSourceGit != null)
			{
				sutFetcher = new SutFetchGit();
				ProgramSource = paramSourceGit;
			}
			else if (paramSourceUrl != null)
			{
				sutFetcher = new SutFetchUrl();
				ProgramSource = paramSourceUrl;
			}
			else if (paramSourceBase64 != null)
			{
				sutFetcher = new SutFetchBase64();
				ProgramSource = paramSourceBase64;
			}
			else if (paramSourceFilePath != null)
			{
				sutFetcher = new SutFetchFileSystem();
				ProgramSource = paramSourceFilePath;
			}
			else
			{
				System.out.println("This should never happen!!");
			}
	
			// set the states of the Automation Result and Request to "inProgress" - if they are not that already
			if (!(execAutoRequest.getState().iterator().next().getValue()
					.equals(OslcValues.AUTOMATION_STATE_INPROGRESS.getValue())))
			{
				resAutoResult.replaceState(OslcValues.AUTOMATION_STATE_INPROGRESS);
				VeriFitCompilationManager.internalUpdateAutomationResult(resAutoResult, resAutoResultId);
				execAutoRequest.replaceState(OslcValues.AUTOMATION_STATE_INPROGRESS);
				VeriFitCompilationManager.internalUpdateAutomationRequest(execAutoRequest, execAutoRequestId);
			}		
			
			// prepare result contributions
			Contribution fetchLog = new Contribution();
			fetchLog.setDescription("Output of the program fetching process.");
			fetchLog.setTitle("Fetching Output");
			fetchLog.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			
			Contribution executionTime = new Contribution();
			executionTime.setDescription("Total execution time of the analysis in milliseconds."); // TODO CHECK really milliseconds?
			executionTime.setTitle("executionTime");
			executionTime.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			
			Contribution statusMessage = new Contribution();
			statusMessage.setDescription("Status messages from the adapter about the execution.");
			statusMessage.setTitle("statusMessage");
			statusMessage.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
			
			Contribution returnCode = new Contribution();
			returnCode.setDescription("Return code of the execution. If non-zero, then the verdict will be #failed.");
			returnCode.setTitle("returnCode");
			returnCode.addValueType(OslcValues.OSLC_VAL_TYPE_INTEGER);	
			

		    /* TODO will need later (GET for contributions)
			Contribution compStdoutLog = VeriFitCompilationResourcesFactory.createContribution(this.resAutoResultId + "-" + "stdout");
		    */
			Contribution compStdoutLog = new Contribution();
			compStdoutLog.setDescription("Standard output of the compilation.");
			compStdoutLog.setTitle("stdout");
			compStdoutLog.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);

		    /* TODO will need later (GET for contributions)
			Contribution compStdoutLog = VeriFitCompilationResourcesFactory.createContribution(this.resAutoResultId + "-" + "stderr");
		    */
			Contribution compStderrLog = new Contribution();
			compStderrLog.setDescription("Error output of the compilation.");
			compStderrLog.setTitle("stderr");
			compStderrLog.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);

			Contribution sutAsContribution = new Contribution();
			sutAsContribution.setDescription("Created SUT resource. Also linked to by the createdSUT property.");
			sutAsContribution.setTitle("SUT");
			sutAsContribution.addValueType(new Link (new URI (VeriFitCompilationProperties.PATH_RESOURCE_SHAPES + "sUT")));
			
			// init compilation toggle flag
			Boolean performCompilation = true;
			if (paramNoCompilation != null && Boolean.valueOf(paramNoCompilation))
				performCompilation = false;
			Link executionVerdict = OslcValues.AUTOMATION_VERDICT_PASSED;
			
			// fetch source file
			Path folderPath = null;
			try {
				// create the program path and name
				folderPath = createSutDir(execAutoRequestId);
				this.filesToDeleteIfInterrupted.add(folderPath.toAbsolutePath().toFile());
				String filenameSUT = "";	// used for optional unpacking later
				
			    // get the source file
				filenameSUT = sutFetcher.fetchSut(ProgramSource, folderPath);
				fetchLog.setValue("TODO currently only shows error messages");
				
			    // unzip the SUT if requested
			    if (paramUnpackZip != null && paramUnpackZip.equalsIgnoreCase("true"))
			    {
			    	File sutZipFile = folderPath.resolve(filenameSUT).toFile();
			    	Utils.unzipFile(folderPath, sutZipFile);
			    }
			    
			    statusMessage.setValue("SUT fetch successful\n");
			
			} catch (Exception e) {
				executionVerdict = OslcValues.AUTOMATION_VERDICT_ERROR;
				statusMessage.setValue("SUT fetch failed: " + e.getMessage() + "\n");
				fetchLog.setValue(e.getMessage());
				performCompilation = false;
	
			} finally {
				resAutoResult.addContribution(fetchLog);
			}
			
			// compile source file if the fetching did not fail and compilation was requested
			if (performCompilation)
			{

		    	// add file URIs to standard output contributions, add them to the automation result, and update it in the triplestore 
		    	// this allows clients to query the contents of stdout and stderr during execution
		    	String stdOutputsIdentifier = "_compilation_" + this.execAutoRequestId;
		    	compStdoutLog.setFilePath(folderPath.resolve(".adapter/stdout" + stdOutputsIdentifier).toAbsolutePath().toString());		// TODO HACK has to match the path used in executeString()
		    	compStderrLog.setFilePath(folderPath.resolve(".adapter/stderr" + stdOutputsIdentifier).toAbsolutePath().toString());		// TODO HACK 
		    	resAutoResult.addContribution(compStdoutLog);
				resAutoResult.addContribution(compStderrLog);
				VeriFitCompilationManager.internalUpdateAutomationResult(resAutoResult, OslcValues.getResourceIdFromUri(resAutoResult.getAbout()));


				// start execution
				ExecutionResult compRes = executeString(folderPath, paramBuildCommand, 0, "_compilation_" + this.execAutoRequestId, this.filesToDeleteIfInterrupted, null, VeriFitCompilationProperties.CONFIG_OS);
				statusMessage.appendValue("Executing: " + paramBuildCommand + "\n   as: " + compRes.executedString + "\n   In dir: " + folderPath + "\n");
				if (compRes.exceptionThrown != null)
				{
					// there was an error
					executionVerdict = OslcValues.AUTOMATION_VERDICT_ERROR;
					statusMessage.appendValue("Compilation execution error: " + compRes.exceptionThrown.getMessage());
				}
				else if (compRes.retCode != 0)
		    	{	// if the compilation returned non zero, set the verdict as failed
					executionVerdict = OslcValues.AUTOMATION_VERDICT_FAILED;
					statusMessage.appendValue("Compilation failed (returned non-zero: " + compRes.retCode + ")\n");
		    	}
		    	else
		    	{
		    		statusMessage.appendValue("Compilation completed successfully\n");
		    	}				    
				
				// only do more processing if there was no exception during execution
				if (executionVerdict != OslcValues.AUTOMATION_VERDICT_ERROR)
				{
					// add contributions to the automation result
					executionTime.setValue(Long.toString(compRes.totalTime));
					resAutoResult.addContribution(executionTime);
					returnCode.setValue(Integer.toString(compRes.retCode));
					resAutoResult.addContribution(returnCode);
	
					// load stdout and stderr file contents into contribution values
					try {
						compStdoutLog.setValue(Utils.removeAnsiAndNonXML10Chars(new String(Files.readAllBytes(compRes.stdoutFile.toPath()))));
					} catch (IOException e) {
						compStdoutLog.setValue("Failed to load contents of this file: " + e.getMessage());
					}
					compStdoutLog.setFilePath(compRes.stdoutFile.getAbsolutePath());
			    	try {
						compStderrLog.setValue(Utils.removeAnsiAndNonXML10Chars(new String(Files.readAllBytes(compRes.stderrFile.toPath()))));
					} catch (IOException e) {
						compStderrLog.setValue("Failed to load contents of this file: " + e.getMessage());
					}
			    	compStderrLog.setFilePath(compRes.stderrFile.getAbsolutePath());

			    	// the stdout and stderr already were added earlier, need to remove them and replace them with the new versions
			    	// (this will directly modify the resAutoResult.contributions because of pass by reference in java)
					Set<Contribution> tmpContribs = resAutoResult.getContribution();
					tmpContribs.removeIf(c -> (c.getTitle().equals("stdout") || c.getTitle().equals("stderr")));
					tmpContribs.add(compStdoutLog);
					tmpContribs.add(compStderrLog);
				}
			} else {
				statusMessage.appendValue("Compilation not performed\n");
			}
			
			
			// create the SUT resource if the compilation was successful
			if (executionVerdict == OslcValues.AUTOMATION_VERDICT_PASSED)
			{
				SUT newSut = new SUT();
				newSut.setTitle("SUT - " + execAutoRequest.getTitle());
				newSut.setLaunchCommand(paramLaunchCommand);
				if (!(paramBuildCommand == null)) //TODO
					newSut.setBuildCommand(paramBuildCommand);
				newSut.setCompiled(performCompilation);
				newSut.setSUTdirectoryPath(folderPath.toAbsolutePath().toString());
				newSut.setCreator(execAutoRequest.getCreator());
				newSut.setProducedByAutomationRequest(VeriFitCompilationResourcesFactory.constructLinkForAutomationRequest(execAutoRequestId));
				newSut = VeriFitCompilationManager.createSUT(newSut, execAutoRequestId); // TODO
				resAutoResult.setCreatedSUT(VeriFitCompilationResourcesFactory.constructLinkForSUT(OslcValues.getResourceIdFromUri(newSut.getAbout()))); // TODO
				sutAsContribution.setValue(newSut.getAbout().toString());	// add the SUT as an actual Contribution resource as well for standard compliance
				resAutoResult.addContribution(sutAsContribution); 
				
				statusMessage.appendValue("SUT resource created\n");
			}
			
			// update the AutoResult state and verdict, and AutoRequest state
			resAutoResult.addContribution(statusMessage);
			resAutoResult.replaceState(OslcValues.AUTOMATION_STATE_COMPLETE);
			resAutoResult.replaceVerdict(executionVerdict);
			VeriFitCompilationManager.internalUpdateAutomationResult(resAutoResult, OslcValues.getResourceIdFromUri(resAutoResult.getAbout()));
			execAutoRequest.replaceState(OslcValues.AUTOMATION_STATE_COMPLETE);
			VeriFitCompilationManager.internalUpdateAutomationRequest(execAutoRequest, execAutoRequestId);

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
			
		} catch (Exception e) {
			log.error("Unexpected error during request execution!", e);
		} finally {
			// notify the execution manager that this execution finished
			// in a finally clause to make sure it gets called no matter what 
			executionFinishedNotifyManager();
		}
	}
	

	/**
	 * Creates a folder in the "SUT" adapter folder
	 * @param subfolder How to name the subfolder
	 * @return	Path to the new folder
	 * @throws IOException 
	 */
	protected Path createSutDir(String subfolder) throws IOException
	{
		Path subfolderPath = FileSystems.getDefault().getPath(VeriFitCompilationProperties.SUT_FOLDER).resolve(subfolder);
	    
	    if (!Files.exists(subfolderPath))
	    {   
            Files.createDirectory(subfolderPath);
	    }
	    return subfolderPath;
	}

	/**
	 * Generate a filename for an SUT
	 * @param autoRequestId ID of the executed AutomationRequest
	 * @return Name for the new file
	 */
	protected String genFileName(String autoRequestId)
	{
	    String currentDate = new Date().toString().replace(' ', '-');
	    return "req" + autoRequestId + "." + currentDate;
	}
}