package kanjiClasses;

/**
 * StrokeKanji
 * 
 * Kanji class for stroke-level analysis
 * 
 * @author etaiklein
 * 6/7/14
 */

import java.io.File;
import java.util.ArrayList;
import utils.ChineseOnlineHandwritingToJPG;
import utils.ImageProcessingUtils;


public class StrokeKanji extends Kanji{

	// the number of strokes (pen lifts) used to draw the kanji 
	public int numstrokes;
	// the GBK code of the kanji
	public Character label;
	//list of all values the user's pen passes through
	public int[] fullStrokes;
	//list of start/end xy points for the strokes
	public int[] reducedStrokes;

	//features

	// the length of each
	public int[] lengths;
	// their angle in degrees
	public int[] angles;
	// the angle between the end of one stroke and the start of another - in degrees
	public int[] moves;
	// the distance of the stroke center from the center of the kanji by percentage
	public int[] distances;

	//averagedFeatures

	// the length of each
	public int[] avelengths;
	// their angle in degrees
	public int[] aveangles;
	// the angle between the end of one stroke and the start of another - in degrees
	public int[] avemoves;
	// the distance of the stroke center from the center of the kanji by percentage
	public int[] avedistances;

	//standardDeviation of averages

	// the standard deviation of the length of each
	public int[] lengthstdev;
	// the standard deviation of their angle in degrees
	public int[] anglestdev;
	// the standard deviation of the angle between the end of one stroke and the start of another - in degrees
	public int[] movestdev;
	// the distance of the stroke center from the center of the kanji by percentage
	public int[] distancesstdev;

	// An SVG schematic of the kanji - 
	public String svg;

	//the source file of the kanji
	public String source;

	public StrokeKanji(){
	}
	
	public StrokeKanji(String location){
		source = location;
		label = source.charAt(source.lastIndexOf("/") +1);
	}

	public StrokeKanji(int[] strokes){
		reducedStrokes = strokes;
		lengths = avelengths = lengthstdev = new int[30];
		angles = aveangles = anglestdev = new int[30];
		distances = avedistances = distances = new int[120];
		moves = avemoves = movestdev = new int[29];
	}

	/**
	 * getKanjis
	 * Static method to get an array of kanjis from a data file
	 * 
	 * @param folder - the data file 
	 * @param fileType - distinguished between ".pot" - CASIA files and ".txt" - Collected files
	 * @return StrokeKanji[] the kanji array
	 */
	public static StrokeKanji[] getKanjis(File folder, String fileType){
		StrokeKanji[] kanjis = null;
		if (fileType == ".pot"){
			kanjis = new ChineseOnlineHandwritingToJPG().decodeAll(folder, false);}
		if (fileType == ".txt"){
			ArrayList<StrokeKanji> kanjiArray = new ImageProcessingUtils().textToStrokeKanji(folder.getAbsolutePath());
			kanjis = new StrokeKanji[kanjiArray.size()];
			kanjis = (StrokeKanji[]) kanjiArray.toArray(kanjis);}
		return kanjis;
	}

	//prints the Kanji and its strokes
	public String toString(){
		String mystring = "Kanji: [" + label;
		mystring += "] strokes: [";
		for (int stroke : reducedStrokes){
			if (stroke != 0){
				mystring += "" + (int)(stroke/25) + ", ";
			}
		}
		mystring = mystring.substring(0, mystring.length() - 2) + "]";

		return mystring;
	}

