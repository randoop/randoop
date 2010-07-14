/*
 * $Id: GCDModEvalTest.java 1386 2007-09-30 17:36:50Z kredel $
 */

package edu.jas.ufd;

//import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//import edu.jas.arith.BigInteger;
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
 * GCD Modular Evaluation algorithm tests with JUnit.
 * @author Heinz Kredel.
 */

public class GCDModEvalTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>GCDModEvalTest</CODE> object.
 * @param name String.
 */
   public GCDModEvalTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GCDModEvalTest.class);
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

   GenPolynomial<GenPolynomial<ModInteger>> ar;
   GenPolynomial<GenPolynomial<ModInteger>> br;
   GenPolynomial<GenPolynomial<ModInteger>> cr;
   GenPolynomial<GenPolynomial<ModInteger>> dr;
   GenPolynomial<GenPolynomial<ModInteger>> er;

   int rl = 3; 
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
       mi = null;
       ufd = null;
       dfac = null;
       cfac = null;
       rfac = null;
   }


/**
 * Test modular evaluation gcd.
 * 
 */
 public void testModEvalGcd() {

     GreatestCommonDivisorAbstract<ModInteger> ufd_me
        = new GreatestCommonDivisorModEval();

     for (int i = 0; i < 1; i++) {
         a = dfac.random(kl*(i+2),ll+2*i,el+0*i,q);
         b = dfac.random(kl*(i+2),ll+2*i,el+0*i,q);
         c = dfac.random(kl*(i+2),ll+2*i,el+0*i,q);
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

         d = ufd_me.gcd(a,b);

         c = ufd.basePrimitivePart(c).abs();
         e = PolyUtil.<ModInteger>basePseudoRemainder(d,c);
         //System.out.println("c  = " + c);
         //System.out.println("d  = " + d);
         assertTrue("c | gcd(ac,bc) " + e, e.isZERO() );

         e = PolyUtil.<ModInteger>basePseudoRemainder(a,d);
         //System.out.println("e = " + e);
         assertTrue("gcd(a,b) | a" + e, e.isZERO() );

         e = PolyUtil.<ModInteger>basePseudoRemainder(b,d);
         //System.out.println("e = " + e);
         assertTrue("gcd(a,b) | b" + e, e.isZERO() );
     }
 }

}
