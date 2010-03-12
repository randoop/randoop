/*
 * $Id: SolvableGroebnerBaseTest.java 1889 2008-07-12 13:38:05Z kredel $
 */

package edu.jas.ring;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

import edu.jas.arith.BigRational;
import edu.jas.poly.GenSolvablePolynomial;
import edu.jas.poly.GenSolvablePolynomialRing;
import edu.jas.poly.PolynomialList;
import edu.jas.poly.RelationTable;
import edu.jas.poly.TermOrder;
import edu.jas.poly.WeylRelations;

/**
 * Solvable Groebner base sequential tests with JUnit.
 * @author Heinz Kredel.
 */

public class SolvableGroebnerBaseTest extends TestCase {

    //private static final Logger logger = Logger.getLogger(SolvableGroebnerBaseTest.class);

/**
 * main.
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>SolvableGroebnerBaseTest</CODE> object.
 * @param name String.
 */
   public SolvableGroebnerBaseTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(SolvableGroebnerBaseTest.class);
     return suite;
   }

   int port = 4711;
   String host = "localhost";

   GenSolvablePolynomial<BigRational> a;
   GenSolvablePolynomial<BigRational> b;
   GenSolvablePolynomial<BigRational> c;
   GenSolvablePolynomial<BigRational> d;
   GenSolvablePolynomial<BigRational> e;

   List<GenSolvablePolynomial<BigRational>> L;
   PolynomialList<BigRational> F;
   PolynomialList<BigRational> G;

   GenSolvablePolynomialRing<BigRational> ring;

   SolvableGroebnerBase<BigRational> sbb;

   BigRational cfac;
   TermOrder tord;
   RelationTable<BigRational> table;

   int rl = 4; //4; //3; 
   int kl = 10;
   int ll = 4;
   int el = 2;
   float q = 0.3f; //0.4f

   protected void setUp() {
       cfac = new BigRational(9);
       tord = new TermOrder();
       ring = new GenSolvablePolynomialRing<BigRational>(cfac,rl,tord);
       table = ring.table;
       a = b = c = d = e = null;
       sbb = new SolvableGroebnerBaseSeq<BigRational>();

       a = ring.random(kl, ll, el, q );
       b = ring.random(kl, ll, el, q );
       c = ring.random(kl, ll, el, q );
       d = ring.random(kl, ll, el, q );
       e = d; //ring.random(kl, ll, el, q );
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       ring = null;
       tord = null;
       table = null;
       cfac = null;
       sbb = null;
   }


/**
 * Test sequential GBase.
 */
 public void testSequentialGBase() {

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add(a);

     L = sbb.leftGB( L );
     assertTrue("isLeftGB( { a } )", sbb.isLeftGB(L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     //System.out.println("L = " + L.size() );

     L = sbb.leftGB( L );
     assertTrue("isLeftGB( { a, b } )", sbb.isLeftGB(L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     L = sbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c } )", sbb.isLeftGB(L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     L = sbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c, d } )", sbb.isLeftGB(L) );

     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     L = sbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c, d, e } )", sbb.isLeftGB(L) );
 }


/**
 * Test Weyl sequential GBase.
 * 
 */
 public void testWeylSequentialGBase() {

     int rloc = 4;
     ring = new GenSolvablePolynomialRing<BigRational>(cfac,rloc);

     WeylRelations<BigRational> wl = new WeylRelations<BigRational>(ring);
     wl.generate();
     table = ring.table;

     a = ring.random(kl, ll, el, q );
     b = ring.random(kl, ll, el, q );
     c = ring.random(kl, ll, el, q );
     d = ring.random(kl, ll, el, q );
     e = d; //ring.random(kl, ll, el, q );

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add(a);

     L = sbb.leftGB( L );
     assertTrue("isLeftGB( { a } )", sbb.isLeftGB(L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     //System.out.println("L = " + L.size() );

     L = sbb.leftGB( L );
     assertTrue("isLeftGB( { a, b } )", sbb.isLeftGB(L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     L = sbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c } )", sbb.isLeftGB(L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     L = sbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c, d } )", sbb.isLeftGB(L) );

     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     L = sbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c, d, e } )", sbb.isLeftGB(L) );
 }


/**
 * Test sequential twosided GBase.
 * 
 */
 public void testSequentialTSGBase() {

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add(a);

     L = sbb.twosidedGB( L );
     //System.out.println("L = " + L.size() );
     assertTrue("isTwosidedGB( { a } )", sbb.isTwosidedGB(L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);

     L = sbb.twosidedGB( L );
     //System.out.println("L = " + L.size() );
     assertTrue("isTwosidedGB( { a, b } )", sbb.isTwosidedGB(L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     L = sbb.twosidedGB( L );
     //System.out.println("L = " + L.size() );
     assertTrue("isTwosidedGB( { a, b, c } )", sbb.isTwosidedGB(L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     L = sbb.twosidedGB( L );
     //System.out.println("L = " + L.size() );
     assertTrue("isTwosidedGB( { a, b, c, d } )", sbb.isTwosidedGB(L) );

     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     L = sbb.twosidedGB( L );
     //System.out.println("L = " + L.size() );
     assertTrue("isTwosidedGB( { a, b, c, d, e } )", sbb.isTwosidedGB(L) );
 }



/**
 * Test Weyl sequential twosided GBase
 * is always 1.
 */
 public void testWeylSequentialTSGBase() {

     int rloc = 4;
     ring = new GenSolvablePolynomialRing<BigRational>(cfac,rloc);

     WeylRelations<BigRational> wl = new WeylRelations<BigRational>(ring);
     wl.generate();
     table = ring.table;

     a = ring.random(kl, ll, el, q );
     b = ring.random(kl, ll, el, q );
     c = ring.random(kl, ll, el, q );
     d = ring.random(kl, ll, el, q );
     e = d; //ring.random(kl, ll, el, q );

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add(a);

     //System.out.println("La = " + L );
     L = sbb.twosidedGB( L );
     //System.out.println("L = " + L );
     assertTrue("isTwosidedGB( { a } )", sbb.isTwosidedGB(L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);

     L = sbb.twosidedGB( L );
     //System.out.println("L = " + L );
     assertTrue("isTwosidedGB( { a, b } )", sbb.isTwosidedGB(L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     L = sbb.twosidedGB( L );
     //System.out.println("L = " + L );
     assertTrue("isTwosidedGB( { a, b, c } )", sbb.isTwosidedGB(L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     L = sbb.twosidedGB( L );
     //System.out.println("L = " + L );
     assertTrue("isTwosidedGB( { a, b, c, d } )", sbb.isTwosidedGB(L) );

     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     L = sbb.twosidedGB( L );
     //System.out.println("L = " + L );
     assertTrue("isTwosidedGB( { a, b, c, d, e } )", sbb.isTwosidedGB(L) );
 }

}
