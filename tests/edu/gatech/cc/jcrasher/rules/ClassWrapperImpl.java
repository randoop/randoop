/*
 * ClassWrapperImpl.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.rules;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import edu.gatech.cc.jcrasher.Constants;
import edu.gatech.cc.jcrasher.Constants.PlanFilter;
import edu.gatech.cc.jcrasher.Constants.Visibility;
import edu.gatech.cc.jcrasher.plans.NullPlan;
import edu.gatech.cc.jcrasher.plans.Plan;
import edu.gatech.cc.jcrasher.plans.PresetPlans;

/**
 * ClassWrapperImpl
 *
 * Each class or interface X has exactly one ClassWrapper XW.
 * - A global mapping: Class<?> --> ClassWrapper holds all tupels (X, XW)*
 * - ClassWrapper knows all preset valus of its wrapped type and all
 * 	found methods or constructors, which return the wrapped type:
 * 
 *   1. Constructors return an object of its type: X X(P*)
 *   2. Methods can return objects of some type: X Z.foo(P*)
 *   3. Both of 1. and 2. for each implementing/ extending class or interface
 * 
 * CS 8903 Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-05-17 outsourced rules relation to ConstructionRules
 * 2002-05-23 integrated nested classes and constructor-omission for simple type
 * 2002-08-01 moved planning to plans.compute
 */
public class ClassWrapperImpl implements ClassWrapper {

	protected Class<?> wrappedClass = null;  //Wrapped Class<?> object

	/**
	 * Methods declared anywhere as public, non-abstract returning the wrapped type
	 */
	protected final List<Method> constrPublicMeth = new Vector<Method>();


	/**
	 * Some preset plans, e.g., {0, 1, -1}.
	 * NullPlan is never included.
	 */
	protected final List<Plan> presetPlans = new Vector<Plan>();
	
		
	/**
	 * Child-classes of X: Class.getName() --> Class
	 * Holds all tupels (name, class) where:
	 * (XImpl implements X) or (Y extends X)
	 */
	protected final Hashtable<String,Class<?>> children = new Hashtable<String,Class<?>>();	
	
	
	/**
	 * Indicates if info of wrapped class has already been extracted and
	 * disseminated to this and other wrappers
	 */
	protected boolean isSearched = false;
		

	/**
	 * Indicates if wrapped class is part of needed-for-construction-relation
	 * of the passed classes. true iff
	 * - parameter, enclosing instance of any function that constructs any class
	 *   of interest.
	 * - child of a class of interest (due to subtype-polymorphism).
	 * 
	 * Only for a class of interest (= having this flag on) will all
	 * ways to cunstruct it be printed
	 */
	protected boolean isNeeded = false;	


	
	/**
	 * Constructor
	 */
	public ClassWrapperImpl(Class<?> pClass) {
		assert pClass != null;
		assert presetPlans.isEmpty();
		
		wrappedClass = pClass;
		presetPlans.addAll(Arrays.asList(PresetPlans.getPreset(pClass)));
	}

	

	public boolean isLibraryType() {
		assert wrappedClass != null;
		
		if (wrappedClass.isPrimitive() || wrappedClass.isArray()) {
			return true;
		}
		
		for (int i=0; i<Constants.LIBRARY_TYPES.length; i++) {
			if (wrappedClass.getName().indexOf(Constants.LIBRARY_TYPES[i])>=0) {
				/* Changed startsWith to (indexOf >= 0) 
				 * to suppress arrays and inner types as well. */
				return true;
			}
		}
		
		return false;
	}
	
	
	
	/**
	 * Indicates if wrapped class is part of needed-for-construction-relation
	 * of the passed classes. true iff
	 * - parameter, enclosing instance of any function that constructs any class
	 *   of interest.
	 * - child of a class of interest (due to subtype-polymorphism).
	 * 
	 * Only for a class of interest (= having this flag on) will all
	 * ways to cunstruct it be printed
	 */
	public boolean isNeeded() {
		return isNeeded;
	}
	
	public void setIsNeeded() {
		isNeeded = true;
	}

	
	/**
	 * @return whether the wrapped class was searched for rules
	 */
	protected boolean isSearched() {
		return isSearched;
	}

	protected void setIsSearched() {
		this.isSearched = true;
	}


	/**
	 * @return true iff representing an innner class
	 */
	public boolean isInnerClass() {
		assert wrappedClass != null;
		boolean res = false;
		
		Class<?> enclosedBy = wrappedClass.getDeclaringClass();
		if ((enclosedBy != null) //nested
			&& (Modifier.isStatic(wrappedClass.getModifiers()) == false)) {
				
			res = true;
		}
		
		return res;
	}	
	
	
	/**
	 * @return the wrapped class
	 */
	public Class<?> getWrappedClass() {
		return wrappedClass;
	}

	
		
