package kanjiClasses;
/**
 * PixelKanji
 * 
 * Kanji class for pixel-level analysis
 * 
 * @author etaiklein
 * 6/7/14
 */
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import utils.ImageProcessingUtils;


public class PixelKanji extends Kanji{

	public int tokens = 0;
	public double[][] averagePixels;
	public double[][] standardDeviation;

	public PixelKanji(String location){
		source = location;
		label = source.charAt(source.lastIndexOf("/") +1);
	}

	public PixelKanji(Character l){
		label = l;
	}

	public String toString(){

		return "label: " + label + " tokens: " + tokens;
	}

	/**
	 * 
	 * BuildAveragePixels
	 * 
	 * Averages an array of pixels. Can generate an average image as well
	 * 
	 * @param allPixels
	 */
	public void buildAveragePixels(int[][][] allPixels){

		averagePixels = new double[400][400];

		//xbar = sum(x) / N
		for (int i = 0; i < tokens; i++){
			if (allPixels[i] != null){
				for (int row = 0; row < height; row++) {
					for (int col = 0; col < width; col++) {
						averagePixels[col][row] = ((double)(averagePixels[col][row]*i) + (double)allPixels[i][col][row]) / (double)(i+1);
					}
				}
			}
		}
		
//		renderAverage();

	}

	/**
	 * renderAverage
	 * 
	 * Creates an image from an array of pixels
	 * 
	 * @note To be used by the method buildAveragePixels
	 */
	public void renderAverage(){

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = (WritableRaster) image.getData();

		//rasters have 3 extra dimensions
		int scale = 4;
		int[] pixels = new int[width*height*scale];

		int i = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				//compensate for buffer size by scaling the pixels

				for (int j = 0; j < scale; j++){
					pixels[i++] = (int) (averagePixels[col][row]*255);
				}
			}
		}

		raster.setPixels(0,0,width,height,pixels);
		image.setData(raster);
		ImageProcessingUtils.createImage(image, "NewAverageKanji/" + label + "average.png");

	}
	/**
	 * buildStandardDeviation
	 * 
	 * gets an array of standard deviations per pixel for an array of pixels.
	 * 
	 * @param allPixels
	 * 
	 * @note To be used after the method buildAveragePixels is called
	 */
	public void buildStandardDeviation(int[][][] allPixels){

		standardDeviation = new double[400][400];

		//stdev = sqrt ( sum((x - xbar)^2)/N-1)
		for (int i = 0; i < tokens; i++){
			if (allPixels[i] != null){
				for (int row = 0; row < height; row++) {
					for (int col = 0; col < width; col++) {
						//summing squares
						standardDeviation[row][col] += ((allPixels[i][row][col] - averagePixels[row][col])*(allPixels[i][row][col] - averagePixels[row][col]));
					}
				}
				i++;
			}
		}
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				// dividing and sqrt
				standardDeviation[row][col] = Math.sqrt(standardDeviation[row][col]/(tokens - 1));
			}
		}

	}
	/**
	 * getZScore
	 * Computes a zScore for a set of pixels
	 * 
	 * @param pixels the pixels to compute
	 * @return double zscore the score
	 */
	public double getZScore(int[][] pixels){
		double z = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				z+= Math.abs((double) pixels[row][col] - (double) averagePixels[row][col])/((double) standardDeviation[row][col] + 1);
			}
		}
		return z;
	}

	/**
	 * distance
	 * @summary Computes a score based on how different one kanji is from this kanji
	 * 
	 * @param otherKanjiPixels - The pixels of the Kanji to Compare to
	 * @return sum - the difference score
	 * 
	 * @pseudocode
	 * 1. loop through each kanji's pixel array
	 * 2. sum the difference between pixels
	 * 
	 */
	public int distance(int[][] otherKanjiPixels){

		double sum = 0;
		
		for (int row = 0; row < averagePixels.length; row++) {
			for (int col = 0; col < averagePixels[0].length; col++) {	
				//sum the difference between each pixel
				sum += Math.abs((double) averagePixels[row][col] - (double)otherKanjiPixels[row][col]);
			}
		}

		return (int)sum;
	}
	/**
	 * toPixels
	 * turns an image into a pixel array
	 *  
	 * @param String fileType - the methods for rendering pixels for .png and .jpg files are different
	 * @return in[][] pixels
	 */
	public int[][] toPixels(String fileType){
		
		if (fileType == ".png"){
			return toPixels();
		}
		
		if (fileType == ".jpg"){
			return toPixelsFromData();
		}
		
		return null;
		
	}
	/**
	 * getdata
	 * gets a data buffer from an image file. functions like get pixels
	 *  
	 * @return DataBuffer
	 */
	public DataBuffer getdata() {
		try {
			return ImageIO.read(new File(source)).getRaster().getDataBuffer();				
		} catch (IOException e) {e.printStackTrace();}
		return null;
	}
	
	/**
	 * toPixelsFromData
	 * gets a data buffer from an image file and gets pixels from the buffer
	 *  
	 * @return int[][] pixels
	 */
	public int[][] toPixelsFromData(){
		DataBuffer vecData = getdata();		
		
		int[][] pixels = new int[400][400];
		
		//VectorData contains pixel information in extra dimensions
		for (int y = 0; y < vecData.getSize(); y++) {
			if (y % 3 == 0){
			pixels[y/1200][(y/3) % 400] = vecData.getElem(y)/255;
			}
		}
		return pixels;
	}
	
	
}



