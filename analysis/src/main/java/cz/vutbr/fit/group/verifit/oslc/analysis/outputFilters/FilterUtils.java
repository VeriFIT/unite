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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;

public class FilterUtils {


	/**
	 * Loads the entire contents of a file identified by its contribution filePath property.
	 * Only UTF8 characters are allowed
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static String loadContentsOfFilePathFile(String filePath) throws IOException {
		Path f = Paths.get(filePath);
		byte [] fileContents = Files.readAllBytes(f);
		return Utils.removeNonUtf8CharactersFromBytes(fileContents);
	}
	
}
