/*
 * $Id: GCDSubresTest.java 2086 2008-08-17 18:54:36Z kredel $
 */

package edu.jas.ufd;

//import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import edu.jas.arith.BigInteger;
//import edu.jas.arith.BigRational;
//import edu.jas.arith.ModInteger;
//import edu.jas.arith.PrimeList;

//import edu.jas.poly.ExpVector;
import edu.jas.poly.TermOrder;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolyUtil;


/**
 * GCD Subresultant PRS algorithm tests with JUnit.
 * @author Heinz Kredel.
 */

public class GCDSubresTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>GCDSubresTest</CODE> object.
 * @param name String.
 */
   public GCDSubresTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GCDSubresTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   GreatestCommonDivisorAbstract<BigInteger> ufd; 

   TermOrder to = new TermOrder( TermOrder.INVLEX );

   GenPolynomialRing<BigInteger> dfac;
   GenPolynomialRing<BigInteger> cfac;
   GenPolynomialRing<GenPolynomial<BigInteger>> rfac;

   BigInteger ai;
   BigInteger bi;
   BigInteger ci;
   BigInteger di;
   BigInteger ei;

   GenPolynomial<BigInteger> a;
   GenPolynomial<BigInteger> b;
   GenPolynomial<BigInteger> c;
   GenPolynomial<BigInteger> d;
   GenPolynomial<BigInteger> e;

   GenPolynomial<GenPolynomial<BigInteger>> ar;
   GenPolynomial<GenPolynomial<BigInteger>> br;
   GenPolynomial<GenPolynomial<BigInteger>> cr;
   GenPolynomial<GenPolynomial<BigInteger>> dr;
   GenPolynomial<GenPolynomial<BigInteger>> er;

   int rl = 5; 
   int kl = 4;
   int ll = 5;
   int el = 3;
   float q = 0.3f;

   protected void setUp() {
       a = b = c = d = e = null;
       ai = bi = ci = di = ei = null;
       ar = br = cr = dr = er = null;
       ufd = new GreatestCommonDivisorPrimitive<BigInteger>();
       dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),rl,to);
       cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),rl-1,to);
       rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac,1,to);
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       ai = bi = ci = di = ei = null;
       ar = br = cr = dr = er = null;
       ufd = null;
       dfac = null;
       cfac = null;
       rfac = null;
   }


/**
 * Test base gcd subresultant.
 * 
 */
 public void testBaseGcdSubres() {

     ufd = new GreatestCommonDivisorSubres<BigInteger>();

     dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),1,to);

     for (int i = 0; i < 1; i++) {
         a = dfac.random(kl*(i+2),ll+2*i,el+2,q);
         b = dfac.random(kl*(i+2),ll+2*i,el+2,q);
         c = dfac.random(kl*(i+2),ll+2,el+2,q);
         c = c.multiply( dfac.univariate(0) );
         if ( c.isZERO() ) {
             // skip for this turn
            continue;
         }
         //a = ufd.basePrimitivePart(a);
         //b = ufd.basePrimitivePart(b);
         c = ufd.basePrimitivePart(c).abs();

         //System.out.println("a  = " + a);
         //System.out.println("b  = " + b);
         //System.out.println("c  = " + c);

         assertTrue("length( c"+i+" ) <> 0", c.length() > 0);
         //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
         //assertTrue(" not isONE( c"+i+" )", !c.isONE() );
         
         a = a.multiply(c);
         b = b.multiply(c);

         d = ufd.baseGcd(a,b);
         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("d  = " + d);
         //System.out.println("c  = " + c);
         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );

         e = PolyUtil.<BigInteger>basePseudoRemainder(a,d);
         //System.out.println("e = " + e);
         assertTrue("gcd(a,b) | a" + e, e.isZERO() );

         e = PolyUtil.<BigInteger>basePseudoRemainder(b,d);
         //System.out.println("e = " + e);
         assertTrue("gcd(a,b) | b" + e, e.isZERO() );
     }
 }


/**
 * Test recursive gcd subresultant.
 * 
 */
 public void testRecursiveGCDsubres() {

     ufd = new GreatestCommonDivisorSubres<BigInteger>();

     di = new BigInteger( 1 );
     dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),2,to);
     cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),2-1,to);
     rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac,1,to);

     for (int i = 0; i < 5; i++) {
         ar = rfac.random(kl,ll,el+i,q);
         br = rfac.random(kl,ll,el,q);
         cr = rfac.random(kl,ll,el,q);
         cr = ufd.recursivePrimitivePart(cr).abs();
         //System.out.println("ar = " + ar);
         //System.out.println("br = " + br);
         //System.out.println("cr = " + cr);

         if ( ar.isZERO() || br.isZERO() || cr.isZERO() ) {
            // skip for this turn
            continue;
         }
         assertTrue("length( cr"+i+" ) <> 0", cr.length() > 0);
         //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
         //assertTrue(" not isONE( c"+i+" )", !c.isONE() );
         
         ar = ar.multiply(cr);
         br = br.multiply(cr);
         //System.out.println("ar = " + ar);
         //System.out.println("br = " + br);
         //System.out.println("cr = " + cr);

         dr = ufd.recursiveUnivariateGcd(ar,br);
         //System.out.println("dr = " + dr);

         er = PolyUtil.<BigInteger>recursivePseudoRemainder(dr,cr);
         //System.out.println("er = " + er);
         assertTrue("c | gcd(ac,bc) " + er, er.isZERO() );

         er = PolyUtil.<BigInteger>recursivePseudoRemainder(ar,dr);
         //System.out.println("er = " + er);
         assertTrue("gcd(a,b) | a" + er, er.isZERO() );

         er = PolyUtil.<BigInteger>recursivePseudoRemainder(br,dr);
         //System.out.println("er = " + er);
         assertTrue("gcd(a,b) | b" + er, er.isZERO() );
     }
 }


