/**
 * 
 */
package ch.hesge.sedypro.javainstrumenter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import ch.hesge.sedypro.utils.ConsoleUtils;
import ch.hesge.sedypro.utils.FileUtils;
import ch.hesge.sedypro.utils.StringUtils;


/**
 * This engine allow java sources instrumentation.
 * 
 * Copyright HEG Geneva 2014, Switzerland
 * 
 * @author Eric Harth
 *
 */
public class JavaInstrumenter {

	// Private attributes
	private Path sourcePath;
	private Path targetPath;
	private int instrumentedClassCount;
	private Map<String, String> visitedFiles;
	private String VERSION = "1.0.8";
	
	/**
	 * Default constructor
	 */
	public JavaInstrumenter() {
		visitedFiles = new HashMap<>();
	}

	/**
	 * Startup method
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			JavaInstrumenter instrumenter = new JavaInstrumenter();
			instrumenter.start(args);
		}
		catch (Exception e) {
			ConsoleUtils.println("exception raised: " + StringUtils.toThrowableString(e));
		}
	}
	
	/**
	 * Initialize the engine before starting.
	 */
	public void start(String[] args) {

		printBanner();
		
		if (args == null || args.length < 1) {
			printUsage();
		}
		else {

			// Check if specified folders are valid
			checkSourceFolder(args);
			checkTargetFolder(args);
			
			ConsoleUtils.println("source: " + sourcePath);
			ConsoleUtils.println("target: " + targetPath + "\n");		

			// Start instrumenting
			doCloneSourceFolder();
			doInstrumentFolder();
			doGenerateTraceLoggerClass();
		}
	}

	/**
	 * Verify that source folder specified in argument is valid
	 * and is present on filesystem.
	 * 
	 * @param args
	 */
	public void checkSourceFolder(String[] args) {
		
		String folder = args[0];
		
		if (folder.endsWith("/")) {
			folder = folder.substring(0,  folder.length() - 1);
		}
		
		// Retrieve normalized absolute path
		sourcePath = Paths.get(folder).toAbsolutePath().normalize();

		// Check if folder really exists
		if (!sourcePath.toFile().exists()) {
			throw new RuntimeException("source-folder doesn't exist !");
		}
	}
	
	/**
	 * Verify that target folder specified in argument is valid
	 * and is not already present on filesystem.
	 * 
	 * @param args
	 */
	public void checkTargetFolder(String[] args) {
		
		String folder = sourcePath.toString() + ".instrumented";

		if (args.length > 1) {
			folder = args[1];
		}

		// Retrieve normalized absolute path
		targetPath = Paths.get(folder).toAbsolutePath().normalize();

		// Verify that folder does not exist
		if (targetPath.toFile().exists()) {
			throw new RuntimeException("target-folder already exist !");
		}
	}

	/**
	 * Duplicate all source folder content into the target path
	 */
	private void doCloneSourceFolder() {
		
		ConsoleUtils.println("cloning folder " + sourcePath.toAbsolutePath().normalize().toString());

		try {
			FileUtils.removeFolder(targetPath);
			FileUtils.copyFolder(sourcePath, targetPath);
		} 
		catch (Exception e) {
			ConsoleUtils.println("error while cloning source folder: " + StringUtils.toThrowableString(e));
		}
	}

	/**
	 * Generate TraceLogger class invocated by instrumented methods
	 */
	private void doGenerateTraceLoggerClass() {
		
		try {
			// Scan all folder recursively to discover src folder
			Files.walkFileTree(Paths.get(targetPath.toString()), new SimpleFileVisitor<Path>() {
	
				@Override
				public FileVisitResult preVisitDirectory(Path folderPath, BasicFileAttributes attrs) throws IOException {
	
					if (folderPath.getFileName().toString().equals("src")) {
						
						// Create "TraceLogger.java" file to the target "src" folder
						Files.createDirectories(Paths.get(folderPath + "/org/hesge/sedypro"));
						Path source = Paths.get("data/instrumenter/TraceLogger.java");
						Path target = Paths.get(folderPath + "/org/hesge/sedypro/TraceLogger.java");
						Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
					}
	
					return FileVisitResult.CONTINUE;
				}
			});	
	
			ConsoleUtils.println("tracelogger class successfully generated");
		}
		catch (Exception e) {
			ConsoleUtils.println("error while generating TraceLogger file: " + StringUtils.toThrowableString(e));
		}
	}
	
