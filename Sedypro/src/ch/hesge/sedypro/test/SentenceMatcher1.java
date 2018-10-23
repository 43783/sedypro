package ch.hesge.sedypro.test;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class SentenceMatcher1 {

	private static ILexicalDatabase db = new NictWordNet();

	private static RelatednessCalculator[] RELATEDNESS_CALCULATORS = {
			
			new WuPalmer(db), 						// WUP		shortest path between synsets
			new Resnik(db), 						// RES		lowest super-ordinate (most specific common subsumer) 
			new JiangConrath(db),					// JCN 		conditional probability of encountering an instance of a child-synset given an instance of a parent synset
			new Lin(db), 			    			// LIN		similar to JCN with small modification. 
			new LeacockChodorow(db), 				// LCH		shortest path between synsets
			new edu.cmu.lti.ws4j.impl.Path(db),		// PATH		the number of nodes along the shortest path between the senses in the 'is-a' hierarchies
			new Lesk(db),  							// LESK		gloss overlapping
			new HirstStOnge(db), 					// HSO		semantic proxymity based on subtrees in the horizontal, upward and downward direction
	
	};

	
	public static void main(String[] args) {

		/*
		String sent1 = "Screen The boy is playing with a dog.";
		String sent2 = "Display The kid is playing with his pet.";
		 */
		
		String sent1 = "Finally a massive hurricane attacked my home";
		String sent2 = "Eventually a huge cyclone hit the entrance of my house";
		
		String[] words1 = sent1.split(" ");
		String[] words2 = sent2.split(" ");

		SentenceMatcher1 matcher = new SentenceMatcher1();
		matcher.computeSimilarities(words1,  words2);
		matcher.computeAggregatedSimilarities(words1,  words2);
		
		double similarityScore = matcher.getSimilarityScore(words1,  words2, 0.4);
		
		System.out.println("Similarity score: " + similarityScore);
	}
	
	private void computeSimilarities(String[] words1, String[] words2) {

		for (RelatednessCalculator rc : RELATEDNESS_CALCULATORS) {

			// Print algo name
			System.out.println(rc.getClass().getSimpleName() + ":");

			// Retrieve the matching matrix
			double[][] similarityMatrix = getSimilarityMatrix(words1, words2, rc);

			// And print it
			printSimilarityMatrix(words1, words2, similarityMatrix);
		}
	}
	
	private void computeAggregatedSimilarities(String[] words1, String[] words2) {
		
		System.out.println("aggregated similarity matrix: ");

		double[][] aggregatedMatrix = getAggregatedMatrixSimilarities(words1, words2);
		printSimilarityMatrix(words1, words2, aggregatedMatrix);
	}
	
	public double getSimilarityScore(String[] words1, String[] words2, double threshold) {
		
		double matchingScore = 0;
		double[][] aggregatedMatrix = getAggregatedMatrixSimilarities(words1, words2);
		
		// count all cell's value greater that threhold
		for (int i = 0; i < words2.length; i++) {
			for (int j = 0; j < words1.length; j++) {
				if (aggregatedMatrix[i][j] > threshold) {
					matchingScore++;
				}
			}
		}
		
		return matchingScore/(words1.length*words2.length);		
	}
	
	public double[][] getSimilarityMatrix(String[] words1, String[] words2, RelatednessCalculator rc) {
		
		double[][] result = new double[words2.length][words1.length];
		
		for (int i = 0; i < words2.length; i++) {
			for (int j = 0; j < words1.length; j++) {
				String word1 = words2[i];
				String word2 = words1[j];
				double score = rc.calcRelatednessOfWords(words2[i], words1[j]);
				if (score > 1) score = 1;
				result[i][j] = score;
			}
		}
		return result;
	}
	
	private double[][] getAggregatedMatrixSimilarities(String[] words1, String[] words2) {
		
		double[][] aggregatedMatrix = new double[words2.length][words1.length];

		for (int i = 0; i < words2.length; i++) {
			for (int j = 0; j < words1.length; j++) {
				aggregatedMatrix[i][j] = 0;
			}
		}

		for (RelatednessCalculator rc : RELATEDNESS_CALCULATORS) {		
						
			// Retrieve the matching matrix
			double[][] similarityMatrix = getSimilarityMatrix(words1, words2, rc);
			
			for (int i = 0; i < words2.length; i++) {
				for (int j = 0; j < words1.length; j++) {
					aggregatedMatrix[i][j] += similarityMatrix[i][j];
				}
			}
		}
		
		// Normalize the matrix
		for (int i = 0; i < words2.length; i++) {
			for (int j = 0; j < words1.length; j++) {
				aggregatedMatrix[i][j] = aggregatedMatrix[i][j] < 1 ? aggregatedMatrix[i][j] : 1;
				aggregatedMatrix[i][j] = aggregatedMatrix[i][j] > 0 ? aggregatedMatrix[i][j] : 0;
			}
		}
		
		return aggregatedMatrix;
	}
	
	public void printSimilarityMatrix(String[] words1, String[] words2, double[][] similarityMatrix) {
		
		// print top header
		System.out.print(String.format("%1$-15s", ""));
		for (int k = 0; k < words1.length; k++) {
			System.out.print(String.format("%1$-15s", words1[k]));
		}
		System.out.println();

		for (int i = 0; i < words2.length; i++) {

			// print left header
			System.out.print(String.format("%1$-15s", words2[i]));

			for (int j = 0; j < words1.length; j++) {
				System.out.print(String.format("%1$-15.2f", similarityMatrix[i][j]));
			}

			System.out.println();
		}

		System.out.println();
	}
	

}
