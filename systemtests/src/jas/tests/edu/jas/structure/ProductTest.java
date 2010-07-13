
/*
 * $Id: ProductTest.java 1662 2008-02-05 17:22:57Z kredel $
 */

package edu.jas.structure;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

import edu.jas.arith.BigRational;
import edu.jas.arith.BigInteger;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;


/**
 * Product test with JUnit. 
 * @author Heinz Kredel.
 */

public class ProductTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
       BasicConfigurator.configure();
       junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>ProductTest</CODE> object.
 * @param name String.
 */
   public ProductTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(ProductTest.class);
     return suite;
   }

   ProductRing<BigRational> fac;
   ModIntegerRing pfac;
   ProductRing<ModInteger> mfac;
   ProductRing<BigInteger> ifac;

   Product< BigRational > a;
   Product< BigRational > b;
   Product< BigRational > c;
   Product< BigRational > d;
   Product< BigRational > e;
   Product< BigRational > f;

   Product< ModInteger > ap;
   Product< ModInteger > bp;
   Product< ModInteger > cp;
   Product< ModInteger > dp;
   Product< ModInteger > ep;
   Product< ModInteger > fp;

   Product< BigInteger > ai;
   Product< BigInteger > bi;
   Product< BigInteger > ci;
   Product< BigInteger > di;
   Product< BigInteger > ei;
   Product< BigInteger > fi;


   int pl = 5; 
   int rl = 1; 
   int kl = 13;
   int ll = 7;
   int el = 3;
   float q = 0.9f;
   int il = 2; 
   //long p = 1152921504606846883L; // 2^60-93; 

   protected void setUp() {
       a = b = c = d = e = null;
       ap = bp = cp = dp = ep = null;
       ai = bi = ci = di = ei = null;
       BigRational cfac = new BigRational(2,3);
       fac = new ProductRing<BigRational>( cfac, pl );
       List<RingFactory<ModInteger>> lpfac 
           = new ArrayList<RingFactory<ModInteger>>();
       pfac = new ModIntegerRing( 2 );
       lpfac.add(pfac);
       pfac = new ModIntegerRing( 3 );
       lpfac.add(pfac);
       pfac = new ModIntegerRing( 5 );
       lpfac.add(pfac);
       pfac = new ModIntegerRing( 7 );
       lpfac.add(pfac);
       mfac = new ProductRing<ModInteger>( lpfac );
       BigInteger cifac = new BigInteger(3);
       ifac = new ProductRing<BigInteger>( cifac, pl );
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       ap = bp = cp = dp = ep = null;
       ai = bi = ci = di = ei = null;
       fac = null;
       pfac = null;
       mfac = null;
       ifac = null;
   }


/**
 * Test constructor for rational.
 * 
 */
 public void testRatConstruction() {
     c = fac.getONE();
     //System.out.println("c = " + c);
     assertTrue("isZERO( c )", !c.isZERO() );
     assertTrue("isONE( c )", c.isONE() );

     d = fac.getZERO();
     //System.out.println("d = " + d);
     assertTrue("isZERO( d )", d.isZERO() );
     assertTrue("isONE( d )", !d.isONE() );
 }


/**
 * Test constructor for modular.
 * 
 */
 public void testModConstruction() {
     cp = mfac.getONE();
     //System.out.println("cp = " + cp);
     assertTrue("isZERO( cp )", !cp.isZERO() );
     assertTrue("isONE( cp )", cp.isONE() );

     dp = mfac.getZERO();
     //System.out.println("dp = " + dp);
     assertTrue("isZERO( dp )", dp.isZERO() );
     assertTrue("isONE( dp )", !dp.isONE() );
 }


/**
 * Test random rational.
 * 
 */
 public void testRatRandom() {
     for (int i = 0; i < 7; i++) {
         a = fac.random(kl*(i+1));
         if ( a.isZERO() ) {
            continue;
         }
         assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
         assertTrue(" not isONE( a"+i+" )", !a.isONE() );
         a = fac.random( kl, q );
         if ( a.isZERO() ) {
            continue;
         }
         //System.out.println("a = " + a);
         assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
         assertTrue(" not isONE( a"+i+" )", !a.isONE() );
     }
 }


