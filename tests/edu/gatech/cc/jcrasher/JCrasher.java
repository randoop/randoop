/*
 * JCrasher.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher;

import static edu.gatech.cc.jcrasher.Constants.FS;
import static edu.gatech.cc.jcrasher.Constants.MAX_PLAN_RECURSION;
import static edu.gatech.cc.jcrasher.Constants.OPTION_DEPTH;
import static edu.gatech.cc.jcrasher.Constants.OPTION_HELP;
import static edu.gatech.cc.jcrasher.Constants.OPTION_OUTDIR;
import static edu.gatech.cc.jcrasher.Constants.OPTION_VERBOSE;
import static edu.gatech.cc.jcrasher.Constants.OUT_DIR;
import static edu.gatech.cc.jcrasher.Constants.PS;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import randoop.Globals;


/**
 * JCrasher
 * 
 * Main routine of application
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-07-27 pass in maximal depth of constructor chaining
 */
public class JCrasher {
	
	protected static final String usage =
		"Usage: java edu.gatech.cc.jcrasher.JCrasher OPTION* (CLASS|PACKAGE)+" + Globals.lineSep + "" + 
		"Generate JUnit testclasses case sources for every CLASS and all classes within" + Globals.lineSep + "" +
		"every PACKAGE and their sub-packages." + Globals.lineSep + "" +
		"Example: java edu.gatech.cc.jcrasher.JCrasher p1.C p2" + Globals.lineSep + "" + Globals.lineSep + "" +
		
		"  -d, --"+OPTION_DEPTH+"=INT   maximal depth of method chaining (default 3)" + Globals.lineSep + "" +
		"  -h, --"+OPTION_HELP+"        print these instructions" + Globals.lineSep + "" +
		"  -o, --"+OPTION_OUTDIR+"=DIR  where JCrasher writes testclasses case sources to (default .)" + Globals.lineSep + "" +
		"      --"+OPTION_VERBOSE+"     print the rules used for creating testclasses cases"
	;

	protected static String name = "JCrasher";	
	protected static String hint =
		"Try `java edu.gatech.cc.jcrasher.JCrasher --"+OPTION_HELP+"' for more information."
	;

    // Carlos's hack for added options: store them as globals.
    public static String prependPackage = null;
    private static boolean publicOnly = false;

    public static boolean jcrasherSupressTestsThatUseNull = false;
	
	/**
	 * Program name n and hint h will be printed in case
	 * a parsing error occurs. 
	 */
	public static void setNameAndHint(String n, String h) {
		assert n!=null && h!=null;
		name = n;
		hint = h;
	}
	
	/*
	 * Print out cause of termination, hint and terminate
	 */
	protected static void die(String cause) {
  	System.err.println(
  			name +": " +cause +Globals.lineSep +
				hint +Globals.lineSep);
  	System.exit(0);
	}

