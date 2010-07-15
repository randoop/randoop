
/*
 * $Id: QuotientRatTest.java 1241 2007-07-29 09:25:03Z kredel $
 */

package edu.jas.application;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

import edu.jas.arith.BigRational;

import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.TermOrder;

import edu.jas.kern.ComputerThreads;


/**
 * Quotient over BigRational GenPolynomial tests with JUnit. 
 * @author Heinz Kredel.
 */

public class QuotientRatTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
       BasicConfigurator.configure();
       junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>QuotientRatTest</CODE> object.
 * @param name String.
 */
   public QuotientRatTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(QuotientRatTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   QuotientRing<BigRational> zFac;
   QuotientRing<BigRational> eFac;
   GenPolynomialRing<BigRational> mfac;

   Quotient< BigRational > a;
   Quotient< BigRational > b;
   Quotient< BigRational > c;
   Quotient< BigRational > d;
   Quotient< BigRational > e;
   Quotient< BigRational > az;
   Quotient< BigRational > bz;
   Quotient< BigRational > cz;
   Quotient< BigRational > dz;
   Quotient< BigRational > ez;

   int rl = 3; 
   int kl = 5;
   int ll = 3; //6;
   int el = 2;
   float q = 0.4f;

   protected void setUp() {
       a = b = c = d = e = null;
       TermOrder to = new TermOrder( TermOrder.INVLEX );
       mfac = new GenPolynomialRing<BigRational>( new BigRational(1), rl, to );
       eFac = new QuotientRing<BigRational>( mfac );
       zFac = new QuotientRing<BigRational>( mfac, false );
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       //eFac.terminate();
       eFac = null;
       zFac = null;
       ComputerThreads.terminate();
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstruction() {
     c = eFac.getONE();
     //System.out.println("c = " + c);
     //System.out.println("c.val = " + c.val);
     assertTrue("length( c ) = 1", c.num.length() == 1);
     assertTrue("isZERO( c )", !c.isZERO() );
     assertTrue("isONE( c )", c.isONE() );

     d = eFac.getZERO();
     //System.out.println("d = " + d);
     //System.out.println("d.val = " + d.val);
     assertTrue("length( d ) = 0", d.num.length() == 0);
     assertTrue("isZERO( d )", d.isZERO() );
     assertTrue("isONE( d )", !d.isONE() );
 }


/**
 * Test random polynomial.
 * 
 */
 public void testRandom() {
     for (int i = 0; i < 7; i++) {
         //a = eFac.random(ll+i);
         a = eFac.random(kl*(i+1), ll+2+2*i, el, q );
         //System.out.println("a = " + a);
         if ( a.isZERO() || a.isONE() ) {
            continue;
         }
         assertTrue("length( a"+i+" ) <> 0", a.num.length() >= 0);
         assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
         assertTrue(" not isONE( a"+i+" )", !a.isONE() );
     }
 }


/**
 * Test addition.
 * 
 */
 public void testAddition() {

     a = eFac.random(kl,ll,el,q);
     b = eFac.random(kl,ll,el,q);
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);

     c = a.sum(b);
     d = c.subtract(b);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     d = d.monic();
     //System.out.println("d = " + d);
     assertEquals("a+b-b = a",a,d);

     c = a.sum(b);
     d = b.sum(a);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+b = b+a",c,d);

     //System.out.println("monic(d) = " + d.monic());

     c = eFac.random(kl,ll,el,q);
     //System.out.println("c = " + c);
     d = c.sum( a.sum(b) );
     e = c.sum( a ).sum(b);
     //System.out.println("d = " + d);
     //System.out.println("e = " + e);
     assertEquals("c+(a+b) = (c+a)+b",d,e);


     c = a.sum( eFac.getZERO() );
     d = a.subtract( eFac.getZERO() );
     assertEquals("a+0 = a-0",c,d);

     c = eFac.getZERO().sum( a );
     d = eFac.getZERO().subtract( a.negate() );
     assertEquals("0+a = 0+(-a)",c,d);
 }


/**
 * Test object multiplication.
 * 
 */
 public void testMultiplication() {

     a = eFac.random(kl,ll,el,q);
     assertTrue("not isZERO( a )", !a.isZERO() );

     b = eFac.random(kl,ll,el,q);
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

     c = eFac.random(kl,ll,el,q);
     //System.out.println("c = " + c);
     d = a.multiply( b.multiply(c) );
     e = (a.multiply(b)).multiply(c);

     //System.out.println("d = " + d);
     //System.out.println("e = " + e);

     //System.out.println("d-e = " + d.subtract(c) );

     assertEquals("a(bc) = (ab)c",d,e);
     assertTrue("a(bc) = (ab)c", d.equals(e) );

     c = a.multiply( eFac.getONE() );
     d = eFac.getONE().multiply( a );
     assertEquals("a*1 = 1*a",c,d);

     if ( a.isUnit() ) {
        c = a.inverse();
        d = c.multiply(a);
        //System.out.println("a = " + a);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertTrue("a*1/a = 1",d.isONE()); 
     }
 }


/**
 * Test addition with syzygy gcd and euclids gcd.
 * 
 */
 public void xtestAdditionGcd() {

     long te, tz;

     a = eFac.random(kl,ll,el,q);
     b = eFac.random(kl,ll,el,q);
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);

     az = new Quotient<BigRational>(zFac,a.num,a.den,true);
     bz = new Quotient<BigRational>(zFac,b.num,b.den,true);

     te = System.currentTimeMillis();
     c = a.sum(b);
     d = c.subtract(b);
     d = d.monic();
     te = System.currentTimeMillis() - te;
     assertEquals("a+b-b = a",a,d);

     tz = System.currentTimeMillis();
     cz = az.sum(bz);
     dz = cz.subtract(bz);
     dz = dz.monic();
     tz = System.currentTimeMillis() - tz;
     assertEquals("a+b-b = a",az,dz);

     System.out.println("te = " + te);
     System.out.println("tz = " + tz);

     c = a.sum(b);
     d = b.sum(a);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertEquals("a+b = b+a",c,d);

     c = eFac.random(kl,ll,el,q);
     cz = new Quotient<BigRational>(zFac,c.num,c.den,true);


     te = System.currentTimeMillis();
     d = c.sum( a.sum(b) );
     e = c.sum( a ).sum(b);
     te = System.currentTimeMillis() - te;
     assertEquals("c+(a+b) = (c+a)+b",d,e);

     tz = System.currentTimeMillis();
     dz = cz.sum( az.sum(bz) );
     ez = cz.sum( az ).sum(bz);
     tz = System.currentTimeMillis() - tz;
     assertEquals("c+(a+b) = (c+a)+b",dz,ez);

     System.out.println("te = " + te);
     System.out.println("tz = " + tz);

     c = a.sum( eFac.getZERO() );
     d = a.subtract( eFac.getZERO() );
     assertEquals("a+0 = a-0",c,d);

     c = eFac.getZERO().sum( a );
     d = eFac.getZERO().subtract( a.negate() );
     assertEquals("0+a = 0+(-a)",c,d);
 }

}