/**
 * Test arbitrary recursive gcd subresultant.
 * 
 */
 public void testArbitraryRecursiveGCDsubres() {

     ufd = new GreatestCommonDivisorSubres<BigInteger>();

     di = new BigInteger( 1 );
     dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),2,to);
     cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),2-1,to);
     rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac,1,to);

     for (int i = 0; i < 5; i++) {
         ar = rfac.random(kl,ll,el+i,q);
         br = rfac.random(kl,ll,el,q);
         cr = rfac.random(kl,ll,el,q);
         cr = ufd.recursivePrimitivePart(cr).abs();
         //System.out.println("ar = " + ar);
         //System.out.println("br = " + br);
         //System.out.println("cr = " + cr);

         if ( ar.isZERO() || br.isZERO() || cr.isZERO() ) {
            // skip for this turn
            continue;
         }
         assertTrue("length( cr"+i+" ) <> 0", cr.length() > 0);
         //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
         //assertTrue(" not isONE( c"+i+" )", !c.isONE() );
         
         ar = ar.multiply(cr);
         br = br.multiply(cr);
         //System.out.println("ar = " + ar);
         //System.out.println("br = " + br);
         //System.out.println("cr = " + cr);

         dr = ufd.recursiveGcd(ar,br);
         //System.out.println("dr = " + dr);

         er = PolyUtil.<BigInteger>recursivePseudoRemainder(dr,cr);
         //System.out.println("er = " + er);
         assertTrue("c | gcd(ac,bc) " + er, er.isZERO() );

         er = PolyUtil.<BigInteger>recursivePseudoRemainder(ar,dr);
         //System.out.println("er = " + er);
         assertTrue("gcd(a,b) | a" + er, er.isZERO() );

         er = PolyUtil.<BigInteger>recursivePseudoRemainder(br,dr);
         //System.out.println("er = " + er);
         assertTrue("gcd(a,b) | b" + er, er.isZERO() );
     }
 }


/**
 * Test gcd subresultant.
 * 
 */
 public void testGCDsubres() {

     //GreatestCommonDivisorAbstract<BigInteger> ufd_pp; 
     //ufd_pp = ufd;

     ufd = new GreatestCommonDivisorSubres<BigInteger>();

     dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),5,to);

     for (int i = 0; i < 2; i++) {
         a = dfac.random(kl,ll,el,q);
         b = dfac.random(kl,ll,el,q);
         c = dfac.random(kl,ll,el,q);
         c = c.multiply( dfac.univariate(0) );
         c = ufd.primitivePart(c).abs();
         //System.out.println("a = " + a);
         //System.out.println("b = " + b);
         //System.out.println("c = " + c);

         if ( a.isZERO() || b.isZERO() || c.isZERO() ) {
            // skip for this turn
            continue;
         }
         assertTrue("length( c"+i+" ) <> 0", c.length() > 0);
         //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
         //assertTrue(" not isONE( c"+i+" )", !c.isONE() );
         
         a = a.multiply(c);
         b = b.multiply(c);
         //System.out.println("a = " + a);
         //System.out.println("b = " + b);
         //System.out.println("c = " + c);

         d = ufd.gcd(a,b);
         //System.out.println("c = " + c);
         //System.out.println("d = " + d);

         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("e = " + e);
         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );

         e = PolyUtil.<BigInteger>basePseudoRemainder(a,d);
         //System.out.println("e = " + e);
         assertTrue("gcd(a,b) | a " + e, e.isZERO() );

         e = PolyUtil.<BigInteger>basePseudoRemainder(b,d);
         //System.out.println("e = " + e);
         assertTrue("gcd(a,b) | b " + e, e.isZERO() );
     }
 }


