/*
 * CodeWriterImpl.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.writer;

import static edu.gatech.cc.jcrasher.Constants.LS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;

import edu.gatech.cc.jcrasher.JCrasher;
import edu.gatech.cc.jcrasher.plans.blocks.Block;

/**
 * CodeWriterImpl
 * 
 * ADT hiding the creation of java source files
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2005-08-09 moved JUnitAll related methods to JUnitAll.
 * 2004-12-15 fix for Windows paths that contain spaces, thanks to Matt at UWM. 
 * 2002-11-20 added "edu.gatech.edu.gatech.cc.jcrasher.testall.eclipse.runtime.ClassRegistry.resetClasses()" in generated setup() method
 * 2002-10-16 fixed hardcoded ClassWrapperImplTest in suite.addTestSuite(ClassWrapperImplTestn.class)
 */
public class CodeWriterImpl implements CodeWriter {
	
	
	/**
	 * Constructor
	 */
	public CodeWriterImpl() {
		
	}
	
	
	/**
	 * Create file header
	 * 
	 * @param pClass<?> Class<?> for which we generate a junit file
	 * pTName Name of the java class to be generated
	 */	
	protected StringBuffer getHeader(Class<?> pClass) {
		StringBuffer sb = new StringBuffer();
		
		/* Extract p.q from p.q.C */
		StringBuffer pack = new StringBuffer("");	//default package
        
        StringBuffer originalPackage = new StringBuffer("");
        
        if (JCrasher.prependPackage != null) {
            pack.append(JCrasher.prependPackage);
        }
        
		String[] leafNameParts = pClass.getName().split("\\.");
		for (int i=0; i<leafNameParts.length-1; i++) {
            if (i == 0 && JCrasher.prependPackage != null) {
                pack.append(".");
            }
			if (i>0) {
				pack.append(".");
                originalPackage.append(".");
			}
			pack.append(leafNameParts[i]);
            originalPackage.append(leafNameParts[i]);
		}

		if (pack.length() > 0) {
			//package testclasses.package;
			sb.append(
					"package " +pack.toString() +";" +LS);
		}
		else {}	// default package
		
        if (JCrasher.prependPackage != null) {
            sb.append("import " + originalPackage.toString() + ".*;");
        }
        
		return sb;
	}
	
	/* import [..]
	 * public class MyTesteeTestX extends [..] {
	 */
	protected StringBuffer getTestCaseHeader(String pTName) {
		StringBuffer sb = new StringBuffer();
		
		//sb.append(Constants.LS +"import edu.gatech.cc.junit.FilteringTestCase;" +Constants.LS);
		
		//import junit.framework.*;
		//sb.append(Constants.LS +"import junit.framework.*;" +Constants.LS);

		/*
		 * public class CTest extends ReInitializingTestCase {
		 */
		sb.append(																				LS +
				"public class " +pTName +" extends edu.gatech.cc.junit.FilteringTestCase {" +LS);		
		return sb;		
	}


