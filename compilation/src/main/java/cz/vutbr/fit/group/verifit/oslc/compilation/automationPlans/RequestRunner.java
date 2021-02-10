/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.compilation.automationPlans;

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
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.Base64.Decoder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc4j.core.model.Link;

import cz.vutbr.fit.group.verifit.oslc.compilation.VeriFitCompilationConstants;
import cz.vutbr.fit.group.verifit.oslc.compilation.VeriFitCompilationManager;
import cz.vutbr.fit.group.verifit.oslc.compilation.VeriFitCompilationResourcesFactory;

/**
 * A thread designed to execute an AutomationRequest.
 * @author Ondřej Vašíček
 *
 */
public abstract class RequestRunner extends Thread
{
	public RequestRunner()
	{
		super();
	}
	
	/**
	 * Runs a specified compilation command inside of a SUT directory
	 * @param folderPath Path to the directory
	 * @param buildCommand Command to run
	 * @return Triple of (return_code, stdout, stderr)
	 * @throws IOException when the command execution fails (error)
	 */
	protected Triple<Integer,String,String> compileSUT(Path folderPath, String buildCommand) throws IOException
	{
		Process process;
		String shell = (SystemUtils.IS_OS_LINUX ? "/bin/bash" : "cmd");	// TODO assumes that "not linux" means "windows"
		String shellArg = (SystemUtils.IS_OS_LINUX ? "-c" : "/c");
		process = Runtime.getRuntime().exec(new String[] {shell, shellArg, buildCommand}, null, folderPath.toFile());	// launch string as "bash -c" or "cmd /c"	
		
		InputStream stdout = process.getInputStream();
		InputStream stderr = process.getErrorStream();
		InputStreamReader stdoutReader = new InputStreamReader(stdout);
		BufferedReader stdoutBuffReader = new BufferedReader(stdoutReader);
		InputStreamReader stderrReader = new InputStreamReader(stderr);
		BufferedReader stderrBuffReader = new BufferedReader(stderrReader);
		String line, stdoutLog = "", stderrLog = "";
		
		while ((line = stdoutBuffReader.readLine()) != null) {
			stdoutLog = stdoutLog.concat(line + "\n");
		}
		while ((line = stderrBuffReader.readLine()) != null) {
			stderrLog = stderrLog.concat(line + "\n");
		}
		
		// wait for the process to exit
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// TODO Dont know what that means really
			e.printStackTrace();
		}

		return Triple.of(process.exitValue(), stdoutLog, stderrLog);
	}
	
	/**
	 * Creates a folder in the "tmp/" adapter folder
	 * @param subfolder How to name the subfolder
	 * @return	Path to the new folder
	 * @throws IOException 
	 */
	protected Path createTmpDir(String subfolder) throws IOException
	{
		Path subfolderPath = FileSystems.getDefault().getPath("tmp").resolve(subfolder);
	    
	    if (!Files.exists(subfolderPath))
	    {   
            Files.createDirectory(subfolderPath); sdfgsdfgsdf
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