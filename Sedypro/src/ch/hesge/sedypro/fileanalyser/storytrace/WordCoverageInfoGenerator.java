package ch.hesge.sedypro.fileanalyser.storytrace;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ch.hesge.sedypro.utils.DictionaryEntry;
import ch.hesge.sedypro.utils.DictionaryUtils;
import ch.hesge.sedypro.utils.StringUtils;

public class WordCoverageInfoGenerator {

	/**
	 * Default constructor
	 */
	public WordCoverageInfoGenerator() {
	}

 	/**
	 * Generate coverage information.
	 * 
 	 * @throws IOException 
 	 */
 	public void doGenerate(String storyDictionaryFilename, String traceDictionaryFilename, String storyTranslatedFilename, String coverageFilename) throws IOException {
 		
 		// Retrieve input/output paths
 		Path storyDictionaryPath = Paths.get(storyDictionaryFilename).toAbsolutePath().normalize();
 		Path traceDictionaryPath = Paths.get(traceDictionaryFilename).toAbsolutePath().normalize();
 		Path storyTranslatedDictionaryPath = Paths.get(storyTranslatedFilename).toAbsolutePath().normalize();
 		Path coveragePath   = Paths.get(coverageFilename).toAbsolutePath().normalize();

 		// Load required dictionaries
 		Map<String, DictionaryEntry> storyDictionary = DictionaryUtils.loadDictionary(storyDictionaryPath);
 		Map<String, DictionaryEntry> traceDictionary = DictionaryUtils.loadDictionary(traceDictionaryPath);
 		Map<String, DictionaryEntry> storyTranslatedDictionary = DictionaryUtils.loadDictionary(storyTranslatedDictionaryPath);

 		// Retrieve all matching words
 		Set<String> matchingWords = DictionaryUtils.getIntersectionSet(storyTranslatedDictionary, traceDictionary);
 		
 		// Calculate story distinct words
 		Set<String> storyWords = new HashSet<>();
 		for(DictionaryEntry entry : storyDictionary.values()) {
 			storyWords.addAll(entry.getOriginalWords().keySet());
 		}
 		
 		// Retrieve all matching original story words
 		Set<String> storyMatchingWords = new HashSet<>();
 		for(String matchingWord : matchingWords) {
 			storyMatchingWords.addAll(storyTranslatedDictionary.get(matchingWord).getOriginalWords().keySet());
 		}
 		
 		// Calculate trace distinct words
 		Set<String> traceWords = new HashSet<>();
 		for(DictionaryEntry entry : traceDictionary.values()) {
 			traceWords.addAll(entry.getOriginalWords().keySet());
 		}
 		
 		// Retrieve all matching original trace words
 		Set<String> traceMatchingWords = new HashSet<>();
 		for(String matchingWord : matchingWords) {
 			traceMatchingWords.addAll(traceDictionary.get(matchingWord).getOriginalWords().keySet());
 		}
 		
 		// Retrive all trace words unmatched
 		Set<String> traceUnmatchedWords = new TreeSet<>(traceWords);
 		traceUnmatchedWords.removeAll(traceMatchingWords);

 		int storyCoverageRatio = (int) Math.round(100.0 * storyMatchingWords.size() / storyWords.size());
 		int traceCoverageRatio = (int) Math.round(100.0 * traceMatchingWords.size() / traceWords.size());
 		
		// If file already exists, suppress it
		if (coveragePath.toFile().exists()) {
			Files.delete(coveragePath);
		}

		try ( PrintWriter writer = new PrintWriter(new FileWriter(coveragePath.toFile())) ) {
	 		
	 		writer.println("Text Coverage Information");
	 		writer.println("-------------------------");
	 		writer.println(); 		
	 		writer.println("Story words:    " + storyWords.size());
	 		writer.println("Trace words:    " + traceWords.size());
	 		writer.println("Matching words: " + matchingWords.size());
	 		writer.println("Story coverage: " + storyCoverageRatio + "%");
	 		writer.println("Trace coverage: " + traceCoverageRatio + "%");
	 		writer.println();
	 		
	 		writer.println("Unmatched trace words: ");
	 		String strWordList = formatUnmatchedTraceWords(2, 100, traceUnmatchedWords);
	 		writer.println(strWordList);
		}
 	}
 	
 	/**
 	 * Reformat unmatched trace words into more 'readable' representation.
 	 * s
 	 * @param indentation
 	 * @param maxLength
 	 * @param unmachtedTraceWords
 	 * @return
 	 */
 	private String formatUnmatchedTraceWords(int indentation, int maxLength, Set<String> unmachtedTraceWords) {
 		
 		String result = "";
 		String indent = StringUtils.repeat(" ", indentation);
 		String line   = indent;
 		
 		for(String word : unmachtedTraceWords) {
 			line += word + ", ";
 			
 			if (line.length() > maxLength) {
 				result += line + "\n";
 				line = indent;
 			}
 		}

 		result = StringUtils.removeTrailString(result, ",");
 		
 		return result;
 	}
}
