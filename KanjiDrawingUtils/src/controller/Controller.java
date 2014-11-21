package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import pixelLevelClassifiers.*;
import strokeLevelClassifiers.*;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;



/**
 * 
 * Controller runs and compares all tests
 *
 * 
 * @author etaiklein
 * 6/7/14
 */

public class Controller {


	File collectedPixelTest = new File("./ScaledKanjiTest");
	File collectedPixelTrain = new File("./ScaledKanjiTrain");

	File collectedFilesTest = new File("./kanjiTXTtest");
	File collectedFilesTrain = new File("./kanjiTXTtrain");
	
	File casiaPixelTest = new File("./kanjiSVGtestOther");
	File casiaPixelTrain = new File("./KanjiSVGtrainingOther");
	File casiaPixelTestLite = new File("./chinatestlite");
	File casiaPixelTrainLite = new File("./chinatrainlite");

	File originalCASIATestFiles = new File("/Users/etaiklein/Android/KanjiDrawingUtils/competition_POT/testing");
	File originalCASIATrainFiles = new File("/Users/etaiklein/Android/KanjiDrawingUtils/competition_POT");
	File liteCASIATestFiles = new File("/Users/etaiklein/Android/KanjiDrawingUtils/potlitetrain/testing");
	File liteCASIATrainFiles = new File("/Users/etaiklein/Android/KanjiDrawingUtils/potlitetrain");
	

