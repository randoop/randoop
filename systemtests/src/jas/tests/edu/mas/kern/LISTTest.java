/*
 * $Id: LISTTest.java 1899 2008-07-12 14:04:08Z kredel $
 */

package edu.mas.kern;


import edu.jas.arith.BigRational;

import static edu.mas.kern.LIST.*;

import java.util.List;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Basic list processing tests with JUnit.
 * @author Heinz Kredel.
 */

public class LISTTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>LISTTest</CODE> object.
 * @param name String.
 */
   public LISTTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(LISTTest.class);
     return suite;
   }


   boolean timing =false;

   protected void setUp() {
       //a = b = c = d = e = null;
   }

   protected void tearDown() {
       //a = b = c = d = e = null;
   }


/**
 * Test static initialization LIST.
 */
 public void testLISTinit() {
     LIST<Object> a = null;
     assertTrue("a == () ", isEmpty(a) );
     assertEquals("len(a) == 0 ", LENGTH(a), 0 );
     a = new LIST<Object>();
     assertTrue("a == () ", isEmpty(a) );
     assertEquals("len(a) == 0 ", LENGTH(a), 0 );
     //System.out.println("a = " + a);
     a = new LIST<Object>( a.list );
     assertTrue("a == () ", isEmpty(a) );
     assertEquals("len(a) == 0 ", LENGTH(a), 0 );
 }


/**
 * Test static LIST creation.
 */
 public void testLISTcreate() {
     Object five = 5;
     LIST<Object> a = null;
     assertTrue("a == () ", isEmpty(a) );
     assertEquals("len(a) == 0 ", LENGTH(a), 0 );
     //System.out.println("a = " + a);
     a = LIST1( five );
     assertFalse("a != () ", isEmpty(a) );
     assertEquals("len(a) == 1 ", LENGTH(a), 1 );
     //System.out.println("a = " + a);
     a = COMP( five, a );
     assertFalse("a != () ", isEmpty(a) );
     assertEquals("len(a) == 2 ", LENGTH(a), 2 );
     //System.out.println("a = " + a);

     LIST<Object> b = LIST2(five,five);
     assertFalse("b != () ", isEmpty(b) );
     assertEquals("len(a) == 2 ", LENGTH(a), 2 );
     assertTrue("a == b ", EQUAL(a,b) );
 }


/**
 * Test static LIST operations.
 */
 public void testLISToper() {
     Object five = 5;
     LIST<Object> a = LIST1( five );
     assertFalse("a != () ", isEmpty(a) );
     assertEquals("len(a) == 1 ", LENGTH(a), 1 );
     a = COMP( five, a );
     assertFalse("a != () ", isEmpty(a) );
     assertEquals("len(a) == 2 ", LENGTH(a), 2 );

     LIST<Object> b = CINV(a);
     assertFalse("b != () ", isEmpty(b) );
     assertEquals("len(a) == 2 ", LENGTH(a), 2 );
     assertTrue("a == b ", EQUAL(a,b) );

     LIST<Object> c = INV(a);
     assertFalse("c != () ", isEmpty(c) );
     assertEquals("len(c) == 2 ", LENGTH(c), 2 );
     assertTrue("a == c ", EQUAL(a,c) );
     //System.out.println("a = " + a);
     //System.out.println("c = " + c);
 }


/**
 * Test static LIST many elements.
 */
 public void testLISTelems() {
     int max = 100;
     Object n;
     LIST<Object> a = null;
     for ( int i = 0; i < max; i++ ) {
         n = i;
         a = COMP( n, a );
     }
     assertFalse("a != () ", isEmpty(a) );
     assertEquals("len(a) == "+max+" ", LENGTH(a), max );
     //System.out.println("a = " + a);

     LIST<Object> b = CINV(a);
     assertFalse("b != () ", isEmpty(b) );
     assertEquals("len(b) == "+max+" ", LENGTH(b), max );
     //System.out.println("b = " + b);

     b = INV( b );
     assertFalse("b != () ", isEmpty(b) );
     assertEquals("len(b) == "+max+" ", LENGTH(b), max );
     //System.out.println("b = " + b);
     assertTrue("a == INV(CINV(a)) ", EQUAL(a,b) );
 }


