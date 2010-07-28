/*
 * $Id: PolyUtilAppTest.java 1744 2008-03-24 14:23:08Z kredel $
 */

package edu.jas.application;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import edu.jas.arith.BigRational;
import edu.jas.poly.TermOrder;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.AlgebraicNumber;
import edu.jas.poly.AlgebraicNumberRing;

import edu.jas.structure.Product;
import edu.jas.structure.ProductRing;
import edu.jas.application.PolyUtilApp;


/**
 * PolyUtilApp tests with JUnit.
 * @author Heinz Kredel.
 */

public class PolyUtilAppTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>PolyUtilAppTest</CODE> object.
 * @param name String.
 */
   public PolyUtilAppTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(PolyUtilAppTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   TermOrder to = new TermOrder( TermOrder.INVLEX );

   GenPolynomialRing<BigRational> dfac;
   GenPolynomialRing<BigRational> cfac;
   GenPolynomialRing<GenPolynomial<BigRational>> rfac;

   BigRational ai;
   BigRational bi;
   BigRational ci;
   BigRational di;
   BigRational ei;

   GenPolynomial<BigRational> a;
   GenPolynomial<BigRational> b;
   GenPolynomial<BigRational> c;
   GenPolynomial<BigRational> d;
   GenPolynomial<BigRational> e;

   GenPolynomial<GenPolynomial<BigRational>> ar;
   GenPolynomial<GenPolynomial<BigRational>> br;
   GenPolynomial<GenPolynomial<BigRational>> cr;
   GenPolynomial<GenPolynomial<BigRational>> dr;
   GenPolynomial<GenPolynomial<BigRational>> er;

   int rl = 5; 
   int kl = 5;
   int ll = 5;
   int el = 5;
   float q = 0.6f;

   protected void setUp() {
       a = b = c = d = e = null;
       ai = bi = ci = di = ei = null;
       ar = br = cr = dr = er = null;
       dfac = new GenPolynomialRing<BigRational>(new BigRational(1),rl,to);
       cfac = null; //new GenPolynomialRing<BigRational>(new BigRational(1),rl-1,to);
       rfac = null; //new GenPolynomialRing<GenPolynomial<BigRational>>(cfac,1,to);
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       ai = bi = ci = di = ei = null;
       ar = br = cr = dr = er = null;
       dfac = null;
       cfac = null;
       rfac = null;
   }


/**
 * Test product represenation conversion, rational numbers.
 * 
 */
 public void testProductConversionRN() {
     GenPolynomialRing<BigRational> ufac;
     ufac = new GenPolynomialRing<BigRational>(new BigRational(1),1);

     ProductRing<GenPolynomial<BigRational>> pfac;
     pfac = new ProductRing<GenPolynomial<BigRational>>( ufac, rl );

     Product<GenPolynomial<BigRational>> cp;

     c = dfac.getONE();
     //System.out.println("c = " + c);

     cp = PolyUtilApp.<BigRational>toProduct(pfac,c);
     //System.out.println("cp = " + cp);
     assertTrue("isONE( cp )", cp.isONE() );

     c = dfac.random(kl,ll,el,q);
     //System.out.println("c = " + c);

     cp = PolyUtilApp.<BigRational>toProduct(pfac,c);
     //System.out.println("cp = " + cp);
     assertTrue("!isONE( cp )", !cp.isONE() );
 }


/**
 * Test polynomal over product represenation conversion, algebraic numbers.
 * 
 */
 public void testPolyProductConversionAN() {
     GenPolynomialRing<BigRational> ufac;
     ufac = new GenPolynomialRing<BigRational>(new BigRational(1),1);

     GenPolynomial<BigRational> m;
     m = ufac.univariate(0,2);
     m = m.subtract( ufac.univariate(0,1) );
     //System.out.println("m = " + m);

     AlgebraicNumberRing<BigRational> afac;
     afac = new AlgebraicNumberRing<BigRational>(m);
     //System.out.println("afac = " + afac);

     ProductRing<AlgebraicNumber<BigRational>> pfac;
     pfac = new ProductRing<AlgebraicNumber<BigRational>>( afac, rl );

     GenPolynomialRing<Product<AlgebraicNumber<BigRational>>> dpfac;
     dpfac = new GenPolynomialRing<Product<AlgebraicNumber<BigRational>>>( pfac, 2 );

     GenPolynomialRing<AlgebraicNumber<BigRational>> dfac;
     dfac = new GenPolynomialRing<AlgebraicNumber<BigRational>>( afac, 2, to);


     GenPolynomial<AlgebraicNumber<BigRational>> c;
     GenPolynomial<Product<AlgebraicNumber<BigRational>>> cp;

     c = dfac.getONE();
     //System.out.println("c = " + c);

     cp = PolyUtilApp.<AlgebraicNumber<BigRational>>toProductGen(dpfac,c);
     //System.out.println("cp = " + cp);
     assertTrue("isZERO( cp )", cp.isONE() );
     
     c = dfac.random(kl,ll,el,q);
     //System.out.println("c = " + c);

     cp = PolyUtilApp.<AlgebraicNumber<BigRational>>toProductGen(dpfac,c);
     //System.out.println("cp = " + cp);
     assertTrue("!isONE( cp )", !cp.isONE() );
 }

}
