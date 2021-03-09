/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.lyo.store.StoreAccessException;

import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.IFilter;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.FilterManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.properties.VeriFitAnalysisProperties;

/**
 * A singleton class responsible for holding AutomationPlan configuration properties that are not included in the
 * AutomationPlan resource properties. AutomationPlans themselves are stored in the sparql triplestore.
 */
public class AutomationPlanConfManager {

    private static AutomationPlanConfManager INSTANCE;

    private AutomationPlanLoading autoPlanLoader;
    private FilterManager filterManager;
    private Map<String, AutomationPlanConf> automationPlanConfigurations;
    
    private AutomationPlanConfManager() {
        this.automationPlanConfigurations = new HashMap<String, AutomationPlanConf>();
        this.autoPlanLoader = new AutomationPlanLoading();
        this.filterManager = new FilterManager();
    }

    public synchronized static AutomationPlanConfManager getInstance() {
        if(INSTANCE == null)
        {
            INSTANCE = new AutomationPlanConfManager();
        }
        return INSTANCE;
    }

    public void initializeAutomationPlans() throws StoreAccessException, Exception
    {
    	this.autoPlanLoader.loadAutomationPlans(new File(VeriFitAnalysisProperties.AUTOPLANS_DEF_PATH_BUILTIN));
    	this.autoPlanLoader.loadAutomationPlans(new File(VeriFitAnalysisProperties.AUTOPLANS_DEF_PATH_CUSTOM));
    	
    	Collection<AutomationPlanConf> autoPlanConfs = this.autoPlanLoader.getAutoPlanConfs();
    	this.filterManager.loadDefaultFilters(autoPlanConfs);
    	this.filterManager.loadFilters(autoPlanConfs, Paths.get(VeriFitAnalysisProperties.PLUGIN_FILTER_CONF_PATH_BUILTIN));
    	//this.filterManager.loadFilters(autoPlanConfs, Paths.get(VeriFitAnalysisProperties.PLUGIN_FILTER_CONF_PATH_CUSTOM)); // TODO is subdirectory so no need to load again
    	
    	for (AutomationPlanConf conf : autoPlanConfs) {
    		this.automationPlanConfigurations.put(conf.getIdentifier(), conf);
    	}
    	
    	this.autoPlanLoader.persistAutomationPlans(this.automationPlanConfigurations);
    }
    
    public AutomationPlanConf getAutoPlanConf(String identifier)
    {
        return this.automationPlanConfigurations.get(identifier);
    }

    public Set<String> getAllAutoPlanIds()
    {
        return this.automationPlanConfigurations.keySet();
    }
    
    public void addAutoPlanConf(String identifier, AutomationPlanConf conf)
    {
        this.automationPlanConfigurations.put(identifier, conf);
    }

    public static class AutomationPlanConf {
    	private String identifier;
        private String launchCommand;
        private String toolSpecificArgs;
        private Boolean oneInstanceOnly;
        private Map<String, IFilter> filters; 

        public AutomationPlanConf(String identifier, String launchCommand, String toolSpecificArgs, Boolean oneInstanceOnly) {
        	this.identifier = identifier;
            this.launchCommand = launchCommand;
            this.toolSpecificArgs = toolSpecificArgs;
            this.oneInstanceOnly = oneInstanceOnly;
            this.filters = new HashMap<String, IFilter>();
        }

        public void setLaunchCommand(String s) {
        	this.launchCommand = s;
        }
        
        public String getLaunchCommand() {
            return launchCommand;
        }

        public String getToolSpecificArgs() {
            return toolSpecificArgs;
        }

        public Boolean getOneInstanceOnly() {
            return oneInstanceOnly;
        }
        
        public String getIdentifier() {
            return identifier;
        }

		public Map<String, IFilter> getFilters() {
			return filters;
		}

		public IFilter getFilter(String name) {
			return filters.get(name);
		}
		
		public Boolean containsFilter(String name) {
			return filters.containsKey(name);
		}

		public void putFilter(String filterName, IFilter filter) {
			this.filters.put(filterName, filter);
		}
        
    }
}
