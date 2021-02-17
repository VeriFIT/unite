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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
		public String stdout;
		public String stderr;
		public Boolean timeouted;
		public String timeoutType;
	};

	/**
	 * Executes a string in a directory.
	 * 
	 * @param folderPath      Path to the directory
	 * @param stringToExecute 
	 * @param timeout         Time limit for execution in seconds. Zero
	 *                        means no time limit
	 * @return a "ExecutionResult" object that holds the stdout, stderr, return code,
	 *         and timeout flag
	 * @throws IOException when the command execution fails (error)
	 */
	protected ExecutionResult executeString(Path folderPath, String stringToExecute, int timeout) throws IOException {
		Process process;
		String shell = (SystemUtils.IS_OS_LINUX ? "/bin/bash" : "cmd");	// TODO assumes that "not linux" means "windows"
		String shellArg = (SystemUtils.IS_OS_LINUX ? "-c" : "/c");
		process = Runtime.getRuntime().exec(new String[] {shell, shellArg, stringToExecute}, null, folderPath.toFile());	// launch string as "bash -c" or "cmd /c"	
		
		InputStream stdout = process.getInputStream();
		InputStream stderr = process.getErrorStream();
		InputStreamReader stdoutReader = new InputStreamReader(stdout);
		BufferedReader stdoutBuffReader = new BufferedReader(stdoutReader);
		InputStreamReader stderrReader = new InputStreamReader(stderr);
		BufferedReader stderrBuffReader = new BufferedReader(stderrReader);

		// spawn two threads to capture stdout and stderr while this thread takes care of the timeout
		ExecutorService outCaptureThreads = Executors.newFixedThreadPool(2);
		Future<String> stdoutLog = outCaptureThreads.submit(() -> {
			String line, output = "";		
			while ((line = stdoutBuffReader.readLine()) != null) {
				output = output.concat(line + "\n");
			}
			return output;
		});
		Future<String> stderrLog = outCaptureThreads.submit(() -> {
			String line, output = "";		
			while ((line = stderrBuffReader.readLine()) != null) {
				output = output.concat(line + "\n");
			}
			return output;
		});
		outCaptureThreads.shutdown();

		// wait for the process to exit
		Boolean exitedInTime = true;
		String timeoutType = "";
		try {
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

		} catch (InterruptedException e) {
			// TODO Dont know what that means really
			e.printStackTrace();
		}

		ExecutionResult res = new ExecutionResult();
		res.retCode = process.exitValue();
		res.timeouted = !exitedInTime;
		res.timeoutType = timeoutType;
		try {
			res.stdout = stdoutLog.get();
			res.stderr = stderrLog.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace(); // TODO
		}

		return res;
	}
} 