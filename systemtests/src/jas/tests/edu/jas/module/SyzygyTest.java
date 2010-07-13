/*
 * $Id: SyzygyTest.java 1887 2008-07-12 13:37:07Z kredel $
 */

package edu.jas.module;

import java.util.List;
//import java.util.Iterator;
import java.util.ArrayList;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

//import edu.jas.structure.RingElem;

import edu.jas.arith.BigRational;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolynomialList;
import edu.jas.poly.TermOrder;
import edu.jas.poly.GenPolynomialTokenizer;

//import edu.jas.ring.Reduction;
import edu.jas.ring.GroebnerBase;
import edu.jas.ring.GroebnerBaseSeq;

import edu.jas.vector.ModuleList;
//import edu.jas.module.Syzygy;


/**
 * Syzygy tests with JUnit. 
 * @author Heinz Kredel.
 */

public class SyzygyTest extends TestCase {

    //private static final Logger logger = Logger.getLogger(SyzygyTest.class);

/**
 * main.
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>SyzygyTest</CODE> object.
 * @param name String.
 */
   public SyzygyTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(SyzygyTest.class);
     return suite;
   }

   int port = 4711;
   String host = "localhost";

   GenPolynomialRing<BigRational> fac;

   PolynomialList<BigRational> F;
   List<GenPolynomial<BigRational>> G;

   GroebnerBase<BigRational> bb;
   ModGroebnerBase<BigRational> mbb;

   Syzygy<BigRational> sz;

   GenPolynomial<BigRational> a;
   GenPolynomial<BigRational> b;
   GenPolynomial<BigRational> c;
   GenPolynomial<BigRational> d;
   GenPolynomial<BigRational> e;
   GenPolynomial<BigRational> zero;
   GenPolynomial<BigRational> one;

   TermOrder tord;

   List<GenPolynomial<BigRational>> L;
   List<List<GenPolynomial<BigRational>>> K;
   List<GenPolynomial<BigRational>> V;
   List<List<GenPolynomial<BigRational>>> W;
   ModuleList<BigRational> M;
   ModuleList<BigRational> N;
   ModuleList<BigRational> Z;

   int rl = 3; //4; //3; 
   int kl = 3; //7;
   int ll = 7; //9;
   int el = 2;
   float q = 0.3f; //0.4f

   protected void setUp() {
       BigRational coeff = new BigRational(9);
       tord = new TermOrder();
       fac = new GenPolynomialRing<BigRational>(coeff,rl,tord);

       bb = new GroebnerBaseSeq<BigRational>();
       mbb = new ModGroebnerBaseAbstract<BigRational>();
       sz = new SyzygyAbstract<BigRational>();

       a = b = c = d = e = null;
       L = null;
       K = null;
       V = null;

       do {
          a = fac.random(kl, ll, el, q );
          b = fac.random(kl, ll, el, q );
          c = fac.random(kl, ll, el, q );
          d = fac.random(kl, ll, el, q );
       } while ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() );
       e = d; //fac.random(kl, ll, el, q );

       one = fac.getONE();
       zero = fac.getZERO();
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       L = null;
       K = null;
       V = null;
       fac = null;
       tord = null;
       bb = null;
       mbb = null;
       sz = null;
   }


