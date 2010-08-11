/*
 * $Id: SolvableSyzygyTest.java 1887 2008-07-12 13:37:07Z kredel $
 */

package edu.jas.module;

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

import edu.jas.poly.PolynomialList;
import edu.jas.poly.TermOrder;
import edu.jas.poly.GenPolynomialTokenizer;
import edu.jas.poly.GenSolvablePolynomial;
import edu.jas.poly.GenSolvablePolynomialRing;
import edu.jas.poly.WeylRelations;
import edu.jas.poly.RelationTable;

import edu.jas.ring.SolvableGroebnerBase;
import edu.jas.ring.SolvableGroebnerBaseSeq;

import edu.jas.vector.ModuleList;
//import edu.jas.module.SolvableSyzygy;


/**
 * SolvableSyzygy tests with JUnit. 
 * @author Heinz Kredel.
 */

public class SolvableSyzygyTest extends TestCase {

    //private static final Logger logger = Logger.getLogger(SolvableSyzygyTest.class);

/**
 * main.
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>SolvableSyzygyTest</CODE> object.
 * @param name String.
 */
   public SolvableSyzygyTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(SolvableSyzygyTest.class);
     return suite;
   }

   int port = 4711;
   String host = "localhost";

   BigRational cfac;
   GenSolvablePolynomialRing<BigRational> fac;

   PolynomialList<BigRational> F;
   List<GenSolvablePolynomial<BigRational>> G;

   GenSolvablePolynomial<BigRational> a;
   GenSolvablePolynomial<BigRational> b;
   GenSolvablePolynomial<BigRational> c;
   GenSolvablePolynomial<BigRational> d;
   GenSolvablePolynomial<BigRational> e;
   GenSolvablePolynomial<BigRational> zero;
   GenSolvablePolynomial<BigRational> one;

   TermOrder tord;
   RelationTable table;

   List<GenSolvablePolynomial<BigRational>> L;
   List<List<GenSolvablePolynomial<BigRational>>> K;
   List<GenSolvablePolynomial<BigRational>> V;
   List<List<GenSolvablePolynomial<BigRational>>> W;
   ModuleList<BigRational> M;
   ModuleList<BigRational> N;
   ModuleList<BigRational> Z;

   SolvableGroebnerBase<BigRational> sbb;
   ModSolvableGroebnerBase<BigRational> msbb;

   SolvableSyzygy<BigRational> ssz;

   int rl = 4; //4; //3; 
   int kl = 5;
   int ll = 7;
   int el = 2;
   float q = 0.3f; //0.4f

   protected void setUp() {
       cfac = new BigRational(1);
       tord = new TermOrder();
       fac = new GenSolvablePolynomialRing<BigRational>(cfac,rl,tord);
       table = fac.table; 
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
       sbb = new SolvableGroebnerBaseSeq<BigRational>();
       msbb = new ModSolvableGroebnerBaseAbstract<BigRational>();
       ssz = new SolvableSyzygyAbstract<BigRational>();

   }

   protected void tearDown() {
       a = b = c = d = e = null;
       L = null;
       K = null;
       V = null;
       fac = null;
       tord = null;
       table = null;
       sbb = null;
       msbb = null;
       ssz = null;
   }


