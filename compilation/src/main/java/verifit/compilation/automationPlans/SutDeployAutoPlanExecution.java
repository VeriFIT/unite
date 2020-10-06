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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Base64.Decoder;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc4j.core.model.Link;

import verifit.compilation.VeriFitCompilationConstants;
import verifit.compilation.VeriFitCompilationManager;
import verifit.compilation.VeriFitCompilationResourcesFactory;
import verifit.compilation.automationPlans.sutFetcher.SutFetchBase64;
import verifit.compilation.automationPlans.sutFetcher.SutFetchFileSystem;
import verifit.compilation.automationPlans.sutFetcher.SutFetchGit;
import verifit.compilation.automationPlans.sutFetcher.SutFetchUrl;
import verifit.compilation.automationPlans.sutFetcher.SutFetcher;
import verifit.compilation.resources.SUT;
import org.eclipse.lyo.oslc.domains.auto.Contribution;
/**
 * A thread for executing the Deploy SUT Automation Plan.
 * @author od42
 *
 */
public class SutDeployAutoPlanExecution extends RequestRunner
{
	final private String serviceProviderId;
	final private String execAutoRequestId;
	private AutomationRequest execAutoRequest;
	final private String resAutoResultId;
	private AutomationResult resAutoResult;
	final private Map<String, String> inputParamsMap;
	
	/**
	 * Creating the thread automatically starts the execution
	 * @param serviceProviderId	ID of the service provider
	 * @param execAutoRequest	Executed AutomationRequest resource object
	 * @param execAutoResult	Result AutomationResult resource object
	 * @param inputParamsMap	Input parameters as a "name" => "value" map
	 */
	public SutDeployAutoPlanExecution(String serviceProviderId, AutomationRequest execAutoRequest, AutomationResult resAutoResult, Map<String, String> inputParamsMap) 
	{
		super();
		
		this.serviceProviderId = serviceProviderId;
		this.inputParamsMap = inputParamsMap;
		this.execAutoRequestId = VeriFitCompilationManager.getResourceIdFromUri(execAutoRequest.getAbout());
		this.execAutoRequest = execAutoRequest;
		this.resAutoResultId = VeriFitCompilationManager.getResourceIdFromUri(resAutoResult.getAbout());
		this.resAutoResult = resAutoResult;
		
		this.start();
	}