/**
 * Test static LIST destructive operations.
 */
 public void testLISTdestruct() {
     Object n = 5; 
     LIST<Object> a = LIST1( n );
     LIST<Object> b = LIST1( n );
     assertEquals("len(a) == 1 ", LENGTH(a), 1 );
     assertEquals("len(b) == 1 ", LENGTH(b), 1 );
     SRED(a,b);
     assertFalse("a != () ", isEmpty(a) );
     assertFalse("b != () ", isEmpty(b) );
     assertEquals("len(b) == 1 ", LENGTH(b), 1 );
     assertEquals("len(a) == 2 ", LENGTH(a), 2 );
     //System.out.println("a = " + a);

     n = 7;
     SFIRST(a,n);
     //System.out.println("a = " + a);
     assertEquals("len(a) == 2 ", LENGTH(a), 2 );
     LIST<Object> c = COMP( n, b );
     assertEquals("len(c) == 2 ", LENGTH(c), 2 );
     assertTrue("a == c ", EQUAL(a,c) );
 }


/**
 * Test static LIST recursive structures.
 */
 public void testLISTrecursive() {
     Object n = 5; 
     LIST<Object> a = LIST1( n );
     LIST<LIST<Object>> b = LIST2( a, a );
     LIST<LIST<LIST<Object>>> c = LIST3( b, b, b );
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     //System.out.println("c = " + c);
     assertEquals("len(a) == 1 ", LENGTH(a), 1 );
     assertEquals("len(b) == 2 ", LENGTH(b), 2 );
     assertEquals("len(c) == 3 ", LENGTH(c), 3 );

     //System.out.println("EXTENT(a) = " + EXTENT(a));
     //System.out.println("EXTENT(b) = " + EXTENT(b));
     //System.out.println("EXTENT(c) = " + EXTENT(c));
     assertEquals("EXTENT(a) == 1 ", EXTENT(a), 1 );
     assertEquals("EXTENT(b) == 2 ", EXTENT(b), 2 );
     assertEquals("EXTENT(c) == 6 ", EXTENT(c), 6 );

     //System.out.println("ORDER(a) = " + ORDER(a));
     //System.out.println("ORDER(b) = " + ORDER(b));
     //System.out.println("ORDER(c) = " + ORDER(c));
     assertEquals("ORDER(a) == 1 ", ORDER(a), 1 );
     assertEquals("ORDER(b) == 2 ", ORDER(b), 2 );
     assertEquals("ORDER(c) == 3 ", ORDER(c), 3 );
 }


/**
 * Test static LIST rational content.
 */
 public void testLISTcontent() {
     int max = 5000;
     long t0, t1;
     BigRational cf = new BigRational(2,3);
     BigRational n; 
     LIST<BigRational> a = null;
     t0 = System.currentTimeMillis();
     for ( int i = 0; i < max; i++ ) {
         n = cf.random(5); 
         a = COMP( n, a );
     }
     t1 = System.currentTimeMillis();
     if ( timing ) System.out.println("t.comp = " + (t1-t0));
     //System.out.println("a = " + LENGTH(a));
     assertEquals("len(a) == "+max+" ", LENGTH(a), max );

     List<BigRational> b = new ArrayList<BigRational>();
     /* is/was inefficient */
     LIST<BigRational> ap = a;
     t0 = System.currentTimeMillis();
     while ( ! isEmpty( ap ) ) {
         n = FIRST(ap); ap = RED(ap); 
         b.add( n );
         //System.out.println("n = " + n);
     }
     t1 = System.currentTimeMillis();
     if ( timing ) System.out.println("t.red  = " + (t1-t0));
     //System.out.println("b = " + b.size());
     assertEquals("size(b) == "+max+" ", b.size(), max );

     b = new ArrayList<BigRational>();
     int len = LENGTH( a );
     t0 = System.currentTimeMillis();
     for ( int i = 0; i < len; i++ ) {
         n = LELT(a,i);
         b.add( n );
     }
     t1 = System.currentTimeMillis();
     if ( timing ) System.out.println("t.lelt = " + (t1-t0));
     //System.out.println("b = " + b.size());

     LIST<BigRational> c = new LIST<BigRational>( b );
     //System.out.println("c = " + LENGTH(c));
     assertEquals("len(c) == "+max+" ", LENGTH(c), max );
     assertTrue("a == c ", EQUAL(a,c) );
 }

}
