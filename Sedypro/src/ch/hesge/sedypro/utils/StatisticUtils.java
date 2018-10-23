package ch.hesge.sedypro.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is a generic utility class for information retrieval.
 * 
 * Copyright HEG Geneva 2014, Switzerland
 * 
 * @author Eric Harth
 */

public class StatisticUtils {

 	/**
 	 * Compute the inverse document frequency vector among all trace matrix.
 	 * That is among all trace words, compute relevant score (rare term are scored higher than frequent ones).
 	 * 
 	 * N = total number of document
 	 * nt = number of document containing term t
 	 * 
 	 * idf :                 log( N/nt ) = -log( nt/N )
 	 * idf-smooth:           log( 1 + N/nt )
 	 * idf-probabilistic:    log( (N-nt) / nt )
 	 * 
 	 * idf-custom:           1 - nt / N		=>		between [0, 1] (0 = frequent, 1 = rare)
 	 * 
 	 * @param traceMatrix
 	 * @return a vector
 	 */
 	public static Vector getTraceIdfVector(Matrix traceMatrix) {
 		
		Vector tfVector = new Vector(traceMatrix.getColumnSize());

		// Calculate the term frequency vector (tf)
		for (int i = 0; i < traceMatrix.getRowSize(); i++) {
			tfVector = tfVector.add(traceMatrix.getRow(i));
		}

		Vector idfVector = new Vector(traceMatrix.getColumnSize());

		// Calculate the inverse term frequency vector (idf)
		for (int i = 0; i < traceMatrix.getColumnSize(); i++) {
			double N  = traceMatrix.getRowSize();
			double nt = tfVector.getValue(i);
			
//			double idfValue = Math.log(N/nt); 
//			double idfValue = -Math.log(nt/N); 
//			double idfValue = Math.log(1 + N / nt); 
//			double idfValue = Math.log( (N-nt) / nt); 
//			double idfValue = 1 - nt / N;
			
			double idfValue = 1 - nt / N;
			idfVector.setValue(i, idfValue);
		}
		
		return idfVector;
 	}
 	
 	/**
 	 * Retrieve all weight associated to term present in dictionary.
 	 * 
 	 * @param dictionary
 	 * @return
 	 */
 	public static  Vector getWordWeights(List<String> dictionary) {

 		Vector wordWeights = new Vector(dictionary.size());
 		
 		for (int i = 0; i < dictionary.size(); i++) {

 			String word = dictionary.get(i);

 			double weight = 0;
 
			// Select weight according to word type
			if (word.contains("s:")) {
				weight = 5;
			}
			else if (word.contains("a:")) {
				weight = 8;
			}
			else if (word.contains("c:")) {
				weight = 3;
			}
			
			wordWeights.setValue(i, weight);
 		}
 		
 		return wordWeights;
 	} 	
 	
 	/**
 	 * Retrieve the matching matrix of all words present in story and trace matrix for a specific story index.
 	 * 
	 * 							matching words
	 * 
	 * 			   trace 1		|  0 0 0  |
	 * 	 		   trace 2		|  1 0 0  |   
	 * 	 matrix =  trace 3	m	|  0 2 0  |
	 * 			   trace 4		|  0 0 0  |
	 * 			   trace 5		|  0 0 3  |
	 * 			        			 ---------
	 *                   	         n
	 *           
	 *      m = number of trace steps
	 *      n = number of matching words (in both trace and step)
	 *      
	 *      if n = 1 => current matching word is a subject
	 *      if n = 2 => current matching word is an action
	 *      if n = 3 => current matching word is a complement
	 *      
	 *      
 	 * @param storyMatrix
 	 * @param traceMatrix
 	 * @param wordsDictionary
 	 * @param storyIndex
 	 * @return
 	 */
 	public static Matrix getMatchingMatrix(Matrix storyMatrix, Matrix traceMatrix, List<String> wordsDictionary, int storyIndex) {
 		
 		Matrix matchingMatrix = new Matrix(traceMatrix.getRowSize(), traceMatrix.getColumnSize());
 		
 		// Retrieve current story-vector
		Vector storyVector = storyMatrix.getRow(storyIndex);

		for (int i = 0; i < matchingMatrix.getRowSize(); i++) {
			
			Vector traceVector = traceMatrix.getRow(i);

			// Retrieve matching terms in story and trace
			Vector matchingVector = storyVector.ebeMultiply(traceVector);
			
			// Build column values according to word type
			for (int j = 0; j < matchingVector.size(); j++) {
				if (matchingVector.getValue(j) > 0) {
					
					String matchingWord = wordsDictionary.get(j);
					
					if (matchingWord.startsWith("s:")) {
						matchingMatrix.setValue(i, j, 1);
					}
					
					if (matchingWord.startsWith("a:")) {
						matchingMatrix.setValue(i, j, 2);
					}

					if (matchingWord.startsWith("c:")) {
						matchingMatrix.setValue(i, j, 3);
					}
				}
			}
		}

		return matchingMatrix;
 	}
 		
