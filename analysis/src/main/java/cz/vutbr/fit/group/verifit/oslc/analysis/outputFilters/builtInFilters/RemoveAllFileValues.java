/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.builtInFilters;

import java.util.List;
import java.util.Map;

import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.IFilter;

public class RemoveAllFileValues implements IFilter {

	@Override
	public void parse(List<Map<String, String>> inoutContributions) {

		for (Map<String, String> contrib : inoutContributions)
		{
			if (contrib.get("fileURI") != null) // if is file
				contrib.remove("value");
		}
	}

	@Override
	public String getName() {
		return "removeAllFileValues";
	}
	
	
}
