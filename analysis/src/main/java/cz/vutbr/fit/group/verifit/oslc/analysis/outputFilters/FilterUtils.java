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
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static String loadContentsOfFilePathFile(String filePath) throws IOException {
		Path f = Paths.get(filePath);
		byte [] fileContents = Files.readAllBytes(f);
		return new String(fileContents);
	}
	
}
