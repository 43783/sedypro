package ch.hesge.sedypro.fileanalyser.story;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import ch.hesge.sedypro.utils.DictionaryEntry;
import ch.hesge.sedypro.utils.DictionaryUtils;
import ch.hesge.sedypro.utils.NLPUtils;
import ch.hesge.sedypro.utils.StringUtils;

public class StoryWordsGenerator {

	/**
	 * Default constructor
	 */
	public StoryWordsGenerator() {
	}
	
 	/**
 	 * Generate output file.
 	 * 
 	 * @throws IOException 
 	 */
 	public void doGenerate(String storyDictionaryFilename, String storyWordsFilename) throws IOException {
 		
		// Retrieve input/output paths
 		Path storyDictionaryPath = Paths.get(storyDictionaryFilename).toAbsolutePath().normalize();
 		Path storyWordsDictionaryPath = Paths.get(storyWordsFilename).toAbsolutePath().normalize();
		
		// Load required dictionaries
 		Map<String, DictionaryEntry> storyDictionary = DictionaryUtils.loadDictionary(storyDictionaryPath);
 		Map<Integer, Set<String>> storyLines = DictionaryUtils.getLineToWordMap(storyDictionary);
 		
		// If file already exists, suppress it
		if (storyWordsDictionaryPath.toFile().exists()) {
			Files.delete(storyWordsDictionaryPath);
		}
		
		// Save, for each story line, all words present in dictionary
		try ( PrintWriter writer = new PrintWriter(new FileWriter(storyWordsDictionaryPath.toFile())) ) {
			
			for(int storyLine : storyLines.keySet()) {
				
				Set<String> storyWords = storyLines.get(storyLine);
				String outputString = StringUtils.toString(storyWords, ",");
				outputString = NLPUtils.getNaturalWordsOrder(outputString);

				writer.println(outputString);
			}
		}
	}

}
