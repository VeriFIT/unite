package verifit.analysis.automationPlans;

import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc4j.provider.jena.LyoJenaModelException;
import org.eclipse.lyo.store.StoreAccessException;
import verifit.analysis.VeriFitAnalysisManager;
import verifit.analysis.exceptions.OslcResourceException;

import java.io.FileNotFoundException;

import static verifit.analysis.VeriFitAnalysisProperties.AUTOPLANS_DEF_PATH;
import static verifit.analysis.automationPlans.AutomationPlanDefinition.getDummyAutomationPlanDefinition;
import static verifit.analysis.utils.parseResourcesFromXmlFile;

public class AutomationPlanLoading {
    /**
     * Creates all the predefined AutomationPlans making them available in the adapter catalog
     * @throws StoreAccessException
     */
    public static void createPredefinedAutomationPlans() throws StoreAccessException
    {
        try {
            // create the dummy tool AutoPlan (independent of xml config)
            VeriFitAnalysisManager.createAutomationPlan(getDummyAutomationPlanDefinition());
        } catch (OslcResourceException e) {
            System.out.println("WARNING: Failed to create dummy tool automation plan" + e.getMessage());
        }


        // load automation plans
        AutomationPlan[] autoPlans = null;
        try {
            autoPlans = parseResourcesFromXmlFile(AUTOPLANS_DEF_PATH, AutomationPlan.class);
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Loading AutomationPlan: Failed to open the conf. xml file: " + e.getMessage());
            System.exit(1);
        } catch (LyoJenaModelException e) {
            System.out.println("ERROR: Loading AutomationPlan: Failed to parse the conf. xml file: " + e.getMessage());
            System.exit(1);
        }
        // create the loaded automation plans
        for (AutomationPlan plan : autoPlans)
        {
            try {
                if (plan.getIdentifier().equals("dummy"))
                    System.out.println("WARNING: User defined AutomationPlan with identifier \"dummy\" can not be " +
                            "created. Identifier is reserved for the internal testing tool.");
                else
                    VeriFitAnalysisManager.createAutomationPlan(plan);
            } catch (OslcResourceException e) {
                System.out.println("WARNING: " + e.getMessage());
            }
        }
    }
}
