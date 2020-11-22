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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc4j.core.model.Link;

import verifit.analysis.VeriFitAnalysisConstants;
import verifit.analysis.VeriFitAnalysisManager;
import verifit.analysis.VeriFitAnalysisResourcesFactory;
import verifit.analysis.utils;
import verifit.analysis.outputParser.ParserManager;
import verifit.analysis.resources.SUT;
import org.eclipse.lyo.oslc.domains.auto.Contribution;

import static verifit.analysis.utils.getResourceIdFromUri;

/**
 * A thread for executing analysis of an SUT.
 * @author od42
 *
 */
public class SutAnalyse extends RequestRunner
{
	final private String serviceProviderId;
	final private String execAutoRequestId;
	private AutomationRequest execAutoRequest;
	final private String resAutoResultId;
	private AutomationResult resAutoResult;
	final private String execSutId;
	private SUT execSut;
	final private Map<String, Pair<String,Integer>> inputParamsMap;

	final private AutomationPlanConfManager.AutomationPlanConf autoPlanConf;
	
	/**
	 * @param serviceProviderId	ID of the service provider
	 * @param execAutoRequest	Executed AutomationRequest resource object
	 * @param resAutoResult		Result AutomationResult resource object
	 * @param execSut			Executed SUT resource object
	 * @param inputParamsMap	Input parameters as a map of "name" => "(value, position)"
	 */
	public SutAnalyse(String serviceProviderId, AutomationRequest execAutoRequest, AutomationResult resAutoResult, SUT execSut, Map<String, Pair<String,Integer>> inputParamsMap) 
	{
		super();
		
		this.serviceProviderId = serviceProviderId;
		this.inputParamsMap = inputParamsMap;
		this.execAutoRequestId = getResourceIdFromUri(execAutoRequest.getAbout());
		this.execAutoRequest = execAutoRequest;
		this.resAutoResultId = getResourceIdFromUri(resAutoResult.getAbout());
		this.resAutoResult = resAutoResult;
		this.execSutId = getResourceIdFromUri(execSut.getAbout());;
		this.execSut = execSut;

		// load the AutomationPlanConfiguration
		AutomationPlanConfManager autoPlanConfManager = AutomationPlanConfManager.getInstance();
		this.autoPlanConf = autoPlanConfManager.getAutoPlanConf(
				getResourceIdFromUri(execAutoRequest.getExecutesAutomationPlan().getValue())
			);
	}

