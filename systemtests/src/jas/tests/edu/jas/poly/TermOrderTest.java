/*
 * $Id: TermOrderTest.java 1888 2008-07-12 13:37:34Z kredel $
 */

package edu.jas.poly;

//import edu.jas.poly.TermOrder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

/**
 * TermOrder tests with JUnit.
 * Tests also ExpVector comparisons.
 * @author Heinz Kredel
 */

public class TermOrderTest extends TestCase {

/**
 * main.
 */
   public static void main (String[] args) {
          BasicConfigurator.configure();
          junit.textui.TestRunner.run( suite() );
   }

/**
 * Constructs a <CODE>TermOrderTest</CODE> object.
 * @param name String.
 */
   public TermOrderTest(String name) {
          super(name);
   }

/**
 * suite.
 */ 
 public static Test suite() {
     TestSuite suite= new TestSuite(TermOrderTest.class);
     return suite;
   }

   //private final static int bitlen = 100;

   ExpVector a;
   ExpVector b;
   ExpVector c;
   ExpVector d;

   TermOrder t;
   TermOrder s;


   protected void setUp() {
       a = b = c = d = null;
       t = s = null;
   }

   protected void tearDown() {
       a = b = c = d = null;
       t = s = null;
   }


/**
 * Test constructor and toString.
 * 
 */
 public void testConstructor() {

     s = new TermOrder();
     t = new TermOrder();
     assertEquals("t = s",t,s);

     String x = t.toString();
     String y = s.toString();

     assertEquals("x = y",x,y);

     t = new TermOrder(TermOrder.INVLEX);
     x = "INVLEX";
     boolean z = t.toString().startsWith(x);
     assertTrue("INVLEX(.)",z);

     s = new TermOrder(TermOrder.IGRLEX);
     t = new TermOrder(TermOrder.IGRLEX);
     assertEquals("t = s",t,s);
   }


/**
 * Test constructor, split TO.
 * 
 */
 public void testConstructorSplit() {

     int r = 10;
     int sp = 5;
     s = new TermOrder(r,sp);
     t = new TermOrder(r,sp);
     assertEquals("t = s",t,s);

     String x = t.toString();
     String y = s.toString();
     assertEquals("x = y",x,y);
     //System.out.println("s = " + s);

     s = new TermOrder(TermOrder.IGRLEX,TermOrder.INVLEX,r,sp);
     t = new TermOrder(TermOrder.IGRLEX,TermOrder.INVLEX,r,sp);
     assertEquals("t = s",t,s);
     //System.out.println("s = " + s);
   }


/**
 * Test constructor weight and toString.
 * 
 */
 public void testConstructorWeight() {
     long [][] w = new long [][] { new long[] { 1l, 1l, 1l, 1l, 1l } };

     s = new TermOrder(w);
     t = new TermOrder(w);
     assertEquals("t = s",t,s);

     String x = t.toString();
     String y = s.toString();

     assertEquals("x = y",x,y);
     //System.out.println("s = " + s);

     //int r = 5;
     //int sp = 3;
     w = new long [][] { new long[] { 5l, 4l, 3l, 2l, 1l } };

     //s = new TermOrder(w,sp);
     //t = new TermOrder(w,sp);
     //assertEquals("t = s",t,s);

     //x = t.toString();
     //y = s.toString();

     //assertEquals("x = y",x,y);
     //System.out.println("s = " + s);

     x = "W(";
     boolean z = t.toString().startsWith(x);
     assertTrue("W(.)",z);
   }


/**
 * Test compare weight.
 * 
 */
 public void testCompareWeight() {
     float q = (float) 0.9;

     a = ExpVector.EVRAND(5,10,q);
     b = ExpVector.EVRAND(5,10,q);

     c = a.sum(b);

     long [][] w = new long [][] { new long[] { 1l, 1l, 1l, 1l, 1l } };

     t = new TermOrder(w);

     int x = ExpVector.EVIWLC(w,c,a);
     int y = ExpVector.EVIWLC(w,c,b);

     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     x = ExpVector.EVIWLC(w,a,c);
     y = ExpVector.EVIWLC(w,b,c);

     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     x = ExpVector.EVIWLC(w,a,a);
     y = ExpVector.EVIWLC(w,b,b);

     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);
   }


