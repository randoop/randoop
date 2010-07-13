/*
 * $Id: GaloisFieldTest.java 1255 2007-07-29 10:16:33Z kredel $
 */

package edu.jas.poly;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//import edu.jas.arith.BigRational;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;

import edu.jas.poly.GenPolynomial;

//import edu.jas.structure.RingElem;


/**
 * Galois field tests with JUnit.
 * @author Heinz Kredel.
 */
public class GaloisFieldTest extends TestCase {


/**
 * main
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }


/**
 * Constructs a <CODE>GaloisFieldTest</CODE> object.
 * @param name String.
 */
   public GaloisFieldTest(String name) {
          super(name);
   }


/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GaloisFieldTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   AlgebraicNumberRing<ModInteger> fac;
   GenPolynomialRing<ModInteger> mfac;

   AlgebraicNumber< ModInteger > a;
   AlgebraicNumber< ModInteger > b;
   AlgebraicNumber< ModInteger > c;
   AlgebraicNumber< ModInteger > d;
   AlgebraicNumber< ModInteger > e;

   int rl = 1; 
   int kl = 10;
   int ll = 15;
   int el = ll;
   float q = 0.5f;

   protected long getPrime() {
       long prime = 2; //2^60-93; // 2^30-35; //19; knuth (2,390)
       for ( int i = 1; i < 60; i++ ) {
           prime *= 2;
       }
       prime -= 93;
       //System.out.println("prime = " + prime);
       return prime;
   }

