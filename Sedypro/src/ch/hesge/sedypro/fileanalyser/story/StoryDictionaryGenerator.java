package ch.hesge.sedypro.fileanalyser.story;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import ch.hesge.sedypro.utils.DictionaryEntry;
import ch.hesge.sedypro.utils.DictionaryUtils;
import ch.hesge.sedypro.utils.NLPUtils;
import ch.hesge.sedypro.utils.StringUtils;
import ch.hesge.sedypro.utils.TagUtils;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.TaggedWord;

public class StoryDictionaryGenerator {

	private Map<String, String> stopWordMap;
	private Map<String, DictionaryEntry> dictionaryEntries;
	
	/**
	 * Default constructor
	 */
	public StoryDictionaryGenerator() {
		dictionaryEntries = new TreeMap<>();
	}
	
 	/**
 	 * Generate the dictionary of stemmed word classified by type (subject, action, complement).
 	 * Each word is also marked with all storylines where it is used.
 	 * 
 	 * @throws IOException 
 	 */
 	public void doGenerate(String storyFilename, String dictionaryFilename, String stopWordFilename) throws IOException {
 		
		// Retrieve input/output paths
		Path storyPath      = Paths.get(storyFilename).toAbsolutePath().normalize();
		Path dictionaryPath = Paths.get(dictionaryFilename).toAbsolutePath().normalize();
		Path stopWordPath   = Paths.get(stopWordFilename).toAbsolutePath().normalize();
		
		// Load required dictionaries
		stopWordMap = DictionaryUtils.loadStopWordsDictionary(stopWordPath);
		
		// Read the story file line by line
		try (Stream<String> textLines = Files.lines(storyPath)) {

			int lineNumber = 0;
			Iterator<String> lineIterator =  textLines.iterator();

			while (lineIterator.hasNext()) {
				
				lineNumber++;
				
				// Retrieve current line content (without line number header)
				String storyLine = lineIterator.next().substring(6).trim();	
				
				// Retrieve all words contained in story
				List<String> words = StringUtils.toStringList(storyLine, " ");
				
    				// Extract subject
				String subject = words.remove(0);
				for (String stem : NLPUtils.getStemmedWords(subject, POS.NOUN)) {
					addDictionaryEntry(stem, DictionaryEntry.SUBJECT, subject, lineNumber);
				}
				
				// Extract action
				String action = words.remove(0);
				for (String stem : NLPUtils.getStemmedWords(action, POS.VERB)) {
					addDictionaryEntry(stem, DictionaryEntry.ACTION, action, lineNumber);
				}
				
				// All left words in sentence are considered as complements
				for (TaggedWord word : NLPUtils.getTaggedWords(words)) {
					
	    	    			// Detect atomic action
		    	    		if (TagUtils.isVerb(word)) {
	    					for (String stem : NLPUtils.getStemmedWords(word.word(), POS.VERB)) {
	    						addDictionaryEntry(stem, DictionaryEntry.ACTION, word.word(), lineNumber);
	    					}
		    	    		}
		    	    		else {
	    					for (String stem : NLPUtils.getStemmedWords(word.word(), POS.NOUN)) {
	    						addDictionaryEntry(stem, DictionaryEntry.COMPLEMENT, word.word(), lineNumber);
	    					}
		    	    		}
				}				
			}
		}
		
		// If file already exists, suppress it
		if (dictionaryPath.toFile().exists()) {
			Files.delete(dictionaryPath);
		}
		
		// Save generated dictionary
		try ( PrintWriter writer = new PrintWriter(new FileWriter(dictionaryPath.toFile())) ) {
		 
			for (DictionaryEntry entry : dictionaryEntries.values()) {
				
				String outputLine = "";
				
				String storyWord = entry.getWord();
				String storyType = entry.getWordType();
	
				outputLine += storyType + ":" + storyWord + ":";
				
				for(String originalWord : entry.getOriginalWords().keySet()) {
					String storyLines   = StringUtils.toString(entry.getOriginalWords().get(originalWord), ",");
					outputLine += "(" + originalWord + "," + storyLines + ")";
				}
				
				writer.println(outputLine);
			}
		}
 	}
 	
 	/**
 	 * Add a new word into the story dictionary.
 	 * 
 	 * @param word
 	 * @param wordType
 	 * @param originalWord
 	 * @param lineNumber
 	 */
 	private void addDictionaryEntry(String word, String wordType, String originalWord, int lineNumber) {
 		
 		// Skip stopword and too small words
 		if (word.length() < 2 || (wordType == DictionaryEntry.COMPLEMENT && stopWordMap.containsKey(word))) {
 			return;
 		}
 		
 		// Normalize words
 		word = word.toLowerCase();
 		originalWord = originalWord.toLowerCase();
 		
		String entryKey = wordType + ":" + word;
		
		// Create a new entry, if not already present
		if (!dictionaryEntries.containsKey(entryKey)) {
			
			DictionaryEntry entry = new DictionaryEntry();
			entry.setWord(word);
			entry.setWordType(wordType);
			
			dictionaryEntries.put(entryKey, entry);
		}
		
		DictionaryEntry entry = dictionaryEntries.get(entryKey);
		
		// Create a new original word entry, if not already present
		if (!entry.getOriginalWords().containsKey(originalWord)) {
			entry.getOriginalWords().put(originalWord,  new TreeSet<Integer>());
		}
		
		// Finally update line number for current word and original word
		entry.getOriginalWords().get(originalWord).add(lineNumber);
 	}
}