/**
 * Test compare weight 2 rows.
 * 
 */
 public void testCompareWeight2() {
     float q = (float) 0.9;

     a = ExpVector.EVRAND(5,10,q);
     b = ExpVector.EVRAND(5,10,q);

     c = a.sum(b);

     long [][] w = new long [][] { new long[] { 1l, 1l, 1l, 1l, 1l },
                                   new long[] { 1l, 1l, 1l, 1l, 1l } };

     t = new TermOrder(w);

     int x = ExpVector.EVIWLC(w,c,a);
     int y = ExpVector.EVIWLC(w,c,b);

     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     x = ExpVector.EVIWLC(w,a,c);
     y = ExpVector.EVIWLC(w,b,c);

     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     x = ExpVector.EVIWLC(w,a,a);
     y = ExpVector.EVIWLC(w,b,b);

     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);
   }


/**
 * Test compare weight split.
 * 
 */
 public void testCompareWeightSplit() {
     float q = (float) 0.9;
     int r = 8;
     int sp = 4;

     a = ExpVector.EVRAND(r,10,q);
     b = ExpVector.EVRAND(r,10,q);
     c = a.sum(b);

     long [][] w = new long [][] { new long[] { 1l, 1l, 1l, 1l, 1l, 1l, 1l, 1l } };

     //t = new TermOrder(w,sp);

     int x;
     int y;
     x = ExpVector.EVIWLC(w,c,a);
     y = ExpVector.EVIWLC(w,c,b);
     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     x = ExpVector.EVIWLC(w,c,a,0,sp);
     y = ExpVector.EVIWLC(w,c,b,0,sp);
     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     x = ExpVector.EVIWLC(w,c,a,sp,r);
     y = ExpVector.EVIWLC(w,c,b,sp,r);
     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);


     x = ExpVector.EVIWLC(w,a,c);
     y = ExpVector.EVIWLC(w,b,c);
     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     x = ExpVector.EVIWLC(w,a,c,0,sp);
     y = ExpVector.EVIWLC(w,b,c,0,sp);
     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     x = ExpVector.EVIWLC(w,a,c,sp,r);
     y = ExpVector.EVIWLC(w,b,c,sp,r);
     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);


     x = ExpVector.EVIWLC(w,a,a);
     y = ExpVector.EVIWLC(w,b,b);
     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);

     x = ExpVector.EVIWLC(w,a,a,0,sp);
     y = ExpVector.EVIWLC(w,b,b,0,sp);
     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);

     x = ExpVector.EVIWLC(w,a,a,sp,r);
     y = ExpVector.EVIWLC(w,b,b,sp,r);
     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);


     long [][] w2 = new long [][] { new long[] { 1l, 1l, 1l, 1l, 0l, 0l, 0l, 0l },
                                    new long[] { 0l, 0l, 0l, 0l, 1l, 1l, 1l, 1l } };

     //t = new TermOrder(w2);

     x = ExpVector.EVIWLC(w2,c,a);
     y = ExpVector.EVIWLC(w2,c,b);
     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     x = ExpVector.EVIWLC(w2,c,a,0,sp);
     y = ExpVector.EVIWLC(w2,c,b,0,sp);
     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     x = ExpVector.EVIWLC(w2,c,a,sp,r);
     y = ExpVector.EVIWLC(w2,c,b,sp,r);
     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);


     x = ExpVector.EVIWLC(w2,a,c);
     y = ExpVector.EVIWLC(w2,b,c);
     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     x = ExpVector.EVIWLC(w2,a,c,0,sp);
     y = ExpVector.EVIWLC(w2,b,c,0,sp);
     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     x = ExpVector.EVIWLC(w2,a,c,sp,r);
     y = ExpVector.EVIWLC(w2,b,c,sp,r);
     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);


     x = ExpVector.EVIWLC(w2,a,a);
     y = ExpVector.EVIWLC(w2,b,b);
     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);

     x = ExpVector.EVIWLC(w2,a,a,0,sp);
     y = ExpVector.EVIWLC(w2,b,b,0,sp);
     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);

     x = ExpVector.EVIWLC(w2,a,a,sp,r);
     y = ExpVector.EVIWLC(w2,b,b,sp,r);
     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);
 }



/**
 * Test ascend comparators.
 * 
 */
 public void testAscendComparator() {
     float q = (float) 0.9;

     a = ExpVector.EVRAND(5,10,q);
     b = ExpVector.EVRAND(5,10,q);

     c = a.sum(b);

     t = new TermOrder();

     int x = t.getAscendComparator().compare(c,a);
     int y = t.getAscendComparator().compare(c,b);

     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     x = t.getAscendComparator().compare(a,c);
     y = t.getAscendComparator().compare(b,c);

     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     x = t.getAscendComparator().compare(a,a);
     y = t.getAscendComparator().compare(b,b);

     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);
   }


