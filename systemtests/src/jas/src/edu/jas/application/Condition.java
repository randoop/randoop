/*
 * $Id: Condition.java 1978 2008-08-03 10:40:45Z kredel $
 */

package edu.jas.application;


import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.jas.structure.GcdRingElem;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.ExpVector;


/**
 * Condition. An ideal of polynomials considered to be zero and a list of
 * polynomials considered to be non-zero.
 * @param <C> coefficient type
 * @author Heinz Kredel.
 */
public class Condition<C extends GcdRingElem<C>> implements Serializable {


    private static final Logger logger = Logger.getLogger(Condition.class);


    private final boolean debug = logger.isDebugEnabled();


    /**
     * Colors.
     */
    public static enum Color {
        GREEN, RED, WHITE
    };


    /**
     * Data structure for condition zero.
     */
    public final Ideal<C> zero;


    /**
     * Data structure for condition non-zero.
     */
    public final List<GenPolynomial<C>> nonZero;


    /**
     * Condition constructor. Constructs an empty condition.
     * @param ring polynomial ring factory for coefficients.
     */
    public Condition(GenPolynomialRing<C> ring) {
        this(new Ideal<C>(ring, new ArrayList<GenPolynomial<C>>()));
        if (ring == null) {
            throw new RuntimeException("only for non null rings");
        }
    }


    /**
     * Condition constructor.
     * @param z an ideal of zero polynomials.
     * @param nz a list of non-zero polynomials.
     */
    public Condition(Ideal<C> z, List<GenPolynomial<C>> nz) {
        if (z == null || nz == null) {
            throw new RuntimeException("only for non null condition parts");
        }
        zero = z;
        nonZero = nz;
    }


    /**
     * Condition constructor.
     * @param z an ideal of zero polynomials.
     */
    public Condition(Ideal<C> z) {
        this(z, new ArrayList<GenPolynomial<C>>());
    }


    /**
     * toString.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Condition[ 0 == " + zero.list.list.toString() + ", 0 != " + nonZero
                + " ]";
    }


    /**
     * equals.
     * @param ob an Object.
     * @return true if this is equal to o, else false.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object ob) {
        Condition<C> c = null;
        try {
            c = (Condition<C>) ob;
        } catch (ClassCastException e) {
            return false;
        }
        if (c == null) {
            return false;
        }
        // if ( ! zero.getList().equals( c.zero.getList() ) ) {
        if (!zero.equals(c.zero)) {
            return false;
        }
        for (GenPolynomial<C> p : nonZero) {
            if (!c.nonZero.contains(p)) {
                return false;
            }
        }
        List<GenPolynomial<C>> cnz = c.nonZero;
        for (GenPolynomial<C> p : c.nonZero) {
            if (!nonZero.contains(p)) {
                return false;
            }
        }
        // does not work:
        // if ( ! isNonZero( c.nonZero ) ) {
        // return false;
        // }
        // if ( ! c.isNonZero( nonZero ) ) {
        // return false;
        // }
        return true;
    }


    /**
     * Hash code for this condition.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h;
        h = zero.getList().hashCode();
        h = h << 17;
        h += nonZero.hashCode();
        // h = h << 11;
        // h += pairlist.hashCode();
        return h;
    }


    /**
     * Is empty condition.
     * @return true if this is the empty condition, else false.
     */
    public boolean isEmpty() {
        return (zero.isZERO() && nonZero.size() == 0);
    }


    /**
     * Is contradictory.
     * @return true if this condition is contradictory, else false.
     */
    public boolean isContradictory() {
        return (zero.isONE() || nonZero.contains(zero.getRing().getZERO()));
    }


    /**
     * Extend condition with zero polynomial.
     * @param z a polynomial to be treated as zero.
     * @return new condition.
     */
    public Condition<C> extendZero(GenPolynomial<C> z) {
        z = zero.engine.squarefreePart(z); // leads to errors in nonZero? -no
                                            // more
        Ideal<C> idz = zero.sum(z);
        List<GenPolynomial<C>> list = idz.normalform(nonZero);
        if (list.size() != nonZero.size()) { // contradiction
            if (debug) {
                logger.info("zero    = " + zero.getList());
                logger.info("z       = " + z);
                logger.info("idz     = " + idz.getList());
                logger.info("list    = " + list);
                logger.info("nonZero = " + nonZero);
            }
            return null;
        }
        List<GenPolynomial<C>> L = new ArrayList<GenPolynomial<C>>(list.size());
        for (GenPolynomial<C> h : list) {
            if (h != null && !h.isZERO()) {
                GenPolynomial<C> r = h.monic();
                if (!L.contains(r)) { // may happen after reduction
                    L.add(r);
                }
            }
        }
        Condition<C> nc = new Condition<C>(idz, L);

        List<GenPolynomial<C>> Z;
        Z = new ArrayList<GenPolynomial<C>>(idz.getList().size());
        for (GenPolynomial<C> zi : idz.getList()) {
            if (!nc.isNonZero(zi)) {
                Z.add(zi);
            } else {
                System.out.println("zi != 0 in zero: " + zi);
            }
        }
        if (Z.size() == idz.getList().size()) {
            return nc;
        }
        System.out.println("contradiction(==0):");
        System.out.println("zero = " + zero.getList());
        System.out.println("ZZZZ = " + Z);
        System.out.println("list = " + list);
        Ideal<C> id = new Ideal<C>(zero.getRing(), Z);
        // return new Condition<C>( id, L );
        return null; // contradiction
    }


