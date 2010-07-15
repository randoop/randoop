/*
 * AbstractConstructionRules.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.rules;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;


/**
 * AbstractConstructionRules
 * 
 * Hiding the storage of ClassWrappers.
 */
public abstract class AbstractConstructionRules implements ConstructionRules {
	

	/********************************************************************
	 * Class.getName() --> constructing meths, constructors, and children
	 * 
	 * e.g. "java.util.Vector" --> {Vector Z.foo(), Vector(), etc}   
	 */
	protected Hashtable<String,ClassWrapper> class2wrapper = 
		new Hashtable<String,ClassWrapper>();


	/**
	 * Set user-specified types.
	 * 
	 * @param classes types specified by user to be crashed.
	 */
	protected void init(Set<Class<?>> classes) {
		for (Class<?> c: classes) {	//put empty wrapper in mapping
			getWrapper(c);
		}
	}	
	

	/**
	 * @return all class-wrappers
	 */
	public ClassWrapper[] getWrappers() {
		Collection<ClassWrapper> wrappers = class2wrapper.values(); 
		return wrappers.toArray(new ClassWrapper[wrappers.size()]);
	}
		

	
	/**************************************************************************
	 * @return wrapper of class: create & insert if not done yet 
	 */
	public ClassWrapper getWrapper(Class<?> pClass) {
		ClassWrapper wrapper = null;		
		wrapper = class2wrapper.get(pClass.getName());

		if (wrapper==null) {	//create & insert if not done yet
			wrapper = new ClassWrapperImpl(pClass);
			class2wrapper.put(pClass.getName(), wrapper);
		}
		
		return wrapper;
	}
}
