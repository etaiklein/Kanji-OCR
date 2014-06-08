package utils;
/**
 * ChineseOnlineHanwritingToJPG.java
 * 
 * Program to decode Handwriting samples from the Chinese Handwriting Database
 * using http://www.nlpr.ia.ac.cn/events/CHRcompetition2013/Download/GNT-format.pdf
 * and turn them into images
 * 
 * Etai Klein
 * 6/7/14
 * 
 **/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import kanjiClasses.StrokeKanji;

import org.apache.commons.io.FileUtils;


public class ChineseOnlineHandwritingToJPG{

	int filenum = 0;
	int position = 0;
	SVGtoJPG jpgs = new SVGtoJPG();
	int samplesize;
	int starttag;
	int endtag;
	int[] strokes;
	StrokeKanji[] KanjiList;
	ChineseCharacterLookup gbkl;

	/**
	 * ChineseOnlineHandwritingToJPG constructor
	 * 
	 * Prepares the Character Lookup Table
	 */

	public ChineseOnlineHandwritingToJPG(){
		//load GBK table for lookup
		gbkl = new ChineseCharacterLookup();
		gbkl.Decode();
	}


	/**
	 * DecodeAll
	 * 
	 * Reads each kanji from the Database Byte Buffer and stores information about it
	 * 
	 * @return an array of Kanji objects
	 */

	public StrokeKanji[] decodeAll(File file, boolean writeToFile){
		//create a byte buffer from the file
		try {
			ByteBuffer bb = ByteBuffer.wrap(FileUtils.readFileToByteArray(file));
			bb.order(ByteOrder.LITTLE_ENDIAN);


			int i = 0;
			while (bb.remaining() > 0){
				decodeToStrokes(bb);
				i++;
				//				System.out.println(KanjiList[i-1]);
			}
			
			KanjiList = new StrokeKanji[i];
			position = 0;
			bb.rewind();
			i = 0;
			while (bb.remaining() > 0){
				KanjiList[i++] = decodeToStrokes(bb); //Records information about the Kanji

				//				System.out.println(KanjiList[i-1]);
			}

			if (writeToFile){
				bb.rewind();
				position = 0;
				while (bb.remaining() > 0){
					decodeToJPG(bb); //Writes JPG files
				}
			}


		} catch (IOException e) {e.printStackTrace();}

		return KanjiList;
	}

	/**
	 * Decode
	 * 
	 * Reads a single kanji from the Database Byte Buffer and writes information about it
	 */	
	public void decode(File file){

		try {
			ByteBuffer bb = ByteBuffer.wrap(FileUtils.readFileToByteArray(file));
			decodeToJPG(bb);

		} catch (IOException e) {e.printStackTrace();}
	}


	/**
	 * byteArrayToString
	 * 
	 * @param b A byte array
	 * @return A string in the form "[byte, byte .... ]"
	 */
	String byteArrayToString(byte[] b){
		String mystring = "[";
		for (byte bit : b){
			mystring += bit;
			mystring += " ";
		}
		mystring += "]";
		return mystring;
	}


	/**
	 * decodeToSVG
	 * 
	 * Writes the buffer as an SVG - this is a necessary step to get from the original format to a JPG file
	 * 
	 * 
	 * @param bb - the ByteBuffer containing all of the information about the Kanji
	 * @return Kanji - a kanji object containing the information about the SVG file
	 * 
	 * @pseudocode
	 * 1. Get the Kanji's code, size and number of strokes
	 * 2. Copy each stroke to a string in SVG format
	 * 3. Save the temporary SVG file
	 * 4. Transcode the file into a JPG
	 * 
	 * @discussion Why is it necessary to render as an SVG?
	 * The original format has the kanji start and end points for each stroke
	 *  at seemingly arbitrary points in space
	 *Using a scalar representation and then rendering it seemed like a good idea at the time.
	 *
	 */

