package verifit.analysis.automationPlans;

import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc4j.provider.jena.LyoJenaModelException;
import org.eclipse.lyo.store.StoreAccessException;
import verifit.analysis.VeriFitAnalysisManager;
import verifit.analysis.VeriFitAnalysisProperties;
import verifit.analysis.exceptions.OslcResourceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;

import static verifit.analysis.automationPlans.AutomationPlanDefinition.getDummyAutomationPlanConf;
import static verifit.analysis.automationPlans.AutomationPlanDefinition.getDummyAutomationPlanDefinition;
import static verifit.analysis.utils.parseResourcesFromXmlFile;

public class AutomationPlanLoading {

    /**
     * Each Automation Plan is defined by an .rdf file and one .properties file of the same name.
     * The .rdf file holds the base of the Automation Plan resource and the .properties file specifies other
     * configuration (e.g. path to tool executable etc.)
     * @return
     */
    public static void loadAutomationPlans() throws StoreAccessException
    {
        // create the predefined AutomationPlans from the source code
        createPredefinedAutomationPlans();

        // load automation plans one by one from the user configuration
        File[] autoPlansDefFiles = findAutomationPlanDefinitionFiles();
        if (autoPlansDefFiles == null)
        {
            System.out.println("WARNING: Loading AutomationPlans: Did not find any AutomationPlan definitions in "
                + VeriFitAnalysisProperties.AUTOPLANS_DEF_PATH);
        }
        else {
            for (File autoPlanDef : autoPlansDefFiles)
            {
                if (autoPlanDef.getName().equals("ExampleTool.rdf"))
                    continue; // skip the example

                // load the matching properties file
                String autoPlanPropsName = autoPlanDef.getName().replace(".rdf", ".properties");
                Path autoPlanPropsPath = autoPlanDef.toPath().getParent().resolve(autoPlanPropsName);
                Properties autoPlanProps = new Properties();
                try {
                    autoPlanProps.load(new FileInputStream(autoPlanPropsPath.toFile()));
                } catch (IOException e) {
                    System.out.println("WARNING: Loading AutomationPlan: Failed to open matching .properties file ("
                            + autoPlanPropsName + "): " + e.getMessage());
                    continue;
                }

                // parse the AutoPlan .rdf file
                AutomationPlan[] parsedResources = new AutomationPlan[0];
                try {
                    parsedResources = parseResourcesFromXmlFile(autoPlanDef, AutomationPlan.class);
                } catch (FileNotFoundException e) {
                    System.out.println("WARNING: Loading AutomationPlan: Failed to open definition .rdf file ("
                            + autoPlanDef.getName() + "): " + e.getMessage());
                    continue;
                } catch (LyoJenaModelException | org.apache.jena.riot.RiotException e) {
                    System.out.println("WARNING: Loading AutomationPlan: Failed to parse definition .rdf file: ("
                            + autoPlanDef.getName() + "): " + e.getMessage());
                    continue;
                }
                if (parsedResources == null || parsedResources.length != 1) {
                    System.out.println("WARNING: Loading AutomationPlan: Parsed definition .rdf file ("
                            + autoPlanDef.getName() + ") did not contain exactly on Automation Plan resource");
                    continue;
                }
                AutomationPlan newAutoPlan = parsedResources[0];

                // load the properties
                String toolLaunchCommand = autoPlanProps.getProperty("toolLaunchCommand");
                if (toolLaunchCommand == null)
                {
                    System.out.println("WARNING: Loading AutomationPlan: The matching .properties file ("
                            + autoPlanDef.getName() + ") is missing a toolLaunchCommand");
                    continue;
                }
                String toolSpecificArgs = autoPlanProps.getProperty("toolSpecificArgs");
                toolSpecificArgs = (toolSpecificArgs == null ? "" : toolSpecificArgs); // default value
                Boolean OneInstanceOnly = Boolean.parseBoolean(autoPlanProps.getProperty("OneInstanceOnly"));
                OneInstanceOnly = (OneInstanceOnly == null ? false : OneInstanceOnly); // default value

                // create the Automation Plan
                if ("dummy".equals(newAutoPlan.getIdentifier())) {
                    System.out.println("WARNING: Loading AutomationPlan: User defined AutomationPlan with identifier" +
                            " \"dummy\" can not be created. Identifier is reserved for the internal testing tool.");
                    continue;
                } else {
                    try {
                        newAutoPlan = VeriFitAnalysisManager.createAutomationPlan(newAutoPlan); //// commitment point
                    } catch (OslcResourceException e) {
                        System.out.println("WARNING: " + e.getMessage());
                        continue;
                    }
                }

                // save the properties
                AutomationPlanConfManager autoPlanConfManager = AutomationPlanConfManager.getInstance();
                autoPlanConfManager.addAutoPlanConf(
                        newAutoPlan.getIdentifier(),
                        new AutomationPlanConfManager.AutomationPlanConf(
                                toolLaunchCommand,
                                toolSpecificArgs,
                                OneInstanceOnly
                        ));
            }
        }
    }

    /**
     * Creates all the predefined AutomationPlans making them available in the adapter catalog
     * @throws StoreAccessException
     */
    private static void createPredefinedAutomationPlans() throws StoreAccessException
    {
        try {
            // create the dummy tool AutoPlan (independent of xml config)
            VeriFitAnalysisManager.createAutomationPlan(getDummyAutomationPlanDefinition());

            // save the properties
            AutomationPlanConfManager.getInstance()
                    .addAutoPlanConf(
                        getDummyAutomationPlanDefinition().getIdentifier(),
                        getDummyAutomationPlanConf()
                    );

        } catch (OslcResourceException e) {
            System.out.println("WARNING: Failed to create dummy tool automation plan" + e.getMessage());
        }
    }

    /**
     * Definitions are .rdf files in a specified directory
     * @return
     */
    private static File[] findAutomationPlanDefinitionFiles()
    {
        File autoPlansDefFolder = new File(VeriFitAnalysisProperties.AUTOPLANS_DEF_PATH);
        File[] autoPlanDefFiles = autoPlansDefFolder.listFiles(
                (dir, name) -> name.endsWith(".rdf")
            );

        return autoPlanDefFiles;
    }
}
