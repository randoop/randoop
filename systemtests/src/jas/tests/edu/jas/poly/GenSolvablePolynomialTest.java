/*
 * $Id: GenSolvablePolynomialTest.java 1255 2007-07-29 10:16:33Z kredel $
 */

package edu.jas.poly;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import edu.jas.arith.BigRational;

import edu.jas.poly.GenSolvablePolynomial;
import edu.jas.poly.GenSolvablePolynomialRing;

import edu.jas.structure.RingElem;
//import edu.jas.structure.RingFactory;


/**
 * GenSolvablePolynomial Test using JUnit.
 * <b>Note:</b> not optimal since GenSolvablePolynomial does not 
 * implement RingElem&lt;GenSolvablePolynomial&gt;
 * @author Heinz Kredel.
 */

public class GenSolvablePolynomialTest extends TestCase {

/**
 * main
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>GenSolvablePolynomialTest</CODE> object.
 * @param name String.
 */
   public GenSolvablePolynomialTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GenSolvablePolynomialTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   int rl = 6;  // even for Weyl 
   int kl = 10;
   int ll = 7;
   int el = 4;
   float q = 0.5f;

   protected void setUp() {
       // a = b = c = d = e = null;
   }

   protected void tearDown() {
       // a = b = c = d = e = null;
   }


/**
 * Test constructors and factory.
 * 
 */
 public void testConstructors() {
        // rational numbers
        BigRational rf = new BigRational();
        // System.out.println("rf = " + rf);

        BigRational r = rf.fromInteger( 99 );
        // System.out.println("r = " + r);
        r = rf.random( 9 );
        // System.out.println("r = " + r);

        RingElem<BigRational> re = new BigRational( 3 );
        // System.out.println("re = " + re);


        // polynomials over rational numbers
        GenSolvablePolynomialRing<BigRational> pf = new GenSolvablePolynomialRing<BigRational>(rf,2);
        // System.out.println("pf = " + pf);

        GenSolvablePolynomial<BigRational> p = pf.getONE();
        // System.out.println("p = " + p);
        p = pf.random( 9 );
        // System.out.println("p = " + p);
        p = pf.getZERO();
        // System.out.println("p = " + p);

        RingElem< GenPolynomial<BigRational> > pe = new GenSolvablePolynomial<BigRational>( pf );
        //System.out.println("pe = " + pe);
        //System.out.println("p.equals(pe) = " + p.equals(pe) );
        //System.out.println("p.equals(p) = " + p.equals(p) );
        assertTrue("p.equals(pe) = ", p.equals(pe) );
        assertTrue("p.equals(p) = ", p.equals(p) );

        pe = pe.sum( p ); // why not p = p.add(pe) ?
        //System.out.println("pe = " + pe);
        p = pf.random( 9 );
        p = (GenSolvablePolynomial<BigRational>)p.subtract( p ); 
        //System.out.println("p = " + p);
        //System.out.println("p.isZERO() = " + p.isZERO());
        assertTrue("p.isZERO() = ", p.isZERO());


        // polynomials over (polynomials over rational numbers)
        GenSolvablePolynomialRing< GenPolynomial<BigRational> > ppf = new GenSolvablePolynomialRing< GenPolynomial<BigRational> >(pf,3);
        // System.out.println("ppf = " + ppf);

        GenSolvablePolynomial< GenPolynomial<BigRational> > pp = ppf.getONE();
        // System.out.println("pp = " + pp);
        pp = ppf.random( 2 );
        // System.out.println("pp = " + pp);
        pp = ppf.getZERO();
        // System.out.println("pp = " + pp);

        RingElem< GenPolynomial< GenPolynomial<BigRational> > > ppe = new GenSolvablePolynomial< GenPolynomial<BigRational> >( ppf );
        // System.out.println("ppe = " + ppe);
        // System.out.println("pp.equals(ppe) = " + pp.equals(ppe) );
        // System.out.println("pp.equals(pp) = " + pp.equals(pp) );
        assertTrue("pp.equals(ppe) = ", pp.equals(ppe) );
        assertTrue("pp.equals(pp) = ", pp.equals(pp) );

        ppe = ppe.sum( pp ); // why not pp = pp.add(ppe) ?
        //System.out.println("ppe = " + ppe);
        pp = ppf.random( 2 );
        pp = (GenSolvablePolynomial< GenPolynomial<BigRational>>)pp.subtract( pp ); 
        //System.out.println("pp = " + pp);
        //System.out.println("pp.isZERO() = " + pp.isZERO());
        assertTrue("pp.isZERO() = ", pp.isZERO());


        // polynomials over (polynomials over (polynomials over rational numbers))
        GenSolvablePolynomialRing< GenPolynomial< GenPolynomial<BigRational> > > pppf = new GenSolvablePolynomialRing< GenPolynomial< GenPolynomial<BigRational> > >(ppf,4);
        // System.out.println("pppf = " + pppf);

        GenSolvablePolynomial< GenPolynomial< GenPolynomial<BigRational> > > ppp = pppf.getONE();
        //System.out.println("ppp = " + ppp);
        ppp = pppf.random( 2 );
        // System.out.println("ppp = " + ppp);
        ppp = pppf.getZERO();
        // System.out.println("ppp = " + ppp);

        RingElem< GenPolynomial< GenPolynomial< GenPolynomial<BigRational> > > > pppe = new GenSolvablePolynomial< GenPolynomial< GenPolynomial<BigRational> > >( pppf );
        // System.out.println("pppe = " + pppe);
        // System.out.println("ppp.equals(pppe) = " + ppp.equals(pppe) );
        // System.out.println("ppp.equals(ppp) = " + ppp.equals(ppp) );
        assertTrue("ppp.equals(pppe) = ", ppp.equals(pppe) );
        assertTrue("ppp.equals(ppp) = ", ppp.equals(ppp) );

        pppe = pppe.sum( ppp ); // why not ppp = ppp.add(pppe) ?
        // System.out.println("pppe = " + pppe);
        ppp = pppf.random( 2 );
        ppp = (GenSolvablePolynomial<GenPolynomial<GenPolynomial<BigRational>>>)ppp.subtract( ppp ); 
        // System.out.println("ppp = " + ppp);
        // System.out.println("ppp.isZERO() = " + ppp.isZERO());
        assertTrue("ppp.isZERO() = ", ppp.isZERO());

        // some tests
        //GenSolvablePolynomial<BigRational> pfx = new GenSolvablePolynomial<BigRational>();
        //System.out.println("pfx = " + pfx);
    }


/**
 * Test extension and contraction.
 * 
 */
 public void testExtendContract() {
     // rational numbers
     BigRational cf = new BigRational( 99 );
     // System.out.println("cf = " + cf);

     // polynomials over rational numbers
     GenSolvablePolynomialRing<BigRational> pf = new GenSolvablePolynomialRing<BigRational>(cf,rl);
     // System.out.println("pf = " + pf);

     GenSolvablePolynomial<BigRational> a = pf.random(kl,ll,el,q);
     //System.out.println("a = " + a);

     int k = rl;
     GenSolvablePolynomialRing<BigRational> pfe = pf.extend(k);
     GenSolvablePolynomialRing<BigRational> pfec = pfe.contract(k);
     assertEquals("pf == pfec",pf,pfec);

     GenSolvablePolynomial<BigRational> ae 
         = (GenSolvablePolynomial<BigRational>)a.extend(pfe,0,0);

     Map<ExpVector,GenPolynomial<BigRational>> m = ae.contract(pfec);
     List<GenPolynomial<BigRational>> ml = new ArrayList<GenPolynomial<BigRational>>( m.values() );
     GenSolvablePolynomial<BigRational> aec = (GenSolvablePolynomial<BigRational>)ml.get(0);
     assertEquals("a == aec",a,aec);
     //System.out.println("ae = " + ae);
     //System.out.println("aec = " + aec);
 }


/**
 * Test extension and contraction for Weyl relations.
 * 
 */
 public void testExtendContractWeyl() {
     // rational numbers
     BigRational cf = new BigRational( 99 );
     // System.out.println("cf = " + cf);

     // polynomials over rational numbers
     GenSolvablePolynomialRing<BigRational> pf = new GenSolvablePolynomialRing<BigRational>(cf,rl);
     WeylRelations<BigRational> wl = new WeylRelations<BigRational>(pf);
     wl.generate();
     // System.out.println("pf = " + pf);

     GenSolvablePolynomial<BigRational> a = pf.random(kl,ll,el,q);
     //System.out.println("a = " + a);

     int k = rl;
     GenSolvablePolynomialRing<BigRational> pfe = pf.extend(k);
     GenSolvablePolynomialRing<BigRational> pfec = pfe.contract(k);
     assertEquals("pf == pfec",pf,pfec);

     GenSolvablePolynomial<BigRational> ae 
         = (GenSolvablePolynomial<BigRational>)a.extend(pfe,0,0);

     Map<ExpVector,GenPolynomial<BigRational>> m = ae.contract(pfec);
     List<GenPolynomial<BigRational>> ml = new ArrayList<GenPolynomial<BigRational>>( m.values() );
     GenSolvablePolynomial<BigRational> aec = (GenSolvablePolynomial<BigRational>)ml.get(0);
     assertEquals("a == aec",a,aec);
     //System.out.println("ae = " + ae);
     //System.out.println("aec = " + aec);
 }


/**
 * Test reversion.
 * 
 */
 public void testReverse() {
     // rational numbers
     BigRational cf = new BigRational( 99 );
     // System.out.println("cf = " + cf);

     // polynomials over rational numbers
     GenSolvablePolynomialRing<BigRational> pf = new GenSolvablePolynomialRing<BigRational>(cf,rl);
     //System.out.println("pf = " + pf);

     GenSolvablePolynomial<BigRational> a = pf.random(kl,ll,el,q);
     //System.out.println("a = " + a);

     GenSolvablePolynomialRing<BigRational> pfr = pf.reverse();
     GenSolvablePolynomialRing<BigRational> pfrr = pfr.reverse();
     assertEquals("pf == pfrr",pf,pfrr);
     //System.out.println("pfr = " + pfr);

     GenSolvablePolynomial<BigRational> ar 
        = (GenSolvablePolynomial<BigRational>)a.reverse(pfr);
     GenSolvablePolynomial<BigRational> arr 
        = (GenSolvablePolynomial<BigRational>)ar.reverse(pfrr);
     assertEquals("a == arr",a,arr);
     //System.out.println("ar = " + ar);
     //System.out.println("arr = " + arr);
 }


/**
 * Test reversion for Weyl relations.
 * 
 */
 public void testReverseWeyl() {
     // rational numbers
     BigRational cf = new BigRational( 99 );
     // System.out.println("cf = " + cf);

     // polynomials over rational numbers
     GenSolvablePolynomialRing<BigRational> pf = new GenSolvablePolynomialRing<BigRational>(cf,rl);
     WeylRelations<BigRational> wl = new WeylRelations<BigRational>(pf);
     wl.generate();
     //System.out.println("pf = " + pf);

     GenSolvablePolynomial<BigRational> a = pf.random(kl,ll,el,q);
     //System.out.println("a = " + a);

     GenSolvablePolynomialRing<BigRational> pfr = pf.reverse();
     GenSolvablePolynomialRing<BigRational> pfrr = pfr.reverse();
     assertEquals("pf == pfrr",pf,pfrr);
     //System.out.println("pfr = " + pfr);

     GenSolvablePolynomial<BigRational> ar 
        = (GenSolvablePolynomial<BigRational>)a.reverse(pfr);
     GenSolvablePolynomial<BigRational> arr 
        = (GenSolvablePolynomial<BigRational>)ar.reverse(pfrr);
     assertEquals("a == arr",a,arr);
     //System.out.println("ar = " + ar);
     //System.out.println("arr = " + arr);
 }

}
