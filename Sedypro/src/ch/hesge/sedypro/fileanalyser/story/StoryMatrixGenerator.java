package ch.hesge.sedypro.fileanalyser.story;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.hesge.sedypro.utils.DictionaryEntry;
import ch.hesge.sedypro.utils.DictionaryUtils;
import ch.hesge.sedypro.utils.Matrix;
import ch.hesge.sedypro.utils.StringUtils;
import ch.hesge.sedypro.utils.Vector;

public class StoryMatrixGenerator {

	private Matrix outputMatrix;
	private List<String> matchingWords;

	/**
	 * Default constructor
	 */
	public StoryMatrixGenerator() {
	}
	
 	/**
	 * Generate the story matrix as follow:
	 * 
	 * 
	 * 			   step 1		|  x x x  |
	 * 	 		   step 2		|  x x x  |   
	 * 	 matrix =  step 3	m	|  x x x  |
	 * 			   step 4		|  x x x  |
	 * 			   step 5		|  x x x  |
	 * 			        			 ---------
	 *                   	         n
	 *           
	 *      m = row count, n = column count
	 *      
	 *      m = story step count
	 *      n = distinct terms count in story (that intersect with trace words)
	 *                   
	 *  So, one row represents all words used for a single story step, among
	 *  all available trace words. 
 	 * 
 	 * @throws IOException 
 	 */
 	public void doGenerate(String storyDictionaryFilename, String traceDictionaryFilename) throws IOException {
 		
 		// Retrieve input/output paths
 		Path storyDictionaryPath = Paths.get(storyDictionaryFilename).toAbsolutePath().normalize();
 		Path traceDictionaryPath = Paths.get(traceDictionaryFilename).toAbsolutePath().normalize();

		// Load intersection between story/trace dictionary
 		matchingWords = new ArrayList<>(DictionaryUtils.loadIntersectionSet(storyDictionaryPath, traceDictionaryPath));
 		
		// Load required dictionaries
 		Map<String, DictionaryEntry> storyDictionary = DictionaryUtils.loadDictionary(storyDictionaryPath);
 		Map<Integer, Set<String>> storyLines = DictionaryUtils.getLineToWordMap(storyDictionary);

 		// Create the output matrix
		outputMatrix = new Matrix(storyLines.size(), matchingWords.size());
 		
		List<String> matchingWordList = new ArrayList<>(matchingWords);
		
 		// Scan all story lines and all words in each line
 		for(int i = 0; i < storyLines.size(); i++) {
			for(int j = 0; j < matchingWordList.size(); j++) {
				boolean isPresentInTrace = storyLines.get(i+1).contains(matchingWordList.get(j));
				outputMatrix.setValue(i, j, isPresentInTrace ? 1.0 : 0.0);
			}
 		}
 	}
 	
	/**
	 * Write the matrix to file
	 * 
	 * @param matrixFilename
	 * @throws IOException
	 */
	public void saveMatrix(String matrixFilename) throws IOException {

		Path matrixPath = Paths.get(matrixFilename).toAbsolutePath().normalize();

		// If file already exists, suppress it
		if (matrixPath.toFile().exists()) {
			Files.delete(matrixPath);
		}

		try ( PrintWriter writer = new PrintWriter(new FileWriter(matrixPath.toFile())) ) {

			for(int i = 0; i < outputMatrix.getRowSize(); i++) {

				Vector outputRow = outputMatrix.getRow(i);
				String outputString = StringUtils.toString(outputRow, ",");
				writer.println(outputString);
			}
		}
	} 	

	/**
	 * Write the matrix to file
	 * 
	 * @param matrixFilename
	 * @throws IOException
	 */
	public void saveMatrixWithHeaders(String matrixFilename) throws IOException {

		Path matrixPath = Paths.get(matrixFilename).toAbsolutePath().normalize();

		// If file already exists, suppress it
		if (matrixPath.toFile().exists()) {
			Files.delete(matrixPath);
		}

		try ( PrintWriter writer = new PrintWriter(new FileWriter(matrixPath.toFile())) ) {

			// Write matrix story headers.
			String headerLine = "," + StringUtils.toString(matchingWords, ",");
			writer.println(headerLine);
			
			for(int i = 0; i < outputMatrix.getRowSize(); i++) {

				String outputString = "";
				
				// Write matrix trace headers
				outputString += String.format("S%1$04d,", i+1);

				// Write matrix row
				Vector outputRow = outputMatrix.getRow(i);
				outputString += StringUtils.toString(outputRow, ",");
				
				writer.println(outputString);
			}
		}
	} 	
}
