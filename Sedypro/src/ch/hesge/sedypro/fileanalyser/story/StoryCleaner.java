package ch.hesge.sedypro.fileanalyser.story;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import ch.hesge.sedypro.utils.DictionaryUtils;

public class StoryCleaner {

	/**
	 * Default constructor
	 * 
	 */
	public StoryCleaner() {
	}
	
 	/**
 	 * Generate output file.
 	 * 
 	 * @param storyFilename
 	 * @param cleanedFilename
 	 * @throws IOException
 	 */
	public void doGenerate(String storyFilename, String cleanedFilename) throws IOException {
		
		// Retrieve input/output paths
		Path storyPath   = Paths.get(storyFilename).toAbsolutePath().normalize();
		Path cleanedPath = Paths.get(cleanedFilename).toAbsolutePath().normalize();

		// Load required dictionaries
		Map<String, String> storyLines = DictionaryUtils.loadStory(storyPath);
 		
		// If file already exists, suppress it
		if (cleanedPath.toFile().exists()) {
			Files.delete(cleanedPath);
		}
		
		try ( PrintWriter writer = new PrintWriter(new FileWriter(cleanedPath.toFile())) ) {
			
			for(String storyLineKey : storyLines.keySet()) {
				
				String storyLine =  storyLines.get(storyLineKey);
				
				// Keep only alphanum characters
				storyLine  = storyLine.replaceAll("[^a-zA-Z0-9.#]", " ");

				// Remove multiple commas
				storyLine  = storyLine.replaceAll("\\.{2}", " ");

				// Remove multiple spaces
				storyLine  = storyLine.replaceAll(" +", " ");
				
				writer.println(storyLineKey.toUpperCase() + " " + storyLine);
			}
		}
	}
	
}
