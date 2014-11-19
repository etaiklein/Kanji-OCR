package classifierInterfaces;
/**
 * KNNInferface
 * 
 * An Interface to use statistical processing for Classification using K Nearest Neighbors
 * 
 * @author etaiklein
 * 6/7/14
 * 
 */

import java.io.File;
import kanjiClasses.Kanji;

public interface KNNInterface {

	int test(File train, File test, String fileType);

	void train(Kanji k);

}
