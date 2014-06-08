package kanjiClasses;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import utils.ImageProcessingUtils;


public class Kanji {

	public Character label;
	public String source;
	public int distance;
	public int height = 400;
	public int width = 400;
	
	
	public int[][] toPixels() {
		try {
			return new ImageProcessingUtils().bufToPixels(ImageIO.read(new File(source)));
		} catch (IOException e) {e.printStackTrace();}
		return null;
	}
}
