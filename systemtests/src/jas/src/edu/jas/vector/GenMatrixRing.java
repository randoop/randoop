/*
 * $Id: GenMatrixRing.java 1265 2007-07-29 10:22:22Z kredel $
 */

package edu.jas.vector;

//import java.io.IOException;
import java.io.Reader;
//import java.io.StringReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import edu.jas.structure.RingElem;
import edu.jas.structure.RingFactory;
import edu.jas.structure.AlgebraFactory;

//import edu.jas.util.StringUtil;

/**
 * GenMatrixRing implements a generic matrix algebra factory with RingFactory.
 * Matrices of n rows and m columns over C.
 * @author Heinz Kredel
 */

public class GenMatrixRing<C extends RingElem<C> > 
            implements AlgebraFactory< GenMatrix<C>, C > {

    private static final Logger logger = Logger.getLogger(GenMatrixRing.class);

    public final RingFactory< C > coFac;

    public final int rows;

    public final int cols;

    public final int blocksize;

    public final static int DEFAULT_BSIZE = 10; 

    public final GenMatrix<C> ZERO;

    public final GenMatrix<C> ONE;

    private final static Random random = new Random(); 

    public final static float DEFAULT_DENSITY = 0.5f; 

    private float density = DEFAULT_DENSITY; 



/**
 * Constructors for GenMatrixRing.
 * @param b coefficient factory. 
 * @param r number of rows. 
 * @param c number of colums. 
 */

    public GenMatrixRing(RingFactory< C > b, int r, int c) {
        this(b,r,c,DEFAULT_BSIZE);
    }


/**
 * Constructors for GenMatrixRing.
 * @param b coefficient factory. 
 * @param r number of rows. 
 * @param c number of colums. 
 * @param s block size for blocked operations. 
 */

    public GenMatrixRing(RingFactory< C > b, int r, int c, int s) {
        if ( b == null ) {
            throw new RuntimeException("RingFactory is null");
        }
        if ( r < 1 ) {
            throw new RuntimeException("rows < 1 " + r);
        }
        if ( c < 1 ) {
            throw new RuntimeException("cols < 1 " + c);
        }
        coFac = b;
        rows = r;
        cols = c;
        blocksize = s;
        ArrayList<C> z = new ArrayList<C>( cols ); 
        for ( int i = 0; i < cols; i++ ) {
            z.add( coFac.getZERO() );
        }
        ArrayList<ArrayList<C>> m = new ArrayList<ArrayList<C>>( rows ); 
        for ( int i = 0; i < rows; i++ ) {
            m.add( (ArrayList<C>)z.clone() );
        }
        ZERO = new GenMatrix<C>( this, m );
        m = new ArrayList<ArrayList<C>>( rows ); 
        C one = coFac.getONE();
        ArrayList<C> v; 
        for ( int i = 0; i < rows; i++ ) {
            if ( i < cols ) {
               v = (ArrayList<C>)z.clone();
               v.set(i, one );
               m.add( v );
            }
        }
        ONE = new GenMatrix<C>( this, m );
    }


    /**
     * toString method.
     */
    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append( coFac.getClass().getSimpleName() );
        s.append("[" + rows + "," + cols + "]");
        return s.toString();
    }


    /**
     * Get the constant one for the GenMatrix.
     * @return ZERO.
     */
    public GenMatrix<C> getZERO() {
        return ZERO;
    }


    /**
     * Get the constant one for the GenMatrix.
     * @return 1.
     */
    public GenMatrix<C> getONE() {
        return ONE;
    }


    @Override
    public boolean equals( Object other ) { 
        if ( ! (other instanceof GenMatrixRing) ) {
            return false;
        }
        GenMatrixRing omod = (GenMatrixRing)other;
        if ( rows != omod.rows ) {
            return false;
        }
        if ( cols != omod.cols ) {
            return false;
        }
        if ( ! coFac.equals(omod.coFac) ) {
            return false;
        }
        return true;
    }


    /** Hash code for this matrix ring.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() { 
       int h;
       h = rows * 17 + cols;
       h = 37 * h + coFac.hashCode();
       return h;
    }


    /**
     * Query if this ring is a field.
     * May return false if it is to hard to determine if this ring is a field.
     * @return true if it is known that this ring is a field, else false.
     */
    public boolean isField() {
       return false;
    }


    /**
     * Query if this monoid is commutative.
     * @return true if this monoid is commutative, else false.
     */
    public boolean isCommutative() {
        return false;
    }


    /**
     * Query if this ring is associative.
     * @return true if this monoid is associative, else false.
     */
    public boolean isAssociative() {
        return (rows == cols);
    }


    /**
     * Characteristic of this ring.
     * @return characteristic of this ring.
     */
    public java.math.BigInteger characteristic() {
       return coFac.characteristic();
    }


    /**
     * Transposed matrix ring.
     * @return transposed ring factory.
     */
    public GenMatrixRing<C> transpose() {
        if ( rows == cols ) {
           return this;
        }
        return new GenMatrixRing<C>(coFac, cols, rows, blocksize);
    }


    /**
     * Product matrix ring for multiplication.
     * @param other matrix ring factory.
     * @return product ring factory.
     */
    public GenMatrixRing<C> product(GenMatrixRing<C> other) {
        if ( cols != other.rows ) {
           throw new RuntimeException("invalid dimensions in product");
        }
        if ( ! coFac.equals( other.coFac ) ) {
           throw new RuntimeException("invalid coefficients in product");
        }
        if ( rows == other.rows && cols == other.cols ) {
           return this;
        }
        return new GenMatrixRing<C>(coFac, rows, other.cols, blocksize);
    }


    /**
     * Get the matrix for a.
     * @param a long
     * @return matrix corresponding to a.
     */
    public GenMatrix<C> fromInteger(long a) {
        C c = coFac.fromInteger(a);
        return ONE.scalarMultiply(c);
    }


    /**
     * Get the matrix for a.
     * @param a long
     * @return matrix corresponding to a.
     */
    public GenMatrix<C> fromInteger(BigInteger a) {
        C c = coFac.fromInteger(a);
        return ONE.scalarMultiply(c);
    }


    /**
     * From List of coefficients.
     * @param om list of list of coefficients.
     */
    public GenMatrix<C> fromList(List<List<C>> om) {
        if ( om == null ) {
            return ZERO;
        }
        if ( om.size() > rows ) {
           throw new RuntimeException("size v > rows " + rows + " < " + om);
        }
        ArrayList<ArrayList<C>> m = new ArrayList<ArrayList<C>>( rows );
        for ( int i = 0; i < rows; i++ ) {
            List<C> ov = om.get( i++ );
            ArrayList<C> v;
            if ( ov == null ) {
               v = ZERO.matrix.get(0);
            } else {
               if ( ov.size() > cols ) {
                  throw new RuntimeException("size v > cols " + cols + " < " + ov);
               }
               v = new ArrayList<C>( cols );
               v.addAll( ov );
               // pad with zeros if required:
               for ( int j = v.size(); j < cols; j++ ) {
                   v.add( coFac.getZERO() );
               }
            }
            m.add( v );
        }
        return new GenMatrix<C>(this,m);
    }


    /**
     * Random matrix.
     * @param k size of random coefficients.
     */
    public GenMatrix<C> random(int k) {
        return random( k, density, random );
    }


    /**
     * Random matrix.
     * @param k size of random coefficients.
     * @param q density of nozero coefficients.
     */
    public GenMatrix<C> random(int k, float q) {
        return random( k, q, random );
    }


    /**
     * Random matrix.
     * @param k size of random coefficients.
     * @param random is a source for random bits.
     * @return a random element.
     */
    public GenMatrix<C> random(int k, Random random) {
        return random( k, density, random );
    }


    /**
     * Random matrix.
     * @param k size of random coefficients.
     * @param q density of nozero coefficients.
     * @param random is a source for random bits.
     * @return a random element.
     */
    public GenMatrix<C> random(int k, float q, Random random) {
        ArrayList<ArrayList<C>> m = new ArrayList<ArrayList<C>>( rows );
        for ( int i = 0; i < rows; i++ ) {
            ArrayList<C> v = new ArrayList<C>( cols );
            for ( int j = 0; j < cols; j++ ) {
                C e; 
                if ( random.nextFloat() < q ) {
                   e = coFac.random(k);
                } else {
                   e = coFac.getZERO();
                }
                v.add( e );
           }
           m.add( v );
        }
        return new GenMatrix<C>(this,m);
    }


    /**
     * Random upper triangular matrix.
     * @param k size of random coefficients.
     * @param q density of nozero coefficients.
     */
    public GenMatrix<C> randomUpper(int k, float q) {
        return randomUpper( k, q, random );
    }


    /**
     * Random upper triangular matrix.
     * @param k size of random coefficients.
     * @param q density of nozero coefficients.
     * @param random is a source for random bits.
     * @return a random element.
     */
    public GenMatrix<C> randomUpper(int k, float q, Random random) {
        ArrayList<ArrayList<C>> m = new ArrayList<ArrayList<C>>( rows );
        for ( int i = 0; i < rows; i++ ) {
            ArrayList<C> v = new ArrayList<C>( cols );
            for ( int j = 0; j < cols; j++ ) {
                C e = coFac.getZERO(); 
                if ( j >= i ) {
                   if ( random.nextFloat() < q ) {
                      e = coFac.random(k);
                   } 
                } 
                v.add( e );
           }
           m.add( v );
        }
        return new GenMatrix<C>(this,m);
    }


    /**
     * Random lower triangular matrix.
     * @param k size of random coefficients.
     * @param q density of nozero coefficients.
     */
    public GenMatrix<C> randomLower(int k, float q) {
        return randomLower( k, q, random );
    }


    /**
     * Random lower triangular matrix.
     * @param k size of random coefficients.
     * @param q density of nozero coefficients.
     * @param random is a source for random bits.
     * @return a random element.
     */
    public GenMatrix<C> randomLower(int k, float q, Random random) {
        ArrayList<ArrayList<C>> m = new ArrayList<ArrayList<C>>( rows );
        for ( int i = 0; i < rows; i++ ) {
            ArrayList<C> v = new ArrayList<C>( cols );
            for ( int j = 0; j < cols; j++ ) {
                C e = coFac.getZERO(); 
                if ( j <= i ) {
                   if ( random.nextFloat() < q ) {
                      e = coFac.random(k);
                   } 
                } 
                v.add( e );
           }
           m.add( v );
        }
        return new GenMatrix<C>(this,m);
    }


    /**
     * copy matrix.
     */
    public GenMatrix<C> copy(GenMatrix<C> c) {
        if ( c == null ) {
           return c;
        } else {
           return c.clone();
        }
        //return new GenMatrix<C>( this, c.val );//clone val
    }


    /**
     * parse a matrix from a String.
     * Syntax: [ [ c, ..., c ], ..., [ c, ..., c ] ]
     */
    public GenMatrix<C> parse(String s) {
        int i = s.indexOf("[");
        if ( i >= 0 ) {
           s = s.substring(i+1);
        }
        ArrayList<ArrayList<C>> mat = new ArrayList<ArrayList<C>>( rows );
        ArrayList<C> v;
        GenVector<C> vec;
        GenVectorModul<C> vmod = new GenVectorModul<C>( coFac, cols );
        String e;
        int j;
        do {
           i = s.indexOf("]");     // delimit vector
           j = s.lastIndexOf("]"); // delimit matrix
           if ( i != j ) {
              if ( i >= 0 ) {
                 e = s.substring(0,i);
                 s = s.substring(i);
                 vec = vmod.parse( e );
                 v = (ArrayList<C>)vec.val;
                 mat.add( v );
                 i = s.indexOf(",");
                 if ( i >= 0 ) {
                    s = s.substring(i+1);
                 }
              }
           } else { // matrix delimiter
              if ( i >= 0 ) {
                 e = s.substring(0,i);
                 if ( e.trim().length() > 0 ) {
                    throw new RuntimeException("Error e not empty " + e);
                 }
                 s = s.substring(i+1);
              }
              break;
           }
        } while ( i >= 0 );
        return new GenMatrix<C>( this, mat );
        //throw new RuntimeException("parse not jet implemented");
        //return ZERO;
    }


    /**
     * parse a matrix from a Reader.
     */
    public GenMatrix<C> parse(Reader r) {
	throw new RuntimeException("not supported.");
    }


}