 	/**
 	 * Compute vector score associated to a story matching matrix.
 	 * Notes. this method is obsolete and not used anymore.
 	 * 
 	 * @param matchingMatrix
 	 * @return
 	 */
 	public static Vector computeSentenceScoreVector(Matrix matchingMatrix, List<String> wordList, int windowSize) {
 		
 		// Adjust window-size and calculate its half-size
 		windowSize += windowSize % 2 == 0 ? 1 : 0;
 		int windowHalfSize = windowSize / 2;

 		Vector scoreVector = new Vector(matchingMatrix.getColumnSize());

 		// Expand matrix to the left and right from window-size
 		Matrix scoreMatrix = new Matrix(matchingMatrix.getRowSize(), matchingMatrix.getColumnSize() + windowSize - 1);
 		for(int i = 0; i < matchingMatrix.getColumnSize(); i++) {
 			scoreMatrix.setColumn(i + windowHalfSize, matchingMatrix.getColumn(i));
 		}

 		// Scan each trace steps
 		for(int i = windowHalfSize; i < scoreMatrix.getColumnSize() - windowHalfSize; i++) {
 			
			// Scan potential subjects
 			for (int subjectIndex = i - windowHalfSize; subjectIndex < i + windowHalfSize + 1; subjectIndex++) {
	 			for(int k1 = 0; k1 < scoreMatrix.getRowSize(); k1++) {
	 				if (wordList.get(k1).startsWith("s") && scoreMatrix.getValue(k1, subjectIndex) > 0) {
	 					
	 					// Scan potential actions
			 			for (int actionIndex = i - windowHalfSize; actionIndex < i + windowHalfSize + 1; actionIndex++) {
				 			for(int k2 = 0; k2 < scoreMatrix.getRowSize(); k2++) {
				 				if (wordList.get(k2).startsWith("a") && scoreMatrix.getValue(k2, actionIndex) > 0) {
				 					
					 				boolean isFullSentenceMatch = false;

					 				// Scan potential complements
						 			for (int complementIndex = i - windowHalfSize; complementIndex < i + windowHalfSize + 1; complementIndex++) {
							 			for(int k3 = 0; k3 < scoreMatrix.getRowSize(); k3++) {
							 				if (wordList.get(k3).startsWith("c") && scoreMatrix.getValue(k3, complementIndex) > 0) {
							 					
									 			// ==> full sentence matching, check that sentence has at least some terms centered in i
								 				if (subjectIndex == i || actionIndex == i || complementIndex == i) {
									 				isFullSentenceMatch = true; 
							 	 					double newScore = computeSentenceScore(scoreMatrix, windowSize, i, subjectIndex, actionIndex, complementIndex, k1, k2, k3);
							 						scoreVector.setValue(i - windowHalfSize, scoreVector.getValue(i - windowHalfSize) + newScore);
								 				}
							 				}
							 			}
						 			}

						 			// ==> only partial sentence matching, check that sentence has at least some terms centered in i
						 			if (!isFullSentenceMatch && (subjectIndex == i || actionIndex == i)) {
				 	 					double newScore = computeSentenceScore(scoreMatrix, windowSize, i, subjectIndex, actionIndex, -1, k1, k2, -1);
				 						scoreVector.setValue(i - windowHalfSize, scoreVector.getValue(i - windowHalfSize) + newScore);
					 				}
				 				}
				 			}
			 			}
	 				}
	 			}
 			}
 		}
 					
 		return scoreVector;
 	}
 	
