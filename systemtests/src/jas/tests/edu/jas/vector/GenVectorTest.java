/*
 * $Id: GenVectorTest.java 1265 2007-07-29 10:22:22Z kredel $
 */

package edu.jas.vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


//import edu.jas.structure.RingElem;
//import edu.jas.structure.ModulElem;

import edu.jas.arith.BigRational;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;


/**
 * GenVector tests with JUnit 
 * @author Heinz Kredel.
 */

public class GenVectorTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>RatGenVectorTest</CODE> object.
 * @param name String.
 */
   public GenVectorTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GenVectorTest.class);
     return suite;
   }

   int rl = 5;
   int kl = 10;
   int ll = 10;
   float q = 0.5f;

   protected void setUp() {
   }

   protected void tearDown() {
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstruction() {
     BigRational cfac = new BigRational(1);
     GenVectorModul<BigRational> mfac 
         = new GenVectorModul<BigRational>(cfac,ll);
 
     assertTrue("#columns = "+ll, mfac.cols == ll);
     assertTrue("cfac == coFac ", cfac == mfac.coFac);

     GenVector<BigRational> a; 
     a = mfac.getZERO();
     //System.out.println("a = " + a);
     assertTrue("isZERO( a )", a.isZERO() );

     GenVector<BigRational> b = new GenVector<BigRational>(mfac); 
     //System.out.println("b = " + b);
     assertTrue("isZERO( b )", b.isZERO() );

     assertTrue("a == b ", a.equals(b) );

     GenVector<BigRational> c = b.clone();
     //System.out.println("c = " + c);
     assertTrue("isZERO( c )", c.isZERO() );
     assertTrue("a == c ", a.equals(c) );

     GenVector<BigRational> d = mfac.copy(b);
     //System.out.println("d = " + d);
     assertTrue("isZERO( d )", d.isZERO() );
     assertTrue("a == d ", a.equals(d) );
 }


/**
 * Test random vector.
 * 
 */
 public void testRandom() {
     BigRational cfac = new BigRational(1);
     GenVectorModul<BigRational> mfac 
        = new GenVectorModul<BigRational>(cfac,ll);
     GenVector<BigRational> a; 

     for (int i = 0; i < 7; i++) {
         a = mfac.random(kl,q);
         //System.out.println("a = " + a);
         if ( a.isZERO() ) {
            continue;
         }
         assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
     }
 }


/**
 * Test addition.
 * 
 */
 public void testAddition() {
     BigRational cfac = new BigRational(1);
     GenVectorModul<BigRational> mfac 
        = new GenVectorModul<BigRational>(cfac,ll);
     GenVector<BigRational> a, b, c, d, e; 

     a = mfac.random(kl,q);
     b = mfac.random(kl,q);
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);

     c = a.sum(b);
     d = c.subtract(b);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+b-b = a",a,d);

     c = a.sum( b );
     d = c.sum( b.negate() );
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+b+(-b) = a",a,d);

     c = a.sum(b);
     d = b.sum(a);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+b = b+a",c,d);

     c = mfac.random(kl,q);
     d = a.sum( b ).sum( c );
     e = a.sum( b.sum( c ) );
     //System.out.println("d = " + d);
     //System.out.println("e = " + e);
     assertEquals("a+(b+c) = (a+b)+c",d,e);
 }


/**
 * Test scalar multiplication.
 * 
 */
 public void testMultiplication() {
     BigRational cfac = new BigRational(1);
     GenVectorModul<BigRational> mfac 
        = new GenVectorModul<BigRational>(cfac,ll);
     BigRational r, s, t; 
     GenVector<BigRational> a, b, c, d, e; 

     r = cfac.random(kl);
     //System.out.println("r = " + r);
     s = r.inverse();
     //System.out.println("s = " + s);

     a = mfac.random(kl,q);
     //System.out.println("a = " + a);

     c = a.scalarMultiply(r);
     d = c.scalarMultiply(s);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a*b*(1/b) = a",a,d);


     b = mfac.random(kl,q);
     //System.out.println("b = " + b);

     t = cfac.getONE();
     //System.out.println("t = " + t);
     c = a.linearCombination(b,t);
     d = b.linearCombination(a,t);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+1*b = b+1*a",c,d);

     c = a.linearCombination(b,t);
     d = a.sum(b);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+1*b = b+1*a",c,d);

     s = t.negate();
     //System.out.println("s = " + s);
     c = a.linearCombination(b,t);
     d = c.linearCombination(b,s);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+1*b+(-1)*b = a",a,d);

     c = a.linearCombination(t,b,t);
     d = c.linearCombination(t,b,s);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a*1+b*1+b*(-1) = a",a,d);

     t = cfac.getZERO();
     //System.out.println("t = " + t);
     c = a.linearCombination(b,t);
     //System.out.println("c = " + c);
     assertEquals("a+0*b = a",a,c);

     d = a.linearCombination(t,b,t);
     //System.out.println("d = " + d);
     assertEquals("0*a+0*b = 0",mfac.getZERO(),d);

     r = a.scalarProduct(b);
     s = b.scalarProduct(a);
     //System.out.println("r = " + r);
     //System.out.println("s = " + s);
     assertEquals("a.b = b.a",r,s);
 }