	public StrokeKanji decodeToJPG(ByteBuffer bb){

		//Step 1. Get basic information about the kanji

		StrokeKanji k = new StrokeKanji();

		//put it in little_endian order
		bb.order(ByteOrder.LITTLE_ENDIAN);
		samplesize = (int) bb.getShort();
		int original = (bb.getChar());

		//lookup the tagcode in the gbk table
		try {k.label = gbkl.Lookup(Integer.toHexString(original)).charAt(0);
		} catch (IOException e) {e.printStackTrace();} 

		bb.getChar();
		k.numstrokes = bb.getShort();

		strokes = new int[(samplesize - (bb.position() - position)) / 2];		
		int i = 0;
		boolean firstSubstroke = true;
		int strokenum = 1;

		int currentX = 0;
		int currentY = 0;
		int maxX = 0;
		int maxY = 0;
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;

		//each SVG kanji has a style, path and end
		String viewBox;
		String style = "<g id= \"" + k.label + "\" file = \"" + filenum + "\" style=\"fill:none;stroke:#000000;stroke-width:5;stroke-linecap:round;stroke-linejoin:round;\">";
		String path = "<path id = \"" + k.label + "_" + strokenum + "\" d = \"";
		String end = "</g></svg> ";

		//Step 2. Get the range of each stroke. Add the scaled start and end points to the SVG string

		//For each stroke, get it's maximum range to better scale it later

		while((bb.position() - position) < samplesize){
			strokes[i++] = bb.getShort();

			if (i > 1 && i % 2 == 0 && strokes[i-2] != -1 && strokes[i-1] != -1){
				currentX = strokes[i-2];
				currentY = strokes[i-1];

				//get mins + maxes
				maxX = Math.max(currentX, maxX);
				maxY = Math.max(currentY, maxY);
				minX = Math.min(currentX, minX);
				minY = Math.min(currentY, minY);

			}
		}

		//For each stroke, find the XY start and end coordinate and add it to my path

		i = 0;
		while (i < strokes.length){
			i++;
			if (i > 1 && i % 2 == 0 && strokes[i-2] != -1 && strokes[i-1] != -1){
				currentX = strokes[i-2];
				currentY = strokes[i-1];

				//M = Moveto, L = Lineto. 
				if (firstSubstroke){path += "M"; firstSubstroke = false;}
				else {path += "L";}

				path += (currentX - minX) + " " + (currentY - minY) + " ";

			}

			//end stroke path and start new paths for new strokes
			if (i > 2 && strokes[i-3] == -1 && strokes[i-2] == 0){
				path += " \" />";				
				//if there is a new stroke, mark it to the path
				if (strokes[i-1] != -1){
					firstSubstroke = true;
					path += "\n<path id = \"" + k.label + "_" + ++strokenum + "\" d = \"";
				}
			}		

		}

		//scale the SVG using the min and max
		viewBox = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox = \"" + -5 + " " + -5 + " " + (maxX - minX + 10) + " " + (maxY - minY + 10) + "\" version = \"1.1\">";

		bb.position(position + samplesize);
		position = bb.position();

		k.svg = viewBox + "\n" + style + "\n" + path + "\n" + end;

		//Step 3. write the svg to a temporary file
		try {
			File temp = new File("temp.svg");
			// if file doesnt exists, then create it
			if (!temp.exists()) {
				temp.createNewFile();
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(temp.getAbsoluteFile()));
			bw.write(k.svg);
			bw.close();

		} catch (IOException e) {e.printStackTrace();}

		//Step 4. Transcode the file

		if (this.filenum < 51){
			jpgs.transcode("temp.svg", "./kanjiSVGtraining/" + String.valueOf(k.label) + this.filenum + ".jpg");
		}else{
			jpgs.transcode("temp.svg", "./kanjiSVGtest/" + String.valueOf(k.label) + this.filenum + ".jpg");
		}

		//clean up
		new File("temp.svg").delete();

		return k;

	}

	/**
	 * decodeToStrokes
	 * 
	 * Reads stroke data about a kanji and saves it in a Kanji Object. 
	 * The point is to use these features for later classification 
	 * 
	 * @param bb - The byte buffer containing the information about the Kanji
	 * @return Kanji - a Kanji object
	 * 
	 * @pseudocode
	 * 1. read the tag code and the number of strokes
	 * 2. read in every stroke
	 * 3. process to get angles
	 * 
	 */

