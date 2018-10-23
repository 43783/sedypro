package ch.hesge.sedypro.fileanalyser.storytrace;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ch.hesge.sedypro.utils.DictionaryUtils;
import ch.hesge.sedypro.utils.MappingRegion;
import ch.hesge.sedypro.utils.Matrix;
import ch.hesge.sedypro.utils.StatisticUtils;
import ch.hesge.sedypro.utils.Vector;

public class StoryTraceLatexGenerator {

	private Matrix storyMatrix;
	private Matrix traceMatrix;
	private Matrix scoreMatrix1;
	private Matrix scoreMatrix2;
	private Map<Integer, Integer> storyToTraceMapping;
	private List<String> matchingWords;
	private List<String> storySteps;
	private 	List<MappingRegion> originalRegions; 
	private 	List<MappingRegion> automaticRegions; 
	private 	Map<Integer, List<MappingRegion>> mappingRegions1; 
	private 	Map<Integer, List<MappingRegion>> mappingRegions2; 
	private 	Map<Integer, List<MappingRegion>> mappingRegions3; 
	private 	Map<Integer, List<MappingRegion>> mappingRegions4; 
	
	/**
	 * Default constructor
	 */
	public StoryTraceLatexGenerator() {
	}

 	/**
	 * Generate the story/trace latex file.
 	 * 
 	 * @throws IOException 
 	 */
 	public void doGenerate(String storyMatrixFilename, String traceMatrixFilename,  String scoreMatrix1Filename, String scoreMatrix2Filename, String storyFilename, String mappingFilename, String storyDictionaryFilename, String traceDictionaryFilename, String latexFilename) throws IOException {
 		
		// Retrieve input/output paths
 		Path storyMatrixPath     = Paths.get(storyMatrixFilename).toAbsolutePath().normalize();
 		Path traceMatrixPath     = Paths.get(traceMatrixFilename).toAbsolutePath().normalize();
 		Path scoreMatrix1Path    = Paths.get(scoreMatrix1Filename).toAbsolutePath().normalize();
 		Path scoreMatrix2Path    = Paths.get(scoreMatrix2Filename).toAbsolutePath().normalize();
		Path storyPath           = Paths.get(storyFilename).toAbsolutePath().normalize();
		Path manualMappingPath   = Paths.get(mappingFilename).toAbsolutePath().normalize();
 		Path latexPath           = Paths.get(latexFilename).toAbsolutePath().normalize();
 		Path storyDictionaryPath = Paths.get(storyDictionaryFilename).toAbsolutePath().normalize();
 		Path traceDictionaryPath = Paths.get(traceDictionaryFilename).toAbsolutePath().normalize();
 		
 		// Load dictionaries, matrices and words used in matrix
		storyMatrix         = DictionaryUtils.loadMatrix(storyMatrixPath);
		traceMatrix         = DictionaryUtils.loadMatrix(traceMatrixPath);
		scoreMatrix1        = DictionaryUtils.loadMatrix(scoreMatrix1Path);
		scoreMatrix2        = DictionaryUtils.loadMatrix(scoreMatrix2Path);
		storyToTraceMapping = loadStoryToTraceMapping(manualMappingPath);
 		matchingWords       = new ArrayList<>(DictionaryUtils.loadIntersectionSet(storyDictionaryPath, traceDictionaryPath));
 		storySteps          = loadStoryLines(storyPath);
 		mappingRegions1     = StatisticUtils.computeMappingRegions1(scoreMatrix1); 
 		mappingRegions2     = StatisticUtils.computeMappingRegions2(scoreMatrix2); 
 		mappingRegions3     = StatisticUtils.computeMappingRegions3(scoreMatrix2); 
 		mappingRegions4     = StatisticUtils.computeMappingRegions4(scoreMatrix1, scoreMatrix2); 
 		originalRegions     = StatisticUtils.computeOriginalMappingPath(storyToTraceMapping, traceMatrix.getRowSize());
 		automaticRegions    = StatisticUtils.computeMappingPath(mappingRegions4, traceMatrix.getRowSize());
 		
 		// If file already exists, suppress it
		if (latexPath.toFile().exists()) {
			Files.delete(latexPath);
		}

		String latexContent = "";
		
		latexContent += "%!TEX TS-program = xelatex\n";
		latexContent += "%!TEX encoding = UTF-8 Unicode\n";
		latexContent += "\\documentclass [12pt] {article}\n";
		latexContent += "\\usepackage{pgfplots}\n";
		latexContent += "\\usepackage[labelformat=empty]{caption}\n";
		latexContent += "\\usepackage{fontspec}\n";
		latexContent += "\\usepackage{geometry}\n";
		latexContent += "\\pgfplotsset{compat=1.14}\n";
		latexContent += "\\pgfplotsset{/pgfplots/width=0.95\\textwidth}\n";
		latexContent += "\\pgfplotsset{/pgfplots/height=0.2\\textwidth}\n\n";
		latexContent += "\\pgfplotsset{/pgfplots/ytick=\\empty}\n";
		latexContent += "\\begin{document}\n";
		latexContent += "\\title{Story to trace analysis}\n";
		latexContent += "\\author{Eric Harth}\n";
		latexContent += "\\maketitle\n";
		
		latexContent += "\\clearpage\n";
		latexContent += "\\section{Matching report}\n";
		latexContent += getMappingReport(); 
		
		latexContent += "\\clearpage\n";
		latexContent += "\\section{Matching sequence}\n";
		latexContent += getMappingGrid(); 

		latexContent += "\\clearpage\n";
		latexContent += "\\section{Story analysis}\n";
		latexContent += "\n";

		for (int i = 0; i < storyMatrix.getRowSize(); i++) {

			latexContent += "\\subsection{" + storySteps.get(i) + "}\n";
			latexContent += "\\begin{tikzpicture}\n"; 
			latexContent += "\\matrix {\n"; 
			latexContent += getOccurenceGraph(i);
			latexContent += getTfidfGraph1(i);
			latexContent += getMappingGraph1(i);
			latexContent += getTfidf2Graph(i);
			latexContent += getMappingGraph2(i);
			latexContent += getTripletGraph(i);
			latexContent += getEnterExitGraph(i);
			latexContent += getMappingGraph3(i);
			latexContent += getMappingGraph4(i);
			//latexContent += getEnterExitGraph2(i);
			latexContent += "};\n"; 
			latexContent += "\\end{tikzpicture}\n"; 
			latexContent += "\n";
		}

		/*
		latexContent += "\\begin{tikzpicture}\n"; 
		latexContent += "\\matrix {\n"; 

		for (int i = 0; i < storyMatrix.getRowSize(); i++) {

			latexContent += getEnterExitGraph(i);
			
			if (i != 0 && i % 10 == 0) {
				latexContent += "};\n"; 
				latexContent += "\\end{tikzpicture}\n"; 
				latexContent += "\n";
				latexContent += "\\newpage;\n"; 
				latexContent += "\\begin{tikzpicture}\n"; 
				latexContent += "\\matrix {\n"; 
			}
		}
		latexContent += "};\n"; 
		latexContent += "\\end{tikzpicture}\n"; 
		latexContent += "\n";
		*/

		latexContent += "\\end{document}\n";

		// Finally write latex file to filesystem
		try ( PrintWriter writer = new PrintWriter(new FileWriter(latexPath.toFile())) ) {
			writer.println(latexContent);
		}
	}
 	