/**
 * Test ascend comparators split.
 * 
 */
 public void testAscendComparatorSplit() {
     float q = (float) 0.9;

     int r = 10;
     int sp = 5;

     a = ExpVector.EVRAND(r,10,q);
     b = ExpVector.EVRAND(r,10,q);
     c = a.sum(b);

     t = new TermOrder(r,sp);

     int x = t.getAscendComparator().compare(c,a);
     int y = t.getAscendComparator().compare(c,b);
     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     x = t.getAscendComparator().compare(a,c);
     y = t.getAscendComparator().compare(b,c);
     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     x = t.getAscendComparator().compare(a,a);
     y = t.getAscendComparator().compare(b,b);
     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);
   }


/**
 * Test ascend comparators weight and split.
 * 
 */
 public void testAscendComparatorWeightSplit() {
     float q = (float) 0.9;

     int r = 8;
     int sp = 5;
     long [][] w  = new long [][] { new long[] { 1l, 2l, 3l, 4l, 5l, 1l, 2l, 3l } };
     long [][] w2 = new long [][] { new long[] { 1l, 2l, 3l, 4l, 5l, 0l, 0l, 0l },
                                    new long[] { 0l, 0l, 0l, 0l, 0l, 1l, 2l, 3l }  };

     a = ExpVector.EVRAND(r,10,q);
     b = ExpVector.EVRAND(r,10,q);
     c = a.sum(b);

     // t = new TermOrder(w,sp);
     t = new TermOrder(w2);
     TermOrder t2 = new TermOrder(w2);
     // now t equals t2

     int x = t.getAscendComparator().compare(c,a);
     int y = t.getAscendComparator().compare(c,b);
     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     int x2 = t2.getAscendComparator().compare(c,a);
     int y2 = t2.getAscendComparator().compare(c,b);
     assertEquals("x2 = 1",1,x2);
     assertEquals("y2 = 1",1,y2);

     assertEquals("x = x2",x,x2);
     assertEquals("y = y2",y,y2);


     x = t.getAscendComparator().compare(a,c);
     y = t.getAscendComparator().compare(b,c);
     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     x2 = t2.getAscendComparator().compare(a,c);
     y2 = t2.getAscendComparator().compare(b,c);
     assertEquals("x2 = -1",-1,x2);
     assertEquals("y2 = -1",-1,y2);

     assertEquals("x = x2",x,x2);
     assertEquals("y = y2",y,y2);


     x = t.getAscendComparator().compare(a,a);
     y = t.getAscendComparator().compare(b,b);
     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);

     x2 = t2.getAscendComparator().compare(a,a);
     y2 = t2.getAscendComparator().compare(b,b);
     assertEquals("x2 = 0",0,x2);
     assertEquals("y2 = 0",0,y2);

     assertEquals("x = x2",x,x2);
     assertEquals("y = y2",y,y2);
   }


/**
 * Test descend comparators.
 * 
 */
 public void testDescendComparator() {
     float q = (float) 0.9;

     a = ExpVector.EVRAND(5,10,q);
     b = ExpVector.EVRAND(5,10,q);

     c = a.sum(b);

     t = new TermOrder();

     int x = t.getDescendComparator().compare(c,a);
     int y = t.getDescendComparator().compare(c,b);

     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     x = t.getDescendComparator().compare(a,c);
     y = t.getDescendComparator().compare(b,c);

     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     x = t.getDescendComparator().compare(a,a);
     y = t.getDescendComparator().compare(b,b);

     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);
   }


/**
 * Test descend comparators split.
 * 
 */
 public void testDescendComparatorSplit() {
     float q = (float) 0.9;

     int r = 10;
     int sp = 5;

     a = ExpVector.EVRAND(r,10,q);
     b = ExpVector.EVRAND(r,10,q);
     c = a.sum(b);

     t = new TermOrder(r,sp);

     int x = t.getDescendComparator().compare(c,a);
     int y = t.getDescendComparator().compare(c,b);
     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     x = t.getDescendComparator().compare(a,c);
     y = t.getDescendComparator().compare(b,c);
     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     x = t.getDescendComparator().compare(a,a);
     y = t.getDescendComparator().compare(b,b);
     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);
   }


