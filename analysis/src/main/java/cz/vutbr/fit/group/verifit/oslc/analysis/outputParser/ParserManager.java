/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.analysis.outputParser;

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

import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisManager;

public final class ParserManager {
    
    private static ParserManager INSTANCE;
	
	private Map<String,IParser> toolParsers;

    private ParserManager() {
    	this.toolParsers = new HashMap<String,IParser>(); // TODO map implementation ?
    	loadParsers();
    }
 
    public synchronized static ParserManager getInstance() {
		if(INSTANCE == null) 
		{
			INSTANCE = new ParserManager();
		}
		return INSTANCE;
    }
    
    private void loadParsers() {
    	//this.toolParsers.put("infer", new NoFileValuesParser()); // TODO tmp instead of actual plugins
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
    	IParser parser = toolParsers.getOrDefault(toolIdentifier, new DefaultParser());
    	
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
    		for (Link valueType : contrib.getValueType())	// should contain only one --> iterate anyway just in case, last one overwrites others TODO 
    		{
        		newMap.put("valueType", valueType.getValue().toString());
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
    		String value = mapContrib.get("value");
			String description = mapContrib.get("description");
    		String fileURI = mapContrib.get("fileURI");
			String valueType = mapContrib.get("valueType");
			Set<Link> setValueType = new HashSet<Link>();
    		if (valueType != null)
    		{
				try {
					setValueType.add(new Link(new URI(valueType)));
				} catch (URISyntaxException e1) {
					System.out.println("WARNING: Output of contribution parser for tool \"" + toolIdentifier + "\" - invalid valueType: " + e1.getMessage());
				}
			}
    		
    		// create new contribution base on the Map element
			try {
				Contribution newContrib = new Contribution();
				newContrib.setTitle(name);
				newContrib.setValue(value);
				newContrib.setDescription(description);
				newContrib.setFileURI((fileURI != null ? new URI(fileURI) : null));
				newContrib.setValueType(setValueType);
				parsedContributions.add(newContrib);
			} catch (URISyntaxException e) {
				// TODO should never happen
				e.printStackTrace();
			}
    	}
    	
    	return parsedContributions;
    }
}