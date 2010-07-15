/*
 * JUnitAllImpl.java
 * 
 * Copyright 2005 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.gatech.cc.jcrasher.Constants;
import edu.gatech.cc.jcrasher.JCrasher;

public class JUnitAllImpl implements JUnitAll {

	protected FileWriter fw = null;  //file not yet created in file system
	
	
	/**
	 * Create JUnitAll.java that calls all testclasses suites to be generated.
	 * @param testRoot directory in which JUnitAll.java should be created.
	 */
	public void create(String testRoot) {
		if (fw!=null) {return;}	//Already created.
		assert testRoot!=null;
		
		File f = CreateFileUtil.createOutFile(testRoot, "JUnitAll");  //default package
		try {
			fw = new FileWriter(f);
	    fw.write(junitAllHeader);	//contents up until calls to testclasses suites.
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Create JUnitAll.java that calls all testclasses suites to be generated.
	 * @param c place JUnitAll.java in directory from which c was loaded
	 * in case no out-directory has been set explicitly.
	 */
	public void create(Class<?> c) {
		if (fw!=null) {return;}	//Already created.
		assert c!=null;
		create(CreateFileUtil.getTestRoot(c));
	}
	
	
	/**
	 * Append invocation of testclasses suite.
	 * 
	 * @param test has to have testX() methods, which JUnit automatically
	 * adds to the TestSuite
	 */
	public void addTestSuite(String test) {
		assert fw!=null;
		assert test!=null;
		
		try {
	    fw.write("\t\tsuite.addTestSuite("
                + (JCrasher.prependPackage != null ? JCrasher.prependPackage + "." : "")
                +test +".class);"+Constants.LS);
		}
		catch(IOException e) {
			e.printStackTrace();
		}				
	}	
	
	
		
	/**
	 * Append invocation of testclasses suite.
	 * 
	 * @param test must have a suite() method that returns a JUnit Test.
	 * @see junit.runner.BaseTestRunner#getTest(java.lang.String) 
	 */
	public void addTest(String test) {
		assert fw!=null;
		assert test!=null;
		
		try {
	    fw.write("\t\tsuite.addTest("
                 + (JCrasher.prependPackage != null ? JCrasher.prependPackage + "." : "")
                +test +".suite());"+Constants.LS);
		}
		catch(IOException e) {
			e.printStackTrace();
		}		
	}	
	
	
	
	/**
	 * Append invocation of testclasses suite for testee c.
	 */
	public void addTest(Class<?> c) {
		assert fw!=null;
		assert c!=null;
		addTest(c.getName() +"Test");
	}	

	
	
	/**
	 * Write closing code of JUnitAll.java.
	 */
	public void finish() {
		if (fw==null) {return;}  //nothing to finish
		
		try {
	    fw.write(junitAllFooter);
	    fw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}		
	}

}
