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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Base64.Decoder;

public class SutFetchBase64 implements SutFetcher {

	/**
	 * Decode a base64 encoded string into bytes and write them into a file as a byte stream.
	 * @param base64EncProgram Base64 encoded file to decode and write. First line is the name of the file, second line is the base64 string.
	 * @param folderPath	Path to the folder where to  save the file
	 * @throws IOException
	 * @throws IllegalArgumentException	When the base64EncProgram is invalid
	 */
	@Override
	public String fetchSut(String base64EncProgram, Path folderPath) throws IOException, IllegalArgumentException
	{
		int idxSplit = base64EncProgram.indexOf('\n');
		if (idxSplit == -1)
			throw new IllegalArgumentException("Invalid format of sourceBase64 value. No \"\\n\" delimiter found. Expected format: filename\\nbase64");	// should never happen because this gets checked earlier
		
		String filename = base64EncProgram.substring(0, base64EncProgram.indexOf('\n'));
		String base64 = base64EncProgram.substring(base64EncProgram.indexOf('\n') + 1);
		
		Decoder decoder = Base64.getDecoder();
		byte[] decodedProgram = decoder.decode(base64);
		
		Path pathToFile = folderPath.resolve(filename);
		FileOutputStream fileOutStream = new FileOutputStream(pathToFile.toFile());
		fileOutStream.write(decodedProgram, 0, decodedProgram.length);
		fileOutStream.flush();
		fileOutStream.close();
		
		return filename;
	}
	
}
