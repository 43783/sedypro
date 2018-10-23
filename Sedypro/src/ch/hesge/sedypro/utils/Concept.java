package ch.hesge.sedypro.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.mit.jwi.item.ISynset;

public class Concept {

	private ISynset synset;
	private Set<String> glossTerms;
	private List<Concept> hypernyms;
	
	public Concept() {
		this.hypernyms = new ArrayList<>();
		this.glossTerms = new HashSet<>();
	}
	
	public ISynset getSynset() {
		return synset;
	}

	public void setSynset(ISynset synset) {
		this.synset = synset;
	}

	public Set<String> getGlossTerms() {
		return glossTerms;
	}

	public List<Concept> getHypernyms() {
		return hypernyms;
	}

	public String getLemma() {
		return synset.getWord(1).getLemma();
	}
}
