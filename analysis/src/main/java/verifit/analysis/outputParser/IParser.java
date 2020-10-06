package verifit.analysis.outputParser;

import java.util.List;
import java.util.Map;

public interface IParser {

	/**
	 * Processes output contributions of an analysis execution. Parsers are plugins created for specific tools and use cases.
	 * All created contributions are passed to the parser and only the ones returned by the parser will be included in the
	 * final Automation Result (returned to the client and saved in the database). The simplest parser just returns its input
	 * without any modifications. A proper/advanced parser can remove or modify contributions, or create new contributions
	 * based on information obtained from the original ones. 
	 * 
	 * @param inputContributions	Each element of the list represents one output contribution (e.g. stdout or a log file).
	 * 								Expected map entries are "name", "value", "description", "fileURI" and "type".
	 * @return The same list as the one that was passed as input. Can be modified by adding new list elements or removing/modifying existing ones.
	 */
	public List<Map<String,String>> parse(List<Map<String,String>> inputContributions);
	
}