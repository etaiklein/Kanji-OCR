package pixelLevelClassifiers;
/**
 * PixelKnn - A Java implementation of K-Nearest Neighbors for use in training an Optical Character Recognition Model
 * 
 * Uses pixel-based K-Nearest-Neighbors for Classification
 * 
 * KNN is a method of classification using features
 * The basic idea is that you compute data with many features (dimensions) as a simpler representation in 2-dimensional space.
 * First you populate the space with training data
 * Then you can classify unknown data by classify it as the mode of the k nearest neighbors in that space
 * 
 * 
 */


import java.awt.image.*;
import java.awt.*;
import java.io.*;

import classifierInterfaces.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import kanjiClasses.*;


public class PixelKnn implements KNNInterface{

	int maxDistance = Integer.MAX_VALUE;
	int current;
	int kNearest;
	PixelKanji[] VectorSpace;

	//correct, incorrect
	public int[] stats = new int[2];

	public HashMap<Character, Integer[]> counter = new HashMap<Character, Integer[]>();

	//constructor initializes new vector space and k-nearest neighbors k value
	public PixelKnn(int k){
		//how many data points do we look at to determine a new point's classification?
		kNearest = k;
	}


	/*************************** METHODS ***************************/

	/**
	 * toBufferedImage - 
	 * Converts an Image to a Buffered Image
	 * @param Image img
	 * @return BufferedImage bimage
	 * 
	 */

