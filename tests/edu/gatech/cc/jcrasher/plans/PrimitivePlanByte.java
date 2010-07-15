/*
 * PrimitivePlanByte.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans;

/**
 * PrimitivePlanByte
 *
 * Java-syntax of how to define a value
 * 
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-06-04 last changed
 */
public class PrimitivePlanByte extends PrimitivePlan {

	/**
	 * Constructor
	 * 
	 * @param pVariable hardcoded primitive value, not via java-wrapper-constructor
	 */
	public PrimitivePlanByte(Object pVariable) {
		super(pVariable);
	}
	

	/***************************************************************************
	 * How to reproduce this value=object?
	 */
	@Override
    public String toString() {
		return "(byte)" +getValue().toString();
	}
}