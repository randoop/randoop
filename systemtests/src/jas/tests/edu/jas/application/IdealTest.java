/*
 * $Id: IdealTest.java 1901 2008-07-12 14:05:14Z kredel $
 */

package edu.jas.application;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

import edu.jas.arith.BigRational;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolynomialList;
import edu.jas.poly.TermOrder;

import edu.jas.ring.GroebnerBase;
import edu.jas.ring.GroebnerBaseSeq;


/**
 * Ideal tests with JUnit.
 * @author Heinz Kredel.
 */
public class IdealTest extends TestCase {

    //private static final Logger logger = Logger.getLogger(IdealTest.class);

/**
 * main
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>IdealTest</CODE> object.
 * @param name String.
 */
   public IdealTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(IdealTest.class);
     return suite;
   }

   TermOrder to;
   GenPolynomialRing<BigRational> fac;

   List<GenPolynomial<BigRational>> L;
   PolynomialList<BigRational> F;
   List<GenPolynomial<BigRational>> G;
   List<? extends GenPolynomial<BigRational>> M;

   GroebnerBase<BigRational> bb;

   GenPolynomial<BigRational> a;
   GenPolynomial<BigRational> b;
   GenPolynomial<BigRational> c;
   GenPolynomial<BigRational> d;
   GenPolynomial<BigRational> e;

   int rl = 3; //4; //3; 
   int kl = 4; //10
   int ll = 5; //7
   int el = 3;
   float q = 0.2f; //0.4f

   protected void setUp() {
       BigRational coeff = new BigRational(17,1);
       to = new TermOrder( /*TermOrder.INVLEX*/ );
       fac = new GenPolynomialRing<BigRational>(coeff,rl,to);
       bb = new GroebnerBaseSeq<BigRational>();
       a = b = c = d = e = null;
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       fac = null;
       bb = null;
   }


/**
 * Test Ideal sum.
 * 
 */
 public void testIdealSum() {

     Ideal<BigRational> I;
     Ideal<BigRational> J;
     Ideal<BigRational> K;

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

     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );

     I = new Ideal<BigRational>(fac,L,false);
     assertTrue("not isZERO( I )", !I.isZERO() );
     assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );


     L = bb.GB( L );
     assertTrue("isGB( { a } )", bb.isGB(L) );

     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );

     I = new Ideal<BigRational>(fac,L,false);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );


     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     //System.out.println("L = " + L.size() );

     I = new Ideal<BigRational>(fac,L,false);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     //assertTrue("not isGB( I )", !I.isGB() );


     L = bb.GB( L );
     assertTrue("isGB( { a, b } )", bb.isGB(L) );

     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     // assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );


     J = I;
     K = J.sum( I );
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     assertTrue("equals( K, I )", K.equals(I) );


     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);
     assertTrue("isGB( { c } )", bb.isGB(L) );

     J = new Ideal<BigRational>(fac,L,true);
     K = J.sum( I );
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     assertTrue("K contains(I)", K.contains(I) );
     assertTrue("K contains(J)", K.contains(J) );

     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     assertTrue("isGB( { d } )", bb.isGB(L) );
     J = new Ideal<BigRational>(fac,L,true);
     I = K;
     K = J.sum( I );
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     assertTrue("K contains(I)", K.contains(I) );
     assertTrue("K contains(J)", K.contains(J) );


     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     assertTrue("isGB( { e } )", bb.isGB(L) );
     J = new Ideal<BigRational>(fac,L,true);
     I = K;
     K = J.sum( I );
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     assertTrue("equals( K, I )", K.equals(I) );
     assertTrue("K contains(J)", K.contains(I) );
     assertTrue("I contains(K)", I.contains(K) );
 }