 	/**
	 * Build all mapping graph with automatic and manual region matching.
	 * 
	 * @return
	 */
	private String getMappingGrid() {
		
		String outputString = "";
	
		double storyHeight = 5;
		double xMax = traceMatrix.getRowSize();
		outputString += "\\begin{tikzpicture}\n";
			
		// A4 size: width = 160mm, height = 180mm
		double hSize = 138;
		double vSize = 180;
		double hScale =  1 / xMax * hSize / 10;
		double verticalPos = vSize;
		
		for(int i = 0; i < storyMatrix.getRowSize(); i++) {
	
			// Next page
			if (verticalPos < storyHeight) {
				outputString += "\\draw[black,thin] (0," + (verticalPos/10) + ") rectangle (" + ((hSize + 1)/10) + "," + (vSize/10) + ");\n";
				outputString += "\\end{tikzpicture}\n"; 
				outputString += "\\\\\n";
				outputString += "\\begin{tikzpicture}";
				verticalPos = vSize;
			}
	
			// Calculate area associated to story step
			verticalPos -= storyHeight;
						
	 		// Draw manual defined regions
//	 		if (originalRegions.get(i) != null) {
//	 			
//				MappingRegion originalMapping = originalRegions.get(i);
//		 		double xStart = originalMapping.getStartIndex() + 1;
//		 		double xEnd   = originalMapping.getEndIndex() + 1;
//	 			double yStart = verticalPos + storyHeight / 2 - storyHeight * 0.3;
//	 			double yEnd   = verticalPos + storyHeight / 2 + storyHeight * 0.3;
//	
//	 			xStart = Math.min(hSize/10, xStart * hScale);
//	 			xEnd   = Math.min(hSize/10, xEnd   * hScale);
//	 			yStart /= 10;
//	 			yEnd   /= 10;
//	 			
//				outputString += "\\fill[red!40!white] (" + xStart + "," + yStart + ") rectangle (" + xEnd + "," + yEnd + ");\n";
//	 		}
		
			// Draw all mapping regions
	 		for(MappingRegion region : mappingRegions4.get(i)) {
	 			
	 			double xStart = region.getStartIndex() + 1;
	 			double xEnd   = region.getEndIndex() + 1;
	 			double yStart = verticalPos + storyHeight / 2 - storyHeight * 0.3;
	 			double yEnd   = verticalPos + storyHeight / 2 + storyHeight * 0.3;
	 			
	 			xStart = Math.min(hSize/10, xStart * hScale);
	 			xEnd   = Math.min(hSize/10, xEnd   * hScale);
	 			yStart /= 10;
	 			yEnd   /= 10;
	
	 			outputString += "\\draw[blue, thick] (" + xStart + "," + yStart + ") rectangle (" + xEnd + "," + yEnd + ");\n";
	 		}

	 		// Draw automatic detected regions
			for(MappingRegion region : automaticRegions) {
				if (region.getStoryIndex() == i) {
					
		 			double xStart = region.getStartIndex() + 1;
		 			double xEnd   = region.getEndIndex() + 1;
		 			double yStart = verticalPos + storyHeight / 2 - storyHeight * 0.1;
		 			double yEnd   = verticalPos + storyHeight / 2 + storyHeight * 0.1;
		 			
		 			xStart = Math.min(hSize/10, xStart * hScale);
		 			xEnd   = Math.min(hSize/10, xEnd   * hScale);
		 			yStart /= 10;
		 			yEnd   /= 10;
	
		 			outputString += "\\fill[red] (" + xStart + "," + yStart + ") rectangle (" + xEnd + "," + yEnd + ");\n";
				}
			}
	
			double xStart = 0;
			double xEnd   = (hSize + 1) / 10;
			double yStart = verticalPos / 10;
			double yEnd   = (verticalPos + storyHeight) / 10;
					
	 		outputString += "\\draw[help lines] (" + xStart + "," + yStart + ") -- (" + xEnd + "," + yStart + ");\n";
	 		outputString += "\\draw[help lines] (" + xStart + "," + yEnd + ") -- (" + xEnd + "," +  yEnd + ");\n";
		}
		
		outputString += "\\draw[black,thin] (0," + (verticalPos/10) + ") rectangle (" + ((hSize + 1)/10) + "," + (vSize/10) + ");\n";
		outputString += "\\end{tikzpicture}\n"; 
		outputString += "\\\\\n";
	
		return outputString;
	}

