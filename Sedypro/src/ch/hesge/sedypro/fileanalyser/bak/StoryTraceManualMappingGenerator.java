package ch.hesge.sedypro.fileanalyser.bak;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import ch.hesge.sedypro.utils.DictionaryUtils;
import ch.hesge.sedypro.utils.StringUtils;

public class StoryTraceManualMappingGenerator {

	private String LINE_SEPARATOR = StringUtils.repeat("-",  80);
	
	/**
	 * Default constructor
	 * 
	 */
	public StoryTraceManualMappingGenerator() {
	}
	
 	/**
 	 * Generate output file.
 	 * 
 	 * @param storyFilename
 	 * @param traceFilename
 	 * @throws IOException
 	 */
	public void doGenerate(String storyFilename, String traceFilename, String mappingFilename, String outputFilename) throws IOException {
		
		// Retrieve input/output paths
		Path storyPath = Paths.get(storyFilename).toAbsolutePath().normalize();
		Path tracePath = Paths.get(traceFilename).toAbsolutePath().normalize();
		Path mappingPath = Paths.get(mappingFilename).toAbsolutePath().normalize();
		Path outputPath = Paths.get(outputFilename).toAbsolutePath().normalize();

		// Load required dictionaries
		Map<String, String> storyLines = DictionaryUtils.loadStory(storyPath);
		Map<String, String> traceLines = DictionaryUtils.loadTrace(tracePath);
		Map<String, String> storyToTraceMapping = DictionaryUtils.loadTraceToStoryMapping(mappingPath);
		 
		try ( PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile())) ) {
	
			for(String traceLineKey : traceLines.keySet()) {
				
				// Add story line
				if (storyToTraceMapping.containsKey(traceLineKey)) {
					String storyLineKey = storyToTraceMapping.get(traceLineKey);
					String storyLine = storyLines.get(storyLineKey);
					writer.println(LINE_SEPARATOR);
					writer.println(storyLineKey.toUpperCase() + ": " + storyLine);
					writer.println(LINE_SEPARATOR);
				}

				// Add trace line
				String traceLine = traceLines.get(traceLineKey);
				writer.println(traceLineKey.toUpperCase() + ": " + traceLine);
			}
		}
	}
	
}
