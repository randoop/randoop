/*
 * $Id: GroebnerBaseSeqPairDistTest.java 1257 2007-07-29 10:17:38Z kredel $
 */

package edu.jas.ring;

//import edu.jas.poly.GroebnerBase;

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

import edu.jas.structure.RingElem;

import edu.jas.arith.BigRational;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.GenPolynomialTokenizer;
import edu.jas.poly.PolynomialList;

import edu.jas.ring.GroebnerBase;
import edu.jas.ring.GroebnerBaseSeq;

/**
 * Groebner base distributed, sequential pair list, tests with JUnit.
 * @author Heinz Kredel
 */

public class GroebnerBaseSeqPairDistTest extends TestCase {

    //private static final Logger logger = Logger.getLogger(GroebnerBaseSeqPairDistTest.class);

/**
 * main
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>GroebnerBaseSeqPairDistTest</CODE> object.
 * @param name String.
 */
public GroebnerBaseSeqPairDistTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(GroebnerBaseSeqPairDistTest.class);
     return suite;
   }

   int port = 4711;
   String host = "localhost";

   GenPolynomialRing<BigRational> fac;

   List<GenPolynomial<BigRational>> L;
   PolynomialList<BigRational> F;
   List<GenPolynomial<BigRational>> G;

   GroebnerBase<BigRational> bbseq;
   GroebnerBaseSeqPairDistributed<BigRational> bbdist;

   GenPolynomial<BigRational> a;
   GenPolynomial<BigRational> b;
   GenPolynomial<BigRational> c;
   GenPolynomial<BigRational> d;
   GenPolynomial<BigRational> e;

   int rl = 3; //4; //3; 
   int kl = 3;
   int ll = 7;
   int el = 3;
   float q = 0.2f; //0.4f

   int threads = 2;

   protected void setUp() {
       BigRational coeff = new BigRational(9);
       fac = new GenPolynomialRing<BigRational>(coeff,rl);
       a = b = c = d = e = null;
       bbseq = new GroebnerBaseSeq<BigRational>();
       bbdist = new GroebnerBaseSeqPairDistributed<BigRational>(threads,port);
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       fac = null;
       bbseq = null;
       bbdist.terminate();
       bbdist = null;
   }


/**
 * Helper method to start threads with distributed clients.
 * 
 */

  Thread[] startThreads() {
     Thread[] clients = new Thread[threads];
     for (int t = 0; t < threads; t++) {
         clients[t] = new Thread( new JunitSeqPairClient(host,port) );
         clients[t].start();
     }
     return clients;
 }

/**
 * Helper method to stop threads with distributed clients.
 * 
 */

    void stopThreads(Thread[] clients) {
        for (int t = 0; t < threads; t++) {
            try {
                clients[t].join();
            } catch (InterruptedException e) {
            }
        }
    }


/**
 * Test distributed GBase.
 * 
 */
 public void testSeqPairDistributedGBase() {

     Thread[] clients;

     L = new ArrayList<GenPolynomial<BigRational>>();

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );
     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );
     e = d; //fac.random(kl, ll, el, q );

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     assertTrue("not isZERO( a )", !a.isZERO() );
     L.add(a);

     clients = startThreads();
     L = bbdist.GB( L );
     stopThreads( clients );
     assertTrue("isGB( { a } )", bbseq.isGB(L) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     L.add(b);
     //System.out.println("L = " + L.size() );

     clients = startThreads();
     L = bbdist.GB( L );
     stopThreads( clients );
     assertTrue("isGB( { a, b } )", bbseq.isGB(L) );

     assertTrue("not isZERO( c )", !c.isZERO() );
     L.add(c);

     clients = startThreads();
     L = bbdist.GB( L );
     stopThreads( clients );
     assertTrue("isGB( { a, b, c } )", bbseq.isGB(L) );

     assertTrue("not isZERO( d )", !d.isZERO() );
     L.add(d);

     clients = startThreads();
     L = bbdist.GB( L );
     stopThreads( clients );
     assertTrue("isGB( { a, b, c, d } )", bbseq.isGB(L) );

     assertTrue("not isZERO( e )", !e.isZERO() );
     L.add(e);

     clients = startThreads();
     L = bbdist.GB( L );
     stopThreads( clients );
     assertTrue("isGB( { a, b, c, d, e } )", bbseq.isGB(L) );
 }


