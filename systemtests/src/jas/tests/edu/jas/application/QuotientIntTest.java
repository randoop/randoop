
/*
 * $Id: QuotientIntTest.java 1886 2008-07-12 13:36:42Z kredel $
 */

package edu.jas.application;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

//import edu.jas.arith.BigRational;
import edu.jas.arith.BigInteger;

import edu.jas.kern.PrettyPrint;
import edu.jas.kern.ComputerThreads;

import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.TermOrder;


/**
 * Quotient over BigInteger GenPolynomial tests with JUnit. 
 * @author Heinz Kredel.
 */

public class QuotientIntTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
       BasicConfigurator.configure();
       junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>QuotientIntTest</CODE> object.
 * @param name String.
 */
   public QuotientIntTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(QuotientIntTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   QuotientRing<BigInteger> efac;
   GenPolynomialRing<BigInteger> mfac;

   Quotient< BigInteger > a;
   Quotient< BigInteger > b;
   Quotient< BigInteger > c;
   Quotient< BigInteger > d;
   Quotient< BigInteger > e;

   int rl = 3; 
   int kl = 5;
   int ll = 4; //6;
   int el = 2;
   float q = 0.4f;

   protected void setUp() {
       a = b = c = d = e = null;
       BigInteger cfac = new BigInteger(1);
       TermOrder to = new TermOrder( TermOrder.INVLEX );
       mfac = new GenPolynomialRing<BigInteger>( cfac, rl, to );
       efac = new QuotientRing<BigInteger>( mfac );
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       //efac.terminate();
       efac = null;
       ComputerThreads.terminate();
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstruction() {
     c = efac.getONE();
     //System.out.println("c = " + c);
     //System.out.println("c.val = " + c.val);
     assertTrue("length( c ) = 1", c.num.length() == 1);
     assertTrue("isZERO( c )", !c.isZERO() );
     assertTrue("isONE( c )", c.isONE() );

     d = efac.getZERO();
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
         //a = efac.random(ll+i);
         a = efac.random(kl*(i+1), ll+2+2*i, el, q );
         //System.out.println("a = " + a);
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

     a = efac.random(kl,ll,el,q);
     b = efac.random(kl,ll,el,q);
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);

     c = a.sum(b);
     d = c.subtract(b);
     assertEquals("a+b-b = a",a,d);

     c = a.sum(b);
     d = b.sum(a);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);

     assertEquals("a+b = b+a",c,d);

     c = efac.random(kl,ll,el,q);
     d = c.sum( a.sum(b) );
     e = c.sum( a ).sum(b);
     assertEquals("c+(a+b) = (c+a)+b",d,e);


     c = a.sum( efac.getZERO() );
     d = a.subtract( efac.getZERO() );
     assertEquals("a+0 = a-0",c,d);

     c = efac.getZERO().sum( a );
     d = efac.getZERO().subtract( a.negate() );
     assertEquals("0+a = 0+(-a)",c,d);

 }


/**
 * Test object multiplication.
 * 
 */
 public void testMultiplication() {

     a = efac.random(kl,ll,el,q);
     assertTrue("not isZERO( a )", !a.isZERO() );

     b = efac.random(kl,ll,el,q);
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

     c = efac.random(kl,ll,el,q);
     //System.out.println("c = " + c);
     d = a.multiply( b.multiply(c) );
     e = (a.multiply(b)).multiply(c);

     //System.out.println("d = " + d);
     //System.out.println("e = " + e);

     //System.out.println("d-e = " + d.subtract(c) );

     assertEquals("a(bc) = (ab)c",d,e);
     assertTrue("a(bc) = (ab)c", d.equals(e) );

     c = a.multiply( efac.getONE() );
     d = efac.getONE().multiply( a );
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
 * Test parse().
 * 
 */
 public void testParse() {
     a = efac.random(kl*2,ll*2,el*2,q*2);
     assertTrue("not isZERO( a )", !a.isZERO() );

     //PrettyPrint.setInternal();
     //System.out.println("a = " + a);
     PrettyPrint.setPretty();
     //System.out.println("a = " + a);
     String p = a.toString();
     //System.out.println("p = " + p);
     b = efac.parse(p);
     //System.out.println("b = " + b);

     //c = a.subtract(b);
     //System.out.println("c = " + c);
     assertEquals("parse(a.toSting()) = a",a,b);
 }

}
