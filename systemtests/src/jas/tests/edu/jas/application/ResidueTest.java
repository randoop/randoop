
/*
 * $Id: ResidueTest.java 1663 2008-02-05 17:32:07Z kredel $
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
 * Residue tests with JUnit. 
 * @author Heinz Kredel.
 */

public class ResidueTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
       //BasicConfigurator.configure();
       junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>ResidueTest</CODE> object.
 * @param name String.
 */
   public ResidueTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(ResidueTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   Ideal<BigRational> id;
   ResidueRing<BigRational> fac;
   GenPolynomialRing<BigRational> mfac;
   List<GenPolynomial<BigRational>> F;

   Residue< BigRational > a;
   Residue< BigRational > b;
   Residue< BigRational > c;
   Residue< BigRational > d;
   Residue< BigRational > e;

   int rl = 3; 
   int kl = 3;
   int ll = 7;
   int el = 3;
   float q = 0.4f;
   int il = 2; 

   protected void setUp() {
       a = b = c = d = e = null;
       mfac = new GenPolynomialRing<BigRational>( new BigRational(1), rl );
       F = new ArrayList<GenPolynomial<BigRational>>( il );
       for ( int i = 0; i < il; i++ ) {
           GenPolynomial<BigRational> mo = mfac.random(kl,ll,el,q);
           while ( mo.isConstant() ) {
                 mo = mfac.random(kl,ll,el,q);
           }
           F.add( mo );
       }
       id = new Ideal<BigRational>(mfac,F);
       fac = new ResidueRing<BigRational>( id );
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
     //System.out.println("c.val = " + c.val);
     assertTrue("length( c ) = 1", c.val.length() == 1);
     assertTrue("isZERO( c )", !c.isZERO() );
     assertTrue("isONE( c )", c.isONE() );

     d = fac.getZERO();
     //System.out.println("d = " + d);
     //System.out.println("d.val = " + d.val);
     assertTrue("length( d ) = 0", d.val.length() == 0);
     assertTrue("isZERO( d )", d.isZERO() );
     assertTrue("isONE( d )", !d.isONE() );
 }


/**
 * Test random polynomial.
 * 
 */
 public void testRandom() {
     for (int i = 0; i < 7; i++) {
         //a = fac.random(ll+i);
         a = fac.random(kl*(i+1), ll+2*i, el+i, q );
         //System.out.println("a = " + a);
      if ( a.isZERO() || a.isONE() ) {
            continue;
      }
         assertTrue("length( a"+i+" ) <> 0", a.val.length() >= 0);
         assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
         assertTrue(" not isONE( a"+i+" )", !a.isONE() );
     }
 }


/**
 * Test addition.
 * 
 */
 public void testAddition() {

     a = fac.random(kl,ll,el,q);
     b = fac.random(kl,ll,el,q);

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
 * 
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
