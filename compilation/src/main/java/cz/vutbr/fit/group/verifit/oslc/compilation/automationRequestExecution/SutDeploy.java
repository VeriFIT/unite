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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc.domains.auto.Contribution;
import org.eclipse.lyo.oslc4j.core.model.Link;

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
import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;
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
	final private String execAutoRequestId;
	private AutomationRequest execAutoRequest;
	final private String resAutoResultId;
	private AutomationResult resAutoResult;
	final private List<ExecutionParameter> execParameters;
	
	/**
	 * @param execAutoRequest	Executed AutomationRequest resource object
	 * @param execAutoResult	Result AutomationResult resource object
	 * @param inputParamsMap	Input parameters as a "name" => "value" map
	 */
	public SutDeploy(AutomationRequest execAutoRequest, AutomationResult resAutoResult, List<ExecutionParameter> execParameters) 
	{
		super();

		this.execParameters = execParameters;
		this.execAutoRequestId = Utils.getResourceIdFromUri(execAutoRequest.getAbout());
		this.execAutoRequest = execAutoRequest;
		this.resAutoResultId = Utils.getResourceIdFromUri(resAutoResult.getAbout());
		this.resAutoResult = resAutoResult;
	}

	/**
	 * Thread main
	 */
	public void run()
	{
		// input parameters
		String paramSourceGit = null;
		String paramSourceUrl =  null;
		String paramSourceBase64 =  null;
		String paramSourceFilePath =  null;
		String paramBuildCommand =  null;
		String paramLaunchCommand =  null;
		String paramUnpackZip =  null;
		String paramCompile =  null;
		
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
			else if (param.getName().equals("compile")) paramCompile = param.getValue();
			
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
		
		// set the states of the Automation Result and Request to "inProgress"
		resAutoResult.replaceState(OslcValues.AUTOMATION_STATE_INPROGRESS);
		VeriFitCompilationManager.updateAutomationResult(null, resAutoResult, resAutoResultId);
		execAutoRequest.replaceState(OslcValues.AUTOMATION_STATE_INPROGRESS);
		VeriFitCompilationManager.updateAutomationRequest(null, execAutoRequest, execAutoRequestId);
		
		
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
		
		Contribution compStdoutLog = new Contribution();
		compStdoutLog.setDescription("Standard output of the compilation.");
		compStdoutLog.setTitle("stdout");
		compStdoutLog.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
		
		Contribution compStderrLog = new Contribution();
		compStderrLog.setDescription("Error output of the compilation.");
		compStderrLog.setTitle("stderr");
		compStderrLog.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
		
		
		// init compilation toggle flag
		Boolean performCompilation = Boolean.valueOf(paramCompile);
		Link executionVerdict = OslcValues.AUTOMATION_VERDICT_PASSED;
		
		// fetch source file
		Path folderPath = null;
		try {
			// create the program path and name
			folderPath = createSutDir(execAutoRequestId);
			String filenameSUT = "";	// used for optional unpacking later
			
		    // get the source file
			filenameSUT = sutFetcher.fetchSut(ProgramSource, folderPath);
			fetchLog.setValue("TODO currently only shows error messages");
			
		    // unzip the SUT if requested
		    if (paramUnpackZip.equalsIgnoreCase("true"))
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
			ExecutionResult compRes = executeString(folderPath, paramBuildCommand, 0, "_compilation_" + this.execAutoRequestId);
			statusMessage.setValue(statusMessage.getValue() + "Executing: " + paramBuildCommand + "\n   as: " + compRes.executedString + "\n   In dir: " + folderPath + "\n");
			if (compRes.exceptionThrown != null)
			{
				// there was an error
				executionVerdict = OslcValues.AUTOMATION_VERDICT_ERROR;
				statusMessage.setValue(statusMessage.getValue() +  "Compilation execution error: " + compRes.exceptionThrown.getMessage());
			}
			else if (compRes.retCode != 0)
	    	{	// if the compilation returned non zero, set the verdict as failed
				executionVerdict = OslcValues.AUTOMATION_VERDICT_FAILED;
				statusMessage.setValue(statusMessage.getValue() +  "Compilation failed (returned non-zero: " + compRes.retCode + ")\n");
	    	}
	    	else
	    	{
	    		statusMessage.setValue(statusMessage.getValue() +  "Compilation completed successfully\n");
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
					compStdoutLog.setValue(new String(Files.readAllBytes(compRes.stdoutFile.toPath())));
				} catch (IOException e) {
					compStdoutLog.setValue("Failed to load contents of this file: " + e.getMessage());
				}
		    	resAutoResult.addContribution(compStdoutLog);
		    	try {
					compStderrLog.setValue(new String(Files.readAllBytes(compRes.stderrFile.toPath())));
				} catch (IOException e) {
					compStderrLog.setValue("Failed to load contents of this file: " + e.getMessage());
				}
		    	resAutoResult.addContribution(compStderrLog);
			}
		} else {
			statusMessage.setValue(statusMessage.getValue() + "Compilation not performed\n");
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
			resAutoResult.setCreatedSUT(VeriFitCompilationResourcesFactory.constructLinkForSUT(Utils.getResourceIdFromUri(newSut.getAbout()))); // TODO
			
			statusMessage.setValue(statusMessage.getValue() + "SUT resource created\n");
		}
		
		// update the AutoResult state and verdict, and AutoRequest state
		resAutoResult.addContribution(statusMessage);
		resAutoResult.replaceState(OslcValues.AUTOMATION_STATE_COMPLETE);
		resAutoResult.replaceVerdict(executionVerdict);
		VeriFitCompilationManager.updateAutomationResult(null, resAutoResult, Utils.getResourceIdFromUri(resAutoResult.getAbout()));
		execAutoRequest.setState(new HashSet<Link>());
		execAutoRequest.addState(OslcValues.AUTOMATION_STATE_COMPLETE);
		VeriFitCompilationManager.updateAutomationRequest(null, execAutoRequest, execAutoRequestId);
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