/*
 * $Id: CGBSeqTest.java 2033 2008-08-10 11:37:41Z kredel $
 */

package edu.jas.application;


import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

import edu.jas.kern.ComputerThreads;

import edu.jas.arith.BigRational;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.GenPolynomialTokenizer;
import edu.jas.poly.PolynomialList;


/**
 * Comprehenssive Groebner base sequential tests with JUnit.
 * @author Heinz Kredel.
 */

public class CGBSeqTest extends TestCase {

    //private static final Logger logger = Logger.getLogger(CGBSeqTest.class);

    /**
     * main
     */
    public static void main (String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run( suite() );
        ComputerThreads.terminate();
    }

    /**
     * Constructs a <CODE>CGBSeqTest</CODE> object.
     * @param name String.
     */
    public CGBSeqTest(String name) {
        super(name);
    }

    /**
     * suite.
     */ 
    public static Test suite() {
        TestSuite suite= new TestSuite(CGBSeqTest.class);
        return suite;
    }

    GenPolynomialRing<BigRational> cfac;
    GenPolynomialRing<GenPolynomial<BigRational>> fac;

    List<GenPolynomial<GenPolynomial<BigRational>>> L;

    ComprehensiveGroebnerBaseSeq<BigRational> bb;

    GenPolynomial<GenPolynomial<BigRational>> a;
    GenPolynomial<GenPolynomial<BigRational>> b;
    GenPolynomial<GenPolynomial<BigRational>> c;
    GenPolynomial<GenPolynomial<BigRational>> d;
    GenPolynomial<GenPolynomial<BigRational>> e;

    int rl = 2; //4; //3; 
    int kl = 2;
    int ll = 3;
    int el = 3;
    float q = 0.2f; //0.4f

    protected void setUp() {
        BigRational coeff = new BigRational(kl);
        String[] cv = { "a" }; //, "b" }; 
        cfac = new GenPolynomialRing<BigRational>(coeff,1,cv);
        String[] v = { "x" }; //, "y" }; 
        fac = new GenPolynomialRing<GenPolynomial<BigRational>>(cfac,1,v);
        a = b = c = d = e = null;
        bb = new ComprehensiveGroebnerBaseSeq<BigRational>(coeff);
    }

    protected void tearDown() {
        a = b = c = d = e = null;
        fac = null;
        cfac = null;
        bb = null;
    }


    /*
     * Dummy test method for jUnit.
     * 
    public void testDummy() {
    }
     */


    /**
     * Test sequential CGB.
     * 
     */
    public void testSequentialCGB() {

        L = new ArrayList<GenPolynomial<GenPolynomial<BigRational>>>();

        a = fac.random(kl, ll, el, q );
        b = fac.random(kl, ll, el, q );
        c = a; //fac.random(kl, ll, el, q );
        d = c; //fac.random(kl, ll, el, q );
        e = d; //fac.random(kl, ll, el, q );

        if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
            return;
        }

        assertTrue("not isZERO( a )", !a.isZERO() );
        L.add(a);

        L = bb.GB( L );
        assertTrue("isGB( { a } )", bb.isGB(L) );

        assertTrue("not isZERO( b )", !b.isZERO() );
        L.add(b);
        //System.out.println("L = " + L.size() );

        L = bb.GB( L );
        assertTrue("isGB( { a, b } )", bb.isGB(L) );

        assertTrue("not isZERO( c )", !c.isZERO() );
        L.add(c);

        L = bb.GB( L );
        assertTrue("isGB( { a, b, c } )", bb.isGB(L) );

        if ( true ) {
            return;
        }

        assertTrue("not isZERO( d )", !d.isZERO() );
        L.add(d);

        L = bb.GB( L );
        assertTrue("isGB( { a, b, c, d } )", bb.isGB(L) );

        assertTrue("not isZERO( e )", !e.isZERO() );
        L.add(e);

        L = bb.GB( L );
        assertTrue("isGB( { a, b, c, d, e } )", bb.isGB(L) );
    }


    /**
     * Test Trinks CGB.
     * 
     */
    @SuppressWarnings("unchecked") 
    public void testTrinks7GBase() {
        PolynomialList<GenPolynomial<BigRational>> F = null;
        List<GenPolynomial<GenPolynomial<BigRational>>> G = null;
        List<ColoredSystem<BigRational>> Gs = null;
        String exam = "IntFunc(b) (S,T,Z,P,W) L "
            + "( "  
            + "( 45 P + 35 S - { 165 b + 36 } ), " 
            + "( 35 P + 40 Z + 25 T - 27 S ), "
            + "( 15 W + 25 S P + 30 Z - 18 T - { 165 b**2 } ), "
            + "( - 9 W + 15 T P + 20 S Z ), "
            + "( P W + 2 T Z - { 11 b**3 } ), "
            + "( 99 W - { 11 b } S + { 3 b**2 } ), "
            + "( { b**2 + 33/50 b + 2673/10000 } ) "
            + ") ";
        Reader source = new StringReader( exam );
        GenPolynomialTokenizer parser
            = new GenPolynomialTokenizer( source );
        try {
            F = (PolynomialList<GenPolynomial<BigRational>>) parser.nextPolynomialSet();
        } catch(ClassCastException e) {
            fail(""+e);
        } catch(IOException e) {
            fail(""+e);
        }
        //System.out.println("F = " + F);

        G = bb.GB(F.list);
        assertTrue("isGB( GB(Trinks7) )", bb.isGB(G) );

        //PolynomialList<GenPolynomial<BigRational>> trinks 
        //    = new PolynomialList<GenPolynomial<BigRational>>(F.ring,G);
        //System.out.println("G = " + trinks);
        //System.out.println("G = " + G);
    }

}
