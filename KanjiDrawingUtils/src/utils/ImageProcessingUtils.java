package utils;
/**
 * 
 * ImageProcessingUtils.java
 * 
 * This class contains image processing methods.
 * 
 * Methods:
 * 
 * GetPixels (deprecated) - Returns pixels from images collected from the Data Collection App
 * 
 * byteToPixels - Gets Pixels from a Byte Array
 * bufToPixels - Gets Pixels from a BufferedImage
 * byteToScaledImage - Stretches the content of a 400x400 image to fullscreen
 * CreateImage - Writes an image from a BufferedImage to the computer
 * ImportKanji - Reads a Byte Array created from an Android Screenshot and processes it into a full 400x400 scaled image
 * 
 *
 * Etai Klein 
 * 6/7/14
 * 
 **/

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import kanjiClasses.StrokeKanji;

public class ImageProcessingUtils {

	/**
	 * byteToScaledPixels
	 * 
	 * @summary returns a 400x400 pixel array from a byte array
	 * 
	 * @param byteArray
	 * @param subImageX The leftmost pixel
	 * @param subImageY The top pixel
	 * @param subImageWidth 
	 * @param subImageHeight
	 * @return pixel array
	 * @throws IOException
	 * 
	 * @pseudocode
	 * 1. Convert into a buffered image
	 * 2. Scale the image
	 * 3. Redraw the buffered image
	 * 4. Loop through pixels
	 * 
	 */


	public int[][] byteToScaledPixels(byte[] byteArray, int subImageX, int subImageY, int subImageWidth, int subImageHeight) throws IOException {

		//Step 1. convert byte array into BufferedImage
		InputStream in = new ByteArrayInputStream(byteArray);
		BufferedImage bu = ImageIO.read(in);

		//Step 2. scale to 400x400
		bu = bu.getSubimage(subImageX, subImageY, subImageWidth, subImageHeight);
		Image img = bu.getScaledInstance(400, 400, Image.SCALE_SMOOTH);

		//Step 3. Redraw the image 

		// clear the buffer
		bu = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bu.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		//Step 4. Loop through the pixels 

		return bufToPixels(bu);

	}


	/** 
	 * bufToPixels
	 * 
	 * @summary loop through pixels of a buffered image
	 * Overloaded to allow image offsets
	 * 
	 * @param bu a buffered image
	 * @param leftoffset
	 * @param topoffset
	 * @param rightoffset
	 * @param bottomoffset
	 * @return a pixel array
	 * 
	 * @pseudocode loop through pixels
	 * 
	 */

	public int[][]bufToPixels(BufferedImage bu){
		return bufToPixels(bu, 0, 0, 0, 0);
	}

	int[][]bufToPixels(BufferedImage bu, int leftoffset, int topoffset, int rightoffset, int bottomoffset){
	
		
		//prep pixels for iteration
		int[][] pixels = new int[bu.getWidth()][bu.getHeight()];
		//the screenshot has a display on top so we provide a pixel buffer.
		for (int row = topoffset; row < bu.getHeight() - bottomoffset; row++){
			for (int col = leftoffset; col < bu.getWidth() - rightoffset; col++){
				pixels[col][row] = bu.getRGB(col, row) != 0 ? 1 : 0;	
			}
		}	

		return pixels;

	}

	/**
	 * byteToScaledImage
	 * 
	 * @summary Scales an image to fit a 400x400 window exactly 
	 * 
	 * @param byteArray - the original image bytearray
	 * @param name - Where to write the image
	 * @param leftoffset
	 * @param topoffset
	 * @param rightoffset
	 * @param bottomoffset
	 * @return
	 * @throws IOException
	 * 
	 * @pseudocode
	 * 1.Convert to bufferedImage
	 * 2.Loop through to keep track of the extreme left, right, top and bottom pixels
	 * 3.Redraw the image
	 * 
	 * @Discussion - the Android screenshots has a display on top (at about 110 pixels) so I provide a pixel buffer option
	 * 
	 */

