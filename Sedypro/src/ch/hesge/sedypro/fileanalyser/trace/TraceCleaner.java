package ch.hesge.sedypro.fileanalyser.trace;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import ch.hesge.sedypro.utils.ConsoleUtils;
import ch.hesge.sedypro.utils.DictionaryUtils;
import ch.hesge.sedypro.utils.StringUtils;
import ch.hesge.sedypro.utils.Trace;

public class TraceCleaner {

	private Map<String,String> substitutionStringsMap;
	
	/**
	 * Default constructor
	 * 
	 */
	public TraceCleaner() {

	}
	
 	/**
 	 * Generate output file.
 	 * 
 	 * @param traceFilename
 	 * @param cleanedFilename
 	 * @throws IOException
 	 */
	public void doGenerate(String traceFilename, String cleanedFilename, String substitutionStringsFilename) throws IOException {
		
		// Retrieve input/output paths
		Path tracePath  = Paths.get(traceFilename).toAbsolutePath().normalize();
		Path cleanedPath = Paths.get(cleanedFilename).toAbsolutePath().normalize();
		Path substitutionStringsPath = Paths.get(substitutionStringsFilename).toAbsolutePath().normalize();

		// Load required dictionaries
		substitutionStringsMap = DictionaryUtils.loadTraceReplacementDictionary(substitutionStringsPath);

		try {
			
			int lineCounter  = 0;
			int errorCounter = 0;
			int traceLevel   = 0;
			
			// If file already exists, suppress it
			if (cleanedPath.toFile().exists()) {
				Files.delete(cleanedPath);
			}

			Map<Long, Stack<Trace>> threadTraces = new Hashtable<>();
			
			try ( PrintWriter writer = new PrintWriter(new FileWriter(cleanedPath.toFile())) ) {
					try (Stream<String> textLines = Files.lines(tracePath)) {

					Iterator<String> lineIterator =  textLines.iterator();
					
					while (lineIterator.hasNext()) {
												
						// Retrieve current trace line
						String traceLine = lineIterator.next();			
						Trace trace = parseTraceLine(traceLine);
						
						if (trace != null) {
	
							// Check for thread stack trace
							if (!threadTraces.containsKey(trace.getThreadId())) {
								threadTraces.put(trace.getThreadId(), new Stack<Trace>());
							}
	
							// Retrieve current thread stack
							Stack<Trace> threadStack = threadTraces.get(trace.getThreadId());
	
							// Trace entering, so push trace into its stack
							if (trace.isEnteringTrace()) {
								
								lineCounter++;
								
								trace.setLevel(traceLevel++);
								threadStack.push(trace);
	
								// Retrieve cleaned trace string to output
								String cleanedTrace = getCleanTraceString(trace);
								String outputString = String.format("T%1$04d %2$s", lineCounter, cleanedTrace);
								writer.println(outputString);
							}
	
							// Trace exiting, so compute duration trace
							else {
								Trace popedTrace = threadStack.pop();
								traceLevel = popedTrace.getLevel();
	
								long traceDuration = trace.getTimestamp() - popedTrace.getTimestamp();
								popedTrace.setDuration(traceDuration);
								
								trace = popedTrace;
							}
						}
	
						// Otherwise show error line
						else {
							ConsoleUtils.println(" error in line: " + lineCounter + ", content: " + traceLine);
						}
					}
	
					if (errorCounter > 0) {
						ConsoleUtils.println(errorCounter + " detected in traces");
					}
				}
			}
		}
		catch (Exception e) {
			ConsoleUtils.println("error while analysing trace files: " + StringUtils.toThrowableString(e));
		}
	}
	
	/**
	 * Parse a single trace line.
	 * 
	 * @param traceLine
	 * @return
	 */
	private Trace parseTraceLine(String traceLine) {

		Trace newTrace = null;
		
		// Now, extract all trace parts
		String regexp = "(?<endtag>END\\s)?" + "(?<package>\\S+)?" + "\\s" + "(?<classname>\\S+)?" + "(\\s){5}" + "\\[(?<thread>\\d+)\\]" + "\\s" + "(?<signature>.*\\(.*\\))" + "\\s" + "AS" + "\\s" + "(?<returntype>.+)" + "\\s" + "\\[(?<timestamp>\\d+)\\]" + "\\s" + "(?<arguments>.*)";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(traceLine);
		
		if (matcher.matches()) {

			boolean isEnteringTrace = matcher.group("endtag") == null;

			// Extract information from traceline
			String strPackage    = matcher.group("package");
			String strClassname  = matcher.group("classname");
			String strThread     = matcher.group("thread");
			String strSignature  = matcher.group("signature");
			String strReturnType = matcher.group("returntype");
			String strTimestamp  = matcher.group("timestamp");
			String strArguments  = matcher.group("arguments");

			// Convert values
			String instanceId = "0";
			long threadId     = Long.valueOf(strThread);
			long timestamp    = Long.valueOf(strTimestamp);

			newTrace = new Trace();

			newTrace.setEnteringTrace(isEnteringTrace);
			newTrace.setInstanceId(instanceId);
			newTrace.setPackageName(strPackage);
			newTrace.setClassName(strClassname);
			newTrace.setThreadId(threadId);
			newTrace.setSignature(strSignature);
			newTrace.setReturnType(strReturnType);
			newTrace.setTimestamp(timestamp);
			newTrace.setArguments(strArguments);
		}
		
		return newTrace;
	}
	
	/**
	 * Retrieve a simplified (cleaned) representation of current trace.
	 * 
	 * @param trace
	 * @return
	 */
	private String getCleanTraceString(Trace trace) {
		
		// Keep indentation
		String traceIndent = StringUtils.repeat(" ",  trace.getLevel()*4);
		
		// Retrieve relevant trace information
		String classname = trace.getClassName();
		String signature = trace.getSignature();
		String arguments = trace.getArguments();
		
		// Extract method name and parameters list
		String method = signature.substring(0, signature.indexOf("("));
		String params = signature.substring(signature.indexOf("(")).replaceAll("\\(|\\)", "");

		// Now, extract only parameter's name
		String parameters = "";
		String regexp = "(?<subject>\\S+)\\s+(?<name>[^,\\s]+),?";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(params);
		while (matcher.find()) parameters += matcher.group("name") + " ";
		
		// Merge all parts into a single line
		String cleanTraceLine = classname + " " + method + " " + parameters + " " + arguments;

		// Remove all string defined in conf/trace-cleaner-list.txt (2-passes)
		for (int i = 0; i < 2; i++) {
			for (String stringToReplace : substitutionStringsMap.keySet()) {
				String substitutionString = substitutionStringsMap.get(stringToReplace);
				cleanTraceLine = cleanTraceLine.replaceAll(stringToReplace, substitutionString);
			}
		}

		// Keep only alphanum characters
		cleanTraceLine  = cleanTraceLine.replaceAll("[^a-zA-Z0-9.#]", " ");

		// Remove #Object
		cleanTraceLine  = cleanTraceLine.replaceAll("#Object", " ");

		// Remove multiple commas
		cleanTraceLine  = cleanTraceLine.replaceAll("\\.{2}", " ");

		// Remove multiple spaces
		cleanTraceLine  = cleanTraceLine.replaceAll(" +", " ");

		return traceIndent + cleanTraceLine;
	}
}
