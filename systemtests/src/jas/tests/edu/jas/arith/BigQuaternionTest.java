/*
 * $Id: BigQuaternionTest.java 1244 2007-07-29 09:54:27Z kredel $
 */

package edu.jas.arith;

//import edu.jas.arith.BigRational;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * BigQuaternion tests with JUnit. 
 * @author Heinz Kredel.
 */

public class BigQuaternionTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>BigQuaternionTest</CODE> object.
 * @param name String.
 */
   public BigQuaternionTest(String name) {
          super(name);
   }

/**
 */ 
 /**
 * @return suite.
 */
public static Test suite() {
     TestSuite suite= new TestSuite(BigQuaternionTest.class);
     return suite;
   }

   BigQuaternion a;
   BigQuaternion b;
   BigQuaternion c;
   BigQuaternion d;
   BigQuaternion e;
   BigQuaternion fac;

   protected void setUp() {
       a = b = c = d = e = null;
       fac = new BigQuaternion();
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       fac = null;
   }


/**
 * Test static initialization and constants.
 * 
 */
 public void testConstants() {
     a = BigQuaternion.ZERO;
     b = BigQuaternion.ONE;
     c = b.subtract(b);
     assertEquals("1-1 = 0",c,a);
     assertTrue("1-1 = 0",c.isZERO());
     assertTrue("1 = 1", b.isONE() );

     a = BigQuaternion.ZERO;
     b = BigQuaternion.ONE;
     c = b.subtract(b);
     assertEquals("1-1 = 0",c,a);
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstructor() {
     a = new BigQuaternion( "6/8" );
     b = new BigQuaternion( "3/4" );

     assertEquals("6/8 = 3/4",a,b);

     a = new BigQuaternion( "3/4 i 4/5 j 1/5 k 2/5" );
     b = new BigQuaternion( "-3/4 i -4/5 j -1/5 k -2/5" );
     assertEquals("3/4 + i 4/5 + j 1/5 + k 2/5",a,b.negate());

     String s = "6/1111111111111111111111111111111111111111111";
     a = new BigQuaternion( s );
     String t = a.toString();

     assertEquals("stringConstr = toString",s,t);

     a = new BigQuaternion( 1 );
     b = new BigQuaternion( -1 );
     c = b.sum(a);

     assertTrue("1 = 1", a.isONE() );
     assertEquals("1+(-1) = 0",c,BigQuaternion.ZERO);
   }


/**
 * Test random rationals.
 * 
 */
 public void testRandom() {
     a = fac.random( 500 );
     b = new BigQuaternion( a.getRe(), a.getIm(), a.getJm(), a.getKm() );
     c = b.subtract(a);

     assertEquals("a-b = 0",BigQuaternion.ZERO,c);

     d = new BigQuaternion( b.getRe(), b.getIm(), b.getJm(), b.getKm() );
     assertEquals("sign(a-a) = 0", 0, b.compareTo(d) );
 }


/**
 * Test addition.
 * 
 */
 public void testAddition() {
     a = fac.random( 100 );
     b = a.sum( a );
     c = b.subtract( a );
     assertEquals("a+a-a = a",c,a);
     assertEquals("a+a-a = a",0,c.compareTo(a));

     d = a.sum( BigQuaternion.ZERO );
     assertEquals("a+0 = a",d,a);
     d = a.subtract( BigQuaternion.ZERO );
     assertEquals("a-0 = a",d,a);
     d = a.subtract( a );
     assertEquals("a-a = 0",d,BigQuaternion.ZERO);

 }


/**
 * Test multiplication.
 * 
 */
 public void testMultiplication() {
     a = fac.random( 100 );
     b = a.multiply( a );
     c = b.divide( a );
     assertEquals("a*a/a = a",c,a);
     assertEquals("a*a/a = a",0,c.compareTo(a));

     d = a.multiply( BigQuaternion.ONE );
     assertEquals("a*1 = a",d,a);
     d = a.divide( BigQuaternion.ONE );
     assertEquals("a/1 = a",d,a);

     a = fac.random( 100 );
     b = a.inverse();
     c = a.multiply( b );
     assertTrue("a*1/a = 1", c.isONE() );

     b = a.abs();
     c = b.inverse();
     d = b.multiply( c );
     assertTrue("abs(a)*1/abs(a) = 1", d.isONE() );

     b = a.abs();
     c = a.conjugate();
     d = a.multiply( c );
     assertEquals("abs(a)^2 = a a^", b, d );
 }


/**
 * Test multiplication axioms.
 * 
 */
 public void testMultiplicationAxioms() {
     a = fac.random( 100 );
     b = fac.random( 100 );

     c = a.multiply( b );
     d = b.multiply( a );
     assertTrue("a*b != b*a",!c.equals(d));

     c = fac.random( 100 );

     d = a.multiply( b.multiply( c ) );
     e = a.multiply( b ).multiply( c );
     assertTrue("a(bc) = (ab)c",e.equals(d));
 }


/**
 * Test distributive law.
 * 
 */
 public void testDistributive() {
     a = fac.random( 20 );
     b = fac.random( 20 );
     c = fac.random( 20 );

     d = a.multiply( b.sum(c) );
     e = a.multiply( b ).sum( a.multiply(c) );

     assertEquals("a(b+c) = ab+ac",d,e);
 }


}
