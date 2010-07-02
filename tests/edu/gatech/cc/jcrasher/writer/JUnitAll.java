/*
 * JUnitAll.java
 * 
 * Copyright 2005 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.writer;

import edu.gatech.cc.jcrasher.Constants;

public interface JUnitAll {
	
	public final static String junitAllHeader = 
		"import junit.framework.*;" +Constants.LS +
		Constants.LS +
		"/**" +Constants.LS +
		" * TestSuite that collects all testcases genereated for all JUnit classes." +Constants.LS +
		" */" +Constants.LS +
		"public class JUnitAll extends TestSuite {" +Constants.LS +
		Constants.LS +
		"\tpublic JUnitAll(String pName) {" +Constants.LS +
		"\t\tsuper(pName);" +Constants.LS +
		"\t}" +Constants.LS +
		Constants.LS +
		"\tpublic static Test suite() {" +Constants.LS +
		"\t\tTestSuite suite = new TestSuite();" +Constants.LS;
	
	public final static String junitAllFooter = 
		"\t\treturn suite;" +Constants.LS +
		"\t}" +Constants.LS +
		"}" +Constants.LS;
	
	/**
	 * Create a new JUnitAll.java that calls all testclasses suites to be generated.
	 * @param testRoot directory in which JUnitAll.java should be created.
	 */
	public void create(String testRoot);
	
	/**
	 * Create a new JUnitAll.java that calls all testclasses suites to be generated.
	 * @param c place JUnitAll.java in directory from which c was loaded
	 * in case no out-directory has been set explicitly.
	 */
	public void create(Class<?> c);	
	
	/**
	 * Append invocation of testclasses suite for testee c.
	 */
	public void addTest(Class<?> c);		
	
	
	/**
	 * Append invocation of testclasses suite.
	 */
	public void addTest(String test);	
	
	
	/**
	 * Append invocation of testclasses suite.
	 * 
	 * @param test has to have testX() methods, which JUnit automatically
	 * adds to the TestSuite
	 */
	public void addTestSuite(String test);
	
	
	/**
	 * Write closing code of JUnitAll.java.
	 */
	public void finish();		
}
