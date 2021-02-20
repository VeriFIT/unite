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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
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
import cz.vutbr.fit.group.verifit.oslc.domain.SUT;
import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;
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
	final private Map<String, Pair<String,Integer>> inputParamsMap;
	
	/**
	 * @param execAutoRequest	Executed AutomationRequest resource object
	 * @param execAutoResult	Result AutomationResult resource object
	 * @param inputParamsMap	Input parameters as a "name" => "value" map
	 */
	public SutDeploy(AutomationRequest execAutoRequest, AutomationResult resAutoResult, Map<String, Pair<String,Integer>> inputParamsMap) 
	{
		super();
		
		this.inputParamsMap = inputParamsMap;
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
		
		// get the input parameters	// TODO .getLeft() is ugly	
		final String paramSourceGit = (inputParamsMap.get("sourceGit") == null) ? null : inputParamsMap.get("sourceGit").getLeft();
		final String paramSourceUrl = (inputParamsMap.get("sourceUrl") == null) ? null : inputParamsMap.get("sourceUrl").getLeft();
		final String paramSourceBase64 = (inputParamsMap.get("sourceBase64") == null) ? null : inputParamsMap.get("sourceBase64").getLeft();
		final String paramSourceFilePath = (inputParamsMap.get("sourceFilePath") == null) ? null : inputParamsMap.get("sourceFilePath").getLeft();
		final String paramBuildCommand = (inputParamsMap.get("buildCommand") == null) ? null : inputParamsMap.get("buildCommand").getLeft();
		final String paramLaunchCommand = (inputParamsMap.get("launchCommand") == null) ? null : inputParamsMap.get("launchCommand").getLeft();
		final String paramUnpackZip = (inputParamsMap.get("unpackZip") == null) ? null : inputParamsMap.get("unpackZip").getLeft();
		final String paramTimeout = (inputParamsMap.get("timeout") == null) ? null : inputParamsMap.get("timeout").getLeft();

		// check wich one of the source parameters was used 
		SutFetcher sutFetcher = null;
		String ProgramDefinition = "";	//TODO use something else than a string (speed)
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
		
		
		// prepare result contributions - program fetching, compilation
		Contribution fetchLog = new Contribution();
		fetchLog.setDescription("Output of the program fetching process. Provider messages are prefixed with #.");
		fetchLog.setTitle("Fetching Output");
		fetchLog.addValueType(OslcValues.OSLC_VAL_TYPE_STRING);
		//fetchLog.addType(new Link(new URI("http://purl.org/dc/dcmitype/Text"))); //TODO
		
		Contribution executionTime = new Contribution();
		executionTime.setDescription("Total execution time of the analysis in milliseconds."); // TODO CHECK really milliseconds?
		executionTime.setTitle("stdout");
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
		
		
		Boolean performCompilation = true;	// flag to disable a part of the execution in case of an error
		Link executionVerdict = OslcValues.AUTOMATION_VERDICT_PASSED;
		
		// fetch source file
		Path folderPath = null;
		try {
			// create the program path and name
			folderPath = createSutDir(execAutoRequestId);
			String filenameSUT = "";	// used for optional unpacking later
			
		    // get the source file
			filenameSUT = sutFetcher.fetchSut(ProgramSource, folderPath);
		    
		    // unzip the SUT if requested
		    if (paramUnpackZip.equals("true") || paramUnpackZip.equals("True"))
		    {
		    	unzipFile(folderPath, filenameSUT);
		    }
		    
		    statusMessage.setValue("SUT fetch successful\n");
		
		} catch (Exception e) {
			executionVerdict = OslcValues.AUTOMATION_VERDICT_ERROR;
			statusMessage.setValue("# SUT fetch failed: " + e.getMessage() + "\n");
			fetchLog.setValue(e.getMessage());
			performCompilation = false;

		} finally {
			// create the fetching log Contribution and add it to the AutomationResult
			resAutoResult.addContribution(fetchLog);
		}
		
		    
		// Do not compile if there was no buildCommand (e.g. for static analysis)
		if (paramBuildCommand == null || paramBuildCommand.equals("")) //TODO
			performCompilation = false;

		// compile source file if the fetching did not fail
		if (performCompilation)
		{
			ExecutionResult compRes = null;
			try {
				compRes = executeString(folderPath, paramBuildCommand, Integer.parseInt(paramTimeout), this.execAutoRequestId);
		    	
		    	if (compRes.retCode != 0)
		    	{	// if the compilation returned non zero, set the verdict as failed
					executionVerdict = OslcValues.AUTOMATION_VERDICT_FAILED;
					statusMessage.setValue(statusMessage.getValue() +  "Compilation failed (returned non-zero: " + compRes.retCode + ")\n");
		    	}
		    	else
		    	{
		    		statusMessage.setValue(statusMessage.getValue() +  "Compilation completed successfully\n");
		    	}				    
			} catch (IOException e) {
				// there was an error
				executionVerdict = OslcValues.AUTOMATION_VERDICT_ERROR;
				statusMessage.setValue(statusMessage.getValue() +  "Compilation execution error");
				
			} finally {
				resAutoResult.addContribution(statusMessage); // TODO add infos abou stuff below too
			}
			
			// only do more processing if there was no exception during execution
			if (executionVerdict != OslcValues.AUTOMATION_VERDICT_ERROR)
			{
				// add contributions to the automation result
				executionTime.setValue(Long.toString(compRes.totalTime));
				resAutoResult.addContribution(executionTime);
				returnCode.setValue(Integer.toString(compRes.retCode));
				resAutoResult.addContribution(returnCode);

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
		}
		
		
		// create the SUT resource if the compilation was successful
		if (executionVerdict == OslcValues.AUTOMATION_VERDICT_PASSED)
		{
			SUT newSut = new SUT();
			newSut.setTitle("SUT - " + execAutoRequest.getTitle());
			newSut.setLaunchCommand(paramLaunchCommand);
			if (!(paramBuildCommand == null || paramBuildCommand.equals(""))) //TODO
				newSut.setBuildCommand(paramBuildCommand);
			newSut.setSUTdirectoryPath(folderPath.toAbsolutePath().toString());
			newSut.setCreator(execAutoRequest.getCreator());
			newSut.setProducedByAutomationRequest(VeriFitCompilationResourcesFactory.constructLinkForAutomationRequest(execAutoRequestId));
			VeriFitCompilationManager.createSUT(newSut, execAutoRequestId); // TODO
			resAutoResult.setCreatedSUT(VeriFitCompilationResourcesFactory.constructLinkForSUT(Utils.getResourceIdFromUri(newSut.getAbout()))); // TODO
		}
		
		// update the AutoResult state and verdict, and AutoRequest state
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
		Path subfolderPath = FileSystems.getDefault().getPath("SUT").resolve(subfolder);
	    
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
	
	/**
	 * TODO
	 * @param folderPath
	 * @param filename
	 * @throws IOException
	 */
	protected void unzipFile(Path folderPath, String filename) throws IOException
	{
		Path pathToFile = folderPath.resolve(filename);

    	ZipFile zf = new ZipFile(pathToFile.toFile());
        Enumeration<? extends ZipEntry> zipEntries = zf.entries();
        while(zipEntries.hasMoreElements())
        {
        	ZipEntry entry = zipEntries.nextElement();
        	if (entry.isDirectory()) {
                Path dirToCreate = folderPath.resolve(entry.getName());
                Files.createDirectories(dirToCreate);
            } else {
            	Path fileToCreate = folderPath.resolve(entry.getName());
                Files.copy(zf.getInputStream(entry), fileToCreate);
            }
        }
	}
}