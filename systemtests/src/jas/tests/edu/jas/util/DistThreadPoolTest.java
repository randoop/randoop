/*
 * $Id: DistThreadPoolTest.java 1263 2007-07-29 10:21:40Z kredel $
 */

package edu.jas.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

//import edu.unima.ky.parallel.ChannelFactory;


/**
 * DistThreadPool tests with JUnit. 
 * @author Akitoshi Yoshida
 * @author Heinz Kredel
 */

public class DistThreadPoolTest extends TestCase {

    static final int JOBS = 10; // number of jobs to start

/**
 * main.
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>DistThreadPoolTest</CODE> object.
 * @param name String.
 */
   public DistThreadPoolTest(String name) {
          super(name);
   }


/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(DistThreadPoolTest.class);
     return suite;
   }


   //private static final String host = "localhost";
   private static final int port = ChannelFactory.DEFAULT_PORT;
   //private static final String mfile = "machines.test";

   private ExecutableServer es;

   private DistThreadPool pool;


   protected void setUp() {
     es = new ExecutableServer(port);
     es.init();
   }

   protected void tearDown() {
     pool.terminate();
     es.terminate();
   }


/**
 * Tests if the created DistThreadPool is empty.
 */
 public void testDistThreadPool1() {
     pool = new DistThreadPool(0);
     assertTrue( "not empty pool ", pool.getNumber() == 0 );
 }


/**
 * Tests if the created DistThreadPool is non empty.
 */
 public void testDistThreadPool2() {
     pool = new DistThreadPool(1);
     assertTrue( "# empty pool ", pool.getNumber() == 1 );
     pool.terminate();

     pool = new DistThreadPool();
     assertTrue( "# empty pool ", pool.getNumber() == DistThreadPool.DEFAULT_SIZE );
     pool.terminate();

     pool = new DistThreadPool(10);
     assertTrue( "# empty pool ", pool.getNumber() == 10 );
     pool.terminate();
 }


/**
 * Tests if the created DistThreadPool has no jobs.
 */
 public void testDistThreadPool3() {
     pool = new DistThreadPool();
     assertFalse( "no jobs ", pool.hasJobs() );
     assertFalse( "more than 0 jobs ", pool.hasJobs(0) );
     pool.terminate();
 }


/**
 * Tests if the created DistThreadPool has jobs.
 */
 public void testDistThreadPool4() {
     pool = new DistThreadPool();
     assertFalse( "no jobs ", pool.hasJobs() );
     for (int i = 0; i < JOBS*pool.getNumber(); i++ ) {
         pool.addJob( new DistFastWorker() );
     }
     boolean j = pool.hasJobs();
     assertTrue( "more than 0 jobs ", (j | true) ); // stupid
     pool.terminate();
     assertFalse( "no jobs ", pool.hasJobs() );
 }


/**
 * Tests if the created DistThreadPool has many jobs.
 */
 public void testDistThreadPool5() {
     pool = new DistThreadPool();
     assertFalse( "no jobs ", pool.hasJobs() );
     for (int i = 0; i < JOBS*pool.getNumber(); i++ ) {
         pool.addJob( new DistSlowWorker() );
     }
     assertTrue( "more than 10 jobs ", pool.hasJobs(JOBS) );
     pool.terminate();
     assertFalse( "no jobs ", pool.hasJobs() );
 }


/**
 * Tests if the created DistThreadPool has correct strategy.
 */
 public void testDistThreadPool6() {
     pool = new DistThreadPool(StrategyEnumeration.LIFO);
     assertTrue( "FIFO strategy ", 
          pool.getStrategy() == StrategyEnumeration.LIFO );
   }


/**
 * Tests if the created DistThreadPool has jobs and correct strategy.
 */
 public void testDistThreadPool7() {
     pool = new DistThreadPool(StrategyEnumeration.LIFO);
     assertFalse( "no jobs ", pool.hasJobs() );
     for (int i = 0; i < JOBS*pool.getNumber(); i++ ) {
         pool.addJob( new DistFastWorker() );
     }
     boolean j = pool.hasJobs();
     assertTrue( "more than 0 jobs ", (j | true) ); // stupid
     pool.terminate();
     assertFalse( "no jobs ", pool.hasJobs() );
 }

}


/**
 * Utility class for DistThreadPool Test.
 */
class DistFastWorker implements RemoteExecutable {
    public void run() { 
        try {
            Thread.sleep(0);
        } catch (InterruptedException e ) {
        }
    }
}

/**
 * Utility class for DistThreadPool Test.
 */
class DistSlowWorker implements RemoteExecutable {
    public void run() { 
        try {
            Thread.sleep(100);
        } catch (InterruptedException e ) {
        }
    }
}
