/*
 * TestStmt.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans.blocks;

/**
 * TestStmt
 * 
 * ADT hiding a code statement intended to crash some method or constructor
 * - to generate a needed instance
 * - to invoke some method to modify a instance (side-effect)
 * - to invoke some method/ constructor under testclasses
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-06-12 last changed
 */
public interface BlockStatement {
	
	/**
	 * @return a specialized representation of the statement like 
	 * - A a = new A(null);
	 * - b.m(0);
	 */
	public String toString(Class<?> testeeType);
}