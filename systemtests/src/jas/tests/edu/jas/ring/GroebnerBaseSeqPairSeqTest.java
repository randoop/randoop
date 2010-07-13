/*
 * $Id: GroebnerBaseSeqPairSeqTest.java 1555 2007-12-30 12:44:04Z kredel $
 */

package edu.jas.ring;


import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

import edu.jas.arith.BigRational;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.GenPolynomialTokenizer;
import edu.jas.poly.PolynomialList;

import edu.jas.ring.GroebnerBase;
import edu.jas.ring.GroebnerBaseSeqPairSeq;
import edu.jas.ring.ExtendedGB;



/**
 * Groebner base sequential, sequential pair list, tests with JUnit.
 * @author Heinz Kredel.
 */

public class GroebnerBaseSeqPairSeqTest extends TestCase {

    //private static final Logger logger = Logger.getLogger(GroebnerBaseSeqPairSeqTest.class);

/**
 * main
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>GroebnerBaseSeqPairSeqTest</CODE> object.
 * @param name String.
 */
   public GroebnerBaseSeqPairSeqTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GroebnerBaseSeqPairSeqTest.class);
     return suite;
   }

   GenPolynomialRing<BigRational> fac;

   List<GenPolynomial<BigRational>> L;
   PolynomialList<BigRational> F;
   List<GenPolynomial<BigRational>> G;

   GroebnerBase<BigRational> bb; // do interface

   GenPolynomial<BigRational> a;
   GenPolynomial<BigRational> b;
   GenPolynomial<BigRational> c;
   GenPolynomial<BigRational> d;
   GenPolynomial<BigRational> e;

   int rl = 3; //4; //3; 
   int kl = 2; // 10;
   int ll = 5; //7;
   int el = 3;
   float q = 0.3f; //0.2f; //0.4f

   protected void setUp() {
       BigRational coeff = new BigRational(9);
       fac = new GenPolynomialRing<BigRational>(coeff,rl);
       a = b = c = d = e = null;
       bb = new GroebnerBaseSeqPairSeq<BigRational>();
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       fac = null;
       bb = null;
   }


