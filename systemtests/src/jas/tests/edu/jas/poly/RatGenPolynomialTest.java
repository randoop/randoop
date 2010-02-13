/*
 * $Id: RatGenPolynomialTest.java 1888 2008-07-12 13:37:34Z kredel $
 */

package edu.jas.poly;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.jas.arith.BigRational;


/**
 * BigRational coefficients GenPolynomial tests with JUnit.
 * @author Heinz Kredel.
 */

public class RatGenPolynomialTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>RatGenPolynomialTest</CODE> object.
 * @param name String.
 */
   public RatGenPolynomialTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(RatGenPolynomialTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   GenPolynomialRing<BigRational> fac;

   GenPolynomial<BigRational> a;
   GenPolynomial<BigRational> b;
   GenPolynomial<BigRational> c;
   GenPolynomial<BigRational> d;
   GenPolynomial<BigRational> e;

   int rl = 7; 
   int kl = 10;
   int ll = 10;
   int el = 5;
   float q = 0.5f;

   protected void setUp() {
       a = b = c = d = e = null;
       fac = new GenPolynomialRing<BigRational>(new BigRational(1),rl);
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
     assertTrue("length( c ) = 1", c.length() == 1);
     assertTrue("isZERO( c )", !c.isZERO() );
     assertTrue("isONE( c )", c.isONE() );

     d = fac.getZERO();
     assertTrue("length( d ) = 0", d.length() == 0);
     assertTrue("isZERO( d )", d.isZERO() );
     assertTrue("isONE( d )", !d.isONE() );
 }


/**
 * Test random polynomial.
 */
 public void testRandom() {
     for (int i = 0; i < 7; i++) {
         a = fac.random(ll);
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
     BigRational x = BigRational.RNRAND(kl);

     b = new GenPolynomial<BigRational>(fac,x, u);
     c = a.sum(b);
     d = a.sum(x,u);
     assertEquals("a+p(x,u) = a+(x,u)",c,d);

     c = a.subtract(b);
     d = a.subtract(x,u);
     assertEquals("a-p(x,u) = a-(x,u)",c,d);

     a = new GenPolynomial<BigRational>(fac);
     b = new GenPolynomial<BigRational>(fac,x, u);
     c = b.sum(a);
     d = a.sum(x,u);
     assertEquals("a+p(x,u) = a+(x,u)",c,d);

     c = a.subtract(b);
     d = a.subtract(x,u);
     assertEquals("a-p(x,u) = a-(x,u)",c,d);
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

     BigRational x = a.leadingBaseCoefficient().inverse();
     c = a.monic();
     d = a.multiply(x);
     assertEquals("a.monic() = a(1/ldcf(a))",c,d);

     BigRational y = b.leadingBaseCoefficient().inverse();
     c = b.monic();
     d = b.multiply(y);
     assertEquals("b.monic() = b(1/ldcf(b))",c,d);

     e = new GenPolynomial<BigRational>(fac,y);
     d = b.multiply(e);
     assertEquals("b.monic() = b(1/ldcf(b))",c,d);

     d = e.multiply(b);
     assertEquals("b.monic() = (1/ldcf(b) (0))*b",c,d);
 }


/**
 * Test distributive law.
 * 
 */
 public void testDistributive() {
     a = fac.random(kl,ll,el,q);
     b = fac.random(kl,ll,el,q);
     c = fac.random(kl,ll,el,q);

     d = a.multiply( b.sum(c) );
     e = a.multiply( b ).sum( a.multiply(c) );

     assertEquals("a(b+c) = ab+ac",d,e);
 }


/**
 * Test object quotient and remainder.
 * 
 */

 public void testQuotRem() {

     fac = new GenPolynomialRing<BigRational>(new BigRational(1),1);

     a = fac.random(ll).monic();
     assertTrue("not isZERO( a )", !a.isZERO() );

     b = fac.random(ll).monic();
     assertTrue("not isZERO( b )", !b.isZERO() );

     GenPolynomial<BigRational> h = a;
     GenPolynomial<BigRational> g = fac.random(ll).monic();
     assertTrue("not isZERO( g )", !g.isZERO() );
     a = a.multiply(g);
     b = b.multiply(g);
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     //System.out.println("g = " + g);

     GenPolynomial<BigRational>[] qr;
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


     GenPolynomial<BigRational>[] gst;
     gst = a.egcd(b);
     //System.out.println("egcd = " + gst[0]);
     //System.out.println(", s = " + gst[1] + ", t = " + gst[2]);
     c = gst[0];
     d = gst[1];
     e = gst[2];
     assertEquals("g = gcd(a,b)", c, g );

     GenPolynomial<BigRational> x;
     x = a.multiply(d).sum( b.multiply(e) ).monic(); 
     //System.out.println("x = " + x);
     assertEquals("gcd(a,b) = a s + b t", c, x );


     //System.out.println("g = " + g);
     //System.out.println("h = " + h);
     c = h.modInverse(g);
     //System.out.println("c = " + c);
     x = c.multiply(h).remainder( g ).monic(); 
     //System.out.println("x = " + x);
     assertTrue("h invertible mod g", x.isONE() );

 }

}
