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


package verifit.compilation.automationPlans;

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
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64.Decoder;

import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc4j.core.model.Link;

import verifit.compilation.VeriFitCompilationConstants;
import verifit.compilation.VeriFitCompilationManager;
import verifit.compilation.VeriFitCompilationResourcesFactory;

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
	 * Run the analysis by passing the stringToExecute to Runtime exec()
	 * @param stringToExecute	String to pass to exec()
	 * @return Output of the analysis (stdout.concat(stderr))
	 * @throws IOException When the execution fails (including anaconda errors)
	 */
    protected String executeTheAnalysis(String stringToExecute) throws IOException
    { 
			// execute the automation plan
			Process process;
			process = Runtime.getRuntime().exec(stringToExecute);
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
			
			//TODO stderr is appended to the end of stdout
			String outputLog = stdoutLog.concat(stderrLog);
			
			// check for errors in the output
			if (outputLog.contains("E:   The Operating System configuration prevents Pin from using the default (parent) injection mode."))
			{
				throw new IOException(outputLog);
			}
			if (stderrLog.matches("^error. analyser .+ not found.\n$"))
			{
				throw new IOException("Internal error. Adapter Analyser allowedValues inconsistent with installed ANaConDA analysers.\n" + stderrLog);
			}
			if (stderrLog.matches("^error. program .+ not found.\n$"))
			{
				throw new IOException("Internal error. Compiled program binary not found.\n" + stderrLog);
			}
			
			return outputLog;
    } 
    
	
	/**
	 * Compiles a given file using gcc and creates the binary.
	 * @param pathToFile Path to the source file to compile
	 * @param parameters Compilation parameters. -o is NOT allowed
	 * @return	Map with 3 values
	 * 				"path" => "path to binary" -- is null on compilation failure
	 * 				"out" => "stdout.concat(stderr) of the compilation"
	 * @throws IOException
	 */
	protected Map<String,String> compileSourceFile(String pathToFile, String parameters) throws IOException
	{
		String binaryPath = pathToFile + ".compiled";
		
		Process process;
		process = Runtime.getRuntime().exec("gcc " + pathToFile + " -o " + binaryPath + " " + parameters);
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
		
		// check gcc return value
		if (process.exitValue() != 0)
		{
			binaryPath = null;
		}
		
		Map<String,String> res = new HashMap<String,String>();
		res.put("path", binaryPath);
		res.put("out", stdoutLog.concat(stderrLog));
		return res;
	}
	
	/**
	 * Download a file from a URL and save it as a byte stream.
	 * Source - https://stackoverflow.com/a/921400
	 * @param url	URL of the file to download
	 * @param pathToFile	Name and path to save the file
	 * @throws IOException 
	 */
	protected void downloadFileFromUrl(String url, String pathToFile) throws IOException
	{
		try {
			URL website = new URL(url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(pathToFile);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (FileNotFoundException e) {
			throw new IOException("File download failed: " + e.getMessage());
		}
	}
	
	/**
	 * Copy a file from the file system as a byte stream. 
	 * @param pathToSource Path to the source file
	 * @param pathToFile	Path where to create the file including its name
	 * @throws IOException
	 */
	protected void createFileFromFilesystem(String pathToSource, String pathToFile) throws IOException
	{
		byte[] buffer = new byte[64]; // TODO random size
		FileInputStream fileInputStream = new FileInputStream(pathToSource);
		FileOutputStream fileOutStream = new FileOutputStream(pathToFile);

		int nBytes = 0;
		while ((nBytes = fileInputStream.read(buffer)) != -1)
		{
			fileOutStream.write(buffer, 0, nBytes);
		}
		fileOutStream.flush();
		fileInputStream.close();
		fileOutStream.close();
	}
	
	/**
	 * Decode a base64 encoded string into bytes and write them into a file as a byte stream.
	 * @param base64EncProgram Base64 encoded file to decode and write
	 * @param pathToFile	Path where to create the file including its name
	 * @throws IOException
	 */
	protected void createFileFromBase64(String base64EncProgram, String pathToFile) throws IOException
	{
		Decoder decoder = Base64.getDecoder();
		byte[] decodedProgram = decoder.decode(base64EncProgram);
		
		FileOutputStream fileOutStream = new FileOutputStream(pathToFile);
		fileOutStream.write(decodedProgram, 0, decodedProgram.length);
		fileOutStream.flush();
		fileOutStream.close();
	}
	
	/**
	 * Creates a folder in the "tmp/" adapter folder
	 * @param subfolder How to name the subfolder
	 * @return	Path to the new folder
	 */
	protected String createTmpDir(String subfolder)
	{
		File programDir = new File("tmp/" + subfolder);
	    if (!programDir.exists())
	    {
	    	programDir.mkdirs();
	    }
	    
	    return "tmp/" + subfolder;
	}
	
	/**
	 * Generate a filename for a program to execute in an AutomationRequest
	 * @param autoRequestId ID of the executed AutomationRequest
	 * @return Name for the new file
	 */
	protected String genFileName(String autoRequestId)
	{
	    String currentDate = new Date().toString().replace(' ', '-');
	    return "req" + autoRequestId + "." + currentDate + ".cpp";
	}
} 