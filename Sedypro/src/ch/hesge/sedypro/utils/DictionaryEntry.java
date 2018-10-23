package ch.hesge.sedypro.utils;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DictionaryEntry implements Comparable<DictionaryEntry> {

	public static String SUBJECT    = "s";
	public static String ACTION     = "a";
	public static String COMPLEMENT = "c";
	
	private String word;
	private String wordType;
	private Map<String, Set<Integer>> originalWords;
	
	public DictionaryEntry() {
		originalWords = new TreeMap<>();
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getWordType() {
		return wordType;
	}
	
	public void setWordType(String wordType) {
		this.wordType = wordType;
	}
	
	public Map<String, Set<Integer>> getOriginalWords() {
		return originalWords;
	}

	@Override
    public int compareTo(final DictionaryEntry o) {
        return (wordType + ":" + word).compareTo(o.wordType + ":" + o.word);
    }	
	
	public DictionaryEntry clone() {
		DictionaryEntry entry = new DictionaryEntry();
		entry.word = this.word;
		entry.wordType = this.wordType;
		originalWords.putAll(this.originalWords);
		return entry;
	}
}