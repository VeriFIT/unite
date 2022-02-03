/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters;

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
import cz.vutbr.fit.group.verifit.oslc.OslcValues;
import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisResourcesFactory;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager.AutomationPlanConf;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.builtInFilters.AddAllFileValues;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.builtInFilters.AddAllNonBinaryFileValues;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.builtInFilters.AddStdoutAndStderrValues;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.builtInFilters.DefaultFilter;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.builtInFilters.RemoveAllFileValues;
import cz.vutbr.fit.group.verifit.oslc.analysis.properties.VeriFitAnalysisProperties;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;


public final class FilterManager {
    

    public FilterManager() {
    }

    /**
     * @param automationPlanConfs An In/Out parameter
     */
    public void loadDefaultFilters(Collection<AutomationPlanConf> automationPlanConfs)
    {
    	// load filters for every Automation Plan
    	for (AutomationPlanConf conf : automationPlanConfs)
    	{
	    	// add the default filters to all autoPlans
			addDefaultFilters(conf);
    	}
    }
    
    /**
     * Loads plugin filters for each tool and adds them into the automationPlan's configuration
     * @param automationPlanConfs An In/Out parameter
     */
    public void loadFilters(Collection<AutomationPlanConf> automationPlanConfs, Path filterConfDir) {
    	
    	// initialize the ExtensionManager
    	ExtensionManager extMgr = new ExtensionManager();
    	extMgr.registerBuiltinExtensions();
    	extMgr.refreshExtensionInfoLoaders();
    	extMgr.loadExtensionInfo(filterConfDir);
    	
    	// load filters for every Automation Plan
    	for (AutomationPlanConf conf : automationPlanConfs)
    	{
    		String id = conf.getIdentifier();
    		
    		// load custom plugin filter
			// find all filter extensions for the given Automation Plan
			ExtensionInfoQuery query = new ExtensionInfoQuery();
			query.contains("implements", IFilter.class.getName());
			query.contains("tool", id);
			List< IExtension > exts = extMgr.findExtension(query);
			
			// select a plugin filter for the given Automation Plan
			if (exts.size() != 0)
			{
				for (IExtension ext : exts)
				{
					IFilter pluginFilter = (IFilter) ext;
					String pluginName = pluginFilter.getName();
					
					if (conf.containsFilter(pluginName))
						System.out.println("ERROR: Can not load plugin filter \"" + pluginFilter.getClass() + "\" for Automation Plan: \"" + id + "\"\n"
								+ "   Name \"" + pluginName + "\" is already taken.");
					else
						conf.putFilter(pluginName, pluginFilter);					
				}
			}
    	}
    }
    
    private void addDefaultFilters(AutomationPlanConf conf) {
		conf.putFilter(new DefaultFilter().getName(), new DefaultFilter());
		conf.putFilter(new AddStdoutAndStderrValues().getName(), new AddStdoutAndStderrValues());
		conf.putFilter(new RemoveAllFileValues().getName(), new RemoveAllFileValues());
		conf.putFilter(new AddAllNonBinaryFileValues().getName(), new AddAllNonBinaryFileValues());
		conf.putFilter(new AddAllFileValues().getName(), new AddAllFileValues());
	}

	/**
     * Uses a plugin implemented filter to process contributions created by an analysis tool. Can modify or delete contributions 
     * or even create new ones based on the original ones.
     * @param filter				name of the filter to use
     * @param outputContributions	contributions produced by the tool
     */
    public static Set<Contribution> filterContributionsForTool(IFilter filter, Set<Contribution> outputContributions)
    {
    	// prepare the filter input
    	List<Map<String,String>> contributionsForFilter = new LinkedList<Map<String,String>>();
    	for (Contribution contrib : outputContributions)
    	{
    		Map<String,String> newMap = new HashMap<String,String>();
    		newMap.put("id", OslcValues.getResourceIdFromUri(contrib.getAbout()));
    		newMap.put("title", contrib.getTitle());
    		newMap.put("value", contrib.getValue());
			newMap.put("filePath", contrib.getFilePath());
    		newMap.put("description", contrib.getDescription());
    		for (Link valueType : contrib.getValueType())	// should contain only one --> iterate anyway just in case, last one overwrites others TODO 
    		{
        		newMap.put("valueType", valueType.getValue().toString());
    		}
    		contributionsForFilter.add(newMap);
    	}
    	
    	// call the filter to process the contributions
    	filter.filter(contributionsForFilter);
    	
    	// process the filter output
    	Set<Contribution> filteredContributions = new HashSet<Contribution>();
    	for (Map<String,String> mapContrib : contributionsForFilter)
    	{
    		String title = mapContrib.get("title");
    		if (title == null)
    		{
    			System.out.println("WARNING: Output of contribution filter - title missing");
    		}
    		String value = mapContrib.get("value");
			String description = mapContrib.get("description");
    		String filePath = mapContrib.get("filePath");
			String valueType = mapContrib.get("valueType");
			Set<Link> setValueType = new HashSet<Link>();
    		if (valueType != null)
    		{
				try {
					setValueType.add(new Link(new URI(valueType)));
				} catch (URISyntaxException e) {
					System.out.println("WARNING: Output of contribution filter - invalid valueType: " + e.getMessage());
				}
			}
    		String id = mapContrib.get("id");
    		if (id == null)
    		{
    			System.out.println("ERROR: Output of contribution filter - \"id\" missing. Contribution \"" + title + "\" can not be created.");
    			continue;
    		}
    		
    		// create new contribution base on the Map element
			Contribution newContrib = VeriFitAnalysisResourcesFactory.createContribution(id);
			newContrib.setTitle(title);
			newContrib.setValue(value);
			newContrib.setDescription(description);
			newContrib.setFilePath(filePath);
			newContrib.setValueType(setValueType);
			filteredContributions.add(newContrib);
    	}
    	
    	return filteredContributions;
    }
}