package ch.hesge.sedypro.utils;

import edu.stanford.nlp.ling.TaggedWord;

public class TagUtils {

	/**
	 * <code>
	 * Standford Tag list
	 *  
	 * references:
	 * 		https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
	 * 		https://web.stanford.edu/~jurafsky/slp3/10.pdf
	 * 
	 * 		Number		Tag			Description											Exemple
	 * 		1.			CC			Coordinating conjunction								and, but, or
	 * 		2.			CD			Cardinal number										one, two
	 * 		3.			DT			Determiner											a, the
	 * 		4.			EX			Existential there									there
	 * 		5.			FW			Foreign word											mea culpa
	 * 		6.			IN			Preposition or subordinating conjunction				of, in, by
	 * 		7.			JJ			Adjective											yellow
	 * 		8.			JJR			Adjective, comparative								bigger
	 * 		9.			JJS			Adjective, superlative								wildest
	 * 		10.			LS			List item marker										1, 2, One
	 * 		11.			MD			Modal												can, should
	 * 		12.			NN			Noun, singular or mass								llama
	 * 		13.			NNS			Noun, plural											llamas
	 * 		14.			NNP			Proper noun, singular								IBM
	 * 		15.			NNPS			Proper noun, plural									Carolinas
	 * 		24.			SYM			Symbol												+,%, &
	 * 		25.			TO			to													to
	 * 		26.			UH			Interjection											ah, oops
	 * 		27.			VB			Verb, base form										eat
	 * 		28.			VBD			Verb, past tense										ate
	 * 		29.			VBG			Verb, gerund or present participle					eating
	 * 		30.			VBN			Verb, past participle								eaten
	 * 		31.			VBP			Verb, non-3rd person singular present				eat
	 * 		32.			VBZ			Verb, 3rd person singular present					eats
	 * 		33.			WDT			Wh-determiner										which, that
	 * 		34.			WP			Wh-pronoun											what, who
	 * 		35.			WP$			Possessive wh-pronoun								whose
	 * 		36.			WRB			Wh-adverb											how, where
	 * <code>
	 * 
	 */
	
	/**
	 * Return true if word is a form of VERB.
	 * 
	 * 		11.			MD			Modal												can, should
	 * 		27.			VB			Verb, base form										eat
	 * 		28.			VBD			Verb, past tense										ate
	 * 		29.			VBG			Verb, gerund or present participle					eating
	 * 		30.			VBN			Verb, past participle								eaten
	 * 		31.			VBP			Verb, non-3rd person singular present				eat
	 * 		32.			VBZ			Verb, 3rd person singular present					eats
	 * 
	 * 
	 * @param word
	 * @return
	 */
	public static boolean isVerb(TaggedWord word) {
		
		switch (word.tag()) {
			case "VB":
			case "VBD":
			case "VBG":
			case "VBN":
			case "VBP":
			case "VBZ":
			case "MD":
				return true;
		}
		
		return false;
	}

	/**
	 * Return true if word is a form of NOUN.
	 * 
	 * 		12.			NN			Noun, singular or mass								llama
	 * 		13.			NNS			Noun, plural											llamas
	 * 		14.			NNP			Proper noun, singular								IBM
	 * 		15.			NNPS			Proper noun, plural									Carolinas
	 * 
	 * 
	 * @param word
	 * @return
	 */
	public static boolean isNoun(TaggedWord word) {
		
		switch (word.tag()) {
			case "NN":
			case "NNS":
			case "NNP":
			case "NNPS":
				return true;
		}
		
		return false;
	}

	/**
	 * Return true if word is a form of NOUN.
	 * 
	 * 		2.			CD			Cardinal number										one, two
	 * 		3.			DT			Determiner											a, the
	 * 		4.			EX			Existential there									there
	 * 		5.			FW			Foreign word											mea culpa
	 * 		6.			IN			Preposition or subordinating conjunction				of, in, by
	 * 		7.			JJ			Adjective											yellow
	 * 		8.			JJR			Adjective, comparative								bigger
	 * 		9.			JJS			Adjective, superlative								wildest
	 * 		10.			LS			List item marker										1, 2, One
	 * 		26.			UH			Interjection											ah, oops
	 * 		33.			WDT			Wh-determiner										which, that
	 * 		34.			WP			Wh-pronoun											what, who
	 * 		35.			WP$			Possessive wh-pronoun								whose
	 * 		36.			WRB			Wh-adverb											how, where
	 * 
	 * @param word
	 * @return
	 */
	public static boolean isComplement(TaggedWord word) {
		
		switch (word.tag()) {
			case "CD":
			case "DT":
			case "EX":
			case "FW":
			case "IN":
			case "JJ":
			case "JJR":
			case "JJS":
			case "LS":
			case "UH":
			case "WDT":
			case "WP":
			case "WP$":
			case "WRB":
				return true;
		}
		
		return false;
	}
}
