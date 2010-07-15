/*
 * ConstructionRules.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.rules;

import java.util.Set;

/**
 * ConstructionRules
 *
 * Stores the relation, filled and queried by CrasherImpl 
 */
public interface ConstructionRules {

	
	/**
	 * For each passed [abstract] class and interface do:
	 *      add wrapper to mapping;
	 *      queue all reachable types like meth-param, enclosed classes;
	 *      create/ sign in to parent-classwrapper;
	 *      distribute declared constr/ meths to wrapper of return types;
	 * 
	 * While (new wrapper added, which are potentially not yet processed) do:
	 *      Process added wrappers as above.
	 */
	public void findRules(Set<Class<?>> pClasses);
	

	/**
	 * @return wrapper of class: fresh one created & inserted if not done yet 
	 */
	public ClassWrapper getWrapper(Class<?> pClass);
	

	/**
	 * @return all class-wrappers
	 */
	public ClassWrapper[] getWrappers();
}
