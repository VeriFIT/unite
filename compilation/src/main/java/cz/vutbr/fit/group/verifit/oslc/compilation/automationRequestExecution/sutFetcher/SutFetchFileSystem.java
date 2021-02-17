/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cz.vutbr.fit.group.verifit.oslc.compilation.automationRequestExecution.sutFetcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

public class SutFetchFileSystem implements SutFetcher {

	/**
	 * Copy a file from the file system as a byte stream.
	 * @param pathToSource Path to the source file
	 * @param folderPath	Path to the folder where to  save the file
	 * @return Name of the new directory / file
	 * @throws IOException
	 */
	@Override
	public String fetchSut(String pathToSource, Path folderPath) throws IOException
	{
		try {
			File source = new File(pathToSource);
			
			if (source.isDirectory())
				FileUtils.copyDirectory(source, folderPath.toFile(), true);
			else
				FileUtils.copyFileToDirectory(source, folderPath.toFile(), true);	
		    
		    return source.getName();
		    
		} catch (IOException e) {
			throw new IOException("Fetch from file system failed: " + e.getMessage());
		}
	}
}
