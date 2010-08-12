/*
 * CrasherTypesForConstruction.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import edu.gatech.cc.jcrasher.planner.Planner;
import edu.gatech.cc.jcrasher.planner.PlannerImpl;
import edu.gatech.cc.jcrasher.plans.blocks.Block;
import edu.gatech.cc.jcrasher.rules.ConstructionRules;
import edu.gatech.cc.jcrasher.rules.ConstructionRulesImpl;
import edu.gatech.cc.jcrasher.writer.CodeWriter;
import edu.gatech.cc.jcrasher.writer.CodeWriterImpl;
import edu.gatech.cc.jcrasher.writer.JUnitAll;
import edu.gatech.cc.jcrasher.writer.JUnitAllImpl;

/**
 *
 * - Build up the "needs-type-for-construction" relation
 * - Build up plans thru type-space upto max. recursion depth
 * - Distribute nr testclasses classes among the classes under testclasses
 * - Come up with random sample over each class's plan space
 *
 * CS 8903 Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-05-17 last changed 
 */
public class CrasherImpl implements Crasher {
	
	/**
	 * Database holding the relation needed for planning how to obtain
	 * an object via combinations of functions in type-space
	 */
	protected ConstructionRules rules = new ConstructionRulesImpl();
	
	/**
	 * Planner finds plans how to get values of a type and stores them with
	 * type in rules database.
	 */
	protected Planner planner = null;
	
	/**
	 * CodeWriter transform a sequence of testcases into a java output file
	 */
	protected CodeWriter writer = new CodeWriterImpl();

	
	public CrasherImpl() {
		planner = new PlannerImpl(rules);
	}

	
	/*
	 * @return array 0, 1, 2, .., spaceSzie-1
	 */
	 private int[] getExhaustiveEnumeration(int spaceSize) {
	 	int[] res = new int[spaceSize];
	 	for (int i=0; i<spaceSize; i++) {
	 		res[i] = i;
	 	}
	 	return res;
	 }
	 

	/*
	 * @return sorted array of nrIndices "random" integers ,
	 * 	values 0..spaceSize-1, no duplicates
	 */
	private int[] getRandomIndices(int spaceSize, int nrIndices) {
		//TODO Why do we need a TreeMap here?
		
		//generate random indices until desired amount reached = one random sample, no duplicate indices
		TreeMap<Integer,Object> indexMap = new TreeMap<Integer,Object>();
		while (indexMap.size() < nrIndices) {	//re-insert Integer as key
			indexMap.put(new Integer((int) (Math.random() * spaceSize)), null);
		}
			
		//convert hash-list of indices to int-array
		int[] res = new int[indexMap.size()];
		Iterator<Integer> indexIterator = indexMap.keySet().iterator();
		for (int i=0; i<res.length; i++) {
			res[i] = indexIterator.next().intValue();
		}
		
		return res;
	}
	

	/**
	 * Main programs hands over list of classes to crash provided by user.
	 * - Discover the relation and print it to screen.
	 * - Plan to set max. depth.
	 */
	public void crashClasses(Set<Class<?>> classSet) {
		assert classSet!=null && classSet.size()>0;		

		/* Find rules how to (transitively) construct user-specified types:
		 * A(B(C()), -1) and only A user-specified */
		rules.findRules(classSet);
		
		/* 
		 * Determine size of each class's plan-space --> sum = total space
		 * 
		 * Switch from sets to arrays:
		 * We keep plan-info in different arrays, which are all
		 * indexed by the index into the classes array.
		 * 
		 * TODO Refactor to keep the additional info as ClassWrapper fields
		 * so we do no longer need these arrays. 
		 */
		Class<?>[] classes = classSet.toArray(new Class<?>[classSet.size()]);
		int[] planSpaceSizes = new int[classes.length];
		int totalPlanSpaceSize = 0;
		for (int i=0; i<classes.length; i++) {
			planSpaceSizes[i] = planner.getPlanSpaceSize(classes[i]);
			totalPlanSpaceSize += planSpaceSizes[i];
		}
		
		planner.flush();		//print rules used for creating tests		

		/* 
		 * Determine nrSamples per class 
		 */
		int[] nrSamples = new int[classes.length];
		
		/* "Exhaustive" testclasses iff nrPlans < nrTestMethods */
		double methsPerPlan = 1.0;	//how many testclasses-methods do we spend on a plan?
		
		/* We limit ourselves to fewer testclasses-methods iff nrPlans >= nrTestMethods */
		int maxNrTestMeths = 
			Constants.MAX_NR_TEST_CLASSES * Constants.MAX_NR_TEST_METHS_PER_CLASS;
		if (totalPlanSpaceSize > maxNrTestMeths) {			
			methsPerPlan = (double)maxNrTestMeths / (double)totalPlanSpaceSize;
		}
			
		/* Assign each class (at least) as many testclasses-methods as it has plans */
		for (int i=0; i<classes.length; i++) {
			double methsPerClass = Constants.MAX_NR_TEST_METHS_PER_CLASS;
			nrSamples[i] = (int) 
				((planSpaceSizes[i] + methsPerClass/2) / 	//add half of max meth per class to emulate rounding
				methsPerClass* methsPerPlan);
			if (nrSamples[i] == 0) {
				nrSamples[i] += 1;
			}
		}
			
		writeTestCases(classes, nrSamples, planSpaceSizes);
	}

	
	