	/**
	 * buildAverage
	 * fills the average features for each stroke
	 * 
	 * @param allKanjiLengths
	 * @param allKanjiAngles
	 * @param allKanjiMoves
	 * @param allKanjiDistances 
	 */
	public void buildAverage(int[][] allKanjiLengths,
			int[][] allKanjiAngles, int[][] allKanjiMoves, int[][] allKanjiDistances) {

		avelengths = aveangles =new int[30];
		avemoves = new int[29];
		avedistances = new int[60];
		
		//for each stroke
		for (int strokenum = 0; strokenum < lengths.length; strokenum++){

			//init values
			int lengthsum = 0;
			int anglesum = 0;
			int movesum = 0;
			int xdistsum = 0;
			int ydistsum = 0;
			int kanjinum = 0;

			//sum the values for each features for each kanji
			for (kanjinum = 0; kanjinum < allKanjiLengths.length; kanjinum++){
				lengthsum += allKanjiLengths[kanjinum][strokenum];
				anglesum += allKanjiAngles[kanjinum][strokenum];
				xdistsum += allKanjiDistances[kanjinum][strokenum*2];
				ydistsum += allKanjiDistances[kanjinum][strokenum*2 + 1];
				if (strokenum < 29){
					movesum += allKanjiMoves[kanjinum][strokenum];
				}
			}

			//update the current values
			avelengths[strokenum] = lengthsum/(kanjinum+1);
			aveangles[strokenum] = anglesum/(kanjinum+1);
			avedistances[strokenum*2] = xdistsum/(kanjinum+1);
			avedistances[strokenum*2 + 1] = ydistsum/(kanjinum+1);
			if (strokenum < 29){
				avemoves[strokenum] = movesum/(kanjinum+1);
			}
		}

	}
	/**
	 * buildStandardDeviation
	 * fills the standard deviation for each feature for each stroke
	 * 
	 * @param allKanjiLengths
	 * @param allKanjiAngles
	 * @param allKanjiMoves
	 * @param allKanjiDistances 
	 */
	public StrokeKanji buildStandardDeviation(int[][] allKanjiLengths, int[][] allKanjiAngles, int[][] allKanjiMoves, int[][] allKanjiDistances) {
		//stdev = sqrt ( sum((x - xbar)^2)/N-1)

		lengthstdev = anglestdev =new int[30];
		movestdev = new int[29];
		distancesstdev = new int[60];
		
		int lengthsum = 0;
		int anglesum = 0;
		int movesum = 0;
		int xdistsum = 0;
		int ydistsum = 0;

		//sum((x - xbar)^2
		for (int strokenum = 0; strokenum < lengths.length; strokenum++){

			int kanjinum = 0;
			for (kanjinum = 0; kanjinum < allKanjiLengths.length; kanjinum++){

				lengthsum += (allKanjiLengths[kanjinum][strokenum] - avelengths[strokenum]) * (allKanjiLengths[kanjinum][strokenum] - avelengths[strokenum]);
				anglesum += (allKanjiAngles[kanjinum][strokenum] - aveangles[strokenum]) * (allKanjiAngles[kanjinum][strokenum] - aveangles[strokenum]);
				xdistsum += (allKanjiDistances[kanjinum][strokenum*2] - avedistances[strokenum*2]) * (allKanjiDistances[kanjinum][strokenum*2] - avedistances[strokenum*2]);
				ydistsum += (allKanjiDistances[kanjinum][strokenum*2 + 1] - avedistances[strokenum*2 + 1]) * (allKanjiDistances[kanjinum][strokenum*2 + 1] - avedistances[strokenum*2 + 1]);

				if (strokenum < 29){
					movesum += (allKanjiMoves[kanjinum][strokenum] - avemoves[strokenum]) * (allKanjiMoves[kanjinum][strokenum] - avemoves[strokenum]);
				}
			}

			//stdev = sqrt ( sum((x - xbar)^2)/N-1)

			lengthsum = (int) Math.sqrt(lengthsum / (kanjinum-1));
			anglesum = (int) Math.sqrt(anglesum / (kanjinum-1));
			movesum = (int) Math.sqrt(movesum / (kanjinum-1));
			xdistsum = (int) Math.sqrt(xdistsum / (kanjinum-1)); 
			ydistsum = (int) Math.sqrt(ydistsum / (kanjinum-1)); 

			//add the stdev to the correct array

			lengthstdev[strokenum] = lengthsum;
			anglestdev[strokenum] = anglesum;
			distancesstdev[strokenum*2] = xdistsum;
			distancesstdev[strokenum*2 + 1] = ydistsum;
			if (strokenum < 29){
				movestdev[strokenum] = movesum;
			}

		}

		return this;
	}

	/**
	 *distanceFromCenter
	 *
	 * @summary Computes the distance from a stroke's center to the center of a kanji
	 * 
	 * @return distance - an array of distances for each stroke
	 *
	 * @pseudocode
	 * 1. Find each stroke's midpoint
	 * 2. Find the average of each midpoint
	 * 3. return an array of differences of each midpoint to the average
	 */

	public int[] distanceFromCenter() {

		int i = 0;
		numstrokes = 0;
		int[] averageDistance = {0,0};
		
		while ((i + 3) < reducedStrokes.length && reducedStrokes[i] != 0) {

			//Find the midpoint of each stroke's x + y
			distances[i/2] = Math.abs(reducedStrokes[i] - reducedStrokes[i+2]);
			distances[(i/2) + 1] = Math.abs(reducedStrokes[i+1] - reducedStrokes[i+3]);

			//sum the midpoints to average later
			averageDistance[0] += distances[i/2];
			averageDistance[1] += distances[(i/2) + 1];

			i += 4;
			numstrokes += 1;
		}

//		System.out.println("x: " + averageDistance[0] + " y: " + averageDistance[1]);

		//average the midpoints
		averageDistance[0] = averageDistance[0]/numstrokes;
		averageDistance[1] = averageDistance[0]/numstrokes;

		//find each stroke's distance from the midpoint
		for (int j = 0; j < distances.length; j++){
			distances[j] = distances[j] / averageDistance[j%2];
		}

		return distances;

	}


