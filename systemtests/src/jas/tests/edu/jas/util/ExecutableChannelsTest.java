/*
 * $Id: ExecutableChannelsTest.java 1263 2007-07-29 10:21:40Z kredel $
 */

package edu.jas.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

//import edu.unima.ky.parallel.ChannelFactory;


/**
 * ExecutableChannels tests with JUnit.
 * @author Heinz Kredel
 */

public class ExecutableChannelsTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>ExecutableChannelsTest</CODE> object.
 * @param name String.
 */
   public ExecutableChannelsTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(ExecutableChannelsTest.class);
     return suite;
   }

   private static final String host = "localhost";
   private static final int port = ChannelFactory.DEFAULT_PORT;
   private static final String mfile = ExecutableChannels.DEFAULT_MFILE;

   private ExecutableChannels ec;
   private ExecutableServer es;

   protected void setUp() {
       ec = null;
       es = new ExecutableServer(port);
       es.init();
   }

   protected void tearDown() {
       es.terminate();
       es = null;
       if ( ec != null ) {
          ec.close();
          ec = null;
       }
   }


/**
 * Tests if the ExecutableChannels could be initialized with null.
 */
 public void testExecutableChannels1() {
     String[] servers = null;
     ec = new ExecutableChannels(servers);
     assertEquals("numServers<0", -1, ec.numServers() );
     assertEquals("numChannels<0", -1, ec.numChannels() );
     String ts = "" + ec;
     assertEquals("toString", "ExecutableChannels()", ts );
   }


/**
 * Tests if the ExecutableChannels could be initialized with small server array.
 */
 public void testExecutableChannels2() {
     int nums = 1;
     String[] servers = new String[nums];
     for ( int i = 0; i < nums; i++ ) {
         servers[i] = host + ":" + port;
     }
     ec = new ExecutableChannels(servers);
     assertEquals("numServers==1", 1, ec.numServers() );
     assertEquals("numChannels<0", -1, ec.numChannels() );
     String ts = "" + ec;
     assertEquals("toString", "ExecutableChannels("+host+":"+port+")", ts );
   }


/**
 * Tests if the ExecutableChannels could be initialized with big server array.
 */
 public void testExecutableChannels3() {
     int nums = 100;
     String[] servers = new String[nums];
     for ( int i = 0; i < nums; i++ ) {
         servers[i] = host + ":" + port;
     }
     ec = new ExecutableChannels(servers);
     assertEquals("numServers==100", 100, ec.numServers() );
     assertEquals("numChannels<0", -1, ec.numChannels() );
     String ts = "" + ec;
     int l = "ExecutableChannels()".length() + nums * servers[0].length() + nums-1;
     assertEquals("toString.length()", l, ts.length());
   }


/**
 * Tests if the ExecutableChannels could be initialized and opened.
 */
 public void testExecutableChannels4() {
     int nums = 2;
     int numc = nums - 1;
     String[] servers = new String[nums];
     for ( int i = 0; i < nums; i++ ) {
         servers[i] = host + ":" + port;
     }
     ec = new ExecutableChannels(servers);
     assertEquals("numServers==2", nums, ec.numServers() );
     assertEquals("numChannels<0", -1, ec.numChannels() );
     String ts = "" + ec;
     int l = "ExecutableChannels()".length() + nums * servers[0].length() + nums-1;
     assertEquals("toString.length()", l, ts.length());
     try {
         ec.open();
         assertEquals("numServers==1", nums, ec.numServers() );
         assertEquals("numServers==numChannels", numc, ec.numChannels() );
     } catch (IOException e) {
         fail("open()"+e);
     }
     ec.close();
     assertEquals("numChannels<0", -1, ec.numChannels() );
   }