 	/**
	 * Build all matching report.
	 * 
	 * @return
	 */
	private String getMappingReport() {
		
 		String outputString = "";
 		
 		outputString += "";
 		
 		int matchingStories = 0;
 		double totalFoundSteps = 0;
 		double totalSelectedSteps = 0;
 		double totalRelevantSteps  = 0;
 		
 		for(int i = 0; i < originalRegions.size(); i++) {
 			
 			MappingRegion originalRegion = originalRegions.get(i);
 			MappingRegion selectedRegion = automaticRegions.get(i);
 			
 			if (selectedRegion.getIntersection(originalRegion).getLength() > 0) {
 				matchingStories++;
 				totalFoundSteps    += selectedRegion.getIntersection(originalRegion).getLength();	
 				totalSelectedSteps += selectedRegion.getEndIndex() - selectedRegion.getStartIndex();
 				totalRelevantSteps += originalRegion.getEndIndex() - originalRegion.getStartIndex();
 			}
 		}

 		double precision = totalFoundSteps / totalSelectedSteps;
 		double recall    = totalFoundSteps / totalRelevantSteps;
 		double fmeasure  = 2 * ((precision * recall) / (precision + recall));
 		
 		long precisionScore = Math.round(precision * 100);
 		long recallScore    = Math.round(recall * 100);
 		long fmeasureScore  = Math.round(fmeasure * 100);
 		long matchingScore  = Math.round(matchingStories);

 		outputString += "\\begin{itemize}\n";
 		outputString += "\\item Matching story: " + matchingScore + "/" + storyMatrix.getRowSize() + " steps\n";
 		outputString += "\\item Precision score: " + precisionScore + "\\%\n";
 		outputString += "\\item Recall score: " + recallScore + "\\%\n";
 		outputString += "\\item F-measure: " + fmeasureScore + "\\%\n";
 		outputString += "\\end{itemize}\n";
 		
 		return outputString;
	}
	
