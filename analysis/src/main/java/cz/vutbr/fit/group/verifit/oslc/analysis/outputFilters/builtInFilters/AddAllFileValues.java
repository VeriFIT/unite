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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.FilterUtils;
import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;

public class AddAllFileValues extends DefaultFilter {

	@Override
	public void filter(List<Map<String, String>> inoutContributions) {

		for (Map<String, String> contrib : inoutContributions)
		{
			String filePath = contrib.get("filePath");
			if (filePath != null) // if is file
			{
				try {
					contrib.put("value", FilterUtils.loadContentsOfFilePathFile(filePath));
				} catch (IOException e) {
					contrib.put("value", "Failed to load contents of this file: " + e.getMessage());
				}
			}	
		}
	}

	@Override
	public String getName() {
		return "addAllFileValues";
	}
}
