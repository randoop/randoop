/*
 * $Id: ReductionTest.java 1948 2008-07-23 21:35:10Z kredel $
 */

package edu.jas.ring;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

import edu.jas.structure.Product;
import edu.jas.structure.ProductRing;
import edu.jas.structure.RingFactory;

import edu.jas.arith.BigInteger;
import edu.jas.arith.BigRational;
import edu.jas.arith.BigComplex;

import edu.jas.poly.ExpVector;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolynomialList;

//import edu.jas.application.Ideal;


/**
 * Reduction tests with JUnit.
 * @author Heinz Kredel.
 */

public class ReductionTest extends TestCase {

/**
 * main
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>ReductionTest</CODE> object.
 * @param name String
 */
   public ReductionTest(String name) {
          super(name);
   }

/**
 * suite.
 * @return a test suite.
 */
public static Test suite() {
     TestSuite suite= new TestSuite(ReductionTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   GenPolynomialRing<BigRational> fac;

   GenPolynomial<BigRational> a;
   GenPolynomial<BigRational> b;
   GenPolynomial<BigRational> c;
   GenPolynomial<BigRational> d;
   GenPolynomial<BigRational> e;

   List<GenPolynomial<BigRational>> L;
   PolynomialList<BigRational> F;
   PolynomialList<BigRational> G;

   ReductionSeq<BigRational> red;
   Reduction<BigRational> redpar;

   int rl = 4; 
   int kl = 10;
   int ll = 11;
   int el = 5;
   float q = 0.6f;

   protected void setUp() {
       a = b = c = d = e = null;
       fac = new GenPolynomialRing<BigRational>( new BigRational(0), rl );
       red = new ReductionSeq<BigRational>();
       redpar = new ReductionPar<BigRational>();
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       fac = null;
       red = null;
       redpar = null;
   }


/**
 * Test constants and empty list reduction.
 */
 public void testRatReduction0() {
     L = new ArrayList<GenPolynomial<BigRational>>();

     a = fac.random(kl, ll, el, q );
     c = fac.getONE();
     d = fac.getZERO();

     e = red.normalform( L, c );
     assertTrue("isONE( e )", e.isONE() ); 

     e = red.normalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 


     L.add( c );
     e = red.normalform( L, c );
     assertTrue("isZERO( e )", e.isZERO() ); 

     e = red.normalform( L, a );
     assertTrue("isZERO( e )", e.isZERO() ); 

     e = red.normalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 


     L = new ArrayList<GenPolynomial<BigRational>>();
     L.add( d );
     e = red.normalform( L, c );
     assertTrue("isONE( e )", e.isONE() ); 

     e = red.normalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 
 }


/**
 * Test parallel reduction with constants and empty list reduction.
 */
 public void testRatReductionPar0() {
     L = new ArrayList<GenPolynomial<BigRational>>();

     a = fac.random(kl, ll, el, q );
     c = fac.getONE();
     d = fac.getZERO();

     e = redpar.normalform( L, c );
     assertTrue("isONE( e )", e.isONE() ); 

     e = redpar.normalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 


     L.add( c );
     e = redpar.normalform( L, c );
     assertTrue("isZERO( e )", e.isZERO() ); 

     e = redpar.normalform( L, a );
     assertTrue("isZERO( e )", e.isZERO() ); 

     e = redpar.normalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 


     L = new ArrayList<GenPolynomial<BigRational>>();
     L.add( d );
     e = redpar.normalform( L, c );
     assertTrue("isONE( e )", e.isONE() ); 

     e = redpar.normalform( L, d );
     assertTrue("isZERO( e )", e.isZERO() ); 
 }


/**
 * Test rational coefficient reduction.
 * 
 */
 public void testRatReduction() {

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenPolynomial<BigRational>>();
     L.add(a);

     e = red.normalform( L, a );
     assertTrue("isZERO( e )", e.isZERO() );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = red.normalform( L, a );
     assertTrue("isZERO( e ) some times", e.isZERO() ); 

     e = red.SPolynomial( a, b );
     //System.out.println("e = " + e);
     ExpVector ce = a.leadingExpVector().lcm(b.leadingExpVector());
     ExpVector ee = e.leadingExpVector();
     assertFalse("lcm(lt(a),lt(b)) != lt(e) ", ce.equals( e ) ); 


     L = new ArrayList<GenPolynomial<BigRational>>();
     L.add( a );
     assertTrue("isTopRed( a )", red.isTopReducible(L,a) ); 
     assertTrue("isRed( a )", red.isReducible(L,a) ); 
     b = fac.random(kl, ll, el, q );
     L.add( b );
     assertTrue("isTopRed( b )", red.isTopReducible(L,b) ); 
     assertTrue("isRed( b )", red.isReducible(L,b) ); 
     c = fac.random(kl, ll, el, q );
     e = red.normalform( L, c );
     assertTrue("isNF( e )", red.isNormalform(L,e) ); 
 }


/**
 * Test rational coefficient parallel reduction.
 * 
 */
 public void testRatReductionPar() {

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );
     
     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenPolynomial<BigRational>>();
     L.add(a);

     e = redpar.normalform( L, a );
     assertTrue("isZERO( e )", e.isZERO() );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = redpar.normalform( L, a );
     assertTrue("isZERO( e ) some times", e.isZERO() ); 

     L = new ArrayList<GenPolynomial<BigRational>>();
     L.add( a );
     assertTrue("isTopRed( a )", redpar.isTopReducible(L,a) ); 
     assertTrue("isRed( a )", redpar.isReducible(L,a) ); 
     b = fac.random(kl, ll, el, q );
     L.add( b );
     assertTrue("isTopRed( b )", redpar.isTopReducible(L,b) ); 
     assertTrue("isRed( b )", redpar.isReducible(L,b) ); 
     c = fac.random(kl, ll, el, q );
     e = redpar.normalform( L, c );
     assertTrue("isNF( e )", redpar.isNormalform(L,e) ); 
 }


/**
 * Test complex coefficient reduction.
 * 
 */
 public void testComplexReduction() {

     GenPolynomialRing<BigComplex> fac 
          = new GenPolynomialRing<BigComplex>( new BigComplex(0), rl );

     Reduction<BigComplex> cred = new ReductionSeq<BigComplex>();

     GenPolynomial<BigComplex> a = fac.random(kl, ll, el, q );
     GenPolynomial<BigComplex> b = fac.random(kl, ll, el, q );
     GenPolynomial<BigComplex> c;

     assertTrue("not isZERO( a )", !a.isZERO() );

     List<GenPolynomial<BigComplex>> L 
         = new ArrayList<GenPolynomial<BigComplex>>();
     L.add(a);

     GenPolynomial<BigComplex> e 
         = cred.normalform( L, a );
     assertTrue("isZERO( e )", e.isZERO() );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = cred.normalform( L, a );
     assertTrue("isZERO( e ) some times", e.isZERO() ); 

     L = new ArrayList<GenPolynomial<BigComplex>>();
     L.add( a );
     assertTrue("isTopRed( a )", cred.isTopReducible(L,a) ); 
     assertTrue("isRed( a )", cred.isReducible(L,a) ); 
     b = fac.random(kl, ll, el, q );
     L.add( b );
     assertTrue("isTopRed( b )", cred.isTopReducible(L,b) ); 
     assertTrue("isRed( b )", cred.isReducible(L,b) ); 
     c = fac.random(kl, ll, el, q );
     e = cred.normalform( L, c );
     assertTrue("isNF( e )", cred.isNormalform(L,e) ); 
 }


/**
 * Test rational coefficient reduction with recording.
 * 
 */
 public void testRatReductionRecording() {

     List<GenPolynomial<BigRational>> row = null;


     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );
     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );

