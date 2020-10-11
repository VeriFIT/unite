package verifit.analysis.outputParser;

import java.util.List;
import java.util.Map;

public class NoFileValuesParser extends BasicParser implements IParser {

	@Override
	public List<Map<String, String>> parse(List<Map<String, String>> inputContributions)
	{		
		for (Map<String, String> contrib : inputContributions)
		{
			if (!contrib.get("name").equals("Analysis stdout") && !contrib.get("name").equals("Analysis stderr"))
			{
				contrib.put("value", "");
			}
		}
		
		return inputContributions;
	}
}
