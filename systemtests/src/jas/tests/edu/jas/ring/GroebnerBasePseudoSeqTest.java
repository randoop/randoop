/*
 * $Id: GroebnerBasePseudoSeqTest.java 1555 2007-12-30 12:44:04Z kredel $
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

import edu.jas.arith.BigInteger;
import edu.jas.arith.BigRational;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.GenPolynomialTokenizer;
import edu.jas.poly.PolynomialList;
import edu.jas.poly.PolyUtil;

import edu.jas.ring.GroebnerBase;


/**
 * Groebner base pseudo reduction sequential tests with JUnit.
 * @author Heinz Kredel.
 */

public class GroebnerBasePseudoSeqTest extends TestCase {

    //private static final Logger logger = Logger.getLogger(GroebnerBasePseudoSeqTest.class);

/**
 * main
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>GroebnerBasePseudoSeqTest</CODE> object.
 * @param name String.
 */
   public GroebnerBasePseudoSeqTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GroebnerBasePseudoSeqTest.class);
     return suite;
   }

   GenPolynomialRing<BigInteger> fac;

   List<GenPolynomial<BigInteger>> L;
   PolynomialList<BigInteger> F;
   List<GenPolynomial<BigInteger>> G;

   GroebnerBase<BigInteger> bb;

   GenPolynomial<BigInteger> a;
   GenPolynomial<BigInteger> b;
   GenPolynomial<BigInteger> c;
   GenPolynomial<BigInteger> d;
   GenPolynomial<BigInteger> e;

   int rl = 3; //4; //3; 
   int kl = 10;
   int ll = 7;
   int el = 3;
   float q = 0.2f; //0.4f

   protected void setUp() {
       BigInteger coeff = new BigInteger(9);
       fac = new GenPolynomialRing<BigInteger>(coeff,rl);
       a = b = c = d = e = null;
       bb = new GroebnerBasePseudoSeq<BigInteger>(coeff);
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
 public void testSequentialGBase() {

     L = new ArrayList<GenPolynomial<BigInteger>>();

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
 @SuppressWarnings("unchecked") 
 public void testTrinks7GBase() {
     String exam = "Z(B,S,T,Z,P,W) L "
                 + "( "  
                 + "( 45 P + 35 S - 165 B - 36 ), " 
                 + "( 35 P + 40 Z + 25 T - 27 S ), "
                 + "( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), "
                 + "( - 9 W + 15 T P + 20 S Z ), "
                 + "( P W + 2 T Z - 11 B**3 ), "
                 + "( 99 W - 11 B S + 3 B**2 ), "
                 + "( 10000 B**2 + 6600 B + 2673 ) "
                 + ") ";
     Reader source = new StringReader( exam );
     GenPolynomialTokenizer parser
                  = new GenPolynomialTokenizer( source );
     try {
         F = (PolynomialList<BigInteger>) parser.nextPolynomialSet();
     } catch(ClassCastException e) {
         fail(""+e);
     } catch(IOException e) {
         fail(""+e);
     }
     //System.out.println("F = " + F);

     long s,t;
     t = System.currentTimeMillis();
     G = bb.GB(F.list);
     t = System.currentTimeMillis() - t;
     assertTrue("isGB( GB(Trinks7) )", bb.isGB(G) );
     assertEquals("#GB(Trinks7) == 6", 6, G.size() );
     PolynomialList<BigInteger> trinks 
           = new PolynomialList<BigInteger>(F.ring,G);
     //System.out.println("G = " + trinks);

     GenPolynomialRing<BigInteger> ifac = F.ring;
     BigRational cf = new BigRational();
     GenPolynomialRing<BigRational> rfac 
        = new GenPolynomialRing<BigRational>(cf,ifac);

     List<GenPolynomial<BigRational>> Gr, Fr, Gir; 
     Fr = PolyUtil.<BigRational>fromIntegerCoefficients(rfac,F.list);
     GroebnerBaseSeq<BigRational> bbr = new GroebnerBaseSeq<BigRational>();
     s = System.currentTimeMillis();
     Gr = bbr.GB(Fr);
     s = System.currentTimeMillis() - s;

     Gir = PolyUtil.<BigRational>fromIntegerCoefficients(rfac,G);
     Gir = PolyUtil.<BigRational>monic(Gir);

     assertEquals("ratGB == intGB", Gr, Gir );
     //System.out.println("time: ratGB = " + s + ", intGB = " + t);
 }

}