	/*
	 * Print out hint and terminate
	 */
	protected static void die() {
  	System.err.println(hint +Globals.lineSep);
  	System.exit(0);
	}	
	
	
	/**
	 * Load all classes from the jar file that are in one of the
	 * defined packages or their sub-packages.
	 * 
	 * @param packages ::= (package name)*
	 */
	protected static Set<Class<?>> loadFromJar(String jarName, Set<String> packages) {
		assert jarName!=null;
		assert packages!=null;
		Set<Class<?>> res = new HashSet<Class<?>>();
		
		try {
			Enumeration<JarEntry> entries = (new JarFile(jarName)).entries();			
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (!entry.getName().endsWith(".class")) {  
					continue;  //ignore entries that are not class files
				}
				String entryName = entry.getName().replace('/','.').replace('\\','.');
				entryName = entryName.substring(0, entryName.length()-6);  //remove .class suffix
				
				for (String pack: packages) {
					if (entryName.startsWith(pack+".")) {
						try {
							res.add(Class.forName(entryName));
						}
						catch (Throwable t) {}  //ignore misnamed class
					}
				}
			}
		}
		catch (Exception e) {}	//ignore unusable classpath element
		return res;
	}


	
	/**
	 * Load all classes from directory dir.
	 * 
	 * @param pack name of package represented by dir
	 */
	protected static Set<Class<?>> loadFromDir(File dir, String pack) {
		assert dir!=null && dir.exists();
		
		Set<Class<?>> res = new HashSet<Class<?>>();
		
		File[] elems = dir.listFiles();
		for (File elem: elems) {
			
			if (elem.getName().endsWith(".class")) {	//class file
				String cName = elem.getName().replace(PS, ".");
				cName = cName.substring(0,cName.length()-6); 
				try {
					res.add(Class.forName(pack+"."+cName));
				}
				catch (Throwable e) {}	//ignore misnamed class
			}
			
			if (elem.isDirectory()) {	//recurse
				res.addAll(loadFromDir(elem, pack+"."+elem.getName()));
			}
		}
		
		return res;
	}
	
	
	
	/**
	 * Load all classes from the directory that are in one of the
	 * defined packages or their sub-packages.
	 * 
	 * userSpec ::= (package name)+
	 */
	protected static Set<Class<?>> loadFromDir(String dirName, Set<String> packages) {
		assert dirName!=null;
		assert packages!=null && packages.size()>0;
		
		Set<Class<?>> res = new HashSet<Class<?>>();
		
		for (String pack: packages) {
			File dir = new File(dirName+FS+pack.replace(".",FS));
			if (!dir.exists()) {
				continue;
			}
			/* Load all class files in dir and its sub-dirs */
			res.addAll(loadFromDir(dir, pack));
		}
		return res;
	}	
	
	
	
	/**
	 * Load all classes found on the classpath that match userSpec.
	 * 
	 * @param userSpecs ::= (class name | package name)+
	 * package name means that the user wants to load all classes
	 * found in this package and all its sub-packages.
	 */
	protected static Set<Class<?>> parseClasses(String[] userSpecs) {
		Set<Class<?>> res = new HashSet<Class<?>>();	//avoid multiple entires of same class

		/* Load all classes specified by the user directly */
		Set<String> packageSpecs = new HashSet<String>();	//avoid multiple entires
		for (String userSpec: userSpecs) {
			try {
                Class<?> cls = Class.forName(userSpec);
                // Carlos: tests for private classes will not compile.
                if (Modifier.isPrivate(cls.getModifiers()))
                    continue;
                if (JCrasher.publicOnly && !Modifier.isPublic(cls.getModifiers()))
                    continue;
                res.add(cls);
			}
			catch (Exception e) {	//Could not be loaded as a class
				packageSpecs.add(userSpec);
			}
		}
		
		if (packageSpecs.size()==0) {	//Could load all elements of user spec
			return res;
		}
		
		
		
		/* Find all classes that match the user's package specs */
		String[] cpEntries = System.getProperty("java.class.path").split(PS);
		for (String cpElement: cpEntries) {
			if (cpElement.endsWith(".jar")) {	//Load classes from Jar
				res.addAll(loadFromJar(cpElement, packageSpecs));
				continue;
			}
			//Load classes from directory
			res.addAll(loadFromDir(cpElement, packageSpecs));
		}
		
		return res;
	}
	

	
	/** 
	 * set Constants.MAX_PLAN_RECURSION according to user param 
	 */
	protected static void parseDepth(String arg) {
		int maxDepth = 0;
		try {
			maxDepth = Integer.parseInt(arg);
		}
		catch(NumberFormatException e) {
			die(arg +" must be greater than zero");
		}
		
		if (maxDepth <= 0) {
			die(arg +" must be greater than zero");
		}
		else {
			MAX_PLAN_RECURSION = maxDepth;
		}	  		
	}
	
	
	/**
	 * set OUT_DIR according to user param
	 */
	public static void parseOutDir(String arg) {
		OUT_DIR = new File(arg);
		if (OUT_DIR.isDirectory()==false) {
			die(arg +" is not a directory.");
		}
	}
	
	
	/* 
	 * Parse command line parameters using GNU GetOpt 
	 */
	protected static Set<Class<?>> parse(String[] args){
        
        throw new RuntimeException("Currently broken. See does not import LongOpt.");
//        
//		LongOpt[] longopts = new LongOpt[]{
//				new LongOpt(OPTION_DEPTH, LongOpt.REQUIRED_ARGUMENT, null, 'd'),
//				new LongOpt(OPTION_HELP, LongOpt.NO_ARGUMENT, null, 'h'),
//	   		new LongOpt(OPTION_OUTDIR, LongOpt.REQUIRED_ARGUMENT, null, 'o'),
//				new LongOpt(OPTION_VERBOSE, LongOpt.NO_ARGUMENT, null, 0),
//				new LongOpt(OPTION_VERBOSEALL, LongOpt.NO_ARGUMENT, null, 0)
//	  };
//	  Getopt g = new Getopt("JCrasher", args, "d:ho:", longopts);
//	  int opt = -1;
//	  while ((opt = g.getopt()) != -1) {
//	  	switch (opt) {
//	  	
//	      case 0 :	//option exclusively in long format
//	        String option = longopts[g.getLongind()].getName();
//	
//	        if (OPTION_VERBOSE.equals(option)) {	//--verbose
//	        	VERBOSE_LEVEL = Verbose.VERBOSE;
//	        	break;
//	        }
//
//	        if (OPTION_VERBOSEALL.equals(option)) {	//--verboseAll
//	        	VERBOSE_LEVEL = Verbose.ALL;
//	        	break;
//	        }	        
//	        
//	        die("getopt() returned " +opt);  //should not happen.
//	  		  	
//	  	
//	  		case 'd':  //--depth .. maximum nesting depth.
//	  			parseDepth(g.getOptarg());
//	  			break;
//	      
//	      case 'o':  //--outdir .. write testclasses sources to.
//	      	parseOutDir(g.getOptarg());
//	      	break;
//
//	      case 'h':  //--help .. print usage instructions.
//	      	System.out.println(usage);
//	      	System.exit(0);
//	      
//	      case '?': die();
//	      
//	      default : die("getopt() returned " +opt);  //should not happen.
//	  	}
//	  }
//	  
//	  if (g.getOptind() >= args.length) {  //no class specified
//	  	die("no class specified");
//	  }
//	  
//		String[] classPackFromUser = new String[args.length-g.getOptind()];
//		System.arraycopy(args, g.getOptind(), classPackFromUser, 0, classPackFromUser.length);
//	  return parseClasses(classPackFromUser);
	}



	
	/*************************************************************************
	 * Main - called via jvm if started as an application
	 */
	public static void main(String[] args) {
				
        // Carlos: capture my added options:
        // -prependpackage:NAME option
        //     Generated tests will have package name starting with "NAME"
        // -publiconly
        //     Will only create tests for public classes.
        //     This option works only for classes passed explicitly by class name
        //     (not for directories or jar files passed).
        List<String> args2 = new ArrayList<String>();
        for (String s : args) {
            if (s.startsWith("-prependpackage:")) {
                String packageToPrepend = s.substring("-prependpackage:".length());
                if (packageToPrepend.equals("")) {
                    throw new RuntimeException("package to prepend must have at least one character.");
                }
                JCrasher.prependPackage = packageToPrepend;
            } else if (s.startsWith("-publiconly")) {
                JCrasher.publicOnly = true;
            } else if (s.equals("-suppressnull")) {
                JCrasher.jcrasherSupressTestsThatUseNull = true;
            } else {
                args2.add(s);
            }
        }
        
        
		/* Test planning testtime measurement. */
		long startTime= System.currentTimeMillis();

		/* Load classes of given name with system class-loader */
		Set<Class<?>> classes = parse(args2.toArray(new String[0]));
			
		/* Crash loaded class */
		if (classes!=null && classes.size()>0) {
			Crasher crasher = new CrasherImpl();
			crasher.crashClasses(classes);
		}
		
		/* Test planning testtime measurement. */
		long endTime= System.currentTimeMillis();
		long runTime= endTime-startTime;
		System.out.println("Run testtime: " +runTime +" ms.");
		//System.out.println(";" +runTime);		//for structured logging.
	}
}
