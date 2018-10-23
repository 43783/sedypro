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
import ch.hesge.sedypro.utils.NLPUtils;
import ch.hesge.sedypro.utils.StringUtils;
import edu.mit.jwi.item.POS;

public class StoryTranslationGenerator {

	private Map<String, DictionaryEntry> dictionaryEntries;
	
	/**
	 * Default constructor
	 */
	public StoryTranslationGenerator() {
		
	}	
	
 	/**
 	 * Take original story dictionary and enrich it with all
 	 * possible translation of stems.
 	 * 
 	 * @throws IOException 
 	 */
 	public void doGenerate(String dictionaryFilename, String translationsFilename, String translatedDictionaryFilename) throws IOException {
 		

		// Retrieve input/output paths
 		Path dictionaryPath = Paths.get(dictionaryFilename).toAbsolutePath().normalize();
 		Path translationsPath = Paths.get(translationsFilename).toAbsolutePath().normalize();
 		Path translatedDictionaryPath = Paths.get(translatedDictionaryFilename).toAbsolutePath().normalize();

		// Load required dictionaries
 		dictionaryEntries = DictionaryUtils.loadDictionary(dictionaryPath);
		Map<String, Set<String>> translationDictionary = DictionaryUtils.loadTranslationDictionary(translationsPath);
		
		// Scan all story words to translate
		for (String storyWordKey : translationDictionary.keySet()) {
			
			String word = storyWordKey.substring(2);
			String type = storyWordKey.substring(0, 1);
			POS pos     = type.startsWith("a") ? POS.VERB : POS.NOUN;

			// Retrieve all story words to translate
			List<String> storyWords = new ArrayList<>();
			storyWords.add(storyWordKey.toLowerCase());
			for (String stem : NLPUtils.getStemmedWords(word, pos)) {
				storyWords.add(type + ":" + stem);
			}

			// Retrieve all trace translations
			for (String traceWordKey : translationDictionary.get(storyWordKey)) {
				
				word = traceWordKey.substring(2); 
				type = traceWordKey.substring(0, 1);
				pos  = type.startsWith("a") ? POS.VERB : POS.NOUN;
				
				// Retrieve all story words to translate
				List<String> traceWords = new ArrayList<>();
				traceWords.add(traceWordKey.toLowerCase());
				for (String stem : NLPUtils.getStemmedWords(word, pos)) {
					traceWords.add(type + ":" + stem);
				}
				
				// Now add all translations found
				for(String storyWord : storyWords) {
					for(String traceWord : traceWords) {
						addTranslation(storyWord, traceWord);
					}
				}
			}
		}
		
		// If file already exists, suppress it
		if (translatedDictionaryPath.toFile().exists()) {
			Files.delete(translatedDictionaryPath);
		}
		
		// Save generated dictionary
		try ( PrintWriter writer = new PrintWriter(new FileWriter(translatedDictionaryPath.toFile())) ) {
		 
			for (DictionaryEntry entry : dictionaryEntries.values()) {
				
				String outputLine = entry.getWordType() + ":" + entry.getWord() + ":";
				
				for(String originalWord : entry.getOriginalWords().keySet()) {
					String storyLines   = StringUtils.toString(entry.getOriginalWords().get(originalWord), ",");
					outputLine += String.format("(%1$s,%2$s)", originalWord, storyLines);
				}
				
				writer.println(outputLine);
			}
		}
 	}

 	/**
 	 * Add a new word into the story dictionary.
 	 * 
 	 * @param storyWord
 	 * @param traceWord
 	 */
 	private void addTranslation(String storyWord, String traceWord) {
 		
 		if (dictionaryEntries.containsKey(storyWord)) {
 			
 			// Retrieve original entry
 			DictionaryEntry storyEntry = dictionaryEntries.get(storyWord);
 			
 			// Create a new traceentry, if not already present
 			if (!dictionaryEntries.containsKey(traceWord)) {
 				
 	 	 		// Clone original entry
 	 	 		DictionaryEntry traceEntry = storyEntry.clone();
 	 	 		traceEntry.setWord(traceWord.substring(2));
 	 	 		traceEntry.setWordType(traceWord.substring(0, 1));
 	 	 		dictionaryEntries.put(traceWord, traceEntry);
 			}
 			
 			// Update trace entry with story entry original words
 			dictionaryEntries.get(traceWord).getOriginalWords().putAll(storyEntry.getOriginalWords());
 		}
 	}
 }
