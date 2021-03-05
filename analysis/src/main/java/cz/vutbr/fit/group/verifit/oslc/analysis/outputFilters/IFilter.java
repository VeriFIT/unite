/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters;

import java.util.List;
import java.util.Map;

public interface IFilter {

	/**
	 * Processes output contributions of an analysis execution. Filters are plugins created for specific tools and use cases.
	 * All created contributions are passed to the filter and only the ones returned by the filter will be included in the
	 * final Automation Result (returned to the client and saved in the database). The simplest filter just returns its input
	 * without any modifications. A proper/advanced filter can remove or modify contributions, or create new contributions
	 * based on information obtained from the original ones. 
	 * 
	 * @param inoutContributions	Each element of the list represents one output contribution (e.g. stdout or a log file).
	 * 								Expected map entries are "id", "title", "value", "valueType", "description", and "filePath".
	 */
	public void filter(List<Map<String,String>> inoutContributions);

	/**
	 * Every filter needs a name that is unique per tool (only one of each name for each AutomationPlan)
	 * @return
	 */
	public String getName();
	
}
