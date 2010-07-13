/*
 * $Id: RatPolyGenPolynomialTest.java 1888 2008-07-12 13:37:34Z kredel $
 */

package edu.jas.poly;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.jas.arith.BigRational;


/**
 * BigRational coefficients GenPolynomial coefficients GenPolynomial tests with JUnit.
 * @author Heinz Kredel.
 */

public class RatPolyGenPolynomialTest extends TestCase {

/**
 * main
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>RatPolyGenPolynomialTest</CODE> object.
 * @param name String.
 */
   public RatPolyGenPolynomialTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(RatPolyGenPolynomialTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   GenPolynomialRing<BigRational> cf;
   GenPolynomialRing<GenPolynomial<BigRational>> fac;

   GenPolynomial<GenPolynomial<BigRational>> a;
   GenPolynomial<GenPolynomial<BigRational>> b;
   GenPolynomial<GenPolynomial<BigRational>> c;
   GenPolynomial<GenPolynomial<BigRational>> d;
   GenPolynomial<GenPolynomial<BigRational>> e;

   int rl = 7; 
   int kl = 10;
   int ll = 10;
   int el = 5;
   float q = 0.5f;

   protected void setUp() {
       a = b = c = d = e = null;
       cf  = new GenPolynomialRing<BigRational>( new BigRational(1), 1 );
       fac = new GenPolynomialRing<GenPolynomial<BigRational>>(cf,rl);
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
 * 
 */
 public void testRandom() {
     for (int i = 0; i < 7; i++) {
         a = fac.random(ll);
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
     GenPolynomial<BigRational> x = cf.random(kl);

     b = new GenPolynomial<GenPolynomial<BigRational>>(fac,x, u);
     c = a.sum(b);
     d = a.sum(x,u);
     assertEquals("a+p(x,u) = a+(x,u)",c,d);

     c = a.subtract(b);
     d = a.subtract(x,u);
     assertEquals("a-p(x,u) = a-(x,u)",c,d);

     a = new GenPolynomial<GenPolynomial<BigRational>>(fac);
     b = new GenPolynomial<GenPolynomial<BigRational>>(fac,x, u);
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

     //GenPolynomial<BigRational> x = a.leadingBaseCoefficient().inverse();
     //c = a.monic();
     //d = a.multiply(x);
     //assertEquals("a.monic() = a(1/ldcf(a))",c,d);

     GenPolynomial<BigRational> y = b.leadingBaseCoefficient();
     //c = b.monic();
     //d = b.multiply(y);
     //assertEquals("b.monic() = b(1/ldcf(b))",c,d);

     e = new GenPolynomial<GenPolynomial<BigRational>>(fac,y);
     c = b.multiply(e);
     // assertEquals("b.monic() = b(1/ldcf(b))",c,d);

     d = e.multiply(b);
     assertEquals("b*p(y,u) = p(y,u)*b",c,d);
 }

}
