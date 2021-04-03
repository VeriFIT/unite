package cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution;

/**
 * Manages AutomationRequest execution. All requests to be run are added to the manager and the manager executes them.
 * Requests can be queued based on Automation Plans. Requests can be deleted or updated to cancel execution.
 * 
 * @author xvasic25
 *
 */
public interface IExecutionManager {

	/**
	 * Add a RequestRunner to the Manager. The manager will then execute the request or queue it up.
	 * @param request A RequestRunner object ready to be executed
	 */
	public void addRequest(RequestRunner request);
	
	/**
	 * Cancel a request's execution by changing its desired state to "canceled"
	 * @param requestId	Identifier of the request to cancel
	 * @throws Exception	If the request does not exist
	 */
	public void cancelRequest(String requestId) throws Exception;
	

	/**
	 * Function called by RequestRunners to let the manager know when they finish execution.
	 * @param request	"this" of the sending request
	 */
	public void executionFinishedCallback(RequestRunner request);
	
}
