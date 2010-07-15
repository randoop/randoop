
/*
 * $Id: LocalTest.java 1241 2007-07-29 09:25:03Z kredel $
 */

package edu.jas.application;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

import edu.jas.arith.BigRational;

//import edu.jas.structure.RingElem;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;


/**
 * Local tests with JUnit. 
 * @author Heinz Kredel.
 */

public class LocalTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
       //BasicConfigurator.configure();
       junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>LocalTest</CODE> object.
 * @param name String.
 */
   public LocalTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(LocalTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   Ideal<BigRational> id;
   LocalRing<BigRational> fac;
   GenPolynomialRing<BigRational> mfac;
   List<GenPolynomial<BigRational>> F;

   Local< BigRational > a;
   Local< BigRational > b;
   Local< BigRational > c;
   Local< BigRational > d;
   Local< BigRational > e;

   int rl = 3; 
   int kl = 3;
   int ll = 5;
   int el = 2;
   float q = 0.3f;
   int il = 2; 

   protected void setUp() {
       a = b = c = d = e = null;
       mfac = new GenPolynomialRing<BigRational>( new BigRational(1), rl );
       id = null;
       while ( id == null || id.isONE() ) {
           F = new ArrayList<GenPolynomial<BigRational>>( il );
           for ( int i = 0; i < il; i++ ) {
               GenPolynomial<BigRational> mo = mfac.random(kl,ll,el,q);
               while ( mo.isConstant() ) {
                    mo = mfac.random(kl,ll,el,q);
               }
               F.add( mo );
           }
           id = new Ideal<BigRational>(mfac,F);
           id = id.GB();
       }
       fac = new LocalRing<BigRational>( id );
       F = null;
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       fac = null;
       id = null;
       mfac = null;
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstruction() {
     c = fac.getONE();
     //System.out.println("c = " + c);
     //System.out.println("c.num = " + c.num);
     assertTrue("length( c ) = 1", c.num.length() == 1);
     assertTrue("isZERO( c )", !c.isZERO() );
     assertTrue("isONE( c )", c.isONE() );

     d = fac.getZERO();
     //System.out.println("d = " + d);
     //System.out.println("d.num = " + d.num);
     assertTrue("length( d ) = 0", d.num.length() == 0);
     assertTrue("isZERO( d )", d.isZERO() );
     assertTrue("isONE( d )", !d.isONE() );
 }


/**
 * Test random polynomial.
 * 
 */
 public void testRandom() {
     for (int i = 0; i < 4; i++) {
         //a = fac.random(ll+i);
         a = fac.random(kl*(i+1), ll+i, el, q );
         //System.out.println("a = " + a);
         assertTrue("length( a"+i+" ) <> 0", a.num.length() >= 0);
         if ( a.isZERO() || a.isONE() ) {
             continue;
         }
         assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
         assertTrue(" not isONE( a"+i+" )", !a.isONE() );
     }
 }


/**
 * Test addition.
 * Not jet working because of monic GBs.
 */
 public void testAddition() {

     a = fac.random(kl,ll,el,q);
     b = fac.random(kl,ll,el,q);
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);

     c = a.sum(b);
     d = c.subtract(b);
     assertEquals("a+b-b = a",a,d);

     c = a.sum(b);
     d = b.sum(a);
     assertEquals("a+b = b+a",c,d);


     c = fac.random(kl,ll,el,q);
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
 * Not jet working because of monic GBs
 */

 public void testMultiplication() {

     a = fac.random(kl,ll,el,q);
     assertTrue("not isZERO( a )", !a.isZERO() );

     b = fac.random(kl,ll,el,q);
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

     c = fac.random(kl,ll,el,q);
     d = a.multiply( b.multiply(c) );
     e = (a.multiply(b)).multiply(c);

     assertEquals("a(bc) = (ab)c",d,e);
     assertTrue("a(bc) = (ab)c", d.equals(e) );

     c = a.multiply( fac.getONE() );
     d = fac.getONE().multiply( a );
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

}