 	/**
 	 * Compute sentence score.
 	 * Notes. this method is obsolete and not used anymore.
 	 * 
 	 * @param scoreMatrix
 	 * @param windowSize
 	 * @param traceIndex
 	 * @param subjectIndex
 	 * @param actionIndex
 	 * @param complementIndex
 	 * @param k1
 	 * @param k2
 	 * @param k3
 	 * @return
 	 */
 	private static double computeSentenceScore(Matrix scoreMatrix, int windowSize, int traceIndex, int subjectIndex, int actionIndex, int complementIndex, int k1, int k2, int k3) {

 		int sentencePosition = 0;
 		
		// Compute sentence distance to current trace index
		sentencePosition = (subjectIndex + actionIndex + (k3 == -1 ? 0 : complementIndex)) / (k3 == -1 ? 2 : 3);
 		
		// Compute score for current sentence
 		double sentenceDistance = Math.abs(traceIndex - sentencePosition);
 		
 		// Retrieve subject/action/complement weights
 		double subjectWeight    = scoreMatrix.getValue(k1, subjectIndex);
 		double actionWeight     = scoreMatrix.getValue(k2, actionIndex);
 		double complementWeight = k3 == -1 ? 0 : scoreMatrix.getValue(k3, complementIndex);
 		
		return (actionWeight + subjectWeight + complementWeight) * (1 - sentenceDistance / windowSize);
 	}
 	
 	/**
 	 * Based on score matrix, compute, for each story the mapping regions.
 	 * 
 	 * @param scoreMatrix
 	 * @return
 	 */
 	public static Map<Integer, List<MappingRegion>> computeMappingRegions1(Matrix scoreMatrix) {
 		
		Map<Integer, List<MappingRegion>> mappingRegions = new TreeMap<>();
		
		// Prepare mapping region for each story step
		for(int i = 0; i < scoreMatrix.getRowSize(); i++) {
			mappingRegions.put(i, new ArrayList<>());
		}

		// Retrieve all mapping regions for each story step
		for(int i = 0; i < scoreMatrix.getRowSize(); i++) {

			Vector scoreVector = scoreMatrix.getRow(i);
			
			// Compute trigger used to select a mapping
			double triggerValue = scoreVector.getL1Norm() / scoreVector.size();
	 		triggerValue += (100 - triggerValue) / 2;

			Vector mappingVector = new Vector(scoreVector.size());

			// Compute mapping vector for current story
			for(int j = 0; j < mappingVector.size(); j++) {
				mappingVector.setValue(j, scoreVector.getValue(j) < triggerValue ? 0 : 1);
			}
			
			List<MappingRegion> regions = convertVectorToRegions(i, mappingVector);
			mappingRegions.get(i).addAll(regions);
		}
		
		return mappingRegions;
 	}

 	/**
 	 * Based on score matrix, compute, for each story the mapping regions.
 	 * 
 	 * @param scoreMatrix
 	 * @return
 	 */
 	public static Map<Integer, List<MappingRegion>> computeMappingRegions2(Matrix scoreMatrix) {
 		
		Map<Integer, List<MappingRegion>> mappingRegions = new TreeMap<>();
		
		// Prepare mapping region for each story step
		for(int i = 0; i < scoreMatrix.getRowSize(); i++) {
			mappingRegions.put(i, new ArrayList<>());
		}

		// Retrieve all mapping regions for each story step
		for(int i = 0; i < scoreMatrix.getRowSize(); i++) {

			Vector scoreVector = scoreMatrix.getRow(i);
			Vector mappingVector = new Vector(scoreVector.size());
			
			// Compute trigger used to select a mapping
			double triggerValue = scoreVector.getL1Norm() / scoreVector.size();
			
	 		// Compute mapping vector for current story
			for(int j = 0; j < mappingVector.size(); j++) {
				mappingVector.setValue(j, scoreVector.getValue(j) < triggerValue ? 0 : 1);
			}
			
			List<MappingRegion> regions = convertVectorToRegions(i, mappingVector);
			mappingRegions.get(i).addAll(regions);
		}
		
		return mappingRegions;
 	}

