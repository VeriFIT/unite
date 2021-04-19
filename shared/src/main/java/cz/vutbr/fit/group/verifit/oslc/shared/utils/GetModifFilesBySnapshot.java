/* 
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cz.vutbr.fit.group.verifit.oslc.shared.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.lyo.oslc.domains.auto.Contribution;
import org.eclipse.lyo.oslc4j.core.model.Link;

import cz.vutbr.fit.group.verifit.oslc.shared.OslcValues;

public class GetModifFilesBySnapshot {
	
	private File dir;	// directory to take the snapshot in
	private Map<String, Long> beforeSnapshot; // A map of (key: file_path; value: file_modification_time)
	private Map<String, Long> afterSnapshot; // A map of (key: file_path; value: file_modification_time)
	private String fileRegex;	// regex used for the first snapshot, will be reused for the second
	
	public GetModifFilesBySnapshot(File dir)
	{
		this.dir = dir;
	}

	/**
	 * Takes the first snapshot to be compared later to a second one.
	 * @param fileRegex 	Only files matching this regex will be snapshoted
	 */
	public void takeBeforeSnapshot(String fileRegex)
	{
		this.fileRegex = fileRegex;
		this.beforeSnapshot = takeDirSnapshot(this.dir, fileRegex);
	}
	
	/**
	 * Takes the second snapshot to be compared with the first one.
	 */
	public void takeAfterSnapshot()
	{
		this.afterSnapshot = takeDirSnapshot(this.dir, this.fileRegex);
	}
	
	public List<File> getModifFiles()
	{
		List<File> modifFiles = new ArrayList<File>();
		
		// check all files that matched the regex in the second snapshot, and decide whether to include it in the output
	    for (Map.Entry<String,Long> newFile : afterSnapshot.entrySet())
	    {
	    	if (beforeSnapshot.containsKey(newFile.getKey()))
	    	{ // if the file existed before the analysis, check if his modif time changed
	    		Long oldTimestamp = beforeSnapshot.get(newFile.getKey());
	    		if (newFile.getValue() <= oldTimestamp)
	    		{ // the file was not modified
	    			continue;
	    		}
	    	}

	    	// the file did not exist before analysis OR was modified --> add it as contribution to the AutoResult
			File currFile = new File(newFile.getKey());
			modifFiles.add(currFile);
		}
	    
	    return modifFiles;
	}

	/**
	 * Takes a snapshot of all file names and their modification times
	 * @param folderPath	Directory to take a snapshot of 
	 * @param fileRegex 	Only files matching this regex will be snapshoted
	 * @return	A map of (key: file_path; value: file_modification_time)
	 */
	private Map<String, Long> takeDirSnapshot(File dir, String fileRegex)
	{
		Map<String, Long> files = new HashMap<String, Long>();
		
		// get all files in a directory recursively and loop over them
		Iterator<File> it = FileUtils.iterateFiles(dir, null, true);
		while (it.hasNext())
		{
            File currFile = it.next();
        	// TODO do I need read permissions to check the modification date?
            String path = currFile.getPath();
        	if (Pattern.matches(fileRegex, path))  
        	{
	        	Long timestamp = currFile.lastModified();
	        	files.put(path, timestamp);
        	}
		}
		
		return files;
	}

}
