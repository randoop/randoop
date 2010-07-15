/*
 * PrimitivePlanShort.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans;

/**
 * PrimitivePlanShort
 *
 * Java-syntax of how to define a value
 * 
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-06-04 last changed
 */
public class PrimitivePlanShort extends PrimitivePlan {

	/**
	 * Constructor
	 * 
	 * @param pVariable hardcoded primitive value, not via java-wrapper-constructor
	 */
	public PrimitivePlanShort(Object pVariable) {
		super(pVariable);
	}
	

	/***************************************************************************
	 * How to reproduce this value=object?
	 */
	@Override
    public String toString() {
		return "(short)" +getValue().toString();
	}
}
