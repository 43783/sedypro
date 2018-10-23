package ch.hesge.sedypro.test;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

/**
 * 
 * http://ws4jdemo.appspot.com/?mode=s&s1=System+displays+the+application+start+view&w1=&s2=Screen+init+Components&w2=
 *
 */
public class SimilarityCalculationDemo {
	
	private static ILexicalDatabase db = new NictWordNet();
	
	private static RelatednessCalculator[] relatednessCalculators = {
			
			new WuPalmer(db), 			// WUP
			new Resnik(db), 			// RES
			new JiangConrath(db),		// JCN 
			new Lin(db), 			    // LIN
			new LeacockChodorow(db), 	// LCH
			new Path(db),				// PATH
			new Lesk(db),  				// LESK
			new HirstStOnge(db), 		// HSO
			
	};
	
	public static void main(String[] args) {

		WS4JConfiguration.getInstance().setMFS(true);

		long t0 = System.currentTimeMillis();
		
		String firstWord = "screen";
		String secondWord = "system";

		System.out.println("comparing '" + firstWord + "' and '" + secondWord + "'...");
		
		for ( RelatednessCalculator calculator : relatednessCalculators ) {
			double s = calculator.calcRelatednessOfWords(firstWord, secondWord);
			System.out.println( calculator.getClass().getName()+"\t"+s );
		}
		
		
		long t1 = System.currentTimeMillis();
		System.out.println( "done in "+(t1-t0)+" msec." );
	}
}