	/**
 	 * Build a term occurence graph (first graph).
 	 * 
 	 * @param storyIndex
 	 * @return
 	 */
 	private String getOccurenceGraph(int storyIndex) {
 		
 		String outputString = "";

 		// Retrieve matching matrix (that is word matching in story and trace for each trace-steps)
 		Matrix matchingMatrix = StatisticUtils.getMatchingMatrix(storyMatrix, traceMatrix, matchingWords, storyIndex);
 		
		// Generate coordinates string
		String coordinates = "";
		for (int i = 0; i < matchingMatrix.getRowSize(); i++) {
			for (int j = 0; j < matchingMatrix.getColumnSize(); j++) {
				
				String wordType = null;
				
				if (matchingMatrix.getValue(i, j) == 1) wordType = "s";
				if (matchingMatrix.getValue(i, j) == 2) wordType = "a";
				if (matchingMatrix.getValue(i, j) == 3) wordType = "c";
				
				if (wordType != null) coordinates += "(" + (i+1) + "," + (j+1) + ")[" + wordType + "]";
			}
		}
			
		// Generate latex fragment
		int xMax = matchingMatrix.getRowSize();
		outputString += "\\begin{axis}[xmin=0,xmax=" + xMax + ",scatter/classes={s={mark=square*,violet},a={mark=square*,red},c={mark=square*,blue}}]\n"; 
		outputString += "\\addplot+[scatter, only marks, scatter src=explicit symbolic,mark size=.3pt] coordinates {" + coordinates + "};\n"; 
		outputString += "\\end{axis}\n";
		outputString += "\\\\\n";
		
 		return outputString;
 	}
 	
