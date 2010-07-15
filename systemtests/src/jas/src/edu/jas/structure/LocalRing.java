/*
 * $Id: LocalRing.java 1663 2008-02-05 17:32:07Z kredel $
 */

package edu.jas.structure;

import java.util.Random;
import java.io.Reader;

import org.apache.log4j.Logger;

import edu.jas.structure.RingElem;
import edu.jas.structure.RingFactory;


/**
 * Local ring factrory based on RingElem principal ideal.
 * Objects of this class are immutable.
 * @author Heinz Kredel
 */
public class LocalRing<C extends RingElem<C> > 
             implements RingFactory< Local<C> >  {

     private static final Logger logger = Logger.getLogger(LocalRing.class);
     //private boolean debug = logger.isDebugEnabled();


    /** Ideal generator for localization. 
     */
    protected final C ideal;


    /** Ring factory. 
     */
    protected final RingFactory<C> ring;


    /** Indicator if this ring is a field.
     */
    protected int isField = -1; // initially unknown


    /** The constructor creates a LocalRing object 
     * from a RingFactory and a RingElem. 
     * @param i localization ideal generator.
     */
    public LocalRing(RingFactory<C> r, C i) {
        ring = r;
        if ( i == null ) {
           throw new RuntimeException("ideal may not be null");
        }
        ideal = i;
        if ( ideal.isONE() ) {
           throw new RuntimeException("ideal may not be 1");
        }
    }


    /** Copy Local element c.
     * @param c
     * @return a copy of c.
     */
    public Local<C> copy(Local<C> c) {
        return new Local<C>( c.ring, c.num, c.den, true );
    }


    /** Get the zero element.
     * @return 0 as Local.
     */
    public Local<C> getZERO() {
        return new Local<C>( this, ring.getZERO() );
    }


    /**  Get the one element.
     * @return 1 as Local.
     */
    public Local<C> getONE() {
        return new Local<C>( this, ring.getONE() );
    }

    
    /**
     * Query if this ring is commutative.
     * @return true if this ring is commutative, else false.
     */
    public boolean isCommutative() {
        return ring.isCommutative();
    }


    /**
     * Query if this ring is associative.
     * @return true if this ring is associative, else false.
     */
    public boolean isAssociative() {
        return ring.isAssociative();
    }


    /**
     * Query if this ring is a field.
     * @return false.
     */
    public boolean isField() {
        if ( isField > 0 ) { 
           return true;
        }
        if ( isField == 0 ) { 
           return false;
        }
        // ??
        return false;
    }


    /**
     * Characteristic of this ring.
     * @return characteristic of this ring.
     */
    public java.math.BigInteger characteristic() {
        return ring.characteristic();
    }


    /** Get a Local element from a BigInteger value.
     * @param a BigInteger.
     * @return a Local.
     */
    public Local<C> fromInteger(java.math.BigInteger a) {
        return new Local<C>( this, ring.fromInteger(a) );
    }


    /** Get a Local element from a long value.
     * @param a long.
     * @return a Local.
     */
    public Local<C> fromInteger(long a) {
        return new Local<C>( this, ring.fromInteger(a) );
    }
    

    /** Get the String representation as RingFactory.
     * @see java.lang.Object#toString()
     */
    @Override
     public String toString() {
        return "Local[ " 
                + ideal.toString() + " ]";
    }


    /** Comparison with any other object.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked") // not jet working
    public boolean equals(Object b) {
        if ( ! ( b instanceof LocalRing ) ) {
           return false;
        }
        LocalRing<C> a = null;
        try {
            a = (LocalRing<C>) b;
        } catch (ClassCastException e) {
        }
        if ( a == null ) {
            return false;
        }
        if ( ! ring.equals( a.ring ) ) {
            return false;
        }
        return ideal.equals( a.ideal );
    }


    /** Hash code for this local ring.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() { 
       int h;
       h = ring.hashCode();
       h = 37 * h + ideal.hashCode();
       return h;
    }


    /** Local random.
     * @param n such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @return a random residue element.
     */
    public Local<C> random(int n) {
      C r = ring.random( n );
      C s = ring.random( n );
      s = s.remainder( ideal );
      while ( s.isZERO() ) {
          logger.debug("zero was in ideal");
          s = ring.random( n );
          s = s.remainder( ideal );
      }
      return new Local<C>( this, r, s, false );
    }


    /** Local random.
     * @param n such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @param rnd is a source for random bits.
     * @return a random residue element.
     */
    public Local<C> random(int n, Random rnd) {
      C r = ring.random( n, rnd );
      C s = ring.random( n, rnd );
      s = s.remainder( ideal );
      while ( s.isZERO() ) {
          logger.debug("zero was in ideal");
          s = ring.random( n, rnd );
          s = s.remainder( ideal );
      }
      return new Local<C>( this, r, s, false );
    }


    /** Parse Local from String.
     * @param s String.
     * @return Local from s.
     */
    public Local<C> parse(String s) {
        C x = ring.parse( s );
        return new Local<C>( this, x );
    }


    /** Parse Local from Reader.
     * @param r Reader.
     * @return next Local from r.
     */
    public Local<C> parse(Reader r) {
	throw new RuntimeException("not supported.");
    }

}
