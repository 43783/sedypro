package ch.hesge.sedypro.fileanalyser.storytrace;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.hesge.sedypro.utils.DictionaryEntry;
import ch.hesge.sedypro.utils.DictionaryUtils;
import ch.hesge.sedypro.utils.Matrix;
import ch.hesge.sedypro.utils.StatisticUtils;
import ch.hesge.sedypro.utils.StringUtils;
import ch.hesge.sedypro.utils.Vector;

public class ScoreMatrixGenerator2 {

	private Matrix outputMatrix;
	private Map<String, DictionaryEntry> storyDictionary;
	private Map<String, DictionaryEntry> traceDictionary;
	private List<String> wordList;

	/**
	 * Default constructor
	 */
	public ScoreMatrixGenerator2() {
	}

 	/**
	 * Generate the story/trace matching matrix as follow:
	 * 
	 * 							trace-steps
	 * 
	 * 			   story 1		|  x x x  |
	 * 	 		   story 2		|  x x x  |   
	 * 	 matrix =  story 3	m	|  x x x  |
	 * 			   story 4		|  x x x  |
	 * 			   story 5		|  x x x  |
	 * 			        			 ---------
	 *                   	         n
	 *           
	 *      m = number of story steps
	 *      n = number of trace steps
	 *                   
	 *  So, a cell (i,j) contains the matching score for terms present in both story and trace.
 	 * 
 	 * @throws IOException 
 	 */
 	public void doGenerate(String storyMatrixFilename, String traceMatrixFilename, String storyDictionaryFilename, String traceDictionaryFilename) throws IOException {
 		
		// Retrieve input/output paths
 		Path storyMatrixPath = Paths.get(storyMatrixFilename).toAbsolutePath().normalize();
 		Path traceMatrixPath = Paths.get(traceMatrixFilename).toAbsolutePath().normalize();
 		Path storyDictionaryPath = Paths.get(storyDictionaryFilename).toAbsolutePath().normalize();
 		Path traceDictionaryPath = Paths.get(traceDictionaryFilename).toAbsolutePath().normalize();

 		// Load dictionaries, matrices and words used in matrix
		Matrix storyMatrix = DictionaryUtils.loadMatrix(storyMatrixPath);
		Matrix traceMatrix = DictionaryUtils.loadMatrix(traceMatrixPath);
		
		// Load intersection between story/trace dictionary
		storyDictionary = DictionaryUtils.loadDictionary(storyDictionaryPath);
		traceDictionary = DictionaryUtils.loadDictionary(traceDictionaryPath);
 		wordList = new ArrayList<>(DictionaryUtils.getIntersectionSet(storyDictionary,  traceDictionary));

		// Retrieve inverse terms frequency among all traces
		Vector idfVector = StatisticUtils.getTraceIdfVector(traceMatrix);

 		// Retrieve all original words that have matched
		List<Set<String>> originalWords = new ArrayList<>();
		for(int i = 0; i < wordList.size(); i++) {
			if (storyDictionary.containsKey(wordList.get(i))) {
				originalWords.add(storyDictionary.get(wordList.get(i)).getOriginalWords().keySet());
			}
		}

 		// Final matrix, each row is associated to story and each column to trace (story x trace)
		outputMatrix = new Matrix(storyMatrix.getRowSize(), traceMatrix.getRowSize());
		
		// Compute score vector for each story step
		for(int i = 0; i < storyMatrix.getRowSize(); i++) {
			
			Vector storyVector = storyMatrix.getRow(i);
			
			Matrix matchingMatrix = computeMatchingMatrix(storyVector, traceMatrix, idfVector, originalWords);
			Vector scoreVector    = computeScoreVector(matchingMatrix);
			
			outputMatrix.setRow(i, scoreVector.normalize(100));
		}
 	}

