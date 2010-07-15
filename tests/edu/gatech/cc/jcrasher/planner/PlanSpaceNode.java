/*
 * PlanSpaceNode.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import edu.gatech.cc.jcrasher.plans.Plan;

/**
 * PlanSpaceNode
 * 
 * Node to access the plans of a (sub-) plan space, e.g.:
 * - all plans of how to invoke a method, chaining up to a given depth
 * - all plans of how to obtain an instance of a type, using chaining up to a given depth
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-08-01 last changed
 */
public interface PlanSpaceNode {

	/**
	 * Precond: true
	 * Postcond: cached sizes of all sub plan spaces to speed up getPlan(int)
	 * 
	 * @return size of this sub plan space = nr different plans this 
	 * 	plan space can return via getPlan(int)
	 */
	public int getPlanSpaceSize();

	/**
	 * Precond: 0 <= planIndex < getPlanSpaceSize()
	 * Postcond: no side-effects
	 * 
	 * @param planIndex the index of the plan according to the node's 
	 * 	canonical order, taken from [0..getPlanSpaceSize()-1]
	 * @return plan according to the ordering semantics, never null
	 */	
	public Plan getPlan(int planIndex);	
}
