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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A singleton class responsible for holding AutomationPlan configuration properties that are not included in the
 * AutomationPlan resource properties. AutomationPlans themselves are stored in the sparql triplestore.
 */
public class AutomationPlanConfManager {

    private static AutomationPlanConfManager INSTANCE;

    private Map<String, AutomationPlanConf> automationPlanConfigurations;

    private AutomationPlanConfManager() {
        this.automationPlanConfigurations = new HashMap<String, AutomationPlanConf>();
    }

    public synchronized static AutomationPlanConfManager getInstance() {
        if(INSTANCE == null)
        {
            INSTANCE = new AutomationPlanConfManager();
        }
        return INSTANCE;
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
        private String launchCommand;
        private String toolSpecificArgs;
        private Boolean oneInstanceOnly;

        public AutomationPlanConf(String launchCommand, String toolSpecificArgs, Boolean oneInstanceOnly) {
            this.launchCommand = launchCommand;
            this.toolSpecificArgs = toolSpecificArgs;
            this.oneInstanceOnly = oneInstanceOnly;
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
    }
}
