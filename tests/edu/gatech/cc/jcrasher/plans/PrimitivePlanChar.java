/*
 * PrimitivePlanChar.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans;

/**
 * PrimitivePlanChar
 *
 * Java-syntax of how to define a value
 * 
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-06-04 last changed
 */
public class PrimitivePlanChar extends PrimitivePlan {

	/**
	 * Constructor
	 * 
	 * @param pVariable hardcoded primitive value, not via java-wrapper-constructor
	 */
	public PrimitivePlanChar(Object pVariable) {
		super(pVariable);
	}
	

	/**
	 * How to reproduce this value=object?
	 */
	// Is it a bug that the value is not quoted if it's a quote, newline, etc.?
	@Override
    public String toString() {return "'" +getValue().toString() +"'";}
}
