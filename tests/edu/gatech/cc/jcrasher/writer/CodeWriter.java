/*
 * CodeWriter.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.writer;

import edu.gatech.cc.jcrasher.plans.blocks.Block;

/**
 * CodeWriter
 * 
 * ADT hiding the creation of java source files
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-06-11 last changed
 */
public interface CodeWriter {
	
	/**
	 * Writes a series pBlocks for pClass<?> of testcases to a new java file.
	 */
	public void generateTestFile(
			Class<?> c,
			int classSeqNr,
			Block[] blocks
	);


	/**
	 * Writes a series pBlocks for pClass<?> of testcases to a new java file
	 * together with a comment.
	 */
	public void generateTestFile(
			Class<?> c,
			int classSeqNr,
			Block[] blocks,
			String comment
	);
	
	/**
	 * Generate a calling testclasses suite
	 */
	public void generateSuite(Class<?> pClass, int nrTestClasses);
	
}
