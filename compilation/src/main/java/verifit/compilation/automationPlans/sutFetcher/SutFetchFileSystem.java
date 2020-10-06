package verifit.compilation.automationPlans.sutFetcher;

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