/**
 * Test random modular.
 * 
 */
 public void testModRandom() {
     for (int i = 0; i < 7; i++) {
         ap = mfac.random(kl,q);
         if ( ap.isZERO() ) {
            continue;
         }
         //System.out.println("ap = " + ap);
         assertTrue(" not isZERO( ap"+i+" )", !ap.isZERO() );
         assertTrue(" not isONE( ap"+i+" )", !ap.isONE() );
     }
 }


/**
 * Test rational addition.
 * 
 */
 public void testRatAddition() {

     a = fac.random(kl,q);
     b = fac.random(kl,q);

     c = a.sum(b);
     d = c.subtract(b);
     assertEquals("a+b-b = a",a,d);

     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);

     c = a.sum(b);
     d = b.sum(a);
     assertEquals("a+b = b+a",c,d);

     //System.out.println("c = " + c);
     //System.out.println("d = " + d);

     c = fac.random(kl,q);
     d = c.sum( a.sum(b) );
     e = c.sum( a ).sum(b);
     assertEquals("c+(a+b) = (c+a)+b",d,e);

     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     //System.out.println("e = " + e);

     c = a.sum( fac.getZERO() );
     d = a.subtract( fac.getZERO() );
     assertEquals("a+0 = a-0",c,d);

     //System.out.println("c = " + c);
     //System.out.println("d = " + d);

     c = fac.getZERO().sum( a );
     d = fac.getZERO().subtract( a.negate() );
     assertEquals("0+a = 0+(-a)",c,d);

     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
 }


/**
 * Test integer addition.
 * 
 */
 public void testIntAddition() {

     ai = ifac.random(kl,q);
     bi = ifac.random(kl,q);

     ci = ai.sum(bi);
     di = ci.subtract(bi);
     assertEquals("a+b-b = a",ai,di);

     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);

     ci = ai.sum(bi);
     di = bi.sum(ai);
     assertEquals("a+b = b+a",ci,di);

     //System.out.println("c = " + c);
     //System.out.println("d = " + d);

     ci = ifac.random(kl,q);
     di = ci.sum( ai.sum(bi) );
     ei = ci.sum( ai ).sum(bi);
     assertEquals("c+(a+b) = (c+a)+b",di,ei);

     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     //System.out.println("e = " + e);

     ci = ai.sum( ifac.getZERO() );
     di = ai.subtract( ifac.getZERO() );
     assertEquals("a+0 = a-0",ci,di);

     //System.out.println("c = " + c);
     //System.out.println("d = " + d);

     ci = ifac.getZERO().sum( ai );
     di = ifac.getZERO().subtract( ai.negate() );
     assertEquals("0+a = 0+(-a)",ci,di);

     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
 }


/**
 * Test modular addition.
 * 
 */
 public void testModAddition() {

     ap = mfac.random(kl,q);
     bp = mfac.random(kl,q);
     //System.out.println("a = " + a);
     //System.out.println("b = " + b);

     cp = ap.sum(bp);
     dp = cp.subtract(bp);
     assertEquals("a+b-b = a",ap,dp);

     cp = ap.sum(bp);
     dp = bp.sum(ap);
     //System.out.println("c = " + c);
     //System.out.println("d = " + d);

     assertEquals("a+b = b+a",cp,dp);

     cp = mfac.random(kl,q);
     dp = cp.sum( ap.sum(bp) );
     ep = cp.sum( ap ).sum(bp);
     assertEquals("c+(a+b) = (c+a)+b",dp,ep);


     cp = ap.sum( mfac.getZERO() );
     dp = ap.subtract( mfac.getZERO() );
     assertEquals("a+0 = a-0",cp,dp);

     cp = mfac.getZERO().sum( ap );
     dp = mfac.getZERO().subtract( ap.negate() );
     assertEquals("0+a = 0+(-a)",cp,dp);
 }