	private int[][] byteToScaledImage(byte[] byteArray, String name, int leftoffset, int topoffset, int rightoffset, int bottomoffset) throws IOException {


		//Step 1. convert byte array back to BufferedImage
		InputStream in = new ByteArrayInputStream(byteArray);
		BufferedImage bu = ImageIO.read(in);

		//prep pixels for iteration
		int[][] pixels = new int[bu.getWidth()][bu.getHeight()];

		// get the box containing the extreme pixels
		int left = bu.getWidth();
		int right = 0;
		int top = bu.getHeight();
		int bottom = 0;

		//Step 2. Loop though the pixels ( modified bufToPixels)
		for (int row = leftoffset; row < bu.getHeight() - rightoffset; row++){
			for (int col = topoffset; col < bu.getWidth() - bottomoffset; col++){
				pixels[col][row] = bu.getRGB(col, row) != 0 ? 1 : 0;

				//keep track of extreme pixels 

				if (pixels[col][row] != 0){

					if (left > col){
						left = col;
					}

					if (right < col){
						right = col;
					}

					if (top > row){
						top = row;
					}

					if (bottom < row){
						bottom = row;
					}

				}
			}
		}	

		//Step 3. Redraw the image		
		bu = bu.getSubimage(left, top, right - left, bottom - top);
		createImage(bu, name);


		return pixels;
	}

	//	public void byteToJPG(byte[] image, String name) throws IOException{
	//		// convert byte array back to BufferedImage
	//		InputStream in = new ByteArrayInputStream(image);
	//		BufferedImage bu = ImageIO.read(in);
	//		bu = bu.getSubimage(0, 110, 748, 748);
	//
	//		createImage(bu, name);
	//		
	//	}

	/**
	 * Create Image
	 * 
	 * @summary writes a buffered image to a file
	 * 
	 * @param bu - the buffered image
	 * @param name - the file name/location
	 *
	 */

	public static void createImage(BufferedImage bu, String name){
		// Create a buffered image with transparency
		Image img = bu.getScaledInstance(400, 400, Image.SCALE_SMOOTH);

		//clear image for redraw
		bu = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bu.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		try {
			//write the image
			ImageIO.write(bu, "png", new File(
					name));
		} catch (IOException e) {e.printStackTrace();}

	}


	/**
	 * textToStrokeKanji
	 * 
	 * @summary - Reads the Android Created .txt files to create Stroke Kanji Objects
	 * 
	 * @param input - the folder containing the .txt files
	 * @return kanjiList - An ArrayList of StrokeKanjis
	 * @throws IOException
	 * 
	 * @pseudocode
	 * 1. loop through the files
	 * 2. Read each line to grab the kanji name and the associated kanji's Strokes
	 * 3. Process the strokes array and retrun the kanjiList
	 * 
	 */

