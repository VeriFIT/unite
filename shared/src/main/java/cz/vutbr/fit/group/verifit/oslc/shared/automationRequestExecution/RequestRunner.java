/* 
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.lyo.oslc4j.core.model.Link;

/**
 * A thread designed to execute an AutomationRequest.
 * 
 * @author Ondřej Vašíček
 *
 */
public abstract class RequestRunner extends Thread {
	public RequestRunner(String executedAutomationRequestId, String executedAutomationPlanId, Link startingState, Link desiredState) {
		super();
		
		this.executedAutomationRequestId = executedAutomationRequestId;
		this.desiredState = desiredState;
		this.startingState = startingState;
	}
	
	/**
	 * Used to identify this runner
	 */
	private String executedAutomationRequestId;
	
	/**
	 * Used for queuing purposes
	 */
	private String executedAutomationPlanId;
	
	/**
	 * Used to determine whether to start execution
	 */
	private Link desiredState;
	
	/**
	 * Used for queuing purposes
	 */
	private Link startingState;

	public String getExecutedAutomationPlanId() {
		return executedAutomationPlanId;
	}

	private IExecutionManager execManager; 
	
	public String getExecutedAutomationRequestId() {
		return executedAutomationRequestId;
	}

	public Link getDesiredState() {
		return desiredState;
	}

	public Link getStartingState() {
		return startingState;
	}
	
	public void setExecManager(IExecutionManager execManager) {
		this.execManager = execManager;
	}
	
	protected void executionFinishedNotifyManager()
	{
		execManager.executionFinishedCallback(this);
	}
	
	public class ExecutionResult {
		public Integer retCode;
		public File stdoutFile;
		public File stderrFile;
		public Boolean timeouted;
		public String timeoutType;
		public Long totalTime;
		public String executedString;
		public Exception exceptionThrown;
	};
	/**
	 * Enum for supported OS versions --> controls which shell to use
	 */
	public enum ConfigOs {
		LINUX,
		WINDOWS_PS1,
		WINDOWS_BAT
	}