	/**
	 * Thread main
	 */
	public void run()
	{
		
		try {
			
			// get the input parameters
			final String paramSourceGit = inputParamsMap.get("sourceGit");
			final String paramSourceUrl = inputParamsMap.get("sourceUrl");
			final String paramSourceBase64 = inputParamsMap.get("sourceBase64");
			final String paramSourceFilePath = inputParamsMap.get("sourceFilePath");
			final String paramBuildCommand = inputParamsMap.get("buildCommand");
			final String paramLaunchCommand = inputParamsMap.get("launchCommand");
			final String paramUnpackZip = inputParamsMap.get("unpackZip");

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
			resAutoResult.setState(new HashSet<Link>());
			resAutoResult.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_INPROGRESS)));
			VeriFitCompilationManager.updateAutomationResult(resAutoResult, serviceProviderId, resAutoResultId);
			execAutoRequest.setState(new HashSet<Link>());
			execAutoRequest.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_INPROGRESS)));
			VeriFitCompilationManager.updateAutomationRequest(execAutoRequest, serviceProviderId, execAutoRequestId);
			
			
		    // prepare result contributions - program fetching, compilation
			Contribution fetchLog = new Contribution();
		    fetchLog.setDescription("Output of the program fetching process. Provider messages are prefixed with #.");
		    fetchLog.setTitle("Fetching Output");
		    //fetchLog.addType(new Link(new URI("http://purl.org/dc/dcmitype/Text"))); //TODO
		    
		    Contribution compStdoutLog = new Contribution();
		    compStdoutLog.setDescription("Standard output of the compilation. Provider messages are prefixed with #.");
		    compStdoutLog.setTitle("Compilation stdout");
		    Contribution compStderrLog = new Contribution();
		    compStderrLog.setDescription("Error output of the compilation. Provider messages are prefixed with #.");
		    compStderrLog.setTitle("Compilation stderr");
			
		    
		    Boolean performCompilation = true;	// flag to disable a part of the execution in case of an error
			String executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_PASSED;
		    
			// fetch source file
			Path folderPath = null;
			try {
				// create the program path and name
				folderPath = createTmpDir(execAutoRequestId);
				String filenameSUT = "";	// used for optional unpacking later
				
			    // get the source file
		    	filenameSUT = sutFetcher.fetchSut(ProgramSource, folderPath);
			    
			    // unzip the SUT if requested
			    if (paramUnpackZip.equals("true") || paramUnpackZip.equals("True"))
			    {
			    	unzipFile(folderPath, filenameSUT);
			    }
			    
				fetchLog.setValue("# SUT fetch successful\n");
			
			} catch (Exception e) {
				executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_ERROR;
				fetchLog.setValue("# SUT fetch failed\n" + e.getMessage());
	    		performCompilation = false;

			} finally {
				// create the fetching log Contribution and add it to the AutomationResult
				fetchLog = VeriFitCompilationManager.createContribution(fetchLog, serviceProviderId);
		    	resAutoResult.addContribution(fetchLog);
			}
			
			    
			// Do not compile if there was no buildCommand (e.g. for static analysis)
			if (paramBuildCommand == null || paramBuildCommand.equals("")) //TODO
				performCompilation = false;

			// compile source file if the fetching did not fail
			if (performCompilation)
			{
				try {
			    	Triple<Integer, String, String> compRes = compileSUT(folderPath, paramBuildCommand);
			    	
			    	if (compRes.getLeft() != 0) // get return code
			    	{	// if the compilation returned non zero, set the verdict as failed
						executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_FAILED;
				    	compStdoutLog.setValue("# Compilation failed (returned non-zero: " + compRes.getLeft() + ")\n"
				    							+ compRes.getMiddle());	// get stdout
			    	}
			    	else
			    	{
				    	compStdoutLog.setValue("# Compilation completed successfully\n" + compRes.getMiddle());	// get stdout
			    	}
			    	compStderrLog.setValue(compRes.getRight());		// get stderr
				    
				} catch (IOException e) {
					// there was an error
					executionVerdict = VeriFitCompilationConstants.AUTOMATION_VERDICT_ERROR;
					compStdoutLog.setValue("# Compilation error");	// get stdout
			    	compStderrLog.setValue(e.getMessage());	// get stderr
		    		
				} finally {
					// create the compilation Contributions and add them to the Automation Result
					compStdoutLog = VeriFitCompilationManager.createContribution(compStdoutLog, serviceProviderId);
					compStderrLog = VeriFitCompilationManager.createContribution(compStderrLog, serviceProviderId);
			    	resAutoResult.addContribution(compStdoutLog);
			    	resAutoResult.addContribution(compStderrLog);
				}
			}
	    	
			// create the SUT resource if the compilation was successful
			if (executionVerdict == VeriFitCompilationConstants.AUTOMATION_VERDICT_PASSED)
			{
				SUT newSut = new SUT();
				newSut.setTitle("SUT - " + execAutoRequest.getTitle());
				newSut.setLaunchCommand(paramLaunchCommand);
				if (!(paramBuildCommand == null || paramBuildCommand.equals(""))) //TODO
					newSut.setBuildCommand(paramBuildCommand);
				newSut.setSUTdirectoryPath(folderPath.toAbsolutePath().toString());
				newSut.setCreator(execAutoRequest.getCreator());
				newSut.setProducedByAutomationRequest(VeriFitCompilationResourcesFactory.constructLinkForAutomationRequest(serviceProviderId, execAutoRequestId));
				VeriFitCompilationManager.createSUT(newSut, serviceProviderId, execAutoRequestId); // TODO
				resAutoResult.setCreatedSUT(VeriFitCompilationResourcesFactory.constructLinkForSUT(serviceProviderId, VeriFitCompilationManager.getResourceIdFromUri(newSut.getAbout()))); // TODO
			}
			
			// update the AutoResult state and verdict, and AutoRequest state
			resAutoResult.setState(new HashSet<Link>());
			resAutoResult.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_COMPLETE)));
			resAutoResult.setVerdict(new HashSet<Link>());
			resAutoResult.addVerdict(new Link(new URI(executionVerdict)));
			VeriFitCompilationManager.updateAutomationResult(resAutoResult, serviceProviderId, VeriFitCompilationManager.getResourceIdFromUri(resAutoResult.getAbout()));
			execAutoRequest.setState(new HashSet<Link>());
			execAutoRequest.addState(new Link(new URI(VeriFitCompilationConstants.AUTOMATION_STATE_COMPLETE)));
			VeriFitCompilationManager.updateAutomationRequest(execAutoRequest, serviceProviderId, execAutoRequestId);
				
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
	}
}