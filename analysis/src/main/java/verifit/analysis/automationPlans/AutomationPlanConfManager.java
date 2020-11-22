package verifit.analysis.automationPlans;

import java.util.HashMap;
import java.util.Map;

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
