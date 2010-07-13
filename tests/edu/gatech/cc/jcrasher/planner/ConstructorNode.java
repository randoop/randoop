/*
 * ConstructorNode.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import static edu.gatech.cc.jcrasher.planner.PlannerImpl.rules;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Vector;

import edu.gatech.cc.jcrasher.Constants;
import edu.gatech.cc.jcrasher.Constants.PlanFilter;
import edu.gatech.cc.jcrasher.plans.Plan;
import edu.gatech.cc.jcrasher.plans.PlanForConstructor;
import edu.gatech.cc.jcrasher.rules.ClassWrapperImpl;

/**
 * ConstructorNode
 * 
 * Node to access the plans of a constructor (sub-) plan space
 * up to a given maximal chaining depth.
 * - Knows how to generate a concrete PlanForConstructor
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-08-01 last changed
 */
public class ConstructorNode extends FunctionNode {

	protected Constructor con = null;	//wrapped constructor
	
	
	/**
	 * Constructor:
	 * 	set dependent type plan space iterator according to pCon
	 * 	by creating iterators, i.e., over param-classes
	 * 
	 * pMeth method, whose plan space is to be traversed
	 * @param pMaxRecursion How deep should we traverse the sub-tree?
	 * @param filter Are we allowed to use null?
	 */
	public ConstructorNode(Constructor pCon, int pMaxRecursion, PlanFilter filter) {		
		assert pMaxRecursion >= 1;	//this method eats up one step in depth
		assert pCon != null;
		
		con = pCon;				
		List<TypeNode> depNodes = new Vector<TypeNode>();

		/* First, .. n-th dimesion: Add each parameter
		 * Inner class: Reflection returns enclosing type as first parameter */
		Class<?>[] paramsTypes = con.getParameterTypes();
		for (int j=0; j<paramsTypes.length; j++) {
			ClassWrapperImpl pW = (ClassWrapperImpl) rules.getWrapper(paramsTypes[j]);
			
			/* Filter (null): First param might be the enclosing type */
			PlanFilter planFilter = filter;
			if ((j==0) && (rules.getWrapper(con.getDeclaringClass()).isInnerClass() == true)) {
				planFilter = Constants.removeNull(filter);
			}
			depNodes.add(new TypeNeededNode(pW, pMaxRecursion-1, planFilter));
		}
		setChildren(depNodes.toArray(new TypeNode[depNodes.size()]));
	}
	
	
	
	/**
	 * Precond: 0 <= planIndex < getPlanSpaceSize()
	 * 
	 * @return concrete method plan according to index.
	 */
	public Plan getPlan(int planIndex) {
		Plan[] depPlans = getChildrenPlans(planIndex);	//enforces our precondition

		/* distinguish inner class from params */				
		if (rules.getWrapper(con.getDeclaringClass()).isInnerClass() == true) {	//first dim is enclosing instance
			assert depPlans.length >= 1;
			
			Plan[] paramPlans = new Plan[depPlans.length-1];
			for (int j=0; j<paramPlans.length; j++) {
				paramPlans[j] = depPlans[j+1];
			}
			return new PlanForConstructor(con, paramPlans, depPlans[0]);
		}
		else {	//Non-inner class constructor with >=0 arguments
			return new PlanForConstructor(con, depPlans);
		}
	}
	
	
	/**
	 * @return the wrapped constructor
	 */
	Constructor getCon() {
		return con;
	}

	
	/**
	 * @return method signature
	 */
	@Override
    public String toString() {
		return con.toString();
	}	
}