     assertTrue("not isZERO( a )", !a.isZERO() );

     L = new ArrayList<GenPolynomial<BigRational>>();

     L.add(a);
     row = new ArrayList<GenPolynomial<BigRational>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = red.normalform( row, L, a );
     assertTrue("isZERO( e )", e.isZERO() );
     assertTrue("not isZERO( b )", !b.isZERO() );
     assertTrue("is Reduction ", red.isReductionNF(row,L,a,e) );

     L.add(b);
     row = new ArrayList<GenPolynomial<BigRational>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = red.normalform( row, L, b );
     assertTrue("is Reduction ", red.isReductionNF(row,L,b,e) );

     L.add(c);
     row = new ArrayList<GenPolynomial<BigRational>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = red.normalform( row, L, c );
     assertTrue("is Reduction ", red.isReductionNF(row,L,c,e) );

     L.add(d);
     row = new ArrayList<GenPolynomial<BigRational>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = red.normalform( row, L, d );
     assertTrue("is Reduction ", red.isReductionNF(row,L,d,e) );
 }


/**
 * Test integer coefficient e-reduction.
 * 
 */
 public void testIntegerEReduction() {

     BigInteger bi = new BigInteger(0);
     GenPolynomialRing<BigInteger> fac 
          = new GenPolynomialRing<BigInteger>( bi, rl );

     EReductionSeq<BigInteger> ered = new EReductionSeq<BigInteger>();

     GenPolynomial<BigInteger> a = fac.random(kl, ll, el, q );
     GenPolynomial<BigInteger> b = fac.random(kl, ll, el, q );

     assertTrue("not isZERO( a )", !a.isZERO() );

     List<GenPolynomial<BigInteger>> L 
         = new ArrayList<GenPolynomial<BigInteger>>();
     L.add(a);

     GenPolynomial<BigInteger> e 
         = ered.normalform( L, a );
     //System.out.println("a = " + a);
     //System.out.println("e = " + e);
     assertTrue("isZERO( e )", e.isZERO() );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = ered.normalform( L, a );
     //System.out.println("b = " + b);
     //System.out.println("e = " + e);
     assertTrue("isZERO( e ) some times", e.isZERO() ); 

     GenPolynomial<BigInteger> c = fac.getONE();
     a = a.sum(c);
     e = ered.normalform( L, a );
     //System.out.println("b = " + b);
     //System.out.println("e = " + e);
     assertTrue("isONE( e ) some times", e.isONE() ); 

     L = new ArrayList<GenPolynomial<BigInteger>>();
     a = c.multiply( bi.fromInteger(4) );
     b = c.multiply( bi.fromInteger(5) );
     L.add( a );
     e = ered.normalform( L, b );
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     //System.out.println("e = " + e);
     assertTrue("isONE( e )", e.isONE() ); 

     a = fac.random(kl, ll, el, q ); //.abs();
     b = fac.random(kl, ll, el, q ); //.abs();
     c = ered.GPolynomial( a, b );
     e = ered.SPolynomial( a, b );
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     //System.out.println("c = " + c);
     //System.out.println("e = " + e);

     BigInteger ci = a.leadingBaseCoefficient().gcd( b.leadingBaseCoefficient() );
     assertEquals("gcd(lbc(a),lbc(b)) = lbc(c) ", ci, c.leadingBaseCoefficient() ); 

     ExpVector ce = a.leadingExpVector().lcm(b.leadingExpVector());
     assertEquals("lcm(lt(a),lt(b)) == lt(c) ", ce, c.leadingExpVector() ); 
     assertFalse("lcm(lt(a),lt(b)) != lt(e) ", ce.equals( e.leadingExpVector() ) ); 

     L = new ArrayList<GenPolynomial<BigInteger>>();
     L.add( a );
     assertTrue("isTopRed( a )", ered.isTopReducible(L,a) ); 
     assertTrue("isRed( a )", ered.isReducible(L,a) ); 
     b = fac.random(kl, ll, el, q );
     L.add( b );
     assertTrue("isTopRed( b )", ered.isTopReducible(L,b) ); 
     assertTrue("isRed( b )", ered.isReducible(L,b) ); 
     c = fac.random(kl, ll, el, q );
     e = ered.normalform( L, c );
     assertTrue("isNF( e )", ered.isNormalform(L,e) ); 
 }


