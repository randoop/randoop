/*
 * Block.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans.blocks;

import java.lang.reflect.Member;

import edu.gatech.cc.jcrasher.plans.PlanForLocalID;

/**
 * Block
 * 
 * ADT hiding a code block intended to crash some method or constructor
 * - some statements to generate needed instances
 * - some statements to invoke methods/ constructors under testclasses
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-06-11 last changed
 */
public interface Block {
	
	/**
	 * To be called from above, like CodeWriter
	 * 
	 * @param pIdent ident on which to put block opening/ closing bracket
	 * @param testee the class under testclasses
	 * @return a specialized representation of the testclasses block like:
	 * 
	 * 	{
	 * 		A.m(null);
	 * 	}
	 */
	public String toString(String pIdent, Class<?> testee);
	
	
	/**
	 * @return testee that this block contains code for calling.
	 */
	public Member getTestee();
	
	
	/**
	 * To be called from below, like BlockStatement
	 * 
	 * @return an unused local identifier encoding pClass, e.g.
	 *         (i1, s2, i3) for a sequence like: (int, String[], Integer)
	 */
	public PlanForLocalID getNextID(Class<?> pClass);
	
	/**
	 * setty
	 * 
	 * @param pBlockStmts (empty) list, but never null
	 */
	public void setBlockStmts(BlockStatement[] pBlockStmts);
}
