package ch.hesge.sedypro.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import ch.hesge.sedypro.utils.Concept;
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

public class SentenceMatcher2 {

	private static IDictionary dictionary;
	
	public SentenceMatcher2() {
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
	
	public void populateWordSenses(Word word) {
		
		IIndexWord idxWord = dictionary.getIndexWord(word.getLemma(), word.getPos());
		
		if (idxWord != null) {
			
			for (IWordID wordId : idxWord.getWordIDs()) {
				
				ISynset synset = dictionary.getSynset(wordId.getSynsetID());
				Concept concept = new Concept();
				concept.setSynset(synset);
				
				word.getSenses().add(concept);
			}
		}
	}

//	public void populateHypernyms(Concept concept) {
//		
//		List<ISynsetID> synsetIds = concept.getSynset().getRelatedSynsets(Pointer.HYPERNYM);
//		
//		for (ISynsetID synsetId : synsetIds) {
//
//			ISynset synset = dictionary.getSynset(synsetId);
//			Concept hypernym = new Concept(synset);
//			
//			List<String> domains = getDomains(hypernym);
//			hypernym.getDomains().addAll(domains);
//			
//			populateHypernyms(hypernym);
//
//			concept.getHypernyms().add(hypernym);
//		}
//	}

	public String getConcepts(String sentence) throws Exception {

		String requestUrl = "https://babelfy.io/v1/disambiguate?text=" + URLEncoder.encode(sentence, "UTF-8")
				+ "&lang=en&key=key";
		URLConnection urlConnection = new URL(requestUrl).openConnection();

		BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

		String result = "";
		String line;
		while ((line = reader.readLine()) != null) {
			result += line;
		}
		reader.close();
		
		JSONObject jsonResult = (JSONObject) new JSONParser().parse(new StringReader(result));

		return result.toString();
	}
	
	public Set<String> getGlossTerms(Concept concept) {
		
		Set<String> glossTerms = new HashSet<>();
		
		String gloss = concept.getSynset().getGloss();
		gloss = gloss.replaceAll("['`\",\\.,\\;:\\(\\)]*", "");
		
		// Tokenize the sentence (stanford tokenizer)
    	List<List<HasWord>> wordList = MaxentTagger.tokenizeText(new StringReader(gloss));
    	
    	// Then extract each gloss term
    	if (!wordList.isEmpty()) {
    		
    	    for (HasWord word : wordList.get(0)) {
    	    	glossTerms.add(word.word());
    	    }
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

//	public List<String> getAllDomains(Concept concept) {
//
//    	Set<String> domainSet = new HashSet<>();
//    	
//    	for (String domain : concept.getDomains()) {
//    		domainSet.add(domain);
//    	}
//
//    	List<List<Concept>> hypernymPaths = getHypernymPaths(concept);
//    	
//    	for (List<Concept> conceptList : hypernymPaths) {
//    		for (Concept c : conceptList) {
//    			for (String d : c.getDomains()) {
//    	    		domainSet.add(d);
//    			}
//    		}
//    	}
//    	
//    	return new ArrayList<String>(domainSet);
//    }

//	public List<Concept> getDomains(Concept concept) {
//		
//		List<Concept> domains = new ArrayList<>();
//		
//		List<ISynsetID> synsetIds = new ArrayList(concept.getSynset().getRelatedSynsets(Pointer.DOMAIN));
//		synsetIds.addAll(concept.getSynset().getRelatedSynsets(Pointer.TOPIC));
//		synsetIds.addAll(concept.getSynset().getRelatedSynsets(Pointer.REGION));
//		synsetIds.addAll(concept.getSynset().getRelatedSynsets(Pointer.USAGE));
//		
//		for (ISynsetID synsetId : synsetIds) {
//
//			ISynset synset = dictionary.getSynset(synsetId);
//			Concept domain = new Concept(synset);
//			domains.add(domain);
//		}
//
//		return domains;
//    }

	public List<String> getDomains(Concept concept) {
		
		List<String> domains = new ArrayList<>();
		
		try (Scanner fileScanner = new Scanner(new File("lib/wordnet.domains/wn-domains-3.2-20070223"))) {

			String conceptKey = concept.getSynset().getID().toString().toLowerCase().substring(4);
			String line = fileScanner.findWithinHorizon(conceptKey + ".*", 0);
			
			if (line != null) {
				
			    String[] domainParts = line.split("[\\t\\s]");

				for (int i=1; i<domainParts.length; i++) {
					domains.add(domainParts[i]);
				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	    return domains;
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
