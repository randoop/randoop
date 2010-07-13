/*
 * Constants.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher;

import java.io.File;

/**
 * 
 * Constants, project-wide
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner 
 * 2002-07-24 1,000,000 looking for exhaustive search up to chaining depth of three
 * 2002-07-25 1,000 looking to calm javac and eclipse ;-)
 * 2002-07-27 changed from interface to allow JCrasher to set max plan recursion
 * 	set MAX_NR_PLANS_PER_TYPE and MAX_NR_TEST_CASES_PER_FCT
 *     to max(Int) to let search be only bounded by depth
 * 2003-11-19 Added list of types that should not be created by method chaining.
 */
public abstract class Constants {	
	
	public enum PlanFilter {
		ALL,						//null, new A(null), ..
		NON_NULL,				//new A(null), ..
		NON_NULL_TRANS	//..
	}	
	
	public enum Visibility {
		GLOBAL,	//All members visible from anywhere = public (default)
		PACKAGE	//All members visible inside the same package
	}	

	public enum Verbose {
		DEFAULT,	//do not print any rules
		VERBOSE,	//print rules used for creating tests
		ALL				//print entire type-space
	}
	public static Verbose VERBOSE_LEVEL = Verbose.DEFAULT;	
	
	
	public final static String[] LIBRARY_TYPES = new String[] {	//library packages
		"java.", 
		"javax.",
		"sun.",
		"com.sun.",
		"org.apache.",
		"org.ietf.",
		"org.omg.",
		"org.w3c.",
		"sunw."
	};

	/**
	 * How deep to search/ plan for each type?
	 */
	public final static int MAX_PLAN_RECURSION_DEFAULT = 3;
	
	/**
	 * To be exclusively set at-most-once by JCrasher on startup:
	 * TO BE USED AS A CONSTANT
	 */
	public static int MAX_PLAN_RECURSION = MAX_PLAN_RECURSION_DEFAULT;

	/**
	 * How many testclasses methods per generated testclasses class:
	 * 1000 and junitMultiCL will generate OutOfMemoryError
	 */
	public final static int MAX_NR_TEST_METHS_PER_CLASS = 500;

	/**
	 * How many testclasses classes should be generated max?
	 * 300 classes * 1 min/ class = 300 min = 5h * 60min/ h
	 */
	public final static int MAX_NR_TEST_CLASSES = 300;
	
	/**
	 * How many blocks per constructor or method under testclasses?
	 */
	public final static int MAX_NR_TEST_CASES_PER_FCT = Integer.MAX_VALUE;
	//1000;

	/**
	 * How many recursive chaining plans max per type?
	 */
	public final static int MAX_NR_PLANS_PER_TYPE = Integer.MAX_VALUE;
	//5000;

			

	/**
	 * Command line parameters
	 */
	public static final String OPTION_DEPTH = "depth";
	public static final String OPTION_HELP = "help";
	public static final String OPTION_OUTDIR = "outdir";
	public static final String OPTION_VERBOSE = "verbose";
	public static final String OPTION_VERBOSEALL = "verboseAll";	
	
	
	/**
	 * Platform dependent text control
	 */
 	public final static String LS = System.getProperty("line.separator");
	public final static String FS = System.getProperty("file.separator");
	public final static String PS = System.getProperty("path.separator");

	
	public static File OUT_DIR = null;
	
	/**
	 * @return null is included by this filter 
	 */
	public static boolean isNullIncluded(PlanFilter f) {
		return PlanFilter.ALL.equals(f);
	}
	
	public static PlanFilter removeNull(PlanFilter f) {
		if (PlanFilter.ALL.equals(f)) {return PlanFilter.NON_NULL;}
		return f;
	}
	
	public static PlanFilter addNull(PlanFilter f) {
		if (PlanFilter.NON_NULL.equals(f)) {return PlanFilter.ALL;}
		return f;
	}	
}
