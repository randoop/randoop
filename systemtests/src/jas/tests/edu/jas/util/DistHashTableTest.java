/*
 * $Id: DistHashTableTest.java 1263 2007-07-29 10:21:40Z kredel $
 */

//package edu.unima.ky.parallel;
package edu.jas.util;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;


/**
 * DistHashTable test with JUnit.
 * @author Heinz Kredel
 */
public class DistHashTableTest extends TestCase {

    /**
     * main.
     */
    public static void main (String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run( suite() );
    }


    /**
     * Constructs a <CODE>DistHashTableTest</CODE> object.
     * @param name String.
     */
    public DistHashTableTest(String name) {
        super(name);
    }


    /**
     * suite.
     * @return a test suite.
     */
    public static Test suite() {
        TestSuite suite= new TestSuite(DistHashTableTest.class);
        return suite;
    }

    private static final String host = "localhost";

    private DistHashTable l1;
    private DistHashTable l2;
    private DistHashTable l3;

    private DistHashTableServer dls;

    int rl = 7; 
    int kl = 10;
    int ll = 10;
    int el = 5;
    float q = 0.5f;

    protected void setUp() {
        dls = new DistHashTableServer();
        dls.init();
    }

    protected void tearDown() {
        dls.terminate();
        dls = null;
        if ( l1 != null ) l1.terminate();
        if ( l2 != null ) l2.terminate();
        if ( l3 != null ) l3.terminate();
        l1 = l2 = l3 = null;
    }


    /**
     * Tests create and terminate DistHashTableServer.
     */
    public void testDistHashTable0() {
    }


    /**
     * Tests create and terminate DistHashTable.
     */
    public void testDistHashTable1() {
        l1 = new DistHashTable(host);
        assertTrue("l1==empty",l1.isEmpty());
    }


    /**
     * Tests if the created DistHashTable has #n objects as content.
     */
    public void testDistHashTable2() {
        l1 = new DistHashTable(host);
        assertTrue("l1==empty",l1.isEmpty());

        l1.putWait( new Integer(1), new Integer(1) );
        assertFalse("l1!=empty",l1.isEmpty());
        assertTrue("#l1==1", l1.size() == 1 );
        l1.putWait( new Integer(2), new Integer(2) );
        assertTrue("#l1==2", l1.size() == 2 );
        l1.putWait( new Integer(3), new Integer(3) );
        assertTrue("#l1==3", l1.size() == 3 );

        Iterator it = null;
        it = l1.iterator();
        int i = 0;
        while ( it.hasNext() ) {
            Object k = it.next();
            Object o = l1.get(k);
            Integer x = new Integer( ++i );
            assertEquals("l1(i)==v(i)", x, o );
            assertEquals("l1(i)==k(i)", x, k );
        }

        l1.clear();
        assertTrue("#l1==0", l1.size() == 0 );
    }


