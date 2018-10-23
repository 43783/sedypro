package ch.hesge.sedypro.semanalyser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.hesge.sedypro.utils.Concept;
import ch.hesge.sedypro.utils.ConsoleUtils;
import ch.hesge.sedypro.utils.FileUtils;
import ch.hesge.sedypro.utils.Sentence;
import ch.hesge.sedypro.utils.StringUtils;
import ch.hesge.sedypro.utils.Word;
import edu.mit.jwi.item.POS;

public class SemanticAnalyser {

	// Private attributes
	private Path scenarioPath;
	private Path tracePath;
	private List<Sentence> scenarioSentences;
	private List<Sentence> traceSentences;
	
	private static String VERSION = "1.0.2";
	private static Pattern TAG_REGEXP_PATTERN;
	
	static {
		String TRACELINE_REGEXP = "\\((?<tag>\\w+):(?<lemma>\\w+)\\)";
		TAG_REGEXP_PATTERN = Pattern.compile(TRACELINE_REGEXP);
	}
	
	/**
	 * Default constructor
	 */
	public SemanticAnalyser() {
	}

	/**
	 * Startup method
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {

		try {
			new SemanticAnalyser().start(args);
		}
		catch (Exception e) {
			ConsoleUtils.println("exception raised: " + StringUtils.toThrowableString(e));
		}
	}
	
	/**
	 * Initialize the engine before starting.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
 	public void start(String[] args) throws FileNotFoundException, IOException {

		printBanner();
		
		if (args == null || args.length < 1) {
			printUsage();
		}
		else {

			// Check if specified folders are valid
			checkInputFiles(args);
			
			System.out.println("loading scenario sentences...");
			loadScenarioFile();

			System.out.println("loading trace sentences...");
			loadTraceFiles();
			
			System.out.println("calculating similarities...");
			calculateSimilarities();
		}
	}
 	
	private void calculateSimilarities() {

		SentenceMatcher sentenceMatcher = new SentenceMatcher();

		for (Sentence scenarioSentence : scenarioSentences) {

			System.out.println("==> " + toString(scenarioSentence));

			sentenceMatcher.populateWordSenses(scenarioSentence);
			sentenceMatcher.disambiguate(scenarioSentence);

			for (Word word : scenarioSentence.getWords()) {

				System.out.println("  word: " + word.getLemma() + ", pos: " + word.getPos().toString());

				if (word.getSenses().isEmpty())
					continue;

				for (Concept wordSense : word.getSenses()) {

					System.out.println("    sense: " + wordSense.getLemma());
					System.out.println("    gloss: " + wordSense.getSynset().getGloss());

					for (List<Concept> hypernymPath : sentenceMatcher.getHypernymPaths(wordSense)) {

						String hypernymList = "";
						for (Concept hypernym : hypernymPath) {
							hypernymList += hypernym.getLemma() + ",";
						}

						hypernymList = StringUtils.removeLastChar(hypernymList);
						System.out.println("    hypernyms: " + hypernymList);
					}
				}
			}
		}
	}
 	
	private String toString(Sentence sentence) {
		
		String strSentence = "";
		
		for(Word word : sentence.getWords()) {
			strSentence += word.getLemma() + " ";
		}
		
		strSentence = StringUtils.removeLastChar(strSentence);
		
		return strSentence;
	}
	
	private String[] toArray(Sentence sentence) {
		
		String[] wordArray = new String[sentence.getWords().size()];
		
		for(int i=0; i<sentence.getWords().size(); i++) {
			wordArray[i] = sentence.getWords().get(i).getLemma();
		}
		
		return wordArray;
	}

	private void loadScenarioFile() throws IOException {

		scenarioSentences = new ArrayList<>();
		List<String> scenarioLines = FileUtils.readFileAsStringList(scenarioPath);
		
		for (String textLine : scenarioLines) {
			
			Sentence sentence = new Sentence();
			
			// No indentation for stories
			sentence.setIndent(0);

			// Rebuild each word from text line
			Matcher regexMatcher = TAG_REGEXP_PATTERN.matcher(textLine);
			
			while (regexMatcher.find()) {

				Word newWord = new Word();
				String tag = regexMatcher.group("tag");
				String lemma = regexMatcher.group("lemma");
				POS pos = POS.getPartOfSpeech(tag.charAt(0));
				newWord.setLemma(lemma);
				newWord.setPos(pos);

				sentence.getWords().add(newWord);
			}
			
			scenarioSentences.add(sentence);
		}
	}

	private void loadTraceFiles() throws IOException {
		
		traceSentences = new ArrayList<>();
		List<String> traceLines = FileUtils.readFileAsStringList(tracePath);
		
		for (String textLine : traceLines) {
			
			Sentence sentence = new Sentence();

			// Retrieve indentation
			int indent = StringUtils.indexOfAnyBut(textLine, " \t");
			sentence.setIndent(indent);
			
			// Rebuild each word from text line
			Matcher regexMatcher = TAG_REGEXP_PATTERN.matcher(textLine);
			
			while (regexMatcher.find()) {

				Word newWord = new Word();
				String tag = regexMatcher.group("tag");
				String lemma = regexMatcher.group("lemma");
				POS pos = POS.getPartOfSpeech(tag.charAt(0));
				newWord.setLemma(lemma);
				newWord.setPos(pos);

				sentence.getWords().add(newWord);
			}

			traceSentences.add(sentence);
		}
	}
	

	/**
	 * Verify that source folder specified in argument is valid
	 * and is present on filesystem.
	 * 
	 * @param args
	 */
	public void checkInputFiles(String[] args) {
		
		String scenarioFilename = args[0];
		
		// Retrieve normalized absolute path
		scenarioPath = Paths.get(scenarioFilename).toAbsolutePath().normalize();

		// Check if file really exists
		if (!scenarioPath.toFile().exists()) {
			throw new RuntimeException("scenario-file doesn't exist !");
		}

		String traceFilename = args[1];
		
		// Retrieve normalized absolute path
		tracePath = Paths.get(traceFilename).toAbsolutePath().normalize();

		// Check if file really exists
		if (!tracePath.toFile().exists()) {
			throw new RuntimeException("trace-file doesn't exist !");
		}
	}
	
	/**
	 * Print copyright and version
	 */
	private void printBanner() {
		ConsoleUtils.println("SemanticAnalyser " + VERSION);
		ConsoleUtils.println("Copyright (c) University of Geneva, Switzerland, 2017\n");
	}

	/**
	 * Print how to launch the tool
	 */
	private void printUsage() {
		ConsoleUtils.println("usage: SemanticAnalyser scenario-file trace-file\n");
		ConsoleUtils.println("description:");
		ConsoleUtils.println("   SemanticAnalyser analyses the semantics of both files");
		ConsoleUtils.println("   and try to match each scenario step to trace region.");
	}
	
}
