/*
 * CreateFileUtil.java
 * 
 * Copyright 2005 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.writer;

import java.io.File;
import java.net.URLDecoder;

import java.io.UnsupportedEncodingException;

import edu.gatech.cc.jcrasher.Constants;
import edu.gatech.cc.jcrasher.JCrasher;


public class CreateFileUtil {

	
	/* 
	 * package/hierarchy
	 */
	protected static String getPackagePath(Class<?> pClass) {
		assert pClass!=null;
		StringBuffer sb = new StringBuffer("");	//default package
		
        if (JCrasher.prependPackage != null) {
            sb.append(JCrasher.prependPackage);
        }
        
		/* Extract p.q from p.q.C */
		String[] leafNameParts = pClass.getName().split("\\.");
		for (int i=0; i<leafNameParts.length-1; i++) {
            if (i == 0 && JCrasher.prependPackage != null) {
                sb.append("/");
            }
			if (i>0) {
				sb.append("/");
			}
			sb.append(leafNameParts[i]);
		}
		return sb.toString();		
	}	
	
	
	/*
	 * @return null iff class is in a jar; path to binary directory
	 * /c:/my/project/bin else.
	 */
	protected static String getBinRoot(Class<?> pClass) {
		assert pClass!=null;
		
		/* package/hierarchy/T.class */
		String classLocRel = pClass.getName().replace('.', '/') +".class";
		
		/* determine location of loaded class file on disk */ 
		ClassLoader cl = pClass.getClassLoader();
		if (cl==null) {  //null --> bootstrap cl --> JDK class.
			cl = ClassLoader.getSystemClassLoader();	//cl that has loaded JCrasher
		}
		String classLocation;
		try {
			classLocation = URLDecoder.decode(cl.getResource(classLocRel).getPath(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}
		
		/* check whether class is in a jar file */
		String extension = classLocation.substring(classLocation.indexOf('.')); 
		if (extension.startsWith(".jar!")) { //matches("(.*)jar!(.*)")
			return null;
		}
		else {
			/* /c:/my/project/bin/package/hierarchy/T.class */
			return classLocation.substring(0, classLocation.length()-classLocRel.length());	
		}				
	}
	
	

	/**
	 * Get absolute path root of directory the junit-file will be stored in,
	 * trying the options in order.
	 * 1. User-specified output directory.
	 * 2. Same directory as binary.
	 * 3. In current working directory.
	 * 
	 * @return /c:/my/project/bin
	 */
	public static String getTestRoot(Class<?> pClass) {
		assert pClass != null;
		String testRoot = null;  											// /c:/my/project/testclasses-root/
		
		if (Constants.OUT_DIR != null) {
			testRoot = Constants.OUT_DIR.getPath();
		}
		else {
			testRoot = getBinRoot(pClass);
			if (testRoot==null) {  //found the class packaged in a jar.
				testRoot = System.getProperty("user.dir");  //current working dir.
			}
		}
		assert testRoot!=null;
		return testRoot;
	}	
	
	
	/**
	 * @param pathAbsolute location in file system
	 * @param typeName name of the java class to be generated
	 * @return handle to /<pathAbsolute>/<typeName>.java
	 */
	public static File createOutFile(String pathAbsolute, String typeName) {
		assert pathAbsolute != null;
		assert typeName != null;
		
		/* TODO: check if file already created --> change own name to avoid replacement */
		File res = new File(pathAbsolute +"/" +typeName +".java");
		
		/* Create sub-dir structure */
		if (res.getParentFile().exists() == false) {
			try {
				res.getParentFile().mkdirs();
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		assert res != null;
		return res;		
	}



	/**
	 * Create a new file handle for the JUnit Java source file to be generated.
	 * 
	 * @param typeName Name of the java class to be generated
	 * @param pClass<?> class used to determine location in file system,
	 * @return handle to /abspath/<package>.<Class<?>>Test.java
	 */
	public static File createOutFile(Class<?> pClass, String typeName) {
		assert pClass != null;
		assert typeName != null;
		
		String pathAbsolute = CreateFileUtil.getTestRoot(pClass) +"/" +getPackagePath(pClass);
		return createOutFile(pathAbsolute, typeName);
	}
}