/**
 * Test integer coefficient d-reduction.
 * 
 */
 public void testIntegerDReduction() {

     BigInteger bi = new BigInteger(0);
     GenPolynomialRing<BigInteger> fac 
          = new GenPolynomialRing<BigInteger>( bi, rl );

     DReductionSeq<BigInteger> dred = new DReductionSeq<BigInteger>();

     GenPolynomial<BigInteger> a = fac.random(kl, ll, el, q );
     GenPolynomial<BigInteger> b = fac.random(kl, ll, el, q );

     assertTrue("not isZERO( a )", !a.isZERO() );

     List<GenPolynomial<BigInteger>> L 
         = new ArrayList<GenPolynomial<BigInteger>>();
     L.add(a);

     GenPolynomial<BigInteger> e 
         = dred.normalform( L, a );
     //System.out.println("a = " + a);
     //System.out.println("e = " + e);
     assertTrue("isZERO( e )", e.isZERO() );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = dred.normalform( L, a );
     //System.out.println("b = " + b);
     //System.out.println("e = " + e);
     assertTrue("isZERO( e ) some times", e.isZERO() ); 

     GenPolynomial<BigInteger> c = fac.getONE();
     a = a.sum(c);
     e = dred.normalform( L, a );
     //System.out.println("b = " + b);
     //System.out.println("e = " + e);
     assertTrue("isONE( e ) some times", e.isONE() ); 

     L = new ArrayList<GenPolynomial<BigInteger>>();
     a = c.multiply( bi.fromInteger(5) );
     L.add( a );
     b = c.multiply( bi.fromInteger(4) );
     e = dred.normalform( L, b );
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     //System.out.println("e = " + e);
     assertTrue("nf(b) = b ", e.equals(b) ); 

     a = fac.random(kl, ll, el, q ); //.abs();
     b = fac.random(kl, ll, el, q ); //.abs();
     c = dred.GPolynomial( a, b );
     e = dred.SPolynomial( a, b );
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     //System.out.println("c = " + c);
     //System.out.println("e = " + e);

     BigInteger ci = a.leadingBaseCoefficient().gcd( b.leadingBaseCoefficient() );
     assertEquals("gcd(lbc(a),lbc(b)) = lbc(c) ", ci, c.leadingBaseCoefficient() ); 

     ExpVector ce = a.leadingExpVector().lcm(b.leadingExpVector());
     assertEquals("lcm(lt(a),lt(b)) == lt(c) ", ce, c.leadingExpVector() ); 
     assertFalse("lcm(lt(a),lt(b)) != lt(e) ", ce.equals( e.leadingExpVector() ) ); 

     L = new ArrayList<GenPolynomial<BigInteger>>();
     L.add( a );
     assertTrue("isTopRed( a )", dred.isTopReducible(L,a) ); 
     assertTrue("isRed( a )", dred.isReducible(L,a) ); 
     b = fac.random(kl, ll, el, q );
     L.add( b );
     assertTrue("isTopRed( b )", dred.isTopReducible(L,b) ); 
     assertTrue("isRed( b )", dred.isReducible(L,b) ); 
     c = fac.random(kl, ll, el, q );
     e = dred.normalform( L, c );
     assertTrue("isNF( e )", dred.isNormalform(L,e) ); 
 }