	/**
	 * Places the stringToExecute into a script file and then executes that script using OS specific shell in a directory while
	 * redirecting its output into files with an optional timeout. Both the script and the output files are placed in a ".adapter" directory.
	 * Stdout gets redirected to "stdoutID" and Stderr to "stderrID" where ID is the value of the "id" parameter.
	 * 
	 * @param folderPath      Path to the directory
	 * @param stringToExecute 
	 * @param timeout         Time limit for execution in seconds. Zero
	 *                        means no time limit
	 * @param id 			  Identifier that is appended to the stdout and stderr output file names (e.g. id="1" -> "stdout1", "stderr1")
	 * @param filesToDeleteIfInterrupted	A collection of Files to be deleted in case the execution is interrupted. An output parameter.
	 * @param envVariables	  a map of name:value holding environment variables to be set when executing
	 * @param configOs	      used to control what execution shell to use
	 * @return a "ExecutionResult" object that holds the stdout, stderr, return code, timeout flag, etc (see the ExecutionResult class)
	 * @throws InterruptedException 
	 */
	protected ExecutionResult executeString(Path folderPath, String stringToExecute, int timeout, String id, Collection<File> filesToDeleteIfInterrupted, Map<String,String> envVariables, ConfigOs configOs) throws InterruptedException
	{
		final String powershellExceptionExitCode = "1";
		
		// identify the OS and tailor the execution for it
		String shell;
		String shellArg = "";
		String fileEnding;
		String scriptContents;
		String execArg = "";
		String pathSlash = "/";
		if (configOs == ConfigOs.LINUX) {
			shell = "/bin/bash";
			fileEnding = ".sh";
			execArg = stringToExecute;
			scriptContents = shell + " -c \"$1\"" + "\n"
					+ "exit $?" + "\n"
					+ "# $1 was: " + execArg; 
		} else {
			if (configOs == ConfigOs.WINDOWS_BAT) // if CMD
			{
				shell = "cmd.exe";
				pathSlash = "\\";
				shellArg = "/c";	// to avoid changing directories due to a powershell profile
				fileEnding = ".bat";
				scriptContents = "@echo off" + "\n" + stringToExecute; 
			}
			else // if (configOs == ConfigOs.WINDOWS_PS1) // Powershell
			{
				shell = "powershell.exe";
				shellArg = "-NoProfile";	// to avoid changing directories due to a powershell profile
				fileEnding = ".ps1";
				scriptContents = "try { \n"
						+ "& " + stringToExecute + "\n"
						+ "exit $LastExitCode\n"
						+ "} catch {\n"
						+ "  Write-Error $_\n"
						+ "  exit " + powershellExceptionExitCode + "\n"
						+ "}";
			}
		}


		String executedString = null;
		Process process = null;
		try {
			// create a directory for execution outputs 
			Path outputsDir = folderPath.resolve(".adapter");
		    if (!outputsDir.toFile().exists())
		    {
		    	outputsDir.toFile().mkdirs();
		    }
			
			// put the string to execute into a script file
			InputStream streamFileToExec = new ByteArrayInputStream(scriptContents.getBytes());
			File fileToExecute = outputsDir.resolve("exec" + id + fileEnding).toFile();
			String scriptToExecute = "." + pathSlash + ".adapter" + pathSlash + fileToExecute.getName();
			executedString = shell + " " + scriptToExecute + " \"" + execArg + "\"";
			FileUtils.copyInputStreamToFile(streamFileToExec, fileToExecute);
			
			// build process to execute the string in a directory using OS specific shell with outputs redirected to files
		    ProcessBuilder processBuilder;
		    if (shellArg.equals("")) {
		    	processBuilder = new ProcessBuilder(shell, scriptToExecute, execArg);
		    } else {
		    	processBuilder = new ProcessBuilder(shell, shellArg, scriptToExecute, execArg);
		    }
		    processBuilder.directory(folderPath.toFile());
			File stdoutFile = outputsDir.resolve("stdout" + id).toFile();
			File stderrFile = outputsDir.resolve("stderr" + id).toFile();
			processBuilder.redirectOutput(stdoutFile);
			processBuilder.redirectError(stderrFile);
			
			// set env variables
			Map<String, String> processEnv = processBuilder.environment();
			if (envVariables != null)
				for (Map.Entry<String,String> envVar : envVariables.entrySet())
					processEnv.put(envVar.getKey(), envVar.getValue());
			
		    // set files to be deleted in case of interrupt
			filesToDeleteIfInterrupted.add(fileToExecute);
			filesToDeleteIfInterrupted.add(stdoutFile);
			filesToDeleteIfInterrupted.add(stderrFile);
			
			// start execution and get a timestamp of the start to measure execution time
			Instant timeStampStart = Instant.now();
			process = processBuilder.start();
			
			// wait for the process to exit
			Boolean exitedInTime = true;
			String timeoutType = "";
			// with timeout
			if (timeout > 0) {
				exitedInTime = process.waitFor(timeout, TimeUnit.SECONDS);
				if (!exitedInTime) {
					// try to kill gracefully
					timeoutType = "graceful";
					process.destroy();
					Boolean killedInTime = process.waitFor(1, TimeUnit.SECONDS); // TODO one second to die
					if (!killedInTime) {
						// kill forcefully
						timeoutType = "forceful";
						process.destroyForcibly();
					}
				}
			}
			// no timeout
			else {
				process.waitFor();
			}
	
			// compute total execution time
			Instant timeStampEnd = Instant.now(); // get execution end time
			long totalTime = Duration.between(timeStampStart, timeStampEnd).toMillis();
	
			
			// produce result
			ExecutionResult res = new ExecutionResult();
			res.retCode = process.exitValue();
			res.stdoutFile = stdoutFile;
			res.stderrFile = stderrFile;
			res.timeouted = !exitedInTime;
			res.timeoutType = timeoutType;
			res.totalTime = totalTime;
			res.executedString = executedString;
			res.exceptionThrown = null;
			return res;
		} catch (InterruptedException e) {
			// this automation request execution was canceled, kill the running process and re-throw higher 
			if (process != null)
			{
				// try to kill gracefully
				process.destroy();
				Boolean killedInTime = process.waitFor(1, TimeUnit.SECONDS); // TODO one second to die
				if (!killedInTime) {
					// kill forcefully
					process.destroyForcibly();
				}
			}
			throw e;
		} catch (Exception e) {
			ExecutionResult res = new ExecutionResult();
			res.retCode = null;
			res.stdoutFile = null;
			res.stderrFile = null;
			res.timeouted = null;
			res.timeoutType = null;
			res.totalTime = null;
			res.executedString = executedString;
			res.exceptionThrown = e;
			return res;
		}
	}
} 