	/**
	 * Start instrumenting current folder.
	 * 
	 * @see ch.hesge.csim2.core.shell.IEngine#doInstrumentFolder()
	 */
	public void doInstrumentFolder() {
		try {

			instrumentedClassCount = 0;
			
			// Initialization
			visitedFiles.clear();
			
			ConsoleUtils.println("instrumentation started.");

			// Then, start scanning source code
			ConsoleUtils.println("source scanning started.");

			// Scan all folder recursively to discover source file
			Files.walkFileTree(Paths.get(targetPath.toString()), new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path filepath, BasicFileAttributes attrs) throws IOException {

					// Retrieves file extension
					String fileExtension = FileUtils.getFileExtension(filepath.toString());

					// Parse source file only and analyze it
					if (fileExtension.equalsIgnoreCase(".java") && canVisitFile(filepath.toString())) {

						try {

							// Skip the logger trace itself (if present)
							if (!filepath.getFileName().toString().equals("TraceLogger.java") ) {

								// Extract instrumented fragment from files
								doInstrumentFile(filepath.toString());
							}

							// Mark current file as visited
							visitedFiles.put(filepath.toString(), filepath.toString());
						}
						catch (Exception e) {
							ConsoleUtils.println("error while instrumenting files: " + StringUtils.toThrowableString(e));
						}
					}

					return FileVisitResult.CONTINUE;
				}
			});
			
