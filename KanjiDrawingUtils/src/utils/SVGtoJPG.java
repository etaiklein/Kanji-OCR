package utils;
/**
 * SVGtoJPG - transcodes data from SVG to JPG file
 * 
 * I don't own this code
 */

import java.io.*;

import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

public class SVGtoJPG {

	public void transcode(String inputfilename, String outputfilename){
		try{
			// Create a JPEG transcoder
			JPEGTranscoder t = new JPEGTranscoder();

			// Set the transcoding hints.
			t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
					new Float(.8));

			// Create the transcoder input.
			String svgURI = new File(inputfilename).toURI().toString();
			TranscoderInput input = new TranscoderInput(svgURI);

			// Create the transcoder output.
			OutputStream ostream = new FileOutputStream(outputfilename);
			TranscoderOutput output = new TranscoderOutput(ostream);

			// Save the image./
			t.transcode(input, output);

			// Flush and close the stream.
			ostream.flush();
			ostream.close();

		} catch (IOException e) {e.printStackTrace();
		} catch (TranscoderException e) {e.printStackTrace();
		}
	}
}