/**
 * Test rational multiplication.
 * 
 */
 public void testRatMultiplication() {

     a = fac.random(kl);
     if ( a.isZERO() ) {
        return;
     }
     assertTrue("not isZERO( a )", !a.isZERO() );

     b = fac.random(kl,q);
     if ( b.isZERO() ) {
        return;
     }
     assertTrue("not isZERO( b )", !b.isZERO() );

     c = b.multiply(a);
     d = a.multiply(b);
     //assertTrue("not isZERO( c )", !c.isZERO() );
     //assertTrue("not isZERO( d )", !d.isZERO() );

     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     e = d.subtract(c);
     assertTrue("isZERO( a*b-b*a ) " + e, e.isZERO() );

     assertTrue("a*b = b*a", c.equals(d) );
     assertEquals("a*b = b*a",c,d);

     c = fac.random(kl,q);
     //System.out.println("c = " + c);
     d = a.multiply( b.multiply(c) );
     e = (a.multiply(b)).multiply(c);

     //System.out.println("d = " + d);
     //System.out.println("e = " + e);
     //System.out.println("d-e = " + d.subtract(c) );

     assertEquals("a(bc) = (ab)c",d,e);
     assertTrue("a(bc) = (ab)c", d.equals(e) );

     c = a.divide(b);
     d = a.remainder(b);
     e = c.multiply(b).sum(d);
     f = a.multiply( e.idempotent() );

     //System.out.println("c = " + c);
     //System.out.println("d = " + d);
     //System.out.println("e = " + e);
     //System.out.println("f = " + f);
     assertEquals("a = (a/b)c+d ",e,f);

     c = a.multiply( fac.getONE() );
     d = fac.getONE().multiply( a );
     assertEquals("a*1 = 1*a",c,d);

     b = a.idempotent();
     c = a.idemComplement();
     d = b.multiply(c);
     assertEquals("idem(a)*idemComp(a) = 0",d,fac.getZERO());
     d = b.sum(c);
     assertEquals("idem(a)+idemComp(a) = 1",d,fac.getONE());

     if ( a.isUnit() ) {
        c = a.inverse();
        d = c.multiply(a);
        e = a.idempotent();
        //System.out.println("a = " + a);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        //System.out.println("e = " + e);
        assertEquals("a*1/a = 1",e,d); 
     }
 }


/**
 * Test integer multiplication.
 * 
 */
 public void testIntMultiplication() {

     ai = ifac.random(kl);
     while ( ai.isZERO() ) {
        ai = ifac.random(kl);
     }
     assertTrue("not isZERO( a )", !ai.isZERO() );

     bi = ifac.random(kl,q);
     if ( bi.isZERO() ) {
        bi = ifac.random(kl,q);
     }
     assertTrue("not isZERO( b )", !bi.isZERO() );

     ci = bi.multiply(ai);
     di = ai.multiply(bi);
     //assertTrue("not isZERO( c )", !c.isZERO() );
     //assertTrue("not isZERO( d )", !d.isZERO() );

     //System.out.println("a = " + ai);
     //System.out.println("b = " + bi);
     ei = di.subtract(ci);
     assertTrue("isZERO( a*b-b*a ) " + ei, ei.isZERO() );

     assertTrue("a*b = b*a", ci.equals(di) );
     assertEquals("a*b = b*a",ci,di);

     ci = ifac.random(kl,q);
     //System.out.println("c = " + ci);
     di = ai.multiply( bi.multiply(ci) );
     ei = (ai.multiply(bi)).multiply(ci);

     //System.out.println("d = " + di);
     //System.out.println("e = " + ei);
     //System.out.println("d-e = " + di.subtract(ci) );

     assertEquals("a(bc) = (ab)c",di,ei);
     assertTrue("a(bc) = (ab)c", di.equals(ei) );

     ci = ai.divide(bi);
     di = ai.remainder(bi);
     ei = ci.multiply(bi).sum(di);
     fi = ai.multiply( ei.idempotent() );

     //System.out.println("c = " + ci);
     //System.out.println("d = " + di);
     //System.out.println("e = " + ei);
     //System.out.println("f = " + fi);
     assertEquals("a = (a/b)c+d ",ei,fi);


     ci = ai.gcd(bi);
     di = ai.remainder(ci);
     ei = bi.remainder(ci);

     //System.out.println("c = " + ci);
     //System.out.println("d = " + di);
     //System.out.println("e = " + ei);
     assertTrue("gcd(a,b) | a ",di.isZERO());
     assertTrue("gcd(a,b) | b ",ei.isZERO());


     Product< BigInteger >[] gcd;
     gcd = ai.egcd(bi);
     ci = gcd[0];
     di = ai.remainder(ci);
     ei = bi.remainder(ci);

     //System.out.println();
     //System.out.println("c = " + ci);
     //System.out.println("d = " + di);
     //System.out.println("e = " + ei);
     assertTrue("gcd(a,b) | a ",di.isZERO());
     assertTrue("gcd(a,b) | b ",ei.isZERO());

     di = ai.multiply(gcd[1]);
     ei = bi.multiply(gcd[2]);
     fi = di.sum(ei);

     //System.out.println("c = " + ci);
     //System.out.println("c1= " + gcd[1]);
     //System.out.println("c2= " + gcd[2]);
     //System.out.println("d = " + di);
     //System.out.println("e = " + ei);
     //System.out.println("f = " + fi);
     assertEquals("gcd(a,b) = c1*a + c2*b ",ci,fi);

     ci = ai.multiply( ifac.getONE() );
     di = ifac.getONE().multiply( ai );
     assertEquals("a*1 = 1*a",ci,di);

     bi = ai.idempotent();
     ci = ai.idemComplement();
     di = bi.multiply(ci);
     assertEquals("idem(a)*idemComp(a) = 0",di,ifac.getZERO());
     di = bi.sum(ci);
     assertEquals("idem(a)+idemComp(a) = 1",di,ifac.getONE());

     if ( ai.isUnit() ) {
        ci = ai.inverse();
        di = ci.multiply(ai);
        ei = ai.idempotent();
        //System.out.println("a = " + a);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        //System.out.println("e = " + e);
        assertEquals("a*1/a = 1",ei,di); 
     }
 }


