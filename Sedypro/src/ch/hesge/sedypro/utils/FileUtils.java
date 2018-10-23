package ch.hesge.sedypro.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a generic utility class for files.
 * 
 * Copyright HEG Geneva 2014, Switzerland
 * 
 * @author Eric Harth
 */

public class FileUtils {

	/**
	 * Check if a filename exists or not.
	 * 
	 * @param filename
	 *            the name of the file to check
	 * @return true if the file exists, false otherwise
	 */
	public static boolean exists(String filename) {
		return new File(filename).exists();
	}

	/**
	 * Check if a filename doesn't exist.
	 * 
	 * @param filename
	 *            the name of the file to check
	 * @return true if the file doesn't exists, true otherwise
	 */
	public static boolean dontExists(String filename) {
		return !new File(filename).exists();
	}

	/**
	 * Return the extension part of a filename. If no extension found, just
	 * return an empty string.
	 * 
	 * @param filename
	 *            the filename where extensions should be extracted
	 * @return the file extension (with ".") or empty string
	 */
	public static String getFileExtension(String filename) {

		String fileExtension = "";
		
		int extensionIndex = filename.toString().lastIndexOf(".");

		if (extensionIndex > -1) {
			fileExtension = filename.substring(extensionIndex);
		}

		return fileExtension;
	}

	/**
	 * Return a filename without its extension.
	 * 
	 * @param filename
	 * @return
	 */
	public static String getNameWithoutExtension(String filename) {

		String newFilename = filename;
		
		int extensionIndex = filename.toString().lastIndexOf(".");

		if (extensionIndex > -1) {
			newFilename = filename.substring(0, extensionIndex);
		}

		return newFilename;
	}

	/**
	 * Read the contents of the file with the given name, and return it as a
	 * String.
	 * 
	 * @param filename
	 *            The path to the file to load
	 * @return The contents of the file
	 * @throws IOException
	 *             If the file could not be read
	 */
	public static String readFileAsString(Path filepath) throws IOException {
		byte[] bytes = Files.readAllBytes(filepath);
		return new String(bytes);
	}

	/**
	 * Write a string content to the file with the given name.
	 * 
	 * @param filepath
	 *            the path to the file to load
	 * @param content
	 *            the new file content
	 * @throws IOException
	 *             if the file could not be read
	 */
	public static void writeFile(Path filepath, String content) throws IOException {
		Files.write(filepath, content.getBytes(), StandardOpenOption.CREATE);
	}

	/**
	 * Read the contents of the file with the given name, and return it as a
	 * list of String.
	 * 
	 * @param filename
	 *            The path to the file to load
	 * @return The contents of the file as a list of string
	 * @throws IOException
	 *             If the file could not be read
	 */
	public static List<String> readFileAsStringList(Path filepath) throws IOException {
		return Files.readAllLines(filepath, Charset.defaultCharset());
	}

	public static void writeFile(Path filepath, List<String> lines) throws IOException {
		Files.write(filepath, lines, StandardOpenOption.CREATE);
	}

	/**
	 * Recursively delete all files contained within a folder
	 * 
	 * @param folder
	 *            the folder to delete.
	 * 
	 * @throws IOException
	 *             If the file is not writable
	 */
	public static void removeFolder(Path folderpath) throws IOException {

		if (Files.exists(folderpath)) {
			Files.walkFileTree(folderpath, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {

					if (e == null) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
					else {
						throw e;
					}
				}
			});
		}
	}

	/**
	 * Recursively clone a folder with all its content.
	 * 
	 * @param sourceFolder
	 *            the source folder to copy
	 * @param targetFolder
	 *            the new folder copy name
	 * @throws IOException
	 */
	public static void copyFolder(final Path sourcePath, final Path targetPath) throws IOException {

		Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path folderPath, BasicFileAttributes attrs) throws IOException {

				// Skip locked subversion file
				// SVN BUG: see
				// http://tortoisesvn.tigris.org/ds/viewMessage.do?dsForumId=4061&dsMessageId=2920379
				if (folderPath.getFileName().toString().equals(".svn")) {
					return FileVisitResult.SKIP_SUBTREE;
				}

				// Recompute target folder path
				Path targetDirectory = Paths.get(targetPath.toString() + folderPath.toString().replace(sourcePath.toString(), ""));

				// Create same folder than the original, but in destination folder
				Files.createDirectories(targetDirectory);

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path filepath, BasicFileAttributes attrs) throws IOException {

				// Recompute target file path
				Path targetFile = Paths.get(targetPath.toString() + filepath.toString().replace(sourcePath.toString(), ""));

				// Copy original file to destination
				Files.copy(filepath, targetFile, StandardCopyOption.REPLACE_EXISTING);

				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Scan files in folders for specific pattern.
	 * 
	 * @param root
	 *            the root path
	 * @param the
	 *            regex used for pattern matching
	 * @return a list of all files found matching regex
	 */
	public static List<Path> searchFiles(Path root, String regex) {
		List<Path> matchingFiles = new ArrayList<>();

		// Just for safety
		if (root != null && regex != null) {
			searchFiles0(root, regex, matchingFiles);
		}

		return matchingFiles;
	}

	/**
	 * Scan files in folders for specific pattern.
	 * 
	 * @param root
	 *            the root path
	 * @param the
	 *            regex used for pattern matching
	 * @return a list of all files found matching regex
	 */
	public static List<Path> searchAllFiles(Path root) {
		List<Path> matchingFiles = new ArrayList<>();

		// Just for safety
		if (root != null) {
			searchFiles0(root, ".*", matchingFiles);
		}

		return matchingFiles;
	}

	/**
	 * Internal method used to recursivally find result
	 */
	private static void searchFiles0(Path path, String regex, List<Path> matchingFiles) {
		File file = path.toFile();

		if (file.isDirectory()) {
			for (File child : file.listFiles())
				searchFiles0(child.toPath(), regex, matchingFiles);
		}
		else if (file.isFile() && file.getName().matches(regex)) {
			matchingFiles.add(file.toPath());
		}
	}
}
