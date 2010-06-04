/*
 * TypeNeededNode.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import static edu.gatech.cc.jcrasher.planner.PlannerImpl.rules;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import edu.gatech.cc.jcrasher.Constants;
import edu.gatech.cc.jcrasher.Constants.PlanFilter;
import edu.gatech.cc.jcrasher.rules.ClassWrapper;
import edu.gatech.cc.jcrasher.rules.ClassWrapperImpl;

/**
 * TypeNeededNode
 * 
 * Constructs a TypeNode from a ClassWrapper:
 * 	extract all constructing functions iff remaining recursion depth >0
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-08-05 last changed
 */
public class TypeNeededNode extends TypeNode {

	protected final String name;
	
	/**
	 * Constructor:
	 * - Grab all (predefined) values
	 * - Grab all constructing functions iff maxRecursion >= 1
	 * 
	 * Precond: maxRecursion >= 0
	 * 
	 * @param pCW contains all  values (and constructing functions)
	 * @param remainingRecursion maximal length of function chain to be appended
	 * @param filter is the invoking function interested i.e. in null?
	 */
	public TypeNeededNode (ClassWrapper pCW, int remainingRecursion, PlanFilter filter) {
		assert pCW != null;
		assert remainingRecursion >= 0;
		
		((ClassWrapperImpl) pCW).setIsNeeded();
		
		name = pCW.getWrappedClass().getName(); 				//FIXME debug		
		List<PlanSpaceNode> childSpaces = new Vector<PlanSpaceNode>();		
		childSpaces.add(new ValueNode(pCW.getPresetPlans(filter)));
		
		/* NON_NULL enforced above, transitively used values may be null */
		filter = Constants.addNull(filter);
		
		/* functions only iff wanted and additional chaining allowed */
		if (remainingRecursion>0) {
			
			/* same for class and all its implementing/ extending children*/		
			List<Class<?>> classes = new Vector<Class<?>>(pCW.getChildren());
			classes.add(pCW.getWrappedClass());
						
			for (Class<?> c: classes) {
				ClassWrapper cw = rules.getWrapper(c);
				assert cw != null;
				
				if (!cw.isLibraryType()) {	//not interested in JDK-defined constructors
					for (Constructor con: cw.getConstrsVisGlobal()) {	//public constructors
						childSpaces.add(new ConstructorNode(con, remainingRecursion, filter));
					}
				}
				
				/* Interested in JDK-returning methods iff defined outside the JDK
				 * This was ensured during findRules. */
				for (Method meth: cw.getConMethsVisGlobal()) {	//constructing methods		
					childSpaces.add(new MethodNode(meth, remainingRecursion, filter));
				}
			}			
		}
		
		/* set gathered child plan spaces in super class */
		setChildren(childSpaces.toArray(new PlanSpaceNode[childSpaces.size()]));
	}
}
