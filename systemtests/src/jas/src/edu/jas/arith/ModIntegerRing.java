/*
 * $Id: ModIntegerRing.java 1663 2008-02-05 17:32:07Z kredel $
 */

package edu.jas.arith;

import java.util.Random;
import java.io.Reader;

//import edu.jas.structure.GcdRingElem;
import edu.jas.structure.RingFactory;
//import edu.jas.kern.PrettyPrint;

import edu.jas.util.StringUtil;


/**
 * ModIntegerRing factory with RingFactory interface.
 * Effectively immutable.
 * @author Heinz Kredel
 */

public final class ModIntegerRing implements RingFactory<ModInteger> {


    /** Module part of the factory data structure. 
     */
    public final java.math.BigInteger modul;


    private final static Random random = new Random();


    /** Indicator if this ring is a field.
     */
    protected int isField = -1; // initially unknown


    /** Certainty if module is probable prime.
     */
    protected int certainty = 10;


    /** The constructor creates a ModIntegerRing object 
     * from a BigInteger object as module part. 
     * @param m math.BigInteger.
     */
    public ModIntegerRing(java.math.BigInteger m) {
        modul = m;
    }


    /** The constructor creates a ModIntegerRing object 
     * from a BigInteger object as module part. 
     * @param m math.BigInteger.
     * @param isField indicator if m is prime.
     */
    public ModIntegerRing(java.math.BigInteger m, boolean isField) {
        modul = m;
        this.isField = ( isField ? 1 :  0 );
    }


    /** The constructor creates a ModIntegerRing object 
     * from a long as module part. 
     * @param m long.
     */
    public ModIntegerRing(long m) {
        this( new java.math.BigInteger( String.valueOf(m) ) );
    }


    /** The constructor creates a ModIntegerRing object 
     * from a long as module part. 
     * @param m long.
     * @param isField indicator if m is prime.
     */
    public ModIntegerRing(long m, boolean isField) {
        this( 
             new java.math.BigInteger( String.valueOf(m) ),
             isField
             );
    }


    /** The constructor creates a ModIntegerRing object 
     * from a String object as module part. 
     * @param m String.
     */
    public ModIntegerRing(String m) {
        this( new java.math.BigInteger( m.trim() ) );
    }


    /** The constructor creates a ModIntegerRing object 
     * from a String object as module part. 
     * @param m String.
     * @param isField indicator if m is prime.
     */
    public ModIntegerRing(String m, boolean isField) {
        this( new java.math.BigInteger( m.trim() ), isField );
    }


    /** Get the module part. 
     * @return modul.
     */
    public java.math.BigInteger getModul() {
        return modul;
    }


    /** Copy ModInteger element c.
     * @param c
     * @return a copy of c.
     */
    public ModInteger copy(ModInteger c) {
        return new ModInteger( this, c.val );
    }


    /** Get the zero element.
     * @return 0 as ModInteger.
     */
    public ModInteger getZERO() {
        return new ModInteger( this, java.math.BigInteger.ZERO );
    }


    /**  Get the one element.
     * @return 1 as ModInteger.
     */
    public ModInteger getONE() {
        return new ModInteger( this, java.math.BigInteger.ONE );
    }


    /**
     * Query if this ring is commutative.
     * @return true.
     */
    public boolean isCommutative() {
        return true;
    }


    /**
     * Query if this ring is associative.
     * @return true.
     */
    public boolean isAssociative() {
        return true;
    }


    /**
     * Query if this ring is a field.
     * @return true if module is prime, else false.
     */
    public boolean isField() {
        if ( isField > 0 ) { 
           return true;
        }
        if ( isField == 0 ) { 
           return false;
        }
        if ( modul.isProbablePrime(certainty) ) {
           isField = 1;
           return true;
        }
        isField = 0;
        return false;
    }


    /**
     * Characteristic of this ring.
     * @return characteristic of this ring.
     */
    public java.math.BigInteger characteristic() {
        return modul;
    }


    /** Get a ModInteger element from a BigInteger value.
     * @param a BigInteger.
     * @return a ModInteger.
     */
    public ModInteger fromInteger(java.math.BigInteger a) {
        return new ModInteger(this,a);
    }


    /** Get a ModInteger element from a long value.
     * @param a long.
     * @return a ModInteger.
     */
    public ModInteger fromInteger(long a) {
        return new ModInteger(this, a );
    }


    /**  Get the String representation.
     * @see java.lang.Object#toString()
     */
    @Override
     public String toString() {
        return " mod(" + modul.toString() + ")";
    }


    /** Comparison with any other object.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object b) {
        if ( ! ( b instanceof ModIntegerRing ) ) {
            return false;
        }
     ModIntegerRing m = (ModIntegerRing)b;
        return ( 0 == modul.compareTo( m.modul ) );
    }


    /** Hash code for this ModIntegerRing.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return modul.hashCode();
    }


    /** ModInteger random.
     * @param n such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @return a random integer mod modul.
     */
    public ModInteger random(int n) {
        return random( n, random );
    }


    /** ModInteger random.
     * @param n such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @param rnd is a source for random bits.
     * @return a random integer mod modul.
     */
    public ModInteger random(int n, Random rnd) {
        java.math.BigInteger v = new java.math.BigInteger( n, rnd );
        return new ModInteger( this, v );
    }


    /** Parse ModInteger from String.
     * @param s String.
     * @return ModInteger from s.
     */
    public ModInteger parse(String s) {
        return new ModInteger(this,s);
    }


    /** Parse ModInteger from Reader.
     * @param r Reader.
     * @return next ModInteger from r.
     */
    public ModInteger parse(Reader r) {
	throw new RuntimeException("not supported.");
    }


    /** ModInteger chinese remainder algorithm.  
     * This is a factory method.
     * Assert c.modul >= a.modul and c.modul * a.modul = this.modul.
     * @param c ModInteger.
     * @param ci inverse of c.modul in ring of a.
     * @param a other ModInteger.
     * @return S, with S mod c.modul == c and S mod a.modul == a. 
     */
    public ModInteger 
           chineseRemainder(ModInteger c, 
                            ModInteger ci, 
                            ModInteger a) {
        if ( false ) { // debug
           if ( c.ring.modul.compareTo( a.ring.modul ) < 1 ) {
               System.out.println("ModInteger error " + c + ", " + a);
           }
        }
        ModInteger b = a.ring.fromInteger( c.val ); // c mod a.modul
        ModInteger d = a.subtract( b ); // a-c mod a.modul
        if ( d.isZERO() ) {
           return fromInteger( c.val );
        }
        b = d.multiply( ci ); // b = (a-c)*ci mod a.modul
        // (c.modul*b)+c mod this.modul = c mod c.modul = 
        // (c.modul*ci*(a-c)+c) mod a.modul = a mod a.modul
        java.math.BigInteger s = c.ring.modul.multiply( b.val );
        s = s.add( c.val );
        return fromInteger( s );
    }

}
