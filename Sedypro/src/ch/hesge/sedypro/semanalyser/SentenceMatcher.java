package ch.hesge.sedypro.semanalyser;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.hesge.sedypro.utils.Concept;
import ch.hesge.sedypro.utils.Sentence;
import ch.hesge.sedypro.utils.Word;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.Pointer;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class SentenceMatcher {

	private static IDictionary dictionary;
	
	public SentenceMatcher() {
		initDictionary();
	}
	
	private void initDictionary() {
		
		// Load the wordnet dictionary 
		try {
			
			URL url = new URL("file", null, "lib/wordnet/db");
			dictionary = new Dictionary(url);
			dictionary.open();		
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Disabiguiates each word sentence.
	 * Remove all other senses and keep only disambiguated senses for each word.
	 * 
	 * @param sentence
	 */
	public void disambiguate(Sentence sentence) {
		
		for (Word word : sentence.getWords()) {
			
			// Build a term set of all other words
			Set<String> otherTermSet = new HashSet<>();
			for (Word otherWord : sentence.getWords()) {
				if (otherWord != word) {
					for (Concept otherSense : otherWord.getSenses()) {
						otherTermSet.addAll(otherSense.getGlossTerms());
					}
				}
			}
			
			if (!word.getSenses().isEmpty()) {
				
				// Now evaluate wich sense has a maximal intersection
				int maxCommonTermCount = 0;
				Concept selectedSense = word.getSenses().get(0);
				
				for (Concept wordSense : word.getSenses()) {
					
					// Calculate set intersection
					Set<String> tmpSet = new HashSet<String>(wordSense.getGlossTerms());
					tmpSet.retainAll(selectedSense.getGlossTerms());
					int setSize = tmpSet.size();
					
					if (setSize > maxCommonTermCount) {
						selectedSense = wordSense;
						maxCommonTermCount = setSize;
					}
				}				

				word.getSenses().clear();
				word.getSenses().add(selectedSense);
			}
		}
	}

	/**
	 * Populate all senses for each word in the sentence.
	 * 
	 * @param sentence
	 */
	public void populateWordSenses(Sentence sentence) {

		for (Word word : sentence.getWords()) {
			
			// Retrieve each word in wordnet
			IIndexWord idxWord = dictionary.getIndexWord(word.getLemma(), word.getPos());
			
			if (idxWord != null) {
				
				// For each word retrieve its senses
				for (IWordID wordId : idxWord.getWordIDs()) {
					
					// Get associated synset
					ISynset synset = dictionary.getSynset(wordId.getSynsetID());

					// Get associated gloss terms
					Set<String> glossTerms = getGlossTerms(synset);
					
					Concept concept = new Concept();
					concept.setSynset(synset);
					concept.getGlossTerms().addAll(glossTerms);

					word.getSenses().add(concept);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param concept
	 */
	public void populateHypernyms(Concept concept) {
		
		List<ISynsetID> synsetIds = concept.getSynset().getRelatedSynsets(Pointer.HYPERNYM);
		
		for (ISynsetID synsetId : synsetIds) {

			ISynset synset = dictionary.getSynset(synsetId);

			Concept hypernym = new Concept();
			hypernym.setSynset(synset);
			
			populateHypernyms(hypernym);

			concept.getHypernyms().add(hypernym);
		}
	}

	/**
	 * Retrieve the set of all terms used in gloss.
	 * Include recursively all terms from hypernym hierarchy.
	 * 
	 * @param synset
	 * @return
	 */
	public Set<String> getGlossTerms(ISynset synset) {
		
		Set<String> glossTerms = new HashSet<>();
		
		String gloss = synset.getGloss();
		gloss = gloss.replaceAll("['`\",\\.,\\;:\\(\\)]*", "");
		
		// Tokenize the sentence (stanford tokenizer)
    	List<List<HasWord>> wordList = MaxentTagger.tokenizeText(new StringReader(gloss));
    	
    	// Then extract each gloss term
    	if (!wordList.isEmpty()) {
    		
    	    for (HasWord word : wordList.get(0)) {
    	    	glossTerms.add(word.word());
    	    }
    	}

    	// Get recursively all terms for all hypernyms
    	List<ISynsetID> synsetIds = synset.getRelatedSynsets(Pointer.HYPERNYM);
		
		for (ISynsetID synsetId : synsetIds) {

			ISynset hypernymSynset = dictionary.getSynset(synsetId);
			glossTerms.addAll(getGlossTerms(hypernymSynset));
		}		

		return glossTerms;
	}
	
	public List<List<Concept>> getHypernymPaths(Concept concept) {

    	List<List<Concept>> hypernymPaths = new ArrayList<>();

        if(concept.getHypernyms().isEmpty()) {
            List<Concept> hypernymList = new ArrayList<>();
            hypernymList.add(concept);
            hypernymPaths.add(hypernymList);
        } 
        else {
            for (Concept c : concept.getHypernyms()) {
                List<List<Concept>> hypernymSubPaths = getHypernymPaths(c);
                for (List<Concept> hypernymList : hypernymSubPaths) {
                    hypernymList.add(0, concept);
                    hypernymPaths.add(hypernymList);
                }
            }
        }

        return hypernymPaths;
    }

	/**
	 * Return Least Common Subsumer (most specialized concept common to both hierarchies).
	 * 
	 * @param hypernyms1
	 * @param hypernyms2
	 * @return
	 */
//	public Concept getLCS(Concept concept1, Concept concept2) {
//		
//		List<Concept> hypernyms1 = getHypernyms(concept1);
//		List<Concept> hypernyms2 = getHypernyms(concept2);
//		
//		Concept lastMatchingConcept = null;
//		
//		for (Concept c1 : hypernyms1) {
//			for (Concept c2 : hypernyms2) {
//				if (!c1.getLemma().equalsIgnoreCase(c2.getLemma()))
//					return lastMatchingConcept;
//				
//				lastMatchingConcept = concept1;
//			}
//		}
//		
//		return lastMatchingConcept;
//	}

}
