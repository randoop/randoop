/*
 * VariableNode.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import java.util.List;
import java.util.Vector;

import edu.gatech.cc.jcrasher.plans.Plan;

/**
 * VariableNode
 * 
 * Node to access a list of values, i.e. has no children plan spaces.
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-08-01 last changed
 */
public class ValueNode implements PlanSpaceNode {

	protected final List<Plan> plans = new Vector<Plan>();

	
	/**
	 * Constructor
	 */
	public ValueNode(List<Plan> pPlans) {
		assert pPlans != null;
		plans.addAll(pPlans);
	}

	
	public int getPlanSpaceSize() {
		return plans.size();
	}

	public Plan getPlan(int planIndex) {
		return plans.get(planIndex);
	}

}
