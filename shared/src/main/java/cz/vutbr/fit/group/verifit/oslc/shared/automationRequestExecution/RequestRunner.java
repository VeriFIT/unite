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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 * A thread designed to execute an AutomationRequest.
 * 
 * @author Ondřej Vašíček
 *
 */
public abstract class RequestRunner extends Thread {
	public RequestRunner() {
		super();
	}
	
	public class ExecutionResult {
		public int retCode;
		public File stdoutFile;
		public File stderrFile;
		public Boolean timeouted;
		public String timeoutType;
		public Long totalTime;
	};

	/**
	 * Executes a string in a directory while redirecting its output into files with an optional timeout.
	 * Stdout gets redirected to "stdoutID" and Stderr to "stderrID" where ID is the value of the "id" parameter.
	 * 
	 * @param folderPath      Path to the directory
	 * @param stringToExecute 
	 * @param timeout         Time limit for execution in seconds. Zero
	 *                        means no time limit
	 * @param id 			  Identifier 
	 * @return a "ExecutionResult" object that holds the stdout, stderr, return code,
	 *         and timeout flag
	 * @throws IOException when the command execution fails (error)
	 */
	protected ExecutionResult executeString(Path folderPath, String stringToExecute, int timeout, String id) throws IOException
	{
		// identify the OS
		String shell = (SystemUtils.IS_OS_LINUX ? "/bin/bash" : "powershell.exe");	// TODO assumes that "not linux" means "windows"
		String shellArg = (SystemUtils.IS_OS_LINUX ? "-c" : "");
		
		// execute string in directory
		Instant timeStampStart = Instant.now(); // get start time for measuring execution time
		Process process = Runtime.getRuntime().exec(new String[] {shell, shellArg, stringToExecute}, null, folderPath.toFile());	// launch string as "bash -c" or "cmd /c"	

		// redirect outputs to files
		InputStream stdout = process.getInputStream();
		InputStream stderr = process.getErrorStream();
		File stdoutFile = folderPath.resolve("stdout" + id).toFile();
		File stderrFile = folderPath.resolve("stderr" + id).toFile();

		// wait for the process to exit
		Boolean exitedInTime = true;
		String timeoutType = "";
		try {
			// with timeout
			if (timeout > 0) {
				exitedInTime = process.waitFor(timeout, TimeUnit.SECONDS);
				FileUtils.copyInputStreamToFile(stdout, stdoutFile);
				FileUtils.copyInputStreamToFile(stderr, stderrFile);
				
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
				FileUtils.copyInputStreamToFile(stdout, stdoutFile);
				FileUtils.copyInputStreamToFile(stderr, stderrFile);
			}

		} catch (InterruptedException e) {
			// TODO Dont know what that means really
			e.printStackTrace();
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
		return res;
	}
} 