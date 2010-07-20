/*
 * FunctionNode.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import edu.gatech.cc.jcrasher.plans.Plan;

/**
 * FunctionNode
 * 
 * Node to access the plans of a method or constructor (sub-) plan space
 * up to a given maximal chaining depth.
 * - Child plan spaces are all TypeNode.
 * - Knows how to transfer an index in a certain combination of child-plans
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-08-01 last changed
 */
public abstract class FunctionNode implements PlanSpaceNode {

	/**
	 * Child types, i.e. victim and param types up to our max depth - 1
	 */
	protected TypeNode[] children = null;
	
	/**
	 * Size of each child's plan space (given their max depth), e.g.:
	 * - (3, 5, 2) --> own size = 30
	 * - (0, 10, 10, 5) --> 0
	 */
	protected int[] childSizes = null;
	protected int[] canonicalSubSapceSizes = null;	//(5*2*1, 2*1, 1) for (3, 5, 2)
	protected int planSpaceSize = -1;	//own plan space size = product of childrens'


	/**
	 * Precond: true
	 * Postcond: cached sizes of all sub plan spaces to speed up getPlan(int)
	 * 
	 * A method's or constructor's plan space size is the product of
	 * 	its child plan spaces.
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
			
			/* Multiply childrens' plan space sizes */
			int res = 1;	//no children: one plan for static non-arg meth
			for (int childSize: childSizes) {
				res *= childSize;	//first zero will zero the entire result
			}
			planSpaceSize = res;
			
			/* Compute canonical sub space sizes for each dimesion */
			canonicalSubSapceSizes = new int[children.length];
			for (int i=children.length-1; i>=0; i--) {
				if (i==children.length-1) {
					canonicalSubSapceSizes[i] = 1;
				}
				else {
					canonicalSubSapceSizes[i] = childSizes[i+1] * canonicalSubSapceSizes[i+1];
				}
			}
		}
		
		return planSpaceSize;
	}
	
	
	
	/**
	 * Precond: 0 <= planIndex < getPlanSpaceSize()
	 * Postcond: no side-effects
	 * 
	 * Retrieve childrens' plans according to canonical ordering. For e.g. sub spaces of sizes
	 * 	(4, 3, 7) --> 84 the indices are a*3*7 + b*7 + c*1, with 
	 * 	a element [0..3], b element [0..2], c element [0..6]
	 *     (0, 0, 0) --> 0
	 * 	(0, 0, 6) --> 6
	 * 	(0, 1, 0) --> 7
	 * 	(3, 2, 6) --> 83
	 * 
	 * So for an index e.g. of 83 the dimensions are checked from left:
	 * 	83 / 3*7*1 = 3 + 20/21 --> children[0].getPlan(3)
	 * 	20 / 7*1 = 2 + 6/7 --> children[1].getPlan(2)
	 * 	6 / 1 = 6 --> children[2].getPlan(6)
	 * 
	 * @param planIndex the index of the plan according to the node's 
	 * 	canonical order, taken from [0..getPlanSpaceSize()-1]
	 * @return childrens' plans according to the ordering semantics, never null
	 */	
	public Plan[] getChildrenPlans(int planIndex) {		
		assert planIndex >= 0; //enforce precondition
		assert planIndex < getPlanSpaceSize();	//terminate here iff any canonicalSubSapceSizes[i] == 0
		
		/* Make sure children and own sizes are cached */
		if (childSizes==null) {
			getPlanSpaceSize();
		}
		
		Plan[] res = new Plan[children.length];	//no children --> empty list
		
		/* Determine index in each child dimension */
		int currentIndex = planIndex;	//index into remaining sub spaces
		for (int i=0; i<res.length; i++) {
			int childIndex = currentIndex / canonicalSubSapceSizes[i];	//division, lower bounded, index in child's dimension
			currentIndex = currentIndex - (childIndex * canonicalSubSapceSizes[i]);	//left over from division
			res[i] = children[i].getPlan(childIndex);
		}
		
		return res;
	}
	
	
	
	/**
	 * Sets the children. To be called by extending classes only.
	 * @param children The children to set
	 */
	protected void setChildren(TypeNode[] children) {
		this.children = children;
	}

}
