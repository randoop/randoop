/*
 * $Id: SolvableReductionTest.java 1257 2007-07-29 10:17:38Z kredel $
 */

package edu.jas.ring;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

import edu.jas.arith.BigRational;
import edu.jas.poly.GenSolvablePolynomial;
import edu.jas.poly.GenSolvablePolynomialRing;
import edu.jas.poly.PolynomialList;
import edu.jas.poly.RelationTable;
import edu.jas.poly.WeylRelations;


/**
 * Solvable Reduction tests with JUnit.
 * @author Heinz Kredel.
 */

public class SolvableReductionTest extends TestCase {

/**
 * main
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>ReductionSolvableTest</CODE> object.
 * @param name String.
 */
   public SolvableReductionTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(SolvableReductionTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   GenSolvablePolynomialRing<BigRational> fac;

   RelationTable table;

   GenSolvablePolynomial<BigRational> a;
   GenSolvablePolynomial<BigRational> b;
   GenSolvablePolynomial<BigRational> c;
   GenSolvablePolynomial<BigRational> d;
   GenSolvablePolynomial<BigRational> e;

   List<GenSolvablePolynomial<BigRational>> L;
   PolynomialList<BigRational> F;
   PolynomialList<BigRational> G;

   SolvableReduction<BigRational> sred;
   SolvableReduction<BigRational> sredpar;

   int rl = 4; 
   int kl = 10;
   int ll = 5;
   int el = 3;
   float q = 0.4f;

   protected void setUp() {
       a = b = c = d = e = null;
       fac = new GenSolvablePolynomialRing<BigRational>( new BigRational(0), rl );
       sred = new SolvableReductionSeq<BigRational>();
       sredpar = new SolvableReductionPar<BigRational>();
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       fac = null;
       sred = null;
       sredpar = null;
   }


/**
 * Test constants and empty list reduction.
 * 
 */
 public void testRatReduction0() {
     L = new ArrayList<GenSolvablePolynomial<BigRational>>();

     a = fac.random(kl, ll, el, q );
     c = fac.getONE();
     d = fac.getZERO();

     e = sred.leftNormalform( L, c );
     assertTrue("isONE( e )", e.isONE() ); 

     e = sred.leftNormalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 


     L.add( c );
     e = sred.leftNormalform( L, c );
     assertTrue("isZERO( e )", e.isZERO() ); 

     // e = Reduction.leftNormalform( L, a );
     // assertTrue("isZERO( e )", e.isZERO() ); 

     e = sred.leftNormalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 


     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add( d );
     e = sred.leftNormalform( L, c );
     assertTrue("isONE( e )", e.isONE() ); 

     e = sred.leftNormalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 
 }


/**
 * Test constants and empty list reduction.
 * 
 */
 public void testWeylRatReduction0() {
     L = new ArrayList<GenSolvablePolynomial<BigRational>>();

     WeylRelations<BigRational> wl = new WeylRelations<BigRational>(fac);
     wl.generate();

     a = fac.random(kl, ll, el, q );
     c = fac.getONE();
     d = fac.getZERO();

     e = sred.leftNormalform( L, c );
     assertTrue("isONE( e )", e.isONE() ); 

     e = sred.leftNormalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 


     L.add( c );
     e = sred.leftNormalform( L, c );
     assertTrue("isZERO( e )", e.isZERO() ); 

     e = sred.leftNormalform( L, a );
     assertTrue("isZERO( e )", e.isZERO() ); 

     e = sred.leftNormalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 


     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add( d );
     e = sred.leftNormalform( L, c );
     assertTrue("isONE( e )", e.isONE() ); 

     e = sred.leftNormalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 
 }


/**
 * Test Rat reduction.
 * 
 */
 public void testRatReduction() {

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add(a);

     e = sred.leftNormalform( L, a );
     assertTrue("isZERO( e )", e.isZERO() );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = sred.leftNormalform( L, a );
     assertTrue("isZERO( e ) some times", e.isZERO() ); 
 }


/**
 * Test Rat reduction parallel.
 * 
 */
 public void testRatReductionPar() {

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();
     L.add(a);

     e = sredpar.leftNormalform( L, a );
     assertTrue("isZERO( e )", e.isZERO() );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = sredpar.leftNormalform( L, a );
     assertTrue("isZERO( e ) some times", e.isZERO() ); 
 }


/**
 * Test Weyl Rational reduction.
 * 
 */
 public void testWeylRatReduction() {
     L = new ArrayList<GenSolvablePolynomial<BigRational>>();

     WeylRelations<BigRational> wl = new WeylRelations<BigRational>(fac);
     wl.generate();

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );

     assertTrue("not isZERO( a )", !a.isZERO() );

     L.add(a);

     e = sred.leftNormalform( L, a );
     assertTrue("isZERO( e )", e.isZERO() );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = sred.leftNormalform( L, a );
     assertTrue("isZERO( e ) some times", e.isZERO() ); 
 }


/**
 * Test Weyl Rational reduction parallel.
 * 
 */
 public void testWeylRatReductionPar() {
     L = new ArrayList<GenSolvablePolynomial<BigRational>>();

     WeylRelations<BigRational> wl = new WeylRelations<BigRational>(fac);
     wl.generate();

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );

     assertTrue("not isZERO( a )", !a.isZERO() );

     L.add(a);

     e = sredpar.leftNormalform( L, a );
     assertTrue("isZERO( e )", e.isZERO() );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = sredpar.leftNormalform( L, a );
     assertTrue("isZERO( e ) some times", e.isZERO() ); 
 }


/**
 * Test Rat reduction recording.
 * 
 */
 public void testRatReductionRecording() {

     List<GenSolvablePolynomial<BigRational>> row = null;


     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );
     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenSolvablePolynomial<BigRational>>();

     L.add(a);
     row = new ArrayList<GenSolvablePolynomial<BigRational>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = sred.leftNormalform( row, L, a );
     assertTrue("isZERO( e )", e.isZERO() );
     assertTrue("not isZERO( b )", !b.isZERO() );
     assertTrue("is leftReduction ", sred.isLeftReductionNF(row,L,a,e) );

     L.add(b);
     row = new ArrayList<GenSolvablePolynomial<BigRational>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = sred.leftNormalform( row, L, b );
     assertTrue("is leftReduction ", sred.isLeftReductionNF(row,L,b,e) );

     L.add(c);
     row = new ArrayList<GenSolvablePolynomial<BigRational>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = sred.leftNormalform( row, L, c );
     assertTrue("is leftReduction ", sred.isLeftReductionNF(row,L,c,e) );

     L.add(d);
     row = new ArrayList<GenSolvablePolynomial<BigRational>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = sred.leftNormalform( row, L, d );
     assertTrue("is leftReduction ", sred.isLeftReductionNF(row,L,d,e) );
 }


}
