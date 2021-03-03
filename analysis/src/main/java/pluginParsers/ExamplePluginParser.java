/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package pluginParsers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.group.verifit.jsem.IExtension;
import cz.vutbr.fit.group.verifit.oslc.analysis.outputParser.IParser;
import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;


/**
 * Use this class as a Template for creating your own parsers.
 * See the IParser interface for more info.
 */
public class ExamplePluginParser implements IParser, IExtension {
	
	/**
	 * TEMPLATE TODO
	 * Make sure to change this value when creating your own filter/parser.
	 * Names have to be unique per AutomationPlan (each AutomationPlan can only have uniquely named filters/parsers associated with it)
	 */
	final String name = "example";

	public void parse(List<Map<String, String>> inoutContributions) {

		/**
		 *  TEMPLATE TODO
		 *  do anything you want with the Contributions 
		 */
		
		/**
		 * Example below
		 */
		
		// delete all current contributions
		inoutContributions.clear();
		
		// and create a single new one instead
		Map<String, String> contrib = new HashMap<String, String>();
		contrib.put("name", "test name");
		contrib.put("value", "test value");
		contrib.put("description", "This contribution was parserd by the TestPluginParser");
		contrib.put("valueType", OslcValues.OSLC_VAL_TYPE_STRING.getValue().toString());
		inoutContributions.add(contrib);
	}

	@Override
	public String getName() {
		return name;
	}
}
