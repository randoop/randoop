/*
 * MethodNode.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import static edu.gatech.cc.jcrasher.planner.PlannerImpl.rules;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Vector;

import edu.gatech.cc.jcrasher.Constants;
import edu.gatech.cc.jcrasher.Constants.PlanFilter;
import edu.gatech.cc.jcrasher.plans.Plan;
import edu.gatech.cc.jcrasher.plans.PlanForMethod;
import edu.gatech.cc.jcrasher.rules.ClassWrapperImpl;

/**
 * MethodNode
 * 
 * Node to access the plans of a method (sub-) plan space
 * up to a given maximal chaining depth.
 * - Knows how to concrete PlanForMethod
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-08-01 last changed
 */
public class MethodNode extends FunctionNode {

	protected Method meth = null;	//wrapped method
	
	
	/**
	 * Constructor:
	 * 	create dependent type plan space nodes according to pMeth
	 * 
	 * @param pMeth method, whose plan space is to be traversed
	 * @param pMaxRecursion How deep should we traverse the sub-tree?
	 */	
	public MethodNode(Method pMeth, int pMaxRecursion, PlanFilter filter) {		
		assert pMaxRecursion >= 1;	//this method eats up one step in depth
		assert pMeth != null;
		
		meth = pMeth;						
		List<TypeNode> depNodes = new Vector<TypeNode>();		
		
		/* First dimension: victim instance */
		if (Modifier.isStatic(pMeth.getModifiers()) == false) {	//non-static method			
			Class<?> decClass = pMeth.getDeclaringClass();
			ClassWrapperImpl vW = (ClassWrapperImpl) rules.getWrapper(decClass);	//victim
			depNodes.add(new TypeNeededNode(vW, pMaxRecursion-1, Constants.removeNull(filter)));
		}
								
		/* Second, .. n-th dimesion: Add each parameter */
		for (Class<?> paramType: pMeth.getParameterTypes()) {			
			ClassWrapperImpl pW = (ClassWrapperImpl) rules.getWrapper(paramType);					
			depNodes.add(new TypeNeededNode(pW, pMaxRecursion-1, filter));
		}

		/* create iterators for each type dimension and set field in super class */	
		setChildren(depNodes.toArray(new TypeNode[depNodes.size()]));
	}



	/**
	 * Precond: 0 <= planIndex < getPlanSpaceSize()
	 * 
	 * @return concrete method plan according to index.
	 */
	public Plan getPlan(int planIndex) {
		Plan[] depPlans = getChildrenPlans(planIndex);	//enforces our precondition
	
		/* Zero-dim ok, iff non-arg static meth */
		if (depPlans.length==0) {
			return new PlanForMethod(meth, new Plan[0]);
		}//end traversal of zero-dim space
	
	
		/* Build method plan, distribute plans to victim, params */
		else {
			assert depPlans.length > 0;	//at least one dimension non-empty
				
			if (Modifier.isStatic(meth.getModifiers()) == false) {	//first dim is victim
				Plan[] paramPlans = new Plan[depPlans.length-1];
				for (int j=0; j<paramPlans.length; j++) {
					paramPlans[j] = depPlans[j+1];
				}
				return new PlanForMethod(meth, paramPlans, depPlans[0]);
			}
			else {	//Non-static method with >=1 arguments
				return new PlanForMethod(meth, depPlans);
			}
		}
	}
	
	
	/**
	 * @return the wrapped method
	 */
	Method getMeth() {
		return meth;
	}
	
	
	/**
	 * @return method signature
	 */
	@Override
    public String toString() {
		return meth.toString();
	}

}
