package cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;

import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;
import cz.vutbr.fit.group.verifit.oslc.shared.queuing.RequestRunnerQueues;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;

public class ExecutionManager implements IExecutionManager {

	/**
	 * Holds all execution runners that have been registered with the manager
	 */
	Map<String, RequestRunner> mapRequestRunners;
	
	/**
	 * Queuing object for runners
	 */
	RequestRunnerQueues AutoRequestQueues;
	
	public ExecutionManager() {
		this.mapRequestRunners = new HashMap<String, RequestRunner>();
		this.AutoRequestQueues = new RequestRunnerQueues();
	}
	

	public synchronized void addRequest(RequestRunner request)
	{
		// set self as manager of the request for callback after execution finishes
		request.setExecManager(this);
		
		// add into the local map
		mapRequestRunners.put(request.getExecutedAutomationRequestId(), request);
		
		// decide to execute or queue
		if (request.getStartingState().equals(OslcValues.AUTOMATION_STATE_INPROGRESS))
		{
			request.start();
		}
		else if (request.getStartingState().equals(OslcValues.AUTOMATION_STATE_QUEUED))
		{
			AutoRequestQueues.queueUp(
					request.getExecutedAutomationPlanId(),
					request);
		}
		else
		{
			// TODO
		}
	}

	public synchronized void cancelRequest(String requestId) throws Exception
	{
		RequestRunner runner = mapRequestRunners.get(requestId);
		if (runner == null)
			throw new Exception("unknown request: " + requestId);
		
		// cancel the execution
		runner.interrupt();
	}
	
	public synchronized void executionFinishedCallback(RequestRunner request)
	{
		String autoPlanId = request.getExecutedAutomationPlanId();
		
    	// if there is a request queue for this requests Automation Plan, then it means this request execution is currently
    	// at the front of the queue and needs to be removed
    	if (AutoRequestQueues.queueExists(autoPlanId))
    	{
    		AutoRequestQueues.popFirst(autoPlanId);
    	}
    	
    	// remove the request from the local map
		mapRequestRunners.remove(request.getExecutedAutomationRequestId());
	}
}
