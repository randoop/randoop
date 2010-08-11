/*
 * $Id: GCDTimingTest.java 2085 2008-08-17 17:53:37Z kredel $
 */

package edu.jas.ufd;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

import edu.jas.arith.BigInteger;
//import edu.jas.arith.BigRational;
//import edu.jas.arith.ModInteger;

//import edu.jas.poly.ExpVector;
import edu.jas.poly.TermOrder;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolyUtil;


/**
 * GreatestCommonDivisor timing tests with JUnit.
 * @author Heinz Kredel.
 */

public class GCDTimingTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          //BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>GCDTimingTest</CODE> object.
 * @param name String.
 */
   public GCDTimingTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GCDTimingTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   GreatestCommonDivisorAbstract<BigInteger> ufd_si; 
   GreatestCommonDivisorAbstract<BigInteger> ufd_pp; 
   GreatestCommonDivisorSubres<BigInteger>   ufd_sr; // because of non sparse pseudo remainder
   GreatestCommonDivisorAbstract<BigInteger> ufd_mosi; 
   GreatestCommonDivisorAbstract<BigInteger> ufd_moevsi; 

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
       ufd_si     = new GreatestCommonDivisorSimple<BigInteger>();
       ufd_pp     = new GreatestCommonDivisorPrimitive<BigInteger>();
       ufd_sr     = new GreatestCommonDivisorSubres<BigInteger>();
       ufd_mosi   = new GreatestCommonDivisorModular(true);
       ufd_moevsi = new GreatestCommonDivisorModular();
       dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),rl,to);
       cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),rl-1,to);
       rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac,1,to);
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       ai = bi = ci = di = ei = null;
       ar = br = cr = dr = er = null;
       ufd_si = null;
       ufd_pp = null;
       ufd_sr = null;
       dfac = null;
       cfac = null;
       rfac = null;
   }


/**
 * Test dummy for junit.
 * 
 */
 public void testDummy() {
     assertTrue("ufd_pp != null", ufd_pp != null);
 }


/**
 * Test base gcd simple.
 * 
 */
 public void xtestBaseGcd() {

     dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),1,to);

     long t;

     for (int i = 0; i < 10; i++) {
         a = dfac.random(kl*(i+2),ll+2*i,el+2*i,q);
         b = dfac.random(kl*(i+2),ll+2*i,el+2*i,q);
         c = dfac.random(kl*(i+2),ll+2*i,el+2*i,q);
         c = c.multiply( dfac.univariate(0) );
         //a = ufd.basePrimitivePart(a);
         //b = ufd.basePrimitivePart(b);
         //c = ufd.basePrimitivePart(c).abs();

         //System.out.println("a  = " + a);
         //System.out.println("b  = " + b);
         //System.out.println("c  = " + c);

         if ( a.isZERO() || b.isZERO() || c.isZERO() ) {
            // skip for this turn
            continue;
         }
         assertTrue("length( c"+i+" ) <> 0", c.length() > 0);
         //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
         //assertTrue(" not isONE( c"+i+" )", !c.isONE() );
         
         a = a.multiply(c);
         b = b.multiply(c);

         System.out.println("\ndegrees: a = " + a.degree() 
                                   + ", b = " + b.degree()  
                                   + ", c = " + c.degree());  
         /*
         t = System.currentTimeMillis();
         d = ufd_si.baseGcd(a,b);
         t = System.currentTimeMillis() - t;
         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("d  = " + d);

         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );
         System.out.println("simple prs        time = " + t);
         */

         t = System.currentTimeMillis();
         d = ufd_pp.baseGcd(a,b);
         t = System.currentTimeMillis() - t;
         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("d  = " + d);

         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );
         System.out.println("primitive prs     time = " + t);


         t = System.currentTimeMillis();
         d = ufd_sr.baseGcd(a,b);
         t = System.currentTimeMillis() - t;
         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("d  = " + d);

         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );
         System.out.println("subsresultant prs time = " + t);
     }
 }