/**
 * Test sequential SolvableSyzygy.
 * 
 */
 public void testSequentialSolvableSyzygy() {

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();

     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);
     assertTrue("isGB( { a } )", sbb.isLeftGB(L) );
     K = ssz.leftZeroRelations( L );
     assertTrue("is ZR( { a } )", ssz.isLeftZeroRelation(K,L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     L = sbb.leftGB(L);
     assertTrue("isGB( { a, b } )", sbb.isLeftGB(L) );
     //System.out.println("\nL = " + L );
     K = ssz.leftZeroRelations( L );
     //System.out.println("\nK = " + K );
     assertTrue("is ZR( { a, b } )", ssz.isLeftZeroRelation(K,L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);
     L = sbb.leftGB(L);
     //System.out.println("\nL = " + L );
     assertTrue("isGB( { a, b, c } )", sbb.isLeftGB(L) );
     K = ssz.leftZeroRelations( L );
     //System.out.println("\nK = " + K );
     assertTrue("is ZR( { a, b, c } )", ssz.isLeftZeroRelation(K,L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);
     L = sbb.leftGB(L);
     //System.out.println("\nL = " + L );
     assertTrue("isGB( { a, b, c, d } )", sbb.isLeftGB(L) );
     K = ssz.leftZeroRelations( L );
     //System.out.println("\nK = " + K );
     assertTrue("is ZR( { a, b, c, d } )", ssz.isLeftZeroRelation(K,L) );

     //System.out.println("K = " + K );
 }


/**
 * Test sequential Weyl SolvableSyzygy.
 * 
 */
 public void testSequentialWeylSolvableSyzygy() {

     int rloc = 4;
     fac = new GenSolvablePolynomialRing<BigRational>(cfac,rloc);

     WeylRelations<BigRational> wl = new WeylRelations<BigRational>(fac);
     wl.generate();
     table = fac.table;

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );
     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );
     e = d; //fac.random(kl, ll, el, q );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();

     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);
     assertTrue("isGB( { a } )", sbb.isLeftGB(L) );
     K = ssz.leftZeroRelations( L );
     assertTrue("is ZR( { a } )", ssz.isLeftZeroRelation(K,L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     L = sbb.leftGB(L);
     assertTrue("isGB( { a, b } )", sbb.isLeftGB(L) );
     //System.out.println("\nL = " + L );
     K = ssz.leftZeroRelations( L );
     //System.out.println("\nK = " + K );
     assertTrue("is ZR( { a, b } )", ssz.isLeftZeroRelation(K,L) );

     // useless since 1 in GB
     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);
     L = sbb.leftGB(L);
     //System.out.println("\nL = " + L );
     assertTrue("isGB( { a, b, c } )", sbb.isLeftGB(L) );
     K = ssz.leftZeroRelations( L );
     //System.out.println("\nK = " + K );
     assertTrue("is ZR( { a, b, c } )", ssz.isLeftZeroRelation(K,L) );

     // useless since 1 in GB
     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);
     L = sbb.leftGB(L);
     //System.out.println("\nL = " + L );
     assertTrue("isGB( { a, b, c, d } )", sbb.isLeftGB(L) );
     K = ssz.leftZeroRelations( L );
     //System.out.println("\nK = " + K );
     assertTrue("is ZR( { a, b, c, d } )", ssz.isLeftZeroRelation(K,L) );

     //System.out.println("K = " + K );
 }