/**
 * Test compare sequential with distributed GBase.
 * 
 */
 public void testSequentialSeqPairDistributedGBase() {

     Thread[] clients;

     List<GenPolynomial<BigRational>> Gs, Gp = null;

     L = new ArrayList<GenPolynomial<BigRational>>();

     a = fac.random(kl, ll, el, q );
     b = fac.random(kl, ll, el, q );
     c = fac.random(kl, ll, el, q );
     d = fac.random(kl, ll, el, q );
     e = d; //fac.random(kl, ll, el, q );

     if ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() ) {
        return;
     }

     L.add(a);
     Gs = bbseq.GB( L );
     clients = startThreads();
     Gp = bbdist.GB( L );
     stopThreads( clients );

     assertTrue("Gs.containsAll(Gp)", Gs.containsAll(Gp) );
     assertTrue("Gp.containsAll(Gs)", Gp.containsAll(Gs) );

     L = Gs;
     L.add(b);
     Gs = bbseq.GB( L );
     clients = startThreads();
     Gp = bbdist.GB( L );
     stopThreads( clients );
     assertTrue("Gs.containsAll(Gp)", Gs.containsAll(Gp) );
     assertTrue("Gp.containsAll(Gs)", Gp.containsAll(Gs) );

     L = Gs;
     L.add(c);
     Gs = bbseq.GB( L );
     clients = startThreads();
     Gp = bbdist.GB( L );
     stopThreads( clients );

     assertTrue("Gs.containsAll(Gp)", Gs.containsAll(Gp) );
     assertTrue("Gp.containsAll(Gs)", Gp.containsAll(Gs) );

     L = Gs;
     L.add(d);
     Gs = bbseq.GB( L );
     clients = startThreads();
     Gp = bbdist.GB( L );
     stopThreads( clients );

     assertTrue("Gs.containsAll(Gp)", Gs.containsAll(Gp) );
     assertTrue("Gp.containsAll(Gs)", Gp.containsAll(Gs) );

     L = Gs;
     L.add(e);
     Gs = bbseq.GB( L );
     clients = startThreads();
     Gp = bbdist.GB( L );
     stopThreads( clients );

     assertTrue("Gs.containsAll(Gp)", Gs.containsAll(Gp) );
     assertTrue("Gp.containsAll(Gs)", Gp.containsAll(Gs) );
 }


/**
 * Test Trinks7 GBase.
 * 
 */
 public void testTrinks7GBase() {
     Thread[] clients;
     String exam = "(B,S,T,Z,P,W) L "
                 + "( "  
                 + "( 45 P + 35 S - 165 B - 36 ), " 
                 + "( 35 P + 40 Z + 25 T - 27 S ), "
                 + "( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), "
                 + "( - 9 W + 15 T P + 20 S Z ), "
                 + "( P W + 2 T Z - 11 B**3 ), "
                 + "( 99 W - 11 B S + 3 B**2 ), "
                 + "( B**2 + 33/50 B + 2673/10000 ) "
                 + ") ";
     Reader source = new StringReader( exam );
     GenPolynomialTokenizer parser
                  = new GenPolynomialTokenizer( source );
     try {
         F = parser.nextPolynomialSet();
     } catch(IOException e) {
         fail(""+e);
     }
     //System.out.println("F = " + F);

     clients = startThreads();
     G = bbdist.GB( F.list );
     stopThreads( clients );

     assertTrue("isGB( GB(Trinks7) )", bbseq.isGB(G) );
     assertEquals("#GB(Trinks7) == 6", 6, G.size() );
     PolynomialList<BigRational> trinks 
           = new PolynomialList<BigRational>(F.ring,G);
     //System.out.println("G = " + trinks);

 }

}


/**
 * Unit Test client to be executed by test threads.
 */

class JunitSeqPairClient<C extends RingElem<C>> implements Runnable {
    private final String host;
    private final int port;

    JunitSeqPairClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        GroebnerBaseSeqPairDistributed<C> bbd;
        bbd = new GroebnerBaseSeqPairDistributed<C>(1,null,port);
        try {
            bbd.clientPart(host);
        } catch (IOException ignored) {
        }
        bbd.terminate();
    }
}

