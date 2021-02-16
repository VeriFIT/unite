/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Struct;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Base64.Decoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc4j.core.model.Link;

import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisConstants;
import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisResourcesFactory;

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
	
	public class analyseSUTres {
		public int retCode;
		public String stdout;
		public String stderr;
		public Boolean timeouted;
		public String timeoutType;
	};

	/**
	 * Analyses an SUT
	 * 
	 * @param folderPath      Path to the directory
	 * @param stringToExecute ... TODO
	 * @param timeout         Time limit for analysis execution in seconds. Zero
	 *                        means no time limit
	 * @return a "analyseSUTres" object that holds the stdout, stderr, return code,
	 *         and timeout flag
	 * @throws IOException when the command execution fails (error)
	 */
	protected analyseSUTres analyseSUT(String folderPath, String stringToExecute, int timeout) throws IOException {
		Process process;
		Path pathAsPath = FileSystems.getDefault().getPath(folderPath);
		String shell = (SystemUtils.IS_OS_LINUX ? "/bin/bash" : "cmd");	// TODO assumes that "not linux" means "windows"
		String shellArg = (SystemUtils.IS_OS_LINUX ? "-c" : "/c");
		process = Runtime.getRuntime().exec(new String[] {shell, shellArg, stringToExecute}, null, pathAsPath.toFile());	// launch string as "bash -c" or "cmd /c"	
		
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

		analyseSUTres res = new analyseSUTres();
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
	
	/**
	 * Takes a snapshot of all file names and their modification times
	 * @param folderPath	Directory to take a snapshot of 
	 * @param fileRegex 	Only files matching this regex will be snapshoted
	 * @return	A map of (key: file_path; value: file_modification_time)
	 */
	protected Map<String, Long> takeDirSnapshot(String folderPath, String fileRegex)
	{
		Map<String, Long> files = new HashMap<String, Long>();
		
		// get all files in a directory recursively and loop over them
		Iterator<File> it = FileUtils.iterateFiles(new File(folderPath), null, true);
		while (it.hasNext())
		{
            File currFile = it.next();
        	// TODO do I need read permissions to check the modification date?
        	if (Pattern.matches(fileRegex, currFile.getName()))  
        	{
	        	String path = currFile.getPath();
	        	Long timestamp = currFile.lastModified();
	        	
	        	files.put(path, timestamp);
        	}
		}
		
		return files;
	}

	/**
	 * ZIPs all specified files into a new ZIP file from the perspective of a directory.
	 * All files have to be inside of that directory.
	 * 
	 * @param filesToZip List of files to zip (no directories)
	 * @param dirToZipFrom  This path will be used to make the filesToZip paths relative
	 * @param pathToZip  Path where to place the new ZIP file
	 * @throws IOException
	 */
	protected void zipFiles(List<File> filesToZip, Path dirToZipFrom, Path pathToZip) throws IOException
	{
		FileOutputStream fileOutStream = new FileOutputStream(pathToZip.toString());
		ZipOutputStream zipOutStream = new ZipOutputStream(fileOutStream);

		for (File currFile : filesToZip)
		{
			// relativize the file path to the dir path
			String relativeFilePath = new File(dirToZipFrom.toString()).toURI().relativize(new File(currFile.getPath()).toURI()).getPath();

			byte[] buffer = new byte[1024];
			FileInputStream fileInStream = new FileInputStream(currFile);
			zipOutStream.putNextEntry(new ZipEntry(relativeFilePath));
			int length;
			while ((length = fileInStream.read(buffer)) > 0) {
				zipOutStream.write(buffer, 0, length);
			}
			zipOutStream.closeEntry();
			fileInStream.close();
		}
		
		zipOutStream.flush();
		fileOutStream.flush();
		zipOutStream.close();
		fileOutStream.close();
	}
	
	/**
	 * @author https://stackoverflow.com/a/39903784
	 * @param f
	 * @return
	 * @throws IOException
	 */
	boolean isBinaryFile(File f) throws IOException {
        String type = Files.probeContentType(f.toPath());
        if (type == null) {
            //type couldn't be determined, assume binary
            return true;
        } else if (type.startsWith("text")
        		|| type.contains("xml")	// TODO text xml inside of an xml property?
        		|| type.contains("json")
        		|| type.contains("html")) {
            return false;
        } else {
            //type isn't text
            return true;
        }
    }
} 