	/**
	 * distance
	 * 
	 * @summary Computes a score based on how different one kanji is from this kanji using hardcoded weights
	 * 
	 * @param kanji - The pixels of the Kanji to Compare to
	 * @return sum - the difference score
	 * 
	 * @pseudocode
	 * 1. loop through each kanji's strokes
	 * 2. Sum the difference between lengths, angles, and moves
	 * 
	 * @note: moves is weighted by 100 since it is a more useful feature
	 * @note: there are no average values for KNN - avex[1] is the same as self.x[1]
	 */
	public double distance(StrokeKanji kanji) {
		
		distances = distanceFromCenter();
		kanji.distances = kanji.distanceFromCenter();
		
		int maxStrokes = 30;
		int weightLengths = 1;
		int weightAngles = 1;
		int weightDistances = 12;
		int weightMoves = 3;

		int sum = 0;
				
		int i = 0;
		while (i < maxStrokes){

			sum+= weightLengths * Math.abs(this.avelengths[i] - kanji.lengths[i]);

			sum+= weightAngles * Math.abs(this.aveangles[i] - kanji.aveangles[i]);	
			sum+= weightDistances * Math.abs(this.distances[i] - kanji.distances[i]);	

			//moves are the difference between strokes, so there will be one less maximum move than maximum stroke
			if (i < maxStrokes-1){
				sum+= weightMoves * Math.abs(this.avemoves[i] - kanji.moves[i]); //weighted 	
			}

			i++;
		}

		return sum;
	}		
	
	/**
	 * distance
	 * 
	 * @summary Computes a score based on how different one kanji is from this kanji using weight inputs
	 * 
	 * @param kanji - The pixels of the Kanji to Compare to
	 * @return sum - the difference score
	 * 
	 * @pseudocode
	 * 1. loop through each kanji's strokes
	 * 2. Sum the difference between lengths, angles, and moves
	 * 
	 * @note: moves is weighted by 100 since it is a more useful feature
	 * @note: there are no average values for KNN - avex[1] is the same as self.x[1]
	 */
	public double distance(StrokeKanji kanji, double weightLengths, double weightAngles, double weightDistances, double weightMoves){

		
		distances = distanceFromCenter();
		kanji.distances = kanji.distanceFromCenter();
		
		int maxStrokes = 30;
		
		int sum = 0;
				
		int i = 0;
		while (i < maxStrokes){

			sum+= weightLengths * Math.abs(this.avelengths[i] - kanji.lengths[i]);

			sum+= weightAngles * Math.abs(this.aveangles[i] - kanji.aveangles[i]);	
			sum+= weightDistances * Math.abs(this.distances[i] - kanji.distances[i]);	

			//moves are the difference between strokes, so there will be one less maximum move than maximum stroke
			if (i < maxStrokes-1){
				sum+= weightMoves * Math.abs(this.avemoves[i] - kanji.moves[i]); //weighted 	
			}

			i++;
		}

		return sum;
	}		
	/**
	 * getZScore
	 * Computes a zScore for a kanji
	 * 
	 * @param kanji - the kanji to score
	 * @return double zscore the score 
	 */
	public double getZScore(StrokeKanji kanji) {
		double z = 0;
		for (int i = 0; i < 30; i++){
			z+= Math.abs((double)kanji.lengths[i] - (double)avelengths[i])/ (double)(lengthstdev[i] + 1.0);
			z+= Math.abs((double)kanji.angles[i] - (double)aveangles[i])/ (double)(anglestdev[i] + 1.0);
			z+= Math.abs((double)kanji.distances[i * 2] - (double)avedistances[i * 2])/ (double)(distancesstdev[i * 2] + 1.0);
			z+= Math.abs((double)kanji.distances[i*2+1] - (double)distancesstdev[i*2+1])/ (double)(distancesstdev[i*2+1] + 1.0);
			if (i < 29){
				z+= Math.abs((double)kanji.moves[i] - (double)avemoves[i])/ (double)(movestdev[i] + 1.0);
			}
		}
		return z;
	}

	/**
	 * getZScore with weights
	 * Computes a zScore for a kanji
	 * 
	 * @param kanji - the kanji to score
	 * @return double zscore the score 
	 */
	public double getZScore(StrokeKanji kanji, double weightLengths, double weightAngles, double weightDistances, double weightMoves ) {
		double z = 0;
		for (int i = 0; i < 30; i++){
			z+= (Math.abs((double)kanji.lengths[i] - (double)avelengths[i])/ (double)(lengthstdev[i] + 1.0) * weightLengths);
			z+= (Math.abs((double)kanji.angles[i] - (double)aveangles[i])/ (double)(anglestdev[i] + 1.0))  * weightAngles;
			z+= (Math.abs((double)kanji.distances[i * 2] - (double)avedistances[i * 2])/ (double)(distancesstdev[i * 2] + 1.0)) * weightDistances;
			z+= (Math.abs((double)kanji.distances[i*2+1] - (double)distancesstdev[i*2+1])/ (double)(distancesstdev[i*2+1] + 1.0))  * weightDistances;
			if (i < 29){
				z+= (Math.abs((double)kanji.moves[i] - (double)avemoves[i])/ (double)(movestdev[i] + 1.0))  * weightMoves;
			}
		}
		return z;
	}

	
}