/**
 * Test rational coefficient r-reduction.
 * 
 */
 public void testRationalRReduction() {

     RingFactory<BigRational> bi = new BigRational(0);
     ProductRing<BigRational> pr = new ProductRing<BigRational>(bi,3);

     GenPolynomialRing<Product<BigRational>> fac 
          = new GenPolynomialRing<Product<BigRational>>( pr, rl );

     RReductionSeq<Product<BigRational>> rred 
         = new RReductionSeq<Product<BigRational>>();

     GenPolynomial<Product<BigRational>> a = fac.random(kl, ll, el, q );
     GenPolynomial<Product<BigRational>> b = fac.random(kl, ll, el, q );
     GenPolynomial<Product<BigRational>> d;

     while ( a.isZERO() ) {
         a = fac.random(kl, ll, el, q );
     }
     while ( b.isZERO() ) {
         b = fac.random(kl, ll, el, q );
     }

     assertTrue("not isZERO( a )", !a.isZERO() );

     List<GenPolynomial<Product<BigRational>>> L 
         = new ArrayList<GenPolynomial<Product<BigRational>>>();
     L.add(a);

     GenPolynomial<Product<BigRational>> e 
         = rred.normalform( L, a );
     //System.out.println("a = " + a);
     //System.out.println("e = " + e);
     assertTrue("isNF( e )", rred.isNormalform(L,e) );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = rred.normalform( L, a );
     //System.out.println("b = " + b);
     //System.out.println("e = " + e);
     assertTrue("isNF( e )", rred.isNormalform(L,e) );

     GenPolynomial<Product<BigRational>> c = fac.getONE();
     a = a.sum(c);
     e = rred.normalform( L, a );
     //System.out.println("a = " + a);
     //System.out.println("e = " + e);
     assertTrue("isNF( e )", rred.isNormalform(L,e) ); 

     L = new ArrayList<GenPolynomial<Product<BigRational>>>();
     L.add( a );
     assertTrue("isTopRed( a )", rred.isTopReducible(L,a) ); 
     assertTrue("isRed( a )", rred.isReducible(L,a) ); 
     //b = fac.random(kl, ll, el, q );
     L.add( b );
     assertTrue("isTopRed( b )", rred.isTopReducible(L,b) ); 
     assertTrue("isRed( b )", rred.isReducible(L,b) ); 
     c = fac.random(kl, ll, el, q );
     e = rred.normalform( L, c );
     assertTrue("isNF( e )", rred.isNormalform(L,e) ); 

     c = rred.booleanClosure(a);
     //System.out.println("a = " + a);
     //System.out.println("c = " + c);
     assertTrue("isBC( c )", rred.isBooleanClosed(c) ); 

     b = a.subtract(c);
     //System.out.println("b = " + b);
     d = rred.booleanRemainder(a);
     //System.out.println("d = " + d);
     assertEquals("a-BC(a)=BR(a)", b, d ); 

     e = c.sum(d);
     //System.out.println("e = " + e);
     assertEquals("a==BC(a)+BR(a)", a, e ); 

     List<GenPolynomial<Product<BigRational>>> B;
     List<GenPolynomial<Product<BigRational>>> Br;
     L = new ArrayList<GenPolynomial<Product<BigRational>>>();
     L.add( a );
     B  = rred.booleanClosure(L);
     Br = rred.reducedBooleanClosure(L);
     //System.out.println("L  = " + L);
     //System.out.println("B = " + B);
     //System.out.println("Br = " + Br);
     assertTrue("isBC( B )", rred.isBooleanClosed(B) ); 
     assertTrue("isBC( Br )", rred.isReducedBooleanClosed(Br) ); 
     assertTrue("isBC( Br )", rred.isBooleanClosed(Br) ); 
     //not always: assertEquals("B == Br", B, Br ); 

     L.add( b );
     B  = rred.booleanClosure(L);
     Br = rred.reducedBooleanClosure(L);
     //System.out.println("L = " + L);
     //System.out.println("B = " + B);
     //System.out.println("Br = " + Br);
     assertTrue("isBC( B )", rred.isBooleanClosed(B) ); 
     assertTrue("isBC( Br )", rred.isReducedBooleanClosed(Br) ); 
     assertTrue("isBC( Br )", rred.isBooleanClosed(Br) ); 
     //not always: assertEquals("B == Br", B, Br ); 

     while ( c.isZERO() ) {
         c = fac.random(kl, ll, el, q );
     }
     L.add( c );
     B  = rred.booleanClosure(L);
     Br = rred.reducedBooleanClosure(L);
     //System.out.println("L = " + L);
     //System.out.println("B = " + B);
     //System.out.println("Br = " + Br);
     assertTrue("isBC( B )", rred.isBooleanClosed(B) ); 
     assertTrue("isBC( Br )", rred.isReducedBooleanClosed(Br) ); 
     assertTrue("isBC( Br )", rred.isBooleanClosed(Br) ); 
     //not always: assertEquals("B == Br", B, Br ); 

     while ( d.isZERO() ) {
         d = fac.random(kl, ll, el, q );
     }
     L.add( d );
     B  = rred.booleanClosure(L);
     Br = rred.reducedBooleanClosure(L);
     //System.out.println("L = " + L);
     //System.out.println("B = " + B);
     //System.out.println("Br = " + Br);
     assertTrue("isBC( B )", rred.isBooleanClosed(B) ); 
     assertTrue("isBC( Br )", rred.isReducedBooleanClosed(Br) ); 
     assertTrue("isBC( Br )", rred.isBooleanClosed(Br) ); 
     //not always: assertEquals("B == Br", B, Br ); 
 }


