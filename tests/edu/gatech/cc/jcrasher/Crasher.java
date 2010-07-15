/*
 * Crasher.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher;

import java.util.Set;

/**
 * Crasher
 *
 * Tries to crash a method and eventually reports about it afterwards.
 *
 * CS 8903 Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-03-13 last changed 
 */
public interface Crasher {


	/**
	 * Do bad things with the methods of these classes and talk about them.
	 */
	public void crashClasses(Set<Class<?>> pClasses);
}
