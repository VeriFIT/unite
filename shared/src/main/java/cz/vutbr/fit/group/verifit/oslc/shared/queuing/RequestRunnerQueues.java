package cz.vutbr.fit.group.verifit.oslc.shared.queuing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution.RequestRunner;

/**
 * A map of queues of Automation Request runners. Each queue is meant to be used for a different Automation Plan (key to the map).
 * The first runner in each queue will be started. 
 */
public class RequestRunnerQueues {
	
	private Map<String,List<RequestRunner>> queueMap;
	
	public RequestRunnerQueues()
	{
		queueMap = new HashMap<String,List<RequestRunner>>();
	}

	public synchronized Boolean queueExists(String queueName) 
	{
		return (queueMap.get(queueName) != null);
	}
	
	public synchronized Boolean empty(String queueName) 
	{
		List<RequestRunner> queue = queueMap.get(queueName);
		if(queue == null)
			return true;
		else
			return queue.isEmpty();
	}
	
	/**
	 * Adds a runner into a named queue. The runner will be started when it reaches the front of the queue (immediately if the queue is empty).
	 * @param queueName
	 * @param runner
	 */
	public synchronized void queueUp(String queueName, RequestRunner runner)
	{	
		// check if the queue exists - create it if it doesnt
		if (queueMap.get(queueName) == null)
		{
			queueMap.put(queueName, new LinkedList<RequestRunner>());
		}
		
		// queue up
		queueMap.get(queueName).add(runner);
		
		// start execution if first in line
		if (queueMap.get(queueName).size() == 1)
			runner.start();
	}
	
	/**
	 * Removes the first runner from a named queue. Meant to be called after the runner has finished execution. Will start the next runner in line.
	 * @param queueName
	 */
	public synchronized void popFirst(String queueName)
	{
		List<RequestRunner> queue = queueMap.get(queueName);
		if (!(queue == null)) // make sure the queue exists
		{
			queue.remove(0);
			if (!(queue.isEmpty()))	// start the next runner if there is one
				queue.get(0).start();
		}
	}
};