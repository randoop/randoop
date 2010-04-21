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
 * @version	$Id: DotClassPlan.java,v 1.2 2008-07-04 14:52:49 cpacheco Exp $
 */
public class DotClassPlan implements Plan {

	public Class<?> getReturnType() {
		return Class.class;
	}

	public String toString(Class<?> testee) {
		return "java.lang.Object.class";
	}

}