    /**
     * Extend condition with non-zero polynomial.
     * @param nz a polynomial to be treated as non-zero.
     * @return new condition.
     */
    public Condition<C> extendNonZero(GenPolynomial<C> nz) {
        GenPolynomial<C> n = zero.normalform(nz).monic();
        if (n == null || n.isZERO()) {
            return this;
        }
        GenPolynomial<C> nq = zero.engine.squarefreePart(n);
        if (nq.equals(n)) {
            List<GenPolynomial<C>> list = addNonZero(n);
            return new Condition<C>(zero, list);
        }
        if (debug) {
            logger.info("squarefree...    " + nz);
            logger.info("squarefree... of " + n);
            logger.info("squarefreePart = " + nq);
        }
        GenPolynomial<C> q = n.divide(nq);
        List<GenPolynomial<C>> list = addNonZero(nq);
        Condition<C> nc = new Condition<C>(zero, list);
        list = nc.addNonZero(q);
        nc = new Condition<C>(zero, list);

        List<GenPolynomial<C>> Z;
        Z = new ArrayList<GenPolynomial<C>>(zero.getList().size());
        for (GenPolynomial<C> zi : zero.getList()) {
            if (!nc.isNonZero(zi)) {
                Z.add(zi);
            } else {
                System.out.println("zi != 0 in zero: " + zi);
            }
        }
        if (Z.size() == zero.getList().size()) {
            return nc;
        }
        System.out.println("contradiction(!=0):");
        System.out.println("zero = " + zero.getList());
        System.out.println("ZZZZ = " + Z);
        System.out.println("list = " + list);
        Ideal<C> id = new Ideal<C>(zero.getRing(), Z);
        // return new Condition<C>( id, list );
        return nc; // null not ok
    }


    /**
     * Determine color of polynomial.
     * @param c polynomial to be colored.
     * @return color of c.
     */
    public Color color(GenPolynomial<C> c) {
        GenPolynomial<C> m = zero.normalform(c).monic();
        if (zero.contains(m)) {
            // System.out.println("m in id = " + m);
            return Color.GREEN;
        }
        if (m.isConstant()) {
            // System.out.println("m constant " + m);
            return Color.RED;
        }
        // if ( nonZero.contains( c ) ) {
        if (isNonZero(m)) {
            // System.out.println("m in nonzero " + m);
            return Color.RED;
        }
        // System.out.println("m white " + m);
        return Color.WHITE;
    }


    /**
     * Test if a polynomial is contained in nonZero. NonZero is treated as
     * multiplicative set.
     * @param cc polynomial searched in nonZero.
     * @return true, if c != 0 wrt. this condition, else false
     */
    public boolean isNonZero(GenPolynomial<C> cc) {
        if (cc == null || cc.isZERO()) { // do not look into zero list
            return false;
        }
        if (nonZero == null || nonZero.size() == 0) {
            return false;
        }
        GenPolynomial<C> c = cc;
        for (GenPolynomial<C> n : nonZero) {
            // System.out.println("nonZero n = " + n);
            if (n.isONE()) { // do not use 1
                continue;
            }
            GenPolynomial<C> q;
            GenPolynomial<C> r;
            do {
                GenPolynomial<C>[] qr = c.divideAndRemainder(n);
                q = qr[0];
                r = qr[1];
                // System.out.println("q = " + q + ", r = " + r + ", c = " + c +
                // ", n = " + n);
                if (r != null && !r.isZERO()) {
                    continue;
                }
                if (q != null && q.isConstant()) {
                    return true;
                }
                c = q;
            } while (r.isZERO() && !c.isConstant());
        }
        return false;
    }


