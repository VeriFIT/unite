package verifit.analysis.outputParser;

import java.util.List;
import java.util.Map;

public class BasicParser implements IParser {

	@Override
	public List<Map<String, String>> parse(List<Map<String, String>> inputContributions) {
		return inputContributions;
	}
	
	/**
	 * TODO
	 * @param unencodedURI
	 * @return use this value as the fileURL element of the contribution map 
	 */
	protected String encodeFileURI(String unencodedURI)
	{
		return unencodedURI.replaceAll("/", "%2F"); // encode slashes in the file path
	}

}
