package strokeLevelClassifiers;
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
import kanjiClasses.StrokeKanji;



public class StrokeGaussian implements GaussianInterface{

	//correct, incorrect
	public int[] zScoreStats = new int[2];

	//correct, incorrect
	public int[] distanceStats = new int[2];

	//keeps track of kanjis we have or have not added to the distribution
	private HashMap<Character, StrokeKanji> kanjiInfoMap = new HashMap<Character, StrokeKanji>();

	//feature Distributions
	int[][] allKanjiLengths = new int[60][30];
	int[][] allKanjiAngles =  new int[60][30];
	int[][] allKanjiMoves =  new int[60][29];

	int currentKanji;
	

	public void updateDistribution(Kanji k){
		allKanjiLengths[currentKanji] = ((StrokeKanji) k).lengths;
		allKanjiAngles[currentKanji] = ((StrokeKanji) k).angles;
		allKanjiMoves[currentKanji++] = ((StrokeKanji) k).moves;
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
	 * 1.build a distribution of each kanji's features
	 * 2.compute their average and standard deviation
	 * 3.score test kanji 
	 * 4.classify using the best score
	 * 
	 */
	public void test(File test, File train, String fileType) {


		// open the last entry's file. Record the number of files.
		int numfiles = 0;
		File fileEntry = train.listFiles()[0];
		while (numfiles < train.listFiles().length){
			if (train.listFiles()[numfiles].getName().endsWith(fileType)){
				fileEntry = train.listFiles()[numfiles++];
			}else{
				numfiles++;
			}
		}
		
		allKanjiLengths = new int[numfiles][30];
		allKanjiAngles = new int[numfiles][30];
		allKanjiMoves = new int[numfiles][29];

		for (StrokeKanji kanji : StrokeKanji.getKanjis(fileEntry, fileType)){
			//if I haven't seen this label before...
			if (!kanjiInfoMap.containsKey(kanji.label)){
				//loop through all instances 
				for (final File innerFileEntry : train.listFiles()) {
					if (innerFileEntry.getName().endsWith(fileType)){
						for (StrokeKanji innerkanji : StrokeKanji.getKanjis(innerFileEntry, fileType)){
							if (innerkanji != null && innerkanji.label.equals(kanji.label)){
								//and update the distribution
								updateDistribution(innerkanji);
							}

						}
					}
				}
				//get the average and standard deviation
				kanji.buildAverage(allKanjiLengths, allKanjiAngles, allKanjiMoves);
				kanji.buildStandardDeviation(allKanjiLengths, allKanjiAngles, allKanjiMoves);
				kanjiInfoMap.put(kanji.label, kanji);		
				//reset the average
				allKanjiLengths = new int[numfiles][30];
				allKanjiAngles = new int[numfiles][30];
				allKanjiMoves = new int[numfiles][29];
				currentKanji = 0;
			}

		}

		//test step

		//use lowestZ for z-score and lowest distance for univariate
		double lowestZ = Integer.MAX_VALUE;
		double lowestZ2 = Integer.MAX_VALUE;

		char guess1 = 0;

		char guess2 = 0;

		for (final File innerFileEntry : test.listFiles()) {
			if (innerFileEntry.getName().endsWith(fileType)){
				for (StrokeKanji kanji : StrokeKanji.getKanjis(innerFileEntry, fileType)){

					//get the kanji with the lowest zscore from the map
					Iterator<Entry<Character, StrokeKanji>> iter = kanjiInfoMap.entrySet().iterator();
					lowestZ = Integer.MAX_VALUE;
					lowestZ2 = Integer.MAX_VALUE;
					while (iter.hasNext()){
						StrokeKanji ki = kanjiInfoMap.get(iter.next().getKey());
						
						//get scores
						double distance = ki.distance(kanji);
						double zScore = ki.getZScore(kanji);

						//keep the best
						if (zScore < lowestZ) { 
							guess1 = ki.label;
							lowestZ = zScore;
						}
						if (distance < lowestZ2) {
							guess2 = ki.label;
							lowestZ2 = distance;
						}
					}

					if (guess1 == kanji.label){
						zScoreStats[0]++;
					}else{zScoreStats[1]++;}

					if (guess2 == kanji.label){
						distanceStats[0]++;
					}else{distanceStats[1]++;}


					System.out.println("guessZsco: " + guess1 + " actual: " + kanji.label+ " correct: " + zScoreStats[0] + 
							" incorrect: " + zScoreStats[1] + " percentage: "  + ((double)zScoreStats[0]/(double)(zScoreStats[0]+zScoreStats[1])));

					System.out.println("guessDist: " + guess2 + " actual: " + kanji.label + " correct: " + distanceStats[0] + 
							" incorrect: " + distanceStats[1] + " percentage: "  + ((double)distanceStats[0]/(double)(distanceStats[0]+distanceStats[1])));

				}		
			}
		}

	}


}