	/*   public MyTestCase(String name) { [..] }
	 * 	 public static void main(String[] args) { [..] }
	 *   public static Test suite() { [..] } 
	 *   protected void setUp() { [..] }
	 *   protected void tearDown() { [..] }
	 */
	protected StringBuffer getServiceMethods(String pTName) {
		StringBuffer sb = new StringBuffer(LS);
		
		//public CTest(String name) {
		sb.append("\t" +"public " +pTName +"(String pName) {" +LS);
		sb.append("\t\tsuper(pName);" +LS +"\t" +"}" +LS);

		//public static void main(String[] args) {
		sb.append(LS+"\t" +"public static void main(String[] args) {" +LS);
		//junit.textui.TestRunner.run(TTest.class); }
		sb.append("\t\tjunit.textui.TestRunner.run(" +pTName+".class);");
		sb.append(LS+"\t" +"}" +LS);

		//public static Test suite() {
		sb.append(LS+"\t" +"public static junit.framework.Test suite() {");
		//return new TestSuite(TTest.class);
		sb.append(LS+"\t\t" +"return new junit.framework.TestSuite(" +pTName +".class);" +LS);
		sb.append("\t}" +LS);		
		
		//protected void setUp() {
		sb.append(LS+"\t/**" +LS);
		sb.append("\t * Executed before each testX()." +LS);
		sb.append("\t * Resets static fields of each user class (except this)." +LS);
		sb.append("\t */" +LS);
		sb.append("\tprotected void setUp() {" +LS);
		sb.append("\t\tedu.gatech.cc.junit.reinit.ClassRegistry.resetClasses();\t//re-initialize static fields of previously loaded classes" +LS);
		sb.append("\t\t//my setUp() code goes here.." +LS);
		sb.append("\t}" +LS);
		
		//protected void tearDown() {
		sb.append(LS+"\t/**" +LS);
		sb.append("\t * Executed after each testX()" +LS);
		sb.append("\t */" +LS);
		sb.append("\tprotected void tearDown() {" +LS);
		sb.append("\t\t//my tearDown() code goes here.." +LS);
		sb.append("\t}" +LS);

		// *** Generated testcases ***/
		sb.append(LS +LS);
		sb.append(LS+"\t/********** Generated testcases **********/");
		return sb;
	}
		
	
//	private StringBuffer getTestCaseMethods (String typeName) {
//		/* classic implementation */
//		StringBuffer sb = new StringBuffer();
//		sb.append(getTestCaseHeader(typeName));
//		sb.append(getServiceMethods(typeName));
//		return sb;
//	}
	
