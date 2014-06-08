package pixelLevelClassifiers;
/**
 * PixelMultivariate Gaussian
 * 
 * Uses Weka, a machine learning library, to classify kanji
 * 
 * This instance uses pixel-level data. The basic idea is to create an ARFF file for each Kanji, and
 * then use the files to run classification
 * 
 * IBK (basically KNN) and SMO (basically multivariate Gaussian) are of particular interest
 * 
 * @author etaiklein
 *
 *Based on tutorials from http://weka.wikispaces.com/
 *
 */

import java.io.File;

import classifierInterfaces.MultivariateWEKA;

import kanjiClasses.PixelKanji;

import weka.classifiers.*;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SparseInstance;

public class PixelMultivariateGaussian implements MultivariateWEKA{

	/**
	 * 
	 * KanjiToARFF
	 * 
	 * @summary Turns all the Kanji Images in a folder into a single ARFF file for WEKA analysis
	 * 
	 * @param path - the path to the Kanji files
	 * @return Instances - the ARFF formated data file
	 * 
	 * @pseudocode
	 * 1. Define my attributes
	 * 2. Create an Instance
	 * 3. Add all Kanjis to Instance
	 */
	
	public Instances KanjiToARFF(String path, String fileType){

		FastVector      atts;
		FastVector		attVals;
		Instances       data;
		double[]        vals;
		int width = 400;
		int height = 400;
		File folder = new File(path);

		// 1. set up attributes
		atts = new FastVector();

		// - nominal
		attVals = new FastVector();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.getName().endsWith(fileType)){
				if (!attVals.contains("" + fileEntry.getName().substring(0,1))){
					attVals.addElement("" + fileEntry.getName().substring(0,1));
				}
			}
		}

		atts.addElement(new Attribute("KanjiName", attVals, 0));

		// - numeric
		int i = 0;
		for (int row = 0; row < height; row++){
			for (int col = 0; col < width; col++){
				atts.addElement(new Attribute("pixel " + i++));
			}
		}


		// 2. create Instances object
		data = new Instances("Kanjis", atts, 0);


		//3. Add Instances in chunks

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.getName().endsWith(fileType)){
				PixelKanji kanji = new PixelKanji(fileEntry.getAbsolutePath());
				// 3. fill with data
				//create the value array
				vals = new double[data.numAttributes()];
				//add the label to values
				vals[0] = attVals.indexOf("" + kanji.label);
			
				//loop through pixels, adding them to the values Attribute
				i = 1;
				int[][] pixels = kanji.toPixels();
				for (int row = 0; row < height; row++){
					for (int col = 0; col < width; col++){
						vals[i++] = pixels[row][col];
					}
				}


				//add the variables to the instance
				data.add(new SparseInstance(1.0, vals));
			}

		}
		return data;
	}

	/**
	 * test - Test train/test data on a certain classifier
	 * 
	 * @param cls - The classifier to use
	 * @param train - The training data
	 * @param test - The testing data
	 * @param name - The name of the classifier
	 */
	
	public void test(Classifier cls, File testfile, File trainfile, String fileType, String name){
		try{

			Instances train = KanjiToARFF(trainfile.getAbsolutePath(), fileType);
			Instances test = KanjiToARFF(testfile.getAbsolutePath(), fileType);
			train.setClassIndex(0);
			test.setClassIndex(0);

			// train classifier
			cls.buildClassifier(train);
			// evaluate classifier and print some statistics
			Evaluation eval = new Evaluation(train);
			eval.evaluateModel(cls, test);
			System.out.println(eval.toSummaryString("\n" + name + " Results\n======\n", false));

			Instances labeled = new Instances(test);

			// label instances
			for (int i = 0; i < test.numInstances(); i++) {
				double clsLabel = cls.classifyInstance(test.instance(i));
				labeled.instance(i).setClassValue(clsLabel);
				//				System.out.println(clsLabel + " -> " + test.classAttribute().value((int) clsLabel));
			}


		}catch (Exception e) {e.printStackTrace();}


	}

}
