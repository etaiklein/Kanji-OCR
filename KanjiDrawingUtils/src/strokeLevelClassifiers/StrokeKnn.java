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


public class StrokeKnn implements KNNInterface{


	ArrayList<StrokeKanji> vectorSpace;
	int kNearest;

	//correct, incorrect
	public int[] stats = new int[2];

	public HashMap<Character, Integer[]> counter = new HashMap<Character, Integer[]>();

	//constructor initializes new vector space and k-nearest neighbors k value
	public StrokeKnn(int k){
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

	public StrokeKanji classify(ArrayList<StrokeKanji> vectors, int knear, StrokeKanji kanji){

		//1. count distances

		//for each new vector
		for (StrokeKanji k : vectors){
			//get the new vector's distance value
			k.distance = (int) k.distance(kanji, 0, 0, 1, 0, 0);
		}


		//2. sort by distance

		//sort the vectors by distance
		Collections.sort(vectors, new Comparator<StrokeKanji>(){

			public int compare(StrokeKanji v1, StrokeKanji v2) {
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
			if (labelCount.get(key) > labelCount.get(max)){
				max = key;
			}
		}


		//5. track stats


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

		Character original = kanji.label;
		kanji.label = max;

//		System.out.println("label: " + max);
//		System.out.println("for label: " + original + " correct: " + counter.get(original)[0] + "false: " + counter.get(original)[1]);
//		System.out.println("for total at label " + kanji.label + " correct: " + stats[0] + "incorrect: " + stats[1] + "percent" + ((double)stats[0] / (((double)stats[1] + (double)stats[1]))));

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
	public int test(File test, File train, String fileType) {


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


		//testing
		for (final File testFileEntry : test.listFiles()) {
			if (testFileEntry.getName().endsWith(fileType)){
				for (StrokeKanji kanji : StrokeKanji.getKanjis(testFileEntry, fileType)){
					if (kanji != null){
						classify(vectorSpace, kNearest, kanji);
					}
				}
			}	

			System.out.println("correct: " + stats[0] + "incorrect: " + stats[1] + " percent " + ((double)stats[0] / ((double)stats[0] + (double)stats[1])));

		}

		return (int)stats[0];
	}


}