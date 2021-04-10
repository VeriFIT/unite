/*
 * Copyright (C) 2020 OndÅ™ej VaÅ¡Ã­Ä�ek <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package pluginFilters.customPluginFilters;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.group.verifit.jsem.IExtension;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.IFilter;

import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues; // use this for defined oslc value types

import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.FilterUtils; // see this for useful utils

// built-in filters
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.builtInFilters.AddAllNonBinaryFileValues;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.builtInFilters.AddStdoutAndStderrValues;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.builtInFilters.DefaultFilter;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.builtInFilters.RemoveAllFileValues;

/**
 * Use this class as a Template for creating your own filters.
 * See the IFilter interface for more info.
 */
public class ExamplePluginFilter implements IFilter, IExtension {
	
	/**
	 * TEMPLATE TODO
	 * Make sure to change this value when creating your own filter.
	 * Names have to be unique per AutomationPlan (each AutomationPlan can only have uniquely named filters associated with it)
	 */
	final String name = "example";

	public void filter(List<Map<String, String>> inoutContributions) {

		/**
		 *  TEMPLATE TODO
		 *  do anything you want with the Contributions.
		 *  
		 *  Delete, create, modify, parse...
		 *  Chain multiple filters...
		 */
		
		/**
		 * Example below
		 */
		
		// run Contributions through one of the builtin parsers first to load stdout and stderr file contents
		new AddStdoutAndStderrValues().filter(inoutContributions);
		
		// look for data race detection reports in stdout
		Boolean dataRaceFound = false;
		for (Map<String, String> contrib : inoutContributions)
		{
			String title = contrib.get("title");
			if (title.equals("stdout"))
			{
				String contentsOfTheStdout = contrib.get("value");
				if (contentsOfTheStdout.contains("Data race detected at"))	// check if a data race was reported
				{
					dataRaceFound = true;
				}
			}	
		}
		
		// create a contribution representing the result (based on the stdout contents)
		Map<String, String> contrib = new HashMap<String, String>();
		contrib.put("id", "example_id");	// use any identifier you want (can be used to e.g. query the contribution), careful about collisions with other contributions
		contrib.put("title", "DataRaceDetected");		// use any name you want
		contrib.put("description", "Holds the result of data race analysis.");
		contrib.put("value", dataRaceFound.toString());
		contrib.put("valueType", "http://www.w3.org/2001/XMLSchema#boolean");
		inoutContributions.add(contrib);
	}

	@Override
	public String getName() {
		return name;
	}
}
