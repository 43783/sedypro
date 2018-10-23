package ch.hesge.sedypro.fileanalyser;

import java.io.IOException;

import ch.hesge.sedypro.fileanalyser.trace.TraceCleaner;
import ch.hesge.sedypro.fileanalyser.trace.TraceDictionaryGenerator;
import ch.hesge.sedypro.fileanalyser.trace.TraceMatrixGenerator;
import ch.hesge.sedypro.fileanalyser.trace.TraceWordsGenerator;

public class TraceAnalyser {

	/**
	 * Default constructor
	 */
	public TraceAnalyser() {
	}
	
	/**
 	 * Clean trace file
 	 * 
 	 * @throws IOException
 	 */
	public void doTraceCleaner() throws IOException {
		
		String traceFilename            = FileAnalyser.INPUT_FOLDER + "2.trace.txt";
		String cleanedFilename          = FileAnalyser.TMP_FOLDER   + "02.cleaned-trace.txt";
		String traceCleanerListFilename = "data/conf/trace-cleaner-list.txt";

		System.out.println("TraceCleaner...");
		new TraceCleaner().doGenerate(traceFilename, cleanedFilename, traceCleanerListFilename);
	}
 	
	/**
	 * Generate trace dictionary
	 * 
	 * @throws IOException
	 */
	public void doTraceDictionaryGenerator() throws IOException {
		
		String traceFilename      = FileAnalyser.TMP_FOLDER + "02.cleaned-trace.txt";
		String dictionaryFilename = FileAnalyser.TMP_FOLDER + "04.trace-dictionary.txt";
		String stopWordFilename   = "data/conf/stop-word-list.txt";

		System.out.println("TraceDictionaryGenerator...");
		new TraceDictionaryGenerator().doGenerate(traceFilename, dictionaryFilename, stopWordFilename);
	}

	/**
	 * Generate trace word dictionary
	 * 
	 * @throws IOException
	 */
	public void doTraceWordsGenerator() throws IOException {
		
		String dictionaryFilename = FileAnalyser.TMP_FOLDER + "04.trace-dictionary.txt";
		String traceWordsFilename = FileAnalyser.TMP_FOLDER + "07.trace-words.txt";

		System.out.println("TraceWordsGenerator...");
		new TraceWordsGenerator().doGenerate(dictionaryFilename, traceWordsFilename);
	}
	
	/**
	 * Generate trace matrix
	 * 
	 * @throws IOException
	 */
	public void doTraceMatrixGenerator() throws IOException {
		
		String storyDictionaryFilename   = FileAnalyser.TMP_FOLDER + "05.story-translated-dictionary.txt";
		String traceDictionaryFilename   = FileAnalyser.TMP_FOLDER + "04.trace-dictionary.txt";
		String matrixFilename            = FileAnalyser.TMP_FOLDER + "09.trace-matrix.csv";
		String matrixWithHeadersFilename = FileAnalyser.TMP_FOLDER + "09.trace-matrix-with-headers.csv";

		System.out.println("TraceMatrixGenerator...");
		
		TraceMatrixGenerator matrixGenerator = new TraceMatrixGenerator();
		matrixGenerator.doGenerate(storyDictionaryFilename, traceDictionaryFilename);
		matrixGenerator.saveMatrix(matrixFilename);
		matrixGenerator.saveMatrixWithHeaders(matrixWithHeadersFilename);
	}
}
