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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import cz.vutbr.fit.group.verifit.oslc.OslcValues;
import cz.vutbr.fit.group.verifit.oslc.compilation.VeriFitCompilationManager;
import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;

public class SutFetchUrl implements SutFetcher {

	
	@Override
	/**
	 * Download a file from a URL.
	 * @param url	URL of the file to download
	 * @param folderPath	Path to the folder where to  save the file
	 * @return Name of the new directory / file
	 * @throws Exception 
	 */
	public String fetchSut(String url, Path folderPath) throws IOException {
		URI sourceUrl = null;
		try {
			sourceUrl = new URI (url);
		} catch (URISyntaxException e) {
			throw new IOException("Download from url failed: " + e.getMessage());
		}
		
		try {
			String fileName = OslcValues.getResourceIdFromUri(sourceUrl); // gets the last part of the URL
			Path pathToFile = folderPath.resolve(fileName);
			
			FileUtils.copyURLToFile(
					  new URL(url), 
					  pathToFile.toFile());
			
			return fileName;

		} catch (IOException e) {
			if (e.getMessage().contains(sourceUrl.getPath()))
				throw new IOException("Download from url failed: Host unreachable or URL not found - " + e.getMessage());
			else
				throw new IOException("Download from url failed: " + e.getMessage());
		}
	}

}