/**
 * Test rational coefficient r-reduction with recording.
 * 
 */
 public void testRatRReductionRecording() {

     RingFactory<BigRational> bi = new BigRational(0);
     ProductRing<BigRational> pr = new ProductRing<BigRational>(bi,3);

     GenPolynomialRing<Product<BigRational>> fac 
          = new GenPolynomialRing<Product<BigRational>>( pr, rl );

     RReductionSeq<Product<BigRational>> rred 
         = new RReductionSeq<Product<BigRational>>();

     GenPolynomial<Product<BigRational>> a = fac.random(kl, ll, el, q );
     GenPolynomial<Product<BigRational>> b = fac.random(kl, ll, el, q );
     GenPolynomial<Product<BigRational>> c, d, e;

     while ( a.isZERO() ) {
         a = fac.random(kl, ll, el, q );
     }
     while ( b.isZERO() ) {
         b = fac.random(kl, ll, el, q );
     }
     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );

     List<GenPolynomial<Product<BigRational>>> row = null;
     List<GenPolynomial<Product<BigRational>>> L;

     assertTrue("not isZERO( a )", !a.isZERO() );
     assertTrue("not isZERO( b )", !b.isZERO() );

     L = new ArrayList<GenPolynomial<Product<BigRational>>>();

     L.add(a);
     row = new ArrayList<GenPolynomial<Product<BigRational>>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = rred.normalform( row, L, a );
     //not for regular rings: assertTrue("isZERO( e )", e.isZERO() );

     //System.out.println("row = " + row);
     //System.out.println("L   = " + L);
     //System.out.println("a   = " + a);
     //System.out.println("e   = " + e);

     assertTrue("is Reduction ", rred.isReductionNF(row,L,a,e) );

     L.add(b);
     row = new ArrayList<GenPolynomial<Product<BigRational>>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = rred.normalform( row, L, b );
     assertTrue("is Reduction ", rred.isReductionNF(row,L,b,e) );

     L.add(c);
     row = new ArrayList<GenPolynomial<Product<BigRational>>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = rred.normalform( row, L, c );
     assertTrue("is Reduction ", rred.isReductionNF(row,L,c,e) );

     L.add(d);
     row = new ArrayList<GenPolynomial<Product<BigRational>>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     e = rred.normalform( row, L, d );
     assertTrue("is Reduction ", rred.isReductionNF(row,L,d,e) );
 }