			ConsoleUtils.println("instrumentation completed (" + instrumentedClassCount + " classes instrumented)");
		}
		catch (Exception e) {
			ConsoleUtils.println("error while instrumenting files: " + StringUtils.toThrowableString(e));
		}
	}

	/**
	 * Check if a file should be visited.
	 * 
	 * @param filepath
	 *        the filepath to check
	 * @return true if the file is not yet parsed, false otherwise
	 */
	private boolean canVisitFile(String filepath) {

		// Reject file outside root folder
		if (!filepath.startsWith(targetPath.toString())) {
			return false;
		}

		// Reject files already parsed
		if (visitedFiles.containsKey(filepath)) {
			return false;
		}

		return true;
	}

	/**
	 * Instrument a single source file by surrounding body method with
	 * try/finally statement.
	 * 
	 * @param filepath
	 *        the file to parse
	 * @throws Exception
	 */
	private void doInstrumentFile(String filepath) throws Exception {
		
		instrumentedClassCount++;
		final String filename = Paths.get(filepath).getFileName().toString();

		// Retrieve source content
		String originalContent = FileUtils.readFileAsString(Paths.get(filepath));
		Document originalSources = new Document(originalContent);

		ConsoleUtils.println("parsing file " + filename + ".");

		// Create a parser
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(originalSources.get().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		parser.setCompilerOptions(options);
		parser.setResolveBindings(true);

		// Parse the source file
		final CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);

		// Display parsing problems
		for (IProblem parsingProblem : compilationUnit.getProblems()) {
			System.out.println(parsingProblem.getMessage());
		}

		// Start recording all AST modifications
		compilationUnit.recordModifications();

		// Instrument all declared methods (standard and anonymous classes)
		compilationUnit.accept(new ASTVisitor() {

			// Instrument all methods
			public boolean visit(MethodDeclaration methodDeclaration) {

				// Skip method interface
				if (!JavaInstrumenterUtils.isInterfaceMethod(methodDeclaration)) {
					doInstrumentMethod(compilationUnit, methodDeclaration);
				}
				return true;
			}
		});

		// Retrieve all source modifications made within the compilation unit
		TextEdit sourceModifications = compilationUnit.rewrite(originalSources, null);

		// Create a new copy of the original sources
		Document modifiedSources = new Document(originalSources.get());

		// And apply all modifications to separate copy of the original sources code
		sourceModifications.apply(modifiedSources);
		String modifiedSource = modifiedSources.get();

		// Save modified (instrumented) version of the original source file
		Files.delete(Paths.get(filepath));
		FileUtils.writeFile(Paths.get(filepath), modifiedSource);
	}

	/**
	 * Parse a function definition and
	 * create its associated instrumentation fragment directly within the
	 * compilation unit.
	 * 
	 * @param compilationUnit
	 * @param methodDeclaration
	 */
	@SuppressWarnings("unchecked")
	private void doInstrumentMethod(CompilationUnit compilationUnit, MethodDeclaration methodDeclaration) {

		AST ast = compilationUnit.getAST();
		Block originalBody = methodDeclaration.getBody();
		String methodName = methodDeclaration.getName().toString();
		
		// Skip abstract method without body
		if (!methodName.equals("hashCode") && originalBody != null) {

			ASTNode constructorInvocation = null;

			// Retrieve super invocation for future use
			if (originalBody.statements().size() > 0) {

				// Retrieve first statement type
				int nodeType = ((ASTNode) originalBody.statements().get(0)).getNodeType();

				// Detect constructor invocation
				if (nodeType == ASTNode.SUPER_CONSTRUCTOR_INVOCATION || nodeType == ASTNode.CONSTRUCTOR_INVOCATION) {
					constructorInvocation = (ASTNode) originalBody.statements().remove(0);
				}
			}

			// Create a try/finally block
			TryStatement tryStatement = ast.newTryStatement();
			tryStatement.setBody(ast.newBlock());
			tryStatement.setFinally(ast.newBlock());

			// Add a Trace enter invocation within the try-statement
			MethodInvocation traceEnterCode = JavaInstrumenterUtils.createTraceInvocation(compilationUnit, "entering", methodDeclaration);
			ExpressionStatement expressionStatement = ast.newExpressionStatement(traceEnterCode);
			tryStatement.getBody().statements().add(expressionStatement);

			// Move all original statements within the try-statement (but after the trace invocation)
			while (originalBody.statements().size() > 0) {
				ASTNode statement = (ASTNode) originalBody.statements().remove(0);
				tryStatement.getBody().statements().add(statement);
			}

			// Add a Trace exit invocation within the try-statement
			MethodInvocation traceExitCode = JavaInstrumenterUtils.createTraceInvocation(compilationUnit, "exiting", methodDeclaration);
			tryStatement.getFinally().statements().add(ast.newExpressionStatement(traceExitCode));

			// Restore the constructor invocation as the first body statement
			if (constructorInvocation != null) {
				originalBody.statements().add(constructorInvocation);
			}

			// Apply the new try-statement as the second statement
			originalBody.statements().add(tryStatement);
			
			ConsoleUtils.println("  method: " + methodDeclaration.getName().toString() + " instrumented.");
		}
	}
	
	/**
	 * Print copyright and version
	 */
	private void printBanner() {
		ConsoleUtils.println("JavaInstrumenter " + VERSION);
		ConsoleUtils.println("Copyright (c) University of Geneva, Switzerland, 2017\n");
	}

	/**
	 * Print how to launch the instrumenter
	 */
	private void printUsage() {
		ConsoleUtils.println("usage: JavaInstrumenter source-folder [target-folder]\n");
		ConsoleUtils.println("description:");
		ConsoleUtils.println("   JavaInstrumenter first clone source-folder into a separate folder.");
		ConsoleUtils.println("   The cloned folder is then scanned and for each function detected,");
		ConsoleUtils.println("   entry/exit fragments are inserted to write traces while running");
		ConsoleUtils.println("   instrumented code version..");
	}
}
