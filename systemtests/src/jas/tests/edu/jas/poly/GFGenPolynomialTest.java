/*
 * $Id: GFGenPolynomialTest.java 1888 2008-07-12 13:37:34Z kredel $
 */

package edu.jas.poly;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//import edu.jas.structure.RingElem;

//import edu.jas.arith.BigRational;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.AlgebraicNumber;
import edu.jas.poly.AlgebraicNumberRing;


/**
 * Galois field coefficients GenPolynomial tests with JUnit.
 * @author Heinz Kredel.
 */

public class GFGenPolynomialTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>GFGenPolynomialTest</CODE> object.
 * @param name String.
 */
   public GFGenPolynomialTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GFGenPolynomialTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   GenPolynomialRing<AlgebraicNumber<ModInteger>> fac;
   AlgebraicNumberRing<ModInteger> cfac;

   GenPolynomial<AlgebraicNumber<ModInteger>> a;
   GenPolynomial<AlgebraicNumber<ModInteger>> b;
   GenPolynomial<AlgebraicNumber<ModInteger>> c;
   GenPolynomial<AlgebraicNumber<ModInteger>> d;
   GenPolynomial<AlgebraicNumber<ModInteger>> e;

   int rl = 7; 
   int kl = 10;
   int ll = 8;
   int el = 5;
   float q = 0.5f;

   protected long getPrime() {
       long prime = 2; //2^60-93; // 2^30-35; //19; knuth (2,390)
       for ( int i = 1; i < 60; i++ ) {
           prime *= 2;
       }
       prime -= 93;
       //System.out.println("prime = " + prime);
       return prime;
   }

   protected void setUp() {
       a = b = c = d = e = null;
       long prime = getPrime();
       ModIntegerRing r = new ModIntegerRing(prime);
       // univariate minimal polynomial
       GenPolynomialRing<ModInteger> mfac =  
           new GenPolynomialRing<ModInteger>(r,1);
       GenPolynomial<ModInteger> modul = mfac.random(5); 
       while ( modul.isZERO() || modul.isUnit() || modul.isConstant() ) {
             modul = mfac.random(5); 
       }
       cfac = new AlgebraicNumberRing<ModInteger>( modul.monic() );
       fac = new GenPolynomialRing<AlgebraicNumber<ModInteger>>(cfac,rl);
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       fac = null;
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstruction() {
     c = fac.getONE();
     //System.out.println("c = " + c);
     assertTrue("length( c ) = 1", c.length() == 1);
     assertTrue("isZERO( c )c"+c, !c.isZERO() );
     assertTrue("isONE( c ) "+c, c.isONE() );

     d = fac.getZERO();
     //System.out.println("d = " + d);
     assertTrue("length( d ) = 0", d.length() == 0);
     assertTrue("isZERO( d )", d.isZERO() );
     assertTrue("isONE( d )", !d.isONE() );
 }


/**
 * Test random polynomial.
 * 
 */
 public void testRandom() {
     for (int i = 0; i < 7; i++) {
         a = fac.random(ll+i);
         //System.out.println("a = " + a);

             // fac.random(rl+i, kl*(i+1), ll+2*i, el+i, q );
         assertTrue("length( a"+i+" ) <> 0", a.length() >= 0);
         assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
         assertTrue(" not isONE( a"+i+" )", !a.isONE() );
     }
 }


/**
 * Test addition.
 * 
 */
 public void testAddition() {

     a = fac.random(ll);
     b = fac.random(ll);

     c = a.sum(b);
     d = c.subtract(b);
     assertEquals("a+b-b = a",a,d);

     c = fac.random(ll);

     ExpVector u = ExpVector.EVRAND(rl,el,q);
     AlgebraicNumber<ModInteger> x = cfac.random(kl);

     b = new GenPolynomial<AlgebraicNumber<ModInteger>>(fac, x, u);
     c = a.sum(b);
     d = a.sum(x,u);
     assertEquals("a+p(x,u) = a+(x,u)",c,d);

     c = a.subtract(b);
     d = a.subtract(x,u);
     assertEquals("a-p(x,u) = a-(x,u)",c,d);

     a = new GenPolynomial<AlgebraicNumber<ModInteger>>(fac);
     b = new GenPolynomial<AlgebraicNumber<ModInteger>>(fac,x, u);
     c = b.sum(a);
     d = a.sum(x,u);
     assertEquals("a+p(x,u) = a+(x,u)",c,d);

     c = a.subtract(b);
     d = a.subtract(x,u);
     assertEquals("a-p(x,u) = a-(x,u)",c,d);


     c = fac.random(ll);
     d = c.sum( a.sum(b) );
     e = c.sum( a ).sum(b);
     assertEquals("c+(a+b) = (c+a)+b",d,e);

     c = a.sum( fac.getZERO() );
     d = a.subtract( fac.getZERO() );
     assertEquals("a+0 = a-0",c,d);

     c = fac.getZERO().sum( a );
     d = fac.getZERO().subtract( a.negate() );
     assertEquals("0+a = 0+(-a)",c,d);
 }


/**
 * Test object multiplication.
 * 
 */

 public void testMultiplication() {

     a = fac.random(ll);
     assertTrue("not isZERO( a )", !a.isZERO() );

     b = fac.random(ll);
     assertTrue("not isZERO( b )", !b.isZERO() );

     c = b.multiply(a);
     d = a.multiply(b);
     assertTrue("not isZERO( c )", !c.isZERO() );
     assertTrue("not isZERO( d )", !d.isZERO() );

     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     e = d.subtract(c);
     assertTrue("isZERO( a*b-b*a ) " + e, e.isZERO() );

     assertTrue("a*b = b*a", c.equals(d) );
     assertEquals("a*b = b*a",c,d);

     c = fac.random(ll);
     //System.out.println("c = " + c);
     d = a.multiply( b.multiply(c) );
     e = (a.multiply(b)).multiply(c);

     //System.out.println("d = " + d);
     //System.out.println("e = " + e);

     //System.out.println("d-e = " + d.subtract(c) );

     assertEquals("a(bc) = (ab)c",d,e);
     assertTrue("a(bc) = (ab)c", d.equals(e) );

     AlgebraicNumber<ModInteger> z = a.leadingBaseCoefficient();
     //System.out.println("z = " + z);
     if ( z.isUnit() ) {
        AlgebraicNumber<ModInteger> x = z.inverse();
        //System.out.println("x = " + x);
        //System.out.println("a = " + a);
        c = a.monic();
        //System.out.println("c = " + c);
        d = a.multiply(x);
        //System.out.println("d = " + d);
        assertEquals("a.monic() = a(1/ldcf(a))",c,d);
     }

     AlgebraicNumber<ModInteger> y = b.leadingBaseCoefficient();
     if ( y.isUnit() ) {
         y = y.inverse();
         c = b.monic();
         d = b.multiply(y);
         assertEquals("b.monic() = b(1/ldcf(b))",c,d);

         e = new GenPolynomial<AlgebraicNumber<ModInteger>>(fac,y);
         d = b.multiply(e);
         assertEquals("b.monic() = b(1/ldcf(b))",c,d);

         d = e.multiply(b);
         assertEquals("b.monic() = (1/ldcf(b))*b",c,d);
     }
 }


/**
 * Test distributive law.
 * 
 */
 public void testDistributive() {
     a = fac.random( ll );
     b = fac.random( ll );
     c = fac.random( ll );

     d = a.multiply( b.sum(c) );
     e = a.multiply( b ).sum( a.multiply(c) );

     assertEquals("a(b+c) = ab+ac",d,e);
 }


/**
 * Test object quotient and remainder.
 * 
 */

 public void testQuotRem1() {

     fac = new GenPolynomialRing<AlgebraicNumber<ModInteger>>(cfac,1);

     a = fac.random(ll).monic();
     assertTrue("not isZERO( a )", !a.isZERO() );

     b = fac.random(ll).monic();
     assertTrue("not isZERO( b )", !b.isZERO() );

     GenPolynomial<AlgebraicNumber<ModInteger>> h = a;
     GenPolynomial<AlgebraicNumber<ModInteger>> g = fac.random(ll).monic();
     assertTrue("not isZERO( g )", !g.isZERO() );
     g = fac.getONE();
     a = a.multiply(g);
     b = b.multiply(g);
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     //System.out.println("g = " + g);

     GenPolynomial<AlgebraicNumber<ModInteger>>[] qr;
     qr = b.divideAndRemainder(a);
     c = qr[0];
     d = qr[1];
     //System.out.println("q = " + c);
     //System.out.println("r = " + d);
     e = c.multiply(a).sum(d);
     assertEquals("b = q a + r", b, e );

     qr = a.divideAndRemainder(b);
     c = qr[0];
     d = qr[1];
     //System.out.println("q = " + c);
     //System.out.println("r = " + d);
     e = c.multiply(b).sum(d);
     assertEquals("a = q b + r", a, e );


     // gcd tests -------------------------------
     c = a.gcd(b);
     //System.out.println("gcd = " + c);
     assertTrue("a mod gcd(a,b) = 0", a.remainder(c).isZERO() );
     assertTrue("b mod gcd(a,b) = 0", b.remainder(c).isZERO() );
     assertEquals("g = gcd(a,b)", c, g );


     GenPolynomial<AlgebraicNumber<ModInteger>>[] gst;
     gst = a.egcd(b);
     //System.out.println("egcd = " + gst[0]);
     //System.out.println(", s = " + gst[1] + ", t = " + gst[2]);
     c = gst[0];
     d = gst[1];
     e = gst[2];
     assertEquals("g = gcd(a,b)", c, g );

     GenPolynomial<AlgebraicNumber<ModInteger>> x;
     x = a.multiply(d).sum( b.multiply(e) ).monic(); 
     //System.out.println("x = " + x);
     assertEquals("gcd(a,b) = a s + b t", c, x );


     //System.out.println("g = " + g);
     //System.out.println("h = " + h);
     if ( a.isZERO() || b.isZERO() ) {
         return;
     }
     try {
         c = a.modInverse(b);
         //System.out.println("c = " + c);
         x = c.multiply(a).remainder( b ).monic(); 
         //System.out.println("x = " + x);
         assertTrue("a invertible mod b", x.isUnit() );
     } catch (RuntimeException e) {
         // dann halt nicht
     }

 }

}
