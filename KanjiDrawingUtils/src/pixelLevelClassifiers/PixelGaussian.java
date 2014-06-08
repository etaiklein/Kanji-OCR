package pixelLevelClassifiers;
/**
 * PixelGaussian
 * 
 * Uses pixel-based statistical processing for Classification
 * 
 * The basic idea is that for each kanji, one can load all (60) versions of it and 
 * get a standard deviation and average value for each pixel
 * 
 * Two types of classification:
 * 
 * 1. Zscore Classifier: You can classify an unknown kanji as the label with the lowest z-score 
 * based on the average and standard deviation
 * 
 * 2. Univariate Classifier: You can classify an unknown kanji based on the distance of its pixels
 * from the average of all kanji tokens of each type
 * 
 * Etai Klein
 * 6/7/14
 * 
 */

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import classifierInterfaces.GaussianInterface;

import kanjiClasses.Kanji;
import kanjiClasses.PixelKanji;




public class PixelGaussian implements GaussianInterface{

	//correct, incorrect
	public int[] zScoreStats = new int[2];

	//correct, incorrect
	public int[] distanceStats = new int[2];

	//map keeps track of the average kanji for each type
	private HashMap<Character, PixelKanji> kanjiInfoMap = new HashMap<Character, PixelKanji>();

	//The distirbution- intialized to 60 tokens but changed later
	int[][][] allKanjiPixels = new int[60][400][400];

	//iterator for allKanjiPixels
	int currentKanji;

	private String type; //.png or .jpg?

	/**
	 * updateDistribution
	 * adds the current kanji to the distribution
	 * 
	 * @param Kanji - the kanji to add to the distribution
	 */
	public void updateDistribution(Kanji k){
		System.out.println("adding..." + k.source);
		allKanjiPixels[currentKanji++] = ((PixelKanji) k).toPixels(type);
	}

	@Override
	/**
	 * test
	 * forms a distribution from all training kanji and classifies all test kanji
	 * 
	 * @param test - the kanji to classify
	 * @param train - the kanji to use in the distribution
	 * @param fileType
	 * 
	 * @pseudocode:
	 * 1.build a distribution of each kanji
	 * 2.compute its average and standard deviation
	 * 3.score test kanji 
	 * 4.classify using the best score
	 * 
	 */
	public void test(File test, File train, String fileType) {

		type = fileType;

		//for each image
		for (final File fileEntry : train.listFiles()) {
			if (fileEntry.getName().endsWith(fileType)){

				//if the kanji is not in the hashmap, run through each kanji in the folder 
				if (!kanjiInfoMap.containsKey(fileEntry.getName().charAt(0))){
					PixelKanji ki = new PixelKanji(fileEntry.getName().charAt(0));

					for (final File innerFileEntry : train.listFiles()) {
						if (fileEntry.getName().endsWith(fileType) && innerFileEntry.getName().charAt(0) == ki.label){
							//build a distribution using all pixels of the same kanji
							currentKanji = ki.tokens;
							updateDistribution(new PixelKanji(innerFileEntry.getAbsolutePath()));
							ki.tokens++;
						}
					}
					//get the average and standard deviation
					ki.buildAveragePixels(allKanjiPixels);
					ki.renderAverage();
					ki.buildStandardDeviation(allKanjiPixels);
					kanjiInfoMap.put(ki.label, ki);		
					//reset the average
					allKanjiPixels = new int[60][400][400];

				}
			}
		}

		//test step

		//for each kanji in the folder
		for (final File fileEntry : test.listFiles()) {
			if (fileEntry.getName().endsWith(fileType)){
				//use lowestZ for z-score and highestz for distance (er... overlap)
				double lowestZ = Integer.MAX_VALUE;
				double closest = Integer.MAX_VALUE;

				char guess1 = 0;

				char guess2 = 0;

				//get the kanji with the lowest zscore from the map
				Iterator<Entry<Character, PixelKanji>> iter = kanjiInfoMap.entrySet().iterator();
				lowestZ = Integer.MAX_VALUE;
				closest = Integer.MAX_VALUE;
				while (iter.hasNext()){
					PixelKanji ki = kanjiInfoMap.get(iter.next().getKey());
					PixelKanji testkanji = new PixelKanji(fileEntry.getAbsolutePath());
					//get the score
					double distance = ki.distance(testkanji.toPixels(fileType));
					double zScore = ki.getZScore(testkanji.toPixels(fileType));
					//update the best score
					if (zScore < lowestZ) { 
						guess1 = ki.label;
						lowestZ = zScore;
					}
					if (distance < closest) {
						guess2 = ki.label;
						closest = distance;
					}
				}
				//track stats
				if (guess1 == fileEntry.getName().charAt(0)){
					zScoreStats[0]++;
				}else{zScoreStats[1]++;}

				if (guess2 == fileEntry.getName().charAt(0)){
					distanceStats[0]++;
				}else{distanceStats[1]++;}

				//print
				System.out.println("guessZsco: " + guess1 + " actual: " + fileEntry.getName().charAt(0) + " correct: " + zScoreStats[0] + 
						" incorrect: " + zScoreStats[1] + " percentage: "  + ((double)zScoreStats[0]/(double)(zScoreStats[0]+zScoreStats[1])));

				System.out.println("guessDist: " + guess2 + " actual: " + fileEntry.getName().charAt(0) + " correct: " + distanceStats[0] + 
						" incorrect: " + distanceStats[1] + " percentage: "  + ((double)distanceStats[0]/(double)(distanceStats[0]+distanceStats[1])));

			}		

		}
	}

}