/*
 * $Id: ModGenSolvablePolynomialTest.java 1255 2007-07-29 10:16:33Z kredel $
 */

package edu.jas.poly;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;
//import edu.jas.structure.RingElem;


/**
 * ModInteger coefficients GenSolvablePolynomial tests with JUnit.
 * @author Heinz Kredel.
 */

public class ModGenSolvablePolynomialTest extends TestCase {

/**
 * main
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>ModGenSolvablePolynomialTest</CODE> object.
 * @param name String.
 */
   public ModGenSolvablePolynomialTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(ModGenSolvablePolynomialTest.class);
     return suite;
   }


   GenSolvablePolynomial<ModInteger> a;
   GenSolvablePolynomial<ModInteger> b;
   GenSolvablePolynomial<ModInteger> c;
   GenSolvablePolynomial<ModInteger> d;
   GenSolvablePolynomial<ModInteger> e;
   GenSolvablePolynomial<ModInteger> f;
   GenSolvablePolynomial<ModInteger> x1;
   GenSolvablePolynomial<ModInteger> x2;

   int ml = 19; // modul 
   int rl = 5; 
   int kl = 10;
   int ll = 5;
   int el = 3;
   float q = 0.5f;

   RelationTable<ModInteger> table;
   GenSolvablePolynomialRing<ModInteger> ring;
   ModIntegerRing cfac;

   protected void setUp() {
       cfac = new ModIntegerRing(ml);
       ring = new GenSolvablePolynomialRing<ModInteger>(cfac,rl);
       table = null; //ring.table;
       a = b = c = d = e = null;
   }

   protected void tearDown() {
       table = null;
       ring = null;
       a = b = c = d = e = null;
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstructor() {
     a = new GenSolvablePolynomial<ModInteger>(ring);
     assertTrue("length( a ) = 0", a.length() == 0);
     assertTrue("isZERO( a )", a.isZERO() );
     assertTrue("isONE( a )", !a.isONE() );

     c = ring.getONE();
     assertTrue("length( c ) = 1", c.length() == 1);
     assertTrue("isZERO( c )", !c.isZERO() );
     assertTrue("isONE( c )", c.isONE() );

     d = ring.getZERO();
     assertTrue("length( d ) = 0", d.length() == 0);
     assertTrue("isZERO( d )", d.isZERO() );
     assertTrue("isONE( d )", !d.isONE() );
 }


/**
 * Test random polynomial.
 * 
 */
 public void testRandom() {
     assertTrue("isCommutative()",ring.isCommutative());

     for (int i = 0; i < 2; i++) {
         // a = ring.random(ll+2*i);
         a = ring.random(kl*(i+1), ll+2*i, el+i, q );
         assertTrue("length( a"+i+" ) <> 0", a.length() >= 0);
         assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
         assertTrue(" not isONE( a"+i+" )", !a.isONE() );
     }
 }


/**
 * Test addition.
 * 
 */
 public void testAddition() {

     a = ring.random(kl, ll, el, q );

     c = (GenSolvablePolynomial<ModInteger>)a.subtract(a);
     assertTrue("a-a = 0", c.isZERO() );

     b = (GenSolvablePolynomial<ModInteger>)a.sum(a);
     c = (GenSolvablePolynomial<ModInteger>)b.subtract(a);

     assertEquals("a+a-a = a",c,a);
     assertTrue("a+a-a = a", c.equals(a) );

     b = ring.random(kl, ll, el, q );
     c = (GenSolvablePolynomial<ModInteger>)b.sum(a);
     d = (GenSolvablePolynomial<ModInteger>)a.sum(b);

     assertEquals("a+b = b+a",c,d);
     assertTrue("a+b = b+a", c.equals(d) );

     c = ring.random(kl, ll, el, q );
     d = (GenSolvablePolynomial<ModInteger>)a.sum(b.sum(c));
     e = (GenSolvablePolynomial<ModInteger>)a.sum(b).sum(c);

     assertEquals("a+(b+c) = (a+b)+c",d,e);
     assertTrue("a+(b+c) = (a+b)+c", d.equals(e) );

     ExpVector u = ExpVector.EVRAND(rl,el,q);
     ModInteger x = cfac.random(kl);

     b = ring.getONE().multiply( x, u);
     c = (GenSolvablePolynomial<ModInteger>)a.sum(b);
     d = (GenSolvablePolynomial<ModInteger>)a.sum(x,u);
     assertEquals("a+p(x,u) = a+(x,u)",c,d);

     c = (GenSolvablePolynomial<ModInteger>)a.subtract(b);
     d = (GenSolvablePolynomial<ModInteger>)a.subtract(x,u);
     assertEquals("a-p(x,u) = a-(x,u)",c,d);

     a = ring.getZERO();
     b = ring.getONE().multiply( x, u);
     c = (GenSolvablePolynomial<ModInteger>)b.sum(a);
     d = (GenSolvablePolynomial<ModInteger>)a.sum(x,u);
     assertEquals("a+p(x,u) = a+(x,u)",c,d);

     c = (GenSolvablePolynomial<ModInteger>)a.subtract(b);
     d = (GenSolvablePolynomial<ModInteger>)a.subtract(x,u);
     assertEquals("a-p(x,u) = a-(x,u)",c,d);
 }


/**
 * Test object multiplication.
 * 
 */

 public void testMultiplication() {

     a = ring.random(kl, ll, el, q );
     assertTrue("not isZERO( a )", !a.isZERO() );
     //a = ModGenSolvablePolynomial.DIRRAS(1, kl, 4, el, q );

     b = ring.random(kl, ll, el, q );
     assertTrue("not isZERO( b )", !b.isZERO() );

     c = b.multiply(a);
     d = a.multiply(b);
     assertTrue("not isZERO( c )", !c.isZERO() );
     assertTrue("not isZERO( d )", !d.isZERO() );

     e = (GenSolvablePolynomial<ModInteger>)d.subtract(c);
     assertTrue("isZERO( a*b-b*a ) " + e, e.isZERO() );

     assertEquals("a*b = b*a",c,d);
     assertTrue("a*b = b*a", c.equals(d) );

     c = ring.random(kl, ll, el, q );
     d = a.multiply( b.multiply(c) );
     e = (a.multiply(b)).multiply(c);

     assertEquals("a(bc) = (ab)c",d,e);
     assertTrue("a(bc) = (ab)c", d.equals(e) );

     ModInteger x = a.leadingBaseCoefficient().inverse();
     c = (GenSolvablePolynomial<ModInteger>)a.monic();
     d = a.multiply(x);
     assertEquals("a.monic() = a(1/ldcf(a))",c,d);

     ExpVector u = ring.evzero;
     ModInteger y = b.leadingBaseCoefficient().inverse();
     c = (GenSolvablePolynomial<ModInteger>)b.monic();
     d = b.multiply(y,u);
     assertEquals("b.monic() = b(1/ldcf(b))",c,d);

     e = ring.getONE().multiply(y,u);
     d = b.multiply(e);
     assertEquals("b.monic() = b(1/ldcf(b))",c,d);

     d = e.multiply(b);
     assertEquals("b.monic() = (1/ldcf(b) (0))*b",c,d);
 }


/**
 * Test Weyl polynomials.
 * 
 */

 public void testWeyl() {

     int rloc = 4;
     ring = new GenSolvablePolynomialRing<ModInteger>(cfac,rloc);

     WeylRelations<ModInteger> wl = new WeylRelations<ModInteger>(ring);
     wl.generate();
     //table = ring.table;
     //System.out.println("table = " + table);
     //System.out.println("ring = " + ring);

     assertFalse("isCommutative()",ring.isCommutative());
     assertTrue("isAssociative()", ring.isAssociative());

     a = ring.random(kl, ll, el+2, q );
     assertTrue("not isZERO( a )", !a.isZERO() );
     //System.out.println("a = " + a);

     b = ring.random(kl, ll, el+2, q );
     assertTrue("not isZERO( b )", !b.isZERO() );
     //System.out.println("b = " + b);


     // non commutative
     c = b.multiply(a);
     d = a.multiply(b);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     assertTrue("not isZERO( c )", !c.isZERO() );
     assertTrue("not isZERO( d )", !d.isZERO() );

     e = (GenSolvablePolynomial<ModInteger>)d.subtract(c);
     assertTrue("!isZERO( a*b-b*a ) " + e, !e.isZERO() );
     assertTrue("a*b != b*a", !c.equals(d) );


     c = ring.random(kl, ll, el, q );
     //System.out.println("\na = " + a);
     //System.out.println("\nb = " + b);
     //System.out.println("\nc = " + c);

     // associative
     //x1 = b.multiply(c);
     //System.out.println("\nx1 = " + x1);
     d = a.multiply( b.multiply(c) );

     //x2 = a.multiply(b);
     //System.out.println("\nx2 = " + x2);
     e = a.multiply(b).multiply(c);

     //System.out.println("\nd = " + d);
     //System.out.println("\ne = " + e);

     //f = (GenSolvablePolynomial<ModInteger>)d.subtract(e);
     //System.out.println("\nf = " + f);

     assertEquals("a(bc) = (ab)c",d,e);
     assertTrue("a(bc) = (ab)c", d.equals(e) );
 }


/**
 * Test distributive law.
 * 
 */
 public void testDistributive() {
     int rloc = 4;
     ring = new GenSolvablePolynomialRing<ModInteger>(cfac,rloc);

     WeylRelations<ModInteger> wl = new WeylRelations<ModInteger>(ring);
     wl.generate();
     //table = ring.table;
     //System.out.println("table = " + table);
     //System.out.println("ring = " + ring);

     a = ring.random(kl,ll,el,q);
     b = ring.random(kl,ll,el,q);
     c = ring.random(kl,ll,el,q);

     d = a.multiply( (GenSolvablePolynomial<ModInteger>)b.sum(c) );
     e = (GenSolvablePolynomial<ModInteger>)a.multiply( b ).sum( a.multiply(c) );

     assertEquals("a(b+c) = ab+ac",d,e);
 }

}
