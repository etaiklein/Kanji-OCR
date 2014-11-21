package strokeLevelClassifiers;

/** 
 * 
 * Knn - A Java implementation of K-Nearest Neighbors for use in training an Optical Character Recognition Model
 * 
 * 
 * Overview:
 * 
 * KNN is a method of classification using features
 * The basic idea is that you compute data with many features (dimensions) as a simpler representation in 2-dimensional space.
 * First you populate the space with training data
 * Then you can classify unknown data by classify it as the mode of the k nearest neighbors in that space
 * 
 * Etai Klein
 * 6/7/14
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import classifierInterfaces.KNNInterface;
import kanjiClasses.*;




public class StrokeKnnHiddenWeights implements KNNInterface{
	
	HashMap<String, Integer> memo = new HashMap<String, Integer>();
	int best = 0;
	String bestWeights = ""; 

	ArrayList<StrokeKanji> vectorSpace;
	int kNearest;

	//correct, incorrect
	public int[] stats = new int[2];

	public HashMap<Character, Integer[]> counter = new HashMap<Character, Integer[]>();

	//constructor initializes new vector space and k-nearest neighbors k value
	public StrokeKnnHiddenWeights(int k){
		//a list of data points
		vectorSpace = new ArrayList<StrokeKanji>();
		//how many data points do we look at to determine a new point's classification?
		kNearest = k;
	}



	/*************************** METHODS ***************************/



	/**
	 * train - 
	 * adds a Kanji to the list of kanjis
	 * @param Kanji k - the kanji to train
	 * 
	 */

	public void train(Kanji k){
		vectorSpace.add((StrokeKanji)k);
	}

	/** 
	 * classify - 
	 * labels a DataPoint based on the labels of the k-nearest neighbors of a vector
	 * @param weightStrokes 
	 * 
	 * @param ArrayList<StrokeKanji> vectorspace (model)
	 * @param int (nearest neighbors)
	 * @param StrokeKanji (Datapoint to be classified)
	 * @return Strokekanji (Datapoint labeled)
	 * 
	 * @pseudocode:
	 * 1. Compute the vector from the datapoint
	 * 2. List the k vectors with the smallest distance to the datapoint
	 * 3. Label the datapoint the modal label of the list
	 * 
	 */

	public StrokeKanji classify(ArrayList<StrokeKanji> vectors, int knear, StrokeKanji kanji, double weightLengths, double weightAngles, double weightDistances, double weightMoves, int weightStrokes){

		//1. count distances

		//for each new vector
		for (StrokeKanji k : vectors){
			//get the new vector's distance value
			k.distance = (int) k.distance(kanji, weightLengths, weightAngles, weightDistances, weightMoves, weightStrokes);
		}


		//2. sort by distance

		//sort the vectors by distance
		Collections.sort(vectors, new Comparator<StrokeKanji>(){

			public int compare(StrokeKanji v1, StrokeKanji v2) {
				//				System.out.println(v1.label + " " + v2.label);
				if (v1.distance > v2.distance){
					return 1;
				}
				else if (v1.distance < v2.distance){
					return -1;
				}
				else {return 0;}
			}

		});


		//3. get the k closest vectors

		//intialize a map to hold the number of times each label appears in the knearest
		HashMap<Character, Integer> labelCount = new HashMap<Character, Integer>();

		// run through the nearest vectors
		for (int i = 0; i< knear; i++){

			//get the current label
			char currentLabel = vectors.get(i).label;
			//			System.out.println("keybuilding:" + currentLabel);

			// if we've seen the label before, increase the value by 1
			if (labelCount.containsKey(currentLabel)){
				labelCount.put(currentLabel, labelCount.get(currentLabel) + 1);
			}
			// if we haven't seen the label before, set the value to 1
			else{labelCount.put(currentLabel, 1);}
		}


		//4. find the most popular label

		//get all the labels in the set
		Set<Character> keys = labelCount.keySet();
		//initialize our max value with the first one
		Character max = keys.iterator().next();

		for (Character key : keys ){
			//			System.out.println("key:" + key);
			if (labelCount.get(key) > labelCount.get(max)){
				max = key;
			}
		}


		//5. track stats


		//				System.out.println("guess: " + max + "actual: " + v1.label);
		if (kanji.label.equals(max)){
			stats[0] += 1; 

			if (counter.containsKey(kanji.label)){
				counter.put(kanji.label, new Integer[]{counter.get(kanji.label)[0] + 1, counter.get(kanji.label)[1]});
			}else{
				counter.put(kanji.label, new Integer[]{1,0});
			}


		} 
		else{ stats[1] += 1;

		if (counter.containsKey(kanji.label)){
			counter.put(kanji.label, new Integer[]{counter.get(kanji.label)[0], counter.get(kanji.label)[1] + 1});
		}else{
			counter.put(kanji.label, new Integer[]{0,1});
		}

		}

		//6. set the label

		//set the label


		Character original = kanji.label;
		kanji.label = max;

//		System.out.println("label: " + max);
//		System.out.println("for label: " + original + " correct: " + counter.get(original)[0] + "false: " + counter.get(original)[1]);
//		System.out.println("for total at label " + kanji.label + " correct: " + stats[0] + "incorrect: " + stats[1] + "percent" + ((double)stats[0] / ((double)stats[1] + (double)stats[1])));

		return kanji;
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
	public int test (File test, File train, String fileType) {
		//training
		for (final File fileEntry : train.listFiles()) {
			if (fileEntry.getName().endsWith(fileType)){
				System.out.println(fileEntry.toString());
				for (StrokeKanji kanji : StrokeKanji.getKanjis(fileEntry, fileType)){
					if (kanji != null){
						train(kanji);
					}
				}
			}
		}
				
		hiddenWeights(test, train, fileType, 3, 0, 0, 0, 0, 0, 0);
		System.out.println("score: " + best + " bestWeights" + bestWeights);
		return best;
	}

	//score: 89 bestWeights 3 1 1 12 3
	//LIVED max: 97 result: 97 knn: 1 lengthsWeighted: 15 anglesWeights: 4 distanceWeighted: 94 movesWeighted: 30
	// LIVED max: 98 result: 98 knn: 1 lengthsWeighted: 11 anglesWeights: 3 distanceWeighted: 117 movesWeighted: 20
	//LIVED max: 83 result: 86 knn: 3 lengthsWeighted: 2 anglesWeights: 1 distanceWeighted: 1 movesWeighted: 3
	public int hiddenWeights(File test, File train, String fileType, int knn, int w1, int w2, int w3, int w4, int w5, int max) {
		if (memo.keySet().contains(" " + knn + " " + w1 + " " + w2 + " " + w3 + " " + w4 + " " + w5)){
//			System.out.println("PRUNED " + knn + " " + w1 + " " + w2 + " " + w3 + " " + w4 + " " + w5);
			return best;
		}
		
		//testing
		stats = new int[2];
		for (final File testFileEntry : test.listFiles()) {
			if (testFileEntry.getName().endsWith(fileType)){
				for (StrokeKanji kanji : StrokeKanji.getKanjis(testFileEntry, fileType)){
					if (kanji != null){
						classify(vectorSpace, kNearest, kanji, w1, w2, w3, w4, w5);
					}
				}
			}
		}
		
		int result = stats[0];
		memo.put(" " + knn + " " + w1 + " " + w2 + " " + w3 + " " + w4 + " " + w5, result);
		if (result > best){
			best = result;
			bestWeights = w1 + " " + w2 + " " + w3 + " " + w4 + " " + w5;
//			System.out.println("score: " + best + " bestWeights " + bestWeights);
		}
		if (result > max){
//			System.out.println("LIVED best: " + best + bestWeights + " max: " + max + " result: " + result +  " knn: " + knn + " lengthsWeighted: " + w1 + " anglesWeights: " + w2 + " distanceWeighted: " + w3 + " movesWeighted: " + w4 );
			hiddenWeights(test, train, fileType, knn, w1, w2, w3 + 1, w4, w5, result);
			hiddenWeights(test, train, fileType, knn, w1, w2, w3, w4 + 1, w5, result);
			hiddenWeights(test, train, fileType, knn, w1, w2 + 1, w3, w4, w5,  result);
			hiddenWeights(test, train, fileType, knn, w1 + 1, w2, w3, w4, w5, result);
			hiddenWeights(test, train, fileType, knn, w1 + 1, w2, w3, w4, w5 + 1, result);
		}else{
//			System.out.println("DIED best: " + best + bestWeights + " max: " + max + " result: " + result + " knn: " + knn + " lengthsWeighted: " + w1 + " anglesWeights: " + w2 + " distanceWeighted: " + w3 + " movesWeighted: " + w4 );
		}
		return best;
	}





}