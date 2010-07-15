/*
 * NullPlan.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans;

import edu.gatech.cc.jcrasher.writer.CodeGenFct;

/**
 * NullPlan
 * 
 * - Provides plan for null reference
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2005-08-30 Added stripping of package iff in same package as testee
 * 2005-08-29 Moved value field to PrimitivePlan
 * 2002-05-28 Moved class from state- to type-space implementation
 */
public class NullPlan implements Plan {

	protected Class<?> returnType = null;  //type of instance created by this plan

	
	public Class<?> getReturnType() {return returnType;}
	

	
	/**
	 * Constructor, to be called from outside the inheritance hierarchy
	 */
	public NullPlan(Class<?> pType) {
		assert pType != null;
		returnType = pType;
	}

	
	protected String getString(String type) {
		assert type!=null;
		return "(" +type +")" + "null";
	}
	

	@Override
    public String toString() {
		assert returnType!=null;
		return getString(CodeGenFct.getName(returnType));
	}
	
	
	/**
 	 * How to reproduce this value=object?
 	 * (Type) null 
   */
	public String toString(Class<?> testee) {
		assert testee!=null;
		assert returnType!=null;		
		return getString(CodeGenFct.getName(returnType, testee));
	}
}