	//overwrite - pass any byte buffer
	public StrokeKanji decodeToStrokes(ByteBuffer bb){

		//Step 1. Read in the tag code

		StrokeKanji k = new StrokeKanji();

		//put it in little_endian order
		bb.order(ByteOrder.LITTLE_ENDIAN);
		samplesize = (int) bb.getShort();
		int original = (bb.getChar());

		//lookup the tagcode in the gbk table
		try {k.label = gbkl.Lookup(Integer.toHexString(original)).charAt(0);
		} catch (IOException e) {e.printStackTrace();} 

		bb.getChar();
		//		System.out.println((char)original + "tagcode: "+ k.tagCode + " byte array: " + toByteArray(k.tagCode.getBytes()));
		k.numstrokes = bb.getShort();

		strokes = new int[(samplesize - (bb.position() - position)) / 2];		
		//there will never be more than 30 strokes per kanji
		k.reducedStrokes = new int[120];
		k.lengths = new int[30];
		k.angles = new int[30];
		k.moves = new int[29];

		int i = 0;
		int g = 0;

		//Step 2. Log the start and end locations of each stroke

		while((bb.position() - position) < samplesize){
			strokes[i++] = bb.getShort();

			//log the first 2 strokes
			if (i == 2){
				//startx
				k.reducedStrokes[g++] = strokes[i-2];
				//starty
				k.reducedStrokes[g++] = strokes[i-1];
			}

			//log start strokes (valid strokes following -1,0)
			if (i > 4 && strokes[i-4] == -1 && strokes [i-3] == 0 && strokes[i-2] != -1 && strokes[i-1] != -1){
				k.reducedStrokes[g++] = strokes[i-2];
				k.reducedStrokes[g++] = strokes[i-1];
			}

			//log end strokes (valid strokes before -1,0)
			if (i > 4 && strokes[i-1] == -1 && strokes[i-2] != -1 && strokes[i-3] != -1){
				k.reducedStrokes[g++] = strokes[i-3];
				k.reducedStrokes[g++] = strokes[i-2];
			}

			//System.out.println("startx: " + strokestartend[g-2] + " starty: " + strokestartend[g-1]);
			//System.out.println("endx: " + strokestartend[g-2] + " endy: " + strokestartend[g-1]);
			//System.out.println("samplessize: " + samplesize + " tagcode: " + tagCode + " numstrokes: " + numstrokes + " position: " + bb.position() + "stroke: " + strokes [i-1]);
		}

		//Step 3. Process strokes to get angles and lengths

		for (int s = 0; s < 30; s++){
			int startx = k.reducedStrokes[s*4];
			int starty = k.reducedStrokes[s*4 + 1];
			int endx = k.reducedStrokes[s*4 + 2];
			int endy = k.reducedStrokes[s*4 + 3];

			//	System.out.println("stroke: " + s + "startx: " + startx + "starty: " + starty + "endx: " + endx + "endy: " + endy);

			int deltaX = startx - endx;
			int deltaY = starty - endy;
			double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
			double angleInDegrees = Math.atan2(deltaY, deltaX) * 180 / Math.PI;
			//System.out.println("deltax: " + deltaX + " deltay: " + deltaY + " angle: " + angleInDegrees + " length: " + length);

			k.angles[s] = (int) angleInDegrees;
			k.lengths[s] = (int) length;
			
			if (s!= 0){
				int lastendx = k.reducedStrokes[(s-1)*4 + 2];
				int lastendy = k.reducedStrokes[(s-1)*4 + 3];
				int strokeDeltaX = lastendx - startx;
				int strokeDeltaY = lastendy - starty;
				k.moves[s-1] = (int) (Math.atan2(strokeDeltaY, strokeDeltaX) * 180 / Math.PI);
			}
		}

		bb.position(position + samplesize);
		position = bb.position();

		//return the kanji object
		return k;

	}


	/**
	 * processKanji
	 * 
	 * Produces a text file with stroke and byte information about the Kanji
	 * 
	 * @param input - The location of the byte buffer (.pot files)
	 * @param output - The location of the output txt file
	 * @param num - the number of files to look at.
	 * 
	 * 
	 */

	public void processKanji(String input, String output, int num){
		ChineseOnlineHandwritingToJPG dh = new ChineseOnlineHandwritingToJPG();

		//testing
		File folder = new File(input);

		//increasing filenum limits the number of items to import
		dh.filenum = 60 - num;

		try{
			File out = new File(output);
			// if file doesnt exists, then create it
			if (!out.exists()) {
				out.createNewFile();
			}

			FileWriter fw = new FileWriter(out.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (final File fileEntry : folder.listFiles()) {
				if (fileEntry.getName().endsWith(".pot")){

					dh.position = 0;
					//decode information about each kanji
					//boolean decides whether or not to write a jpg file for each as well.
					dh.decodeAll(fileEntry, false);

					//write that information to the file
					for (StrokeKanji k : dh.KanjiList){
						if (k != null){
							bw.write("" + k + "\n");
						}
					}
				}	
			}	
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}