 	/**
 	 * Based on score matrix, compute, for each story the mapping regions.
 	 * 
 	 * @param scoreMatrix
 	 * @return
 	 */
 	public static Map<Integer, List<MappingRegion>> computeMappingRegions3(Matrix scoreMatrix) {
		
		Map<Integer, List<MappingRegion>> mappingRegions = new TreeMap<>();
	
		// Prepare mapping region for each story step
		for(int i = 0; i < scoreMatrix.getRowSize(); i++) {
			mappingRegions.put(i, new ArrayList<>());
		}
		
		// Retrieve all mapping regions for each story step (between 1 and trace size - 1)
		for(int i = 1; i < scoreMatrix.getRowSize() - 1; i++) {
						
			Vector previousScore = scoreMatrix.getRow(i-1);
			Vector currentScore  = scoreMatrix.getRow(i);
			Vector nextScore     = scoreMatrix.getRow(i+1);
			
			Vector enterMask = new Vector(scoreMatrix.getColumnSize());
			Vector enterScore = previousScore.substract(currentScore);
			for(int j = 0; j < enterScore.size(); j++) {
				enterMask.setValue(j, enterScore.getValue(j) < 0 ? 1 : 0);
			}
			
			Vector exitMask = new Vector(scoreMatrix.getColumnSize());
			Vector exitScore  = nextScore.substract(currentScore);
			for(int j = 0; j < exitScore.size(); j++) {
				exitMask.setValue(j, exitScore.getValue(j) <= 0 ? 1 : 0);
			}
			
	 		// Compute mapping vector for current story
			Vector mappingVector = enterMask.ebeMultiply(exitMask);
			List<MappingRegion> regions = convertVectorToRegions(i, mappingVector);
			mappingRegions.get(i).addAll(regions);
		}
		
		return mappingRegions;
	}

 	/**
 	 * Based on score matrix, compute, for each story the mapping regions.
 	 * 
 	 * @param scoreMatrix
 	 * @return
 	 */
 	public static Map<Integer, List<MappingRegion>> computeMappingRegions4(Matrix scoreMatrix1, Matrix scoreMatrix2) {
		
		Map<Integer, List<MappingRegion>> mappingRegions = new TreeMap<>();
	
		// Prepare mapping region for each story step
		for(int i = 0; i < scoreMatrix1.getRowSize(); i++) {
			mappingRegions.put(i, new ArrayList<>());
		}
		
		// Retrieve all mapping regions for each story step (between 1 and trace size - 1)
		for(int i = 1; i < scoreMatrix1.getRowSize() - 1; i++) {
						
			Vector tfidfVector = scoreMatrix1.getRow(i);

			// Compute trigger used to select a mapping
			double triggerValue = tfidfVector.getL1Norm() / tfidfVector.size();
	 		triggerValue += (100 - triggerValue) / 2;

	 		// Compute mapping vector for current story
			for(int j = 0; j < tfidfVector.size(); j++) {
				tfidfVector.setValue(j, tfidfVector.getValue(j) < triggerValue ? 0 : 1);
			}

			Vector previousScore = scoreMatrix2.getRow(i-1);
			Vector currentScore  = scoreMatrix2.getRow(i);
			Vector nextScore     = scoreMatrix2.getRow(i+1);
			
			Vector enterMask = new Vector(scoreMatrix1.getColumnSize());
			Vector enterScore = previousScore.substract(currentScore);
			for(int j = 0; j < enterScore.size(); j++) {
				enterMask.setValue(j, enterScore.getValue(j) < 0 ? 1 : 0);
			}
			
			Vector exitMask = new Vector(scoreMatrix1.getColumnSize());
			Vector exitScore  = nextScore.substract(currentScore);
			for(int j = 0; j < exitScore.size(); j++) {
				exitMask.setValue(j, exitScore.getValue(j) <= 0 ? 1 : 0);
			}
			
	 		// Compute mapping vector for current story
			Vector tripletVector = enterMask.ebeMultiply(exitMask);
			Vector tfidfScore = tfidfVector.ebeMultiply(tripletVector.invert());
			Vector mappingVector = tripletVector.add(tfidfScore);
			
			List<MappingRegion> regions = convertVectorToRegions(i, mappingVector);
			mappingRegions.get(i).addAll(regions);
		}
		
		return mappingRegions;
	}
 	