 	/**
 	 * Build term scoring graph using idf scoring (second graph).
 	 * 
 	 * @param storyIndex
 	 * @return
 	 */
 	private String getTfidfGraph1(int storyIndex) {
		
		String outputString = "";
		
		// Retrieve scoring from story-trace matrix
		Vector scoreVector = scoreMatrix1.getRow(storyIndex);
	
		// Compute mean and trigger to display
		double triggerValue = scoreVector.getL1Norm() / scoreVector.size();
 		triggerValue += (100 - triggerValue) / 2;

 		// Generate coordinates string
		String coordinates = "";
		for (int i = 0; i < scoreVector.size(); i++) {
			coordinates += "(" + (i+1) + "," + Math.round(scoreVector.getValue(i) * 10000) / 10000.0 + ")";
		}
	
		// Generate latex fragment
		int xMax = scoreVector.size();
		outputString += "\\begin{axis}[xmin=0,xmax=" + xMax + "]\n";
		outputString += "\\draw [help lines] (0," + triggerValue + ") -- (" + xMax + "," + triggerValue + ");\n";
		outputString += "\\addplot+[blue,thick,mark=none] plot coordinates {" + coordinates + "};\n";
		outputString += "\\end{axis}\n";
		outputString += "\\\\\n";
		
		return outputString;
	}

	/**
	 * Build term scoring graph using idf scoring (second graph).
	 * 
	 * @param storyIndex
	 * @return
	 */
	private String getTfidf2Graph(int storyIndex) {
		
		String outputString = "";
		
		// Retrieve scoring from story-trace matrix
		Vector scoreVector = scoreMatrix2.getRow(storyIndex);
	
		// Compute mean and trigger to display
		double triggerValue = scoreVector.getL1Norm() / scoreVector.size();
		
		// Generate coordinates string
		String coordinates = "";
		for (int i = 0; i < scoreVector.size(); i++) {
			coordinates += "(" + (i+1) + "," + Math.round(scoreVector.getValue(i) * 10000) / 10000.0 + ")";
		}
	
		// Generate latex fragment
		int xMax = scoreVector.size();
		outputString += "\\begin{axis}[xmin=0,xmax=" + xMax + "]\n";
		outputString += "\\draw [help lines] (0," + triggerValue + ") -- (" + xMax + "," + triggerValue + ");\n";
		outputString += "\\addplot+[blue,thick,mark=none] plot coordinates {" + coordinates + "};\n";
		outputString += "\\end{axis}\n";
		outputString += "\\\\\n";
		
		return outputString;
	}

	/**
	 * Build the region mapping graph (manual and automatic matching).
	 * 
	 * @param storyIndex
	 * @return
	 */
 	private String getMappingGraph1(int storyIndex) {
 		
 		String outputString = "";
 				
 		String automaticRegionString = "";

		// Retrieve original region 
		MappingRegion originalMapping = originalRegions.get(storyIndex);
		int startIndex = originalMapping.getStartIndex();
		int endIndex   = originalMapping.getEndIndex();
		String manualRegionString = "\\fill [red!40!white] (" + (startIndex+1) + ",1) rectangle ("+ (endIndex+1) + ",99);\n";

		// Retrieve all regions automatically detected
 		List<MappingRegion> mappings = mappingRegions1.get(storyIndex);
 		
 		if (mappings != null) {
 	 		for(int i = 0; i < mappings.size(); i++) {
 	 			startIndex = mappings.get(i).getStartIndex();
 	 			endIndex   = mappings.get(i).getEndIndex();
 	 			automaticRegionString += "\\draw [blue,thick] (" + (startIndex+1) + ",1) rectangle (" + (endIndex+1) + ",99);\n";
 	 		}
 		}

		// Generate latex fragment
		int xMax = traceMatrix.getRowSize();
		outputString += "\\begin{axis}[height=55,ymin=0,ymax=100,xmin=0,xmax=" + xMax + "]\n";
		outputString += manualRegionString;
		outputString += automaticRegionString;
		outputString += "\\end{axis}\n";
		outputString += "\\\\\n";
		
 		return outputString;
 	}
 	