/**
 * Test integer coefficient pseudo-reduction.
 * 
 */
 public void testIntegerPseudoReduction() {

     BigInteger bi = new BigInteger(0);
     GenPolynomialRing<BigInteger> fac 
          = new GenPolynomialRing<BigInteger>( bi, rl );

     PseudoReductionSeq<BigInteger> pred = new PseudoReductionSeq<BigInteger>();

     GenPolynomial<BigInteger> a = fac.random(kl, ll, el, q );
     GenPolynomial<BigInteger> b = fac.random(kl, ll, el, q );

     if ( a.isZERO() || b.isZERO() ) {
        return;
     }

     assertTrue("not isZERO( a )", !a.isZERO() );

     List<GenPolynomial<BigInteger>> L 
         = new ArrayList<GenPolynomial<BigInteger>>();
     L.add(a);

     GenPolynomial<BigInteger> e;
     e = pred.normalform( L, a );
     //System.out.println("a = " + a);
     //System.out.println("e = " + e);
     assertTrue("isZERO( e )", e.isZERO() );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = pred.normalform( L, a );
     //System.out.println("b = " + b);
     //System.out.println("e = " + e);
     assertTrue("isZERO( e ) some times", e.isZERO() ); 


     GenPolynomial<BigInteger> c = fac.getONE();
     a = a.sum(c);
     e = pred.normalform( L, a );
     //System.out.println("b = " + b);
     //System.out.println("e = " + e);
     assertTrue("isConstant( e ) some times", e.isConstant() ); 

     L = new ArrayList<GenPolynomial<BigInteger>>();
     a = c.multiply( bi.fromInteger(4) );
     b = c.multiply( bi.fromInteger(5) );
     L.add( a );
     e = pred.normalform( L, b );
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     //System.out.println("e = " + e);
     assertTrue("isZERO( e )", e.isZERO() ); 

     a = fac.random(kl, ll, el, q ); //.abs();
     b = fac.random(kl, ll, el, q ); //.abs();
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
 
     L = new ArrayList<GenPolynomial<BigInteger>>();
     L.add( a );
     assertTrue("isTopRed( a )", pred.isTopReducible(L,a) ); 
     assertTrue("isRed( a )", pred.isReducible(L,a) ); 
     b = fac.random(kl, ll, el, q );
     L.add( b );
     assertTrue("isTopRed( b )", pred.isTopReducible(L,b) ); 
     assertTrue("isRed( b )", pred.isReducible(L,b) ); 
     c = fac.random(kl, ll, el, q );
     e = pred.normalform( L, c );
     assertTrue("isNF( e )", pred.isNormalform(L,e) ); 
 }


/**
 * Test integer pseudo coefficient reduction with recording.
 * 
 */
 public void testIntReductionRecording() {

     BigInteger bi = new BigInteger(0);
     GenPolynomialRing<BigInteger> fac 
         = new GenPolynomialRing<BigInteger>( bi, rl );

     PseudoReductionSeq<BigInteger> pred = new PseudoReductionSeq<BigInteger>();

     GenPolynomial<BigInteger> a = fac.random(kl, ll, el, q );
     GenPolynomial<BigInteger> b = fac.random(kl, ll, el, q );
     GenPolynomial<BigInteger> c, d, e, f;

     if ( a.isZERO() || b.isZERO() ) {
        return;
     }
     c = fac.random(kl, ll, el+1, q );
     d = fac.random(kl, ll, el+2, q );

     // ------------
     //a = fac.parse(" 1803 x0 * x1^4 - 299 x0^3 * x1^2 - 464 x1^4 + 648 x1^3 + 383 x0^3 + 1633 ");
     //b = fac.parse(" 593 x0^4 * x1^4 - 673 x0^3 * x1^4 + 36 x0^4 + 627 x1^2 + 617 x1 + 668 x0 + 168 ");
     //b = b.multiply( fac.parse(" 10567759154481 " ) );
     //c = a.multiply( fac.parse(" 593 x0^3 - 938267 x0^2 - 435355888 x0 - 202005132032 ") );

     //d = a.multiply( fac.parse(" 3475696715811 x0^3 - 3050126808003 x0^2 - 784946666064 x0 - 202005132032 ") );

     //-------------

     List<GenPolynomial<BigInteger>> row = null;
     List<GenPolynomial<BigInteger>> L;

     PseudoReductionEntry<BigInteger> mf;

     assertTrue("not isZERO( a )", !a.isZERO() );
     assertTrue("not isZERO( b )", !b.isZERO() );

     L = new ArrayList<GenPolynomial<BigInteger>>();

     L.add(a);
     row = new ArrayList<GenPolynomial<BigInteger>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     mf = pred.normalformFactor( L, a );
     e = mf.pol;
     f = a.multiply( mf.multiplicator );
     e = pred.normalform( row, L, f );
     assertTrue("isZERO( e )", e.isZERO() );
     assertTrue("is Reduction ", pred.isNormalform(L,e) );
     assertTrue("is ReductionNF ", pred.isReductionNF(row,L,f,e) );

     L.add(b);
     row = new ArrayList<GenPolynomial<BigInteger>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     mf = pred.normalformFactor( L, a );
     e = mf.pol;
     f = a.multiply( mf.multiplicator );
     e = pred.normalform( row, L, f );
     assertTrue("is Reduction ", pred.isNormalform(L,e) );
     assertTrue("is ReductionNF ", pred.isReductionNF(row,L,f,e) );

     L.add(c);
     row = new ArrayList<GenPolynomial<BigInteger>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     mf = pred.normalformFactor( L, a );
     e = mf.pol;
     f = a.multiply( mf.multiplicator );
     e = pred.normalform( row, L, f );
     assertTrue("is Reduction ", pred.isNormalform(L,e) );
     assertTrue("is ReductionNF ", pred.isReductionNF(row,L,f,e) );

     L.add(d);
     row = new ArrayList<GenPolynomial<BigInteger>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     mf = pred.normalformFactor( L, a );
     e = mf.pol;
     f = a.multiply( mf.multiplicator );
     e = pred.normalform( row, L, f );
     assertTrue("is Reduction ", pred.isNormalform(L,e) );
     assertTrue("is ReductionNF ", pred.isReductionNF(row,L,f,e) );
 }