 	/**
 	 * Retrieve all region mapping for each story classified by story steps.
 	 * 
 	 * @param mappingMatrix
 	 * @return
 	 */
 	private static Map<Integer, List<MappingRegion>> cloneMappingRegions(Map<Integer, List<MappingRegion>> mappings) {
 		
 		Map<Integer, List<MappingRegion>> mappingRegions = new TreeMap<>();
 		
 		// Prepare mapping region for each story step
 		for(int i = 0; i < mappings.size(); i++) {
 			mappingRegions.put(i, new ArrayList<>());
 		}

 		// Retrieve all mapping regions for each story step
		for(int i = 0; i < mappings.size(); i++) {
			for(int j = 0; j < mappings.get(i).size(); j++) {
			
				MappingRegion originalRegion = mappings.get(i).get(j);
				
				MappingRegion copyRegion = new MappingRegion();
				copyRegion.setStoryIndex(originalRegion.getStoryIndex());
				copyRegion.setStartIndex(originalRegion.getStartIndex());
				copyRegion.setEndIndex(originalRegion.getEndIndex());
				
				mappingRegions.get(i).add(copyRegion);
			}
		}
		
		return mappingRegions;
 	}
 	
	/**
	 * 
	 * @param matchingVector
	 * @return
	 */
	private static List<MappingRegion> convertVectorToRegions(int storyIndex, Vector matchingVector) {
		
 		List<MappingRegion> mappingRegions = new ArrayList<>();

		int startIndex = 0;
		boolean isCapturing = false;
		
		for(int j = 0; j < matchingVector.size(); j++) {
			
			if (!isCapturing && matchingVector.getValue(j) == 1) {
				isCapturing = true;
				startIndex = j;
			}
			
			if (isCapturing && (matchingVector.getValue(j) == 0 || j == matchingVector.size() - 1)) {
				
 				MappingRegion newRegion = new MappingRegion();
 				newRegion.setStoryIndex(storyIndex);
 				newRegion.setStartIndex(startIndex);
 				newRegion.setEndIndex(j);
 				
 				mappingRegions.add(newRegion);
				isCapturing = false;
			}
		}

 		return mappingRegions;
	}
	
	/**
 	 * Base on manual configured file, compute original mapping path (target path to retrive by automatic path).
 	 * 
 	 * @param mappingRegions
 	 * @return
 	 */
 	public static List<MappingRegion> computeOriginalMappingPath(Map<Integer, Integer> storyToTraceMapping, int traceSize) {
 		
 		List<MappingRegion> path = new ArrayList<>();

 		for(int i = 0; i < storyToTraceMapping.size(); i++) {
 			
 	 		int startIndex = storyToTraceMapping.get(i + 1) - 1;
 	 		int endIndex   = traceSize - 1;
 			if (storyToTraceMapping.containsKey(i + 2)) {
 				endIndex = storyToTraceMapping.get(i + 2) - 1;
 			}
 			
			MappingRegion newRegion = new MappingRegion();
			newRegion.setStoryIndex(i);
			newRegion.setStartIndex(startIndex);
			newRegion.setEndIndex(endIndex);
		
			path.add(newRegion);
 		}
 		
 		return path;
 	}
 	