/**
 * Test sequential GBase.
 * 
 */
 public void testSeqPairSequentialGBase() {

     L = new ArrayList<GenPolynomial<BigRational>>();

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );
     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );
     e = d; //fac.random(kl, ll, el, q );

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);

     L = bb.GB( L );
     assertTrue("isGB( { a } )", bb.isGB(L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     //System.out.println("L = " + L.size() );

     L = bb.GB( L );
     assertTrue("isGB( { a, b } )", bb.isGB(L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     L = bb.GB( L );
     assertTrue("isGB( { a, b, c } )", bb.isGB(L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     L = bb.GB( L );
     assertTrue("isGB( { a, b, c, d } )", bb.isGB(L) );

     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     L = bb.GB( L );
     assertTrue("isGB( { a, b, c, d, e } )", bb.isGB(L) );
 }


/**
 * Test Trinks7 GBase.
 * 
 */
 @SuppressWarnings("unchecked") // not jet working
 public void testTrinks7GBase() {
     String exam = "(B,S,T,Z,P,W) L "
                 + "( "  
                 + "( 45 P + 35 S - 165 B - 36 ), " 
                 + "( 35 P + 40 Z + 25 T - 27 S ), "
                 + "( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), "
                 + "( - 9 W + 15 T P + 20 S Z ), "
                 + "( P W + 2 T Z - 11 B**3 ), "
                 + "( 99 W - 11 B S + 3 B**2 ), "
                 + "( B**2 + 33/50 B + 2673/10000 ) "
                 + ") ";
     Reader source = new StringReader( exam );
     GenPolynomialTokenizer parser
                  = new GenPolynomialTokenizer( source );
     try {
         F = (PolynomialList<BigRational>) parser.nextPolynomialSet();
     } catch(ClassCastException e) {
         fail(""+e);
     } catch(IOException e) {
         fail(""+e);
     }
     //System.out.println("F = " + F);

     G = bb.GB(F.list);
     assertTrue("isGB( GB(Trinks7) )", bb.isGB(G) );
     assertEquals("#GB(Trinks7) == 6", 6, G.size() );
     PolynomialList<BigRational> trinks 
           = new PolynomialList<BigRational>(F.ring,G);
     //System.out.println("G = " + trinks);

 }


/**
 * Test sequential extended GBase.
 * 
 */
 public void testSeqPairSequentialExtendedGBase() {

     L = new ArrayList<GenPolynomial<BigRational>>();

     ExtendedGB<BigRational> exgb;

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );
     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );
     e = d; //fac.random(kl, ll, el, q );

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);
     //System.out.println("L = " + L );

     exgb = bb.extGB( L );
     // System.out.println("exgb = " + exgb );
     assertTrue("isGB( { a } )", bb.isGB(exgb.G) );
     assertTrue("isRmat( { a } )", bb.isReductionMatrix(exgb) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     //System.out.println("L = " + L );

     exgb = bb.extGB( L );
     //System.out.println("exgb = " + exgb );
     assertTrue("isGB( { a, b } )", bb.isGB(exgb.G) );
     assertTrue("isRmat( { a, b } )", bb.isReductionMatrix(exgb) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     exgb = bb.extGB( L );
     //System.out.println("exgb = " + exgb );
     assertTrue("isGB( { a, b, c } )", bb.isGB(exgb.G) );
     assertTrue("isRmat( { a, b, c } )", bb.isReductionMatrix(exgb) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     exgb = bb.extGB( L );
     //System.out.println("exgb = " + exgb );
     assertTrue("isGB( { a, b, c, d } )", bb.isGB(exgb.G) );
     assertTrue("isRmat( { a, b, c, d } )", bb.isReductionMatrix(exgb) );


     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     exgb = bb.extGB( L );
     //System.out.println("exgb = " + exgb );
     assertTrue("isGB( { a, b, c, d, e } )", bb.isGB(exgb.G) );
     assertTrue("isRmat( { a, b, c, d, e } )", bb.isReductionMatrix(exgb) );
 }


/**
 * Test Trinks7 GBase.
 * 
 */
 @SuppressWarnings("unchecked") // not jet working
 public void testTrinks7ExtendedGBase() {
     String exam = "(B,S,T,Z,P,W) L "
                 + "( "  
                 + "( 45 P + 35 S - 165 B - 36 ), " 
                 + "( 35 P + 40 Z + 25 T - 27 S ), "
                 + "( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), "
                 + "( - 9 W + 15 T P + 20 S Z ), "
                 + "( P W + 2 T Z - 11 B**3 ), "
                 + "( 99 W - 11 B S + 3 B**2 ), "
                 + "( B**2 + 33/50 B + 2673/10000 ) "
                 + ") ";
     Reader source = new StringReader( exam );
     GenPolynomialTokenizer parser
                  = new GenPolynomialTokenizer( source );
     try {
         F = (PolynomialList<BigRational>) parser.nextPolynomialSet();
     } catch(ClassCastException e) {
         fail(""+e);
     } catch(IOException e) {
         fail(""+e);
     }
     //System.out.println("F = " + F);


     ExtendedGB<BigRational> exgb;
     
     exgb = bb.extGB(F.list);
     //System.out.println("exgb = " + exgb );
     assertTrue("isGB( GB(Trinks7) )", bb.isGB(exgb.G) );
     //assertEquals("#GB(Trinks7) == 6", 6, exgb.G.size() );
     assertTrue("isRmat( GB(Trinks7) )", bb.isReductionMatrix(exgb) );
     PolynomialList<BigRational> trinks 
           = new PolynomialList<BigRational>(F.ring,G);
     //System.out.println("G = " + trinks);

 }

}
