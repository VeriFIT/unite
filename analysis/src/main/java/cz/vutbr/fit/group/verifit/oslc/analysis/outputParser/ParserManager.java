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

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.lyo.oslc.domains.auto.Contribution;
import org.eclipse.lyo.oslc4j.core.model.Link;

import cz.vutbr.fit.group.verifit.jsem.ExtensionInfoQuery;
import cz.vutbr.fit.group.verifit.jsem.ExtensionManager;
import cz.vutbr.fit.group.verifit.jsem.IExtension;
import cz.vutbr.fit.group.verifit.jsem.IExtensionWithInfo;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager.AutomationPlanConf;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputParser.parsers.DefaultParser;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputParser.parsers.RemoveAllFileValuesParser;
import cz.vutbr.fit.group.verifit.oslc.analysis.properties.VeriFitAnalysisProperties;


public final class ParserManager {
    
	private final Path parserConfDir;

    public ParserManager(Path parserConfDir) {
    	this.parserConfDir = parserConfDir;
    }

    /**
     * Loads plugin parsers for each tool and adds them into the automationPlan's configuration
     * @param automationPlanConfs An In/Out parameter
     */
    public void loadParsers(Collection<AutomationPlanConf> automationPlanConfs) {
    	
    	// initialize the ExtensionManager
    	ExtensionManager extMgr = new ExtensionManager();
    	extMgr.registerBuiltinExtensions();
    	extMgr.refreshExtensionInfoLoaders();
    	extMgr.loadExtensionInfo(this.parserConfDir);
    	
    	// load parsers for every Automation Plan
    	for (AutomationPlanConf conf : automationPlanConfs)
    	{
    		String id = conf.getIdentifier();
    		
    		// add the default parsers to all autoPlans
    		addDefaultParsers(conf);
    		
    		// load custom plugin parsers
			// find all parser extensions for the given Automation Plan
			ExtensionInfoQuery query = new ExtensionInfoQuery();
			query.contains("implements", IParser.class.getName());
			query.contains("tool", id);
			List< IExtension > exts = extMgr.findExtension(query);
			
			// select a plugin parser for the given Automation Plan
			if (exts.size() != 0)
			{
				for (IExtension ext : exts)
				{
					IParser pluginParser = (IParser) ext;
					String pluginName = pluginParser.getName();
					
					if (conf.containsParser(pluginName))
						System.out.println("Warning: Can not load plugin parser/filter \"" + pluginParser.getClass() + "\" for Automation Plan: \"" + id + "\"\n"
								+ "   Name \"" + pluginName + "\" is already taken.");
					else
						conf.putParser(pluginName, pluginParser);					
				}
			}
			else
			{
				// TODO future features
				System.out.println("Info: No plugin filters/parsers found for Automation Plan: \"" + id + "\" - Only the default ones will be available");
			}
    	}
    }
    
    private void addDefaultParsers(AutomationPlanConf conf) {
		conf.putParser(new DefaultParser().getName(), new DefaultParser());
		conf.putParser(new RemoveAllFileValuesParser().getName(), new RemoveAllFileValuesParser());
	}

	/**
     * Uses a plugin implemented parser to process contributions created by an analysis tool. Can modify or delete contributions 
     * or even create new ones based on the original ones.
     * @param parser				name of the parser to use
     * @param outputContributions	contributions produced by the tool
     */
    public static Set<Contribution> parseContributionsForTool(IParser parser, Set<Contribution> outputContributions)
    {
    	// prepare the parser input
    	List<Map<String,String>> contributionsForParser = new LinkedList<Map<String,String>>();
    	for (Contribution contrib : outputContributions)
    	{
    		Map<String,String> newMap = new HashMap<String,String>();
    		newMap.put("title", contrib.getTitle());
    		newMap.put("value", contrib.getValue());
    		if (contrib.getFileURI() != null)
    			newMap.put("fileURI", contrib.getFileURI().toString());
    		newMap.put("description", contrib.getDescription());
    		for (Link valueType : contrib.getValueType())	// should contain only one --> iterate anyway just in case, last one overwrites others TODO 
    		{
        		newMap.put("valueType", valueType.getValue().toString());
    		}
    		contributionsForParser.add(newMap);
    	}
    	
    	// call the parser to process the contributions
    	parser.parse(contributionsForParser);
    	
    	// process the parser output
    	Set<Contribution> parsedContributions = new HashSet<Contribution>();
    	for (Map<String,String> mapContrib : contributionsForParser)
    	{
    		String title = mapContrib.get("title");
    		if (title == null)
    		{
    			System.out.println("WARNING: Output of contribution parser - title missing");
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
				} catch (URISyntaxException e) {
					System.out.println("WARNING: Output of contribution parser - invalid valueType: " + e.getMessage());
				}
			}
    		
    		// create new contribution base on the Map element
			try {
				Contribution newContrib = new Contribution();
				newContrib.setTitle(title);
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