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

import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
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
	 * @return Triple of (return_code, stdout, stderr)
	 * @throws IOException when the command execution fails (error)
	 */
	protected Triple<Integer,String,String> compileSUT(Path folderPath, String buildCommand) throws IOException
	{
		Process process;
		process = Runtime.getRuntime().exec(buildCommand, null, folderPath.toFile());
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
	 * Clone a public git repository (no usrname and passwd) TODO
	 * Source - https://onecompiler.com/posts/3sqk5x3td/how-to-clone-a-git-repository-programmatically-using-java
	 * @param url	URL of the repository
	 * @param folderPath	Path to the folder to clone into
	 * @return Name of the new directory / file
	 * @throws IOException 
	 */
	protected String gitClonePublic(String url, Path folderPath) throws IOException
	{
		try {
		    Git.cloneRepository()
		        .setURI(url)
		        .setDirectory(folderPath.toFile())
		        .call();
		    
		    return VeriFitCompilationManager.getResourceIdFromUri(new URI (url)); // gets the last part of the URL
		
		} catch (GitAPIException | JGitInternalException | URISyntaxException e) {
			throw new IOException("Git clone failed: " + e.getMessage());
		}
	}
	
	/**
	 * Download a file from a URL and save it as a byte stream.
	 * Source - https://stackoverflow.com/a/921400
	 * @param url	URL of the file to download
	 * @param folderPath	Path to the folder where to  save the file
	 * @return Name of the new directory / file
	 * @throws IOException 
	 */
	protected String downloadFileFromUrl(String url, Path folderPath) throws IOException
	{
		try {
			String fileName = VeriFitCompilationManager.getResourceIdFromUri(new URI (url)); // gets the last part of the URL
			Path pathToFile = folderPath.resolve(fileName);
			
			URL website = new URL(url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(pathToFile.toFile());
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			
			return fileName;
		} catch (FileNotFoundException | URISyntaxException e) {
			throw new IOException("Download from url failed: " + e.getMessage());
		}
	}
	
	/**
	 * Copy a file from the file system as a byte stream. TODO add dir copy aswell
	 * @param pathToSource Path to the source file
	 * @param folderPath	Path to the folder where to  save the file
	 * @return Name of the new directory / file
	 * @throws IOException
	 */
	protected void createFileFromFilesystem(String pathToSource, Path folderPath) throws IOException
	{
		byte[] buffer = new byte[64]; // TODO random size
		FileInputStream fileInputStream = new FileInputStream(pathToSource);

		String fileName = Path.of(pathToSource).getFileName().toString();
		Path pathToFile = Path.of(pathToSource).resolve(fileName);
		FileOutputStream fileOutStream = new FileOutputStream(pathToFile.toFile());

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
	 * @param base64EncProgram Base64 encoded file to decode and write. First line is the name of the file, second line is the base64 string.
	 * @param folderPath	Path to the folder where to  save the file
	 * @throws IOException
	 * @throws IllegalArgumentException	When the base64EncProgram is invalid
	 */
	protected String createFileFromBase64(String base64EncProgram, Path folderPath) throws IOException, IllegalArgumentException
	{
		int idxSplit = base64EncProgram.indexOf('\n');
		if (idxSplit == -1)
			throw new IllegalArgumentException("Invalid format of sourceBase64 value. No \"\\n\" delimiter found. Expected format: filename\\nbase64");
		
		String filename = base64EncProgram.substring(0, base64EncProgram.indexOf('\n'));
		String base64 = base64EncProgram.substring(base64EncProgram.indexOf('\n') + 1);
		
		Decoder decoder = Base64.getDecoder();
		byte[] decodedProgram = decoder.decode(base64);
		
		Path pathToFile = folderPath.resolve(filename);
		FileOutputStream fileOutStream = new FileOutputStream(pathToFile.toFile());
		fileOutStream.write(decodedProgram, 0, decodedProgram.length);
		fileOutStream.flush();
		fileOutStream.close();
		
		return filename;
	}
	
	/**
	 * Creates a folder in the "tmp/" adapter folder
	 * @param subfolder How to name the subfolder
	 * @return	Path to the new folder
	 * @throws IOException 
	 */
	protected Path createTmpDir(String subfolder) throws IOException
	{
		Path subfolderPath = Path.of("tmp").resolve(subfolder);
	    
	    if (!Files.exists(subfolderPath))
	    {   
            Files.createDirectory(subfolderPath);
	    }
	    return subfolderPath.toAbsolutePath();
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
        for (Iterator<? extends ZipEntry> it = zipEntries.asIterator(); it.hasNext(); )
        {
        	ZipEntry entry = it.next();
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