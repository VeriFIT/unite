package cz.vutbr.fit.group.verifit.oslc.analysis.unicConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.store.StoreAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.group.verifit.oslc.analysis.VeriFitAnalysisManager;
import cz.vutbr.fit.group.verifit.oslc.analysis.automationPlans.AutomationPlanConfManager.AutomationPlanConf;

public class UnicConfLoader {

	private final Logger log;
	private final List<String> listOfLoadedConf; // for logging purposes

	public UnicConfLoader() {
		log = LoggerFactory.getLogger(VeriFitAnalysisManager.class);
		listOfLoadedConf = new ArrayList<String>();
	}


    /**
     * Loads all UniC conf files from a specified directory and makes a map out of them for AHT registration.
     * Configuration can contain multiple analysis tools -- each tool needs a .tasks file and a .config file.
     * The files have the same contents as .properties files, so they can be loaded as such.
     * For AHT we need all configuration files merged into a single Map.
     * Entries from .tasks and .config need to be prefixed to be able to differentiate between them. 
     * @param unicConfFilesPath path to search for conf files in
     * @return A map of all configuration properties from all files 
     */
    public Map<String,String> loadUnicConfFilesForAht(File unicConfFilesPath)
    {
        File[] unicConfFiles = findUnicConfFiles(unicConfFilesPath);
        if (unicConfFiles == null)
        {
            log.warn("Loading UniC configuration: Did not find any configuration files in "
                + unicConfFilesPath.getPath());
        } else {
        	
        }
        

		// all configuration files need to be merged into a single map
        // while prefixing their property names to differentiate them
		Map<String,String> mergedAndPrefixedProperties = new HashMap<String,String>();
        
		// load Unic configuration properties files one by one
		// and keep merging them into the overall map
        for (File unicConf : unicConfFiles)
        {
        	try {
        		Map<String,String> tmpMergeMap = new HashMap<String,String>();
        		
	        	// config consists of a .tasks file and a .config file
	        	File confTasksFile = unicConf;
	            String confUnicName = unicConf.getName().replace(".tasks", ".config");
	            File confUnicFile = unicConf.toPath().getParent().resolve(confUnicName).toFile();
	        	
	            // load both as properties
	    		Properties propertiesUnicConf = new Properties();
					propertiesUnicConf.load(new FileInputStream(confUnicFile));
	    		Properties propertiesTasksConf = new Properties();
	    		propertiesTasksConf.load(new FileInputStream(confTasksFile));
	    		
	    		// prefix their key names and merge them into the overall map
	            Set<String> propertyNames = propertiesUnicConf.stringPropertyNames();
	            for (String name : propertyNames) {
	                String propertyValue = propertiesUnicConf.getProperty(name);
	                tmpMergeMap.put("unic.config." + name, propertyValue);
	    	    }
	            propertyNames = propertiesTasksConf.stringPropertyNames();
	            for (String name : propertyNames) {
	                String propertyValue = propertiesTasksConf.getProperty(name);
	                tmpMergeMap.put("unic.tasks." + name, propertyValue);
	            }
	            
	            // once everything loads fine, then merge the tmp map into the overall one
	            mergedAndPrefixedProperties.putAll(tmpMergeMap);
	            
	            // add to a list for logging
	            listOfLoadedConf.add(unicConf.getName().replace(".tasks", "")); 
	            
        	} catch (IOException e) {
        		log.error("failed to load a UniC configuraion file pair for \"" + unicConf.getName().replace(".tasks", "") + "\" (expected X.tasks + X.config): " + e.getMessage());
        	}
        }			
		
		return mergedAndPrefixedProperties;
    }

    public List<String> getListOfLoadedConf() {
		return listOfLoadedConf;
	}

    
    /**
     * Finds all .tasks files in the UniC configuration directory
     * @param inFolder	Folder to search in
     * @return Collection of .tasks files
     */
    private File[] findUnicConfFiles(File inFolder)
    {
        File unicConfFileFolder = inFolder;
        File[] unicConfFiles = unicConfFileFolder.listFiles(
                (dir, name) -> name.endsWith(".tasks")
            );

        return unicConfFiles;
    }
}
