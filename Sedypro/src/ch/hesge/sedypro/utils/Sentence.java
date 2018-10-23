/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.hesge.sedypro.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single trace entry generated by an code instrumentor.
 * 
 * Copyright HEG Geneva 2014, Switzerland
 * 
 * @author Eric Harth
 */

public class Sentence {

	// Private attributes
	private List<Word> words;
	private int indent; // only valid for trace sentences

	/**
	 * Default constructor
	 */
	public Sentence() {
		words = new ArrayList<>();
	}

	public int getIndent() {
		return indent;
	}

	public void setIndent(int indent) {
		this.indent = indent;
	}

	public List<Word> getWords() {
		return words;
	}
}