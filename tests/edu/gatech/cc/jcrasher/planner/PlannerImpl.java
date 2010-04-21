/*
 * PlannerImpl.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import static edu.gatech.cc.jcrasher.Constants.LS;
import static edu.gatech.cc.jcrasher.Constants.MAX_NR_TEST_METHS_PER_CLASS;
import static edu.gatech.cc.jcrasher.Constants.MAX_PLAN_RECURSION;
import static edu.gatech.cc.jcrasher.Constants.VERBOSE_LEVEL;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.gatech.cc.jcrasher.Constants.PlanFilter;
import edu.gatech.cc.jcrasher.Constants.Verbose;
import edu.gatech.cc.jcrasher.plans.Plan;
import edu.gatech.cc.jcrasher.plans.blocks.Block;
import edu.gatech.cc.jcrasher.rules.ClassWrapper;
import edu.gatech.cc.jcrasher.rules.ConstructionRules;


/**
 * PlannerImpl
 * 
 * Generalized planner creates Blocks with chained plans and local variables:
 * - Hides class-wrapper- and plans- database, how instances are created, what
 *   combination of methods are called etc.
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-07-31 last changed
 */
public class PlannerImpl implements Planner {
	
	/**
	 * All planner classes in this package need access
	 */
	public static ConstructionRules rules = null;

	/* Cache constructed nodes */
	protected Map<Class<?>,ClassUnderTest> plans = new Hashtable<Class<?>,ClassUnderTest>(); 
	

	/**
	 * Constructor
	 */
	public PlannerImpl(ConstructionRules pRules) {
		rules = pRules;
	}
	
	
	/**
	 * @return nr of plans the entire planspace of class c holds for
	 * max chaining depth MAX_PLAN_RECURSION.
	 * This is the sum of the plan-spaces of the public constructors and methods
	 * declared by the class.
	 * 
	 * @param c class under testclasses
	 */
	public int getPlanSpaceSize(Class<?> c) {
		assert c != null;
		/* Create node hierarchy for class and query it for its size */
		
		if (!plans.containsKey(c)) {	//cache miss
			plans.put(c, new ClassUnderTest(c, MAX_PLAN_RECURSION));
		}
		
		return plans.get(c).getPlanSpaceSize();
	}

		
	/**
	 * Retrieve testclasses blocks according of specified class and its indices into plan space.
	 * @param indices is guaranteed to be free of duplicates for entire class-space
	 * @param arrayCursor starting index into indices - retrieve from here upto
	 * 	max tests/ class = next block
	 */
	public Block[] getTestBlocks(Class<?> c, int[] indices, int arrayCursor) {
		assert c != null;
		assert indices != null;
		
		if (!plans.containsKey(c)) {	//cache miss
			plans.put(c, new ClassUnderTest(c, MAX_PLAN_RECURSION));
		}		
		ClassUnderTest node = plans.get(c);		

		/* Retrieve distinct selected plans from plan space */
		List<Block> res = new Vector<Block>();
		for (int i=0; i+arrayCursor<indices.length && i<MAX_NR_TEST_METHS_PER_CLASS; i++) {
				res.add(node.getBlock(indices[i+arrayCursor]));
		}
		return res.toArray(new Block[res.size()]);
	}
	
	
	protected StringBuffer flushRules(boolean isTypeNeeded) {
		StringBuffer sb = new StringBuffer();
		
    for (ClassWrapper wrapper: rules.getWrappers()) {
    	if (wrapper.isNeeded() != isTypeNeeded) {
    		continue;	//not interested in printing
    	}
    	
			sb.append(LS+LS+wrapper.getWrappedClass().getCanonicalName());
			for (Plan value: wrapper.getPresetPlans(PlanFilter.ALL)) {	//preset values
				sb.append(LS+"\t"+value.toString(wrapper.getWrappedClass()));
			}
			if (!wrapper.isLibraryType()) {	//not interested in JDK-defined constructors
				for (Constructor con: wrapper.getConstrsVisGlobal()) {
					sb.append(LS+"\t"+con.toString());	//methods that return this type
				}
			}

			/* Interested in JDK-returning methods iff defined outside the JDK
			 * This was ensured during findRules. */
			for (Method meth: wrapper.getConMethsVisGlobal()) {				
				sb.append(LS+"\t"+meth.toString());	//constructors
			}
    }		
    return sb;
	}
	
	
	/**
	 * To be called after a getX(c) method has been called for each class c
	 * under testclasses
	 */
	public void flush() {
		
		if (Verbose.DEFAULT.equals(VERBOSE_LEVEL)) {
			return;	//no output
		}
		
		/* Verbose.VERBOSE | Verbose.ALL */
				
		/* Each constructor/ method to be crashed = non-private, non-abstract */
		StringBuffer sb = new StringBuffer("*** Methods and constructors under testclasses:");
		for (ClassUnderTest cPlan: plans.values()) {
			sb.append(LS+LS+cPlan.getWrappedClass().getCanonicalName());  //qualified class name
			
			for (PlanSpaceNode pNode: cPlan.getChildren()) {
				FunctionNode fNode = (FunctionNode) pNode;
				sb.append(LS+"\t"+fNode.toString());	//method or constructor under testclasses
			}				
   	}
   		
		
	  /*
	   * 2. Each value and public, non-abstract constructing function 
	   *    of each needed instance and all of their children.
	   */
    sb.append(LS+LS+LS+"*** Rules to create needed values:");
		sb.append(flushRules(true));		
		
  	if (Verbose.ALL.equals(VERBOSE_LEVEL)) {
      sb.append(LS+LS+LS+"*** Rules that were not needed:");
  		sb.append(flushRules(false));
  	}

    sb.append(LS+LS);
    System.out.println(sb);
	}	
}
