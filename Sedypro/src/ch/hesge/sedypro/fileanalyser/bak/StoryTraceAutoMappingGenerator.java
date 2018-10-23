package ch.hesge.sedypro.fileanalyser.bak;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import ch.hesge.sedypro.utils.DictionaryUtils;
import ch.hesge.sedypro.utils.Matrix;
import ch.hesge.sedypro.utils.StringUtils;
import ch.hesge.sedypro.utils.Vector;

public class StoryTraceAutoMappingGenerator {

	private String LINE_SEPARATOR = StringUtils.repeat("-",  80);
	
	/**
	 * Default constructor
	 */
	public StoryTraceAutoMappingGenerator() {
	}

 	/**
	 * Generate the story/trace latex file.
 	 * 
 	 * @throws IOException 
 	 */
 	public void doGenerate(String storyFilename, String traceFilename, String matrixFilename, String outputFilename) throws IOException {
 		
		// Retrieve input/output paths
		Path storyPath   = Paths.get(storyFilename).toAbsolutePath().normalize();
		Path tracePath   = Paths.get(traceFilename).toAbsolutePath().normalize();
 		Path matrixPath  = Paths.get(matrixFilename).toAbsolutePath().normalize();
 		Path outputPath  = Paths.get(outputFilename).toAbsolutePath().normalize();

		// Load dictionaries, matrices and words used in matrix
		Map<String, String> storyLines = DictionaryUtils.loadStory(storyPath);
		Map<String, String> traceLines = DictionaryUtils.loadTrace(tracePath);
		Matrix storyTraceMatrix = DictionaryUtils.loadMatrix(matrixPath);
					
 		// If file already exists, suppress it
		if (outputPath.toFile().exists()) {
			Files.delete(outputPath);
		}

		int traceIndex = 0;
		storyTraceMatrix = storyTraceMatrix.normalize(100);
		Map<String, String> storyToTraceMapping = new TreeMap<>();
		storyToTraceMapping.put("t0001",  "s0001");
		
		// For each story line, retrieve its associated trace line
		for(int i = 0; i < storyTraceMatrix.getRowSize(); i++) {
			
	 		int storyTraceIndex = traceIndex;
			Vector traceVector = storyTraceMatrix.getRow(i);
			String storyKey = String.format("s%1$04d", i + 1);

	 		for (int k = traceIndex; k < traceVector.size(); k++) {

	 			String traceKey = String.format("t%1$04d", k + 1);
	 			
	 			if (traceVector.getValue(k) > 95) {
	 				storyTraceIndex = k;
	 				storyToTraceMapping.put(traceKey, storyKey);
	 				break;
	 			}
	 		}
	 		
			// Next story will lookup from current trace step
			traceIndex = Math.min(Math.max(traceIndex, storyTraceIndex + 1), traceVector.size());
		}
		
		// Build the story-to-trace mapping file
		try ( PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile())) ) {
			
			for(String traceKey : traceLines.keySet()) {
				
				// Add story line
				if (storyToTraceMapping.containsKey(traceKey)) {
					String storyKey = storyToTraceMapping.get(traceKey);
					String storyLine = storyLines.get(storyKey);
					writer.println(LINE_SEPARATOR);
					writer.println(storyKey.toUpperCase() + ": " + storyLine);
					writer.println(LINE_SEPARATOR);
				}

				// Add trace line
				String traceLine = traceLines.get(traceKey);
				writer.println(traceKey.toUpperCase() + ": " + traceLine);
			}
		}
	}
}
