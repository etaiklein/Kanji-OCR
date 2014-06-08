package utils;
/**
 * GBK_Lookup
 * 
 * Parses an online GBK table for easy lookup
 * Etai Klein 5/1/14
 * 
 * The table can be found http://www.cs.nyu.edu/~yusuke/tools/unicode_to_gb2312_or_gbk_table.html
 * 
 * GBK is china's charset (like unicode)
 * 
 * Etai Klein
 * 6/7/14
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class ChineseCharacterLookup{
	
	private String originalFile = "GBKhtml.txt";
	private String table = "GBKtable.txt";
	
	
	/**
	 * Decode
	 * 
	 * @summary Parses the html file (GBKhtml.txt) to build a simple lookup table 
	 * 
	 * @pseudocode 
	 * 1.Create the new file
	 * 2.Write each GBK character and hex equivalent to the file
	 * 
	 * @return
	 */
	
	
	//Strips tags from the html
	@SuppressWarnings("resource")
	public String Decode(){

		//Step 1. File creation
		try{
			// the file to read from
			File input = new File(originalFile);
			// the file to write to
			File output = new File(table);
			
			// if file doesnt exists, then create it
			if (!output.exists()) {
				output.createNewFile();
			}

			//prepare writer
			FileWriter fw = new FileWriter(output.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			//prepare reader
			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(input),Charset.forName("UTF-8")));
			
			bw.write("[symbol] is above [GBKhex] for example, [ ] [20], [!] [21], [下] [cfc2] \nOriginal: http://www.cs.nyu.edu/~yusuke/tools/unicode_to_gb2312_or_gbk_table.html");
			
			//Step 2. Reach HTML characted by character
			int c = reader.read();
			while(c != -1) {
				
				//skip the insides of tags
				if ((char)c == '<'){
					while((c = reader.read()) != -1 && (char) c != '>'){}
					c = reader.read();
				}
				
				//write alphanumeric characters and HTML encoded characters (starting with &)
				if ((char)c != '<' && (Character.isAlphabetic((char) c) || Character.isDigit((char)c) || (char)c == '&')){
					
					//build the string- this should either be the GBK hex code or corresponding Hex Code or Unicode Bracket
					String mystring = "" + (char) c;
					while((c = reader.read()) != -1 && (char) c != '<'){
						
						if ((char) c != '*'){
						mystring += (char) c;}
					}
					
					//translate HTML code to characters
					mystring = StringEscapeUtils.unescapeHtml4(mystring) + "\n";

					//write the new string
					if (!mystring.startsWith("U")){
					bw.write(mystring);
					}
					
					//read the next character
				}else if ((char)c != '<'){
					c = reader.read();
				}
			}
			
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		} 

		return null;

	}
	
	/**
	 * Lookup
	 * 
	 * Looks up a hex code by a kanji or a kanji by a hex code
	 * 
	 * @param hex
	 * @return
	 * @throws IOException
	 * 
	 * @pseudocode
	 * 1. Find the index of the query
	 * 2. Return its match
	 * 
	 */
	
	public String Lookup(String hex) throws IOException{
		// read lines from the file
        List<String> filestring = FileUtils.readLines(new File(table));  
        
        // find the character corresponding to the hex code
        int index = (filestring.indexOf(hex) -1);
        
        // return the character if it exists
        return index > 0 ? filestring.get(index) : null;
	}
	
	public String Lookup(Character kanji) throws IOException{
		// read lines from the file
        List<String> filestring = FileUtils.readLines(new File(table)); 
        
        // find the character corresponding to the hex code
        int index = (filestring.indexOf(kanji)+1);
        
        // return the character if it exists
        return index > 0 ? filestring.get(index) : null;
	}

	
}
