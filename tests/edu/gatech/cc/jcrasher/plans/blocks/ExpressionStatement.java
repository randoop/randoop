/*
 * ExpressionStatement.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans.blocks;

import edu.gatech.cc.jcrasher.plans.Plan;
import edu.gatech.cc.jcrasher.plans.PlanForFunction;

/**
 * ExpressionStatement
 * 
 * ADT hiding a code statement to invoke a function (causing side-effect) 
 * 
 * We need only a subset as follows:
 * 
 * ExpressionStatement ::= 
 * 			  MethodInvocation;
 * 			| ClassInstanceCreationExpression;
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-06-12 last changed
 */
public class ExpressionStatement implements BlockStatement {

	/**
	 * Plan to invoke the wrapped fct
	 */
	private Plan fctPlan  = null;

	/**
	 * Constructor
	 * 
	 * Postcond:
	 * 1. fctPlan != null
	 */
	public ExpressionStatement(PlanForFunction pFctPlan) {
		assert pFctPlan != null;
		
		fctPlan = pFctPlan;
	}


	/**
	 * @return a specialized representation of the statement like
	 * - b.m(0);
	 * - new A();
	 */
	public String toString(Class<?> testeeType) {
		String res = null;
		
		res = fctPlan.toString(testeeType) +";";
		
		assert res != null;
		return res;
	}
}
