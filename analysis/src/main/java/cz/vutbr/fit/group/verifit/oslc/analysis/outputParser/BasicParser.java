/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.analysis.outputParser;

import java.util.List;
import java.util.Map;

public class BasicParser implements IParser {

	@Override
	public List<Map<String, String>> parse(List<Map<String, String>> inputContributions) {
		return inputContributions;
	}
	
	/**
	 * TODO
	 * @param unencodedURI
	 * @return use this value as the fileURL element of the contribution map 
	 */
	protected String encodeFileURI(String unencodedURI)
	{
		return unencodedURI.replaceAll("/", "%2F"); // encode slashes in the file path
	}

}
