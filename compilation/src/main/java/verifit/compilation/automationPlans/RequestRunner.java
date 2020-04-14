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
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64.Decoder;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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
	 * Runs a specified compilation command inside of a SUT directory
	 * @param folderPath Path to the directory
	 * @param buildCommand Command to run
	 * @return stdout.concat(stderr) of the compilation TODO
	 * @throws IOException
	 */
	protected String compileSourceFile(File folderPath, String buildCommand) throws IOException
	{
		Process process;
		process = Runtime.getRuntime().exec(buildCommand, null, folderPath.getAbsoluteFile());
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
		
		String output = stdoutLog.concat(stderrLog); //TODO
		
		// check gcc return value
		if (process.exitValue() != 0)
		{
			throw new IOException(output);
		}
		
		return stdoutLog.concat(output);
	}

	/**
	 * Clone a public git repository (no usrname and passwd) TODO
	 * Source - https://onecompiler.com/posts/3sqk5x3td/how-to-clone-a-git-repository-programmatically-using-java
	 * @param url	URL of the repository
	 * @param folderPath	Path to the folder to clone into
	 * @throws IOException 
	 */
	protected void gitClonePublic(String url, File folderPath) throws IOException
	{
		try {
		    Git.cloneRepository()
		        .setURI(url)
		        .setDirectory(folderPath)
		        .call();
		} catch (GitAPIException e) {
			throw new IOException("Git clone failed: " + e.getMessage());
		}
	}
	
	/**
	 * Download a file from a URL and save it as a byte stream.
	 * Source - https://stackoverflow.com/a/921400
	 * @param url	URL of the file to download
	 * @param folderPath	Path to the folder where to  save the file
	 * @throws IOException 
	 */
	protected void downloadFileFromUrl(String url, File folderPath) throws IOException
	{
		try {
			String fileName = VeriFitCompilationManager.getResourceIdFromUri(new URI (url)); // gets the last part of the URL
			String pathToFile = folderPath.getAbsolutePath().toString() + "/" + fileName;
			
			URL website = new URL(url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(pathToFile);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (FileNotFoundException | URISyntaxException e) {
			throw new IOException("Download from url failed: " + e.getMessage());
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
	protected File createTmpDir(String subfolder)
	{
		File programDir = new File("tmp/" + subfolder);
	    if (!programDir.exists())
	    {
	    	programDir.mkdirs();
	    }
	    
	    return new File("tmp/" + subfolder).getAbsoluteFile();
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