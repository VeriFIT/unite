/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package pluginFilters;

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
 * Use this class as a Template for creating your own filtersv.
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
		// delete all current contributions
		inoutContributions.clear();
		
		// and create a single new one instead
		Map<String, String> contrib = new HashMap<String, String>();
		contrib.put("id", "testContributionId");
		contrib.put("title", "test name");
		contrib.put("value", "test value");
		contrib.put("description", "This contribution was filtered by the TestPluginFilter");
		contrib.put("valueType", OslcValues.OSLC_VAL_TYPE_STRING.getValue().toString());
		inoutContributions.add(contrib);
	}

	@Override
	public String getName() {
		return name;
	}
}