/**
 * Test base subresultant.
 * 
 */
 public void testBaseSubresultant() {

     GreatestCommonDivisorSubres<BigInteger> ufd 
        = new GreatestCommonDivisorSubres<BigInteger>();

     dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),1,to);

     for (int i = 0; i < 1; i++) {
         a = dfac.random(kl*(i+2),ll+2*i,el+2,q);
         b = dfac.random(kl*(i+2),ll+2*i,el+2,q);
         c = dfac.random(kl,ll,2,q);
         //c = c.multiply( cfac.univariate(0) );
         //c = dfac.getONE();
         if ( c.isZERO() ) {
             // skip for this turn
            continue;
         }
         //System.out.println("a  = " + a);
         //System.out.println("b  = " + b);
         //System.out.println("c  = " + c);

         assertTrue("length( c"+i+" ) <> 0", c.length() > 0);
         //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
         //assertTrue(" not isONE( c"+i+" )", !c.isONE() );
         
         a = a.multiply(c);
         b = b.multiply(c);

         d = ufd.baseResultant(a,b);
         e = ufd.baseGcd(a,b);
         //System.out.println("d  = " + d);
         //System.out.println("c  = " + c);
         //System.out.println("e  = " + e);
         if ( !e.isConstant() ) {
            assertTrue("res(a,b) == 0 " + d, d.isZERO() );
         } else {
            assertTrue("res(a,b) != 0 " + d, !d.isZERO() );
         }
     }
 }


/**
 * Test recursive subresultant.
 * 
 */
 public void testRecursiveSubresultant() {

     GreatestCommonDivisorSubres<BigInteger> ufd 
        = new GreatestCommonDivisorSubres<BigInteger>();

     di = new BigInteger( 1 );
     dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),2,to);
     cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),2-1,to);
     rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac,1,to);

     for (int i = 0; i < 5; i++) {
         ar = rfac.random(kl,ll,el+i,q);
         br = rfac.random(kl,ll,el,q);
         cr = rfac.random(kl,ll,2,q);
         //cr = rfac.getONE(); 
         //cr = ufd.recursivePrimitivePart(cr).abs();
         //cr = cr.multiply( rfac.univariate(0) );
         //System.out.println("ar = " + ar);
         //System.out.println("br = " + br);
         //System.out.println("cr = " + cr);

         if ( ar.isZERO() || br.isZERO() || cr.isZERO() ) {
            // skip for this turn
            continue;
         }
         assertTrue("length( cr"+i+" ) <> 0", cr.length() > 0);
         //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
         //assertTrue(" not isONE( c"+i+" )", !c.isONE() );
         
         ar = ar.multiply(cr);
         br = br.multiply(cr);
         //System.out.println("ar = " + ar);
         //System.out.println("br = " + br);

         dr = ufd.recursiveResultant(ar,br);
         //System.out.println("cr = " + cr);
         //System.out.println("dr = " + dr);
         er = ufd.recursiveUnivariateGcd(ar,br);
         //System.out.println("er = " + er);

         if ( er.isZERO() ) { // cannot happen since a, b, c != 0
            assertTrue("res(a,b) = 0 " + dr + " e = " + er, dr.isZERO() );
         }
         if ( er.isConstant() && er.leadingBaseCoefficient().isConstant() ) {
            assertTrue("res(a,b) != 0 " + dr 
                       + ", e = " + er + ", a = " + ar + ", b = " + br
                       , !dr.isZERO() );
         } else {
            assertTrue("res(a,b) = 0 or not const " + dr 
                       + ", e = " + er + ", a = " + ar + ", b = " + br
                       , dr.isZERO() || !dr.isConstant() || !dr.leadingBaseCoefficient().isConstant() );
         }

     }
 }


/**
 * Test subresultant.
 * 
 */
 public void testSubres() {

     GreatestCommonDivisorSubres<BigInteger> ufd 
        = new GreatestCommonDivisorSubres<BigInteger>();

     dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),3,to);

     for (int i = 0; i < 2; i++) {
         a = dfac.random(kl,ll,el,q);
         b = dfac.random(kl,ll,el,q);
         c = dfac.random(kl,ll,2,q);
         //c = dfac.getONE();
         //c = c.multiply( dfac.univariate(0) );
         //c = ufd.primitivePart(c).abs();
         //System.out.println("a = " + a);
         //System.out.println("b = " + b);
         //System.out.println("c = " + c);

         if ( a.isZERO() || b.isZERO() || c.isZERO() ) {
            // skip for this turn
            continue;
         }
         assertTrue("length( c"+i+" ) <> 0", c.length() > 0);
         //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
         //assertTrue(" not isONE( c"+i+" )", !c.isONE() );
         
         a = a.multiply(c);
         b = b.multiply(c);
         //System.out.println("a = " + a);
         //System.out.println("b = " + b);

         d = ufd.resultant(a,b);
         e = ufd.gcd(a,b);
         //System.out.println("c = " + c);
         //System.out.println("d = " + d);
         //System.out.println("e = " + e);

         if ( e.isZERO() ) { // cannot happen since a, b, c != 0
            assertTrue("res(a,b) = 0 " + d + " e = " + e, d.isZERO() );
         }
         if ( e.isConstant() ) {
            assertTrue("res(a,b) != 0 " + d 
                       + ", e = " + e + ", a = " + a + ", b = " + b
                       , !d.isZERO() );
         } else {
            assertTrue("res(a,b) = 0 or not const " + d 
                       + ", e = " + e + ", a = " + a + ", b = " + b
                       , d.isZERO() || !d.isConstant() );
         }

     }
 }

}
