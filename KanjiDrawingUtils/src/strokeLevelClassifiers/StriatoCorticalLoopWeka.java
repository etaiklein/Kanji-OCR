package strokeLevelClassifiers;

/**
 * StriatoCorticalLoopWeka
 * 
 * Uses the striato-cortical loop iterative classification algorithm for sorting kanji
 * 
 * Clusterer - Weka K-means
 * Classifier - distance method comparison for tree descent
 * 
 * The basic idea is that anything can be grouped and subgrouped until each group is mostly uniform
 * 
 * Etai Klein
 * 6/7/14
 * 
 */


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import kanjiClasses.*;
import trees.CSLTreeWeka.CLSNode;
import trees.CSLTreeWeka;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;


public class StriatoCorticalLoopWeka {
	
	
	Queue<CLSNode> Q = new LinkedList<CLSNode>();
	CSLTreeWeka T = new CSLTreeWeka();
	int bestScore = 0;
	int[] bestScoreWeights = new int[5];
	
	/** isUniform
	 * 
	 * Are all items in a node equivalent?
	 * 
	 */
	
	public boolean isUniform(CLSNode node){
		//is the current node uniform and equivalent?
//		System.out.println("equality check: ");
		boolean uniform = true;
		Character label = node.getData().get(0).label;
		for (StrokeKanji k : node.getData()){
//			System.out.print(k.label);
			if (!k.label.equals(label)){
				uniform = false;
				break;
			}
		}
//		System.out.println(uniform);
		return uniform;
	}
	
	/** kanjiToARFF
	 * 
	 * Takes a list of Kanji and outputs it into a format usable by WEKA
	 * @return 
	 * 
	 */
	
	public Instances kanjiToARFF(CLSNode node){
		FastVector      atts, attVals;
		Instances       data;
		double[]        vals;

		// 1. set up attributes
		atts = new FastVector();

		attVals = new FastVector();

		for (StrokeKanji kanji : node.getData()){
			if (!attVals.contains("" + kanji.label)){
				attVals.addElement("" + kanji.label);
			}
			
		}

		atts.addElement(new Attribute("KanjiName", attVals, 0));		
		atts.addElement(new Attribute("numstrokes"));		

		
		
		// - numerical features
		for (int i = 0; i < 30; i++){
			atts.addElement(new Attribute("Length" + (i)));
			atts.addElement(new Attribute("Angle" + (i)));
			atts.addElement(new Attribute("DistanceX" + (i)));
			atts.addElement(new Attribute("DistanceY" + (i)));
			if (i != 30){
				atts.addElement(new Attribute("Movement" + (i)));
			}
		}
		

		// 2. create Instances object
		data = new Instances("Kanjis", atts, 0);

		
		//3. Add Instances in chunks

		for (StrokeKanji kanji : node.getData()){
				
			// 3. fill with data
			vals = new double[data.numAttributes()];
			//add the label to values
			vals[0] = attVals.indexOf("" + kanji.label);
			vals[1] = attVals.indexOf(kanji.numstrokes);

			//add the features to values
			int j = 1;
			for (int i = 0; i < 30; i++){
				vals[j++] = kanji.lengths[i];
				vals[j++] = kanji.angles[i];
				vals[j++] = kanji.distances[i*2];
				vals[j++] = kanji.distances[i*2 + 1];
				if (i != 29){vals[j++] = kanji.moves[i];}
			}

			//add the variables to the instance
			data.add(new SparseInstance(1.0, vals));
		}

		return data;
	}
	
	/** K-means
	 * 
	 * Classifies a group into k subgroups
	 * @return 
	 * @return 
	 * @throws Exception 
	 * 
	 */
	
