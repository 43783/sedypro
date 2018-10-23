package org.hesge.sedypro;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import ch.hesge.sedypro.javainstrumenter.TraceLogger;

public class TraceLogger {

	// Private attributes
	private static Writer traceWriter;

	// Private constants
	private static int TRACE_ENTER = 0;
	private static int TRACE_EXIT  = 1;
	private static String DEFAULT_TRACE_FILE = "log/trace.log";
	public static String TRACECLASS_FULLNAME = "org.hesge.sedypro.TraceLogger";

	// Predefined formatter
	private static final String TRACE_ENTER_FORMAT = "%3$s %4$s     [%2$s] %5$s(%6$s) AS %7$s [%1$s] %8$s%n";
	private static final String TRACE_EXIT_FORMAT  = "END %3$s %4$s     [%2$s] %5$s(%6$s) AS %7$s [%1$s] %8$s%n";

	// Create a trace logger
	private static final Logger LOGGER = Logger.getLogger(TraceLogger.class.getName());

	/*
	 * Available info on format within the TRACE_FORMAT string: 
	 * 
	 * <code>
	 * 		1$ = timestamp
	 * 		2$ = threadId 
	 * 		3$ = package name 
	 * 		4$ = classname 
	 * 		5$ = methodname 
	 * 		6$ = parameters types 
	 * 		7$ = return type
	 * 		8$ = arguments values
	 * </code>
	 */

	/**
	 * Log a single method entry
	 */
	public static void entering(String packagename, String classname, String methodName, String parametersTypes, String returnType, String argumentsValues) {
		trace(TRACE_ENTER, packagename, classname, methodName, parametersTypes, returnType, argumentsValues);
	}

	/**
	 * Log a single method exit
	 */
	public static void exiting(String packagename, String classname, String methodName, String parametersTypes, String returnType, String argumentsValues) {
		trace(TRACE_EXIT, packagename, classname, methodName, parametersTypes, returnType, argumentsValues);
	}

	/**
	 * Write a trace into the trace file
	 */
	private static void trace(int traceType, String packagename, String classname, String methodName, String parameterTypes, String returnType, String argumentsValues) {

		try {
			long timestamp = System.currentTimeMillis();
			long threadId = Thread.currentThread().getId();

			// Autocreate trace write, if first invocation
			if (traceWriter == null) {

				// Retrieve output file name
				String propertyValue = System.getProperties().getProperty("ch.hesge.csim2.tracefile");

				if (propertyValue != null) {
					DEFAULT_TRACE_FILE = propertyValue;
				}

				// Create the trace file
				try {
					Path filepath = Paths.get(DEFAULT_TRACE_FILE);
					Files.createDirectories(filepath.getParent());
					Files.deleteIfExists(filepath);
					Files.createFile(filepath);
					traceWriter = new FileWriter(filepath.toFile());
				}
				catch (IOException e) {
					LOGGER.severe("unable to open trace file: " + e.toString() + " ! Exception: " + e.toString());
				}
			}
			
			// Clean classname and arguments content
			classname = classname.replace("class ",  "");
			argumentsValues = argumentsValues.replaceAll("\\r|\\n", " ");
			
			// Create the trace entry
			String traceMessage = String.format((traceType == TRACE_ENTER ? TRACE_ENTER_FORMAT : TRACE_EXIT_FORMAT), timestamp, threadId, packagename, classname, methodName, parameterTypes, returnType, argumentsValues);

			// Add it to the trace file
			traceWriter.append(traceMessage);
			traceWriter.flush();
		}
		catch (IOException e) {
			LOGGER.severe("error while creating a trace: " + e.toString() + " ! Exception: " + e.toString());
		}
	}
}
