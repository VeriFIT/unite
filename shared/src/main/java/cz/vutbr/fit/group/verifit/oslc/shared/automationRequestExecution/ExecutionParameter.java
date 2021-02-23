package cz.vutbr.fit.group.verifit.oslc.shared.automationRequestExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc.domains.auto.ParameterInstance;

public class ExecutionParameter {
	private String name;
	private String value;
	private Integer cmdPosition;
	
	public ExecutionParameter(String name, String value, Integer cmdPosition) {
		this.name = name;
		this.value = value;
		this.cmdPosition = cmdPosition;
	}
	
	private ExecutionParameter(ParameterInstance paramInst, ParameterDefinition paramDef)
	{
		this.name = paramInst.getName();
		this.value = paramInst.getValue();
		this.cmdPosition = paramDef.getCommandlinePosition();
	}
	
	public Boolean isCmdLineParameter()
	{
		return (cmdPosition != null);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getCmdPosition() {
		return cmdPosition;
	}

	public void setCmdPosition(Integer cmdPosition) {
		this.cmdPosition = cmdPosition;
	}
	
	public static List<ExecutionParameter> createExecutionParameters(final Set<ParameterInstance> inputParameters, final Set<ParameterInstance> outputParameters, final Set<ParameterDefinition> parameterDefinitions)
	{
		List<ExecutionParameter> executionParameters = new ArrayList<ExecutionParameter>();
		
		for (ParameterInstance outParam : outputParameters)
		{
			ParameterDefinition paramDef = findParamDefForAParamInstance(outParam, parameterDefinitions);
			executionParameters.add(new ExecutionParameter(outParam, paramDef));
		}
		for (ParameterInstance inParam : inputParameters)
		{
			ParameterDefinition paramDef = findParamDefForAParamInstance(inParam, parameterDefinitions);
			executionParameters.add(new ExecutionParameter(inParam, paramDef));
		}
		
		return executionParameters;
	}
	
	private static ParameterDefinition findParamDefForAParamInstance(final ParameterInstance paramInstance, final Set<ParameterDefinition> parameterDefinitions)
	{
		for (ParameterDefinition paramDef : parameterDefinitions)
		{
			if (paramDef.getName().equals(paramInstance.getName()))
				return paramDef;
		}
		return null;
	}
}
