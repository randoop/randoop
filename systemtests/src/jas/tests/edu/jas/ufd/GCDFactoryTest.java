/*
 * $Id: GCDFactoryTest.java 1355 2007-09-07 20:46:40Z kredel $
 */

package edu.jas.ufd;

//import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

import edu.jas.arith.BigInteger;
import edu.jas.arith.BigRational;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;
//import edu.jas.arith.BigComplex;

import edu.jas.poly.AlgebraicNumber;
import edu.jas.poly.AlgebraicNumberRing;
//import edu.jas.poly.ExpVector;
import edu.jas.poly.TermOrder;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
//import edu.jas.poly.PolyUtil;


/**
 * GreatestCommonDivisor factory tests with JUnit.
 * @author Heinz Kredel.
 */

public class GCDFactoryTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>GCDFactoryTest</CODE> object.
 * @param name String.
 */
   public GCDFactoryTest(String name) {
          super(name);
   }

/**
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GCDFactoryTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   TermOrder to = new TermOrder( TermOrder.INVLEX );

   GenPolynomialRing<BigInteger> dfac;
   GenPolynomialRing<BigInteger> cfac;
   GenPolynomialRing<GenPolynomial<BigInteger>> rfac;

   BigInteger ai;
   BigInteger bi;
   BigInteger ci;
   BigInteger di;
   BigInteger ei;

   GenPolynomial<BigInteger> a;
   GenPolynomial<BigInteger> b;
   GenPolynomial<BigInteger> c;
   GenPolynomial<BigInteger> d;
   GenPolynomial<BigInteger> e;

   GenPolynomial<GenPolynomial<BigInteger>> ar;
   GenPolynomial<GenPolynomial<BigInteger>> br;
   GenPolynomial<GenPolynomial<BigInteger>> cr;
   GenPolynomial<GenPolynomial<BigInteger>> dr;
   GenPolynomial<GenPolynomial<BigInteger>> er;

   int rl = 5; 
   int kl = 4;
   int ll = 5;
   int el = 3;
   float q = 0.3f;

   protected void setUp() {
       a = b = c = d = e = null;
       ai = bi = ci = di = ei = null;
       ar = br = cr = dr = er = null;
       dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),rl,to);
       cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),rl-1,to);
       rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac,1,to);
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
 * Test get BigInteger implementation.
 * 
 */
 public void testBigInteger() {
     BigInteger bi = new BigInteger();
     GreatestCommonDivisor<BigInteger> ufd; 
     
     ufd = GCDFactory./*<BigInteger>*/getImplementation(bi);
     //System.out.println("ufd = " + ufd);
     assertTrue("ufd = Modular " + ufd, ufd instanceof GreatestCommonDivisorModular);
 }


/**
 * Test get ModInteger implementation.
 * 
 */ 
 public void testModInteger() {
     ModIntegerRing mi = new ModIntegerRing(19,true);
     GreatestCommonDivisor<ModInteger> ufd; 

     ufd = GCDFactory./*<ModInteger>*/getImplementation(mi);
     //System.out.println("ufd = " + ufd);
     assertTrue("ufd != ModEval " + ufd, ufd instanceof GreatestCommonDivisorModEval);

     mi = new ModIntegerRing(30);
     ufd = GCDFactory./*<ModInteger>*/getImplementation(mi);
     //System.out.println("ufd = " + ufd);
     assertTrue("ufd != Subres " + ufd, ufd instanceof GreatestCommonDivisorSubres);
 }


/**
 * Test get BigRational implementation.
 * 
 */
 public void testBigRational() {
     BigRational b = new BigRational();
     GreatestCommonDivisor<BigRational> ufd; 

     ufd = GCDFactory./*<BigRational>*/getImplementation(b);
     //System.out.println("ufd = " + ufd);
     assertTrue("ufd = Primitive " + ufd, ufd instanceof GreatestCommonDivisorPrimitive);
 }


/**
 * Test get BigComplex implementation.
 * 
 public void testBigComplex() {
     BigComplex b = new BigComplex();
     GreatestCommonDivisor<BigComplex> ufd; 

     ufd = GCDFactory.<BigComplex>getImplementation(b);
     //System.out.println("ufd = " + ufd);
     assertTrue("ufd != Simple " + ufd, ufd instanceof GreatestCommonDivisorSimple);
 }
 */


/**
 * Test get AlgebraicNumber<BigRational> implementation.
 * 
 */
 public void testAlgebraicNumberBigRational() {
     BigRational b = new BigRational();
     GenPolynomialRing<BigRational> fac;
     fac = new GenPolynomialRing<BigRational>( b, 1 );
     GenPolynomial<BigRational> mo = fac.random(kl,ll,el,q);
     while ( mo.isZERO() || mo.isONE() || mo.isConstant() ) {
          mo = fac.random(kl,ll,el,q);
     }

     AlgebraicNumberRing<BigRational> afac;
     afac = new AlgebraicNumberRing<BigRational>( mo );

     GreatestCommonDivisor<AlgebraicNumber<BigRational>> ufd; 

     ufd = GCDFactory.<AlgebraicNumber<BigRational>>getImplementation(afac);
     //System.out.println("ufd = " + ufd);
     assertTrue("ufd = Subres " + ufd, ufd instanceof GreatestCommonDivisorSubres);


     mo = fac.univariate(0).subtract( fac.getONE() );
     afac = new AlgebraicNumberRing<BigRational>( mo, true );

     ufd = GCDFactory.<AlgebraicNumber<BigRational>>getImplementation(afac);
     //System.out.println("ufd = " + ufd);
     assertTrue("ufd = Simple " + ufd, ufd instanceof GreatestCommonDivisorSimple);

 }


/**
 * Test get AlgebraicNumber<ModInteger> implementation.
 * 
 */
 public void testAlgebraicNumberModInteger() {
     ModIntegerRing b = new ModIntegerRing(19,true);
     GenPolynomialRing<ModInteger> fac;
     fac = new GenPolynomialRing<ModInteger>( b, 1 );
     GenPolynomial<ModInteger> mo = fac.random(kl,ll,el,q);
     while ( mo.isZERO() || mo.isONE() || mo.isConstant() ) {
          mo = fac.random(kl,ll,el,q);
     }

     AlgebraicNumberRing<ModInteger> afac;
     afac = new AlgebraicNumberRing<ModInteger>( mo );


     AlgebraicNumber<ModInteger> a = afac.getONE();
     GreatestCommonDivisor<AlgebraicNumber<ModInteger>> ufd; 

     ufd = GCDFactory.<AlgebraicNumber<ModInteger>>getImplementation(afac);
     //System.out.println("ufd = " + ufd);
     assertTrue("ufd = Subres " + ufd, ufd instanceof GreatestCommonDivisorSubres);
 }


}
