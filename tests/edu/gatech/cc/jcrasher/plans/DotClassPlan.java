/*
 * DotClassPlan.java
 * 
 * Copyright 2005 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans;

/**
 * Object.class
 * 
 * The object representing the Object class
 * 
 * @author		Christoph Csallner
 */
public class DotClassPlan implements Plan {

	public Class<?> getReturnType() {
		return Class.class;
	}

	public String toString(Class<?> testee) {
		return "java.lang.Object.class";
	}

}