	/**
	 * Gather all methods defined anywhere as public, returning the wrapped type
	 * Get known public non-abstract methods, which return an object x.
	 * 
	 * Note that all y with "Y implements/ extends X" are valid, too and can
	 * be obtained by calling this method on classwrappers representing these
	 * child-classes.
	 * 
	 * @return each public method X Z.foo(P*) or an empty list.
	 */
	public List<Method> getConMethsVisGlobal() {
		assert constrPublicMeth != null;
		return constrPublicMeth;
	}

	
	/**
	 * Get constructors of this class of param. visibiility, if non-abstract.
	 * 
	 * @param visibility domain from which constructor needs to be visible
	 *        as defined in edu.gatech.edu.gatech.cc.jcrasher.testall.eclipse.Constants (same package vs. globally)
	 * @return each constuctor X(P*) according to pVisibility 
	 *         or an empty list if abstract class.
	 */
	protected List<Constructor<?>> getConstrs(Visibility visibility) {		
		List<Constructor<?>> res = new Vector<Constructor<?>>();
		
		Constructor<?>[] constructors = wrappedClass.getDeclaredConstructors();

		/* Only for non-abstract classes */
		if (Modifier.isAbstract(wrappedClass.getModifiers()) == false) {
			
			for (Constructor<?> con: constructors) {  //Filter for visibility
				
				/* public-public */
				if ((Visibility.GLOBAL.equals(visibility))
					&& (Modifier.isPublic(con.getModifiers())==true)
					&& (Modifier.isPublic(con.getDeclaringClass().getModifiers())==true))
				{			
					res.add(con);
				}
				
				/* protected, default, public */
				if ((Visibility.PACKAGE.equals(visibility))
					&& (Modifier.isPrivate(con.getModifiers())==false))
				{	//TODO visibility from package x to package y
					res.add(con);
				}
			}			
		}
		
		assert res != null;
		return res;
	}
	

	/**
	 * Get all known public constructors of this class, if non-abstract.
	 * 
	 * @return each public constuctor X(P*) or an empty list if abstract class.
	 */
	public List<Constructor<?>> getConstrsVisGlobal() {
		return getConstrs(Visibility.GLOBAL);		
	}
	

	/**
	 * Get user-predefined standard representative plans like 0, 1, -1, null
	 * 
	 * @param planFilter can exclude null for reference types
	 * @return List of preset plans of wrapped type (= userdefined database).
	 */
	public List<Plan> getPresetPlans(PlanFilter planFilter) {
		if (wrappedClass.isPrimitive()) {	//no null for primitive
			return presetPlans;
		}
		
		/* Reference type */
		if (!Constants.isNullIncluded(planFilter)) {  //null not desired
			return presetPlans;			
		}
		List<Plan> withNull = new Vector<Plan>(presetPlans);  //null desired
		withNull.add(new NullPlan(wrappedClass));
		return withNull;
	}

	
	
	/**
	 * Get all implementing or extending child-classes.
	 * 
	 * @return all classes S with (S implements X) or (S extends X)
	 *         or an empty list.
	 */
	public List<Class<?>> getChildren() {
		List<Class<?>> res = new Vector<Class<?>>();
		for (Enumeration<Class<?>> e = children.elements(); e.hasMoreElements() ;) {
        	res.add(e.nextElement());
		}
		return res;
	}
	

	
	/**
	 * Add a non-abstract constructing method, group by following categories:
	 * - public --> of interest to any testing code
	 * - protected/ default, declared inside package p
	 *     --> of interest to testing code only if in same package
	 * - private
	 *     --> of interest to testing code only if accessiblity checks of
	 *           compilers are circumvented via reflection
	 * 
	 * @param pMeth method to be added
	 */
	protected void addConstrMeth(Method pMeth) {
		assert pMeth != null;
		assert Modifier.isAbstract(pMeth.getModifiers())==false;
		
		/* Public-public */
		if ((Modifier.isPublic(pMeth.getModifiers())==true) 
			&& (Modifier.isPublic(pMeth.getDeclaringClass().getModifiers())==true)) {
			constrPublicMeth.add(pMeth);
		}
		
		/* Protected */
		
		/* Private */
	}
	
	
	/**
	 * Add implementing/ extending child
	 */
	protected void addChild(Class<?> pClass) {
		children.put(pClass.getName(), pClass);
	}
	
	/**
	 * @return canonical name of wrapped class
	 */
	@Override
    public String toString() {
		return wrappedClass.getCanonicalName();
	}
}