   protected void setUp() {
       a = b = c = d = e = null;
       long prime = getPrime();
       mfac = new GenPolynomialRing<ModInteger>( new ModIntegerRing(prime), 1 );
       //System.out.println("mfac = " + mfac);
       GenPolynomial<ModInteger> mo = mfac.random(kl,ll,el,q);
       while ( mo.isConstant() ) {
          mo = mfac.random(kl,ll,el,q);
       }
       fac = new AlgebraicNumberRing<ModInteger>( mo );
       //System.out.println("fac = " + fac);
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       fac = null;
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstruction() {
     c = fac.getONE();
     //System.out.println("c = " + c);
     //System.out.println("c.getVal() = " + c.getVal());
     assertTrue("length( c ) = 1", c.getVal().length() == 1);
     assertTrue("isZERO( c )", !c.getVal().isZERO() );
     assertTrue("isONE( c )", c.getVal().isONE() );

     d = fac.getZERO();
     //System.out.println("d = " + d);
     //System.out.println("d.getVal() = " + d.getVal());
     assertTrue("length( d ) = 0", d.getVal().length() == 0);
     assertTrue("isZERO( d )", d.getVal().isZERO() );
     assertTrue("isONE( d )", !d.getVal().isONE() );
 }


/**
 * Test random polynomial
 * 
 */
 public void testRandom() {
     for (int i = 0; i < 7; i++) {
         a = fac.random(ll+i);
         //System.out.println("a = " + a);

         // fac.random(rl+i, kl*(i+1), ll+2*i, el+i, q );
         assertTrue("length( a"+i+" ) <> 0", a.getVal().length() >= 0);
         assertTrue(" not isZERO( a"+i+" )", !a.getVal().isZERO() );
         assertTrue(" not isONE( a"+i+" )", !a.getVal().isONE() );
     }
 }


/**
 * Test addition.
 * 
 */
 public void testAddition() {
     a = fac.random(ll);
     b = fac.random(ll);

     c = a.sum(b);
     d = c.subtract(b);
     assertEquals("a+b-b = a",a,d);

     c = fac.random(ll);
     d = c.sum( a.sum(b) );
     e = c.sum( a ).sum(b);
     assertEquals("c+(a+b) = (c+a)+b",d,e);


     c = a.sum( fac.getZERO() );
     d = a.subtract( fac.getZERO() );
     assertEquals("a+0 = a-0",c,d);

     c = fac.getZERO().sum( a );
     d = fac.getZERO().subtract( a.negate() );
     assertEquals("0+a = 0+(-a)",c,d);

 }


/**
 * Test object multiplication.
 * 
 */
 public void testMultiplication() {
     a = fac.random(ll);
     assertTrue("not isZERO( a )", !a.isZERO() );

     b = fac.random(ll);
     assertTrue("not isZERO( b )", !b.isZERO() );

     c = b.multiply(a);
     d = a.multiply(b);
     assertTrue("not isZERO( c )", !c.isZERO() );
     assertTrue("not isZERO( d )", !d.isZERO() );

     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     e = d.subtract(c);
     assertTrue("isZERO( a*b-b*a ) " + e, e.isZERO() );

     assertTrue("a*b = b*a", c.equals(d) );
     assertEquals("a*b = b*a",c,d);

     c = fac.random(ll);
     //System.out.println("c = " + c);
     d = a.multiply( b.multiply(c) );
     e = (a.multiply(b)).multiply(c);

     //System.out.println("d = " + d);
     //System.out.println("e = " + e);

     //System.out.println("d-e = " + d.subtract(c) );

     assertEquals("a(bc) = (ab)c",d,e);
     assertTrue("a(bc) = (ab)c", d.equals(e) );

     c = a.multiply( fac.getONE() );
     d = fac.getONE().multiply( a );
     assertEquals("a*1 = 1*a",c,d);


     c = a.inverse();
     d = c.multiply(a);
     //System.out.println("a = " + a);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a*1/a = 1",fac.getONE(),d);
 }


/**
 * Test distributive law.
 * 
 */
 public void testDistributive() {
     a = fac.random( ll );
     b = fac.random( ll );
     c = fac.random( ll );

     d = a.multiply( b.sum(c) );
     e = a.multiply( b ).sum( a.multiply(c) );

     assertEquals("a(b+c) = ab+ac",d,e);
 }


/**
 * Test chinese remainder.
 * 
 */
 public void testChineseRemainder() {
     ModIntegerRing cfac;
     GenPolynomialRing<ModInteger> m0fac;
     GenPolynomial<ModInteger> x0;
     GenPolynomial<ModInteger> x;
     GenPolynomial<ModInteger> m0;
     GenPolynomial<ModInteger> m1;
     GenPolynomial<ModInteger> m01;
     AlgebraicNumberRing<ModInteger> fac0;
     AlgebraicNumberRing<ModInteger> fac1;
     AlgebraicNumberRing<ModInteger> fac01;

     cfac = new ModIntegerRing(19);
     //System.out.println("cfac = " + cfac.getModul());
     m0fac = new GenPolynomialRing<ModInteger>( cfac, 0 );
     //System.out.println("m0fac = " + m0fac);
     mfac = new GenPolynomialRing<ModInteger>( cfac, 1 );
     //System.out.println("mfac = " + mfac);

     x0 = m0fac.getONE();
     //System.out.println("x0 = " + x0);
     x = x0.extend(mfac,0,1);
     //System.out.println("x  = " + x);

     m0 = mfac.fromInteger(2);
     m1 = mfac.fromInteger(5);
     //System.out.println("m0 = " + m0);
     //System.out.println("m1 = " + m1);

     m0 = x.subtract(m0);
     m1 = x.subtract(m1);
     //System.out.println("m0 = " + m0);
     //System.out.println("m1 = " + m1);

     m01 = m0.multiply(m1);
     //System.out.println("m01 = " + m01);

     fac0 = new AlgebraicNumberRing<ModInteger>( m0, true );
     fac1 = new AlgebraicNumberRing<ModInteger>( m1, true );
     fac01 = new AlgebraicNumberRing<ModInteger>( m01, false );
     //System.out.println("fac0 = " + fac0);
     //System.out.println("fac1 = " + fac1);
     //System.out.println("fac01 = " + fac01);

     a = fac01.random( 9 );
     //System.out.println("a = " + a);
     b = new AlgebraicNumber<ModInteger>(fac0,a.getVal());
     //System.out.println("b = " + b);
     c = new AlgebraicNumber<ModInteger>(fac1,a.getVal());
     //System.out.println("c = " + c);

     d = new AlgebraicNumber<ModInteger>(fac1,m0);
     //System.out.println("d = " + d);
     d = d.inverse();
     //System.out.println("d = " + d);

     e = fac01.chineseRemainder(b,d,c);
     //System.out.println("e = " + e);

     assertEquals("cra(a mod (x-m0),a mod (x-m1)) = a (mod 19)",a,e);


     cfac = new ModIntegerRing(getPrime());
     //System.out.println("cfac = " + cfac.getModul());
     m0fac = new GenPolynomialRing<ModInteger>( cfac, 0 );
     //System.out.println("m0fac = " + m0fac);
     mfac = new GenPolynomialRing<ModInteger>( cfac, 1 );
     //System.out.println("mfac = " + mfac);

     x0 = m0fac.getONE();
     //System.out.println("x0 = " + x0);
     x = x0.extend(mfac,0,1);
     //System.out.println("x  = " + x);

     m0 = mfac.fromInteger(21);
     m1 = mfac.fromInteger(57);
     //System.out.println("m0 = " + m0);
     //System.out.println("m1 = " + m1);

     m0 = x.subtract(m0);
     m1 = x.subtract(m1);
     //System.out.println("m0 = " + m0);
     //System.out.println("m1 = " + m1);

     m01 = m0.multiply(m1);
     //System.out.println("m01 = " + m01);

     fac0 = new AlgebraicNumberRing<ModInteger>( m0, true );
     fac1 = new AlgebraicNumberRing<ModInteger>( m1, true );
     fac01 = new AlgebraicNumberRing<ModInteger>( m01, false );
     //System.out.println("fac0 = " + fac0);
     //System.out.println("fac1 = " + fac1);
     //System.out.println("fac01 = " + fac01);

     for ( int i = 0; i < 5; i++ ) {
         a = fac01.random( 9 );
         //System.out.println("a = " + a);
         b = new AlgebraicNumber<ModInteger>(fac0,a.getVal());
         //System.out.println("b = " + b);
         c = new AlgebraicNumber<ModInteger>(fac1,a.getVal());
         //System.out.println("c = " + c);

         d = new AlgebraicNumber<ModInteger>(fac1,m0);
         //System.out.println("d = " + d);
         d = d.inverse();
         //System.out.println("d = " + d);

         e = fac01.chineseRemainder(b,d,c);
         //System.out.println("e = " + e);

         assertEquals("cra(a mod (x-m0),a mod (x-m1)) = a (mod 2^60-93)",a,e);
     }
 }

/**
 * Test interpolate, is chinese remainder special case.
 * 
 */
 public void testInterpolate() {
     ModIntegerRing cfac;
     GenPolynomialRing<ModInteger> m0fac;
     GenPolynomial<ModInteger> x0;
     GenPolynomial<ModInteger> x;
     GenPolynomial<ModInteger> m0;
     GenPolynomial<ModInteger> m1;
     GenPolynomial<ModInteger> m01;
     AlgebraicNumberRing<ModInteger> fac0;
     AlgebraicNumberRing<ModInteger> fac1;
     AlgebraicNumberRing<ModInteger> fac01;

     ModInteger cm;
     ModInteger ci;
     ModInteger di;

     cfac = new ModIntegerRing(19);
     //System.out.println("cfac = " + cfac.getModul());
     m0fac = new GenPolynomialRing<ModInteger>( cfac, 0 );
     //System.out.println("m0fac = " + m0fac);
     mfac = new GenPolynomialRing<ModInteger>( cfac, 1 );
     //System.out.println("mfac = " + mfac);

     x0 = m0fac.getONE();
     //System.out.println("x0 = " + x0);
     x = x0.extend(mfac,0,1);
     //System.out.println("x  = " + x);

     m0 = mfac.fromInteger(2);
     m1 = mfac.fromInteger(5);
     //System.out.println("m0 = " + m0);
     //System.out.println("m1 = " + m1);

     m0 = x.subtract(m0);
     m1 = x.subtract(m1);
     //System.out.println("m0 = " + m0);
     //System.out.println("m1 = " + m1);

     m01 = m0.multiply(m1);
     //System.out.println("m01 = " + m01);

     fac0 = new AlgebraicNumberRing<ModInteger>( m0, true );
     fac1 = new AlgebraicNumberRing<ModInteger>( m1, true );
     fac01 = new AlgebraicNumberRing<ModInteger>( m01, false );
     //System.out.println("fac0 = " + fac0);
     //System.out.println("fac1 = " + fac1);
     //System.out.println("fac01 = " + fac01);

     a = fac01.random( 9 );
     //System.out.println("a = " + a);
     b = new AlgebraicNumber<ModInteger>(fac0,a.getVal());
     //System.out.println("b = " + b);
     c = new AlgebraicNumber<ModInteger>(fac1,a.getVal());
     //System.out.println("c = " + c);
     cm = fac1.modul.trailingBaseCoefficient();
     //System.out.println("cm = " + cm);
     ci = c.val.trailingBaseCoefficient();
     //System.out.println("ci = " + ci);


     d = new AlgebraicNumber<ModInteger>(fac1,m0);
     //System.out.println("d = " + d);
     d = d.inverse();
     //System.out.println("d = " + d);
     di = d.val.leadingBaseCoefficient();
     //System.out.println("di = " + di);

     e = fac01.interpolate(b,di,cm,ci);
     //System.out.println("e = " + e);

     assertEquals("cra(a mod (x-m0),a mod (x-m1)) = a (mod 19)",a,e);

     cfac = new ModIntegerRing(getPrime());
     //System.out.println("cfac = " + cfac.getModul());
     m0fac = new GenPolynomialRing<ModInteger>( cfac, 0 );
     //System.out.println("m0fac = " + m0fac);
     mfac = new GenPolynomialRing<ModInteger>( cfac, 1 );
     //System.out.println("mfac = " + mfac);

     x0 = m0fac.getONE();
     //System.out.println("x0 = " + x0);
     x = x0.extend(mfac,0,1);
     //System.out.println("x  = " + x);

     m0 = mfac.fromInteger(21);
     m1 = mfac.fromInteger(57);
     //System.out.println("m0 = " + m0);
     //System.out.println("m1 = " + m1);

     m0 = x.subtract(m0);
     m1 = x.subtract(m1);
     //System.out.println("m0 = " + m0);
     //System.out.println("m1 = " + m1);

     m01 = m0.multiply(m1);
     //System.out.println("m01 = " + m01);

     fac0 = new AlgebraicNumberRing<ModInteger>( m0, true );
     fac1 = new AlgebraicNumberRing<ModInteger>( m1, true );
     fac01 = new AlgebraicNumberRing<ModInteger>( m01, false );
     //System.out.println("fac0 = " + fac0);
     //System.out.println("fac1 = " + fac1);
     //System.out.println("fac01 = " + fac01);

     for ( int i = 0; i < 5; i++ ) {
         a = fac01.random( 9 );
         //System.out.println("a = " + a);
         b = new AlgebraicNumber<ModInteger>(fac0,a.getVal());
         //System.out.println("b = " + b);
         c = new AlgebraicNumber<ModInteger>(fac1,a.getVal());
         //System.out.println("c = " + c);
         cm = fac1.modul.trailingBaseCoefficient();
         //System.out.println("cm = " + cm);
         ci = c.val.trailingBaseCoefficient();
         //System.out.println("ci = " + ci);

         d = new AlgebraicNumber<ModInteger>(fac1,m0);
         //System.out.println("d = " + d);
         d = d.inverse();
         //System.out.println("d = " + d);
         di = d.val.leadingBaseCoefficient();
         //System.out.println("di = " + di);

         e = fac01.interpolate(b,di,cm,ci);
         //System.out.println("e = " + e);

         assertEquals("cra(a mod (x-m0),a mod (x-m1)) = a (mod 2^60-93)",a,e);
     }

 }

}
