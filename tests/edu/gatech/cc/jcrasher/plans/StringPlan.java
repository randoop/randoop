/*
 * StringPlan.java
 * 
 * Copyright 2002,2005 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans;

/**
 * StringPlan
 * 
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2005-08-29 Moved value field from PrimitivePlan
 */
public class StringPlan extends NullPlan {

	protected String value = null;
	
	@Override
    public String toString() {return "\"" +value +"\"";}
	@Override
    public String toString(Class<?> type) {return toString();}
	
	
	/**
	 * Constructor
	 * 
	 * @param pVariable hardcoded primitive value, never null
	 */
	public StringPlan(Object pVariable) {
		super(String.class);
		
		if (pVariable==null || !pVariable.getClass().equals(String.class)) {
			throw new IllegalArgumentException("Parameter has to be a non-null String");
		}
		
		value = (String) pVariable;
	}
}