/**
 * Test recursive gcd.
 * 
 */
 public void xtestRecursiveGCD() {

     cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),2-1,to);
     rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac,1,to);

     long t;

     for (int i = 0; i < 5; i++) {
         ar = rfac.random(kl,ll,el+i,q);
         br = rfac.random(kl,ll,el+i,q);
         cr = rfac.random(kl,ll,el,q);
         cr = cr.multiply( rfac.univariate(0) );
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

         System.out.println("\ndegrees: a = " + ar.degree() 
                                   + ", b = " + br.degree()  
                                   + ", c = " + cr.degree());  

         t = System.currentTimeMillis();
         dr = ufd_si.recursiveUnivariateGcd(ar,br);
         t = System.currentTimeMillis() - t;
         //System.out.println("dr = " + dr);

         //er = PolyUtil.<BigInteger>recursivePseudoRemainder(dr,cr);
         //System.out.println("er = " + er);

         //assertTrue("c | gcd(ac,bc) " + er, er.isZERO() );
         System.out.println("simple prs        time = " + t);
         /*
         */

         t = System.currentTimeMillis();
         dr = ufd_pp.recursiveUnivariateGcd(ar,br);
         t = System.currentTimeMillis() - t;
         //System.out.println("dr = " + dr);

         er = PolyUtil.<BigInteger>recursivePseudoRemainder(dr,cr);
         //System.out.println("er = " + er);

         assertTrue("c | gcd(ac,bc) " + er, er.isZERO() );
         System.out.println("primitive prs     time = " + t);


         t = System.currentTimeMillis();
         dr = ufd_sr.recursiveUnivariateGcd(ar,br);
         t = System.currentTimeMillis() - t;
         //System.out.println("dr = " + dr);

         er = ufd_sr.recursivePseudoRemainder(dr,cr);
         //System.out.println("er = " + er);

         assertTrue("c | gcd(ac,bc) " + er, er.isZERO() );
         System.out.println("subresultant prs  time = " + t);
     }
 }


/**
 * Test gcd.
 * 
 */
 public void xtestGCD() {

     long t;

     dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),3,to);

     for (int i = 0; i < 5; i++) {
         a = dfac.random(kl+i*30,ll+i,2*el,q);
         b = dfac.random(kl+i*30,ll+i,2*el,q);
         c = dfac.random(kl,ll,el,q);
         //c = dfac.getONE();
         //c = c.multiply( dfac.univariate(0) ).multiply( dfac.univariate(4) );
         //c = c.multiply( dfac.univariate(0) );
         c = ufd_pp.primitivePart(c).abs();
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

         System.out.println("\ndegrees: a = " + a.degree() 
                                   + ", b = " + b.degree()  
                                   + ", c = " + c.degree());  
         /*
         t = System.currentTimeMillis();
         d = ufd_si.gcd(a,b);
         t = System.currentTimeMillis() - t;
         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("d  = " + d);

         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );
         System.out.println("simple prs        time = " + t);
         */
         /*
         t = System.currentTimeMillis();
         d = ufd_pp.gcd(a,b);
         t = System.currentTimeMillis() - t;
         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("d  = " + d);

         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );
         System.out.println("primitive prs     time = " + t);
         */
         
         t = System.currentTimeMillis();
         d = ufd_sr.gcd(a,b);
         t = System.currentTimeMillis() - t;
         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("d  = " + d);

         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );
         System.out.println("subsresultant prs time = " + t);
         
         
         t = System.currentTimeMillis();
         d = ufd_mosi.gcd(a,b);
         t = System.currentTimeMillis() - t;
         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("d  = " + d);

         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );
         System.out.println("modular simple    time = " + t);
         

         t = System.currentTimeMillis();
         d = ufd_moevsi.gcd(a,b);
         t = System.currentTimeMillis() - t;
         e = PolyUtil.<BigInteger>basePseudoRemainder(d,c);
         //System.out.println("d  = " + d);

         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );
         System.out.println("modular eval      time = " + t);
         }
     }

}