 	/**
	 * Build the region mapping graph (manual and automatic matching).
	 * 
	 * @param storyIndex
	 * @return
	 */
	private String getMappingGraph2(int storyIndex) {
		
		String outputString = "";
				
		String automaticRegionString = "";
	
		// Retrieve original region 
		MappingRegion originalMapping = originalRegions.get(storyIndex);
		int startIndex = originalMapping.getStartIndex();
		int endIndex   = originalMapping.getEndIndex();
		String manualRegionString = "\\fill [red!40!white] (" + (startIndex+1) + ",1) rectangle ("+ (endIndex+1) + ",99);\n";
	
		// Retrieve all regions automatically detected
		List<MappingRegion> mappings = mappingRegions2.get(storyIndex);

 		if (mappings != null) {
 			for(int i = 0; i < mappings.size(); i++) {
 				startIndex = mappings.get(i).getStartIndex();
 				endIndex   = mappings.get(i).getEndIndex();
 				automaticRegionString += "\\draw [blue,thick] (" + (startIndex+1) + ",1) rectangle (" + (endIndex+1) + ",99);\n";
 			}
 		}
	
		// Generate latex fragment
		int xMax = traceMatrix.getRowSize();
		outputString += "\\begin{axis}[height=55,ymin=0,ymax=100,xmin=0,xmax=" + xMax + "]\n";
		outputString += manualRegionString;
		outputString += automaticRegionString;
		outputString += "\\end{axis}\n";
		outputString += "\\\\\n";
		
		return outputString;
	}

	/**
 	 * Build term scoring graph using idf scoring (second graph).
 	 * 
 	 * @param storyIndex
 	 * @return
 	 */
 	private String getTripletGraph(int storyIndex) {
 		
		String outputString = "";
		String coordinates1 = "";
		String coordinates2 = "";
		String coordinates3 = "";

		if (storyIndex > 0 && storyIndex < scoreMatrix2.getRowSize() - 1) {
 			
			// Retrieve scoring from story-trace matrix
			Vector previousStoryStepScore = scoreMatrix2.getRow(storyIndex-1);
			Vector currentStoryStepScore  = scoreMatrix2.getRow(storyIndex);
			Vector nextStoryStepScore     = scoreMatrix2.getRow(storyIndex+1);

			// Generate coordinates string
			for (int i = 0; i < previousStoryStepScore.size(); i++) {
				coordinates1 += "(" + (i+1) + "," + Math.round(previousStoryStepScore.getValue(i) * 10000) / 10000.0 + ")";
			}

			// Generate coordinates string
			for (int i = 0; i < currentStoryStepScore.size(); i++) {
				coordinates2 += "(" + (i+1) + "," + Math.round(currentStoryStepScore.getValue(i) * 10000) / 10000.0 + ")";
			}

			// Generate coordinates string
			for (int i = 0; i < nextStoryStepScore.size(); i++) {
				coordinates3 += "(" + (i+1) + "," + Math.round(nextStoryStepScore.getValue(i) * 10000) / 10000.0 + ")";
			}
		}
		
		// Generate latex fragment
		int xMax = scoreMatrix2.getColumnSize();
		outputString += "\\begin{axis}[xmin=0,xmax=" + xMax + "]\n";
		outputString += "\\addplot+[mark=none, green, thin] plot coordinates {" + coordinates1 + "};\n";
		outputString += "\\addplot+[mark=none, blue, thick] plot coordinates {" + coordinates2 + "};\n";
		outputString += "\\addplot+[mark=none, red, thin]   plot coordinates {" + coordinates3 + "};\n";
		outputString += "\\end{axis}\n";
		outputString += "\\\\\n";
		
		return outputString;
	}
 	
