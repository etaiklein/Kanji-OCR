package strokeLevelClassifiers;
/**
 * StrokeMultivariate Gaussian
 * 
 * Uses Weka, a machine learning library, to classify kanji
 * 
 * This instance uses stroke-level data. The basic idea is to create an ARFF file for each Kanji, and
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

import kanjiClasses.StrokeKanji;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SparseInstance;
import classifierInterfaces.MultivariateWEKA;

public class StrokeMultivariateGaussian implements MultivariateWEKA{

	@Override
	/**
	 * 
	 * KanjiToARFF
	 * 
	 * @summary Turns all the Kanji Images in a folder into a single ARFF file for WEKA analysis
	 * 
	 * @param String path - the path to the Kanji files
	 * @param String fileType - the type of data file to extract from
	 * @return Instances - the ARFF formated data file
	 * 
	 * @pseudocode
	 * 1. Define my attributes
	 * 2. Create an Instance
	 * 3. Add all Kanjis to Instance
	 */
	public Instances KanjiToARFF(String path, String fileType){

		FastVector      atts, attVals;
		Instances       data;
		double[]        vals;
		File folder = new File(path);

		// 1. set up attributes
		atts = new FastVector();

		attVals = new FastVector();

		File labelFile = folder.listFiles()[0];
		int it = 0;
		while (!labelFile.getName().endsWith(fileType)){
			labelFile = folder.listFiles()[it++];
		}
		
		// - nominal
		StrokeKanji[] kanjis = StrokeKanji.getKanjis(labelFile, fileType);
		for (StrokeKanji kanji : kanjis){
			if (!attVals.contains("" + kanji.label)){
				attVals.addElement("" + kanji.label);
			}
			
		}

		atts.addElement(new Attribute("KanjiName", attVals, 0));		
		
		// - numerical features
		for (int i = 0; i < 30; i++){
			atts.addElement(new Attribute("Length" + (i)));
			atts.addElement(new Attribute("Angle" + (i)));
			if (i != 30){
				atts.addElement(new Attribute("Movement" + (i)));
			}
			i++;
		}


		// 2. create Instances object
		data = new Instances("Kanjis", atts, 0);


		//3. Add Instances in chunks

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.getName().endsWith(fileType))
			for (StrokeKanji kanji : StrokeKanji.getKanjis(fileEntry, fileType)){
				
				// 3. fill with data

				vals = new double[data.numAttributes()];
				//add the label to values
				vals[0] = attVals.indexOf("" + kanji.label);

				//add the features to values
				int j = 1;
				for (int i = 0; i < 30; i++){
					vals[j++] = kanji.lengths[i];
					vals[j++] = kanji.angles[i];
					if (i > 0){vals[j++] = kanji.moves[i];}
					i++;
				}

				//add the variables to the instance
				data.add(new SparseInstance(1.0, vals));
			}
		}

		return data;
	}


	@Override
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
				//comment this line in for more information
				//	System.out.println(clsLabel + " -> " + test.classAttribute().value((int) clsLabel));
			}


		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