/**
 * Test sequential module SolvableSyzygy.
 * 
 */
 public void testSequentialModSolvableSyzygy() {

     W = new ArrayList<List<GenSolvablePolynomial<BigRational>>>();

     assertTrue("not isZERO( a )", !a.isZERO() );
     V = new ArrayList<GenSolvablePolynomial<BigRational>>();
     V.add(a); V.add(zero); V.add(one);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     assertTrue("isGB( { (a,0,1) } )", msbb.isLeftGB(M) );

     N = msbb.leftGB( M );
     assertTrue("isGB( { (a,0,1) } )", msbb.isLeftGB(N) );

     Z = ssz.leftZeroRelations(N);
     //System.out.println("Z = " + Z);
     assertTrue("is ZR( { a) } )", ssz.isLeftZeroRelation(Z,N) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     V = new ArrayList<GenSolvablePolynomial<BigRational>>();
     V.add(b); V.add(one); V.add(zero);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     //System.out.println("W = " + W.size() );

     N = msbb.leftGB( M );
     assertTrue("isGB( { a, b } )", msbb.isLeftGB(N) );

     Z = ssz.leftZeroRelations(N);
     //System.out.println("Z = " + Z);
     assertTrue("is ZR( { a, b } )", ssz.isLeftZeroRelation(Z,N) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     V = new ArrayList<GenSolvablePolynomial<BigRational>>();
     V.add(c); V.add(one); V.add(zero);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     //System.out.println("W = " + W.size() );

     N = msbb.leftGB( M );
     //System.out.println("GB(M) = " + N);
     assertTrue("isGB( { a,b,c) } )", msbb.isLeftGB(N) );

     Z = ssz.leftZeroRelations(N);
     //System.out.println("Z = " + Z);
     //boolean b = ssz.isLeftZeroRelation(Z,N);
     //System.out.println("boolean = " + b);
     assertTrue("is ZR( { a,b,c } )", ssz.isLeftZeroRelation(Z,N) );
 }


/**
 * Test sequential arbitrary base Syzygy.
 * 
 */
 public void testSequentialArbitrarySyzygy() {

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();

     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);
     assertTrue("isGB( { a } )", sbb.isLeftGB(L) );
     K = ssz.leftZeroRelationsArbitrary( L );
     assertTrue("is ZR( { a } )", ssz.isLeftZeroRelation(K,L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     K = ssz.leftZeroRelationsArbitrary( L );
     //System.out.println("\nN = " + N );
     assertTrue("is ZR( { a, b } )", ssz.isLeftZeroRelation(K,L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);
     K = ssz.leftZeroRelationsArbitrary( L );
     //System.out.println("\nN = " + N );
     assertTrue("is ZR( { a, b, c } )", ssz.isLeftZeroRelation(K,L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);
     K = ssz.leftZeroRelationsArbitrary( L );
     //System.out.println("\nN = " + N );
     assertTrue("is ZR( { a, b, c, d } )", ssz.isLeftZeroRelation(K,L) );

     //System.out.println("N = " + N );
 }


/**
 * Test sequential arbitrary base Syzygy, ex CLO 2, p 214 ff.
 * 
 */
 @SuppressWarnings("unchecked") // not jet working
 public void testSequentialArbitrarySyzygyCLO() {

     PolynomialList<BigRational> F = null;

     String exam = "Rat(x,y) G "
                 + "( "  
                 + "( x y + x ), " 
                 + "( y^2 + 1 ) "
                 + ") ";
     Reader source = new StringReader( exam );
     GenPolynomialTokenizer parser
                  = new GenPolynomialTokenizer( source );
     try {
         F = (PolynomialList<BigRational>) parser.nextSolvablePolynomialSet();
     } catch(ClassCastException e) {
         fail(""+e);
     } catch(IOException e) {
         fail(""+e);
     }
     //System.out.println("F = " + F);

     L = F.castToSolvableList();
     K = ssz.leftZeroRelationsArbitrary( L );
     assertTrue("is ZR( { a, b } )", ssz.isLeftZeroRelation(K,L) );
 }


/**
 * Test sequential arbitrary base Syzygy, ex WA_32.
 * 
 */
 @SuppressWarnings("unchecked") // not jet working
 public void testSequentialArbitrarySyzygyWA32() {

     PolynomialList<BigRational> F = null;

     String exam = "Rat(e1,e2,e3) L "
                 + "RelationTable "
                 + "( "
                 + " ( e3 ), ( e1 ), ( e1 e3 - e1 ), "
                 + " ( e3 ), ( e2 ), ( e2 e3 - e2 ) "
                 + ")"
                 + "( "  
                 + " ( e1 e3^3 + e2^2 ), " 
                 + " ( e1^3 e2^2 + e3 ), "
                 + " ( e3^3 + e3^2 ) "
                 + ") ";
     Reader source = new StringReader( exam );
     GenPolynomialTokenizer parser
                  = new GenPolynomialTokenizer( source );
     try {
         F = (PolynomialList<BigRational>) parser.nextSolvablePolynomialSet();
     } catch(ClassCastException e) {
         fail(""+e);
     } catch(IOException e) {
         fail(""+e);
     }
     //System.out.println("F = " + F);

     L = F.castToSolvableList();
     K = ssz.leftZeroRelationsArbitrary( L );
     assertTrue("is ZR( { a, b } )", ssz.isLeftZeroRelation(K,L) );

 }


/**
 * Test sequential arbitrary module SolvableSyzygy.
 * 
 */
 public void testSequentialArbitraryModSolvableSyzygy() {

     W = new ArrayList<List<GenSolvablePolynomial<BigRational>>>();

     assertTrue("not isZERO( a )", !a.isZERO() );
     V = new ArrayList<GenSolvablePolynomial<BigRational>>();
     V.add(a); V.add(zero); V.add(one);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     assertTrue("isGB( { (a,0,1) } )", msbb.isLeftGB(M) );

     Z = ssz.leftZeroRelationsArbitrary(M);
     //System.out.println("Z = " + Z);
     assertTrue("is ZR( { a) } )", ssz.isLeftZeroRelation(Z,M) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     V = new ArrayList<GenSolvablePolynomial<BigRational>>();
     V.add(b); V.add(one); V.add(zero);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     //System.out.println("W = " + W.size() );

     Z = ssz.leftZeroRelationsArbitrary(M);
     //System.out.println("Z = " + Z);
     assertTrue("is ZR( { a, b } )", ssz.isLeftZeroRelation(Z,M) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     V = new ArrayList<GenSolvablePolynomial<BigRational>>();
     V.add(c); V.add(one); V.add(zero);
     W.add(V);
     M = new ModuleList<BigRational>(fac,W);
     //System.out.println("W = " + W.size() );

     Z = ssz.leftZeroRelationsArbitrary(M);
     //System.out.println("Z = " + Z);
     //boolean b = ssz.isLeftZeroRelation(Z,N);
     //System.out.println("boolean = " + b);
     assertTrue("is ZR( { a,b,c } )", ssz.isLeftZeroRelation(Z,M) );
 }

}