/**
 * Test integer coefficient pseudo r-reduction.
 * 
 */
 public void testIntegerRReduction() {

     RingFactory<BigInteger> bi = new BigInteger(0);
     ProductRing<BigInteger> pr = new ProductRing<BigInteger>(bi,3);

     GenPolynomialRing<Product<BigInteger>> fac 
          = new GenPolynomialRing<Product<BigInteger>>( pr, rl );

     RReductionSeq<Product<BigInteger>> rpred 
         = new RPseudoReductionSeq<Product<BigInteger>>();

     GenPolynomial<Product<BigInteger>> a = fac.random(kl, ll, el, q );
     GenPolynomial<Product<BigInteger>> b = fac.random(kl, ll, el, q );
     GenPolynomial<Product<BigInteger>> c, d, e;

     while ( a.isZERO() ) {
         a = fac.random(kl, ll, el, q );
     }
     while ( b.isZERO() ) {
         b = fac.random(kl, ll, el, q );
     }

     assertTrue("not isZERO( a )", !a.isZERO() );

     List<GenPolynomial<Product<BigInteger>>> L 
         = new ArrayList<GenPolynomial<Product<BigInteger>>>();
     L.add(a);

     e = rpred.normalform( L, a );
     //System.out.println("a = " + a);
     //System.out.println("e = " + e);
     assertTrue("isNF( e )", rpred.isNormalform(L,e) );

     assertTrue("not isZERO( b )", !b.isZERO() );

     L.add(b);
     e = rpred.normalform( L, a );
     assertTrue("isNF( e )", rpred.isNormalform(L,e) );

     c = fac.getONE();
     a = a.sum(c);
     e = rpred.normalform( L, a );
     assertTrue("isNF( e )", rpred.isNormalform(L,e) ); 

     L = new ArrayList<GenPolynomial<Product<BigInteger>>>();
     L.add( a );
     assertTrue("isTopRed( a )", rpred.isTopReducible(L,a) ); 
     assertTrue("isRed( a )", rpred.isReducible(L,a) ); 
     //b = fac.random(kl, ll, el, q );
     L.add( b );
     assertTrue("isTopRed( b )", rpred.isTopReducible(L,b) ); 
     assertTrue("isRed( b )", rpred.isReducible(L,b) ); 
     c = fac.random(kl, ll, el, q );
     e = rpred.normalform( L, c );
     assertTrue("isNF( e )", rpred.isNormalform(L,e) ); 

     c = rpred.booleanClosure(a);
     //System.out.println("\nboolean closure");
     //System.out.println("a = " + a);
     //System.out.println("c = " + c);
     assertTrue("isBC( c )", rpred.isBooleanClosed(c) ); 

     b = a.subtract(c);
     //System.out.println("b = " + b);
     d = rpred.booleanRemainder(a);
     //System.out.println("d = " + d);
     assertEquals("a-BC(a)=BR(a)", b, d ); 

     e = c.sum(d);
     //System.out.println("e = " + e);
     assertEquals("a==BC(a)+BR(a)", a, e ); 

     List<GenPolynomial<Product<BigInteger>>> B;
     List<GenPolynomial<Product<BigInteger>>> Br;
     L = new ArrayList<GenPolynomial<Product<BigInteger>>>();
     L.add( a );
     B  = rpred.booleanClosure(L);
     Br = rpred.reducedBooleanClosure(L);
     assertTrue("isBC( B )", rpred.isBooleanClosed(B) ); 
     assertTrue("isBC( Br )", rpred.isReducedBooleanClosed(Br) ); 
     assertTrue("isBC( Br )", rpred.isBooleanClosed(Br) ); 
     //not always: assertEquals("B == Br", B, Br ); 

     L.add( b );
     B  = rpred.booleanClosure(L);
     Br = rpred.reducedBooleanClosure(L);
     assertTrue("isBC( B )", rpred.isBooleanClosed(B) ); 
     assertTrue("isBC( Br )", rpred.isReducedBooleanClosed(Br) ); 
     assertTrue("isBC( Br )", rpred.isBooleanClosed(Br) ); 
     //not always: assertEquals("B == Br", B, Br ); 

     L.add( c );
     B  = rpred.booleanClosure(L);
     Br = rpred.reducedBooleanClosure(L);
     assertTrue("isBC( B )", rpred.isBooleanClosed(B) ); 
     assertTrue("isBC( Br )", rpred.isReducedBooleanClosed(Br) ); 
     assertTrue("isBC( Br )", rpred.isBooleanClosed(Br) ); 
     //not always: assertEquals("B == Br", B, Br ); 

     while ( d.isZERO() ) {
         d = fac.random(kl, ll, el, q );
     }
     L.add( d );
     B  = rpred.booleanClosure(L);
     Br = rpred.reducedBooleanClosure(L);
     assertTrue("isBC( B )", rpred.isBooleanClosed(B) ); 
     assertTrue("isBC( Br )", rpred.isReducedBooleanClosed(Br) ); 
     assertTrue("isBC( Br )", rpred.isBooleanClosed(Br) ); 
     //not always: assertEquals("B == Br", B, Br ); 
 }


