/*
 * $Id: GCDModularTest.java 2086 2008-08-17 18:54:36Z kredel $
 */

package edu.jas.ufd;

//import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import edu.jas.arith.BigInteger;
//import edu.jas.arith.BigRational;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;
import edu.jas.arith.PrimeList;

//import edu.jas.poly.ExpVector;
import edu.jas.poly.TermOrder;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolyUtil;


/**
 * GCD Modular algorithm tests with JUnit.
 * @author Heinz Kredel.
 */

public class GCDModularTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>GCDModularTest</CODE> object.
 * @param name String.
 */
   public GCDModularTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GCDModularTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   GreatestCommonDivisorAbstract<ModInteger> ufd; 

   TermOrder to = new TermOrder( TermOrder.INVLEX );

   GenPolynomialRing<ModInteger> dfac;
   GenPolynomialRing<ModInteger> cfac;
   GenPolynomialRing<GenPolynomial<ModInteger>> rfac;

   PrimeList primes = new PrimeList();
   ModIntegerRing mi;

   ModInteger ai;
   ModInteger bi;
   ModInteger ci;
   ModInteger di;
   ModInteger ei;

   GenPolynomial<ModInteger> a;
   GenPolynomial<ModInteger> b;
   GenPolynomial<ModInteger> c;
   GenPolynomial<ModInteger> d;
   GenPolynomial<ModInteger> e;
   GenPolynomial<ModInteger> ac;
   GenPolynomial<ModInteger> bc;

   GenPolynomial<GenPolynomial<ModInteger>> ar;
   GenPolynomial<GenPolynomial<ModInteger>> br;
   GenPolynomial<GenPolynomial<ModInteger>> cr;
   GenPolynomial<GenPolynomial<ModInteger>> dr;
   GenPolynomial<GenPolynomial<ModInteger>> er;
   GenPolynomial<GenPolynomial<ModInteger>> arc;
   GenPolynomial<GenPolynomial<ModInteger>> brc;

   int rl = 5; 
   int kl = 4;
   int ll = 5;
   int el = 3;
   float q = 0.3f;

   protected void setUp() {
       a = b = c = d = e = null;
       ai = bi = ci = di = ei = null;
       ar = br = cr = dr = er = null;
       mi = new ModIntegerRing(primes.get(0),true);
       ufd = new GreatestCommonDivisorPrimitive<ModInteger>();
       dfac = new GenPolynomialRing<ModInteger>(mi,rl,to);
       cfac = new GenPolynomialRing<ModInteger>(mi,rl-1,to);
       rfac = new GenPolynomialRing<GenPolynomial<ModInteger>>(cfac,1,to);
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
 * Test modular algorithm gcd with modular evaluation recursive algorithm.
 * 
 */
 public void testModularEvaluationGcd() {

     GreatestCommonDivisorAbstract<BigInteger> ufd_m
        = new GreatestCommonDivisorModular(/*false*/);

     GreatestCommonDivisorAbstract<BigInteger> ufd
        = new GreatestCommonDivisorPrimitive<BigInteger>();

     GenPolynomial<BigInteger> a;
     GenPolynomial<BigInteger> b;
     GenPolynomial<BigInteger> c;
     GenPolynomial<BigInteger> d;
     GenPolynomial<BigInteger> e;

     GenPolynomialRing<BigInteger> dfac 
         = new GenPolynomialRing<BigInteger>(new BigInteger(),3,to);

     for (int i = 0; i < 2; i++) {
         a = dfac.random(kl,ll+i,el+i,q);
         b = dfac.random(kl,ll+i,el+i,q);
         c = dfac.random(kl,ll+i,el+i,q);
         c = c.multiply( dfac.univariate(0) );
         //a = ufd.basePrimitivePart(a);
         //b = ufd.basePrimitivePart(b);

         if ( a.isZERO() || b.isZERO() || c.isZERO() ) {
            // skip for this turn
            continue;
         }
         assertTrue("length( c"+i+" ) <> 0", c.length() > 0);
         //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
         //assertTrue(" not isONE( c"+i+" )", !c.isONE() );
         
         a = a.multiply(c);
         b = b.multiply(c);

         //System.out.println("a  = " + a);
         //System.out.println("b  = " + b);

         d = ufd_m.gcd(a,b);

         c = ufd.basePrimitivePart(c).abs();
         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("c  = " + c);
         //System.out.println("d  = " + d);

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
 * Test modular algorithm gcd with simple PRS recursive algorithm.
 * 
 */
 public void testModularSimpleGcd() {

     GreatestCommonDivisorAbstract<BigInteger> ufd_m
        = new GreatestCommonDivisorModular(true);

     GreatestCommonDivisorAbstract<BigInteger> ufd
        = new GreatestCommonDivisorPrimitive<BigInteger>();

     GenPolynomial<BigInteger> a;
     GenPolynomial<BigInteger> b;
     GenPolynomial<BigInteger> c;
     GenPolynomial<BigInteger> d;
     GenPolynomial<BigInteger> e;

     GenPolynomialRing<BigInteger> dfac 
         = new GenPolynomialRing<BigInteger>(new BigInteger(),3,to);

     for (int i = 0; i < 1; i++) {
         a = dfac.random(kl,ll+i,el+i,q);
         b = dfac.random(kl,ll+i,el+i,q);
         c = dfac.random(kl,ll+i,el+i,q);
         c = c.multiply( dfac.univariate(0) );
         //a = ufd.basePrimitivePart(a);
         //b = ufd.basePrimitivePart(b);

         if ( a.isZERO() || b.isZERO() || c.isZERO() ) {
            // skip for this turn
            continue;
         }
         assertTrue("length( c"+i+" ) <> 0", c.length() > 0);
         //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
         //assertTrue(" not isONE( c"+i+" )", !c.isONE() );
         
         a = a.multiply(c);
         b = b.multiply(c);

         //System.out.println("a  = " + a);
         //System.out.println("b  = " + b);

         d = ufd_m.gcd(a,b);

         c = ufd.basePrimitivePart(c).abs();
         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("c  = " + c);
         //System.out.println("d  = " + d);

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
 * Test recursive content and primitive part, modular coefficients.
 * 
 */
 public void testRecursiveContentPPmodular() {

     dfac = new GenPolynomialRing<ModInteger>(mi,2,to);
     cfac = new GenPolynomialRing<ModInteger>(mi,2-1,to);
     rfac = new GenPolynomialRing<GenPolynomial<ModInteger>>(cfac,1,to);

     GreatestCommonDivisorAbstract<ModInteger> ufd 
          = new GreatestCommonDivisorPrimitive<ModInteger>();

     for (int i = 0; i < 1; i++) {
         a = cfac.random(kl,ll+2*i,el+i,q).monic();
         cr = rfac.random(kl*(i+2),ll+2*i,el+i,q);
         cr = PolyUtil.<ModInteger>monic( cr );
         //System.out.println("a  = " + a);
         //System.out.println("cr = " + cr);
           //a = ufd.basePrimitivePart(a);
           //b = distribute(dfac,cr);
           //b = ufd.basePrimitivePart(b);
           //cr = recursive(rfac,b);
           //System.out.println("a  = " + a);
           //System.out.println("cr = " + cr);

         cr = cr.multiply(a);
         //System.out.println("cr = " + cr);

        if ( cr.isZERO() ) {
           // skip for this turn
           continue;
         }
         assertTrue("length( cr"+i+" ) <> 0", cr.length() > 0);
         //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
         //assertTrue(" not isONE( c"+i+" )", !c.isONE() );
         
         c = ufd.recursiveContent(cr).monic();
         dr =ufd.recursivePrimitivePart(cr);
         dr = PolyUtil.<ModInteger>monic( dr );
         //System.out.println("c  = " + c);
         //System.out.println("dr = " + dr);

         //System.out.println("monic(a) = " + a.monic());
         //System.out.println("monic(c) = " + c.monic());

         ar = dr.multiply(c);
         //System.out.println("ar = " + ar);
         assertEquals("c == cont(c)pp(c)", cr, ar );
     }
 }


/**
 * Test base gcd modular coefficients. 
 * 
 */
 public void testGCDbaseModular() {

     dfac = new GenPolynomialRing<ModInteger>(mi,1,to);

     GreatestCommonDivisorAbstract<ModInteger> ufd 
          = new GreatestCommonDivisorPrimitive<ModInteger>();

     for (int i = 0; i < 1; i++) {
         a = dfac.random(kl,ll,el+3+i,q).monic();
         b = dfac.random(kl,ll,el+3+i,q).monic();
         c = dfac.random(kl,ll,el+3+i,q).monic();
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
         
         ac = a.multiply(c);
         bc = b.multiply(c);
         //System.out.println("ac = " + ac);
         //System.out.println("bc = " + bc);

         //e = PolyUtil.<ModInteger>basePseudoRemainder(ac,c);
         //System.out.println("ac/c a = 0 " + e);
         //assertTrue("ac/c-a != 0 " + e, e.isZERO() );
         //e = PolyUtil.<ModInteger>basePseudoRemainder(bc,c);
         //System.out.println("bc/c-b = 0 " + e);
         //assertTrue("bc/c-b != 0 " + e, e.isZERO() );

         d = ufd.baseGcd(ac,bc);
         d = d.monic(); // not required
         //System.out.println("d = " + d);

         e = PolyUtil.<ModInteger>basePseudoRemainder(d,c);
         //System.out.println("e = " + e);

         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );
     }
 }


/**
 * Test recursive gcd modular coefficients. 
 * 
 */
 public void testRecursiveGCDModular() {

     dfac = new GenPolynomialRing<ModInteger>(mi,2,to);
     cfac = new GenPolynomialRing<ModInteger>(mi,2-1,to);
     rfac = new GenPolynomialRing<GenPolynomial<ModInteger>>(cfac,1,to);

     //     GreatestCommonDivisorAbstract<ModInteger> ufd 
     //     = new GreatestCommonDivisorPrimitive<ModInteger>();

     for (int i = 0; i < 1; i++) {
         ar = rfac.random(kl,2,el+2,q);
         br = rfac.random(kl,2,el+2,q);
         cr = rfac.random(kl,2,el+2,q);
         ar = PolyUtil.<ModInteger>monic( ar );
         br = PolyUtil.<ModInteger>monic( br );
         cr = PolyUtil.<ModInteger>monic( cr );
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
         
         arc = ar.multiply(cr);
         brc = br.multiply(cr);
         //System.out.println("arc = " + arc);
         //System.out.println("brc = " + brc);

          //er = PolyUtil.<ModInteger>recursivePseudoRemainder(arc,cr);
          //System.out.println("ac/c-a = 0 " + er);
          //assertTrue("ac/c-a != 0 " + er, er.isZERO() );
          //er = PolyUtil.<ModInteger>recursivePseudoRemainder(brc,cr);
          //System.out.println("bc/c-b = 0 " + er);
          //assertTrue("bc/c-b != 0 " + er, er.isZERO() );

         dr = ufd.recursiveUnivariateGcd(arc,brc);
         dr = PolyUtil.<ModInteger>monic( dr );
         //System.out.println("cr = " + cr);
         //System.out.println("dr = " + dr);

         er = PolyUtil.<ModInteger>recursivePseudoRemainder(dr,cr);
         //System.out.println("er = " + er);

         assertTrue("c | gcd(ac,bc) " + er, er.isZERO() );
     }
 }


/**
 * Test arbitrary recursive gcd modular coefficients. 
 * 
 */
 public void testArbitraryRecursiveGCDModular() {

     dfac = new GenPolynomialRing<ModInteger>(mi,2,to);
     cfac = new GenPolynomialRing<ModInteger>(mi,2-1,to);
     rfac = new GenPolynomialRing<GenPolynomial<ModInteger>>(cfac,1,to);

     //     GreatestCommonDivisorAbstract<ModInteger> ufd 
     //     = new GreatestCommonDivisorPrimitive<ModInteger>();

     for (int i = 0; i < 1; i++) {
         ar = rfac.random(kl,2,el+2,q);
         br = rfac.random(kl,2,el+2,q);
         cr = rfac.random(kl,2,el+2,q);
         ar = PolyUtil.<ModInteger>monic( ar );
         br = PolyUtil.<ModInteger>monic( br );
         cr = PolyUtil.<ModInteger>monic( cr );
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
         
         arc = ar.multiply(cr);
         brc = br.multiply(cr);
         //System.out.println("arc = " + arc);
         //System.out.println("brc = " + brc);

          //er = PolyUtil.<ModInteger>recursivePseudoRemainder(arc,cr);
          //System.out.println("ac/c-a = 0 " + er);
          //assertTrue("ac/c-a != 0 " + er, er.isZERO() );
          //er = PolyUtil.<ModInteger>recursivePseudoRemainder(brc,cr);
          //System.out.println("bc/c-b = 0 " + er);
          //assertTrue("bc/c-b != 0 " + er, er.isZERO() );

         dr = ufd.recursiveGcd(arc,brc);
         dr = PolyUtil.<ModInteger>monic( dr );
         //System.out.println("cr = " + cr);
         //System.out.println("dr = " + dr);

         er = PolyUtil.<ModInteger>recursivePseudoRemainder(dr,cr);
         //System.out.println("er = " + er);

         assertTrue("c | gcd(ac,bc) " + er, er.isZERO() );
     }
 }


/**
 * Test gcd modular coefficients.
 * 
 */
 public void testGcdModular() {

     dfac = new GenPolynomialRing<ModInteger>(mi,4,to);

     for (int i = 0; i < 1; i++) {
         a = dfac.random(kl,ll,el+i,q).monic();
         b = dfac.random(kl,ll,el+i,q).monic();
         c = dfac.random(kl,ll,el+i,q).monic();
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
         
         ac = a.multiply(c);
         bc = b.multiply(c);
         //System.out.println("ac = " + ac);
         //System.out.println("bc = " + bc);

         //e = PolyUtil.<ModInteger>basePseudoRemainder(ac,c);
         //System.out.println("ac/c-a = 0 " + e);
         //assertTrue("ac/c-a != 0 " + e, e.isZERO() );
         //e = PolyUtil.<ModInteger>basePseudoRemainder(bc,c);
         //System.out.println("bc/c-b = 0 " + e);
         //assertTrue("bc/c-b != 0 " + e, e.isZERO() );

         d = ufd.gcd(ac,bc);
         //System.out.println("d = " + d);
         e = PolyUtil.<ModInteger>basePseudoRemainder(d,c);
         //System.out.println("e = " + e);
         //System.out.println("c = " + c);
         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );

         e = PolyUtil.<ModInteger>basePseudoRemainder(ac,d);
         //System.out.println("gcd(ac,bc) | ac " + e);
         assertTrue("gcd(ac,bc) | ac " + e, e.isZERO() );
         e = PolyUtil.<ModInteger>basePseudoRemainder(bc,d);
         //System.out.println("gcd(ac,bc) | bc " + e);
         assertTrue("gcd(ac,bc) | bc " + e, e.isZERO() );
     }
 }

}