	public ArrayList<StrokeKanji> textToStrokeKanji(String input) {
		ArrayList<StrokeKanji> kanjilist = new ArrayList<StrokeKanji>();
		try{
			File txtFile = new File(input);
			String line;
			BufferedReader br = new BufferedReader(new FileReader(txtFile));

			//Step 2. read through each line to grab the relevant information
			while ((line = br.readLine()) != null) {
				String imageName = null;
				String strokes = null;
				int[] strokeArray = null;
				StrokeKanji sk = new StrokeKanji();

				//get the kanji's name and the length of its byte array
				imageName = line.split(Pattern.quote(" ["))[0];
				//get all the bytes associated with the kanji
				strokes = line.split(Pattern.quote(" ["))[1];
				strokes = strokes.substring(0, strokes.length()-1);

				strokeArray = new int[120];
				int i = 0;

				//parse each byte and add it to the byte array
				for (String s : strokes.split(Pattern.quote(", "))){
					strokeArray[i++] = Integer.parseInt(s);
				}
				sk = new StrokeKanji(strokeArray);
				sk.label = imageName.charAt(0);
				//Step 3. Process strokes to get angles and lengths

				for ( i = 0; i < 30.; i++){
						int startx = sk.reducedStrokes[i*4];
						int starty = sk.reducedStrokes[i*4 + 1];
						int endx = sk.reducedStrokes[i*4 + 2];
						int endy = sk.reducedStrokes[i*4 + 3];

						//	System.out.println("stroke: " + s + "startx: " + startx + "starty: " + starty + "endx: " + endx + "endy: " + endy);

						int deltaX = startx - endx;
						int deltaY = starty - endy;
						double strokelength = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
						double angleInDegrees = Math.atan2(deltaY, deltaX) * 180 / Math.PI;
						//System.out.println("deltax: " + deltaX + " deltay: " + deltaY + " angle: " + angleInDegrees + " length: " + length);

						sk.angles[i] = (int) angleInDegrees;
						sk.lengths[i] = (int) strokelength;

						if (i!= 0){
							int lastendx = sk.reducedStrokes[(i-1)*4 + 2];
							int lastendy = sk.reducedStrokes[(i-1)*4 + 3];
							int strokeDeltaX = lastendx - startx;
							int strokeDeltaY = lastendy - starty;
							sk.moves[i-1] = (int) (Math.atan2(strokeDeltaY, strokeDeltaX) * 180 / Math.PI);
						}
					}
				
				kanjilist.add(sk);
			}
			br.close();

		}	



		catch (IOException e){e.printStackTrace();}
		return kanjilist;
	}

	/**
	 * textToPNG
	 * 
	 * @summary - Reads the Android Created .txt files to create scaled screenshots
	 * 
	 * @param input - the folder containing the .txt files
	 * @param output - the folder where you want to place the images
	 * @throws IOException
	 * 
	 * @pseudocode
	 * 1. loop through the files
	 * 2. Read each line to grab the kanji name and the associated image's byte array
	 * 3. Process the byte array and create the image
	 * 
	 */

	public void textToPNG(String input, String output) {

		try{
			//Step 1. loop through the input files
			File myfile = new File(input);
			for (final File txtFile : myfile.listFiles()){
				if (txtFile.getName().endsWith(".txt")){

					String line;
					BufferedReader br = new BufferedReader(new FileReader(txtFile));

					//Step 2. read through each line to grab the relevant information
					while ((line = br.readLine()) != null) {
						String imageName = null;
						String bytes = null;
						byte[] byteArray = null;

						//get the kanji's name and the length of its byte array
						int length = line.split(Pattern.quote(" [")).length;
						imageName = line.split(Pattern.quote(" ["))[0];


						//if the image wasn't blank...
						if (length > 1){

							//get all the bytes associated with the kanji
							bytes = line.split(Pattern.quote(" ["))[2];
							bytes = bytes.substring(0, bytes.length()-1);

							byteArray = new byte[bytes.split(Pattern.quote(", ")).length];
							int i = 0;

							//parse each byte and add it to the byte array
							for (String b : bytes.split(Pattern.quote(", "))){
								byteArray[i++] = Byte.parseByte(b);
							}

							//Step 3. Process the image and save it.
							String name = output + "/" + imageName + txtFile.getName().substring(0, txtFile.getName().lastIndexOf(".")) + ".png";
							byteToScaledImage(byteArray,name, 112, 2, 1, 1);
						}
					}
					br.close();
				}
			}
		}
		catch (IOException e){

		}
	}

	//creates all the image files
	static void run(String inputLocation, String OutputLocation){
		new ImageProcessingUtils().textToPNG(inputLocation, OutputLocation);
	}

	public static void main(String[] argv){
		run(new File("CollectedKanjiInput").getAbsolutePath(),new File("CollectedKanjiOutputTest").getAbsolutePath());
	}

}