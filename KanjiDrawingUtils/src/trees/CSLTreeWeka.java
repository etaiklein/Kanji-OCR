package trees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import kanjiClasses.StrokeKanji;

public class CSLTreeWeka {
		
		private CLSNode root = new CLSNode(1);
		public int depth = 1;
		
		public class CLSNode {
			
			public StrokeKanji label;
			private ArrayList<StrokeKanji> data = new ArrayList<StrokeKanji>();
			public CLSNode parent = null;
			public ArrayList<CLSNode> children = new ArrayList<CLSNode>();
			private Instance centroid = null;
			public int depth;
			
			public CLSNode(int mydepth){
				depth = mydepth;
			}

			public void add(StrokeKanji k) {
				data.add(k);
			}
			
			public ArrayList<StrokeKanji> getData() {
				return data;
			}
			
			public void setData(ArrayList<StrokeKanji> data) {
				this.data = data;
			}

			public Instance getCentroid() {
				return centroid;
			}

			public void setCentroid(Instance cluster) {
				this.centroid = cluster;
			}
			
			public String toString(){				
				String ce = "";
				if (centroid != null){
					ce += "" + centroid.attribute(0);
					ce = ce.substring(22, ce.length()-1);
				}
				
				String a = " [";
				for (StrokeKanji k : data){
					a += k.label + ", ";
				}
				a = a.substring(0, a.length()-2);
				
				return "Node:" + a + "] parent [" + ce + "]" + " depth " + depth; 				
			}
			
		}

		public CLSNode getRoot() {
			return root;
		}

		public void setRoot(CLSNode root) {
			this.root = root;
		}
		
		public Instances getClusterCentroids(){
			
			FastVector      atts, attVals;

			// 1. set up attributes
			atts = new FastVector();

			attVals = new FastVector();

			for (StrokeKanji kanji : root.data){
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
			Instances I = new Instances("Kanjis", atts, 0);

			//	3. populate with leaf centroids
			Queue<CLSNode> Q = new LinkedList<CLSNode>();
			Q.add(root);
			while (!Q.isEmpty()){
				CLSNode currentNode = Q.poll();
				for (CLSNode child : currentNode.children){
					Q.add(child);
				}
				if (currentNode.children.isEmpty()){
					I.add(currentNode.centroid);
				}
			}
			return I;
		}
				
		public String toString(){
			String mystring = "depth " + depth;
			Queue<CLSNode> Q = new LinkedList<CLSNode>();
			Q.add(root);
			HashMap<Integer, Integer> h = new HashMap<Integer,Integer>();
			while (!Q.isEmpty()){
				CLSNode currentNode = Q.poll();
				for (CLSNode child : currentNode.children){
					Q.add(child);
				}
				if (currentNode.children.isEmpty()){
					int key = currentNode.data.size();
					if (h.containsKey(key)){
						h.put(key, h.get(key) + 1);
					}else{
						h.put(key, 1);
					}
				}
			}
			mystring += "\nKanji in each node" + h.entrySet();
			return mystring;
		}
		
	}