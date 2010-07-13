
/*
 * $Id: QuotientTest.java 1259 2007-07-29 10:18:54Z kredel $
 */

package edu.jas.structure;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

//import edu.jas.arith.BigRational;
import edu.jas.arith.BigInteger;

//import edu.jas.structure.RingElem;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;


/**
 * Quotient tests with JUnit. 
 * @author Heinz Kredel.
 */

public class QuotientTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
       BasicConfigurator.configure();
       junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>QuotientTest</CODE> object.
 * @param name String.
 */
   public QuotientTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(QuotientTest.class);
     return suite;
   }


   QuotientRing<BigInteger> fac;
   GenPolynomialRing<BigInteger> pfac;
   QuotientRing< GenPolynomial<BigInteger> > mfac;

   Quotient< BigInteger > a;
   Quotient< BigInteger > b;
   Quotient< BigInteger > c;
   Quotient< BigInteger > d;
   Quotient< BigInteger > e;

   Quotient< GenPolynomial<BigInteger> > ap;
   Quotient< GenPolynomial<BigInteger> > bp;
   Quotient< GenPolynomial<BigInteger> > cp;
   Quotient< GenPolynomial<BigInteger> > dp;
   Quotient< GenPolynomial<BigInteger> > ep;

   int rl = 2; 
   int kl = 17;
   int ll = 3; //6;
   int el = 2;
   float q = 0.4f;

   protected void setUp() {
       a = b = c = d = e = null;
       ap = bp = cp = dp = ep = null;
       fac = new QuotientRing<BigInteger>( new BigInteger(1) );
       pfac = new GenPolynomialRing<BigInteger>( new BigInteger(1), 1 );
       mfac = new QuotientRing<GenPolynomial<BigInteger>>( pfac );
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       ap = bp = cp = dp = ep = null;
       fac = null;
       pfac = null;
       mfac = null;
   }


/**
 * Test constructor for integer.
 * 
 */
 public void testIntConstruction() {
     c = fac.getONE();
     //System.out.println("c = " + c);
     assertTrue("isZERO( c )", !c.isZERO() );
     assertTrue("isONE( c )", c.isONE() );

     d = fac.getZERO();
     //System.out.println("d = " + d);
     assertTrue("isZERO( d )", d.isZERO() );
     assertTrue("isONE( d )", !d.isONE() );
 }


/**
 * Test constructor for polynomial.
 * 
 */
 public void testPolyConstruction() {
     cp = mfac.getONE();
     assertTrue("isZERO( cp )", !cp.isZERO() );
     assertTrue("isONE( cp )", cp.isONE() );

     dp = mfac.getZERO();
     assertTrue("isZERO( dp )", dp.isZERO() );
     assertTrue("isONE( dp )", !dp.isONE() );
 }


/**
 * Test random integer.
 * 
 */
 public void testIntRandom() {
     for (int i = 0; i < 7; i++) {
         a = fac.random(kl*(i+1));
         //a = fac.random(kl*(i+1), ll+2+2*i, el, q );
         //System.out.println("a = " + a);
         assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
         assertTrue(" not isONE( a"+i+" )", !a.isONE() );
     }
 }


/**
 * Test random polynomial.
 * 
 */
 public void testPolyRandom() {
     for (int i = 0; i < 7; i++) {
         ap = mfac.random(kl+i);
         assertTrue(" not isZERO( ap"+i+" )", !ap.isZERO() );
         assertTrue(" not isONE( ap"+i+" )", !ap.isONE() );
     }
 }


/**
 * Test integer addition.
 * 
 */
 public void testIntAddition() {

     a = fac.random(kl);
     b = fac.random(kl);
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);

     c = a.sum(b);
     d = c.subtract(b);
     assertEquals("a+b-b = a",a,d);

     c = a.sum(b);
     d = b.sum(a);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);

     assertEquals("a+b = b+a",c,d);

     c = fac.random(kl);
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
 * Test polynomial addition.
 * 
 */
 public void testPolyAddition() {

     ap = mfac.random(kl);
     bp = mfac.random(kl);
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);

     cp = ap.sum(bp);
     dp = cp.subtract(bp);
     assertEquals("a+b-b = a",ap,dp);

     cp = ap.sum(bp);
     dp = bp.sum(ap);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);

     assertEquals("a+b = b+a",cp,dp);

     cp = mfac.random(kl);
     dp = cp.sum( ap.sum(bp) );
     ep = cp.sum( ap ).sum(bp);
     assertEquals("c+(a+b) = (c+a)+b",dp,ep);


     cp = ap.sum( mfac.getZERO() );
     dp = ap.subtract( mfac.getZERO() );
     assertEquals("a+0 = a-0",cp,dp);

     cp = mfac.getZERO().sum( ap );
     dp = mfac.getZERO().subtract( ap.negate() );
     assertEquals("0+a = 0+(-a)",cp,dp);
 }


/**
 * Test integer multiplication.
 * 
 */
 public void testIntMultiplication() {

     a = fac.random(kl);
     assertTrue("not isZERO( a )", !a.isZERO() );

     b = fac.random(kl);
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

     c = fac.random(kl);
     //System.out.println("c = " + c);
     d = a.multiply( b.multiply(c) );
     e = (a.multiply(b)).multiply(c);

     //System.out.println("d = " + d);
     //System.out.println("e = " + e);

     //System.out.println("d-e = " + d.subtract(c) );

     assertEquals("a(bc) = (ab)c",d,e);
     assertTrue("a(bc) = (ab)c", d.equals(e) );

     c = a.multiply( fac.getONE() );
     d = fac.getONE().multiply( a );
     assertEquals("a*1 = 1*a",c,d);

     if ( a.isUnit() ) {
        c = a.inverse();
        d = c.multiply(a);
        //System.out.println("a = " + a);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertTrue("a*1/a = 1",d.isONE()); 
     }
 }


/**
 * Test polynomial multiplication.
 * 
 */
 public void testPolyMultiplication() {

     ap = mfac.random(kl);
     assertTrue("not isZERO( a )", !ap.isZERO() );

     bp = mfac.random(kl);
     assertTrue("not isZERO( b )", !bp.isZERO() );

     cp = bp.multiply(ap);
     dp = ap.multiply(bp);
     assertTrue("not isZERO( c )", !cp.isZERO() );
     assertTrue("not isZERO( d )", !dp.isZERO() );

     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     ep = dp.subtract(cp);
     assertTrue("isZERO( a*b-b*a ) " + ep, ep.isZERO() );

     assertTrue("a*b = b*a", cp.equals(dp) );
     assertEquals("a*b = b*a",cp,dp);

     cp = mfac.random(kl);
     //System.out.println("c = " + c);
     dp = ap.multiply( bp.multiply(cp) );
     ep = (ap.multiply(bp)).multiply(cp);

     //System.out.println("d = " + d);
     //System.out.println("e = " + e);

     //System.out.println("d-e = " + d.subtract(c) );

     assertEquals("a(bc) = (ab)c",dp,ep);
     assertTrue("a(bc) = (ab)c", dp.equals(ep) );

     cp = ap.multiply( mfac.getONE() );
     dp = mfac.getONE().multiply( ap );
     assertEquals("a*1 = 1*a",cp,dp);

     if ( ap.isUnit() ) {
        cp = ap.inverse();
        dp = cp.multiply(ap);
        //System.out.println("a = " + a);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertTrue("a*1/a = 1",dp.isONE()); 
     }
 }

}