	public static void main(String[] argv){

		Controller controller = new Controller();

		StriatoCorticalLoopWeka SLC = new StriatoCorticalLoopWeka();
		PixelGaussian PixGau = new PixelGaussian();	
		PixelKnn PixKNN = new PixelKnn(3);
		StrokeGaussian StroGau = new StrokeGaussian();
		StrokeGaussianHiddenWeights StroGauHW = new StrokeGaussianHiddenWeights();
		StrokeKnn StroKNN = new StrokeKnn(3);
		StrokeKnnHiddenWeights StroKNNHW = new StrokeKnnHiddenWeights(3);
		PixelMultivariateGaussian PMG = new PixelMultivariateGaussian();
		StrokeMultivariateGaussian SMG = new StrokeMultivariateGaussian();

		try {
			System.out.println("---------RUNNING SLC---------");
			SLC.test(controller.collectedFilesTest, controller.collectedFilesTrain, ".txt");
			System.out.println("---------RUNNING KNN---------");
			StroKNN.test(controller.collectedFilesTest, controller.collectedFilesTrain, ".txt");
			System.out.println("---------RUNNING KNN HIDDEN WEIGHTS (wait time appx 2min)---------");
			StroKNNHW.test(controller.collectedFilesTest, controller.collectedFilesTrain, ".txt");
			System.out.println("---------RUNNING StroGau---------");
			StroGau.test(controller.collectedFilesTest, controller.collectedFilesTrain, ".txt");
			System.out.println("---------RUNNING StroGauHW---------");
			StroGauHW.test(controller.collectedFilesTest, controller.collectedFilesTrain, ".txt");
			System.out.println("Features in order: lengths, angles, moves, distances, strokes");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// /**Pixel Level Chinese DataSet Tests**/

		// PixKNN.test(controller.casiaPixelTest, controller.casiaPixelTrain, ".jpg");
		// //		correct: 30incorrect: 190percent0.13636363636363635

		// PixGau.test(controller.casiaPixelTest, controller.casiaPixelTrain, ".jpg");
		// //		PixZscore - correct: 46 incorrect: 174 percent: 0.20909090909090908
		// //		PixGau distance - correct: 18 incorrect: 202 percent: 0.08181818181818182

		// try{PMG.test(new IBk(), controller.casiaPixelTestLite, controller.casiaPixelTrainLite, ".jpg", "IBk");}catch(java.lang.OutOfMemoryError e){System.out.println("out of memory IBK");}		
		// //		collected success! 1/5 correct, 20 %

		// try{PMG.test(new SMO(), controller.casiaPixelTestLite, controller.casiaPixelTrainLite, ".jpg", "SMO");}catch(java.lang.OutOfMemoryError e){System.out.println("out of memory IBK");}		
		// //		collected success! 1/5 correct, 20 %


		// /**Pixel Level Collected DataSet Tests**/

		// PixKNN.test(controller.collectedPixelTest, controller.collectedPixelTrain, ".png");
		// //		correct: 14incorrect: 89percent0.13592233009708737

		// PixGau.test(controller.collectedPixelTest, controller.collectedPixelTrain, ".png");
		// //		PixGau Zscore - correct: 61 incorrect: 42 percent: 0.5922330097087378
		// //		PixGau distance - correct: 46 incorrect: 57 percent: 0.44660194174757284

		// //multivariate KNN
		// try{PMG.test(new IBk(), controller.collectedPixelTest, controller.collectedPixelTrain, ".png", "IBk");}catch(java.lang.OutOfMemoryError e){System.out.println("out of memory IBK");}		
		// //		collected success! 55/103 correct,53.3981 %

		// //try{PMG.test(new SMO(), controller.collectedPixelTest, controller.collectedPixelTrain, ".png", "SMO");}catch(java.lang.OutOfMemoryError e){System.out.println("out of memory IBK");}		
		// //		Broken

		// /**Stroke Level Chinese DataSet Tests**/

		// StroKNN.test(controller.originalCASIATestFiles, controller.originalCASIATrainFiles, ".pot");
		// //		correct: 2663incorrect: 34851percent0.07098683158287572
//		 StroGau.test(controller.liteCASIATestFiles, controller.liteCASIATrainFiles, ".pot");
		// //		broken - runtime is too long to test

		// //try{SMG.test(new IBk(), controller.liteCASIATestFiles, controller.liteCASIATrainFiles, ".pot", "IBK");}catch(java.lang.OutOfMemoryError e){System.out.println("out of memory IBK");}		
		// //		broken - Correctly Classified Instances          0/3738               100 %

		// //try{SMG.test(new SMO(), controller.liteCASIATestFiles, controller.liteCASIATrainFiles, ".pot", "SMO");}catch(java.lang.OutOfMemoryError e){System.out.println("out of memory IBK");}		
		// //		broken - runtime

		// /**Stroke level Collected DataSet Tests**/

//		
		// //		correct: 74incorrect: 29percent0.7184466019417476
		// // correct: 86incorrect: 17percent0.8349514563106796 w/ weights

//				 StroGau.test(controller.collectedFilesTest, controller.collectedFilesTrain, ".txt");
		// //		guessZsco: 北 actual: 二 correct: 11 incorrect: 92 percentage: 0.10679611650485436
		// //		guessDist: 二 actual: 二 correct: 55 incorrect: 48 percentage: 0.5339805825242718

		// //		multivariate KNN
		// try{SMG.test(new IBk(), controller.collectedFilesTest, controller.collectedFilesTrain, ".txt", "IBK");}catch(java.lang.OutOfMemoryError e){System.out.println("out of memory IBK");}		
		// //		success!Correctly Classified Instances          62/103               60.1942 %

		// //multivariate SMO
		// try{SMG.test(new SMO(), controller.collectedFilesTest, controller.collectedFilesTrain, ".txt", "SMO");}catch(java.lang.OutOfMemoryError e){System.out.println("out of memory IBK");}		
		// //		success!Correctly Classified Instances          51/103               49.5146 %


		// System.out.println("PixKNN - correct: " + PixKNN.stats[0] + " incorrect: " + PixKNN.stats[1] + " percent: " + (double)((double)PixKNN.stats[0] / (double) (PixKNN.stats[0] + PixKNN.stats[1])));
		// System.out.println("StroKNN - correct: " + StroKNN.stats[0] + " incorrect: " + StroKNN.stats[1] + " percent: " + (double)((double)StroKNN.stats[0] / (double) (StroKNN.stats[0] + StroKNN.stats[1])));
		// System.out.println("PixGau distance - correct: " + PixGau.distanceStats[0] + " incorrect: " + PixGau.distanceStats[1] + " percent: " + (double)((double)PixGau.distanceStats[0] / (double) (PixGau.distanceStats[0] + PixGau.distanceStats[1])));
		// System.out.println("PixGau Zscore - correct: " + PixGau.zScoreStats[0] + " incorrect: " + PixGau.zScoreStats[1] + " percent: " + (double)((double)PixGau.zScoreStats[0] / (double) (PixGau.zScoreStats[0] + PixGau.zScoreStats[1])));
		// System.out.println("stroGau distance - correct: " + StroGau.distanceStats[0] + " incorrect: " + StroGau.distanceStats[1] + " percent: " + (double)((double)StroGau.distanceStats[0] / (double) (StroGau.distanceStats[0] + StroGau.distanceStats[1])));
		// System.out.println("stroGau Zscore - correct: " + StroGau.zScoreStats[0] + " incorrect: " + StroGau.zScoreStats[1] + " percent: " + (double)((double)StroGau.zScoreStats[0] / (double) (StroGau.zScoreStats[0] + StroGau.zScoreStats[1])));

 	}

 }
