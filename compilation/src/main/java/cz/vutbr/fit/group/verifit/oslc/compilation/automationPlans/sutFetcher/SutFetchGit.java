/*
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package verifit.compilation.automationPlans.sutFetcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

import verifit.compilation.VeriFitCompilationManager;

public class SutFetchGit implements SutFetcher {

	/**
	 * Clone a public git repository (no usrname and passwd) TODO
	 * Source - https://onecompiler.com/posts/3sqk5x3td/how-to-clone-a-git-repository-programmatically-using-java
	 * @param url	URL of the repository
	 * @param folderPath	Path to the folder to clone into
	 * @return Name of the new directory / file
	 * @throws IOException 
	 */
	@Override
	public String fetchSut(String url, Path folderPath) throws IOException {
		try {
		    Git.cloneRepository()
		        .setURI(url)
		        .setDirectory(folderPath.toFile())
		        .call();
		    
		    return VeriFitCompilationManager.getResourceIdFromUri(new URI (url)); // gets the last part of the URL
		
		} catch (GitAPIException | JGitInternalException | URISyntaxException e) {
			throw new IOException("Git clone failed: " + e.getMessage());
		}
	}
}
