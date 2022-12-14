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

import java.nio.file.Path;

public interface SutFetcher {
	
	/**
	 * Will fetch a file/directory from 
	 * @param source	Where to get the SUT from
	 * @param destination	Where to place the SUT
	 * @return	Name of the new file/directory
	 */
	public String fetchSut(String source, Path destination) throws Exception;
}
