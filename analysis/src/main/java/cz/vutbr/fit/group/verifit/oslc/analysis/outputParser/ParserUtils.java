package cz.vutbr.fit.group.verifit.oslc.analysis.outputParser;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import cz.vutbr.fit.group.verifit.oslc.shared.utils.Utils;

public class ParserUtils {


	/**
	 * Loads the entire contents of a file identified by its contribution fileURI property.
	 * @param fileURI
	 * @return
	 * @throws IOException
	 */
	public static String loadContentsOfFileUriFile(String fileURI) throws IOException {
		Path f = getPathFromFileUri(fileURI);
		byte [] fileContents = Files.readAllBytes(f);
		return new String(fileContents);
	}
	
	/**
	 * Returns a Path to a file identified by its contribution fileURI property.
	 * @param fileURI
	 * @return
	 */
	public static Path getPathFromFileUri(String fileURI)  {
		String filePath = Utils.decodeFilePathFromId(Utils.getResourceIdFromUri(fileURI));
		return FileSystems.getDefault().getPath(filePath);
	}
	
}
