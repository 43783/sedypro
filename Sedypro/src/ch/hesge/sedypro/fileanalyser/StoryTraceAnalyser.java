package ch.hesge.sedypro.fileanalyser;

import java.io.IOException;

import ch.hesge.sedypro.fileanalyser.storytrace.ScoreMatrixGenerator1;
import ch.hesge.sedypro.fileanalyser.storytrace.ScoreMatrixGenerator2;
import ch.hesge.sedypro.fileanalyser.storytrace.StoryTraceLatexGenerator;
import ch.hesge.sedypro.fileanalyser.storytrace.WordCoverageInfoGenerator;

public class StoryTraceAnalyser {

	/**
	 * Default constructor
	 */
	public StoryTraceAnalyser() {
	}

	/**
	 * Generate annotated word converage information
	 * 
	 * @throws IOException
	 */
	public void doWordCoverageInfoGenerator() throws IOException {
		
		String storyDictionaryFilename = FileAnalyser.TMP_FOLDER    + "03.story-dictionary.txt";
		String traceDictionaryFilename = FileAnalyser.TMP_FOLDER    + "04.trace-dictionary.txt";
		String storyTranslatedFilename = FileAnalyser.TMP_FOLDER    + "05.story-translated-dictionary.txt";
		String coverageFilename        = FileAnalyser.OUTPUT_FOLDER + "1.word-coverage-report.txt";
		System.out.println("WordCoverageInfoGenerator...");
		new WordCoverageInfoGenerator().doGenerate(storyDictionaryFilename, traceDictionaryFilename, storyTranslatedFilename, coverageFilename);
	}
	
	/**
	 * Generate story/trace matrix
	 * 
	 * @throws IOException
	 */
	public void doScoringMatrixGenerator1() throws IOException {
		
		String storyMatrixFilename     = FileAnalyser.TMP_FOLDER + "08.story-matrix.csv";
		String traceMatrixFilename     = FileAnalyser.TMP_FOLDER + "09.trace-matrix.csv";
		String traceDictionaryFilename = FileAnalyser.TMP_FOLDER + "04.trace-dictionary.txt";
		String storyTranslatedFilename = FileAnalyser.TMP_FOLDER + "05.story-translated-dictionary.txt";
		
		System.out.println("ScoreMatrixGenerator1...");
		
		ScoreMatrixGenerator1 matrixGenerator = new ScoreMatrixGenerator1();
		matrixGenerator.doGenerate(storyMatrixFilename, traceMatrixFilename, storyTranslatedFilename, traceDictionaryFilename);

		String excelFilename = FileAnalyser.TMP_FOLDER + "10.score-matrix.csv";
		matrixGenerator.saveMatrixToExcel(excelFilename);
		
		String excelWithHeaderFilename = FileAnalyser.TMP_FOLDER + "10.score-matrix-with-headers.csv";
		matrixGenerator.saveMatrixWithHeadersToExcel(excelWithHeaderFilename);
	}
	
	/**
	 * Generate story/trace matrix
	 * 
	 * @throws IOException
	 */
	public void doScoringMatrixGenerator2() throws IOException {
		
		String storyMatrixFilename     = FileAnalyser.TMP_FOLDER + "08.story-matrix.csv";
		String traceMatrixFilename     = FileAnalyser.TMP_FOLDER + "09.trace-matrix.csv";
		String traceDictionaryFilename = FileAnalyser.TMP_FOLDER + "04.trace-dictionary.txt";
		String storyTranslatedFilename = FileAnalyser.TMP_FOLDER + "05.story-translated-dictionary.txt";

		System.out.println("ScoreMatrixGenerator2...");
		
		ScoreMatrixGenerator2 matrixGenerator = new ScoreMatrixGenerator2();
		matrixGenerator.doGenerate(storyMatrixFilename, traceMatrixFilename, storyTranslatedFilename, traceDictionaryFilename);

		String excelFilename = FileAnalyser.TMP_FOLDER + "11.score-matrix.csv";
		matrixGenerator.saveMatrixToExcel(excelFilename);
		
		String excelWithHeaderFilename = FileAnalyser.TMP_FOLDER + "11.score-matrix-with-headers.csv";
		matrixGenerator.saveMatrixWithHeadersToExcel(excelWithHeaderFilename);
	}
	
	/**
	 * Generate story/trace latex file
	 * 
	 * @throws IOException
	 */
	public void doStoryTraceLatexGenerator() throws IOException {
		
		String storyMatrixFilename      = FileAnalyser.TMP_FOLDER + "08.story-matrix.csv";
		String traceMatrixFilename      = FileAnalyser.TMP_FOLDER + "09.trace-matrix.csv";
		String scoreMatrix1Filename     = FileAnalyser.TMP_FOLDER + "10.score-matrix.csv";
		String scoreMatrix2Filename     = FileAnalyser.TMP_FOLDER + "11.score-matrix.csv";

		String storyFilename            = FileAnalyser.TMP_FOLDER + "01.cleaned-story.txt";
		String mappingFilename          = FileAnalyser.INPUT_FOLDER + "4.manual-mapping.txt";
		String storyDictionaryFilename  = FileAnalyser.TMP_FOLDER + "05.story-translated-dictionary.txt";
		String traceDictionaryFilename  = FileAnalyser.TMP_FOLDER + "04.trace-dictionary.txt";
		String latexFilename            = FileAnalyser.OUTPUT_FOLDER + "2.storytrace-matching.tex";
		
		System.out.println("StoryTraceLatexGenerator...");
		StoryTraceLatexGenerator generator = new StoryTraceLatexGenerator();
		generator.doGenerate(storyMatrixFilename, traceMatrixFilename,  scoreMatrix1Filename, scoreMatrix2Filename, storyFilename, mappingFilename, storyDictionaryFilename, traceDictionaryFilename, latexFilename);
	
	}

//	/**
//	 * Generate annotated story versus trace (manual)
//	 * 
//	 * @throws IOException
//	 */
//	public void doStoryTraceManualMappingGenerator() throws IOException {
//		
//		String storyFilename   = FileAnalyser.TMP_FOLDER + "01.cleaned-story.txt";
//		String traceFilename   = FileAnalyser.TMP_FOLDER + "02.cleaned-trace.txt";
//		String mappingFilename = FileAnalyser.INPUT_FOLDER + "4.manual-mapping.txt";
//		String outputFilename  = FileAnalyser.OUTPUT_FOLDER + "3.storytrace-manual-mapping.txt";
//
//		System.out.println("StoryTraceManualMappingGenerator...");
//		new StoryTraceManualMappingGenerator().doGenerate(storyFilename, traceFilename, mappingFilename, outputFilename);
//	}
//	
//	/**
//	 * Generate annotated story versus trace (automatic)
//	 * 
//	 * @throws IOException
//	 */
//	public void doStoryTraceAutoMappingGenerator() throws IOException {
//		
//		String storyFilename  = FileAnalyser.TMP_FOLDER + "01.cleaned-story.txt";
//		String traceFilename  = FileAnalyser.TMP_FOLDER + "02.cleaned-trace.txt";
//		String matrixFilename = FileAnalyser.TMP_FOLDER + "12.scoring-matrix.csv";
//		String outputFilename = FileAnalyser.OUTPUT_FOLDER + "4.storytrace-auto-mapping.txt";
//
//		System.out.println("StoryTraceAutoMappingGenerator...");
//		new StoryTraceAutoMappingGenerator().doGenerate(storyFilename, traceFilename, matrixFilename, outputFilename);
//	}

}