	/**
 	 * Build term scoring graph using idf scoring (second graph).
 	 * 
 	 * @param storyIndex
 	 * @return
 	 */
 	private String getEnterExitGraph(int storyIndex) {
 		
		String outputString = "";
		String coordinates1 = "";
		String coordinates2 = "";

		if (storyIndex > 0 && storyIndex < scoreMatrix2.getRowSize() - 1) {
 			
			// Retrieve scoring from story-trace matrix
			Vector previousScore = scoreMatrix2.getRow(storyIndex-1);
			Vector currentScore  = scoreMatrix2.getRow(storyIndex);
			Vector nextScore     = scoreMatrix2.getRow(storyIndex+1);

			Vector enterScore = previousScore.substract(currentScore);
			Vector exitScore  = nextScore.substract(currentScore);
			
			// Generate coordinates string
			for (int i = 0; i < enterScore.size(); i++) {
				coordinates1 += "(" + (i+1) + "," + Math.round(enterScore.getValue(i) * 10000) / 10000.0 + ")";
			}

			// Generate coordinates string
			for (int i = 0; i < exitScore.size(); i++) {
				coordinates2 += "(" + (i+1) + "," + Math.round(exitScore.getValue(i) * 10000) / 10000.0 + ")";
			}
		}
		
		// Generate latex fragment
		int xMax = scoreMatrix2.getColumnSize();
		outputString += "\\begin{axis}[xmin=0,xmax=" + xMax + ",ytick=\\empty,xtick=\\empty]\n";
		outputString += "\\draw [help lines] (0,0) -- (" + xMax + ",0);\n";
		outputString += "\\addplot+[mark=none, blue, thin] plot coordinates {" + coordinates1 + "};\n";
		outputString += "\\addplot+[mark=none, red, thin]   plot coordinates {" + coordinates2 + "};\n";
		outputString += "\\end{axis}\n";
		outputString += "\\\\\n";
		
		return outputString;
	}
 	
	/**
 	 * Build term scoring graph using idf scoring (second graph).
 	 * 
 	 * @param storyIndex
 	 * @return
 	 */
 	private String getSyntheticGraph(int storyIndex) {
 		
		String outputString = "";
		String coordinates1 = "";

		if (storyIndex > 0 && storyIndex < scoreMatrix2.getRowSize() - 1) {
 			
			// Retrieve scoring from story-trace matrix
			Vector previousScore = scoreMatrix2.getRow(storyIndex-1);
			Vector currentScore  = scoreMatrix2.getRow(storyIndex);
			Vector nextScore     = scoreMatrix2.getRow(storyIndex+1);

//			Vector enterScore = previousScore.substract(currentScore);
//			Vector exitScore  = nextScore.substract(currentScore);
//			Vector finalScore  = enterScore.substract(exitScore);

			Vector score = previousScore.substract(nextScore);
			
			// Generate coordinates string
			for (int i = 0; i < score.size(); i++) {
				double scoreValue = Math.abs(score.getValue(i));
				coordinates1 += "(" + (i+1) + "," + Math.round(scoreValue * 10000) / 10000.0 + ")";
//				coordinates1 += "(" + (i+1) + "," + Math.round(finalScore.getValue(i) * 10000) / 10000.0 + ")";
			}
		}

		// Generate latex fragment
		int xMax = scoreMatrix2.getColumnSize();
		outputString += "\\begin{axis}[xmin=0,xmax=" + xMax + "]\n";
		outputString += "\\draw [help lines] (0,0) -- (" + xMax + ",0);\n";
		outputString += "\\addplot+[mark=none, blue, thin] plot coordinates {" + coordinates1 + "};\n";
		outputString += "\\end{axis}\n";
		outputString += "\\\\\n";
		
		return outputString;
	}
 	
