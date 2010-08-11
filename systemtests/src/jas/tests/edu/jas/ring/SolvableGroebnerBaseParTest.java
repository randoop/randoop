/*
 * $Id: SolvableGroebnerBaseParTest.java 1889 2008-07-12 13:38:05Z kredel $
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
 * SolvableGroebnerBase parallel tests with JUnit.
 * @author Heinz Kredel.
 */

public class SolvableGroebnerBaseParTest extends TestCase {

    //private static final Logger logger = Logger.getLogger(SolvableGroebnerBaseParTest.class);

/**
 * main.
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>SolvableGroebnerBaseParTest</CODE> object.
 * @param name String.
 */
   public SolvableGroebnerBaseParTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(SolvableGroebnerBaseParTest.class);
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

   SolvableGroebnerBase<BigRational> psbb;

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
       psbb = new SolvableGroebnerBaseParallel<BigRational>();

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
       ((SolvableGroebnerBaseParallel<BigRational>)psbb).terminate(); 
       psbb = null;
   }


/**
 * Test parallel GBase.
 */
 public void testParallelGBase() {

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add(a);

     L = psbb.leftGB( L );
     assertTrue("isLeftGB( { a } )", psbb.isLeftGB(L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     //System.out.println("L = " + L.size() );

     L = psbb.leftGB( L );
     assertTrue("isLeftGB( { a, b } )", psbb.isLeftGB(L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     L = psbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c } )", psbb.isLeftGB(L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     L = psbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c, d } )", psbb.isLeftGB(L) );

     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     L = psbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c, d, e } )", psbb.isLeftGB(L) );
 }


/**
 * Test Weyl parallel GBase.
 * 
 */
 public void testWeylParallelGBase() {

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

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add(a);

     L = psbb.leftGB( L );
     assertTrue("isLeftGB( { a } )", psbb.isLeftGB(L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     //System.out.println("L = " + L.size() );

     L = psbb.leftGB( L );
     assertTrue("isLeftGB( { a, b } )", psbb.isLeftGB(L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     L = psbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c } )", psbb.isLeftGB(L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     L = psbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c, d } )", psbb.isLeftGB(L) );

     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     L = psbb.leftGB( L );
     assertTrue("isLeftGB( { a, b, c, d, e } )", psbb.isLeftGB(L) );
 }


/**
 * Test parallel twosided GBase.
 * 
 */
 public void testParallelTSGBase() {

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add(a);

     L = psbb.twosidedGB( L );
     //System.out.println("L = " + L.size() );
     assertTrue("isTwosidedGB( { a } )", psbb.isTwosidedGB(L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);

     L = psbb.twosidedGB( L );
     //System.out.println("L = " + L.size() );
     assertTrue("isTwosidedGB( { a, b } )", psbb.isTwosidedGB(L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     L = psbb.twosidedGB( L );
     //System.out.println("L = " + L.size() );
     assertTrue("isTwosidedGB( { a, b, c } )", psbb.isTwosidedGB(L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     L = psbb.twosidedGB( L );
     //System.out.println("L = " + L.size() );
     assertTrue("isTwosidedGB( { a, b, c, d } )", psbb.isTwosidedGB(L) );

     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     L = psbb.twosidedGB( L );
     //System.out.println("L = " + L.size() );
     assertTrue("isTwosidedGB( { a, b, c, d, e } )", psbb.isTwosidedGB(L) );
 }



/**
 * Test Weyl parallel twosided GBase
 * is always 1.
 */
 public void testWeylParallelTSGBase() {

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

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add(a);

     //System.out.println("La = " + L );
     L = psbb.twosidedGB( L );
     //System.out.println("L = " + L );
     assertTrue("isTwosidedGB( { a } )", psbb.isTwosidedGB(L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);

     L = psbb.twosidedGB( L );
     //System.out.println("L = " + L );
     assertTrue("isTwosidedGB( { a, b } )", psbb.isTwosidedGB(L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     L = psbb.twosidedGB( L );
     //System.out.println("L = " + L );
     assertTrue("isTwosidedGB( { a, b, c } )", psbb.isTwosidedGB(L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     L = psbb.twosidedGB( L );
     //System.out.println("L = " + L );
     assertTrue("isTwosidedGB( { a, b, c, d } )", psbb.isTwosidedGB(L) );

     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     L = psbb.twosidedGB( L );
     //System.out.println("L = " + L );
     assertTrue("isTwosidedGB( { a, b, c, d, e } )", psbb.isTwosidedGB(L) );
 }

}
