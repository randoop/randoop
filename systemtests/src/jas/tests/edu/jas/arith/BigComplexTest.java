/*
 * $Id: BigComplexTest.java 1244 2007-07-29 09:54:27Z kredel $
 */

package edu.jas.arith;

//import edu.jas.arith.BigRational;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * BigComplex tests with JUnit.
 * @author Heinz Kredel.
 */

public class BigComplexTest extends TestCase {

/**
 * main
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>BigComplexTest</CODE> object.
 * @param name String.
 */
   public BigComplexTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(BigComplexTest.class);
     return suite;
   }

   BigComplex a;
   BigComplex b;
   BigComplex c;
   BigComplex d;
   BigComplex e;

   protected void setUp() {
       a = b = c = d = e = null;
   }

   protected void tearDown() {
       a = b = c = d = e = null;
   }


/**
 * Test static initialization and constants.
 * 
 */
 public void testConstants() {
     a = BigComplex.ZERO;
     b = BigComplex.ONE;
     c = BigComplex.CDIF(b,b);

     assertEquals("1-1 = 0",c,a);
     assertTrue("1-1 = 0",c.isZERO());
     assertTrue("1 = 1", b.isONE() );

     a = BigComplex.ZERO;
     b = BigComplex.ONE;
     c = BigComplex.CDIF(b,b);

     assertEquals("1-1 = 0",c,a);
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstructor() {
     a = new BigComplex( "6/8" );
     b = new BigComplex( "3/4" );

     assertEquals("6/8 = 3/4",a,b);

     a = new BigComplex( "3/4 i 4/5" );
     b = new BigComplex( "-3/4 i -4/5" );

     assertEquals("3/4 + i 4/5 ",a,b.negate());

     String s = "6/1111111111111111111111111111111111111111111";
     a = new BigComplex( s );
     String t = a.toString();

     assertEquals("stringConstr = toString",s,t);

     a = new BigComplex( 1 );
     b = new BigComplex( -1 );
     c = BigComplex.CSUM(b,a);

     assertTrue("1 = 1", a.isONE() );
     assertEquals("1+(-1) = 0",c,BigComplex.ZERO);
   }


/**
 * Test random rationals.
 * 
 */
 public void testRandom() {
     a = BigComplex.CRAND( 500 );
     b = new BigComplex( a.getRe(), a.getIm() );
     c = BigComplex.CDIF(b,a);

     assertEquals("a-b = 0",c,BigComplex.ZERO);

     d = new BigComplex( b.getRe(), b.getIm() );
     assertEquals("sign(a-a) = 0", 0, b.compareTo(d) );
 }


/**
 * Test addition.
 * 
 */
 public void testAddition() {
     a = BigComplex.CRAND( 100 );
     b = BigComplex.CSUM( a, a );
     c = BigComplex.CDIF( b, a );

     assertEquals("a+a-a = a",c,a);
     assertEquals("a+a-a = a",0,c.compareTo(a));

     d = BigComplex.CSUM( a, BigComplex.ZERO );
     assertEquals("a+0 = a",d,a);
     d = BigComplex.CDIF( a, BigComplex.ZERO );
     assertEquals("a-0 = a",d,a);
     d = BigComplex.CDIF( a, a );
     assertEquals("a-a = 0",d,BigComplex.ZERO);

 }


/**
 * Test multiplication.
 * 
 */
 public void testMultiplication() {
     a = BigComplex.CRAND( 100 );
     b = BigComplex.CPROD( a, a );
     c = BigComplex.CQ( b, a );

     assertEquals("a*a/a = a",c,a);
     assertEquals("a*a/a = a",0,c.compareTo(a));

     d = BigComplex.CPROD( a, BigComplex.ONE );
     assertEquals("a*1 = a",d,a);
     d = BigComplex.CQ( a, BigComplex.ONE );
     assertEquals("a/1 = a",d,a);

     a = BigComplex.CRAND( 100 );
     b = BigComplex.CINV( a );
     c = BigComplex.CPROD( a, b );

     assertTrue("a*1/a = 1", c.isONE() );
 }


/**
 * Test distributive law.
 * 
 */
 public void testDistributive() {
     BigComplex fac = new BigComplex();

     a = fac.random( 500 );
     b = fac.random( 500 );
     c = fac.random( 500 );

     d = a.multiply( b.sum(c) );
     e = a.multiply( b ).sum( a.multiply(c) );

     assertEquals("a(b+c) = ab+ac",d,e);
 }

}
