/*
 * $Id: RGroebnerBaseSeqTest.java 1971 2008-08-02 10:41:10Z kredel $
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
import edu.jas.arith.BigRational;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.GenPolynomialTokenizer;
import edu.jas.poly.PolynomialList;
import edu.jas.poly.TermOrder;
import edu.jas.structure.Product;
import edu.jas.structure.ProductRing;
import edu.jas.structure.RingFactory;


/**
 * R-Groebner base sequential tests with JUnit.
 * @author Heinz Kredel.
 */

public class RGroebnerBaseSeqTest extends TestCase {


    //private static final Logger logger = Logger.getLogger(RGroebnerBaseSeqTest.class);

    /**
     * main
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
    }


    /**
     * Constructs a <CODE>RGroebnerBaseSeqTest</CODE> object.
     * @param name String.
     */
    public RGroebnerBaseSeqTest(String name) {
        super(name);
    }


    /**
     * suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(RGroebnerBaseSeqTest.class);
        return suite;
    }


    ProductRing<BigRational> pfac;


    GenPolynomialRing<Product<BigRational>> fac;


    List<GenPolynomial<Product<BigRational>>> L;


    PolynomialList<Product<BigRational>> F;


    List<GenPolynomial<Product<BigRational>>> G;


    GroebnerBase<Product<BigRational>> bb;


    GenPolynomial<Product<BigRational>> a;


    GenPolynomial<Product<BigRational>> b;


    GenPolynomial<Product<BigRational>> c;


    GenPolynomial<Product<BigRational>> d;


    GenPolynomial<Product<BigRational>> e;


    int rl = 3; //4; //3; 


    int kl = 10;


    int ll = 7;


    int el = 3;


    float q = 0.2f; //0.4f


    @Override
    protected void setUp() {
        BigRational coeff = new BigRational(9);
        pfac = new ProductRing<BigRational>(coeff, 4);
        fac = new GenPolynomialRing<Product<BigRational>>(pfac, rl);
        a = b = c = d = e = null;
        bb = new RGroebnerBaseSeq<Product<BigRational>>();
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

        L = new ArrayList<GenPolynomial<Product<BigRational>>>();

        a = fac.random(kl, ll, el, q);
        b = fac.random(kl, ll, el, q);
        c = fac.random(kl, ll, el, q);
        d = fac.random(kl, ll, el, q);
        e = d; //fac.random(kl, ll, el, q );

        if (a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO()) {
            return;
        }

        assertTrue("not isZERO( a )", !a.isZERO());
        L.add(a);

        L = bb.GB(L);
        assertTrue("isGB( { a } )", bb.isGB(L));
        //System.out.println("L = " + L );

        assertTrue("not isZERO( b )", !b.isZERO());
        L.add(b);
        //System.out.println("L = " + L.size() );

        L = bb.GB(L);
        assertTrue("isGB( { a, b } )", bb.isGB(L));
        //System.out.println("L = " + L );

        assertTrue("not isZERO( c )", !c.isZERO());
        L.add(c);

        L = bb.GB(L);
        assertTrue("isGB( { a, b, c } )", bb.isGB(L));
        //System.out.println("L = " + L );

        assertTrue("not isZERO( d )", !d.isZERO());
        L.add(d);

        L = bb.GB(L);
        assertTrue("isGB( { a, b, c, d } )", bb.isGB(L));
        //System.out.println("L = " + L );

        assertTrue("not isZERO( e )", !e.isZERO());
        L.add(e);

        L = bb.GB(L);
        assertTrue("isGB( { a, b, c, d, e } )", bb.isGB(L));
        //System.out.println("L = " + L );
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
        //System.out.println("F = " + F);
        int rl = F.ring.nvar;
        TermOrder to = F.ring.tord;
        String[] vars = F.ring.getVars();
        PolynomialList<Product<ModInteger>> trinks;

        List<RingFactory<ModInteger>> colist;
        colist = new ArrayList<RingFactory<ModInteger>>();

        //colist.add( new ModIntegerRing(2) );
        //colist.add( new ModIntegerRing(3) );
        //colist.add( new ModIntegerRing(5) );
        //colist.add( new ModIntegerRing(30) ); // now ok, was not possible
        colist.add(new ModIntegerRing(19));
        colist.add(new ModIntegerRing(23));
        //colist.add( new ModIntegerRing((2<<30)-19) );
        //System.out.println("colist = " + colist);

        ProductRing<ModInteger> pfac;
        pfac = new ProductRing<ModInteger>(colist);
        //System.out.println("pfac   = " + pfac);

        GenPolynomialRing<Product<ModInteger>> fac;
        fac = new GenPolynomialRing<Product<ModInteger>>(pfac, rl, to, vars);
        //System.out.println("fac    = " + fac);

        List<GenPolynomial<Product<ModInteger>>> Fp;
        Fp = PolyUtilApp.toProduct(fac, F.list);

        List<GenPolynomial<Product<ModInteger>>> Fpp;
        Fpp = new ArrayList<GenPolynomial<Product<ModInteger>>>();

        for (GenPolynomial<Product<ModInteger>> a : Fp) {
            Fpp.add(a.multiply(pfac.getAtomic(0)));
            Fpp.add(a.multiply(pfac.getAtomic(1)));
            //Fpp.add( a );
        }
        Fp = Fpp;

        trinks = new PolynomialList<Product<ModInteger>>(fac, Fp);
        //System.out.println("Fp     = " + trinks);

        GroebnerBase<Product<ModInteger>> bbr = new RGroebnerBaseSeq<Product<ModInteger>>();

        List<GenPolynomial<Product<ModInteger>>> G;
        G = bbr.GB(Fp);
        //System.out.println("gb = " + G );

        //assertEquals("#GB(Trinks7) == 6", 6, G.size() );
        //System.out.println("Fp = " + trinks);
        trinks = new PolynomialList<Product<ModInteger>>(fac, G);
        //System.out.println("G  = " + trinks);

        assertTrue("isGB( GB(Trinks7) )", bbr.isGB(G));
    }


}