	//source http://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
	public static BufferedImage toBufferedImage(Image img)
	{

		if (img instanceof BufferedImage)
		{
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();


		//Return the buffered image
		return bimage;
	}


	/** 
	 * distance - 
	 * finds the distance between two kanji
	 * 
	 * @param PixelKanji unknownKanji, PixelKanji testKanji
	 * @return double distance
	 * 
	 * @pseudocode:
	 * 1. check image size
	 * 2. Add the squared difference for each pixel
	 * 3. Reduce by 1/numpixels
	 * 
	 */

	public int distance(PixelKanji unknownKanji, PixelKanji testKanji){

		int sum = 0;
		int row = 0;
		int col = 0;

		//for each pixel, add their squared difference
		for (row = 0; row < unknownKanji.toPixels().length; row++) {
			for (col = 0; col < unknownKanji.toPixels()[0].length; col++) {
				sum -= Math.abs(unknownKanji.toPixels()[row][col] - testKanji.toPixels()[row][col]);
			}
		}

		return sum;
	}

	/** 
	 * fastdistance - 
	 * finds the distance between two kanjis using databuffer.
	 * 
	 * @param PixelKanji unknownKanji, PixelKanji testKanji
	 * @return double distance
	 * 
	 * @pseudocode:
	 * 1. check image size
	 * 2. Add the squared difference for each pixel
	 * 3. Reduce by 1/numpixels
	 * 
	 */

	public int fastDistance (PixelKanji unknownKanji, PixelKanji testKanji){

		int sum = 0;

		DataBuffer unknownData = unknownKanji.getdata();
		DataBuffer pixelKanjiData = testKanji.getdata();

		for (int y = 0; y < pixelKanjiData.getSize(); y++) {
			//color shouldn't matter since they're black and white
			if (unknownData.getElem(y) != pixelKanjiData.getElem(y)){
				sum+= Math.abs(unknownData.getElem(y) - pixelKanjiData.getElem(y));
			}

		}

		return sum;

	}

	/** 
	 * classify - 
	 * labels a DataPoint based on the labels of the k-nearest neighbors of a vector
	 * 
	 * @param PixelKanji[] VectorSpace vs (model) 
	 * @param int k (nearest neighbors)
	 * @param PixelKanji vec (Datapoint to be classified)
	 * @return PixelKanji classification
	 * 
	 * @pseudocode:
	 * 
	 * 1. Compute the kanji's distance from the datapoint
	 * 2. List the k kanjis with the smallest distance to the datapoint
	 * 3. Label the datapoint the modal label of the list 
	 */

	public PixelKanji classify(PixelKanji[] vectorSpace, int knear, PixelKanji unknownKanji){

		System.out.println("classifying.... " + unknownKanji.label);

		//process

		for (int i = 0; i < VectorSpace.length; i++){
			if (VectorSpace[i] != null){

				//compute the distance from the test kanji
				VectorSpace[i].distance = fastDistance(unknownKanji, VectorSpace[i]);

				//sort the vectors by distance
				Arrays.sort(this.VectorSpace, new Comparator<PixelKanji>(){

					public int compare(PixelKanji v1, PixelKanji v2) {

						if (v1 == null || v2 == null){
							return 0;
						}

						if (v1.distance > v2.distance){
							return 1;
						}
						else if (v1.distance < v2.distance){
							return -1;
						}
						else {return 0;}
					}
				});			


			}
		}

		//count the number of each label for the first k distance vectors

		//intiialize a map to hold the number of times each label appears in the knearest
		HashMap<String, Integer> labelCount = new HashMap<String, Integer>();


		// run through the nearest vectors
		for (int i = 0; i< knear; i++){


			//get the current label
			String currentLabel = ""+ VectorSpace[i].label;

			// if we've seen the label before, increase the value by 1
			if (labelCount.containsKey(currentLabel)){
				labelCount.put(currentLabel, labelCount.get(currentLabel) + 1);
			}
			// if we haven't seen the label before, set the value to 1
			else{labelCount.put(currentLabel, 1);}
		}


		//find the most popular label

		//get all the labels in the set
		Set<String> keys = labelCount.keySet();
		//initialize our max value with the first one
		String max = keys.iterator().next();

		for (String key : keys ){
			if (labelCount.get(key) > labelCount.get(max)){
				max = key;
			}
		}


		//track stats
		System.out.println("guess: " + max + "actual: " + unknownKanji.label);
		if (unknownKanji.label == (max.charAt(0))){stats[0] += 1; System.out.println("correct");} else{ stats[1] += 1;System.out.println("nope");}

		//set the label
		unknownKanji.label = max.charAt(0);
		System.out.println("correct: " + stats[0] + "incorrect: " + stats[1] + "percent" + ((double)stats[0] / ((double)stats[1] + (double)stats[0])));

		return unknownKanji;

	}


	/** 
	 * train
	 * add a new kanji to the list
	 * 
	 * @param Kanji k
	 */
	@Override
	public void train(Kanji k) {
		VectorSpace[current] = ((PixelKanji)k);
	}


	@Override
	/**
	 * test
	 * scores training kanji and classifies all test kanji
	 * 
	 * @param test - the kanji to classify
	 * @param train - the kanji to use in the distribution
	 * @param fileType
	 * 
	 * @pseudocode:
	 * 1.add each kanji to the vectorspace
	 * 2.classify each new kanji
	 * 
	 */
	public int test(File test, File train, String fileType){

		//compute the size of the vectorspace
		int i = 0;
		for (final File fileEntry : train.listFiles()) {
			if (fileEntry.getName().endsWith(fileType)){
				i++;
			}
		}

		VectorSpace = new PixelKanji[i];
		current = 0;

		//train all test kanji
		for (final File fileEntry : train.listFiles()) {
			if (fileEntry.getName().endsWith(fileType)){
				train(new PixelKanji(fileEntry.getAbsolutePath()));
				current++;
			}
		}

		PixelKanji ki = new PixelKanji(test.listFiles()[0].getAbsolutePath());
		System.out.println(ki.label);

		//testing

		for (final File fileEntry : test.listFiles()) {
			if (fileEntry.getName().endsWith(fileType)){
				classify(VectorSpace, kNearest, new PixelKanji(fileEntry.getAbsolutePath()));
			}
		}	

		System.out.println("correct: " + stats[0] + " incorrect: " + stats[1] + " percent" + ((double)stats[0] / ((double)stats[1] + (double)stats[0])));
		return (int)stats[0];
	}
	
}