	public HashMap<Instance, ArrayList<StrokeKanji>> kMeans(CLSNode node, int numClusters) throws Exception{
		//build cluster
		SimpleKMeans K = new SimpleKMeans();
		K.setNumClusters(numClusters);
		K.setPreserveInstancesOrder(true);
		Instances I = kanjiToARFF(node);
		K.buildClusterer(I);
		
		//assign kanjis
		HashMap<Instance,ArrayList<StrokeKanji>> dictionary = new HashMap<Instance,ArrayList<StrokeKanji>>();
		int[] assignments = K.getAssignments();
		ArrayList<StrokeKanji> kanjis = node.getData();
		int i=0;
		for(int clusterNum : assignments) {
			Instance centroid = K.getClusterCentroids().instance(clusterNum);
			//find and update
			if (dictionary.containsKey(centroid)){
				dictionary.get(centroid).add(kanjis.get(i));
			}
			//or create
			else{
				ArrayList<StrokeKanji> ask = new ArrayList<StrokeKanji>();
				ask.add(kanjis.get(i));
				dictionary.put(centroid, ask);
			}
		    i++;
		}
		
		return dictionary;
	}
	
	
	/** RecursivelyClassify
	 * 
	 * Reruns the classify algorithm to find the most correct local maximum of weights
	 * 
	 * @param CLSTree T - the Tree containing all the training data
	 * @param Callable<Integer> classify - the classification method
	 * @param ArrayList<StrokeKanji> kanjis - the test data
	 * @param int[] weights - the weights for this set of classifications (init 0)
	 * @param int bestcorrect - the most correct classifications for any run
	 * @param int[] bestweights - the weights resulting in the best classification
	 * @param double weightValue - the weight multiplier
	 * @param double weightDiminisher - reduces the weight multiplier
	 * 
	 * @pseudocode:
	 * 1. run the classification algorithm
	 * 2. return if you didn't score better 
	 * 3. run again increasing each weight by a diminishing factor
	 * 
	 * @return 

	
	public void recursivelyClassify(CSLTreeWeka T, Callable<Integer> classify, ArrayList<StrokeKanji> kanjis, int[] weights, int bestcorrect, int[] bestweights, double weightValue, double weightDiminisher){
		
		//1. classify
		int correct = classify(T, 0, kanjis);
		//2. return if you didn't score better
		//can switch to >= to increase the chance of finding another local maximum
		if (correct > bestcorrect){
			bestcorrect = correct;
			bestweights = weights;
			//3a. run again increasing each weight
			for (int i = 0; i < weights.length; i++){
				//3b. by a diminishing factor
				int[] weightscopy = weights.clone();
				weightValue = weightValue *  weightDiminisher;
				weightscopy[i] = (int) (weightscopy[i] + weightValue);
				recursivelyClassify(T, classify, kanjis, weightscopy, bestcorrect, bestweights, weightValue, weightDiminisher);
			}
		}
	}
	 */
	
	/** classify | recursive
	 * 
	 * Reruns the classify algorithm to find the most correct local maximum of weights
	 * 
	 * @param CLSTree T - the Tree containing all the training data
	 * @param numcluster - the number of clusters
	 * @param ArrayList<StrokeKanji> kanjis - the test data
	 * @param int[] weights - the weights for this set of classifications (init 0)
	 * @param int bestcorrect - the most correct classifications for any run
	 * @param int[] bestweights - the weights resulting in the best classification
	 * @param double weightValue - the weight multiplier
	 * 
	 * @pseudocode:
	 * 1. run the classification algorithm
	 * 2. return if you didn't score better 
	 * 3. run again increasing each weight by a diminishing factor
	 * 
	 * @return 
	*/

	public void classify(CSLTreeWeka T, int numClusters, ArrayList<StrokeKanji> ary, int[] weights, int bestcorrect, int[] bestweights, double weightValue) throws Exception{


		int correct = 0;
		int incorrect = 0;
		
		for (StrokeKanji kanji : ary){
			Queue<CLSNode> Q = new LinkedList<CLSNode>();
			CLSNode best = T.getRoot();
			Q.add(best);
			while (!Q.isEmpty()){
				CLSNode currentNode = Q.poll();
				double min = Integer.MAX_VALUE;
				best = null;
				for (CLSNode child : currentNode.children){
					double dist = kanji.distance(child.label, weights[0], weights[1], weights[2], weights[3], weights[4]);
					if (dist <= min){
						min = dist;
						best = child;
					}
				}
				if (best !=null && !best.children.isEmpty()){
					Q.add(best);
				}
			}
			if (kanji.label.equals(best.label.label)){
				correct +=1;
			}else{
				incorrect +=1;
			}
						
		}
		
		String myweights = "";
		for (int weight : weights){
			myweights += weight + ", ";
		}
		myweights = myweights.substring(0,myweights.length()-2);
		
		String mybestweights = "";
		for (int bestweight : bestweights){
			mybestweights += bestweight + ", ";
		}
		mybestweights = mybestweights.substring(0,mybestweights.length()-2);
		
		//update global score
		if (correct > bestScore){
			bestScore = correct;
			bestScoreWeights = weights;
		}
		
		if (correct > bestcorrect){
			bestcorrect = correct;
			bestweights = weights;
			for (int i = 0; i < weights.length; i++){
				int[] weightscopy = weights.clone();
				weightValue = weightValue / 1.1;
				weightscopy[i] = (int) (weightscopy[i] + weightValue);
				classify(T, numClusters, ary, weightscopy, bestcorrect, bestweights, weightValue);
			}
//			System.out.println( "correct: " + correct + "incorrect: " + incorrect + " weights " + myweights + " correct " + bestcorrect + " weights " + mybestweights);
		}
	}
	
	
	
	
	
