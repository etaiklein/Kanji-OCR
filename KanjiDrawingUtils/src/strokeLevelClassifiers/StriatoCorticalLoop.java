package strokeLevelClassifiers;

/**
 * StriatoCorticalLoop
 * 
 * Uses the striato-cortical loop iterative classification algorithm for sorting kanji
 * 
 * The basic idea is that anything can be grouped and subgrouped until each group is mostly uniform
 * 
 * Etai Klein
 * 6/7/14
 * 
 */


import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import kanjiClasses.*;
import trees.CLSTree;
import trees.CLSTree.CLSNode;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;


public class StriatoCorticalLoop {
	
	
	Queue<CLSNode> Q = new LinkedList<CLSNode>();
	CLSTree T = new CLSTree();
	
	
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
	
	/**
	 * Classify
	 * 
	 * Uses the centroids at the leaves of the tree for a one time classification
	 * @throws Exception 
	 * 
	 */
		
	public void classify(CLSTree T, int numClusters, ArrayList<StrokeKanji> ary) throws Exception{
		
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
					double dist = kanji.distance(child.label);
					if (dist <= min){
						min = dist;
						best = child;
						System.out.println(kanji.label + " " + min + " " + best);
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
			
//			System.out.println("kanji " + kanji.label + " classified as " + best.label.label);
			
		}
		System.out.println( "correct: " + correct + "incorrect: " + incorrect);
//		
//		//build cluster
//		Instances centroids = T.getClusterCentroids();
//		
//		FastVector attVals = new FastVector();
//
//		// - nominal
//		for (StrokeKanji kanji : ary){
//			if (!attVals.contains("" + kanji.label)){
//				attVals.addElement("" + kanji.label);
//			}
//			
//		}		
//		
//		for (int i = 0; i < centroids.numInstances(); i++){
////			System.out.println(centroids.instance(i).toString(new Attribute("KanjiName", attVals, 0)));
//		}
//		
//		centroids.setClassIndex(0);
//		IBk IBk = new IBk();
//		IBk.buildClassifier(centroids);
//		IBk.setKNN(2);
//
//		CLSNode testNode = T.new CLSNode(0);
//		testNode.setData(ary);
//		Instances test = kanjiToARFF(testNode);
//		test.setClassIndex(0);
//		
//		Evaluation eval = new Evaluation(test);
//		eval.evaluateModel(IBk, test);
//		System.out.println(eval.toSummaryString("\n" + "KNN" + " Results\n======\n", false));
//		Instances labeled = new Instances(test);
//
//		// label instances
//		for (int i = 0; i < test.numInstances(); i++) {
//			double label = IBk.classifyInstance(test.instance(i));
//			labeled.instance(i).setClassValue(label);
//			//comment this line in for more information
//				System.out.println(label + " -> " + test.classAttribute().value((int) label));
//		}
//		
	}
	/**
	 * test
	 * scores training kanji and classifies all test kanji
	 * 
	 * @param test - the kanji to classify
	 * @param train - the kanji to use in the distribution
	 * @param fileType
	 * @throws Exception 
	 * 
	 * @pseudocode:
	 * 1.add each kanji to the root
	 * 2. iteratively classify each new kanji
	 * 
	 */
	public void test(File test, File train, String fileType) throws Exception {
		
		for (int numClusters = 2; numClusters < 3; numClusters++){
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
					classify(T, 2, testKanjis);
				}	
			}
			
		System.out.println("num"+ numClusters + " " + numClusters * T.depth + "\n" + T);
		}
	}
		
	
	public static void main(String argv[]){
		
		File train = new File("./kanjiTXTtrain");
		File test = new File("./kanjiTXTtest");

		String fileType = ".txt";
		
		StriatoCorticalLoop CLS = new StriatoCorticalLoop();
		
		try {
			CLS.test(test, train, fileType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}