	/**
	 * Build the region mapping graph (manual and automatic matching).
	 * 
	 * @param storyIndex
	 * @return
	 */
 	private String getMappingGraph3(int storyIndex) {
 		
 		String outputString = "";
 				
 		String automaticRegionString = "";

		// Retrieve original region 
		MappingRegion originalMapping = originalRegions.get(storyIndex);
		int startIndex = originalMapping.getStartIndex();
		int endIndex   = originalMapping.getEndIndex();
		String manualRegionString = "\\fill [red!40!white] (" + (startIndex+1) + ",1) rectangle ("+ (endIndex+1) + ",99);\n";

		// Retrieve all regions automatically detected
 		List<MappingRegion> mappings = mappingRegions3.get(storyIndex);
 		
 		if (mappings != null) {
 	 		for(int i = 0; i < mappings.size(); i++) {
 	 			startIndex = mappings.get(i).getStartIndex();
 	 			endIndex   = mappings.get(i).getEndIndex();
 	 			automaticRegionString += "\\draw [blue,thick] (" + (startIndex+1) + ",1) rectangle (" + (endIndex+1) + ",99);\n";
 	 		}
 		}

		// Generate latex fragment
		int xMax = traceMatrix.getRowSize();
		outputString += "\\begin{axis}[height=55,ymin=0,ymax=100,xmin=0,xmax=" + xMax + "]\n";
		outputString += manualRegionString;
		outputString += automaticRegionString;
		outputString += "\\end{axis}\n";
		outputString += "\\\\\n";
		
 		return outputString;
 	}

	/**
	 * Build the region mapping graph (manual and automatic matching).
	 * 
	 * @param storyIndex
	 * @return
	 */
 	private String getMappingGraph4(int storyIndex) {
 		
 		String outputString = "";
 				
 		String automaticRegionString = "";

		// Retrieve original region 
		MappingRegion originalMapping = originalRegions.get(storyIndex);
		int startIndex = originalMapping.getStartIndex();
		int endIndex   = originalMapping.getEndIndex();
		String manualRegionString = "\\fill [red!40!white] (" + (startIndex+1) + ",1) rectangle ("+ (endIndex+1) + ",99);\n";

		// Retrieve all regions automatically detected
 		List<MappingRegion> mappings = mappingRegions4.get(storyIndex);
 		
 		if (mappings != null) {
 	 		for(int i = 0; i < mappings.size(); i++) {
 	 			startIndex = mappings.get(i).getStartIndex();
 	 			endIndex   = mappings.get(i).getEndIndex();
 	 			automaticRegionString += "\\draw [blue,thick] (" + (startIndex+1) + ",1) rectangle (" + (endIndex+1) + ",99);\n";
 	 		}
 		}

		// Generate latex fragment
		int xMax = traceMatrix.getRowSize();
		outputString += "\\begin{axis}[height=66,ymin=0,ymax=100,xmin=0,xmax=" + xMax + "]\n";
		outputString += manualRegionString;
		outputString += automaticRegionString;
		outputString += "\\end{axis}\n";
		outputString += "\\\\\n";
		
 		return outputString;
 	}

	/**
 	 * Load the collection of story steps.
 	 * 
 	 * @param storyPath
 	 * @return
 	 * @throws IOException
 	 */
 	private List<String> loadStoryLines(Path storyPath) throws IOException {
 	
 		List<String> storyLines = new ArrayList<>();
		Map<String, String> storySteps = DictionaryUtils.loadStory(storyPath);
 		
 		for(String storyStep : storySteps.values()) {
 			storyLines.add(storyStep.toLowerCase());
 		}
 		
 		return storyLines;
 	}
 	
 	/**
 	 * Load the manual mapping file as a story line to trace line map.
 	 * 
 	 * @param mappingPath
 	 * @return
 	 * @throws IOException
 	 */
 	private Map<Integer, Integer> loadStoryToTraceMapping(Path mappingPath) throws IOException {
 		
 	 	Map<Integer, Integer> storyToTraceMapping = new TreeMap<>();
 	 	Map<String, String> stringMapping = DictionaryUtils.loadStoryToTraceMapping(mappingPath);
 	 	
 	 	for(String storyKey : stringMapping.keySet()) {

 	 		String traceKey = stringMapping.get(storyKey);
 	 		
 	 		int storyLine = Integer.valueOf(storyKey.substring(1));
 	 		int traceLine = Integer.valueOf(traceKey.substring(1));

 	 		storyToTraceMapping.put(storyLine,  traceLine);
 	 	}
 	 	
 	 	return storyToTraceMapping;
 	}
}