/**
 * Test sequential Syzygy.
 * 
 */
 public void testSequentialSyzygy() {

     L = new ArrayList<GenPolynomial<BigRational>>();

     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);
     assertTrue("isGB( { a } )", bb.isGB(L) );
     K = sz.zeroRelations( L );
     assertTrue("is ZR( { a } )", sz.isZeroRelation(K,L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     L = bb.GB(L);
     assertTrue("isGB( { a, b } )", bb.isGB(L) );
     //System.out.println("\nL = " + L );
     K = sz.zeroRelations( L );
     //System.out.println("\nN = " + N );
     assertTrue("is ZR( { a, b } )", sz.isZeroRelation(K,L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);
     L = bb.GB(L);
     //System.out.println("\nL = " + L );
     assertTrue("isGB( { a, b, c } )", bb.isGB(L) );
     K = sz.zeroRelations( L );
     //System.out.println("\nN = " + N );
     assertTrue("is ZR( { a, b, c } )", sz.isZeroRelation(K,L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);
     L = bb.GB(L);
     //System.out.println("\nL = " + L );
     assertTrue("isGB( { a, b, c, d } )", bb.isGB(L) );
     K = sz.zeroRelations( L );
     //System.out.println("\nN = " + N );
     assertTrue("is ZR( { a, b, c, d } )", sz.isZeroRelation(K,L) );

     //System.out.println("N = " + N );
     /*
     */
 }


/**
 * Test sequential module Syzygy.
 * 
 */
 public void testSequentialModSyzygy() {

     W = new ArrayList<List<GenPolynomial<BigRational>>>();

     assertTrue("not isZERO( a )", !a.isZERO() );
     V = new ArrayList<GenPolynomial<BigRational>>();
     V.add(a); V.add(zero); V.add(one);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     assertTrue("isGB( { (a,0,1) } )", mbb.isGB(M) );

     N = mbb.GB( M );
     assertTrue("isGB( { (a,0,1) } )", mbb.isGB(N) );

     Z = sz.zeroRelations(N);
     //System.out.println("Z = " + Z);
     assertTrue("is ZR( { a) } )", sz.isZeroRelation(Z,N) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     V = new ArrayList<GenPolynomial<BigRational>>();
     V.add(b); V.add(one); V.add(zero);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     //System.out.println("W = " + W.size() );

     N = mbb.GB( M );
     assertTrue("isGB( { a, b } )", mbb.isGB(N) );

     Z = sz.zeroRelations(N);
     //System.out.println("Z = " + Z);
     assertTrue("is ZR( { a, b } )", sz.isZeroRelation(Z,N) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     V = new ArrayList<GenPolynomial<BigRational>>();
     V.add(c); V.add(one); V.add(zero);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     //System.out.println("W = " + W.size() );

     N = mbb.GB( M );
     //System.out.println("GB(M) = " + N);
     assertTrue("isGB( { a,b,c) } )", mbb.isGB(N) );

     Z = sz.zeroRelations(N);
     //System.out.println("Z = " + Z);
     //boolean b = Syzygy.isZeroRelation(Z,N);
     //System.out.println("boolean = " + b);
     assertTrue("is ZR( { a,b,c } )", sz.isZeroRelation(Z,N) );

 }


/**
 * Test sequential arbitrary base Syzygy.
 * 
 */
 public void testSequentialArbitrarySyzygy() {

     L = new ArrayList<GenPolynomial<BigRational>>();

     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);
     assertTrue("isGB( { a } )", bb.isGB(L) );
     K = sz.zeroRelationsArbitrary( L );
     assertTrue("is ZR( { a } )", sz.isZeroRelation(K,L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     // L = bb.GB(L);
     // assertTrue("isGB( { a, b } )", bb.isGB(L) );
     //System.out.println("\nL = " + L );
     K = sz.zeroRelationsArbitrary( L );
     //System.out.println("\nN = " + N );
     assertTrue("is ZR( { a, b } )", sz.isZeroRelation(K,L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);
     //L = bb.GB(L);
     //System.out.println("\nL = " + L );
     //assertTrue("isGB( { a, b, c } )", bb.isGB(L) );
     K = sz.zeroRelationsArbitrary( L );
     //System.out.println("\nN = " + N );
     assertTrue("is ZR( { a, b, c } )", sz.isZeroRelation(K,L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);
     //L = bb.GB(L);
     //System.out.println("\nL = " + L );
     //assertTrue("isGB( { a, b, c, d } )", bb.isGB(L) );
     K = sz.zeroRelationsArbitrary( L );
     //System.out.println("\nN = " + N );
     assertTrue("is ZR( { a, b, c, d } )", sz.isZeroRelation(K,L) );

     //System.out.println("N = " + N );

 }


/**
 * Test sequential arbitrary base Syzygy, ex CLO 2, p 214 ff.
 * 
 */
 @SuppressWarnings("unchecked") // not jet working
 public void testSequentialArbitrarySyzygyCLO() {

     PolynomialList<BigRational> F = null;

     String exam = "(x,y) G "
                 + "( "  
                 + "( x y + x ), " 
                 + "( y^2 + 1 ) "
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

     L = F.list;
     K = sz.zeroRelationsArbitrary( L );
     assertTrue("is ZR( { a, b } )", sz.isZeroRelation(K,L) );
 }


/**
 * Test sequential arbitrary module Syzygy.
 * 
 */
 public void testSequentialArbitraryModSyzygy() {

     W = new ArrayList<List<GenPolynomial<BigRational>>>();

     assertTrue("not isZERO( a )", !a.isZERO() );
     V = new ArrayList<GenPolynomial<BigRational>>();
     V.add(a); V.add(zero); V.add(one);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     assertTrue("isGB( { (a,0,1) } )", mbb.isGB(M) );

     Z = sz.zeroRelationsArbitrary(M);
     //System.out.println("Z = " + Z);
     assertTrue("is ZR( { a) } )", sz.isZeroRelation(Z,M) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     V = new ArrayList<GenPolynomial<BigRational>>();
     V.add(b); V.add(one); V.add(zero);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     //System.out.println("W = " + W.size() );

     Z = sz.zeroRelationsArbitrary(M);
     //System.out.println("Z = " + Z);
     assertTrue("is ZR( { a, b } )", sz.isZeroRelation(Z,M) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     V = new ArrayList<GenPolynomial<BigRational>>();
     V.add(c); V.add(one); V.add(zero);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     //System.out.println("W = " + W.size() );

     Z = sz.zeroRelationsArbitrary(M);
     //System.out.println("Z = " + Z);
     //boolean b = Syzygy.isZeroRelation(Z,N);
     //System.out.println("boolean = " + b);
     assertTrue("is ZR( { a,b,c } )", sz.isZeroRelation(Z,M) );

 }

}