/**
 * Test constructor and toString.
 * 
 */
 public void testPolynomialConstruction() {
     BigRational cfac = new BigRational(1);
     GenPolynomialRing<BigRational> pfac 
         = new GenPolynomialRing<BigRational>(cfac,rl);
 
     GenVectorModul<GenPolynomial<BigRational>> mfac 
         = new GenVectorModul<GenPolynomial<BigRational>>(pfac,ll);

     assertTrue("#columns = "+ll, mfac.cols == ll);
     assertTrue("pfac == coFac ", pfac == mfac.coFac);

     GenVector<GenPolynomial<BigRational>> a; 
     a = mfac.getZERO();
     //System.out.println("a = " + a);
     assertTrue("isZERO( a )", a.isZERO() );

     GenVector<GenPolynomial<BigRational>> b = new GenVector<GenPolynomial<BigRational>>(mfac); 
     //System.out.println("b = " + b);
     assertTrue("isZERO( b )", b.isZERO() );

     assertTrue("a == b ", a.equals(b) );

     GenVector<GenPolynomial<BigRational>> c = b.clone();
     //System.out.println("c = " + c);
     assertTrue("isZERO( c )", c.isZERO() );
     assertTrue("a == c ", a.equals(c) );

     GenVector<GenPolynomial<BigRational>> d = mfac.copy(b);
     //System.out.println("d = " + d);
     assertTrue("isZERO( d )", d.isZERO() );
     assertTrue("a == d ", a.equals(d) );
 }


/**
 * Test random vector
 * 
 */
 public void testPolynomialRandom() {
     BigRational cfac = new BigRational(1);
     GenPolynomialRing<BigRational> pfac 
         = new GenPolynomialRing<BigRational>(cfac,rl);

     GenVectorModul<GenPolynomial<BigRational>> mfac 
         = new GenVectorModul<GenPolynomial<BigRational>>(pfac,ll);

     GenVector<GenPolynomial<BigRational>> a; 
     for (int i = 0; i < 7; i++) {
         a = mfac.random(kl,q);
         //System.out.println("a = " + a);
         assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
     }
 }


/**
 * Test addition.
 * 
 */
 public void testPolynomialAddition() {
     BigRational cfac = new BigRational(1);
     GenPolynomialRing<BigRational> pfac 
         = new GenPolynomialRing<BigRational>(cfac,rl);

     GenVectorModul<GenPolynomial<BigRational>> mfac 
         = new GenVectorModul<GenPolynomial<BigRational>>(pfac,ll);

     GenVector<GenPolynomial<BigRational>> a, b, c, d, e; 

     a = mfac.random(kl,q);
     b = mfac.random(kl,q);
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);

     c = a.sum(b);
     d = c.subtract(b);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+b-b = a",a,d);

     c = a.sum( b );
     d = c.sum( b.negate() );
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+b+(-b) = a",a,d);

     c = a.sum(b);
     d = b.sum(a);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+b = b+a",c,d);

     c = mfac.random(kl,q);
     d = a.sum( b ).sum( c );
     e = a.sum( b.sum( c ) );
     //System.out.println("d = " + d);
     //System.out.println("e = " + e);
     assertEquals("a+(b+c) = (a+b)+c",d,e);
 }


/**
 * Test scalar multiplication.
 * 
 */
 public void testPolynomialMultiplication() {
     BigRational cfac = new BigRational(1);
     GenPolynomialRing<BigRational> pfac 
         = new GenPolynomialRing<BigRational>(cfac,rl);

     GenVectorModul<GenPolynomial<BigRational>> mfac 
         = new GenVectorModul<GenPolynomial<BigRational>>(pfac,ll);

     GenPolynomial<BigRational> r, s, t; 
     GenVector<GenPolynomial<BigRational>> a, b, c, d, e; 

     r = pfac.random(kl);
     //System.out.println("r = " + r);
     s = r.negate();
     //System.out.println("s = " + s);

     a = mfac.random(kl,q);
     //System.out.println("a = " + a);

     c = a.scalarMultiply(r);
     d = a.scalarMultiply(s);
     e = c.sum(d);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     //System.out.println("e = " + e);
     assertEquals("a*b + a*(-b) = 0",e,mfac.getZERO());


     b = mfac.random(kl,q);
     //System.out.println("b = " + b);

     t = pfac.getONE();
     //System.out.println("t = " + t);
     c = a.linearCombination(b,t);
     d = b.linearCombination(a,t);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+1*b = b+1*a",c,d);

     c = a.linearCombination(b,t);
     d = a.sum(b);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+1*b = b+1*a",c,d);

     s = t.negate();
     //System.out.println("s = " + s);
     c = a.linearCombination(b,t);
     d = c.linearCombination(b,s);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+1*b+(-1)*b = a",a,d);

     c = a.linearCombination(t,b,t);
     d = c.linearCombination(t,b,s);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+1*b+(-1)*b = a",a,d);

     t = pfac.getZERO();
     //System.out.println("t = " + t);
     c = a.linearCombination(b,t);
     //System.out.println("c = " + c);
     assertEquals("a+0*b = a",a,c);

     d = a.linearCombination(t,b,t);
     //System.out.println("d = " + d);
     assertEquals("0*a+0*b = 0",mfac.getZERO(),d);

     r = a.scalarProduct(b);
     s = b.scalarProduct(a);
     //System.out.println("r = " + r);
     //System.out.println("s = " + s);
     assertEquals("a.b = b.a",r,s);
 }

}
