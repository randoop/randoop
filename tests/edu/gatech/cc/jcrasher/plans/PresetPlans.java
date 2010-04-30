/*
 * PresetPlans.java
 * 
 * Copyright 2005 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

/**
 * Set hardcoded values:
 *
 * Set array of simple type like int[] using predefined simple values.
 * So instead of looking for int[] returning methods we settle for a few
 * hardcoded int[] like {}, {0}, {-1, 0}.
 * 
 * TODO: General treatment of simple type arrays of arbitrary dimension, see: 
 * http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#14152
 */
public class PresetPlans {
	
	/* Primitive types */
	protected static final Plan[] booleanPlans = new Plan[] {
		new PrimitivePlan(new Boolean(false)),
		new PrimitivePlan(new Boolean(true))
	};
	protected static final Plan[] bytePlans = new Plan[] {
		new PrimitivePlanByte(new Byte((byte)0)),
		new PrimitivePlanByte(new Byte((byte)255))
	};
	protected static final Plan[] charPlans = new Plan[] {
		new PrimitivePlanChar(new Character(' ')),
	};
	protected static final Plan[] shortPlans = new Plan[] {
		new PrimitivePlanShort(new Short((short)-1)),
		new PrimitivePlanShort(new Short((short)0))
	};
	protected static final Plan[] intPlans = new Plan[] {
		new PrimitivePlan(new Integer(-1)),
		new PrimitivePlan(new Integer(0)),
		new PrimitivePlan(new Integer(1))
	};
	protected static final Plan[] longPlans = new Plan[] {
		new PrimitivePlan(new Long(-1)),
		new PrimitivePlan(new Long(0))
	};
	protected static final Plan[] floatPlans = new Plan[] {
		new PrimitivePlanFloat(new Float(-100.123456789f)),
		new PrimitivePlanFloat(new Float(0.0f))
	};
	protected static final Plan[] doublePlans = new Plan[] {
		new PrimitivePlan(new Double(-1.123456789d)),
		new PrimitivePlan(new Double(0.0d))
	};	
		
	
	/* Complex types */
	protected static final Plan[] classPlans = new Plan[] {
		new DotClassPlan()
	};
	protected static final Plan[] stringPlans = new Plan[] {
		new StringPlan(""),
		new StringPlan("`\'@#$%^&/({<[|\\n:.,;")
	};
	

	

	/*
	 * new Object()
	 */
	protected static Plan[] getObject() {
		Constructor new_Object = null;
		try {
			new_Object = Object.class.getDeclaredConstructor(new Class[0]);
		}
		catch (Exception e) {e.printStackTrace();}
		assert new_Object!=null;
		
		return new Plan[]{
				new PlanForConstructor(new_Object, new Plan[0])
		};
	}

	
	/*
	 * new java.util.Hashtable()  --implements java.util.Map
	 */
	protected static Plan[] getHashtable() {
		Constructor con = null;
		try {
			con = Hashtable.class.getDeclaredConstructor(new Class[0]);
		}
		catch (Exception e) {e.printStackTrace();}
		assert con!=null;
		
		return new Plan[]{
				new PlanForConstructor(con, new Plan[0])
		};
	}	
	
	
	/*
	 * new java.util.Vector()  --implements java.util.List
	 */
	protected static Plan[] getVector() {
		Constructor con = null;
		try {
			con = java.util.Vector.class.getDeclaredConstructor(new Class[0]);
		}
		catch (Exception e) {e.printStackTrace();}
		assert con!=null;
		
		return new Plan[]{
				new PlanForConstructor(con, new Plan[0])
		};
	}		
	
	
	/*
	 * int[]
	 */
	protected static Plan[] getIntArray1() {
		Class<?> c = int[].class;
		ArrayPlan[] plans = new ArrayPlan[2];
		
		plans[0] = new ArrayPlan(c);  //{}
		plans[0].setComponentPlans(new PrimitivePlan[0]);
		plans[1] = new ArrayPlan(c);  //{0}
		plans[1].setComponentPlans(new PrimitivePlan[] {new PrimitivePlan("0")});
		return plans;
	}
	
	
	/*
	 * String[]
	 */
	protected static Plan[] getStringArray1() {
		Class<?> c = String[].class;
		ArrayPlan[] plans = new ArrayPlan[2];
		
		plans[0] = new ArrayPlan(c);  //{}
		plans[0].setComponentPlans(new StringPlan[0]);
		plans[1] = new ArrayPlan(c);  //{""}
		plans[1].setComponentPlans(new StringPlan[] {new StringPlan("")});
		return plans;
	}	
	
	
	/*
	 * T[]
	 */
	protected static Plan[] getEmptyArray(Class<?> c) {
		ArrayPlan[] plans = new ArrayPlan[1];
		
		plans[0] = new ArrayPlan(c);  //{}
		plans[0].setComponentPlans(new Plan[0]);
		
		return plans;
	}	

	/**
	 * @return possibly empty list of preset plans, but never null.
	 */
	public static Plan[] getPreset(Class<?> pClass) {
		/* Primitive */
		if (pClass.equals(boolean.class))	{return booleanPlans;}
		if (pClass.equals(byte.class))		{return bytePlans;}
		if (pClass.equals(char.class))		{return charPlans;}
		if (pClass.equals(short.class))		{return shortPlans;}
		if (pClass.equals(int.class))			{return intPlans;}
		if (pClass.equals(long.class))		{return longPlans;}
		if (pClass.equals(float.class))		{return floatPlans;}
		if (pClass.equals(double.class))	{return doublePlans;}
		
		/* Complex */
		if (pClass.equals(Class.class))				{return classPlans;}
		if (pClass.equals(Comparable.class))	{return stringPlans;}
		if (pClass.equals(String.class))			{return stringPlans;}
		if (pClass.equals(Object.class))			{return getObject();}
		
		if (pClass.equals(java.util.List.class))	{return getVector();}
		if (pClass.equals(java.util.Map.class))		{return getHashtable();}
		if (pClass.equals(java.util.Vector.class)){return getVector();}
		
		/* Array */
		if (pClass.equals(int[].class)) 	{return getIntArray1();}
		if (pClass.equals(String[].class)){return getStringArray1();}
		if (pClass.isArray()) 						{return getEmptyArray(pClass);}
		
		/* No preset plans for other complex types */
		return new Plan[0];
	}
}