 	/**
 	 * Compute a story matrix defining all matching words for 
 	 * current story with all trace steps.
 	 * 
 	 * @param storyVector
 	 * @param traceMatrix
 	 * @param idfVector
 	 * @return
 	 */
 	private Matrix computeMatchingMatrix(Vector storyVector, Matrix traceMatrix, Vector idfVector, List<Set<String>> originalWords) {
 		
		Matrix matchingMatrix = new Matrix(storyVector.size(), traceMatrix.getRowSize());

		// Scan all trace rows
		for(int i = 0; i < traceMatrix.getRowSize(); i++) {
			
			Vector traceVector = traceMatrix.getRow(i);
			Vector matchingVector = storyVector.ebeMultiply(traceVector).ebeMultiply(idfVector);
			
			// Aggregate weight for same original word matching
			for(int k = 0; k < matchingVector.size(); k++) {
				for(int l = 0; l < matchingVector.size(); l++) {
					if (k != l && matchingVector.getValue(k) > 0 && matchingVector.getValue(l) > 0) {
					
						// Check if original words intersect
						Set<String> intersection = new HashSet<String>(originalWords.get(k));
						intersection.retainAll(originalWords.get(l));
						
						// If intersection, take most favorable weight
						if (intersection.size() > 0) {
							double weight = Math.max(matchingVector.getValue(k), matchingVector.getValue(l));
							matchingVector.setValue(k, weight);
							matchingVector.setValue(l, 0);
						}
					}
				}
			}
			
			matchingMatrix.setColumn(i, matchingVector);
		}
		
		return matchingMatrix;
 	}
 	
 	/**
 	 * Compute vector score associated to a story matching matrix.
 	 * 
 	 * @param storyMatchingMatrix
 	 * @return
 	 */
 	private Vector computeScoreVector(Matrix matchingMatrix) {
 		
 		int windowSize = 7;
 		
 		// Adjust window-size and calculate its half-size
 		windowSize += windowSize % 2 == 0 ? 1 : 0;
 		int windowHalfSize = windowSize / 2;

 		Vector scoreVector = new Vector(matchingMatrix.getColumnSize());

 		// Expand matrix to the left and right from window-size
 		Matrix scoreMatrix = new Matrix(matchingMatrix.getRowSize(), matchingMatrix.getColumnSize() + windowSize - 1);
 		for(int i = 0; i < matchingMatrix.getColumnSize(); i++) {
 			scoreMatrix.setColumn(i + windowHalfSize, matchingMatrix.getColumn(i));
 		}

 		for(int i = windowHalfSize; i < scoreMatrix.getColumnSize() - windowHalfSize; i++) {
 			
 	 		double meanScore = 0;

 	 		for (int j = i - windowHalfSize; j < i + windowHalfSize + 1; j++) {
 	 	 		for(int k = 0; k < scoreMatrix.getRowSize(); k++) {
 	 				meanScore += scoreMatrix.getValue(k, j);
 	 	 		}
 			}
 	 		
 	 		meanScore /= scoreMatrix.getRowSize() * windowSize;
 			scoreVector.setValue(i - windowHalfSize, meanScore);
 		}
 					
 		return scoreVector;
 	}
 	
 	/**
	 * Write the matrix in csv format
	 * 
	 * @param excelFilename
	 * @throws IOException
	 */
	public void saveMatrixToExcel(String excelFilename) throws IOException {

 		Path excelPath = Paths.get(excelFilename).toAbsolutePath().normalize();
		
		// If file already exists, suppress it
		if (excelPath.toFile().exists()) {
			Files.delete(excelPath);
		}

		try ( PrintWriter writer = new PrintWriter(new FileWriter(excelPath.toFile())) ) {

			for(int i = 0; i < outputMatrix.getRowSize(); i++) {

				// Write matrix row
				Vector outputRow = outputMatrix.getRow(i);
				String outputString = StringUtils.toString(outputRow, ",");
				
				writer.println(outputString);
			}
		}
	}

	/**
	 * Write the matrix in csv format
	 * 
	 * @param excelFilename
	 * @throws IOException
	 */
	public void saveMatrixWithHeadersToExcel(String excelFilename) throws IOException {

 		Path excelPath = Paths.get(excelFilename).toAbsolutePath().normalize();
		
		// If file already exists, suppress it
		if (excelPath.toFile().exists()) {
			Files.delete(excelPath);
		}

		try ( PrintWriter writer = new PrintWriter(new FileWriter(excelPath.toFile())) ) {

			// Write matrix story headers.
			String headerLine = StringUtils.repeat(" ",  5) + ",";
			for(int i = 1; i < outputMatrix.getColumnSize()+1; i++) {
				headerLine += String.format("T%1$04d,", i);
			}
			headerLine = StringUtils.removeLastChar(headerLine);
			writer.println(headerLine);
			
			for(int i = 0; i < outputMatrix.getRowSize(); i++) {

				// Write matrix trace headers
				String outputString = String.format("S%1$04d,", i+1);
				
				// Write matrix row
				Vector outputRow = outputMatrix.getRow(i);
				outputString += StringUtils.toString(outputRow, ",");
				
				writer.println(outputString);
			}
		}
	}	
}