/**
 * Test Ideal product.
 * 
 */
 public void testIdealProduct() {

     Ideal<BigRational> I;
     Ideal<BigRational> J;
     Ideal<BigRational> K;
     Ideal<BigRational> H;

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );
     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );
     e = d; //fac.random(kl, ll, el, q );

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);

     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );

     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( b )", !a.isZERO() );
     L.add(b);

     J = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( J )", !J.isZERO() );
     assertTrue("not isONE( J )", !J.isONE() );
     assertTrue("isGB( J )", J.isGB() );

     K = I.product( J );
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     assertTrue("I contains(K)", I.contains(K) );
     assertTrue("J contains(K)", J.contains(K) );

     H = I.intersect( J );
     assertTrue("not isZERO( H )", !H.isZERO() );
     assertTrue("isGB( H )", H.isGB() );
     assertTrue("I contains(H)", I.contains(H) );
     assertTrue("J contains(H)", J.contains(H) );
     assertTrue("H contains(K)", H.contains(K) );
     if ( false /*! H.equals(K)*/ ) {
        System.out.println("I = " + I );
        System.out.println("J = " + J );
        System.out.println("K = " + K );
        System.out.println("H = " + H );
     }


     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);
     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);
     L = bb.GB( L );

     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );

     K = I.product( J );
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     assertTrue("I contains(K)", I.contains(K) );
     assertTrue("J contains(K)", J.contains(K) );

     H = I.intersect( J );
     assertTrue("not isZERO( H )", !H.isZERO() );
     assertTrue("isGB( H )", H.isGB() );
     assertTrue("I contains(H)", I.contains(H) );
     assertTrue("J contains(H)", J.contains(H) );
     assertTrue("H contains(K)", H.contains(K) );
     if ( false /*! H.equals(K)*/ ) {
        System.out.println("I = " + I );
        System.out.println("J = " + J );
        System.out.println("K = " + K );
        System.out.println("H = " + H );
     }


     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);
     L = bb.GB( L );

     J = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( J )", !J.isZERO() );
     //assertTrue("not isONE( J )", !J.isONE() );
     assertTrue("isGB( J )", J.isGB() );

     K = I.product( J );
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     assertTrue("I contains(K)", I.contains(K) );
     assertTrue("J contains(K)", J.contains(K) );

     H = I.intersect( J );
     assertTrue("not isZERO( H )", !H.isZERO() );
     assertTrue("isGB( H )", H.isGB() );
     assertTrue("I contains(H)", I.contains(H) );
     assertTrue("J contains(H)", J.contains(H) );
     assertTrue("H contains(K)", H.contains(K) );
     if ( false /*! H.equals(K)*/ ) {
        System.out.println("I = " + I );
        System.out.println("J = " + J );
        System.out.println("K = " + K );
        System.out.println("H = " + H );
     }
 }


/**
 * Test Ideal quotient.
 * 
 */
 public void testIdealQuotient() {

     Ideal<BigRational> I;
     Ideal<BigRational> J;
     Ideal<BigRational> K;
     Ideal<BigRational> H;

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );
     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );
     e = d; //fac.random(kl, ll, el, q );

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);
     L = bb.GB( L );

     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );


     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( b )", !a.isZERO() );
     L.add(b);
     L = bb.GB( L );

     J = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( J )", !J.isZERO() );
     //assertTrue("not isONE( J )", !J.isONE() );
     assertTrue("isGB( J )", J.isGB() );

     K = I.product( J );
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     assertTrue("I contains(K)", I.contains(K) );
     assertTrue("J contains(K)", J.contains(K) );

     H = K.quotient( J.getList().get(0) );
     assertTrue("not isZERO( H )", !H.isZERO() );
     assertTrue("isGB( H )", H.isGB() );
     assertTrue("equals(H,I)", H.equals(I) ); // GBs only

     H = K.quotient( J );
     assertTrue("not isZERO( H )", !H.isZERO() );
     assertTrue("isGB( H )", H.isGB() );
     assertTrue("equals(H,I)", H.equals(I) ); // GBs only


     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);
     L = bb.GB( L );

     J = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( J )", !J.isZERO() );
     //assertTrue("not isONE( J )", !J.isONE() );
     assertTrue("isGB( J )", J.isGB() );

     K = I.product( J );
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     assertTrue("I contains(K)", I.contains(K) );
     assertTrue("J contains(K)", J.contains(K) );

     H = K.quotient( J );
     assertTrue("not isZERO( H )", !H.isZERO() );
     assertTrue("isGB( H )", H.isGB() );
     assertTrue("equals(H,I)", H.equals(I) ); // GBs only


     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);
     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);
     L = bb.GB( L );

     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( J )", !J.isONE() );
     assertTrue("isGB( I )", I.isGB() );

     K = I.product( J );
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     assertTrue("I contains(K)", I.contains(K) );
     assertTrue("J contains(K)", J.contains(K) );

     H = K.quotient( J );
     assertTrue("not isZERO( H )", !H.isZERO() );
     assertTrue("isGB( H )", H.isGB() );
     assertTrue("equals(H,I)", H.equals(I) ); // GBs only

     if ( false ) {
        System.out.println("I = " + I );
        System.out.println("J = " + J );
        System.out.println("K = " + K );
        System.out.println("H = " + H );
     }
 }


