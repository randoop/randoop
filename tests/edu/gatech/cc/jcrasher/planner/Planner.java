/*
 * Planner.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import edu.gatech.cc.jcrasher.plans.blocks.Block;

/**
 * Planner
 * 
 * Generalized planner creates blocks with chained plans and local variables:
 * - Generates testclasses blocks for any method/ constructor of a passed class.
 * - Hides class-wrapper- and plans- database, how instances are created, what
 *   combination of methods are called etc.
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-06-13 last changed
 */
public interface Planner {
	
	/**
	 * Get nr of plans the entire planspace of class pClass<?> holds for
	 * max chaining depth Constants.MAX_PLAN_RECURSION.
	 * That is the sum of the plan-spaces of the public constructors and methods
	 * declared by the class.
	 */
	public int getPlanSpaceSize(Class<?> pClass);
	
	/**
	 * Retrieve testclasses blocks according of specified class and its indices into plan space.
	 * @param indices is guaranteed to be free of duplicates for entire class-space
	 * @param arrayCursor starting index into indices - retrieve from here upto
	 * 	max tests/ class = next block
	 */
	public Block[] getTestBlocks(Class<?> pClass, int[] indices, int arrayCursor);
	
	/**
	 * Print the methods under testclasses and the rules found for creating testclasses
	 * cases to standard out, depending on the program's verbose level.
	 * 
	 * Default: No output
	 * Verbose: Print rules used to create testclasses cases.
	 * All: Verbose and rules found but not used.
	 */
	public void flush();
}
