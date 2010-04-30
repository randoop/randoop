/*
 * PrimitivePlan.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans;


/**
 * PrimitivePlan
 *
 * Represents how to instantiate a wrapper-object for a java primtive type:
 * (Class.isPrimitive() == true)
 * Instance
 * - is wrapper-object
 * - will be passed to Method.invoke()
 * - will be automatically be converted to primitive type there 
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2005-08-29 Moved value field from PrimitivePlan
 * 2002-05-28 moved class from state- to type-space implementation
 */
public class PrimitivePlan extends NullPlan {

	protected Object value = null;  //result of plan execution

	public Object getValue() {  //never returns null
		assert value!=null;
		return value;
	}
	

	/**
	 * Constructor
	 * 
	 * @param pVariable hardcoded primitive value, not via java-wrapper-constructor
	 *        - never null
	 * 
	 * TODO: values different from hard-coded defaults ;-)
	 */
	public PrimitivePlan(Object pVariable) {
		super(pVariable.getClass());	
		value = pVariable;
		
		if (returnType.equals(Integer.class)) {returnType = Integer.TYPE;}
		if (returnType.equals(Long.class)) {returnType = Long.TYPE;}
		if (returnType.equals(Short.class)) {returnType = Short.TYPE;}
		if (returnType.equals(Byte.class)) {returnType = Byte.TYPE;}
		if (returnType.equals(Character.class)) {returnType = Character.TYPE;}
		if (returnType.equals(Boolean.class)) {returnType = Boolean.TYPE;}
		if (returnType.equals(Double.class)) {returnType = Double.TYPE;}
		if (returnType.equals(Float.class)) {returnType = Float.TYPE;}
	}

	
	@Override
    public String toString() {
		assert value!=null;
		return value.toString();
	}
	
	
	/**
	 * How to reproduce this value=object?
	 */
	@Override
    public String toString(Class<?> testee) {
		return toString();
	}
}
