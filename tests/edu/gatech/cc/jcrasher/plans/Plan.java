/*
 * Plan.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans;

/**
 * Plan
 *
 * - There can be different Plans for each class discovered.
 * - Each plan produces its own value.
 * 
 * ClassWrapper --> Class
 * ClassWrapper --> Plan*
 * Plan         --> Instance
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-03-13 last changed
 * 2002-05-28 moved class from state- to type-space implementation
 * 2002-06-12 Each plan can be used as a VariableInitializer
 */
public interface Plan {
 
	/**
	 * @return type of instance created by this plan
	 */
	public Class<?> getReturnType();

  
	/**
 	 * How to reproduce this value=object?
	 * - Variable or (recursive) constructor-chain for user-output after some
	 *   mehtod-call crashed using this object as param.
	 * - Example: new A(new B(1), null)
 	 */
	public String toString(Class<?> testee);
}

