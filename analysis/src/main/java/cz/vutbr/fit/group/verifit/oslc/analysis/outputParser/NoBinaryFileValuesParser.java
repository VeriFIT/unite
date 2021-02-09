/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package verifit.analysis.outputParser;

import java.util.List;
import java.util.Map;

import verifit.analysis.VeriFitAnalysisConstants;

public class NoBinaryFileValuesParser extends BasicParser implements IParser {

	@Override
	public List<Map<String, String>> parse(List<Map<String, String>> inputContributions)
	{
		for (Map<String, String> contrib : inputContributions)
		{
			if (contrib.get("valueType") == VeriFitAnalysisConstants.OSLC_VAL_TYPE_BASE64BINARY)
			{
				contrib.remove("value");
				contrib.remove("valueType");
			}
		}
		
		return inputContributions;
	}

}
