package ch.hesge.sedypro.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DictionaryUtils {
 	
	/**
	 * Load all story lines from file
	 * @return
	 */
 	public static Map<String, String> loadStory(Path storyPath) throws IOException {
 		
 		Map<String, String> storyLines = new TreeMap<>();
		
		try (Stream<String> textLines = Files.lines(storyPath)) {

			Iterator<String> lineIterator =  textLines.iterator();
		
			int lineNumber = 0;
			
			while (lineIterator.hasNext()) {
			
				lineNumber++;
				
				// Retrieve current line
				String storyLine = lineIterator.next();
				String storyLineKey = String.format("s%1$04d", lineNumber);
				
				// Filter story line number, if present
				if (storyLine.startsWith(storyLineKey.toUpperCase()) ) {
					storyLine = storyLine.substring(6);
				}
						
				// Add each line to map
				storyLines.put(storyLineKey, storyLine);
			}
		}
		
		return storyLines;
 	}

	/**
	 * Load all trace lines from file
	 * @return
	 */
 	public static Map<String, String> loadTrace(Path storyPath) throws IOException {
 		
 		Map<String, String> traceLines = new TreeMap<>();
		
		try (Stream<String> textLines = Files.lines(storyPath)) {

			Iterator<String> lineIterator =  textLines.iterator();
		
			int lineNumber = 0;
			
			while (lineIterator.hasNext()) {
			
				lineNumber++;
				
				// Retrieve current line
				String traceLine = lineIterator.next();
				String traceLineKey  = String.format("t%1$04d", lineNumber);

				// Filter trace line number, if present
				if (traceLine.startsWith(traceLineKey.toUpperCase()) ) {
					traceLine = traceLine.substring(6);
				}
				
				// Add each line to map
				traceLines.put(traceLineKey, traceLine);
			}
		}
		
		return traceLines;
 	}

 	/**
 	 * Return a map of all story-to-trace mapping (ex: S0005:T0030)
 	 * @param mappingPath
 	 * @return
 	 * @throws IOException
 	 */
 	public static Map<String, String> loadStoryToTraceMapping(Path mappingPath) throws IOException {
 		
 		Map<String, String> storyToTraceMapping = new TreeMap<>();
 		
		try (Stream<String> textLines = Files.lines(mappingPath)) {

			Iterator<String> lineIterator =  textLines.iterator();
			
			while (lineIterator.hasNext()) {
				
				// Retrieve current line
				String textLine = lineIterator.next().trim().toLowerCase();		
				
				if (textLine.length() > 0 && !textLine.startsWith("#")) {
					
					// Retrieve mapping entry
					List<String> mappingEntry = StringUtils.toStringList(textLine, ":");
					String storyLineNumber = mappingEntry.get(0).trim();
					String traceLineNumber = mappingEntry.get(1).trim();

					// Update mapping
					storyToTraceMapping.put(storyLineNumber, traceLineNumber);
				}
			}
		}
		
		return storyToTraceMapping;
 	}
 	
 	/**
 	 * Return a map of all trace-to-trace mapping (ex: T0030:S0005)
 	 * @param mappingPath
 	 * @return
 	 * @throws IOException
 	 */
 	public static Map<String, String> loadTraceToStoryMapping(Path mappingPath) throws IOException {
 		
 		Map<String, String> storyToTraceMapping = new TreeMap<>();
 		
		try (Stream<String> textLines = Files.lines(mappingPath)) {

			Iterator<String> lineIterator =  textLines.iterator();
			
			while (lineIterator.hasNext()) {
				
				// Retrieve current line
				String textLine = lineIterator.next().trim().toLowerCase();		
				
				if (textLine.length() > 0 && !textLine.startsWith("#")) {
					
					// Retrieve mapping entry
					List<String> mappingEntry = StringUtils.toStringList(textLine, ":");
					String storyLineNumber = mappingEntry.get(0).trim();
					String traceLineNumber = mappingEntry.get(1).trim();

					// Update mapping
					storyToTraceMapping.put(traceLineNumber, storyLineNumber);
				}
			}
		}
		
		return storyToTraceMapping;
 	}
 	
 	/**
 	 * Load all words and line numbers from dictionary.
 	 * 
 	 * @param dictionaryPath
 	 * @throws IOException
 	 */
 	public static Map<String, DictionaryEntry> loadDictionary(Path dictionaryPath) throws IOException {
 		
 		Map<String, DictionaryEntry> wordDictionary = new TreeMap<>();
		
		try (Stream<String> textLines = Files.lines(dictionaryPath)) {

			Iterator<String> lineIterator =  textLines.iterator();
			
			while (lineIterator.hasNext()) {
				
				// Retrieve current line
				String textLine = lineIterator.next();				
				String regex = "(?<type>.*):(?<word>.*):(?<originalWords>.*)";
				Matcher patternMatcher = Pattern.compile(regex).matcher(textLine);
				
				// For each word, extract all origianl word and lines where it appears
				if (patternMatcher.matches()) {
					
					// Retrieve type, word, lines
					String type  = patternMatcher.group("type");
					String word  = patternMatcher.group("word");
					String originalWords = patternMatcher.group("originalWords");
					
					DictionaryEntry newEntry = new DictionaryEntry();
					newEntry.setWord(word);
					newEntry.setWordType(type);
					
					regex = "\\((?<word>[^,]*),(?<lineNumbers>[^\\)]*)\\)";
					Matcher subPatternMatcher = Pattern.compile(regex).matcher(originalWords);
					
					while (subPatternMatcher.find()) {
						String originalWord = subPatternMatcher.group("word");
						String lineNumbers  = subPatternMatcher.group("lineNumbers");
						
						newEntry.getOriginalWords().put(originalWord, new TreeSet<>());
						newEntry.getOriginalWords().get(originalWord).addAll(StringUtils.toIntegerSet(lineNumbers));
					}
					
					String entryKey = type + ":" + word;
					wordDictionary.put(entryKey, newEntry);
				}
			}
		}
		
		return wordDictionary;
 	} 	

 	/**
 	 * Load two dictionary and compute word intersection.
 	 * 
 	 * @param firstDictionaryPath
 	 * @param secondDictionaryPath
 	 * @return a set of intersection words
 	 * @throws IOException
 	 */
 	public static Set<String> loadIntersectionSet(Path aPath, Path bPath) throws IOException {
 		return getIntersectionSet(loadDictionary(aPath), loadDictionary(bPath));
 	}
 	
 	/**
 	 * Load two dictionary and compute word intersection.
 	 * 
 	 * @param firstDictionaryPath
 	 * @param secondDictionaryPath
 	 * @return a set of intersection words
 	 * @throws IOException
 	 */
 	public static Set<String> getIntersectionSet(Map<String, DictionaryEntry> a, Map<String, DictionaryEntry> b) throws IOException {

 		// Build dictionary intersection
 		Set<String> intersectingSet = new TreeSet<>();
 		intersectingSet.addAll(a.keySet());
 		intersectingSet.retainAll(b.keySet());
 		
 		return intersectingSet;
 	}
 	/**
 	 * Load all story word translation from dictionary.
 	 * 
 	 * @param dictionaryPath
 	 * @throws IOException
 	 */
 	public static Map<String, Set<String>> loadTranslationDictionary(Path dictionaryPath) throws IOException {
 		
 		Map<String, Set<String>> translationDictionary = new TreeMap<>();
		
		try (Stream<String> textLines = Files.lines(dictionaryPath)) {

			Iterator<String> lineIterator =  textLines.iterator();
			
			while (lineIterator.hasNext()) {
				
				// Retrieve current line
				String dictionaryLine = lineIterator.next().trim();		
				
				// Skip empty lines or starting with comment tag
				if (!dictionaryLine.isEmpty() && !dictionaryLine.startsWith("#")) {

					// Retrieve all words in line
					List<String> wordList = StringUtils.toStringList(dictionaryLine, ",");
					
					// Get story word
					String storyWord = wordList.remove(0).trim();
					
					// Create a new entry for it, if not already present
					if (!translationDictionary.containsKey(storyWord)) {
						translationDictionary.put(storyWord, new TreeSet<>());
					}
					
					// Update associated translations
					for(String traceWord : wordList) {
						translationDictionary.get(storyWord).add(traceWord.trim());
					}
				}
			}
		}
		
		return translationDictionary;
 	} 	 	
 	 	
	/**
	 * Load all story or trace words from file
	 * @return
	 */
 	public static Map<String, String> loadLinesToWordsDictionary(Path lineToWordsPath, String lineNumberPrefix) throws IOException {
 		
 		Map<String, String> linesToWordsMap = new TreeMap<>();
 		 
		try (Stream<String> textLines = Files.lines(lineToWordsPath)) {

			Iterator<String> lineIterator =  textLines.iterator();

			int lineNumber = 0;
			
			while (lineIterator.hasNext()) {
			
				lineNumber++;
				
				// Retrieve current line
				String textLine = lineIterator.next();		
				
				// Update story words map
				String textLineKey = String.format("%1s%2$04d", lineNumberPrefix.toLowerCase(), lineNumber);
				linesToWordsMap.put(textLineKey, textLine);
			}
		}
		
		return linesToWordsMap;
 	}

 	
	/**
 	 * Load all forbiden words (only used for complements).
	 * 
	 * @param stopWordPath
	 * @return
	 * @throws IOException
	 */
 	public static Map<String, String> loadStopWordsDictionary(Path stopWordPath) throws IOException {
 		
 		Map<String, String> stopWordMap = new HashMap<String, String>();
 		
		try (Stream<String> lines = Files.lines(stopWordPath)) {
			Iterator<String> lineIterator =  lines.iterator();
			
			while (lineIterator.hasNext()) {
				
				String textLine = lineIterator.next().trim();	
				
				if (!textLine.startsWith("#") && textLine.length() > 0) {
					stopWordMap.put(textLine, textLine);
				}
			}
		}
		
		return stopWordMap;
 	}
 	
 	/**
 	 * Load all strings to be removed from trace.
 	 * 
 	 * @param substitutionStringsPath
 	 * @return
 	 * @throws IOException
 	 */
 	public static Map<String,String> loadTraceReplacementDictionary(Path substitutionStringsPath) throws IOException {
 		
 		Map<String,String> replaceStringMap = new TreeMap<>();
 		
		try (Stream<String> lines = Files.lines(substitutionStringsPath)) {
			
			Iterator<String> lineIterator =  lines.iterator();
			
			while (lineIterator.hasNext()) {
				
				String textLine = lineIterator.next();
				
				if (!textLine.startsWith("#") && textLine.length() > 0) {
					
					String[] subparts = textLine.split(",");
					
					String originalString = subparts[0];
					String replaceString = "";
					
					if (subparts.length > 1) {
						replaceString = subparts[1];
					}
					
					replaceStringMap.put(originalString, replaceString);
				}
			}
		}
		
		return replaceStringMap;
 	}

	/**
 	 * Load the matrix from file.
 	 * 
 	 * @throws IOException 
 	 */
 	public static Matrix loadMatrix(Path matrixPath) throws IOException {
 		
		int rowCount = 0;
 		List<Vector> matrixRows = new ArrayList<>();
		
		try (Stream<String> textLines = Files.lines(matrixPath)) {

			Iterator<String> lineIterator =  textLines.iterator();
			
			while (lineIterator.hasNext()) {
				
				rowCount++;
				
				// Retrieve current line
				String textLine = lineIterator.next();	

				// Convert line to numbeer list
				List<Double> vectorValues = StringUtils.toDoubleList(textLine);
				
				// Add new column values to matrix
				matrixRows.add(new Vector(vectorValues));
			}
		}
		
		int columnCount = matrixRows.get(0).size();
 		Matrix matrix = new Matrix(rowCount, columnCount);
 		matrix.setRows(matrixRows);
 		
 		return matrix;
 	}
 	
 	/**
 	 * Convert a word dictionary into an ordered line-to-word map
 	 * 
 	 * @param dictionary
 	 * @return
 	 */
 	public static Map<Integer, Set<String>> getLineToWordMap(Map<String, DictionaryEntry> dictionary) {
 		
 		Map<Integer, Set<String>> lineToWordDictionary = new TreeMap<>();
 		
 		// For each line, retrieve all associated words
 		for(String word : dictionary.keySet()) {
 	 		for(String originalWord : dictionary.get(word).getOriginalWords().keySet()) {
 	 	 		for(int lineNumber :  dictionary.get(word).getOriginalWords().get(originalWord)) {

 	 	 			// Check if an entry already exists for this line
 	 	 			if (!lineToWordDictionary.containsKey(lineNumber)) { 
 						lineToWordDictionary.put(lineNumber,  new TreeSet<>());
 					}
 	 	 			
 	 	 			// Update the line entry
 					lineToWordDictionary.get(lineNumber).add(word);
 	 	 		}
 	 		}
 		}
 		
 		return lineToWordDictionary;
 	}
}
