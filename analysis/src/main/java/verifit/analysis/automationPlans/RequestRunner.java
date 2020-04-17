/**
 * Author: Ondrej Vasicek
 * VUT-login: xvasic25
 * VUT-email: xvasic25@stud.fit.vutbr.cz
 * 
 * This file is part of my Bachelor's thesis submitted for my IT degree
 * at the Brno University of Technology, Faculty of Information Technology.
 * 
 * This authors header is included only in the files I have personally modified
 * (hopefully did not miss any)
 */


package verifit.analysis.automationPlans;

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
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64.Decoder;

import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc4j.core.model.Link;

import verifit.analysis.VeriFitAnalysisConstants;
import verifit.analysis.VeriFitAnalysisManager;
import verifit.analysis.VeriFitAnalysisResourcesFactory;

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
	 * Analyses an SUT
	 * @param folderPath Path to the directory
	 * @param toolCommand 	Analysis command to run
	 * @param toolParams 	Analysis parameters
	 * @param sutCommand 	SUT launch command to run
	 * @param sutParams 	SUT parameters
	 * @return Triple of (return_code, stdout, stderr)
	 * @throws IOException when the command execution fails (error)
	 */
	protected Triple<Integer,String,String> analyseSUT(File folderPath, String toolCommand, String toolParams, String sutCommand, String sutParams) throws IOException
	{
		Process process;
		process = Runtime.getRuntime().exec(toolCommand + " " + toolParams + " " + sutCommand + " " + sutParams, null, folderPath.getAbsoluteFile());
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
} 