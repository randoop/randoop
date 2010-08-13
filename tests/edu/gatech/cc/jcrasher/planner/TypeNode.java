/*
 * TypeNode.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import edu.gatech.cc.jcrasher.plans.Plan;

/**
 * TypeNode
 * 
 * Node to access the plans of a type (sub-) plan space
 * up to a given maximal chaining depth.
 * - Child plan spaces are all FunctionNode or VariableNode.
 * - Knows how to map an index to an index of one of its child-plans.
 * 
 * Plan space depth recursion can only stop at a type, as a type
 * 	decides whether to recurse to another function or stick with 
 * 	(the predifened) values.
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-08-01 last changed
 */
public abstract class TypeNode implements PlanSpaceNode {

	/**
	 * Child types, i.e. victim and param types up to our max depth - 1
	 */
	private PlanSpaceNode[] children = null;
	
	/**
	 * Size of each child's plan space (given their max depth), e.g.:
	 * - (3, 5, 2) --> own size = 10
	 * - (0, 10, 10, 0, 5) --> 25
	 */
	private int[] childSizes = null;
	private int[] childRanges = null;	// childRanges = (2, 7, 9) --> [0..2], [3..7], [8..9]
															// (-1, 9, 19, 19, 24) --> [0..-1], [0..9], [10..19], [20..19], [20..24]
	private int planSpaceSize = -1;	//own plan space size = sum of childrens'
	
	/**
	 * Precond: 0 <= childIndex < children.length
	 * Postcond: no side effect
	 * 
	 * Map childIndex to the lowest index that belongs to this child, e.g. for
	 * 	childRanges = (-1, 9, 19, 19, 24)
	 * 		lowestRangeHit(0) = 0
	 *  		lowestRangeHit(1) = 0
	 * 		lowestRangeHit(4) = 20
	 */
	private int lowestRangeHit(int childIndex) {
		assert childIndex >= 0;
		assert childIndex < children.length;
		
		if (childRanges == null) {
			getPlanSpaceSize();
		}
		
		int res = 0;	//first index starts at zero
		
		if (childIndex>0) {
			res = childRanges[childIndex-1] + 1;
		}
		
		return res;
	}

	
	/**
	 * Precond: 0 <= childIndex < children.length
	 * Postcond: no side effect
	 * 
	 * Map childIndex to the highest index that belongs to this child, e.g. for
	 * 	childRanges = (-1, 9, 19, 19, 24)
	 * 		highestRangeHit(0) = -1
	 *  		highestRangeHit(1) = 9
	 * 		highestRangeHit(4) = 24
	 */
	private int highestRangeHit(int childIndex) {
		assert childIndex >= 0;
		assert childIndex < children.length;

		if (childRanges == null) {
			getPlanSpaceSize();
		}
		
		return childRanges[childIndex];
	}
	
	
	/**
	 * Sets the children. To be called by extending classes only.
	 * @param children The children to set
	 */
	protected void setChildren(PlanSpaceNode[] children) {
		this.children = children;
	}
	
	protected PlanSpaceNode[] getChildren() {
		return children;
	}
	
	/**
	 * Precond: true
	 * Postcond: cached sizes of all sub plan spaces to speed up getPlan(int)
	 * 
	 * A type's plan space size is the sum of its child plan spaces.
	 * 
	 * @return size of this sub plan space = nr different plans this 
	 * 	plan space can return via getPlan(int)
	 */
	public int getPlanSpaceSize() {
		assert children != null;
		
		/* Compute childrens' and own plan space sizes */
		if (childSizes == null) {	//first call
			childSizes = new int[children.length];
			
			/* Compute child sizes recursively */
			for (int i=0; i<children.length; i++) {
				childSizes[i] = children[i].getPlanSpaceSize();
			}
			
			/*Add up childrens' plan space sizes */
			childRanges = new int[children.length];
			int res = 0;	//no children: one plan for static non-arg meth
			for (int i=0; i<children.length; i++) {
				res += childSizes[i];	//partial sum so far
				childRanges[i] = res - 1;	//compute index-range for each sub space
			}
			planSpaceSize = res;
		}
		
		return planSpaceSize;
	}
	
	
	/**
	 * Precond: 0 <= planIndex < getPlanSpaceSize()
	 * Postcond: no side-effects
	 * 
	 * Retrieve childrens' plan according to canonical ordering. For e.g. sub spaces of sizes
	 * (3, 5, 2) --> childRanges = [0..2], [3..7], [8..9]
	 * 	[0..2] - 0 = [0..2]
	 * 	[3..7] - 3 = [0..4]
	 * 	[8..9] - 8 = [0..1]
	 * 
	 * @param planIndex the index of the plan according to the node's 
	 * 	canonical order, taken from [0..getPlanSpaceSize()-1]
	 * @return childrens' plans according to the ordering semantics, never null
	 */	
	public Plan getPlan(int planIndex) {
		assert planIndex >= 0;
		assert planIndex < getPlanSpaceSize();
		
		/* Make sure children and own sizes are cached */
		if (childSizes==null) {
			getPlanSpaceSize();
		}
		
		int childIndex = getChildIndex(planIndex);
		int childPlanIndex = getChildPlanIndex(childIndex, planIndex);
		
		return children[childIndex].getPlan(childPlanIndex);
	}


	/**
	 * @return index into child's childIndex plan space at index planindex
	 */
	protected int getChildPlanIndex(int childIndex, int planIndex) {
		
		return planIndex-lowestRangeHit(childIndex);
	}
	
	
	/**
	 * Get index into children array, that is hit by this planIndex
	 */
	protected int getChildIndex(int planIndex) {
		
		int i=0;	//find child, whose sub range is hit
		/* [7..7] [8..7] [8..8] 
		 * 	7 --> first range 
		 * 	8 --> third range */
		while ((planIndex < lowestRangeHit(i)) || (planIndex > highestRangeHit(i))) { 
			i++; 
		}
		return i;		
	}
	
	
	/**
	 * Retrieve child plan space node
	 */
	protected PlanSpaceNode getChild(int childIndex) {
		assert childIndex >=0;
		assert childIndex < children.length;
		
		return children[childIndex];
	}
}