/**
 * Test modular multiplication.
 * 
 */
 public void testModMultiplication() {

     ap = mfac.random(kl,q);
     if ( ap.isZERO() ) {
        return;
     }
     assertTrue("not isZERO( a )", !ap.isZERO() );

     bp = mfac.random(kl,q);
     if ( bp.isZERO() ) {
        return;
     }
     assertTrue("not isZERO( b )", !bp.isZERO() );

     cp = bp.multiply(ap);
     dp = ap.multiply(bp);
     //assertTrue("not isZERO( c )", !cp.isZERO() );
     //assertTrue("not isZERO( d )", !dp.isZERO() );

     //System.out.println("a = " + a);
     //System.out.println("b = " + b);
     ep = dp.subtract(cp);
     assertTrue("isZERO( a*b-b*a ) " + ep, ep.isZERO() );

     assertTrue("a*b = b*a", cp.equals(dp) );
     assertEquals("a*b = b*a",cp,dp);

     cp = mfac.random(kl,q);
     //System.out.println("c = " + c);
     dp = ap.multiply( bp.multiply(cp) );
     ep = (ap.multiply(bp)).multiply(cp);

     //System.out.println("d = " + d);
     //System.out.println("e = " + e);

     //System.out.println("d-e = " + d.subtract(c) );

     assertEquals("a(bc) = (ab)c",dp,ep);
     assertTrue("a(bc) = (ab)c", dp.equals(ep) );

     cp = ap.divide(bp);
     dp = ap.remainder(bp);
     ep = cp.multiply(bp).sum(dp);
     fp = ap.multiply( ep.idempotent() );

     //System.out.println("cp = " + cp);
     //System.out.println("dp = " + dp);
     //System.out.println("ep = " + ep);
     //System.out.println("fp = " + fp);
     assertEquals("a = (a/b)c+d ",ep,fp);


     cp = ap.multiply( mfac.getONE() );
     dp = mfac.getONE().multiply( ap );
     assertEquals("a*1 = 1*a",cp,dp);

     bp = ap.idempotent();
     cp = ap.idemComplement();
     dp = bp.multiply(cp);
     assertEquals("idem(a)*idemComp(a) = 0",dp,mfac.getZERO());
     dp = bp.sum(cp);
     assertEquals("idem(a)+idemComp(a) = 1",dp,mfac.getONE());

     if ( ap.isUnit() ) {
        cp = ap.inverse();
        dp = cp.multiply(ap);
        ep = ap.idempotent();
        //System.out.println("ap = " + ap);
        //System.out.println("cp = " + cp);
        //System.out.println("dp = " + dp);
        //System.out.println("ep = " + ep);
        assertEquals("a*1/a = 1",ep,dp); 
     }
 }


}
