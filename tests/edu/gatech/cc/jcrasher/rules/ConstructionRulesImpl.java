/*
 * ConstructionRulesImpl.java
 * 
 * Copyright 2002,2005 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.rules;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * ConstructionRulesImpl
 * 
 * - Builds up relation from user-specified classes.
 * - Creates ClassWrappers and sets them up (protected field-access for flags)
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-05-28 last changed
 */
public class ConstructionRulesImpl extends AbstractConstructionRules {

	/**
	 * Queue super, enclosing, and enclosed types of cw
	 */
	protected void queueFamily(ClassWrapper cw) {
		/* Child-of-relation: Store and queue parents for search
		 * Class<?> X: For each interface S with "X implements S" do
		 * Interface X: For each interface S with "X extends S" do */
		for (Class<?> superInterface: cw.getWrappedClass().getInterfaces()) {
			ClassWrapperImpl superIW = (ClassWrapperImpl) getWrapper(superInterface);
			superIW.addChild(cw.getWrappedClass());
		}
		// Q: Are transitive implemented interfaces returned as well?
		// A: No, gladly not [Class.getInterfaces()]
		// For class R with X extends R do
		Class<?> superClass = cw.getWrappedClass().getSuperclass();
		if (superClass != null) {
			ClassWrapperImpl superCW = (ClassWrapperImpl) getWrapper(superClass);
			superCW.addChild(cw.getWrappedClass());		
		}
		//else wrappedClass<?> was an interface, a primitive type, Object, or void
		

		/* Queue nested classes for search */		
		for (Class<?> nestedClass: cw.getWrappedClass().getDeclaredClasses()) {
			getWrapper(nestedClass);
		}
		
		/* Queue nesting class for search */
		Class<?> nestingClass = cw.getWrappedClass().getDeclaringClass();
		if (nestingClass != null) {	//else top-level class
			getWrapper(nestingClass);
		}		
	}	
	
	
	/**
	 * Add all rules to wrapper of return type iff return type complex.
	 * This implements the rule:
	 *   JCrasher uses for any simple type (int, boolean, etc.)
	 *   only predefined values for testclasses generation.
	 */
	protected void queueMethParams(ClassWrapper cw) {
		/* Methods: store and queue params, return types for search
		 * for each non-abstract declared (incl. overridden) method do */
		for (Method meth: cw.getWrappedClass().getDeclaredMethods()) {
			if (Modifier.isAbstract(meth.getModifiers()) == false){
				
				ClassWrapperImpl rW = (ClassWrapperImpl) getWrapper(meth.getReturnType());
				if (!rW.getWrappedClass().isPrimitive()) {
					rW.addConstrMeth(meth);	//Add to wrapper of complex return type
				}
				
				for (Class<?> paramType: meth.getParameterTypes()) {
					getWrapper(paramType);	//Queue param types even for non-public meths
				}												
			}
		}
		

		/* Constructors: Queue params for search */
		if (Modifier.isAbstract(cw.getWrappedClass().getModifiers()) == false) {

			for (Constructor<?> con: cw.getWrappedClass().getDeclaredConstructors()) {
				for (Class<?> paramType: con.getParameterTypes()) {
					getWrapper(paramType);	//Create wrapper for each param-type
				}
			}
		} 		
	}
	
	

	/**
	 * Add any rule defined by cw to the rule's return type iff
	 * cw is outside the JDK. This implements:
	 *   JCrasher uses for any JDK-defined type (arrays, java.*, sun.*, etc.)
	 *   only predefined values and rules defined outside the JDK for testclasses generation.
	 * 
	 * Store functions,
	 * Store child-of relation,
	 * Queue all reachable types to exhaust search-space
	 * (process non-public methods as well)
	 */
	protected void findRules(ClassWrapper cw) {
		((ClassWrapperImpl) cw).setIsSearched();
		
		if (cw.isLibraryType()) {	//not interested in rules defined by a JDK core type
			return;
		}
		
		queueMethParams(cw);	//add rules defined by cw to rule's return type
		queueFamily(cw);			//queue super, enclosing, and enclosed types
	}
	
	
	
	public void findRules(Set<Class<?>> pClasses) {
		init(pClasses);				//add classes specified by the user

		/* Iteratively extract functions and queue up found referenced types.
		 * Follow all methods--do not restrict to public methods to be crashed.
		 * We want to maximize the number of types we find. */
		boolean maybeMore = true;
		
		while (maybeMore) {		
			maybeMore = false;
			
			for (ClassWrapper wrapper: getWrappers()) {
				ClassWrapperImpl cw = (ClassWrapperImpl) wrapper;
				if (cw.isSearched() == false) {
					//add meths localy and to other (empty) wrappers
					findRules(cw);
					maybeMore = true;
				}
			}
		}			
	}
}
