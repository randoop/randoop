/*
 * $Id: SolvableModuleListTest.java 1891 2008-07-12 13:38:47Z kredel $
 */

package edu.jas.vector;

import java.util.List;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//import edu.jas.structure.RingElem;

import edu.jas.arith.BigRational;

import edu.jas.poly.PolynomialList;
import edu.jas.poly.TermOrder;
import edu.jas.poly.GenPolynomial;
//import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.GenSolvablePolynomial;
import edu.jas.poly.GenSolvablePolynomialRing;
import edu.jas.poly.WeylRelations;
import edu.jas.poly.RelationTable;


/**
 * SolvableModuleList tests using JUnit.
 * @author Heinz Kredel.
 */

public class SolvableModuleListTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>SolvableModuleListTest</CODE> object.
 * @param name String.
 */
   public SolvableModuleListTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(SolvableModuleListTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   ModuleList<BigRational> m;
   PolynomialList<BigRational> p;

   GenSolvablePolynomial<BigRational> a;
   GenSolvablePolynomial<BigRational> b;
   GenSolvablePolynomial<BigRational> c;
   GenSolvablePolynomial<BigRational> d;
   GenSolvablePolynomial<BigRational> e;

   BigRational cfac;
   GenSolvablePolynomialRing<BigRational> pfac;

   int rl = 4; 
   int kl = 4;
   int ll = 4;
   int el = 5;
   float q = 0.5f;

   protected void setUp() {
       a = b = c = d = e = null;
       cfac = new BigRational(1);
       TermOrder tord = new TermOrder();
       pfac = new GenSolvablePolynomialRing<BigRational>(cfac,rl,tord);
       m = null;
       p = null;
   }

   protected void tearDown() {
       a = b = c = d = e = null;
       m = null;
       p = null;
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstructor() {
     p = new PolynomialList<BigRational>(pfac,(List<GenPolynomial<BigRational>>)null);
     assertTrue("p = 0", p.list == null);

     m = new ModuleList<BigRational>(pfac,(List<List<GenPolynomial<BigRational>>>)null);
     assertTrue("m = 0", m.list == null);
 }


/**
 * Test polynomial list.
 * 
 */
 public void testPolynomialList() {
     List<GenPolynomial<BigRational>> l 
         = new ArrayList<GenPolynomial<BigRational>>();
     for (int i = 0; i < 7; i++) {
         a = pfac.random(kl, ll+i, el, q );
         assertTrue("length( a"+i+" ) <> 0", a.length() >= 0);
         assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
         assertTrue(" not isONE( a"+i+" )", !a.isONE() );
         l.add( a );
     }
     p = new PolynomialList<BigRational>(pfac,l);
     //System.out.println("p = "+p);

     assertTrue("p == p", p.equals(p) );
     assertEquals("p.length", 7, p.list.size() );
 }


/**
 * Test module list.
 * 
 */
 public void testModuleList() {
     List<List<GenPolynomial<BigRational>>> l 
         = new ArrayList<List<GenPolynomial<BigRational>>>();
     for (int i = 0; i < 4; i++) {
         List<GenPolynomial<BigRational>> r 
             = new ArrayList<GenPolynomial<BigRational>>();
         for ( int j = 0; j < 3; j++ ) {
             a = pfac.random(kl, ll, el, q );
             assertTrue("length( a"+i+" ) <> 0", a.length() >= 0);
             assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
             assertTrue(" not isONE( a"+i+" )", !a.isONE() );
             r.add( a );
         }
         l.add( r );
     }
     m = new ModuleList<BigRational>(pfac,l);
     //System.out.println("m = "+m);
     assertTrue("m == m", m.equals(m) );
     assertEquals("m.length", 4, m.list.size() );
 }


/**
 * Test module and polynomial list.
 * 
 */
 public void testModulePolynomialList() {
     List<List<GenPolynomial<BigRational>>> l 
         = new ArrayList<List<GenPolynomial<BigRational>>>();
     for (int i = 0; i < 4; i++) {
         List<GenPolynomial<BigRational>> r 
             = new ArrayList<GenPolynomial<BigRational>>();
         for ( int j = 0; j < 3; j++ ) {
             a = pfac.random(kl, ll, el, q );
             assertTrue("length( a"+i+" ) <> 0", a.length() >= 0);
             assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
             assertTrue(" not isONE( a"+i+" )", !a.isONE() );
             r.add( a );
         }
         l.add( r );
     }
     m = new ModuleList<BigRational>(pfac,l);
     //System.out.println("m = "+m);
     assertTrue("m == m", m.equals(m) );
     assertEquals("m.length", 4, m.list.size() );

     p = m.getPolynomialList();
     //System.out.println("p = "+p);
     assertTrue("p == p", p.equals(p) );
     assertEquals("p.length", 4, p.list.size() );
 }


/**
 * Test module and polynomial and module list.
 * 
 */
 public void testModulePolynomialModuleList() {
     List<List<GenPolynomial<BigRational>>> l 
         = new ArrayList<List<GenPolynomial<BigRational>>>();
     for (int i = 0; i < 4; i++) {
         List<GenPolynomial<BigRational>> r 
             = new ArrayList<GenPolynomial<BigRational>>();
         for ( int j = 0; j < 3; j++ ) {
             a = pfac.random(kl, ll, el, q );
             assertTrue("length( a"+i+" ) <> 0", a.length() >= 0);
             assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
             assertTrue(" not isONE( a"+i+" )", !a.isONE() );
             r.add( a );
         }
         l.add( r );
     }
     m = new ModuleList<BigRational>(pfac,l);
     //System.out.println("m = "+m);
     assertTrue("m == m", m.equals(m) );
     assertEquals("m.length", 4, m.list.size() );

     p = m.getPolynomialList();
     //System.out.println("p = "+p);
     assertTrue("p == p", p.equals(p) );
     assertEquals("p.length", 4, p.list.size() );

     ModuleList<BigRational> m2 = null;
     m2 = p.getModuleList( 3 );
     //System.out.println("m2 = "+m2);
     assertTrue("m2 == m2", m2.equals(m2) );
     assertEquals("m2.length", 4, m2.list.size() );

     assertTrue("m == m2", m.equals(m2) );
 }


/**
 * Test module and polynomial and module and polynomial list.
 * 
 */
 public void testModulePolynomialModuleListPolynomial() {
     List<List<GenPolynomial<BigRational>>> l 
         = new ArrayList<List<GenPolynomial<BigRational>>>();
     for (int i = 0; i < 4; i++) {
         List<GenPolynomial<BigRational>> r 
             = new ArrayList<GenPolynomial<BigRational>>();
         for ( int j = 0; j < 3; j++ ) {
             a = pfac.random(kl, ll, el, q );
             assertTrue("length( a"+i+" ) <> 0", a.length() >= 0);
             assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
             assertTrue(" not isONE( a"+i+" )", !a.isONE() );
             r.add( a );
         }
         l.add( r );
     }
     m = new ModuleList<BigRational>(pfac,l);
     //System.out.println("m = "+m);
     assertTrue("m == m", m.equals(m) );
     assertEquals("m.length", 4, m.list.size() );

     p = m.getPolynomialList();
     //System.out.println("p = "+p);
     assertTrue("p == p", p.equals(p) );
     assertEquals("p.length", 4, p.list.size() );

     ModuleList<BigRational> m2 = null;
     m2 = p.getModuleList( 3 );
     //System.out.println("m2 = "+m2);
     assertTrue("m2 == m2", m2.equals(m2) );
     assertEquals("m2.length", 4, m2.list.size() );

     assertTrue("m == m2", m.equals(m2) );

     PolynomialList<BigRational> p2 = null;
     p2 = m2.getPolynomialList();
     //System.out.println("p2 = "+p2);
     assertTrue("p2 == p2", p2.equals(p2) );
     assertEquals("p2.length", 4, p2.list.size() );

     assertTrue("p == p2", p.equals(p2) );
 }


/**
 * Test module and polynomial and module and polynomial list
 * with WeylRelations.
 * 
 */
 public void testWeylModulePolynomialModuleListPolynomial() {
     int rloc = 4;
     pfac = new GenSolvablePolynomialRing<BigRational>(cfac,rloc);

     WeylRelations<BigRational> wl = new WeylRelations<BigRational>(pfac);
     wl.generate();
     RelationTable table1 = pfac.table;
     RelationTable table2 = null;
     RelationTable table3 = null;
     RelationTable table4 = null;
     RelationTable table5 = null;
     //System.out.println("table 1 = " + table1);
     //System.out.println("ring = " + ring);

     List<List<GenPolynomial<BigRational>>> l 
         = new ArrayList<List<GenPolynomial<BigRational>>>();
     for (int i = 0; i < 4; i++) {
         List<GenPolynomial<BigRational>> r 
             = new ArrayList<GenPolynomial<BigRational>>();
         for ( int j = 0; j < 3; j++ ) {
             a = pfac.random(kl, ll, el, q );
             assertTrue("length( a"+i+" ) <> 0", a.length() >= 0);
             assertTrue(" not isZERO( a"+i+" )", !a.isZERO() );
             assertTrue(" not isONE( a"+i+" )", !a.isONE() );
             r.add( a );
         }
         l.add( r );
     }
     m = new ModuleList<BigRational>(pfac,l);
     //System.out.println("m = "+m);
     assertTrue("m == m", m.equals(m) );
     assertEquals("m.length", 4, m.list.size() );

     table2 = ((GenSolvablePolynomialRing<BigRational>)m.ring).table;
     //System.out.println("table 2 = " + table2);
     assertEquals("table1 == table2", table1, table2 );


     p = m.getPolynomialList();
     //System.out.println("p = "+p);
     assertTrue("p == p", p.equals(p) );
     assertEquals("p.length", 4, p.list.size() );

     table3 = ((GenSolvablePolynomialRing<BigRational>)p.ring).table;
     //System.out.println("table 3 = " + table3);

     ModuleList<BigRational> m2 = null;
     m2 = p.getModuleList( 3 );
     //System.out.println("m2 = "+m2);
     assertTrue("m2 == m2", m2.equals(m2) );
     assertEquals("m2.length", 4, m2.list.size() );

     assertTrue("m == m2", m.equals(m2) );

     table4 = ((GenSolvablePolynomialRing<BigRational>)m2.ring).table;
     //System.out.println("table 4 = " + table4);
     assertEquals("table2 == table4", table2, table4 );
     assertEquals("table1 == table4", table1, table4 );


     PolynomialList<BigRational> p2 = null;
     p2 = m2.getPolynomialList();
     //System.out.println("p2 = "+p2);
     assertTrue("p2 == p2", p2.equals(p2) );
     assertEquals("p2.length", 4, p2.list.size() );

     assertTrue("p == p2", p.equals(p2) );
     table5 = ((GenSolvablePolynomialRing<BigRational>)p2.ring).table;
     //System.out.println("table 5= " + table5);
     assertEquals("table2 == table4", table3, table5 );
 }

}