	/**
	 * Classify
	 * 
	 * Uses the centroids at the leaves of the tree for a one time classification
	 * @throws Exception 
	 * 
	 */
		
	public void classify(CSLTreeWeka T, int numClusters, ArrayList<StrokeKanji> ary) throws Exception{
		
		int correct = 0;
		int incorrect = 0;
		
		for (StrokeKanji kanji : ary){
			Queue<CLSNode> Q = new LinkedList<CLSNode>();
			CLSNode best = T.getRoot();
			Q.add(best);
			while (!Q.isEmpty()){
				CLSNode currentNode = Q.poll();
				double min = Integer.MAX_VALUE;
				best = null;
				for (CLSNode child : currentNode.children){
					double dist = kanji.distance(child.label, 1, 1, 1, 1, 1);
					if (dist <= min){
						min = dist;
						best = child;
					}
				}
				if (best !=null && !best.children.isEmpty()){
					Q.add(best);
				}
			}
			if (kanji.label.equals(best.label.label)){
				correct +=1;
			}else{
				incorrect +=1;
			}
						
		}
		System.out.println( "correct: " + correct + " incorrect: " + incorrect + " weights: 1, 1, 1, 1, 1");

	}
	/**
	 * test
	 * builds a tree from all training kanji and classifies all test kanji
	 * 
	 * @param test - the kanji to classify
	 * @param train - the kanji to use in the distribution
	 * @param fileType
	 * @throws Exception 
	 * 
	 * @pseudocode:
	 * 1.add each kanji to the root
	 * 2.cluster in numClusters new nodes
	 * 3.if any nodes are not uniformly labeled, repeat 2-3
	 * 4.traverse the tree to classify
	 * 
	 */
	public void test(File test, File train, String fileType) throws Exception {
		
		int numClusters = 3;
		T.getRoot().children.clear();
		T.depth = 0;
		
		//training
		for (final File fileEntry : train.listFiles()) {
			if (fileEntry.getName().endsWith(fileType)){
				for (StrokeKanji kanji : StrokeKanji.getKanjis(fileEntry, fileType)){
					if (kanji != null){
						T.getRoot().getData().add(kanji);
					}
				}
			}
		}
		
		//classification
		Q.add(T.getRoot());
		while (!Q.isEmpty()) {
			CLSNode currentNode = Q.poll();
			if (isUniform(currentNode)) {continue;}
			HashMap<Instance, ArrayList<StrokeKanji>> clusters = kMeans(currentNode, numClusters);
			for (Instance centroid : clusters.keySet()){
				CLSNode childNode = T.new CLSNode(T.depth);
				childNode.setCentroid(centroid);
				childNode.setData(clusters.get(centroid));
				childNode.label = clusters.get(centroid).get(0);
				childNode.parent = currentNode;
				currentNode.children.add(childNode);
				Q.add(childNode);
			}
			T.depth++;
		}
		
		//testing
		ArrayList<StrokeKanji> testKanjis = new ArrayList<StrokeKanji>();
		for (final File testFileEntry : test.listFiles()) {
			if (testFileEntry.getName().endsWith(fileType)){
				for (StrokeKanji kanji : StrokeKanji.getKanjis(testFileEntry, fileType)){
					if (kanji != null){
						testKanjis.add(kanji);
					}
				}
				//w/o hidden weights
				classify(T, 2, testKanjis);
				//w/ hidden weights
				System.out.println("---------RUNNING SLC HIDDEN WEIGHTS---------");
				classify(T, 2, testKanjis, new int[]{0,0,0,0,0}, 0, new int[]{0,0,0,0,0}, 1000.0);
				String weights = ""; for (int weight : bestScoreWeights){weights += weight + ", ";}weights = weights.substring(0,weights.length()-2);
				System.out.println( "best: " + bestScore + " bestWeights: " + weights );
			}	
		System.out.println("num"+ numClusters + " " + numClusters * T.depth + "\n" + T);
		}
	}
	
}