 	/**
 	 * Retrieve the following region
 	 * @return mappingRegion
 	 */
 	public static MappingRegion getNextRegion(MappingRegion currentRegion, List<MappingRegion> mappingRegions) {
 		
	 		MappingRegion nextRegion = null;
	 		
 	 		// Retrieve next region on the right of the current one
 	 		for(MappingRegion region : mappingRegions) {
 	 			
 	 			// Only select regions laying on the right of current one
				if (region.getEndIndex() > currentRegion.getStartIndex()) {
					
					// If no region already selected, select one by default
	 				if (nextRegion == null) {
 	 					nextRegion = region;
	 				}
					else {
	 						       
						// If region is nearest to the current region the current next region, prefer this one
						double nextRegionDist = nextRegion.getDistance(currentRegion);
						double regionDist = region.getDistance(currentRegion);
						
						if (regionDist <= nextRegionDist) {
							nextRegion = region;
						}
			 		}
				}
 			}
 	 		
 	 		return nextRegion;
 	}
 	
	/**
 	 * Compute most probable mapping-region path associate to story.
 	 * 
 	 * @param mappingRegions
 	 * @return
 	 */
 	public static List<MappingRegion> computeMappingPath(Map<Integer, List<MappingRegion>> mappingRegions, int traceSize) {
 		
 		List<MappingRegion> path = new ArrayList<>();
 		
 		Map<Integer, List<MappingRegion>> mappings = cloneMappingRegions(mappingRegions);

 		MappingRegion firstRegion = new MappingRegion(0, 0, 0);
 		mappings.get(0).add(firstRegion);

 		MappingRegion lastRegion = new MappingRegion(mappingRegions.size() - 1, traceSize - 1, traceSize - 1);
 		mappings.get(mappingRegions.size() - 1).add(lastRegion);
 		
 		// Init current region
 		MappingRegion currentRegion = firstRegion;
 		path.add(currentRegion);

 		// Scan all story mappings
 		for(int i = 1; i < mappings.size(); i++) {

 	 		MappingRegion nextRegion = null;
	 		
 	 		// Retrieve next region on the right of the current one
 	 		for(MappingRegion region : mappings.get(i)) {
 	 			
 	 			// Only select regions laying on the right of current one
				if (region.getEndIndex() > currentRegion.getStartIndex()) {
					
					// If no region already selected, select one by default
	 				if (nextRegion == null) {
 	 					nextRegion = region;
	 				}
					else {
	 						       
						// If region is nearest to the current region the current next region, prefer this one
						double nextRegionDist = nextRegion.getDistance(currentRegion);
						double regionDist = region.getDistance(currentRegion);
						
						if (regionDist <= nextRegionDist) {
							nextRegion = region;
						}
			 		}
				}
 			}
 	 		
 	 		// If no intersection, make current region preceding next one
 	 		if (!nextRegion.isIntersecting(currentRegion)) {
 	 			currentRegion.setEndIndex(nextRegion.getStartIndex());
 	 		}
 	 		
 	 		// If next region is inside previous one, just cut previous one
 	 		else if (nextRegion.isInside(currentRegion)) {
 	 			MappingRegion intersection = nextRegion.getIntersection(currentRegion);
 	 			currentRegion.setEndIndex(intersection.getStartIndex());
 	 		}
 	 		
 	 		// If intersection, adjust next region so it follows current one
 	 		else if (nextRegion.isIntersecting(currentRegion)) {
 	 			MappingRegion intersection = nextRegion.getIntersection(currentRegion);
 	 			nextRegion.setStartIndex(intersection.getEndIndex());
 	 		}

			path.add(nextRegion);
 	 		currentRegion = nextRegion;
 		}
 		
		System.out.println("selected path:");
 		for(MappingRegion region : path) {
 	 		System.out.println("story " + region.getStoryIndex() + ": (" + region.getStartIndex() + "," + region.getEndIndex() + ")");
 		}

 		return path;
 	}

