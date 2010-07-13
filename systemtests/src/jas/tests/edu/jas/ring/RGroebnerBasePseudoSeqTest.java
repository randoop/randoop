/*
 * $Id: RGroebnerBasePseudoSeqTest.java 1971 2008-08-02 10:41:10Z kredel $
 */

package edu.jas.ring;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

import edu.jas.application.PolyUtilApp;
import edu.jas.arith.BigInteger;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.GenPolynomialTokenizer;
import edu.jas.poly.PolynomialList;
import edu.jas.structure.Product;
import edu.jas.structure.ProductRing;
import edu.jas.structure.RingFactory;


/**
 * R-Groebner base sequential tests with JUnit.
 * @author Heinz Kredel.
 */

public class RGroebnerBasePseudoSeqTest extends TestCase {


    // private static final Logger logger =
    // Logger.getLogger(RGroebnerBasePseudoSeqTest.class);

    /**
     * main
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
        // ComputerThreads.terminate();
    }


    /**
     * Constructs a <CODE>RGroebnerBasePseudoSeqTest</CODE> object.
     * @param name String.
     */
    public RGroebnerBasePseudoSeqTest(String name) {
        super(name);
    }


    /**
     * suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(RGroebnerBasePseudoSeqTest.class);
        return suite;
    }


    ProductRing<BigInteger> pfac;


    GenPolynomialRing<Product<BigInteger>> fac;


    List<GenPolynomial<Product<BigInteger>>> L;


    PolynomialList<Product<BigInteger>> F;


    List<GenPolynomial<Product<BigInteger>>> G;


    GroebnerBase<Product<BigInteger>> bb;


    GenPolynomial<Product<BigInteger>> a;


    GenPolynomial<Product<BigInteger>> b;


    GenPolynomial<Product<BigInteger>> c;


    GenPolynomial<Product<BigInteger>> d;


    GenPolynomial<Product<BigInteger>> e;


    int pl = 3;


    int rl = 3; // 4; //3;


    int kl = 7; // 10;


    int ll = 7;


    int el = 3;


    float q = 0.3f; // 0.4f


    @Override
    protected void setUp() {
        BigInteger coeff = new BigInteger(9);
        pfac = new ProductRing<BigInteger>(coeff, pl);
        fac = new GenPolynomialRing<Product<BigInteger>>(pfac, rl);
        a = b = c = d = e = null;
        bb = new RGroebnerBasePseudoSeq<Product<BigInteger>>(pfac);
    }


    @Override
    protected void tearDown() {
        a = b = c = d = e = null;
        fac = null;
        bb = null;
    }


    /**
     * Test sequential GBase.
     * 
     */
    public void testSequentialGBase() {

        L = new ArrayList<GenPolynomial<Product<BigInteger>>>();

        a = fac.random(kl, ll, el, q);
        b = fac.random(kl, ll, el, q);
        c = fac.random(kl, ll, el, q);
        d = fac.random(kl, ll, el, q);
        e = d; // fac.random(kl, ll, el, q );

        if (a.isZERO() || b.isZERO() /* || c.isZERO() || d.isZERO() */) {
            return;
        }

        assertTrue("not isZERO( a )", !a.isZERO());
        L.add(a);
        // System.out.println("L = " + L );

        L = bb.GB(L);
        // System.out.println("L = " + L );
        assertTrue("isGB( { a } )", bb.isGB(L));

        assertTrue("not isZERO( b )", !b.isZERO());
        L.add(b);
        // System.out.println("L = " + L );

        L = bb.GB(L);
        // System.out.println("L = " + L );
        assertTrue("isGB( { a, b } )", bb.isGB(L));

        // assertTrue("not isZERO( c )", !c.isZERO() );
        L.add(c);
        // System.out.println("L = " + L );

        L = bb.GB(L);
        // System.out.println("L = " + L );
        assertTrue("isGB( { a, b, c } )", bb.isGB(L));

        // assertTrue("not isZERO( d )", !d.isZERO() );
        L.add(d);
        // System.out.println("L = " + L );

        L = bb.GB(L);
        // System.out.println("L = " + L );
        assertTrue("isGB( { a, b, c, d } )", bb.isGB(L));

        // assertTrue("not isZERO( e )", !e.isZERO() );
        L.add(e);
        // System.out.println("L = " + L );

        L = bb.GB(L);
        // System.out.println("L = " + L );
        assertTrue("isGB( { a, b, c, d, e } )", bb.isGB(L));
    }


    /**
     * Test Trinks7 GBase.
     * 
     */
    @SuppressWarnings("unchecked")
    public void testTrinks7() {
        String exam = "Z(B,S,T,Z,P,W) L " + "( " + "( 45 P + 35 S - 165 B - 36 ), "
                + "( 35 P + 40 Z + 25 T - 27 S ), "
                + "( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), "
                + "( - 9 W + 15 T P + 20 S Z ), " + "( P W + 2 T Z - 11 B**3 ), "
                + "( 99 W - 11 B S + 3 B**2 ), " + "( 10000 B**2 + 6600 B + 2673 ) "
                + ") ";
        Reader source = new StringReader(exam);
        GenPolynomialTokenizer parser = new GenPolynomialTokenizer(source);

        PolynomialList<BigInteger> F = null;

        try {
            F = (PolynomialList<BigInteger>) parser.nextPolynomialSet();
        } catch (ClassCastException e) {
            fail("" + e);
        } catch (IOException e) {
            fail("" + e);
        }
        // System.out.println("F = " + F);
        PolynomialList<Product<BigInteger>> trinks;

        List<RingFactory<BigInteger>> colist;
        colist = new ArrayList<RingFactory<BigInteger>>();

        // colist.add( new BigInteger() );
        // colist.add( new BigInteger() );
        // colist.add( new BigInteger() );
        // colist.add( new BigInteger() );
        colist.add(new BigInteger());
        colist.add(new BigInteger());
        // colist.add( new BigInteger() );
        // System.out.println("colist = " + colist);

        ProductRing<BigInteger> pfac;
        pfac = new ProductRing<BigInteger>(colist);
        // System.out.println("pfac = " + pfac);

        GenPolynomialRing<Product<BigInteger>> fac;
        fac = new GenPolynomialRing<Product<BigInteger>>(pfac, F.ring);
        // System.out.println("fac = " + fac);

        List<GenPolynomial<Product<BigInteger>>> Fp = null;
        Fp = PolyUtilApp.<BigInteger> toProductGen(fac, F.list);


        List<GenPolynomial<Product<BigInteger>>> Fpp;
        Fpp = new ArrayList<GenPolynomial<Product<BigInteger>>>();
        for (GenPolynomial<Product<BigInteger>> a : Fp) {
            Fpp.add(a.multiply(pfac.getAtomic(0)));
            Fpp.add(a.multiply(pfac.getAtomic(1)));
            // Fpp.add( a );
        }
        Fp = Fpp;

        trinks = new PolynomialList<Product<BigInteger>>(fac, Fp);
        // System.out.println("Fp = " + trinks);
        GroebnerBase<Product<BigInteger>> bbri = new RGroebnerBasePseudoSeq<Product<BigInteger>>(
                pfac);

        List<GenPolynomial<Product<BigInteger>>> G;
        G = bbri.GB(Fp);
        // System.out.println("gb = " + G );

        // assertEquals("#GB(Trinks7) == 6", 6, G.size() );
        // System.out.println("Fp = " + trinks);
        trinks = new PolynomialList<Product<BigInteger>>(fac, G);
        // System.out.println("G = " + trinks);

        assertTrue("isGB( GB(Trinks7) )", bbri.isGB(G));
    }


}