    /**
     * Tests if the two created DistHashTables have #n objects as content.
     */
    public void testDistHashTable3() {
        l2 = new DistHashTable(host);
        assertTrue("l2==empty",l2.isEmpty());
        l1 = new DistHashTable(host);
        assertTrue("l1==empty",l1.isEmpty());

        int i = 0, loops = 10;
        while ( i < loops ) {
            Integer x = new Integer( ++i );
            l1.putWait( x, x );
            assertTrue("#l1==i", l1.size() == i );
        }
        assertTrue("#l1=="+loops, l1.size() == loops );

        while ( l2.size() < loops ) {
            try {
                //System.out.print("*2");
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        assertTrue("#l2=="+loops, l2.size() == loops );
        Iterator it = null;
        it = l2.iterator();
        i = 0;
        while ( it.hasNext() ) {
            Object k = it.next();
            Object o = l2.get(k);
            Integer x = new Integer( ++i );
            //System.out.println("o = " + o + " x = "+ x);
            assertEquals("l2(i)==k(i)", x, k );
            assertEquals("l2(i)==v(i)", x, o );
        }
    }


    /**
     * Tests if the three created DistHashTables have #n objects as content.
     */
    public void testDistHashTable4() {
        l1 = new DistHashTable(host);
        assertTrue("l1==empty",l1.isEmpty());
        l2 = new DistHashTable(host);
        assertTrue("l2==empty",l2.isEmpty());
        l3 = new DistHashTable(host);
        assertTrue("l3==empty",l3.isEmpty());

        int i = 0, loops = 10;
        while ( i < loops ) {
            Integer x = new Integer( ++i );
            l3.putWait( x, x );
            assertTrue("#l3==i", l3.size() == i );
        }
        assertTrue("#l3=="+loops, l3.size() == loops );

        while ( l2.size() < loops || l1.size() < loops-1 ) {
            try {
                //System.out.print("*3");
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        assertTrue("#l2=="+loops, l2.size() == loops );
        assertTrue("#l1=="+loops, l1.size() == loops );
        Iterator it = null;
        it = l2.iterator();
        Iterator it3 = null;
        it3 = l1.iterator();
        i = 0;
        while ( it.hasNext() && it3.hasNext() ) {
            Object k1 = it.next();
            Object k2 = it3.next();
            Object v1 = l2.get(k1);
            Object v2 = l1.get(k2);
            Integer x = new Integer( ++i );
            //System.out.println("o = " + o + " x = "+ x);
            assertEquals("l2(i)==k(i)", x, k1 );
            assertEquals("l1(i)==k(i)", x, k2 );
            assertEquals("l2(i)==v(i)", x, v1 );
            assertEquals("l1(i)==v(i)", x, v2 );
        }
    }


    /**
     * Tests if the two created DistHashTables have #n objects as content 
     * when one is created later.
     */
    public void testDistHashTable5() {
        l1 = new DistHashTable(host);
        assertTrue("l1==empty",l1.isEmpty());

        int i = 0, loops = 10;
        while ( i < loops ) {
            Integer x = new Integer( ++i );
            l1.putWait( x, x );
            assertTrue("#l1==i", l1.size() == i );
        }
        assertTrue("#l1=="+loops, l1.size() == loops );

        l2 = new DistHashTable(host);
        // assertTrue("l2==empty",l2.isEmpty());
        while ( l2.size() < loops ) {
            try {
                //System.out.print("*2");
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        Iterator it = null;
        it = l2.iterator();
        i = 0;
        while ( it.hasNext() ) {
            Object k = it.next();
            Object v = l2.get(k);
            Integer x = new Integer( ++i );
            //System.out.println("o = " + o + " x = "+ x);
            assertEquals("l2(i)==k(i)", x, k );
            assertEquals("l2(i)==v(i)", x, v );
        }
        assertTrue("#l2=="+loops, l2.size() == loops );
    }


    /**
     * Tests if the two created DistHashTables have #n objects as content 
     * using getWait() when one is created later.
     */
    public void testDistHashTable6() {
        l1 = new DistHashTable(host);
        assertTrue("l1==empty",l1.isEmpty());

        int i = 0, loops = 10;
        while ( i < loops ) {
            Integer x = new Integer( ++i );
            l1.putWait( x, x );
            assertTrue("#l1==i", l1.size() == i );
        }
        assertTrue("#l1=="+loops, l1.size() == loops );

        l2 = new DistHashTable(host);

        Iterator it = null;
        it = l1.iterator();
        i = 0;
        while ( it.hasNext() ) {
            Object k = it.next();
            Object v = l2.getWait(k);
            Integer x = new Integer( ++i );
            //System.out.println("o = " + o + " x = "+ x);
            assertEquals("l1(i)==k(i)", x, k );
            assertEquals("l2(i)==v(i)", x, v );
        }
        assertTrue("#l2=="+loops, l2.size() == loops );
    }

}