 	/**
 	 * SMA = Simple Moving Average method.
 	 * Reference: https://en.wikipedia.org/wiki/Moving_average
 	 * 
 	 * @param vector
 	 * @param windowSize
 	 * @return
 	 */
 	public static Vector getSmoothSMA(Vector vector, int windowSize) {
 		
 		Vector result = new Vector(vector.size());
 		
 		// Adjust window-size and calculate its half-size
 		windowSize += windowSize % 2 == 0 ? 1 : 0;
 		int windowHalfSize = windowSize / 2;
 		
 		// Expand original vector to the left and right from window-size
 		Vector expandedVector = new Vector(vector.size() + windowSize - 1);
 		for(int i = 0; i < vector.size(); i++) {
 			expandedVector.setValue(i + windowHalfSize, vector.getValue(i));
 		}
 		 		
 		// Make convolution mean through the sliding window
 		for(int i = windowHalfSize; i < expandedVector.size() - windowHalfSize; i++) {
 			
 			// Calculate the window mean
 	 		double mean = 0;
 	 		for(int k = i - windowHalfSize; k < i + windowHalfSize + 1; k++) {
 	 			mean += expandedVector.getValue(k);
 			}
 	 		mean /= windowSize;
 	 		
 	 		// And put to the resulting vector
 	 		result.setValue(i - windowHalfSize, mean);
 		}
 		
 		return result;
 	}
 	 	
 	/**
 	 * WMA = Weighted Moving Average method (custom).
 	 * Reference: https://en.wikipedia.org/wiki/Moving_average
 	 * 
 	 * @param vector
 	 * @param windowSize
 	 * @return
 	 */
 	public static Vector getSmoothWMA(Vector vector, int windowSize) {
 		
 		Vector result = new Vector(vector.size());
 		
 		// Adjust window-size and calculate its half-size
 		windowSize += windowSize % 2 == 0 ? 1 : 0;
 		int windowHalfSize = windowSize / 2;
 		
 		// Expand original vector to the left and right from window-size
 		Vector expandedVector = new Vector(vector.size() + windowSize - 1);
 		for(int i = 0; i < vector.size(); i++) {
 			expandedVector.setValue(i + windowHalfSize, vector.getValue(i));
 		}
 		
 		// Make convolution mean through the sliding window
 		for(int i = windowHalfSize; i < expandedVector.size() - windowHalfSize; i++) {
 			
 	 		double weightedMean = 0;
 	 		double weightedFactor = 0;

 	 		// Calculate the window mean
 	 		for(int k = i - windowHalfSize; k < i + windowHalfSize + 1; k++) {
 	 			
 	 			// A = Math.abs(2 - i):  0 1 2 3 4 => 2 1 0 1 2
 	 			// B = 1 / (1 + (2 * A): 2 1 0 1 2 => 1/5 1/3 1/1 1/3 1/5
 	 			double weight = 1d / (1 + 2 * Math.abs(2 - k));
 	 			weightedMean += expandedVector.getValue(k) * weight;
 	 			weightedFactor = weight;
 			}
 	 		weightedMean /= weightedFactor;
 	 		
 	 		// And put to the resulting vector
 	 		result.setValue(i - windowHalfSize, weightedMean);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * EMA = Exponential moving average method.
 	 * Reference: https://en.wikipedia.org/wiki/Moving_average
 	 * 
 	 *  
 	 * 			|	Yt, t=1
 	 *	St = 	|
 	 *			|	alpha * Yt + (1 - alpha) St-1, t > 1
 	 * 
 	 * 
 	 * @param vector
 	 * @param alpha
 	 * @return
 	 */
 	public static Vector getSmoothEMA(Vector vector, double alpha) {
 		
 		Vector result = new Vector(vector.size());

 		for(int i = 0; i < result.size(); i++) {
 			
 			
 			if (i == 0) {
 				result.setValue(0, vector.getValue(i));
 			}
 			else {
 				double value = alpha * vector.getValue(i) + (1 - alpha) * result.getValue(i-1);
 				result.setValue(i, value);
 			}
 		}
 		
 		return result;
    }
}
