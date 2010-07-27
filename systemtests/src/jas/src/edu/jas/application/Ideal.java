/*
 * $Id: Ideal.java 1945 2008-07-23 21:19:17Z kredel $
 */

package edu.jas.application;

import java.lang.Comparable;
import java.lang.Cloneable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import java.io.Serializable;

import org.apache.log4j.Logger;

import edu.jas.structure.GcdRingElem;
import edu.jas.structure.NotInvertibleException;

import edu.jas.poly.ExpVector;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolynomialList;
import edu.jas.poly.OptimizedPolynomialList;
import edu.jas.poly.TermOrderOptimization;

import edu.jas.ring.ExtendedGB;
import edu.jas.ring.GroebnerBase;
import edu.jas.ring.Reduction;
import edu.jas.ring.GroebnerBaseSeqPairSeq;
import edu.jas.ring.ReductionSeq;

import edu.jas.ufd.GreatestCommonDivisor;
import edu.jas.ufd.GCDFactory;


/**
 * Ideal implements some methods for ideal arithmetic, 
 * for example intersection and quotient.
 * @author Heinz Kredel
 */
public class Ideal<C extends GcdRingElem<C>> 
    implements Comparable<Ideal<C>>, Serializable, Cloneable {


  private static final Logger logger = Logger.getLogger(Ideal.class);
  private boolean debug = true || logger.isDebugEnabled();



  /** 
   * The data structure is a PolynomialList. 
   */
  protected PolynomialList<C> list;


  /** 
   * Indicator if list is a Groebner Base. 
   */
  protected boolean isGB;


  /** 
   * Indicator if test has been performed if this is a Groebner Base. 
   */
  protected boolean testGB;


  /** 
   * Indicator if list has optimized term order. 
   */
  protected boolean isTopt;


  /** 
   * Groebner base engine. 
   */
  protected final GroebnerBase<C> bb;


  /**
   * Reduction engine.
   */
  protected final Reduction<C> red;


  /**
    * Greatest common divisor engine.
    */
  protected final GreatestCommonDivisor<C> engine;


  /**
   * Constructor.
   * @param ring polynomial ring
   * @param F list of polynomials
   */
  public Ideal( GenPolynomialRing<C> ring, List<GenPolynomial<C>> F ) {
      this( new PolynomialList<C>( ring, F ) );
  }


  /**
   * Constructor.
   * @param ring polynomial ring
   * @param F list of polynomials
   * @param gb true if F is known to be a Groebner Base, else false
   */
  public Ideal( GenPolynomialRing<C> ring, 
                List<GenPolynomial<C>> F, boolean gb ) {
      this( new PolynomialList<C>( ring, F ), gb );
  }


  /**
   * Constructor.
   * @param ring polynomial ring
   * @param F list of polynomials
   * @param gb true if F is known to be a Groebner Base, else false
   * @param topt true if term order is optimized, else false
   */
  public Ideal( GenPolynomialRing<C> ring, 
                List<GenPolynomial<C>> F, boolean gb, boolean topt ) {
      this( new PolynomialList<C>( ring, F ), gb, topt );
  }


  /**
   * Constructor.
   * @param list polynomial list
   */
  public Ideal( PolynomialList<C> list) {
      this( list, false );
  }


  /**
   * Constructor.
   * @param list polynomial list
   * @param bb Groebner Base engine
   * @param red Reduction engine
   */
  public Ideal( PolynomialList<C> list,
                GroebnerBase<C> bb, Reduction<C> red) {
      this( list, false, bb, red );
  }


  /**
   * Constructor.
   * @param list polynomial list
   * @param gb true if list is known to be a Groebner Base, else false
   */
  public Ideal( PolynomialList<C> list, boolean gb) {
      this(list, gb, new GroebnerBaseSeqPairSeq<C>(), new ReductionSeq<C>() );
  }


  /**
   * Constructor.
   * @param list polynomial list
   * @param gb true if list is known to be a Groebner Base, else false
   * @param topt true if term order is optimized, else false
   */
    public Ideal( PolynomialList<C> list, boolean gb, boolean topt) {
     this(list, gb, topt, new GroebnerBaseSeqPairSeq<C>(), new ReductionSeq<C>() );
  }


  /**
   * Constructor.
   * @param list polynomial list
   * @param gb true if list is known to be a Groebner Base, else false
   * @param bb Groebner Base engine
   * @param red Reduction engine
   */
  public Ideal( PolynomialList<C> list, boolean gb,
                GroebnerBase<C> bb, Reduction<C> red) {
       this(list, gb, false, new GroebnerBaseSeqPairSeq<C>(), new ReductionSeq<C>() );
   }


  /**
   * Constructor.
   * @param list polynomial list
   * @param gb true if list is known to be a Groebner Base, else false
   * @param topt true if term order is optimized, else false
   * @param bb Groebner Base engine
   * @param red Reduction engine
   */
   public Ideal( PolynomialList<C> list, boolean gb, boolean topt,
                GroebnerBase<C> bb, Reduction<C> red) {
      if ( list == null || list.list == null ) {
         throw new IllegalArgumentException("list and list.list may not be null");
      }
      this.list = list;
      this.isGB = gb;
      this.isTopt = topt;
      this.testGB = ( gb ? true : false ); // ??
      this.bb = bb;
      this.red = red;
      this.engine = GCDFactory.<C>getProxy( list.ring.coFac );
  }


  /**
   * Clone this.
   * @return a copy of this.
   */
  @Override
  public Ideal<C> clone() {
      return new Ideal<C>( list.clone(), isGB, isTopt, bb, red);
  }


  /**
   * Get the List of GenPolynomials.
   * @return list.list
   */
  public List< GenPolynomial<C> > getList() {
      return list.list;
  }


  /**
   * Get the GenPolynomialRing.
   * @return list.ring
   */
  public GenPolynomialRing<C> getRing() {
      return list.ring;
  }


  /**
   * String representation of the ideal.
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
      return list.toString();
  }


  /** Comparison with any other object.
   * Note: If both ideals are not Groebner Bases, then
   *       false may be returned even the ideals are equal.
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  @SuppressWarnings("unchecked") 
  public boolean equals(Object b) {
      if ( ! (b instanceof Ideal) ) {
         logger.warn("equals no Ideal");
         return false;
      }
      Ideal<C> B = null;
      try {
          B = (Ideal<C>)b;
      } catch (ClassCastException ignored) {
          return false;
      }
      //if ( isGB && B.isGB ) {
      //   return list.equals( B.list ); requires also monic polys
      //} else { // compute GBs ?
      return this.contains( B ) && B.contains( this );
      //}
  }


    /** Ideal list comparison.  
     * @param L other Ideal.
     * @return compareTo() of polynomial lists.
     */
    public int compareTo(Ideal<C> L) {
        return list.compareTo( L.list );
    }


   /** Hash code for this ideal.
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() { 
      int h;
      h = list.hashCode();
      if ( isGB ) {
          h = h << 1;
      }
      if ( testGB ) {
          h += 1;
      }
      return h;
   }


  /**
   * Test if ZERO ideal.
   * @return true, if this is the 0 ideal, else false
   */
  public boolean isZERO() {
      return list.isZERO();
  }


  /**
   * Test if ONE ideal.
   * @return true, if this is the 1 ideal, else false
   */
  public boolean isONE() {
      return list.isONE();
  }


  /**
   * Optimize the term order.
   */
  public void doToptimize() {
      if ( isTopt ) {
          return;
      }
      list = TermOrderOptimization.<C>optimizeTermOrder( list );
      isTopt = true;
      if ( isGB ) {
      isGB = false;
         doGB();
      }
      return;
  }


  /**
   * Test if this is a Groebner base.
   * @return true, if this is a Groebner base, else false
   */
  public boolean isGB() {
      if ( testGB ) {
         return isGB;
      }
      logger.warn("isGB computing");
      isGB = bb.isGB( getList() );
      testGB = true;
      return isGB;
  }


  /**
   * Do Groebner Base. compute the Groebner Base for this ideal.
   */
  public void doGB() {
      if ( isGB && testGB ) {
         return;
      }
      //logger.warn("GB computing");
      List< GenPolynomial<C> > G = getList();
      logger.info("GB computing = " + G);
      G = bb.GB( G );
      if ( isTopt ) { 
         List<Integer> perm = ((OptimizedPolynomialList<C>) list).perm;
         list = new OptimizedPolynomialList<C>( perm, getRing(), G );
      } else {
         list = new PolynomialList<C>( getRing(), G );
      }
      isGB = true;
      testGB = true;
      return;
  }


  /**
   * Groebner Base. Get a Groebner Base for this ideal.
   * @return GB(this)
   */
  public Ideal<C> GB() {
      if ( isGB ) {
          return this;
      }
      doGB();
      return this;
  }


  /**
   * Ideal containment. Test if B is contained in this ideal.
   * Note: this is eventually modified to become a Groebner Base.
   * @param B ideal
   * @return true, if B is contained in this, else false
   */
  public boolean contains( Ideal<C> B ) {
      if ( B == null || B.isZERO() ) {
          return true;
      }
      if ( this.isONE() ) {
          return true;
      }
      if ( !isGB ) {
         doGB();
      }
      List< GenPolynomial<C> > z;
      z = red.normalform( getList(), B.getList() );
      if ( z == null ) {
          return true;
      }
      for ( GenPolynomial<C> p : z ) {
          if ( p == null ) {
              continue;
          }
          if ( ! p.isZERO() ) {
             return false;
          }
      }
      return true;
  }


  /**
   * Ideal containment. Test if b is contained in this ideal.
   * Note: this is eventually modified to become a Groebner Base.
   * @param b polynomial
   * @return true, if b is contained in this, else false
   */
  public boolean contains( GenPolynomial<C> b ) {
      if ( b == null || b.isZERO() ) {
          return true;
      }
      if ( this.isONE() ) {
          return true;
      }
      if ( this.isZERO() ) {
          return false;
      }
      if ( !isGB ) {
         doGB();
      }
      GenPolynomial<C> z;
      z = red.normalform( getList(), b );
      if ( z == null || z.isZERO() ) {
          return true;
      }
      return false;
  }


  /**
   * Summation. Generators for the sum of ideals.
   * Note: if both ideals are Groebner bases, a Groebner base is returned.
   * @param B ideal
   * @return ideal(this+B)
   */
  public Ideal<C> sum( Ideal<C> B ) {
      if ( B == null || B.isZERO() ) {
          return this;
      }
      if ( this.isZERO() ) {
          return B;
      }
      int s = getList().size() + B.getList().size();
      List< GenPolynomial<C> > c;
      c = new ArrayList<GenPolynomial<C>>( s );
      c.addAll( getList() );
      c.addAll( B.getList() );
      Ideal<C> I = new Ideal<C>( getRing(), c, false );
      if ( isGB && B.isGB ) {
         I.doGB();
      }
      return I;
  }


  /**
   * Summation. Generators for the sum of ideal and a polynomial.
   * Note: if this ideal is a Groebner base, a Groebner base is returned.
   * @param b polynomial
   * @return ideal(this+{b})
   */
  public Ideal<C> sum( GenPolynomial<C> b ) {
      if ( b == null || b.isZERO() ) {
          return this;
      }
      int s = getList().size() + 1;
      List< GenPolynomial<C> > c;
      c = new ArrayList<GenPolynomial<C>>( s );
      c.addAll( getList() );
      c.add( b );
      Ideal<C> I = new Ideal<C>( getRing(), c, false );
      if ( isGB ) {
         I.doGB();
      }
      return I;
  }


  /**
   * Product. Generators for the product of ideals.
   * Note: if both ideals are Groebner bases, a Groebner base is returned.
   * @param B ideal
   * @return ideal(this*B)
   */
  public Ideal<C> product( Ideal<C> B ) {
      if ( B == null || B.isZERO() ) {
          return B;
      }
      if ( this.isZERO() ) {
          return this;
      }
      int s = getList().size() * B.getList().size();
      List< GenPolynomial<C> > c;
      c = new ArrayList<GenPolynomial<C>>( s );
      for ( GenPolynomial<C> p : getList() ) {
          for ( GenPolynomial<C> q : B.getList() ) {
              q = p.multiply(q);
              c.add(q);
          }
      }
      Ideal<C> I = new Ideal<C>( getRing(), c, false );
      if ( isGB && B.isGB ) {
         I.doGB();
      }
      return I;
  }


  /**
   * Intersection. Generators for the intersection of ideals.
   * @param B ideal
   * @return ideal(this \cap B), a Groebner base
   */
  public Ideal<C> intersect( Ideal<C> B ) {
      if ( B == null || B.isZERO() ) { // (0)
          return B;
      }
      if ( this.isZERO() ) { 
          return this;
      }
      int s = getList().size() + B.getList().size();
      List< GenPolynomial<C> > c;
      c = new ArrayList<GenPolynomial<C>>( s );
      List< GenPolynomial<C> > a = getList();
      List< GenPolynomial<C> > b = B.getList();

      GenPolynomialRing<C> tfac = getRing().extend(1);
      // term order is also adjusted
      for ( GenPolynomial<C> p : a ) {
          p = p.extend( tfac, 0, 1L ); // t*p
          c.add( p );
      }
      for ( GenPolynomial<C> p : b ) {
          GenPolynomial<C> q = p.extend( tfac, 0, 1L );
          GenPolynomial<C> r = p.extend( tfac, 0, 0L );
          p = r.subtract( q ); // (1-t)*p
          c.add( p );
      }
      logger.warn("intersect computing GB");
      List< GenPolynomial<C> > g = bb.GB( c );
      if ( debug ) {
         logger.debug("intersect GB = " + g);
      }
      Ideal<C> E = new Ideal<C>( tfac, g, true );
      Ideal<C> I = E.intersect( getRing() );
      return I;
  }


  /**
   * Intersection. Generators for the intersection of a ideal 
   * with a polynomial ring. The polynomial ring of this ideal
   * must be a contraction of R and the TermOrder must be 
   * an elimination order.
   * @param R polynomial ring
   * @return ideal(this \cap R)
   */
  public Ideal<C> intersect( GenPolynomialRing<C> R ) {
      if ( R == null ) { 
         throw new IllegalArgumentException("R may not be null");
      }
      int d = getRing().nvar - R.nvar;
      if ( d <= 0 ) {
          return this;
      }
      //GenPolynomialRing<C> tfac = getRing().contract(d);
      //if ( ! tfac.equals( R ) ) { // check ?
      //   throw new RuntimeException("contract(this) != R");
      //}
      List< GenPolynomial<C> > h;
      h = new ArrayList<GenPolynomial<C>>( getList().size() );
      for ( GenPolynomial<C> p : getList() ) {
          Map<ExpVector,GenPolynomial<C>> m = null;
          m = p.contract( R );
          if ( debug ) {
             logger.debug("intersect contract m = " + m);
          }
          if ( m.size() == 1 ) { // contains one power of variables
             for ( ExpVector e : m.keySet() ) {
                 if ( e.isZERO() ) {
                    h.add( m.get( e ) );
                 }
             }
          }
      }
      return new Ideal<C>( R, h, isGB, isTopt );
  }


  /**
   * Quotient. Generators for the ideal quotient.
   * @param h polynomial
   * @return ideal(this : h), a Groebner base
   */
  public Ideal<C> quotient( GenPolynomial<C> h ) {
      if ( h == null ) { // == (0)
          return this;
      }
      if ( h.isZERO() ) { 
          return this;
      }
      if ( this.isZERO() ) { 
          return this;
      }
      List< GenPolynomial<C> > H;
      H = new ArrayList<GenPolynomial<C>>( 1 );
      H.add( h );
      Ideal<C> Hi = new Ideal<C>( getRing(), H, true );

      Ideal<C> I = this.intersect( Hi );

      List< GenPolynomial<C> > Q;
      Q = new ArrayList<GenPolynomial<C>>( I.getList().size() );
      for ( GenPolynomial<C> q : I.getList() ) {
          q = q.divide(h); // remainder == 0
          Q.add( q );
      }
      return new Ideal<C>( getRing(), Q, true /*false?*/ );
  }


  /**
   * Quotient. Generators for the ideal quotient.
   * @param H ideal
   * @return ideal(this : H), a Groebner base
   */
  public Ideal<C> quotient( Ideal<C> H ) {
      if ( H == null ) { // == (0)
          return this;
      }
      if ( H.isZERO() ) { 
          return this;
      }
      if ( this.isZERO() ) { 
          return this;
      }
      Ideal<C> Q = null;
      for ( GenPolynomial<C> h : H.getList() ) {
          Ideal<C> Hi = this.quotient(h);
          if ( Q == null ) {
             Q = Hi;
          } else {
             Q = Q.intersect( Hi );
          }
      }
      return Q;
  }


  /**
   * Infinite quotient. Generators for the infinite ideal quotient.
   * @param h polynomial
   * @return ideal(this : h<sup>s</sup>), a Groebner base
   */
  public Ideal<C> infiniteQuotientRab( GenPolynomial<C> h ) {
      if ( h == null ) { // == (0)
          return this;
      }
      if ( h.isZERO() ) { 
          return this;
      }
      if ( this.isZERO() ) {
          return this;
      }
      Ideal<C> I = this.GB(); // should be already
      List< GenPolynomial<C> > a = I.getList();
      List< GenPolynomial<C> > c;
      c = new ArrayList<GenPolynomial<C>>( a.size()+1 );

      GenPolynomialRing<C> tfac = getRing().extend(1);
      // term order is also adjusted
      for ( GenPolynomial<C> p : a ) {
          p = p.extend( tfac, 0, 0L ); // p
          c.add( p );
      }
      GenPolynomial<C> q  = h.extend( tfac, 0, 1L );
      GenPolynomial<C> r  = h.extend( tfac, 0, 0L );
      GenPolynomial<C> hs = r.subtract( q ); // (1-t)*h
      c.add( hs );
      logger.warn("intersect computing GB");
      List< GenPolynomial<C> > g = bb.GB( c );
      if ( debug ) {
         logger.debug("intersect GB = " + g);
      }
      Ideal<C> E = new Ideal<C>( tfac, g, true );
      Ideal<C> Is = E.intersect( getRing() );
      return Is;
  }


  /**
   * Infinite quotient. Generators for the infinite ideal quotient.
   * @param h polynomial
   * @return ideal(this : h<sup>s</sup>), a Groebner base
   */
  public Ideal<C> infiniteQuotient( GenPolynomial<C> h ) {
      if ( h == null ) { // == (0)
          return this;
      }
      if ( h.isZERO() ) { 
          return this;
      }
      if ( this.isZERO() ) {
          return this;
      }
      int s = 0;
      Ideal<C> I = this.GB(); // should be already
      GenPolynomial<C> hs = h;
      Ideal<C> Is = I;

      boolean eq = false;
      while ( !eq ) {
        Is = I.quotient( hs );
        Is = Is.GB(); // should be already
        logger.info("infiniteQuotient s = " + s);
        eq = Is.contains(I);  // I.contains(Is) always
        if ( !eq ) {
           I = Is;
           s++;
           // hs = hs.multiply( h );
        }
      }
      return Is;
  }


  /**
   * Infinite quotient. Generators for the infinite ideal quotient.
   * @param h polynomial
   * @return ideal(this : h<sup>s</sup>), a Groebner base
   */
  public Ideal<C> infiniteQuotientOld( GenPolynomial<C> h ) {
      if ( h == null ) { // == (0)
          return this;
      }
      if ( h.isZERO() ) { 
          return this;
      }
      if ( this.isZERO() ) {
          return this;
      }
      int s = 0;
      Ideal<C> I = this.GB(); // should be already
      GenPolynomial<C> hs = h;

      boolean eq = false;
      while ( !eq ) {
        Ideal<C> Is = I.quotient( hs );
        Is = Is.GB(); // should be already
        logger.debug("infiniteQuotient s = " + s);
        eq = Is.contains(I);  // I.contains(Is) always
        if ( !eq ) {
           I = Is;
           s++;
           hs = hs.multiply( h );
        }
      }
      return I;
  }


  /**
   * Infinite Quotient. Generators for the ideal infinite  quotient.
   * @param H ideal
   * @return ideal(this : H<sup>s</sup>), a Groebner base
   */
  public Ideal<C> infiniteQuotient( Ideal<C> H ) {
      if ( H == null ) { // == (0)
          return this;
      }
      if ( H.isZERO() ) { 
          return this;
      }
      if ( this.isZERO() ) { 
          return this;
      }
      Ideal<C> Q = null;
      for ( GenPolynomial<C> h : H.getList() ) {
          Ideal<C> Hi = this.infiniteQuotient(h);
          if ( Q == null ) {
             Q = Hi;
          } else {
             Q = Q.intersect( Hi );
          }
      }
      return Q;
  }


  /**
   * Infinite Quotient. Generators for the ideal infinite  quotient.
   * @param H ideal
   * @return ideal(this : H<sup>s</sup>), a Groebner base
   */
  public Ideal<C> infiniteQuotientRab( Ideal<C> H ) {
      if ( H == null ) { // == (0)
          return this;
      }
      if ( H.isZERO() ) { 
          return this;
      }
      if ( this.isZERO() ) { 
          return this;
      }
      Ideal<C> Q = null;
      for ( GenPolynomial<C> h : H.getList() ) {
          Ideal<C> Hi = this.infiniteQuotientRab(h);
          if ( Q == null ) {
             Q = Hi;
          } else {
             Q = Q.intersect( Hi );
          }
      }
      return Q;
  }


  /**
   * Normalform for element.
   * @param h polynomial
   * @return normalform of h with respect to this
   */
  public GenPolynomial<C> normalform( GenPolynomial<C> h ) {
      if ( h == null ) { 
          return h;
      }
      if ( h.isZERO() ) { 
          return h;
      }
      if ( this.isZERO() ) { 
          return h;
      }
      GenPolynomial<C> r;
      r = red.normalform( list.list, h );
      return r;
  }


  /**
   * Normalform for list of elements.
   * @param L polynomial list
   * @return list of normalforms of the elements of L with respect to this
   */
  public List<GenPolynomial<C>> normalform( List<GenPolynomial<C>> L ) {
      if ( L == null ) { 
          return L;
      }
      if ( L.size() == 0 ) { 
          return L;
      }
      if ( this.isZERO() ) { 
          return L;
      }
      List<GenPolynomial<C>> M = new ArrayList<GenPolynomial<C>>( L.size() );
      for ( GenPolynomial<C> h : L ) {
          GenPolynomial<C> r = normalform( h );
          if ( r != null && !r.isZERO() ) {
             M.add( r );
          }
      }
      return M;
  }


  /**
   * Inverse for element modulo this ideal.
   * @param h polynomial
   * @return inverse of h with respect to this, if defined
   */
  public GenPolynomial<C> inverse( GenPolynomial<C> h ) {
      if ( h == null || h.isZERO() ) { 
         throw new RuntimeException(" zero not invertible");
      }
      if ( this.isZERO() ) { 
         throw new NotInvertibleException(" zero ideal");
      }
      List<GenPolynomial<C>> F = new ArrayList<GenPolynomial<C>>( 1 + list.list.size() );
      F.add( h );
      F.addAll( list.list );
      ExtendedGB<C> x = bb.extGB( F );
      List<GenPolynomial<C>> G = x.G;
      GenPolynomial<C> one = null;
      int i = -1;
      for ( GenPolynomial<C> p : G ) {
          i++;
          if ( p == null ) {
             continue;
          }
          if ( p.isUnit() ) {
             one = p;
             break;
          }
      }
      if ( one == null ) { 
         throw new NotInvertibleException(" h = " + h);
      }
      List<GenPolynomial<C>> row = x.G2F.get( i ); // != -1
      GenPolynomial<C> g = row.get(0);
      if ( g == null || g.isZERO() ) { 
         throw new NotInvertibleException(" h = " + h);
      }
      // adjust g to get g*h == 1
      GenPolynomial<C> f = g.multiply(h);
      GenPolynomial<C> k = red.normalform(list.list,f);
      if ( ! k.isONE() ) {
         C lbc = k.leadingBaseCoefficient();
         lbc = lbc.inverse();
         g = g.multiply( lbc );
      }
      if ( debug ) {
         //logger.info("inv G = " + G);
         //logger.info("inv G2F = " + x.G2F);
         //logger.info("inv row "+i+" = " + row);
         //logger.info("inv h = " + h);
         //logger.info("inv g = " + g);
         //logger.info("inv f = " + f);
         f = g.multiply(h);
         k = red.normalform(list.list,f);
         logger.info("inv k = " + k);
         if ( ! k.isUnit() ) {
            throw new NotInvertibleException(" k = " + k);
         }
      }
      return g;
  }


  /**
   * Test if element is a unit modulo this ideal.
   * @param h polynomial
   * @return true if h is a unit with respect to this, else false
   */
  public boolean isUnit( GenPolynomial<C> h ) {
      if ( h == null || h.isZERO() ) { 
         return false;
      }
      if ( this.isZERO() ) { 
         return false;
      }
      List<GenPolynomial<C>> F = new ArrayList<GenPolynomial<C>>( 1 + list.list.size() );
      F.add( h );
      F.addAll( list.list );
      List<GenPolynomial<C>> G = bb.GB( F );
      for ( GenPolynomial<C> p : G ) {
          if ( p == null ) {
             continue;
          }
          if ( p.isUnit() ) {
             return true;
          }
      }
      return false;
  }


  /**
   * Radical approximation. Squarefree generators for the ideal.
   * @return squarefree(this), a Groebner base
   */
  public Ideal<C> squarefree() {
      if ( this.isZERO() ) { 
          return this;
      }
      Ideal<C> R = this;
      Ideal<C> Rp = null;
      List<GenPolynomial<C>> li, ri;
      while ( true ) {
          li = R.getList();
          ri = new ArrayList<GenPolynomial<C>>( li.size() );
          for ( GenPolynomial<C> h : li ) {
              GenPolynomial<C> r = engine.squarefreePart( h );
              ri.add( r );
          }
          Rp = new Ideal<C>( R.getRing(), ri, false ); 
          Rp.doGB();
          if ( R.equals( Rp ) ) {
             break;
          }
          R = Rp;
      }
      return R;
  }


  /**
   * Ideal common zero test.
   * @return -1, 0 or 1 if dimension(this) &eq; -1, 0 or &ge; 1.
   */
  public int commonZeroTest() {
      if ( this.isZERO() ) {
         return 1;
      }
      if ( !isGB ) {
         doGB();
      }
      if ( this.isONE() ) {
         return -1;
      }
      if ( this.list.ring.nvar <= 0 ) {
         return -1;
      }
      //int uht = 0;
      Set<Integer> v = new HashSet<Integer>(); // for non reduced GBs
      // List<GenPolynomial<C>> Z = this.list.list;
      for ( GenPolynomial<C> p : getList() ) {
          ExpVector e = p.leadingExpVector();
          if ( e == null ) {
             continue;
          }
          int[] u = e.dependencyOnVariables();
          if ( u == null ) {
             continue;
          }
          if ( u.length == 1 ) {
             //uht++;
             v.add( u[0] );
          }
      }
      if ( this.list.ring.nvar == v.size() ) {
         return 0;
      }
      return 1;
  }


  /**
   * Ideal dimension.
   * @return a dimension container (dim,maxIndep,list(maxIndep)).
   */
  public Dim dimension() {
      int t = commonZeroTest();
      Set<Integer> S = new HashSet<Integer>();
      Set<Set<Integer>> M = new HashSet<Set<Integer>>();
      if ( t <= 0 ) {
      return new Dim(t,S,M,null);
      }
      int d = 0;
      Set<Integer> U = new HashSet<Integer>();
      for ( int i = 0; i < this.list.ring.nvar; i++ ) {
          U.add( i );
      }
      M = dimension(S,U,M);
      for ( Set<Integer> m : M ) {
          int dp = m.size();
          if ( dp > d ) {
             d = dp;
             S = m;
          }
      }
      return new Dim(d,S,M,this.list.ring.getVars());
  }


  /**
   * Ideal dimension.
   * @param S is a set of independent variables.
   * @param U is a set of variables of unknown status.
   * @param M is a list of maximal sets of independent variables.
   * @return a list of maximal sets of independent variables, eventually containing S.
   */
  protected Set<Set<Integer>> dimension(Set<Integer> S, Set<Integer> U, Set<Set<Integer>> M) {
      Set<Set<Integer>> MP = M;
      Set<Integer> UP = new HashSet<Integer>( U );
      for ( Integer j : U ) {
          UP.remove( j );
          Set<Integer> SP = new HashSet<Integer>( S );
          SP.add( j );
          if ( ! containsHT( SP, getList() ) ) {
             MP = dimension( SP, UP, MP );
          }
      }
      boolean contained = false;
      for ( Set<Integer> m : MP ) {
          if ( m.containsAll( S ) ) {
             contained = true;
             break;
       }
      }
      if ( ! contained ) {
         MP.add( S );
      }
      return MP;
  }


  /**
   * Ideal head term containment test.
   * @param G list of polynomials.
   * @param H index set.
   * @return true, if the vaiables of the head terms of each polynomial in G are contained in H, 
   * else false.
   */
  protected boolean containsHT(Set<Integer> H, List<GenPolynomial<C>> G) {
      Set<Integer> S = null;
      for ( GenPolynomial<C> p : G ) {
          if ( p == null ) {
             continue;
          }
          ExpVector e = p.leadingExpVector();
          if ( e == null ) {
             continue;
          }
          int[] v = e.dependencyOnVariables();
          if ( v == null ) {
             continue;
          }
       // System.out.println("v = " + Arrays.toString(v));
       if ( S == null ) { // revert indices
          S = new HashSet<Integer>( H.size() );
          int r = e.length()-1;
          for ( Integer i : H ) {
           S.add( r-i );
          }
       }
          if ( contains( v, S ) ) { // v \subset S
             return true;
          }
      }
      return false;
  }


  /**
   * Set containment.
   * is v \subset H.
   * @param v index array.
   * @param H index set.
   * @return true, if each element of v is contained in H, else false .
   */
  protected boolean contains(int[] v, Set<Integer> H) {
      for ( int i = 0; i < v.length; i++ ) {
          if ( ! H.contains( v[i] ) ) {
             return false;
          }
      }
      return true;
  }


  /**
   * Container for dimension parameters.
   */
  public static class Dim implements Serializable {
        public final int d;
        public final Set<Integer> S;
        public final Set<Set<Integer>> M;
        public final String[] v;



     public Dim(int d, Set<Integer> S, Set<Set<Integer>> M, String[] v) {
            this.d = d;
            this.S = S;
            this.M = M;
            this.v = v;
     }


     /**
      * String representation of the ideal.
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
     StringBuffer sb = new StringBuffer("Dimension( " + d + ", ");
     if ( v == null ) {
         sb.append( "" + S + ", " + M + " )" );
         return sb.toString();
     }
     String[] s = new String[ S.size() ];
     int j = 0;
     for ( Integer i : S ) {
         s[j] = v[i]; j++;
     }
     sb.append( Arrays.toString( s ) + ", " );
     sb.append("[ ");
     boolean first = true;
     for ( Set<Integer> m : M ) {
         if ( first ) {
            first = false;
         } else {
               sb.append( ", " );
         }
         s = new String[ m.size() ];
         j = 0;
         for ( Integer i : m ) {
          s[j] = v[i]; j++;
         }
         sb.append( Arrays.toString( s ) );
     }
     sb.append(" ] )");
        return sb.toString();
     }

  }

}