/**
 * Test Ideal infinite quotient.
 * 
 */
 public void testIdealInfiniteQuotient() {

     Ideal<BigRational> I;
     Ideal<BigRational> J;
     Ideal<BigRational> K;

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );
     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );
     e = d; //fac.random(kl, ll, el, q );

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add( b );
     L = bb.GB( L );
     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );

     J = I.infiniteQuotient( a );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);
     L = bb.GB( L );
     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );

     J = I.infiniteQuotient( a );
     assertTrue("equals(J,I)", J.equals(I) ); // GBs only

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);
     L = bb.GB( L );
     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );

     J = I.infiniteQuotient( a );
     assertTrue("isGB( J )", J.isGB() );
     assertTrue("equals(J,I)", J.equals(I) ); // GBs only


     G = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( a )", !a.isZERO() );
     G.add( a );
     G = bb.GB( G );
     K = new Ideal<BigRational>(fac,G,true);
     assertTrue("not isZERO( K )", !K.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( K )", K.isGB() );

     J = I.infiniteQuotient( K );
     assertTrue("equals(J,I)", J.equals(I) ); // GBs only


     assertTrue("not isZERO( e )", !e.isZERO() );
     G.add( e );
     G = bb.GB( G );
     K = new Ideal<BigRational>(fac,G,true);
     assertTrue("not isZERO( K )", !K.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( K )", K.isGB() );

     J = I.infiniteQuotient( K );
     assertTrue("equals(J,I)", J.equals(I) ); // GBs only
 }


/**
 * Test Ideal infinite quotient with Rabinowich trick.
 * 
 */
 public void testIdealInfiniteQuotientRabi() {

     Ideal<BigRational> I;
     Ideal<BigRational> J;
     Ideal<BigRational> K;
     Ideal<BigRational> JJ;

     a = fac.random(kl-1, ll-1, el-1, q/2 );
     b = fac.random(kl-1, ll-1, el, q/2 );
     c = fac.random(kl-1, ll-1, el, q/2 );
     d = fac.random(kl-1, ll-1, el, q/2 );
     e = a; //fac.random(kl, ll-1, el, q );

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     L = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add( b );
     L = bb.GB( L );
     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );

     J = I.infiniteQuotientRab( a );
     JJ = I.infiniteQuotient( a );
     assertTrue("equals(J,JJ)", J.equals(JJ) ); // GBs only

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);
     L = bb.GB( L );
     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );

     J = I.infiniteQuotientRab( a );
     assertTrue("equals(J,I)", J.equals(I) ); // GBs only
     JJ = I.infiniteQuotient( a );
     assertTrue("equals(J,JJ)", J.equals(JJ) ); // GBs only

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);
     L = bb.GB( L );
     I = new Ideal<BigRational>(fac,L,true);
     assertTrue("not isZERO( I )", !I.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( I )", I.isGB() );

     J = I.infiniteQuotientRab( a );
     assertTrue("isGB( J )", J.isGB() );
     assertTrue("equals(J,I)", J.equals(I) ); // GBs only
     JJ = I.infiniteQuotient( a );
     assertTrue("equals(J,JJ)", J.equals(JJ) ); // GBs only


     G = new ArrayList<GenPolynomial<BigRational>>();
     assertTrue("not isZERO( a )", !a.isZERO() );
     G.add( a );
     G = bb.GB( G );
     K = new Ideal<BigRational>(fac,G,true);
     assertTrue("not isZERO( K )", !K.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( K )", K.isGB() );

     J = I.infiniteQuotientRab( K );
     assertTrue("equals(J,I)", J.equals(I) ); // GBs only
     JJ = I.infiniteQuotient( a );
     assertTrue("equals(J,JJ)", J.equals(JJ) ); // GBs only


     assertTrue("not isZERO( e )", !e.isZERO() );
     G.add( e );
     G = bb.GB( G );
     K = new Ideal<BigRational>(fac,G,true);
     assertTrue("not isZERO( K )", !K.isZERO() );
     //assertTrue("not isONE( I )", !I.isONE() );
     assertTrue("isGB( K )", K.isGB() );

     J = I.infiniteQuotientRab( K );
     assertTrue("equals(J,I)", J.equals(I) ); // GBs only
     JJ = I.infiniteQuotient( a );
     assertTrue("equals(J,JJ)", J.equals(JJ) ); // GBs only
 }


/**
 * Test Ideal common zeros.
 * 
 */
 public void testIdealCommonZeros() {

     Ideal<BigRational> I;
     L = new ArrayList<GenPolynomial<BigRational>>();

     I = new Ideal<BigRational>(fac,L,true);
     assertEquals("commonZeroTest( I )", I.commonZeroTest(), 1 );

     a = fac.getZERO();
     L.add(a);
     I = new Ideal<BigRational>(fac,L,true);
     assertEquals("commonZeroTest( I )", I.commonZeroTest(), 1 );

     b = fac.getONE();
     L.add(b);
     I = new Ideal<BigRational>(fac,L,true);
     assertEquals("commonZeroTest( I )", I.commonZeroTest(), -1 );

     L = new ArrayList<GenPolynomial<BigRational>>();
     a = fac.random(kl, ll, el, q );
     if ( !a.isZERO() && !a.isConstant() ) {
        L.add(a);
        I = new Ideal<BigRational>(fac,L,true);
        assertEquals("commonZeroTest( I )", I.commonZeroTest(), 1 );
     }

     L = (List<GenPolynomial<BigRational>>) fac.univariateList();
     I = new Ideal<BigRational>(fac,L,true);
     assertEquals("commonZeroTest( I )", I.commonZeroTest(), 0 );

     L.remove(0);
     I = new Ideal<BigRational>(fac,L,true);
     assertEquals("commonZeroTest( I )", I.commonZeroTest(), 1 );
 }


/**
 * Test Ideal dimension.
 * 
 */
 public void testIdealDimension() {

     Ideal<BigRational> I;
     L = new ArrayList<GenPolynomial<BigRational>>();
     Ideal.Dim dim;

     I = new Ideal<BigRational>(fac,L,true);
     assertEquals("dimension( I )", rl, I.dimension().d );

     a = fac.getZERO();
     L.add(a);
     I = new Ideal<BigRational>(fac,L,true);
     assertEquals("dimension( I )", rl, I.dimension().d );

     b = fac.getONE();
     L.add(b);
     I = new Ideal<BigRational>(fac,L,true);
     assertEquals("dimension( I )", -1, I.dimension().d );

     L = new ArrayList<GenPolynomial<BigRational>>();
     a = fac.random(kl, ll, el, q );
     if ( !a.isZERO() && !a.isConstant() ) {
        L.add(a);
        I = new Ideal<BigRational>(fac,L,true);
     //System.out.println("a = " + a);
     dim = I.dimension();
     //System.out.println("dim(I) = " + dim);
        assertTrue("dimension( I )", dim.d >= 1 );
     }

     L = (List<GenPolynomial<BigRational>>) fac.univariateList();
     I = new Ideal<BigRational>(fac,L,true);
     dim = I.dimension();
     assertEquals("dimension( I )", 0, dim.d );

     while ( L.size() > 0 ) {
      L.remove(0);
      I = new Ideal<BigRational>(fac,L,true);
      //System.out.println("I = " + I);
      dim = I.dimension();
      //System.out.println("dim(I) = " + dim);
      assertEquals("dimension( I )", rl-L.size(), dim.d );
     }

     L = (List<GenPolynomial<BigRational>>) fac.univariateList();
     I = new Ideal<BigRational>(fac,L,true);
     I = I.product( I );
     //System.out.println("I = " + I);
     dim = I.dimension();
     //System.out.println("dim(I) = " + dim);
     assertEquals("dimension( I )", 0, dim.d );

     L = I.getList();
     while ( L.size() > 0 ) {
      L.remove(0);
      I = new Ideal<BigRational>(fac,L,true);
      //System.out.println("I = " + I);
      dim = I.dimension();
      //System.out.println("dim(I) = " + dim);
      assertTrue("dimension( I )", dim.d > 0);
     }
 }


/**
 * Test Ideal term order optimization.
 * 
 */
 public void testIdealTopt() {

     Ideal<BigRational> I;
     Ideal<BigRational> J;
     Ideal<BigRational> K;

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

     I = new Ideal<BigRational>(fac,L);
     I.doGB();
     assertTrue("not isZERO( I )", !I.isZERO() );
     assertTrue("isGB( I )", I.isGB() );
     //System.out.println("I = " + I);

     J = I.clone(); //new Ideal<BigRational>(fac,L);
     J.doToptimize();
     assertTrue("not isZERO( J )", !J.isZERO() );
     assertTrue("isGB( J )", J.isGB() );
     //System.out.println("J = " + J);

     if ( I.isONE() ) {
     return;
     }

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);

     I = new Ideal<BigRational>(fac,L);
     K = I.clone();
     I.doGB();
     assertTrue("not isZERO( I )", !I.isZERO() );
     assertTrue("isGB( I )", I.isGB() );
     //System.out.println("GB(I) = " + I);

     K.doToptimize();
     K.doGB();
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     //System.out.println("GB(opt(K)) = " + K);

     J = I.clone(); 
     J.doToptimize();
     assertTrue("not isZERO( J )", !J.isZERO() );
     assertTrue("isGB( J )", J.isGB() );
     //System.out.println("opt(GB(J)) = " + J);

     if ( I.isONE() ) {
     return;
     }

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     I = new Ideal<BigRational>(fac,L);
     K = I.clone();
     I.doGB();
     assertTrue("not isZERO( I )", !I.isZERO() );
     assertTrue("isGB( I )", I.isGB() );
     //System.out.println("GB(I) = " + I);

     K.doToptimize();
     K.doGB();
     assertTrue("not isZERO( K )", !K.isZERO() );
     assertTrue("isGB( K )", K.isGB() );
     //System.out.println("GB(opt(K)) = " + K);

     J = I.clone(); 
     J.doToptimize();
     assertTrue("not isZERO( J )", !J.isZERO() );
     assertTrue("isGB( J )", J.isGB() );
     //System.out.println("opt(GB(J)) = " + J);

 }

}
