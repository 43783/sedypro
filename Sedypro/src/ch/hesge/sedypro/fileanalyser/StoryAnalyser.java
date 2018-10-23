package ch.hesge.sedypro.fileanalyser;

import java.io.IOException;

import ch.hesge.sedypro.fileanalyser.story.StoryCleaner;
import ch.hesge.sedypro.fileanalyser.story.StoryDictionaryGenerator;
import ch.hesge.sedypro.fileanalyser.story.StoryMatrixGenerator;
import ch.hesge.sedypro.fileanalyser.story.StoryTranslationGenerator;
import ch.hesge.sedypro.fileanalyser.story.StoryWordsGenerator;

public class StoryAnalyser {

	/**
	 * Default constructor
	 */
	public StoryAnalyser() {
	}

	/**
 	 * Clean story file
 	 * 
 	 * @throws IOException
 	 */
	public void doStoryCleaner() throws IOException {

		String storyFilename   = FileAnalyser.INPUT_FOLDER + "1.story.txt";
		String cleanedFilename = FileAnalyser.TMP_FOLDER   + "01.cleaned-story.txt";

		System.out.println("StoryCleaner...");
		new StoryCleaner().doGenerate(storyFilename, cleanedFilename);
	}
 	
	/**
	 * Generate story dictionary
	 * 
	 * @throws IOException
	 */
	public void doStoryDictionaryGenerator() throws IOException {
		
		String storyFilename      = FileAnalyser.TMP_FOLDER + "01.cleaned-story.txt";
		String dictionaryFilename = FileAnalyser.TMP_FOLDER + "03.story-dictionary.txt";
		String stopWordFilename   = "data/conf/stop-word-list.txt";

		System.out.println("StoryDictionaryGenerator...");
		new StoryDictionaryGenerator().doGenerate(storyFilename, dictionaryFilename, stopWordFilename);
	}

	/**
	 * Generate translation dictionary
	 * 
	 * @throws IOException
	 */
	public void doStoryTranslationGenerator() throws IOException {
		
		String dictionaryFilename           = FileAnalyser.TMP_FOLDER   + "03.story-dictionary.txt";
		String translationsFilename         = FileAnalyser.INPUT_FOLDER + "3.translations.txt";
		String translatedDictionaryFilename = FileAnalyser.TMP_FOLDER   + "05.story-translated-dictionary.txt";

		System.out.println("StoryTranslationGenerator...");
		new StoryTranslationGenerator().doGenerate(dictionaryFilename, translationsFilename, translatedDictionaryFilename);
	}

	/**
	 * Generate storyd word dictionary
	 * 
	 * @throws IOException
	 */
	public void doStoryWordsGenerator() throws IOException {
		
		String dictionaryFilename = FileAnalyser.TMP_FOLDER + "05.story-translated-dictionary.txt";
		String storyWordsFilename = FileAnalyser.TMP_FOLDER + "06.story-words.txt";

		System.out.println("StoryWordsGenerator...");
		new StoryWordsGenerator().doGenerate(dictionaryFilename, storyWordsFilename);
	}

	/**
	 * Generate story matrix
	 * 
	 * @throws IOException
	 */
	public void doStoryMatrixGenerator() throws IOException {
		
		String storyDictionaryFilename   = FileAnalyser.TMP_FOLDER + "05.story-translated-dictionary.txt";
		String traceDictionaryFilename   = FileAnalyser.TMP_FOLDER + "04.trace-dictionary.txt";
		String matrixFilename            = FileAnalyser.TMP_FOLDER + "08.story-matrix.csv";
		String matrixWithHeadersFilename = FileAnalyser.TMP_FOLDER + "08.story-matrix-with-headers.csv";

		System.out.println("StoryMatrixGenerator...");
		
		StoryMatrixGenerator matrixGenerator = new StoryMatrixGenerator();
		matrixGenerator.doGenerate(storyDictionaryFilename, traceDictionaryFilename);
		matrixGenerator.saveMatrix(matrixFilename);
		matrixGenerator.saveMatrixWithHeaders(matrixWithHeadersFilename);
	}

}
