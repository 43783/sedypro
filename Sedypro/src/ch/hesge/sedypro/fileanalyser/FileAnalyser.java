package ch.hesge.sedypro.fileanalyser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ch.hesge.sedypro.utils.ConsoleUtils;
import ch.hesge.sedypro.utils.FileUtils;
import ch.hesge.sedypro.utils.StringUtils;

public class FileAnalyser {

	private static StoryAnalyser storyAnalyser = new StoryAnalyser();;
	private static TraceAnalyser traceAnalyser = new TraceAnalyser();;
	private static StoryTraceAnalyser storyTraceAnalyser = new StoryTraceAnalyser();
	
	public static String ROOT_FOLDER   = "data/analysis/atm/";
	public static String INPUT_FOLDER  = ROOT_FOLDER + "input/";
	public static String OUTPUT_FOLDER = ROOT_FOLDER + "output/";
	public static String TMP_FOLDER   = ROOT_FOLDER + "tmp/";
	
	private static String VERSION = "2.0.1";

	/**
	 * Default constructor
	 */
	public FileAnalyser() {
	}

	/**
	 * Startup method
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			new FileAnalyser().start(args);
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
		createDefaultFolders();
		
		storyAnalyser.doStoryCleaner();
		traceAnalyser.doTraceCleaner();	
		
		storyAnalyser.doStoryDictionaryGenerator();
		traceAnalyser.doTraceDictionaryGenerator();
		
		storyAnalyser.doStoryTranslationGenerator();

		storyAnalyser.doStoryWordsGenerator();
		traceAnalyser.doTraceWordsGenerator();

		storyAnalyser.doStoryMatrixGenerator();
		traceAnalyser.doTraceMatrixGenerator();
		storyTraceAnalyser.doScoringMatrixGenerator1();
		storyTraceAnalyser.doScoringMatrixGenerator2();
		
		storyTraceAnalyser.doWordCoverageInfoGenerator();
		storyTraceAnalyser.doStoryTraceLatexGenerator();
		
		ConsoleUtils.println("\nGeneration done.");

 	}

	/**
	 * Print copyright and version
	 */
	private void printBanner() {
		ConsoleUtils.println("FileAnalyser " + VERSION);
		ConsoleUtils.println("Copyright (c) University of Geneva, Switzerland, 2018\n");
	}
	
	/**
	 * Create default folder used by the application.
	 * 
	 * @throws IOException
	 */
	private void createDefaultFolders() throws IOException {
		
		Path inputPath  = Paths.get(INPUT_FOLDER).toAbsolutePath().normalize();
		Path outputPath  = Paths.get(OUTPUT_FOLDER).toAbsolutePath().normalize();
		Path tmpPath  = Paths.get(TMP_FOLDER).toAbsolutePath().normalize();

		// Create input folder, if missing
		if (!inputPath.toFile().exists()) {
			Files.createDirectory(inputPath);
		}

		// Delete old output folder
		if (outputPath.toFile().exists()) {
			FileUtils.removeFolder(outputPath);
		}
		
		// Delete old working/temporary folder
		if (tmpPath.toFile().exists()) {
			FileUtils.removeFolder(tmpPath);
		}

		// Create fresh new folders
		Files.createDirectory(tmpPath);
		Files.createDirectory(outputPath);
	}
}
