package verifit.compilation.automationPlans.sutFetcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import verifit.compilation.VeriFitCompilationManager;

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
			String fileName = VeriFitCompilationManager.getResourceIdFromUri(sourceUrl); // gets the last part of the URL
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
