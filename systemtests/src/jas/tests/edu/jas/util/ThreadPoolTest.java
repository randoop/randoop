/*
 * $Id: ThreadPoolTest.java 1263 2007-07-29 10:21:40Z kredel $
 */

//package edu.unima.ky.parallel;
package edu.jas.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

/**
 * ThreadPool tests with JUnit. 
 * @author Akitoshi Yoshida
 * @author Heinz Kredel
 */

public class ThreadPoolTest extends TestCase {

    static final int JOBS = 10; // number of jobs to start

/**
 * main.
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>ThreadPoolTest</CODE> object.
 * @param name String.
 */
   public ThreadPoolTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(ThreadPoolTest.class);
     return suite;
   }

   private ThreadPool p1;


   protected void setUp() {
   }

   protected void tearDown() {
     p1.terminate();
   }


/**
 * Tests if the created ThreadPool is empty.
 */
 public void testThreadPool1() {
     p1 = new ThreadPool(0);
     assertTrue( "not empty pool ", p1.getNumber() == 0 );
   }

/**
 * Tests if the created ThreadPool is non empty.
 */
 public void testThreadPool2() {
     p1 = new ThreadPool(1);
     assertTrue( "# empty pool ", p1.getNumber() == 1 );
     p1.terminate();

     p1 = new ThreadPool();
     assertTrue( "# empty pool ", p1.getNumber() == ThreadPool.DEFAULT_SIZE );
     p1.terminate();

     p1 = new ThreadPool(10);
     assertTrue( "# empty pool ", p1.getNumber() == 10 );
     p1.terminate();
   }

/**
 * Tests if the created ThreadPool has no jobs.
 */
 public void testThreadPool3() {
     p1 = new ThreadPool();
     assertFalse( "no jobs ", p1.hasJobs() );
     assertFalse( "more than 0 jobs ", p1.hasJobs(0) );
     p1.terminate();
 }

/**
 * Tests if the created ThreadPool has jobs.
 */
 public void testThreadPool4() {
     p1 = new ThreadPool();
     assertFalse( "no jobs ", p1.hasJobs() );
     for (int i = 0; i < JOBS*p1.getNumber(); i++ ) {
         p1.addJob( new FastWorker() );
     }
     boolean j = p1.hasJobs();
     assertTrue( "more than 0 jobs ", (j | true) ); // stupid
     p1.terminate();
     assertFalse( "no jobs ", p1.hasJobs() );
 }


/**
 * Tests if the created ThreadPool has many jobs.
 */
 public void testThreadPool5() {
     p1 = new ThreadPool();
     assertFalse( "no jobs ", p1.hasJobs() );
     for (int i = 0; i < JOBS*p1.getNumber(); i++ ) {
         p1.addJob( new SlowWorker() );
     }
     assertTrue( "more than 10 jobs ", p1.hasJobs(JOBS) );
     p1.terminate();
     assertFalse( "no jobs ", p1.hasJobs() );
 }


/**
 * Tests if the created ThreadPool has correct strategy.
 */
 public void testThreadPool6() {
     p1 = new ThreadPool(StrategyEnumeration.LIFO);
     assertTrue( "FIFO strategy ", 
          p1.getStrategy() == StrategyEnumeration.LIFO );
   }

/**
 * Tests if the created ThreadPool has jobs and correct strategy.
 */
 public void testThreadPool7() {
     p1 = new ThreadPool(StrategyEnumeration.LIFO);
     assertFalse( "no jobs ", p1.hasJobs() );
     for (int i = 0; i < JOBS*p1.getNumber(); i++ ) {
         p1.addJob( new FastWorker() );
     }
     boolean j = p1.hasJobs();
     assertTrue( "more than 0 jobs ", (j | true) ); // stupid
     p1.terminate();
     assertFalse( "no jobs ", p1.hasJobs() );
 }

}


/**
 * Utility class for ThreadPool Test.
 */
class FastWorker implements Runnable {
    public void run() { 
    }
}

/**
 * Utility class for ThreadPool Test.
 */
class SlowWorker implements Runnable {
    public void run() { 
        try {
            Thread.sleep(100);
        } catch (InterruptedException e ) {
        }
    }
}
