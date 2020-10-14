package verifit.analysis.outputParser;

import java.util.List;
import java.util.Map;

import verifit.analysis.VeriFitAnalysisConstants;

public class NoBinaryFileValuesParser extends BasicParser implements IParser {

	@Override
	public List<Map<String, String>> parse(List<Map<String, String>> inputContributions)
	{
		for (Map<String, String> contrib : inputContributions)
		{
			if (contrib.get("valueType") == VeriFitAnalysisConstants.OSLC_VAL_TYPE_BASE64BINARY)
			{
				contrib.remove("value");
				contrib.remove("valueType");
			}
		}
		
		return inputContributions;
	}

}
