package verifit.analysis;

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
}
