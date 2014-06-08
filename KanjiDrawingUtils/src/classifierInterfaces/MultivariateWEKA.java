package classifierInterfaces;
/**
 * MultivariateGaussianInferface
 * 
 * An Interface to use the Weka Library for Multivariate Processing
 * 
 * Etai Klein
 * 6/7/14
 * 
 */

import java.io.File;

import weka.classifiers.Classifier;
import weka.core.Instances;

public interface MultivariateWEKA {
	
	public Instances KanjiToARFF(String path, String fileType);

	public void test(Classifier cls, File train, File test, String fileType, String name);
}
