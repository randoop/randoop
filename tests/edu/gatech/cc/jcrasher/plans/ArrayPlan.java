/*
 * ArrayPlan.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans;


import edu.gatech.cc.jcrasher.rules.ClassWrapper;
import edu.gatech.cc.jcrasher.writer.CodeGenFct;

/**
 * ArrayPlan
 *
 * - Manages initialization with some objects of component type
 * 
 * ClassWrapper --> Class
 * ClassWrapper --> Plan*
 * Plan   --> Instance
 *
 * CS 8903 Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-03-16 last changed 
 */
public class ArrayPlan extends NullPlan {

  /**
   * Which plans generate array's components to initialize this value?
   * - zero-elem-array --> empty array
   * - list of params in order to initialize araray
   */
  protected Plan[] componentPlans = null;
  
  /**
   * Type of leaf component, which is not an array e.g. int
   */
  protected Class<?> leafType = null;

  
  /**
   * Depth of array-tree
   */
  protected int dimensionality = 1;
  
  
  /**
   * Set leafType and dimensionality
   */
  protected void discoverLeafLevel() {

	leafType = getReturnType().getComponentType();
	
	/* descend in array-tree */
  	while (leafType.isArray() == true) {
  	  dimensionality += 1;
  	  leafType = leafType.getComponentType();
  	}
  }
  

	/***********************************************************************
	 * Constructor
	 */
	public ArrayPlan(ClassWrapper pCW) {
		this(pCW.getWrappedClass());
	}
	
	
	/**
	 * Constructor
	 *   added for extending ESC Java, Christoph Csallner 2004-06-08
	 */
	public ArrayPlan(Class<?> type) {
		super(type);
		discoverLeafLevel(); 
	}
  
  /**
   * get plans for all components
   */
  protected Plan[] getComponentPlans() {
  	return componentPlans;
  }

  /**
   * set plans for all components
   */
  public void setComponentPlans(Plan[] pPlans) {
  	componentPlans = pPlans;
  }
  

  
  /**
   * How to reproduce this value=object?
   * - Variable or (recursive) initializer-chain for user-output after some
   *   mehtod-call crashed using this object as param.
   * - Example: {{11,12}, {21,22}}
   */
  @Override
public String toString(Class<?> testee) {  		 	
		/* Constructor */
		StringBuffer res = new StringBuffer();
		res.append("new " +CodeGenFct.getName(leafType, testee));
	
		/* print dimensionality times [] */
		for (int d=0; d<dimensionality; d++) {
			res.append("[]");
		}
	
		res.append("{");
	
		/* Recurse to parameters */
		for (int i=0; i<componentPlans.length; i++) {
	       	if (i>0) { res.append(", "); }        			//separator		
			res.append(componentPlans[i].toString(testee)); //value
		}
		
		/* return assembeled String */
		res.append("}");	
		return res.toString();
  }
}