	/**
	 * Write all testclasses cases as JUnit testclasses files to disk:
	 * 
	 * JUnitAll
	 *   ATest
	 *     ATest1
	 *     ATest2
	 *     ...
	 *   BTest
	 *     BTest1
	 *     ...
	 *   ...
	 *   
	 * @param classes to be crashed
	 */
	protected void writeTestCases(
			Class<?>[] classes,
			int[] nrSamples,
			int[] planSpaceSizes)
	{
		/*
		 * JUnitAll.java
	 	 */
		JUnitAll junitAll = new JUnitAllImpl();
		if (Constants.OUT_DIR!=null) {
			try {	//create JUnitAll.java in user-specified out directory
				junitAll.create(Constants.OUT_DIR.getCanonicalPath());
			}
			catch(IOException e) {}
		}
		junitAll.create(classes[0]);	//create where we loaded testee class from
		

		/*
		 * Generate testclasses classes.
		 * Each testclasses class is a random sample over the class's plan space.
	 	 */
		for (int i=0; i<classes.length; i++) {
			
			int nrIndices = Constants.MAX_NR_TEST_METHS_PER_CLASS * nrSamples[i];
			int[] sample = null;

			//more methods than plans in space?
			if (nrIndices >= planSpaceSizes[i]) sample = getExhaustiveEnumeration(planSpaceSizes[i]);
			else	sample = getRandomIndices(planSpaceSizes[i], nrIndices);
			assert sample.length == nrIndices;
			printStatistics(classes[i], sample.length, planSpaceSizes[i]);
			
			/* 
			 * split result into junks of max 500 for each testclasses class
			 */
			int sliceCount = 0;
			int arrayCursor = 0;	//next block to be retrieved and printed

			while (arrayCursor < sample.length) {
				sliceCount += 1;
				
				/* Retrieve next block of up to max nr testclasses/class as defined by sample */
				Block[] blocks = planner.getTestBlocks(
						classes[i],
						sample,	//TODO sample should be per method
						arrayCursor);
				writer.generateTestFile(classes[i], sliceCount, blocks);
				arrayCursor += blocks.length;
			}
			writer.generateSuite(classes[i], sliceCount);	//create a management class
			junitAll.addTest(classes[i]);
		}
		junitAll.finish();		
	}
	

	
	/**
	 * ClassToBeCrashed: 5 of 100 tests created
	 */
	protected void printStatistics(Class<?> c, int nrTests, int planSpaceSize) {
		//Used following report style for initial JCrasher randoop.experiments
		//class-under-testclasses; plan-depth; #tests; #planspace
//		System.out.print(
//			";"+c.getName()+
//			";"+Constants.MAX_PLAN_RECURSION+
//			";"+nrTests+
//			";"+planSpaceSize
//		);			
		System.out.println(
				c.getName()+": "+nrTests+" of "+planSpaceSize+" tests created"
		);
	}	
}
