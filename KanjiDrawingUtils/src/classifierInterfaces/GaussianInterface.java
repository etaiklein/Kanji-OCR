package classifierInterfaces;

import java.io.File;

import kanjiClasses.Kanji;

/**
 * GaussianInferface
 * 
 * An Interface to use statistical processing for Classification Using Gaussian Distributions
 * 
 * @author etaiklein
 * 6/7/14
 * 
 */

public interface GaussianInterface {
	
	public void updateDistribution(Kanji k);

	void test(File test, File train, String fileType);


}
