package ch.hesge.sedypro.fileanalyser.trace;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import ch.hesge.sedypro.utils.DictionaryEntry;
import ch.hesge.sedypro.utils.DictionaryUtils;
import ch.hesge.sedypro.utils.NLPUtils;
import ch.hesge.sedypro.utils.StringUtils;

public class TraceWordsGenerator {

	/**
	 * Default constructor
	 */
	public TraceWordsGenerator() {
	}
	
 	/**
 	 * Generate output file.
 	 * 
 	 * @throws IOException 
 	 */
 	public void doGenerate(String traceDictionaryFilename, String traceWordsFilename) throws IOException {
 		
		// Retrieve input/output paths
 		Path traceDictionaryPath = Paths.get(traceDictionaryFilename).toAbsolutePath().normalize();
 		Path traceWordsDictionaryPath = Paths.get(traceWordsFilename).toAbsolutePath().normalize();
		
		// Load required dictionaries
 		Map<String, DictionaryEntry> traceDictionary = DictionaryUtils.loadDictionary(traceDictionaryPath);
 		Map<Integer, Set<String>> traceLines = DictionaryUtils.getLineToWordMap(traceDictionary);
 		
		// If file already exists, suppress it
		if (traceWordsDictionaryPath.toFile().exists()) {
			Files.delete(traceWordsDictionaryPath);
		}

		// Save, for each trace line, all words present in dictionary
		try ( PrintWriter writer = new PrintWriter(new FileWriter(traceWordsDictionaryPath.toFile())) ) {
			
			for(int traceLine : traceLines.keySet()) {
				
				Set<String> traceWords = traceLines.get(traceLine);
				String outputString = StringUtils.toString(traceWords, ",");
				outputString = NLPUtils.getNaturalWordsOrder(outputString);

				writer.println(outputString);
			}
		} 	
	}
}
