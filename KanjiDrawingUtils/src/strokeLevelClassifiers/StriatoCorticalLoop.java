package strokeLevelClassifiers;

/**
 * StriatoCorticalLoop
 * 
 * Uses the striato-cortical loop iterative classification algorithm for sorting kanji
 * 
 * Clusterer - custom K-means
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
import java.util.LinkedList;
import java.util.Queue;

import kanjiClasses.*;
import trees.CSLTree;
import trees.CSLTree.CLSNode;


public class StriatoCorticalLoop {
	
	
	Queue<CLSNode> Q = new LinkedList<CLSNode>();
	CSLTree T = new CSLTree();
	
	
	/** isUniform
	 * 
	 * Are all items in a node equivalent?
	 * 
	 */
	
	public boolean isUniform(CLSNode node){
		//is the current node uniform and equivalent?
		boolean uniform = true;
		if (node.getData().size() < 2){return true;}

		Character label = node.getData().get(0).label;
		for (StrokeKanji k : node.getData()){
			if (!k.label.equals(label)){
				uniform = false;
				break;
			}
		}
		return uniform;
	}
	
	/** getCentroid
	 * 
	 * returns a randomized StrokeKanji
	 * 
	 */
	
	public StrokeKanji getCentroid(){
		StrokeKanji centroid = new StrokeKanji(new int[30]);
		for (int i = 0; i < 15; i++){
			centroid.angles[i] = (int) (Math.random() * 180 * ( Math.random() -.5));
			centroid.moves[i] = (int) (Math.random() * 180 * (Math.random() - .5));
			centroid.lengths[i] = (int) (Math.random() * 600);
			centroid.distances[i*2] = (int) (Math.random() * 5);
			centroid.distances[i*2 + 1] = (int) (Math.random() * 100);

		}
		return centroid;
	}
	
	/** getRanges
	 * 
	 * prints the range of values for each potential feature to help pick smarter centroids
	 * 
	 */

	
	public void getRanges(){

		File train = new File("./kanjiTXTtrain");
		String fileType = ".txt";
		
		int[] minangle = new int[30];
		for (int i = 0; i < 30; i++){ minangle[i] = Integer.MAX_VALUE;}
		int[] maxangle = new int[30];
		int[] minlengths = new int[30];
		for (int i = 0; i < 30; i++){ minlengths[i] = Integer.MAX_VALUE;}
		int[] maxlengths = new int[30];
		int[] mindistances = new int[60];
		for (int i = 0; i < 60; i++){ mindistances[i] = Integer.MAX_VALUE;}
		int[] maxdistances = new int[60];
		int[] minmoves = new int[29];
		for (int i = 0; i < 29; i++){ minmoves[i] = Integer.MAX_VALUE;}
		int[] maxmoves = new int[29];


		//training
		for (final File fileEntry : train.listFiles()) {
			if (fileEntry.getName().endsWith(fileType)){
				for (StrokeKanji kanji : StrokeKanji.getKanjis(fileEntry, fileType)){
					if (kanji != null){
						kanji.distances = kanji.distanceFromCenter();
						for (int k = 0; k < 30; k++){
							if (kanji.angles[k] < minangle[k]){minangle[k] = kanji.angles[k];} 
							if (kanji.angles[k] > maxangle[k]){maxangle[k] = kanji.angles[k];} 
							if (kanji.lengths[k] < minangle[k]){minlengths[k] = kanji.lengths[k];} 
							if (kanji.lengths[k] > maxangle[k]){maxlengths[k] = kanji.lengths[k];} 
							if (kanji.distances[k*2] < mindistances[k*2]){mindistances[k*2] = kanji.distances[k*2];} 
							if (kanji.distances[k*2] > maxdistances[k*2]){maxdistances[k*2] = kanji.distances[k*2];} 
							if (kanji.distances[k*2 + 1] < mindistances[k*2 + 1]){mindistances[k*2 + 1] = kanji.distances[k*2 + 1];} 
							if (kanji.distances[k*2 + 1] > maxdistances[k*2 + 1]){maxdistances[k*2 + 1] = kanji.distances[k*2 + 1];} 
							if (k < 29 && kanji.moves[k] < minmoves[k]){minmoves[k] = kanji.moves[k]; }
							if (k < 29 && kanji.moves[k] > maxmoves[k]){maxmoves[k] = kanji.moves[k]; }
						}
					}
				}
			}
		}
		
		for (int k = 0; k < 30; k++){
			System.out.println( "-------" + k + "-------");
			System.out.println("minangle: " + minangle[k]);
			System.out.println("maxangle: " + maxangle[k]);
			System.out.println("minlength: " + minlengths[k]);
			System.out.println("maxlength: " + maxlengths[k]);
			System.out.println("mindistances: " + mindistances[k*2]);
			System.out.println("maxdistances: " + maxdistances[k*2]);
			System.out.println("mindistances: " + mindistances[k*2 + 1]);
			System.out.println("maxdistances: " + maxdistances[k*2 + 1]);
			if (k > 28){continue;}
			System.out.println("minmoves" + minmoves[k]);
			System.out.println("maxmoves" + maxmoves[k]);
		}
		
	}
	
	/** converged
	 * 
	 * tests equality of two lists of centroids (StrokeKanji)
	 * 
	 *  @return boolean - is converged?
	 */
	
	public boolean converged(ArrayList<StrokeKanji> centroids, ArrayList<StrokeKanji> oldCentroids){
		try {
		for (int j = 0; j < centroids.size() ; j++){
			for (int k = 0; k < 30; k++){
				if (centroids.get(j).angles[k] != oldCentroids.get(j).angles[k]){return false;}
				if (centroids.get(j).lengths[k] != oldCentroids.get(j).lengths[k]){return false;}
				if (centroids.get(j).distances[k*2] != oldCentroids.get(j).distances[k*2]){return false;}
				if (centroids.get(j).distances[k*2 + 1] != oldCentroids.get(j).distances[k*2 + 1]){return false;}
				if (k < 29 && centroids.get(j).moves[k] != oldCentroids.get(j).moves[k]){return false;}
			}
		}
		return true;
		}
		catch (Exception e){
		return false;
		}
	}
	
	
	/** K-means
	 * 
	 * Classifies a group into k subgroups
	 * @return 
	 * @return 
	 * 
	 */
	
	public ArrayList<ArrayList<StrokeKanji>> kMeans(CLSNode currentNode, int numCentroids){
		
		//1. initialize centroids
		int limit = 20;
		int it = 0;
		ArrayList<ArrayList<StrokeKanji>> data = new ArrayList<ArrayList<StrokeKanji>>(numCentroids);
		ArrayList<StrokeKanji> centroids = new ArrayList<StrokeKanji>(), oldCentroids = new ArrayList<StrokeKanji>();
		for (int i = 0; i < numCentroids; i++){
			centroids.add(i, getCentroid());
		}
		//2. while not yet converged or under iteration limit
		while (!converged(centroids, oldCentroids) && it < limit){
			//2a iterate, set centroids
			oldCentroids = centroids;
			it++;
			
			//2b label data by closest centroid
			data = new ArrayList<ArrayList<StrokeKanji>>(numCentroids);
			for (int i = 0; i < numCentroids; i++){
				data.add(new ArrayList<StrokeKanji>());
			}
			
			for (StrokeKanji kanji : currentNode.getData()){
				double min = Integer.MAX_VALUE;
				int closest = -1;
				for (int i = 0; i < centroids.size(); i++){
					double dist = kanji.distance(centroids.get(i), 1, 1, 3, 3, 10);
					if (dist < min){
						min = dist;
						closest = i;
					}
				}
				data.get(closest).add(kanji);
				
			}
			
			//2c reassign centroids
			for (int i = 0; i < numCentroids; i++){
				int datasize = data.get(i).size();
				//assign new centroids if empty
				if (datasize == 0){
					centroids.remove(i);
					centroids.add(i, getCentroid());
					continue;
				}else{
				}
				
				//get average centroid 
				centroids.get(i).aveangles = new int[30];
				centroids.get(i).avelengths = new int[30];
				centroids.get(i).avemoves = new int[29];
				centroids.get(i).avedistances = new int[60];
				for (int j = 0; j < datasize ; j++){
					for (int k = 0; k < 30; k++){
						centroids.get(i).aveangles[k] += data.get(i).get(j).angles[k];
						centroids.get(i).avelengths[k] += data.get(i).get(j).lengths[k];
						centroids.get(i).avedistances[k*2] += data.get(i).get(j).distances[k*2];
						centroids.get(i).avedistances[k*2 + 1] += data.get(i).get(j).distances[k*2 + 1];
						if (k < 29){centroids.get(i).avemoves[k] += data.get(i).get(j).moves[k];}
					}
				}
				for (int k = 0; k < 30; k++){
					centroids.get(i).angles[k] = centroids.get(i).aveangles[k] / datasize;
					centroids.get(i).avelengths[k] += centroids.get(i).avelengths[k] / datasize;
					centroids.get(i).avedistances[k*2] += centroids.get(i).avedistances[k*2] / datasize;
					centroids.get(i).avedistances[k*2 + 1] += centroids.get(i).avedistances[k*2 + 1] / datasize;
					if (k < 29){centroids.get(i).avemoves[k] += centroids.get(i).avemoves[k] / datasize;}
				}
				
			}
			
			
		}
		//3. return centroids
		return data;
	}
		
	/**
	 * Classify
	 * 
	 * Uses the centroids at the leaves of the tree for a one time classification
	 * @throws Exception 
	 * 
	 */
		
	public void classify(CSLTree T, int numClusters, ArrayList<StrokeKanji> ary) throws Exception{
		
		int correct = 0;
		int incorrect = 0;
		
		for (StrokeKanji kanji : ary){
			Queue<CLSNode> Q = new LinkedList<CLSNode>();
			CLSNode best = T.getRoot();
			Q.add(best);
			while (!Q.isEmpty()){
				CLSNode currentNode = Q.poll();
				double min = Integer.MAX_VALUE;
				best = T.getRoot();
				for (CLSNode child : currentNode.children){
					double dist = kanji.distance(child.label, 1, 1, 3, 3, 10);
					if (dist <= min){
						min = dist;
						best = child;
					}
				}
				if (best !=null && !best.children.isEmpty()){
					Q.add(best);
				}
			}
			if (best != null && kanji.label.equals(best.label)){
				correct +=1;
			}else{
				incorrect +=1;
			}
						
		}
		System.out.println( "correct: " + correct + "incorrect: " + incorrect);

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
				  ArrayList<ArrayList<StrokeKanji>> clusters = kMeans(currentNode, numClusters);
				for ( ArrayList<StrokeKanji> cluster : clusters){
					if (cluster.size() < 1){continue;}
					CLSNode childNode = T.new CLSNode(T.depth);
					childNode.setData(cluster);
					childNode.label = cluster.get(0);
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
					classify(T, numClusters, testKanjis);
				}	
			}
			
		System.out.println("num"+ numClusters + " " + numClusters * T.depth + "\n" + T);
		
	}
	
	public static void main(String argv[]){
		
		File train = new File("./kanjiTXTtrain");
		String fileType = ".txt";
		File test = new File("./kanjiTXTtest");
		
		StriatoCorticalLoop SCL = new StriatoCorticalLoop();
		
		try {
			SCL.test(test, train, fileType);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}