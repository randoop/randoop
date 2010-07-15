/*
 * $Id: ModGroebnerBaseTest.java 1887 2008-07-12 13:37:07Z kredel $
 */

package edu.jas.module;

import java.util.List;
//import java.util.Iterator;
import java.util.ArrayList;
//import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;


//import edu.jas.structure.RingElem;

import edu.jas.arith.BigRational;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolynomialList;
import edu.jas.poly.TermOrder;

//import edu.jas.ring.Reduction;
//import edu.jas.ring.GroebnerBase;

import edu.jas.vector.ModuleList;


/**
 * ModGroebnerBase tests with JUnit.
 * @author Heinz Kredel.
 */

public class ModGroebnerBaseTest extends TestCase {

    //private static final Logger logger = Logger.getLogger(ModGroebnerBaseTest.class);

/**
 * main.
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>ModGroebnerBaseTest</CODE> object.
 * @param name String.
 */
   public ModGroebnerBaseTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(ModGroebnerBaseTest.class);
     return suite;
   }

   int port = 4711;
   String host = "localhost";

   GenPolynomialRing<BigRational> fac;

   PolynomialList<BigRational> F;
   List<GenPolynomial<BigRational>> G;

   GenPolynomial<BigRational> a;
   GenPolynomial<BigRational> b;
   GenPolynomial<BigRational> c;
   GenPolynomial<BigRational> d;
   GenPolynomial<BigRational> e;

   TermOrder tord;

   List<List<GenPolynomial<BigRational>>> L;
   List<GenPolynomial<BigRational>> V;
   ModuleList<BigRational> M;
   ModuleList<BigRational> N;

   ModGroebnerBase<BigRational> mbb;

   int rl = 3; //4; //3; 
   int kl = 8;
   int ll = 5;
   int el = 2;
   float q = 0.2f; //0.4f

   protected void setUp() {
       BigRational coeff = new BigRational(9);
       tord = new TermOrder();
       fac = new GenPolynomialRing<BigRational>(coeff,rl,tord);
       mbb = new ModGroebnerBaseAbstract<BigRational>();
       a = b = c = d = e = null;

       do {
          a = fac.random(kl, ll, el, q );
          b = fac.random(kl, ll, el, q );
          c = fac.random(kl, ll, el, q );
          d = fac.random(kl, ll, el, q );
       } while ( a.isZERO() || b.isZERO() || c.isZERO() || d.isZERO() );
       e = d; //fac.random(kl, ll, el, q );
   }

   protected void tearDown() {
       mbb = null;
       a = b = c = d = e = null;
       fac = null;
       tord = null;
   }


/**
 * Test sequential GBase.
 * 
 */
 public void testSequentialModGB() {

     L = new ArrayList<List<GenPolynomial<BigRational>>>();

     assertTrue("not isZERO( a )", !a.isZERO() );
     V = new ArrayList<GenPolynomial<BigRational>>();
     V.add(a); V.add(fac.getZERO()); V.add(fac.getONE());
     L.add(V);
     M = new ModuleList<BigRational>(fac,L);
     assertTrue("isGB( { (a,0,1) } )", mbb.isGB(M) );

     N = mbb.GB( M );
     assertTrue("isGB( { (a,0,1) } )", mbb.isGB(N) );

     assertTrue("not isZERO( b )", !b.isZERO() );
     V = new ArrayList<GenPolynomial<BigRational>>();
     V.add(b); V.add(fac.getONE()); V.add(fac.getZERO());
     L.add(V);
     M = new ModuleList<BigRational>(fac,L);
     //System.out.println("L = " + L.size() );

     N = mbb.GB( M );
     assertTrue("isDIRPGB( { (a,0,1),(b,1,0) } )", mbb.isGB(N) );
     //System.out.println("N = " + N );

     assertTrue("not isZERO( c )", !c.isZERO() );
     V = new ArrayList<GenPolynomial<BigRational>>();
     V.add(c); V.add(fac.getZERO()); V.add(fac.getZERO());
     L.add(V);
     M = new ModuleList<BigRational>(fac,L);
     //System.out.println("L = " + L.size() );

     N = mbb.GB( M );
     assertTrue("isDIRPGB( { (a,),(b,),(c,) } )", mbb.isGB(N) );
     //System.out.println("N = " + N );

     assertTrue("not isZERO( d )", !d.isZERO() );
     V = new ArrayList<GenPolynomial<BigRational>>();
     V.add(d); V.add(fac.getZERO()); V.add(fac.getZERO());
     L.add(V);
     M = new ModuleList<BigRational>(fac,L);
     //System.out.println("L = " + L.size() );

     N = mbb.GB( M );
     assertTrue("isDIRPGB( { (a,b,c,d) } )", mbb.isGB(N) );
     //System.out.println("N = " + N );

 }

}
