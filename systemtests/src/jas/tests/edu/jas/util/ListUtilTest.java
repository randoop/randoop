/*
 * $Id: ListUtilTest.java 2218 2008-11-16 13:47:25Z kredel $
 */

package edu.jas.util;


import java.util.List;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import edu.jas.structure.RingElem;
import edu.jas.structure.UnaryFunctor;

import edu.jas.arith.BigInteger;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;
import edu.jas.arith.BigRational;
import edu.jas.arith.BigComplex;

import edu.jas.poly.TermOrder;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;


/**
 * ListUtil tests with JUnit.
 * @author Heinz Kredel.
 */

public class ListUtilTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>ListUtilTest</CODE> object.
 * @param name String.
 */
   public ListUtilTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(ListUtilTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

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
   int kl = 5;
   int ll = 5;
   int el = 3;
   float q = 0.3f;

   protected void setUp() {
       a = b = c = d = e = null;
       ai = bi = ci = di = ei = null;
       ar = br = cr = dr = er = null;
       dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),rl,to);
       cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),rl-1,to);
       rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac,1,to);
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       ai = bi = ci = di = ei = null;
       ar = br = cr = dr = er = null;
       dfac = null;
       cfac = null;
       rfac = null;
   }


/**
 * Test list map.
 * 
 */
 public void testListMap() {
     ai = new BigInteger();
     List<BigInteger> list = new ArrayList<BigInteger>();
     for ( int i = 0; i < 10; i++ ) {
         list.add( ai.random(7) );
     }
     bi = ai.getONE();
     List<BigInteger> nl;
     nl = ListUtil.<BigInteger,BigInteger>map( list, new Multiply<BigInteger>(bi) );
     assertEquals("list == nl ",list,nl);

     List<GenPolynomial<BigInteger>> plist 
        = new ArrayList<GenPolynomial<BigInteger>>();
     for ( int i = 0; i < 10; i++ ) {
         plist.add( dfac.random(7) );
     }
     b = dfac.getONE();
     List<GenPolynomial<BigInteger>> pnl;
     pnl = ListUtil.<GenPolynomial<BigInteger>,GenPolynomial<BigInteger>>map( plist, 
                    new Multiply<GenPolynomial<BigInteger>>(b) );
     assertEquals("plist == pnl ",plist,pnl);
 }

}


/**
 * Internal scalar multiplication functor.
 */
class Multiply<C extends RingElem<C>> implements UnaryFunctor<C,C> {
        C x;
        public Multiply(C x) {
            this.x = x;
        }
        public C eval(C c) {
            return c.multiply(x);
        }
}
