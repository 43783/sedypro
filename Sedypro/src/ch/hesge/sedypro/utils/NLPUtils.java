package ch.hesge.sedypro.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.IStemmer;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class NLPUtils {

	// Static global attributes
	private static IDictionary wordnet;
	private static MaxentTagger stanfordTagger;
	private static IStemmer wordnetStemmer;
	
	/**
	 * Return the WORDNET dictionary
	 * @return IDictionary
	 */
	private static IDictionary getWordnet() {
		
		if (wordnet == null) {
			
			try {
				// Initialize wordnet
				wordnet = new Dictionary(new URL("file", null, "lib/wordnet/db"));
				wordnet.open();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return wordnet;
	}
	
	/**
	 * Return the STANFORD tagger
	 * @return MaxentTagger
	 */
	private static MaxentTagger getTagger() {
		
		if (stanfordTagger == null) {
			
			try {
				// Initialize the stanford tagger
				stanfordTagger = new MaxentTagger("lib/stanford/english-left3words-distsim.tagger");			
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return stanfordTagger;
	}
	
	/**
	 * Return the WORDNET stemmer
	 * @return IStemmer
	 */
	private static IStemmer getStemmer() {
		
		if (wordnetStemmer == null) {
			
			try {
				// Initialize the wordnet stemmer
				wordnetStemmer = new WordnetStemmer(getWordnet());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return wordnetStemmer;
	}
	
	/**
	 * Retrieve all stems composing the word.
	 * 
	 * @param word
	 * @param wordType
	 * @return
	 */
	public static List<String> getStemmedWords(String word, POS wordType) {
 	 	
 		Set<String> stemSet = new TreeSet<>();
 		
 		// First, add original word and its stems
 		stemSet.add(word.toLowerCase());
		stemSet.addAll(getStemmer().findStems(word, wordType));

		// Then split word, if possible
		List<String> camelWords = StringUtils.splitCamelCase(word);

		// Finally add all found stems
		for (String wordPart : camelWords) {
			
			// Retrieve all stems from wordnets
			List<String> subStems = new ArrayList<>();
			for(String stem : getStemmer().findStems(wordPart, wordType)) {
				subStems.add(stem.toLowerCase());
			}
			
			// Add the original subword
			stemSet.add(wordPart.toLowerCase());

			// Add all subword stems
			stemSet.addAll(subStems);
		}
 		 
		return new ArrayList<>(stemSet);
 	}
	
	/**
	 * Tag all words passed in argument.
	 * 
	 * @param words
	 * @return
	 */
	public static List<TaggedWord> getTaggedWords(List<String> words) {

		// First convert words into HasWord
		List<HasWord> stanfordList = new ArrayList<HasWord>();
		
		for (String word : words) {
			Word hasWord = new Word();
			hasWord.setWord(word);
			stanfordList.add(hasWord);
		}
		
		return getTagger().tagSentence(stanfordList);
	}
	
	/**
	 * Reorder words in a natural order.
	 * 
	 * @param words
	 * @return
	 */
	public static String getNaturalWordsOrder(String words) {
		
		List<String> subjects = new ArrayList<>();
		List<String> actions = new ArrayList<>();
		List<String> complements = new ArrayList<>();
		
		List<String> wordList = StringUtils.toStringList(words, ",");
		
		for(String word : wordList) {
			
			if (word.startsWith("s:")) {
				subjects.add(word);
			}
			else if (word.startsWith("a:")) {
				actions.add(word);
			}
			else if (word.startsWith("c:")) {
				complements.add(word);
			}
		}
		
		// Recombine words in proper order
		List<String> orderedWords = new ArrayList<>();
		orderedWords.addAll(subjects);
		orderedWords.addAll(actions);
		orderedWords.addAll(complements);
		
		return StringUtils.toString(orderedWords, ",");
	}	
	
}
