package verifit.compilation.automationPlans.sutFetcher;

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