    /**
     * Test if a list of polynomials is contained in nonZero. NonZero is treated
     * as multiplicative set.
     * @param L list of polynomials to be searched in nonZero.
     * @return true, if all c in L != 0 wrt. this condition, else false
     */
    public boolean isNonZero(List<GenPolynomial<C>> L) {
        if (L == null || L.size() == 0) {
            return true;
        }
        for (GenPolynomial<C> c : L) {
            if (!isNonZero(c)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Add polynomial to nonZero. NonZero is treated as multiplicative set.
     * @param cc polynomial to be added to nonZero.
     * @return new list of non-zero polynomials.
     */
    public List<GenPolynomial<C>> addNonZero(GenPolynomial<C> cc) {
        if (cc == null || cc.isZERO()) { // do not look into zero list
            return nonZero;
        }
        List<GenPolynomial<C>> list;
        if (nonZero == null) { // cannot happen
            list = new ArrayList<GenPolynomial<C>>();
            list.add(cc);
            return list;
        }
        list = new ArrayList<GenPolynomial<C>>(nonZero);
        GenPolynomial<C> c = cc;
        for (GenPolynomial<C> n : nonZero) {
            if (n.isONE()) { // do not use 1
                continue;
            }
            GenPolynomial<C> q;
            GenPolynomial<C> r;
            do {
                GenPolynomial<C>[] qr = c.divideAndRemainder(n);
                q = qr[0];
                r = qr[1];
                if (r != null && !r.isZERO()) {
                    continue;
                }
                if (q != null && q.isConstant()) {
                    return list;
                }
                c = q;
            } while (r.isZERO() && !c.isConstant());
        }
        if (nonZero.size() == 0) {
            logger.info("added to empty nonzero = " + cc);
        } else {
            logger.info("added to nonzero = " + c);
        }
        list.add(c);
        return list;
    }


    /**
     * Determine polynomial. If this condition does not determine the
     * polynomial, then a run-time exception is thrown.
     * @param A polynomial.
     * @return new determined colored polynomial.
     */
    public ColorPolynomial<C> determine(GenPolynomial<GenPolynomial<C>> A) {
        ColorPolynomial<C> cp = null;
        if (A == null) {
            return cp;
        }
        GenPolynomial<GenPolynomial<C>> zero = A.ring.getZERO();
        GenPolynomial<GenPolynomial<C>> green = zero;
        GenPolynomial<GenPolynomial<C>> red = zero;
        GenPolynomial<GenPolynomial<C>> white = zero;
        if (A.isZERO()) {
            cp = new ColorPolynomial<C>(green, red, white);
            return cp;
        }
        GenPolynomial<GenPolynomial<C>> Ap = A;
        GenPolynomial<GenPolynomial<C>> Bp;
        while (!Ap.isZERO()) {
            Map.Entry<ExpVector, GenPolynomial<C>> m = Ap.leadingMonomial();
            ExpVector e = m.getKey();
            GenPolynomial<C> c = m.getValue();
            Bp = Ap.reductum();
            // System.out.println( "color(" + c + ") = " + color(c) );
            switch (color(c)) {
            case GREEN:
                green = green.sum(c, e);
                Ap = Bp;
                continue;
            case RED:
                red = red.sum(c, e);
                white = Bp;
                return new ColorPolynomial<C>(green, red, white);
                // since break is not possible
            default:
                System.out.println("error cond       = " + this);
                System.out.println("error poly     A = " + A);
                System.out.println("error poly green = " + green);
                System.out.println("error poly    Ap = " + Ap);
                throw new RuntimeException("error, c is white = " + c);
                // is catched in minimalGB
            }
        }
        cp = new ColorPolynomial<C>(green, red, white);
        // System.out.println("determined = " + cp);
        return cp;
    }


    /**
     * Re determine colored polynomial.
     * @param s colored polynomial.
     * @return determined colored polynomial wrt. this.conditions.
     */
    public ColorPolynomial<C> reDetermine(ColorPolynomial<C> s) {
        ColorPolynomial<C> p = determine(s.getEssentialPolynomial());
        // assume green terms stay green wrt. this condition
        GenPolynomial<GenPolynomial<C>> g = s.green.sum( p.green );
        p = new ColorPolynomial<C>(g, p.red, p.white);
        return p;
    }


    /**
     * Determine list of polynomials. If this condition does not determine all
     * polynomials, then a run-time exception is thrown. The returned list does
     * not contain polynomials with all green terms.
     * @param L list of polynomial.
     * @return new determined list of colored polynomials.
     */
    public List<ColorPolynomial<C>> determine(List<GenPolynomial<GenPolynomial<C>>> L) {
        List<ColorPolynomial<C>> cl = null;
        if (L == null) {
            return cl;
        }
        cl = new ArrayList<ColorPolynomial<C>>(L.size());
        for (GenPolynomial<GenPolynomial<C>> A : L) {
            ColorPolynomial<C> c = determine(A);
            if (c != null && !c.isZERO()) {
                cl.add(c);
            }
        }
        return cl;
    }


}