/**
 * Test integer pseudo coefficient r-reduction with recording.
 * 
 */
 public void testIntRReductionRecording() {

     RingFactory<BigInteger> bi = new BigInteger(0);
     ProductRing<BigInteger> pr = new ProductRing<BigInteger>(bi,3);

     GenPolynomialRing<Product<BigInteger>> fac 
          = new GenPolynomialRing<Product<BigInteger>>( pr, rl );

     RPseudoReductionSeq<Product<BigInteger>> rpred 
         = new RPseudoReductionSeq<Product<BigInteger>>();

     GenPolynomial<Product<BigInteger>> a = fac.random(kl, ll, el, q );
     GenPolynomial<Product<BigInteger>> b = fac.random(kl, ll, el, q );
     GenPolynomial<Product<BigInteger>> c, d, e, f;

     while ( a.isZERO() ) {
         a = fac.random(kl, ll, el, q );
     }
     while ( b.isZERO() ) {
         b = fac.random(kl, ll, el, q );
     }
     assertTrue("not isZERO( a )", !a.isZERO() );
     assertTrue("not isZERO( b )", !b.isZERO() );

     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );

     List<GenPolynomial<Product<BigInteger>>> row = null;
     List<GenPolynomial<Product<BigInteger>>> L;

     PseudoReductionEntry<Product<BigInteger>> mf;

     L = new ArrayList<GenPolynomial<Product<BigInteger>>>();

     L.add(a);
     row = new ArrayList<GenPolynomial<Product<BigInteger>>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     mf = rpred.normalformFactor( L, a );
     e = mf.pol;
     f = a.multiply( mf.multiplicator );
     e = rpred.normalform( row, L, f );
     //not for regular rings: assertTrue("isZERO( e )", e.isZERO() );
     assertTrue("is Reduction ", rpred.isNormalform(L,e) );
     assertTrue("is ReductionNF ", rpred.isReductionNF(row,L,f,e) );

     L.add(b);
     row = new ArrayList<GenPolynomial<Product<BigInteger>>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     mf = rpred.normalformFactor( L, a );
     e = mf.pol;
     f = a.multiply( mf.multiplicator );
     e = rpred.normalform( row, L, f );
     assertTrue("is Reduction ", rpred.isNormalform(L,e) );
     assertTrue("is ReductionNF ", rpred.isReductionNF(row,L,f,e) );

     L.add(c);
     row = new ArrayList<GenPolynomial<Product<BigInteger>>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     mf = rpred.normalformFactor( L, a );
     e = mf.pol;
     f = a.multiply( mf.multiplicator );
     e = rpred.normalform( row, L, f );
     assertTrue("is Reduction ", rpred.isNormalform(L,e) );
     assertTrue("is ReductionNF ", rpred.isReductionNF(row,L,f,e) );

     L.add(d);
     row = new ArrayList<GenPolynomial<Product<BigInteger>>>( L.size() );
     for ( int m = 0; m < L.size(); m++ ) {
         row.add(null);
     }
     mf = rpred.normalformFactor( L, a );
     e = mf.pol;
     f = a.multiply( mf.multiplicator );
     e = rpred.normalform( row, L, f );
     assertTrue("is Reduction ", rpred.isNormalform(L,e) );
     assertTrue("is ReductionNF ", rpred.isReductionNF(row,L,f,e) );
 }



}
