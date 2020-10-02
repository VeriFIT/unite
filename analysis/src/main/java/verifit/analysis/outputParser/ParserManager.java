package verifit.analysis.outputParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.lyo.oslc.domains.auto.Contribution;
import org.eclipse.lyo.oslc4j.core.model.Link;

import verifit.analysis.VeriFitAnalysisManager;

public final class ParserManager {
    
    private static ParserManager INSTANCE;
	
	private Map<String,IParser> toolParsers;

    private ParserManager() {
    	this.toolParsers = new HashMap<String,IParser>(); // TODO map implementation ?
    }
 
    public synchronized static ParserManager getInstance() {
		if(INSTANCE == null) 
		{
			INSTANCE = new ParserManager();
		}
		return INSTANCE;
    }
    
    /**
     * Uses a plugin implemented parser to process contributions created by an analysis tool. Can modify or delete contributions 
     * or even create new ones based on the original ones.
     * @param toolIdentifier		dcterms:identifier of the tool which these contributions belong to (used to select parser)
     * @param outputContributions	contributions produced by the tool
     * @return	parsed contributions
     */
    public Set<Contribution> parseContributionsForTool(String toolIdentifier, Set<Contribution> outputContributions)
    {
    	// get a parser plugin implementation for the specified tool, default to the basic one (does nothing)
    	IParser parser = toolParsers.getOrDefault(toolIdentifier, new BasicParser());
    	
    	// prepare the parser input
    	List<Map<String,String>> parserInput = new LinkedList<Map<String,String>>();
    	for (Contribution contrib : outputContributions)
    	{
    		Map<String,String> newMap = new HashMap<String,String>();
    		newMap.put("name", contrib.getTitle());
    		newMap.put("value", contrib.getValue());
    		if (contrib.getFileURI() != null)
    			newMap.put("fileURI", contrib.getFileURI().toString());
    		newMap.put("description", contrib.getDescription());
    		for (Link type : contrib.getType())	// should contain only one --> iterate anyway just in case, last one overwrites others TODO 
    		{
        		newMap.put("type", type.getValue().toString());
    		}
    		parserInput.add(newMap);
    	}
    	
    	// call the parser to process the contributions
    	List<Map<String,String>> parserOutput = parser.parse(parserInput);
    	
    	// process the parser output
    	Set<Contribution> parsedContributions = new HashSet<Contribution>();
    	for (Map<String,String> mapContrib : parserOutput)
    	{
    		String name = mapContrib.get("name");
    		if (name == null)
    		{
    			System.out.println("WARNING: Output of contribution parser for tool \"" + toolIdentifier + "\" - name missing");
    		}
			String description = mapContrib.get("description");
    		if (description == null)
    		{
    			System.out.println("WARNING: Output of contribution parser for tool \"" + toolIdentifier + "\" - description missing");
    		}
    		String fileURI = mapContrib.get("fileURI");
			String type = mapContrib.get("type");
    		if (type == null)
    		{
    			System.out.println("WARNING: Output of contribution parser for tool \"" + toolIdentifier + "\" - type missing");
    		}
    		Set<Link> setType = new HashSet<Link>();
    		try {
				setType.add(new Link(new URI(type)));
			} catch (URISyntaxException e1) {
				System.out.println("WARNING: Output of contribution parser for tool \"" + toolIdentifier + "\" - invalid type: " + e1.getMessage());
			}
    		
    		// create new contribution base on the Map element
			try {
				Contribution newContrib = new Contribution();
				newContrib.setTitle(name);
				newContrib.setDescription(description);
				newContrib.setFileURI(new URI(fileURI));
				newContrib.setType(setType);
				parsedContributions.add(newContrib);
			} catch (URISyntaxException e) {
				// TODO should never happen
				e.printStackTrace();
			}
    	}
    	
    	return parsedContributions;
    }
}