	/* TestCase calls methods under testclasses
	 * @param typeName name of testclasses case class.
	 * @param className name of tested class.
	 * @param methName null indicates multiple tested methods in this testclasses case. */
	protected StringBuffer getTestCaseMethods (String typeName, String className, String methName) {
		StringBuffer sb = new StringBuffer(getTestCaseHeader(typeName));
		
		if (methName!=null) {  //overwrite method declared by FilteringTestCase.
			/*
			 * protected String getNameOfTestedMeth() {
			 * 	return "p.q.r.T.meth";
			 * }
			 */ 
			sb.append(																			 		 LS +"\t"		+
					"protected String getNameOfTestedMeth() {"			+LS +"\t\t" +
						"return \"" +className +"." +methName +"\";" 	+LS +"\t"		+
					"}" 																						+LS);
		}
		
		sb.append(getServiceMethods(typeName));
		return sb;
	}
	
	
	/**
	 * TestCase calls methods under testclasses
	 */
	protected StringBuffer getTestSuiteMethods (String typeName, int nrTestCases) {
		StringBuffer sb = new StringBuffer(LS);
		
		//import junit.framework.*;
		//sb.append("import junit.framework.*;" +LS);

		/* 
		 * public class CTest extends TestCase {
		 */
		sb.append(
				"public class " +typeName +" extends junit.framework.TestCase {" +LS);

		
		/* 
		 * public CTest(String name) {
		 * 	super(name);
		 * }
		 */
		sb.append(																	 LS +"\t" 	+
				"public " +typeName +"(String name) {" 	+LS +"\t\t" +
					"super(name);" 												+LS +"\t" 	+
				"}" 																		+LS );

		
		/* 
		 * public static void main(String[] args) {
		 * 	junit.textui.TestRunner.run(TTest.class);
		 * }
		 */
		sb.append(																			 LS +"\t"		+
				"public static void main(String[] args) {" 	+LS +"\t\t" +
					"junit.textui.TestRunner.run(" +typeName+".class);" +LS +"\t" +
				"}" 																				+LS +LS);

		
		/* 
		 * public static Test suite() {
		 * 	TestSuite suite = new TestSuite();
		 * 	suite.addTestSuite(ClassWrapperImplTest1.class);
		 *  return suite;
		 * }
		 */
		sb.append(																			 		 LS	+"\t" 	+
				"public static junit.framework.Test suite() {" 	+LS +"\t\t" +
					"junit.framework.TestSuite suite = new junit.framework.TestSuite();");
		for (int i=1; i<=nrTestCases; i++) {
			sb.append(																				 LS +"\t\t" +
					"suite.addTestSuite(" +typeName +i +".class);");	
		}
		sb.append(																					 LS	+"\t\t" +
					"return suite;" 															+LS +"\t"		+
				"}" 																						+LS);
		
		return sb;
	}
	

	
	/**
	 * Generate a calling testclasses suite
	 */
	public void generateSuite(Class<?> pClass, int nrTestClasses) {
		
		/* Calling managment class */
		String cName = pClass.getName().substring(pClass.getName().lastIndexOf('.')+1);
		String tName = cName +"Test";
		File mgrFile = CreateFileUtil.createOutFile(pClass, tName);
		try {
			FileWriter outWriter = new FileWriter(mgrFile);
	    outWriter.write(getHeader(pClass).toString());
	    outWriter.write(getTestSuiteMethods(tName, nrTestClasses).toString());
	    outWriter.write(LS +"}");			
	    outWriter.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/* @return <init> for constructor and simple name for methods. */
	protected String getTestedMethName(Block block) {
		if (block.getTestee() instanceof Constructor<?>) {
			return "<init>";
		}
		else {
			return block.getTestee().getName();
		}
	}

	
	/* @return name of meth under testclasses or <init>
	 * if common in blocks, null else. */
	protected String getTestedMethName(Block[] blocks) {
		assert blocks!=null;
		switch (blocks.length) {
			case 0: return null;
			case 1: return getTestedMethName(blocks[0]);
			default:
				String testedMethName = getTestedMethName(blocks[0]);
				for (int i=1; i<blocks.length; i++) {
					if (!getTestedMethName(blocks[i]).equals(testedMethName)) {
						return null;
					}
				}
				return testedMethName;
			}
	}
	
	
	protected FileWriter createTestFile(Class<?> c, int classSeqNr, Block[] blocks) {
		assert c != null;
		assert blocks != null;
		
		/* Infer name of tested method, null if ambigious. */
		String testedMethName = getTestedMethName(blocks);

		/* Determine name of generated class = <className>Test */
		String cName = c.getName().substring(c.getName().lastIndexOf('.')+1);
		String tName = cName +"Test" +classSeqNr;
						
		/* Get handle to new file: open new stream */
		File outFile = CreateFileUtil.createOutFile(c, tName);
		FileWriter outWriter = null;
		try {
			outWriter = new FileWriter(outFile);

	    /* Append (header, testclasses-blocks, footer) */
	    outWriter.write(getHeader(c).toString());
	    outWriter.write(getTestCaseMethods(tName, c.getName(), testedMethName).toString() +LS);
	
			for (int i=0; i<blocks.length; i++) {
				/* 
				 * public void test123() throws Throwable {
				 * 	try {
				 *		P p = new P();
				 *		C.funcUnderTest(p);
				 *	}
				 *	catch (Exception e) {dispatchException(e);} 
				 * 
				 */ 				
				outWriter.write(																	 	 LS +"\t" 	+
						"public void testclasses" +i +"() throws Throwable {"	+LS +"\t\t" +
							"try" +
								blocks[i].toString("\t\t", c)					+LS +"\t\t"	+
                                //"catch (IllegalArgumentException e) { return; }" +
							"catch (Exception e) {dispatchException(e);}" +LS +"\t"		+
						"}"																							+LS
				);
			}	        
		}
		catch(IOException e) {
			e.printStackTrace();
		}		
		return outWriter;
	}
	
	
	
	protected void closeTestFile(FileWriter outWriter) {
		try {
			outWriter.write(LS +"}");
			outWriter.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	protected void closeTestFile(FileWriter outWriter, String comment) {
		try {
			outWriter.write(LS +comment);
			outWriter.write("}");
			outWriter.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Writes a series pBlocks for pClass<?> of testcases to a new java file.
	 * TestCase ::= MethodName Block
	 * 
	 * Additionaly create/ update calling testclasses suite
	 */
	public void generateTestFile(Class<?> c, int classSeqNr, Block[] blocks) {
		FileWriter outWriter = createTestFile(c, classSeqNr, blocks);
		closeTestFile(outWriter);
	}
	
	

	public void generateTestFile(
			Class<?> c,
			int classSeqNr,
			Block[] blocks,
			String comment)
	{
		FileWriter outWriter = createTestFile(c, classSeqNr, blocks);
		closeTestFile(outWriter, comment);
	}

}
