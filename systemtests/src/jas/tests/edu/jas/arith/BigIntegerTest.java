/*
 * $Id: BigIntegerTest.java 1244 2007-07-29 09:54:27Z kredel $
 */

package edu.jas.arith;

//import edu.jas.arith.BigInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * BigInteger tests with JUnit.
 * @author Heinz Kredel.
 */

public class BigIntegerTest extends TestCase {

/**
 * main
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>BigIntegerTest</CODE> object.
 * @param name String.
 */
   public BigIntegerTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(BigIntegerTest.class);
     return suite;
   }

   private final static int bitlen = 100;

   BigInteger a;
   BigInteger b;
   BigInteger c;
   BigInteger d;
   BigInteger e;

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
     a = BigInteger.ZERO;
     b = BigInteger.ONE;
     c = BigInteger.IDIF(b,b);

     assertEquals("1-1 = 0",c,a);
     assertTrue("1-1 = 0",c.isZERO());
     assertTrue("1 = 1", b.isONE() );
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstructor() {
     a = new BigInteger( "34" );
     b = new BigInteger( "34" );

     assertEquals("34 = 34",a,b);

     a = new BigInteger( "-4" );
     b = new BigInteger( "-4" );

     assertEquals("-4 = -4",a,b);

     String s = "1111111111111111111111111111111111111111111";
     a = new BigInteger( s );
     String t = a.toString();

     assertEquals("stringConstr = toString",s,t);

     a = new BigInteger( 1 );
     b = new BigInteger( -1 );
     c = BigInteger.ISUM(b,a);

     assertTrue("1 = 1", a.isONE() );
     assertEquals("1+(-1) = 0",c,BigInteger.ZERO);
   }


/**
 * Test random integer.
 * 
 */
 public void testRandom() {
     a = BigInteger.IRAND( 500 );
     b = new BigInteger( "" + a );
     c = BigInteger.IDIF(b,a);

     assertEquals("a-b = 0",c,BigInteger.ZERO);

     d = new BigInteger( b.getVal() );
     assertEquals("sign(a-a) = 0", 0, b.compareTo(d) );
 }


/**
 * Test addition.
 * 
 */
 public void testAddition() {
     a = BigInteger.IRAND( bitlen );
     b = BigInteger.ISUM(a,a);
     c = BigInteger.IDIF(b,a);

     assertEquals("a+a-a = a",c,a);
     assertEquals("a+a-a = a", 0, c.compareTo(a) );

     d = BigInteger.ISUM( a, BigInteger.ZERO );
     assertEquals("a+0 = a",d,a);
     d = BigInteger.IDIF( a, BigInteger.ZERO );
     assertEquals("a-0 = a",d,a);
     d = BigInteger.IDIF( a, a );
     assertEquals("a-a = 0", d, BigInteger.ZERO );

 }


/**
 * Test multiplication.
 * 
 */
 public void testMultiplication() {
     a = BigInteger.IRAND( bitlen );
     b = BigInteger.IPROD( a, a );
     c = BigInteger.IQ( b, a );

     assertEquals("a*a/a = a",c,a);
     assertEquals("a*a/a = a",0,BigInteger.ICOMP(c,a));

     d = BigInteger.IPROD( a, BigInteger.ONE );
     assertEquals("a*1 = a",d,a);
     d = BigInteger.IQ( a, BigInteger.ONE );
     assertEquals("a/1 = a",d,a);

     a = BigInteger.IRAND( bitlen*2 );
     b = BigInteger.IRAND( bitlen );
     BigInteger[] qr = BigInteger.IQR( a, b );
     c = BigInteger.IPROD( qr[0], b );
     c = BigInteger.ISUM( c, qr[1] );
     assertEquals("a = q*b+r)",a,c);
 }


/**
 * Test distributive law.
 * 
 */
 public void testDistributive() {
     BigInteger fac = new BigInteger();

     a = fac.random( bitlen );
     b = fac.random( bitlen );
     c = fac.random( bitlen );

     d = a.multiply( b.sum(c) );
     e = a.multiply( b ).sum( a.multiply(c) );

     assertEquals("a(b+c) = ab+ac",d,e);
 }


/**
 * Test gcd.
 * 
 */
 public void testGcd() {
     a = BigInteger.IRAND( bitlen );
     b = BigInteger.IRAND( bitlen );
     c = BigInteger.IGCD( a, b ); // ~1

     BigInteger[] qr = BigInteger.IQR( a, c );
     d = BigInteger.IPROD( qr[0], c );
     assertEquals("a = gcd(a,b)*q1",a,d);
     assertEquals("a/gcd(a,b) = q*x + 0", qr[1], BigInteger.ZERO );

     qr = BigInteger.IQR( b, c );
     d = BigInteger.IPROD( qr[0], c );
     assertEquals("b = gcd(a,b)*q1",b,d);
     assertEquals("b/gcd(a,b) = q*x + 0", qr[1], BigInteger.ZERO );


     c = BigInteger.IRAND( bitlen*4 );
     a = BigInteger.IPROD( a, c );
     b = BigInteger.IPROD( b, c );
     c = BigInteger.IGCD( a, b ); // = c

     qr = BigInteger.IQR( a, c );
     d = BigInteger.IPROD( qr[0], c );
     assertEquals("a = gcd(a,b)*q1",a,d);
     assertEquals("a/gcd(a,b) = q*x + 0", qr[1], BigInteger.ZERO );

     qr = BigInteger.IQR( b, c );
     d = BigInteger.IPROD( qr[0], c );
     assertEquals("b = gcd(a,b)*q1",b,d);
     assertEquals("b/gcd(a,b) = q*x + 0", qr[1], BigInteger.ZERO );

 }

}
