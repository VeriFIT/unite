package verifit.analysis;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.lyo.oslc4j.provider.jena.JenaModelHelper;
import org.eclipse.lyo.oslc4j.provider.jena.LyoJenaModelException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public class utils {
	
	public static String base64Encode(byte [] bytes)
	{
		Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(bytes);
	}
	
	public static byte [] base64Decode(String base64Str)
	{
		Decoder decoder = Base64.getDecoder();
		return decoder.decode(base64Str);
	}

	/**
	 * Parses resources <T> from an XML file and returns them as an array.
	 * @param pathToFile	Path to the XML file to load from
	 * @param clazz	<T>.class of the resource to be parsed from the XML file
	 * @return An array of <T> resources
	 * @throws FileNotFoundException Error accessing the XML file
	 * @throws LyoJenaModelException Error parsing the XML file
	 */
	public static <T> T[] parseResourcesFromXmlFile(String pathToFile, Class<T> clazz) throws FileNotFoundException, LyoJenaModelException
	{
		InputStream inStream = new FileInputStream(new File(pathToFile));
		Model model = ModelFactory.createDefaultModel();
		model.read(inStream, null);
		return JenaModelHelper.unmarshal(model, clazz);
	}
}