/**
 * Test descend comparators weight and split.
 * 
 */
 public void testDescendComparatorWeightSplit() {
     float q = (float) 0.9;

     int r = 8;
     int sp = 5;
     long [][] w  = new long [][] { new long[] { 1l, 2l, 3l, 4l, 5l, 1l, 2l, 3l } };
     long [][] w2 = new long [][] { new long[] { 1l, 2l, 3l, 4l, 5l, 0l, 0l, 0l },
                                    new long[] { 0l, 0l, 0l, 0l, 0l, 1l, 2l, 3l }  };

     a = ExpVector.EVRAND(r,10,q);
     b = ExpVector.EVRAND(r,10,q);
     c = a.sum(b);

     //t = new TermOrder(w,sp);
     t = new TermOrder(w2);
     TermOrder t2 = new TermOrder(w2);
     // now t equals t2

     int x = t.getDescendComparator().compare(c,a);
     int y = t.getDescendComparator().compare(c,b);
     assertEquals("x = -1",-1,x);
     assertEquals("y = -1",-1,y);

     int x2 = t2.getDescendComparator().compare(c,a);
     int y2 = t2.getDescendComparator().compare(c,b);
     assertEquals("x2 = -1",-1,x2);
     assertEquals("y2 = -1",-1,y2);

     assertEquals("x = x2",x,x2);
     assertEquals("y = y2",y,y2);


     x = t.getDescendComparator().compare(a,c);
     y = t.getDescendComparator().compare(b,c);
     assertEquals("x = 1",1,x);
     assertEquals("y = 1",1,y);

     x2 = t2.getDescendComparator().compare(a,c);
     y2 = t2.getDescendComparator().compare(b,c);
     assertEquals("x2 = 1",1,x2);
     assertEquals("y2 = 1",1,y2);

     assertEquals("x = x2",x,x2);
     assertEquals("y = y2",y,y2);


     x = t.getDescendComparator().compare(a,a);
     y = t.getDescendComparator().compare(b,b);
     assertEquals("x = 0",0,x);
     assertEquals("y = 0",0,y);

     x2 = t2.getDescendComparator().compare(a,a);
     y2 = t2.getDescendComparator().compare(b,b);
     assertEquals("x2 = 0",0,x2);
     assertEquals("y2 = 0",0,y2);

     assertEquals("x = x2",x,x2);
     assertEquals("y = y2",y,y2);
   }


/**
 * Test exception.
 * 
 */
 public void testException() {
     float q = (float) 0.9;

     a = ExpVector.EVRAND(5,10,q);
     b = ExpVector.EVRAND(5,10,q);

     int wrong = 99;
     int x = 0;

     try {
         t = new TermOrder(wrong);
     } catch (IllegalArgumentException e) {
         return;
     }
     fail("IllegalArgumentException");
   }


/**
 * Test exception split.
 * 
 */
 public void testExceptionSplit() {
     float q = (float) 0.9;

     int r = 10;
     int sp = 5;

     a = ExpVector.EVRAND(r,10,q);
     b = ExpVector.EVRAND(r,10,q);

     int wrong = 99;
     int x = 0;

     try {
         t = new TermOrder(wrong,wrong,r,sp);
     } catch (IllegalArgumentException e) {
         return;
     }
     fail("IllegalArgumentException");
   }


/**
 * Test compare exception.
 * 
 */
 public void testCompareException() {
     float q = (float) 0.9;

     a = ExpVector.EVRAND(5,10,q);
     b = ExpVector.EVRAND(5,10,q);

     int notimpl = TermOrder.REVITDG+2;
     int x = 0;

     try {
         t = new TermOrder(notimpl);
         x = t.getDescendComparator().compare(a,b);
     } catch (IllegalArgumentException e) {
         return;
     } catch (NullPointerException e) {
         return;
     }
     fail("IllegalArgumentException");
   }


/**
 * Test compare exception split.
 * 
 */
 public void testCompareExceptionSplit() {
     float q = (float) 0.9;

     int r = 10;
     int sp = 5;

     a = ExpVector.EVRAND(r,10,q);
     b = ExpVector.EVRAND(r,10,q);

     int notimpl = TermOrder.REVITDG+2;
     int x = 0;

     try {
         t = new TermOrder(notimpl,notimpl,r,sp);
         x = t.getDescendComparator().compare(a,b);
     } catch (IllegalArgumentException e) {
         return;
     } catch (NullPointerException e) {
         return;
     }
     fail("IllegalArgumentException");
   }


/**
 * Test compare exception weight.
 * 
 */
 public void testCompareExceptionWeigth() {
     float q = (float) 0.9;

     int r = 10;
     int sp = 5;

     a = ExpVector.EVRAND(r,10,q);
     b = ExpVector.EVRAND(r,10,q);

     int x = 0;

     try {
         t = new TermOrder((long[][])null);
         x = t.getDescendComparator().compare(a,b);
     } catch (IllegalArgumentException e) {
         return;
     } catch (NullPointerException e) {
         return;
     }
     fail("IllegalArgumentException");
   }

}
