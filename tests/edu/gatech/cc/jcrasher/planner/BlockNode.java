/*
 * TypeNode.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import edu.gatech.cc.jcrasher.plans.blocks.Block;

/**
 * TypeNode
 * 
 * Node to access the blocks created from plans.
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-08-06 last changed
 */
public interface BlockNode {

	/**
	 * Retrieve block with given index from the underlying class's plan space.
	 * 
	 * Precond: 0 <= planIndex < getPlanSpaceSize()
	 * Postcond: no side-effects
	 */
	public Block getBlock(int planIndex);
}