/**
 * Tests if 11 ExecutableChannels could be initialized and opened.
 */
 public void testExecutableChannels5() {
     int nums = 11; // max 11 by some limit on number of threads
     int numc = nums - 1;
     String[] servers = new String[nums];
     for ( int i = 0; i < nums; i++ ) {
         servers[i] = host + ":" + port;
     }
     ec = new ExecutableChannels(servers);
     assertEquals("numServers==1", nums, ec.numServers() );
     assertEquals("numChannels<0", -1, ec.numChannels() );
     String ts = "" + ec;
     int l = "ExecutableChannels()".length() + nums * servers[0].length() + nums-1;
     assertEquals("toString.length()", l, ts.length());
     try {
         ec.open();
         assertEquals("numServers==1", nums, ec.numServers() );
         assertEquals("numServers==numChannels", numc, ec.numChannels() );
     } catch (IOException e) {
         fail("open()"+e);
     }
     ec.close();
     assertEquals("numChannels<0", -1, ec.numChannels() );
   }


/**
 * Tests if 10 ExecutableChannels to 1 server could be initialized and opened.
 */
 public void testExecutableChannels6() {
     int nums = 2; // max 11 by some limit on number of threads
     int numc = 10; // max 11 by some limit on number of threads
     String[] servers = new String[nums];
     for ( int i = 0; i < nums; i++ ) {
         servers[i] = host + ":" + port;
     }
     ec = new ExecutableChannels(servers);
     assertEquals("numServers==1", nums, ec.numServers() );
     assertEquals("numChannels<0", -1, ec.numChannels() );
     try {
         ec.open(numc);
         assertEquals("numServers==1", nums, ec.numServers() );
         assertEquals("numServers==numChannels", numc, ec.numChannels() );
     } catch (IOException e) {
         fail("open()"+e);
     }
     ec.close();
     assertEquals("numChannels<0", -1, ec.numChannels() );
   }


/**
 * Tests if 5 ExecutableChannels to 10 servers could be initialized and opened.
 */
 public void testExecutableChannels7() {
     int nums = 10; // max 11 by some limit on number of threads
     int numc = 5; // max 11 by some limit on number of threads
     String[] servers = new String[nums];
     for ( int i = 0; i < nums; i++ ) {
         servers[i] = host + ":" + port;
     }
     ec = new ExecutableChannels(servers);
     assertEquals("numServers==1", nums, ec.numServers() );
     assertEquals("numChannels<0", -1, ec.numChannels() );
     try {
         ec.open(numc);
         assertEquals("numServers==1", nums, ec.numServers() );
         assertEquals("numServers==numChannels", numc, ec.numChannels() );
     } catch (IOException e) {
         fail("open()"+e);
     }
     ec.close();
     assertEquals("numChannels<0", -1, ec.numChannels() );
   }


/**
 * Tests if the ExecutableChannels could be initialized with servers from file.
 */
 public void testExecutableChannels8() {
     try {
         ec = new ExecutableChannels( mfile );
     } catch (FileNotFoundException e) {
         fail("readfile()"+e);
     }
     assertEquals("numServers==4", 4, ec.numServers() );
     assertEquals("numChannels<0", -1, ec.numChannels() );
     int numc = ec.numServers();
     try {
         ec.open(numc);
         assertEquals("numServers==numChannels", numc, ec.numChannels() );
     } catch (IOException e) {
         fail("open()"+e);
     }
     ec.close();
     assertEquals("numChannels<0", -1, ec.numChannels() );
   }


/**
 * Tests if the ExecutableChannels could be initialized with servers from file.
 */
 public void testExecutableChannels9() {
     try {
         ec = new ExecutableChannels( mfile );
     } catch (FileNotFoundException e) {
         fail("readfile()"+e);
     }
     assertEquals("numServers==4", 4, ec.numServers() );
     assertEquals("numChannels<0", -1, ec.numChannels() );
     int numc = 10;
     try {
         ec.open(numc);
         assertEquals("numServers==numChannels", numc, ec.numChannels() );
     } catch (IOException e) {
         fail("open()"+e);
     }
     ec.close();
     assertEquals("numChannels<0", -1, ec.numChannels() );
   }


}
