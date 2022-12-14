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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.group.verifit.oslc.analysis.outputFilters.IFilter;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;

/**
 * Leaves all non file contributions unchanged. Loads the entire contents of the stdout file, the stderr file,
 * and of all non-binary files as their contribution values. All binary file values will be set as empty.
 */
public class DefaultFilter implements IFilter {

	@Override
	public void filter(List<Map<String, String>> inoutContributions) {
		
		new AddAllNonBinaryFileValues().filter(inoutContributions);

	}

	@Override
	public String getName() {
		return "default";
	}
}