	/**
	 * Thread main
	 */
	public void run()
	{
		
		try {
			// get non-commandline input parameters (dont have commandline position - getRight() == -1)
			final String outputRegex = inputParamsMap.get("outputFileRegex").getLeft();
			final String zipOutputs = inputParamsMap.get("zipOutputs").getLeft();
			final String timeout = inputParamsMap.get("timeout").getLeft();

			// Build the string to execute from commandline input parameters based on their positions TODO maybe move somewhere else
			String buildStringToExecute = ""
				+ this.autoPlanConf.getLaunchCommand() + " "
				+ this.autoPlanConf.getToolSpecificArgs() + " ";
			List<Pair<String,Integer>> inputParamsList = new ArrayList<Pair<String,Integer>>(inputParamsMap.values());
			inputParamsList.sort((Pair<String,Integer> a, Pair<String,Integer> b) -> a.getRight().compareTo(b.getRight()));
			for (Pair<String, Integer> param : inputParamsList)
			{
				if (param.getRight() != -1) // skip the non commandline input params
					buildStringToExecute += param.getLeft() + " ";

			}
			final String stringToExecute = buildStringToExecute;


			// set the states of the Automation Result and Request to "inProgress" - if they are not that already
			if (!(execAutoRequest.getState().iterator().next().getValue()
					.toASCIIString().equals(VeriFitAnalysisConstants.AUTOMATION_STATE_INPROGRESS)))
			{
				resAutoResult.setState(new HashSet<Link>());
				resAutoResult.addState(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_STATE_INPROGRESS)));
				VeriFitAnalysisManager.updateAutomationResult(resAutoResult, serviceProviderId, resAutoResultId);
				execAutoRequest.setState(new HashSet<Link>());
				execAutoRequest.addState(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_STATE_INPROGRESS)));
				VeriFitAnalysisManager.updateAutomationRequest(execAutoRequest, serviceProviderId, execAutoRequestId);
			}

		    // prepare stdin & stdout contributions
			Contribution analysisStdoutLog = new Contribution();
		    analysisStdoutLog.setDescription("Standard output of the analysis. Provider messages are prefixed with #.");
		    analysisStdoutLog.setTitle("Analysis stdout");
		    analysisStdoutLog.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));
		    Contribution analysisStderrLog = new Contribution();
		    analysisStderrLog.setDescription("Error output of the analysis. Provider messages are prefixed with #.");
		    analysisStderrLog.setTitle("Analysis stderr");
		    analysisStderrLog.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));


		    // take a snapshot of SUT files modification times before executing the analysis
		    Map<String, Long> snapshotBeforeAnalysis = takeDirSnapshot(execSut.getSUTdirectoryPath(), outputRegex);

			// analyse SUT
		    String executionVerdict = VeriFitAnalysisConstants.AUTOMATION_VERDICT_PASSED;
			try {
				analysisStdoutLog.setValue("# Executing: " + stringToExecute + "\n");
		    	analyseSUTres analysisRes = analyseSUT(execSut.getSUTdirectoryPath(), stringToExecute, Integer.parseInt(timeout));

				if (analysisRes.timeouted)
				{
					executionVerdict = VeriFitAnalysisConstants.AUTOMATION_VERDICT_FAILED;
			    	analysisStdoutLog.setValue(analysisStdoutLog.getValue() + "# Analysis aborted due to a " + analysisRes.timeoutType + " timeout (" + timeout + " seconds)\n" //TODO
			    							+ analysisRes.stdout);
				}
		    	else if (analysisRes.retCode != 0)
		    	{
					executionVerdict = VeriFitAnalysisConstants.AUTOMATION_VERDICT_ERROR;
			    	analysisStdoutLog.setValue(analysisStdoutLog.getValue() + "# Analysis failed (returned non-zero: " + analysisRes.retCode + ")\n"
			    							+ analysisRes.stdout);
				}
		    	else
		    	{
		    		analysisStdoutLog.setValue(analysisStdoutLog.getValue() + "# Analysis completed successfully\n" + analysisRes.stdout);
		    	}
		    	analysisStderrLog.setValue(analysisRes.stderr);

			} catch (IOException e) {
				// there was an error
				executionVerdict = VeriFitAnalysisConstants.AUTOMATION_VERDICT_ERROR;
				analysisStdoutLog.setValue(analysisStdoutLog.getValue() + "# Analysis error");
				analysisStderrLog.setValue(e.getMessage());	// get stderr

			}

			// add the compilation Contributions to the Automation Result
	    	resAutoResult.addContribution(analysisStdoutLog);
	    	resAutoResult.addContribution(analysisStderrLog);

		    // take a snapshot of SUT files modification times after executing the analysis
	    	// and add all the new ones / modified ones as contributions
			Map<String, Long> snapshotAfterAnalysis = takeDirSnapshot(execSut.getSUTdirectoryPath(), outputRegex);
			List<File> modifFiles = new ArrayList<File>(); // for optional zip later
		    for (Map.Entry<String,Long> newFile : snapshotAfterAnalysis.entrySet())
		    {
		    	if (snapshotBeforeAnalysis.containsKey(newFile.getKey()))
		    	{ // if the file existed before the analysis, check if his modif time changed
		    		Long oldTimestamp = snapshotBeforeAnalysis.get(newFile.getKey());
		    		if (newFile.getValue() <= oldTimestamp)
		    		{ // the file was not modified
		    			continue;
		    		}
		    	}

		    	// the file did not exist before analysis OR was modified --> add it as contribution to the AutoResult
				File currFile = new File(newFile.getKey());
				modifFiles.add(currFile);
			    Contribution newOrModifFile = new Contribution();
	    		String fileId = currFile.getPath().replaceAll("/", "%2F"); // encode slashes in the file path
			    newOrModifFile.setFileURI(VeriFitAnalysisResourcesFactory.constructURIForContribution(serviceProviderId, fileId));
			    newOrModifFile.setDescription("This file was modified or created during execution of this Automation Request. "
			    		+ "To download the file directly send a GET accepting application/octet-stream to the URI in the fit:fileURI property. "
			    		+ "To modify the file send a regular OSLC update PUT request for a Contribution resource to the URI in fit:fileURI."); // TODO
			    newOrModifFile.setTitle(currFile.getName());
			    try {
			    	byte [] fileContents = Files.readAllBytes(FileSystems.getDefault().getPath(currFile.toString())); // TODO RAM usage
			    	if (isBinaryFile(currFile))
			    	{
			    		newOrModifFile.setValue(utils.base64Encode(fileContents));
			    		newOrModifFile.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_BASE64BINARY)));
			    	}
		    		else
		    		{
		    			newOrModifFile.setValue(new String(fileContents));
		    			newOrModifFile.addValueType(new Link(new URI(VeriFitAnalysisConstants.OSLC_VAL_TYPE_STRING)));
		    		}
				} catch (IOException e) {
					newOrModifFile.setValue(e.getMessage());
				}
		    	resAutoResult.addContribution(newOrModifFile);
			}

			// create a zip of all file contributions if needed
			if (zipOutputs.equals("true") || zipOutputs.equals("True"))
			{
				Contribution zipedContribs = new Contribution();
				String zipName = "out" + execAutoRequestId + ".zip";
				Path pathToDir = FileSystems.getDefault().getPath(execSut.getSUTdirectoryPath());
				Path pathToZip = pathToDir.resolve(zipName);;

				try {
					zipFiles(modifFiles, pathToDir, pathToZip);

					String fileId =  pathToZip.toString().replaceAll("/", "%2F"); // encode slashes in the file path
					zipedContribs.setFileURI(VeriFitAnalysisResourcesFactory.constructURIForContribution(serviceProviderId, fileId));
					zipedContribs.setDescription("This is a ZIP of all other file contributions. "
							+ "To download the file directly send a GET accepting application/octet-stream to the URI in the fit:fileURI property."); // TODO
					zipedContribs.setTitle(zipName);
					resAutoResult.addContribution(zipedContribs);
				} catch (Exception e)
				{
					System.out.println("WARNING failed to ZIP outputs: " + e.getMessage());
				}
			}

			// run the AutoResult contributions through a parser
			ParserManager parserManagerInst = ParserManager.getInstance();
			Set<Contribution> parsedContributions = parserManagerInst.parseContributionsForTool(
					getResourceIdFromUri(execAutoRequest.getExecutesAutomationPlan().getValue()),
					resAutoResult.getContribution());
			resAutoResult.setContribution(parsedContributions);

			// update the AutoResult state and verdict, and AutoRequest state
			resAutoResult.setState(new HashSet<Link>());
			resAutoResult.addState(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_STATE_COMPLETE)));
			resAutoResult.setVerdict(new HashSet<Link>());
			resAutoResult.addVerdict(new Link(new URI(executionVerdict)));
			VeriFitAnalysisManager.updateAutomationResult(resAutoResult, serviceProviderId, getResourceIdFromUri(resAutoResult.getAbout()));
			execAutoRequest.setState(new HashSet<Link>());
			execAutoRequest.addState(new Link(new URI(VeriFitAnalysisConstants.AUTOMATION_STATE_COMPLETE)));
			VeriFitAnalysisManager.updateAutomationRequest(execAutoRequest, serviceProviderId, execAutoRequestId);

			// end the request execution (in case it is part of a request queue)
			VeriFitAnalysisManager.finishedAutomationRequestExecution(execAutoRequest);

		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
		}
	}
}