/*
 * $Id: PolyUtil.java 2219 2008-11-16 13:52:02Z kredel $
 */

package edu.jas.poly;

import java.util.Map;
//import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.jas.structure.RingElem;
import edu.jas.structure.RingFactory;
import edu.jas.structure.UnaryFunctor;

import edu.jas.arith.BigInteger;
import edu.jas.arith.BigRational;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;
import edu.jas.arith.BigComplex;

import edu.jas.util.ListUtil;


/**
 * Polynomial utilities, e.g. 
 * conversion between different representations, evaluation and interpolation.
 * @author Heinz Kredel
 */

public class PolyUtil {


    private static final Logger logger = Logger.getLogger(PolyUtil.class);
    private static boolean debug = logger.isDebugEnabled();


    /**
     * Recursive representation. 
     * Represent as polynomial in i variables with coefficients in n-i variables.
     * Works for arbitrary term orders.
     * @param <C> coefficient type.
     * @param rfac recursive polynomial ring factory.
     * @param A polynomial to be converted.
     * @return Recursive represenations of this in the ring rfac.
     */
    public static <C extends RingElem<C>> 
        GenPolynomial<GenPolynomial<C>> 
        recursive( GenPolynomialRing<GenPolynomial<C>> rfac, 
                   GenPolynomial<C> A ) {

        GenPolynomial<GenPolynomial<C>> B = rfac.getZERO().clone();
        if ( A.isZERO() ) {
           return B;
        }
        int i = rfac.nvar;
        GenPolynomial<C> zero = rfac.getZEROCoefficient();
        Map<ExpVector,GenPolynomial<C>> Bv = B.val; //getMap();
        for ( Map.Entry<ExpVector,C> y: A.getMap().entrySet() ) {
            ExpVector e = y.getKey();
            C a = y.getValue();
            ExpVector f = e.contract(0,i);
            ExpVector g = e.contract(i,e.length()-i);
            GenPolynomial<C> p = Bv.get(f);
            if ( p == null ) {
                p = zero;
            }
            p = p.sum( a, g );
            Bv.put( f, p );
        }
        return B;
    }


    /**
     * Distribute a recursive polynomial to a generic polynomial. 
     * Works for arbitrary term orders.
     * @param <C> coefficient type.
     * @param dfac combined polynomial ring factory of coefficients and this.
     * @param B polynomial to be converted.
     * @return distributed polynomial.
     */
    public static <C extends RingElem<C>>
        GenPolynomial<C> 
        distribute( GenPolynomialRing<C> dfac,
                    GenPolynomial<GenPolynomial<C>> B) {
        GenPolynomial<C> C = dfac.getZERO().clone();
        if ( B.isZERO() ) { 
            return C;
        }
        Map<ExpVector,C> Cm = C.val; //getMap();
        for ( Map.Entry<ExpVector,GenPolynomial<C>> y: B.getMap().entrySet() ) {
            ExpVector e = y.getKey();
            GenPolynomial<C> A = y.getValue();
            for ( Map.Entry<ExpVector,C> x: A.val.entrySet() ) {
                ExpVector f = x.getKey();
                C b = x.getValue();
                ExpVector g = e.combine(f);
                assert ( Cm.get(g) != null );
                //if ( Cm.get(g) != null ) { // todo assert, done
                //   throw new RuntimeException("PolyUtil debug error");
                //}
                Cm.put( g, b );
            }
        }
        return C;
    }


    /**
     * BigInteger from ModInteger coefficients, symmetric. 
     * Represent as polynomial with BigInteger coefficients by 
     * removing the modules and making coefficients symmetric to 0.
     * @param fac result polynomial factory.
     * @param A polynomial with ModInteger coefficients to be converted.
     * @return polynomial with BigInteger coefficients.
     */
    public static GenPolynomial<BigInteger> 
        integerFromModularCoefficients( GenPolynomialRing<BigInteger> fac,
                                        GenPolynomial<ModInteger> A ) {
        return PolyUtil.<ModInteger,BigInteger>map(fac,A, new ModSymToInt() );
    }


    /**
     * BigInteger from ModInteger coefficients, positive. 
     * Represent as polynomial with BigInteger coefficients by 
     * removing the modules.
     * @param fac result polynomial factory.
     * @param A polynomial with ModInteger coefficients to be converted.
     * @return polynomial with BigInteger coefficients.
     */
    public static GenPolynomial<BigInteger> 
        integerFromModularCoefficientsPositive( GenPolynomialRing<BigInteger> fac,
                                                GenPolynomial<ModInteger> A ) {
        return PolyUtil.<ModInteger,BigInteger>map(fac,A, new ModToInt() );
    }


    /**
     * BigInteger from BigRational coefficients. 
     * Represent as polynomial with BigInteger coefficients by 
     * multiplication with the lcm of the numerators of the 
     * BigRational coefficients.
     * @param fac result polynomial factory.
     * @param A polynomial with BigRational coefficients to be converted.
     * @return polynomial with BigInteger coefficients.
     */
    public static GenPolynomial<BigInteger> 
        integerFromRationalCoefficients( GenPolynomialRing<BigInteger> fac,
                                         GenPolynomial<BigRational> A ) {
        if ( A == null || A.isZERO() ) {
           return fac.getZERO();
        }
        java.math.BigInteger c = null;
        int s = 0;
        // lcm of denominators
        for ( BigRational y: A.val.values() ) {
            java.math.BigInteger x = y.denominator();
            // c = lcm(c,x)
            if ( c == null ) {
               c = x; 
               s = x.signum();
            } else {
               java.math.BigInteger d = c.gcd( x );
               c = c.multiply( x.divide( d ) );
            }
        }
        if ( s < 0 ) {
           c = c.negate();
        }
        return PolyUtil.<BigRational,BigInteger>map(fac,A, new RatToInt( c ) );
    }


    /**
     * BigInteger from BigRational coefficients. 
     * Represent as list of polynomials with BigInteger coefficients by 
     * multiplication with the lcm of the numerators of the 
     * BigRational coefficients of each polynomial.
     * @param fac result polynomial factory.
     * @param L list of polynomials with BigRational coefficients to be converted.
     * @return polynomial list with BigInteger coefficients.
     */
    public static List<GenPolynomial<BigInteger>> 
        integerFromRationalCoefficients( GenPolynomialRing<BigInteger> fac,
                                         List<GenPolynomial<BigRational>> L ) {
        return ListUtil.<GenPolynomial<BigRational>,GenPolynomial<BigInteger>>map( 
                                                    L, 
                                                    new RatToIntPoly(fac) );
    }


    /**
     * From BigInteger coefficients. 
     * Represent as polynomial with type C coefficients,
     * e.g. ModInteger or BigRational.
     * @param <C> coefficient type.
     * @param fac result polynomial factory.
     * @param A polynomial with BigInteger coefficients to be converted.
     * @return polynomial with type C coefficients.
     */
    public static <C extends RingElem<C>>
        GenPolynomial<C> 
        fromIntegerCoefficients( GenPolynomialRing<C> fac,
                                 GenPolynomial<BigInteger> A ) {
        return PolyUtil.<BigInteger,C>map(fac,A, new FromInteger<C>(fac.coFac) );
    }


    /**
     * From BigInteger coefficients. 
     * Represent as list of polynomials with type C coefficients,
     * e.g. ModInteger or BigRational.
     * @param <C> coefficient type.
     * @param fac result polynomial factory.
     * @param L list of polynomials with BigInteger coefficients to be converted.
     * @return list of polynomials with type C coefficients.
     */
    public static <C extends RingElem<C>>
        List<GenPolynomial<C>> 
        fromIntegerCoefficients( GenPolynomialRing<C> fac,
                                 List<GenPolynomial<BigInteger>> L ) {
        return ListUtil.<GenPolynomial<BigInteger>,GenPolynomial<C>>map( L, 
                                                   new FromIntegerPoly<C>(fac) );
    }


    /**
     * Real part. 
     * @param fac result polynomial factory.
     * @param A polynomial with BigComplex coefficients to be converted.
     * @return polynomial with real part of the coefficients.
     */
    public static GenPolynomial<BigRational> 
        realPart( GenPolynomialRing<BigRational> fac,
                  GenPolynomial<BigComplex> A ) {
        return PolyUtil.<BigComplex,BigRational>map(fac,A, new RealPart() );
    }


    /**
     * Imaginary part. 
     * @param fac result polynomial factory.
     * @param A polynomial with BigComplex coefficients to be converted.
     * @return polynomial with imaginary part of coefficients.
     */
    public static GenPolynomial<BigRational> 
        imaginaryPart( GenPolynomialRing<BigRational> fac,
                       GenPolynomial<BigComplex> A ) {
        return PolyUtil.<BigComplex,BigRational>map(fac,A, new ImagPart() );
    }


    /**
     * Complex from rational real part. 
     * @param fac result polynomial factory.
     * @param A polynomial with BigRational coefficients to be converted.
     * @return polynomial with BigComplex coefficients.
     */
    public static GenPolynomial<BigComplex> 
        complexFromRational( GenPolynomialRing<BigComplex> fac,
                             GenPolynomial<BigRational> A ) {
        return PolyUtil.<BigRational,BigComplex>map(fac,A, new RatToCompl() );
    }


    /** ModInteger chinese remainder algorithm on coefficients.
     * @param fac GenPolynomial<ModInteger> result factory 
     * with A.coFac.modul*B.coFac.modul = C.coFac.modul.
     * @param A GenPolynomial<ModInteger>.
     * @param B other GenPolynomial<ModInteger>.
     * @param mi inverse of A.coFac.modul in ring B.coFac.
     * @return S = cra(A,B), with S mod A.coFac.modul == A 
     *                       and S mod B.coFac.modul == B. 
     */
    public static //<C extends RingElem<C>>
        GenPolynomial<ModInteger> 
        chineseRemainder( GenPolynomialRing<ModInteger> fac,
                          GenPolynomial<ModInteger> A,
                          ModInteger mi,
                          GenPolynomial<ModInteger> B ) {
        ModIntegerRing cfac = (ModIntegerRing)(Object)fac.coFac; // get RingFactory
        GenPolynomial<ModInteger> S = fac.getZERO().clone(); 
        GenPolynomial<ModInteger> Ap = A.clone(); 
        SortedMap<ExpVector,ModInteger> av = Ap.val; //getMap();
        SortedMap<ExpVector,ModInteger> bv = B.getMap();
        SortedMap<ExpVector,ModInteger> sv = S.val; //getMap();
        ModInteger c = null;
        for ( ExpVector e : bv.keySet() ) {
            ModInteger x = av.get( e );
            ModInteger y = bv.get( e ); // assert y != null
            if ( x != null ) {
               av.remove( e );
               c = cfac.chineseRemainder(x,mi,y);
               if ( ! c.isZERO() ) { // 0 cannot happen
                   sv.put( e, c );
               }
            } else {
               //c = cfac.fromInteger( y.getVal() );
               c = cfac.chineseRemainder(A.ring.coFac.getZERO(),mi,y);
               if ( ! c.isZERO() ) { // 0 cannot happen
                  sv.put( e, c ); // c != null
               }
            }
        }
        // assert bv is empty = done
        for ( ExpVector e : av.keySet() ) { // rest of av
            ModInteger x = av.get( e ); // assert x != null
            //c = cfac.fromInteger( x.getVal() );
            c = cfac.chineseRemainder(x,mi,B.ring.coFac.getZERO());
            if ( ! c.isZERO() ) { // 0 cannot happen
               sv.put( e, c ); // c != null
            }
        }
        return S;
    }


    /**
     * GenPolynomial monic, i.e. leadingBaseCoefficient == 1.
     * If leadingBaseCoefficient is not invertible returns this unmodified.
     * @param <C> coefficient type.
     * @param p recursive GenPolynomial<GenPolynomial<C>>.
     * @return monic(p).
     */
    public static <C extends RingElem<C>>
           GenPolynomial<GenPolynomial<C>> 
           monic( GenPolynomial<GenPolynomial<C>> p ) {
        if ( p == null || p.isZERO() ) {
            return p;
        }
        C lc = p.leadingBaseCoefficient().leadingBaseCoefficient();
        if ( !lc.isUnit() ) {
           return p;
        }
        C lm = lc.inverse();
        GenPolynomial<C> L = p.ring.coFac.getONE();
        L = L.multiply(lm);
        return p.multiply(L);
    }


    /**
     * Polynomial list monic. 
     * @param <C> coefficient type.
     * @param L list of polynomials with field coefficients.
     * @return list of polynomials with leading coefficient 1.
     */
    public static <C extends RingElem<C>>
        List<GenPolynomial<C>> monic( List<GenPolynomial<C>> L ) {
        return ListUtil.<GenPolynomial<C>,GenPolynomial<C>>map( L, 
                        new UnaryFunctor<GenPolynomial<C>,GenPolynomial<C>>() {
                            public GenPolynomial<C> eval(GenPolynomial<C> c) {
                                if ( c == null ) {
                                     return null;
                                } else {
                                     return c.monic();
                                }
                            }
                        }
                                                              );
    }


    /**
     * GenPolynomial coefficient wise remainder.
     * @param <C> coefficient type.
     * @param P GenPolynomial.
     * @param s nonzero coefficient.
     * @return coefficient wise remainder.
     * @see edu.jas.poly.GenPolynomial#remainder(edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>>
           GenPolynomial<C> baseRemainderPoly( GenPolynomial<C> P, 
                                               C s ) {
        if ( s == null || s.isZERO() ) {
            throw new RuntimeException(P.getClass().getName()
                                       + " division by zero");
        }
        GenPolynomial<C> h = P.ring.getZERO().clone();
        Map<ExpVector,C> hm = h.val; //getMap();
        for ( Map.Entry<ExpVector,C> m : P.getMap().entrySet() ) {
            ExpVector f = m.getKey(); 
            C a = m.getValue(); 
            C x = a.remainder(s);
            hm.put(f,x);
        }
        return h;
    }


    /**
     * GenPolynomial sparse pseudo remainder.
     * For univariate polynomials.
     * @param <C> coefficient type.
     * @param P GenPolynomial.
     * @param S nonzero GenPolynomial.
     * @return remainder with ldcf(S)<sup>m'</sup> P = quotient * S + remainder.
     * @see edu.jas.poly.GenPolynomial#remainder(edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>>
           GenPolynomial<C> basePseudoRemainder( GenPolynomial<C> P, 
                                                 GenPolynomial<C> S ) {
        if ( S == null || S.isZERO() ) {
            throw new RuntimeException(P.getClass().getName()
                                       + " division by zero");
        }
        if ( P.isZERO() ) {
            return P;
        }
        if ( S.isONE() ) {
            return P.ring.getZERO();
        }
        C c = S.leadingBaseCoefficient();
        ExpVector e = S.leadingExpVector();
        GenPolynomial<C> h;
        GenPolynomial<C> r = P; 
        while ( ! r.isZERO() ) {
            ExpVector f = r.leadingExpVector();
            if ( f.multipleOf(e) ) {
                C a = r.leadingBaseCoefficient();
                f =  f.subtract( e );
                C x = a.remainder(c);
                if ( x.isZERO() ) {
                   C y = a.divide(c);
                   h = S.multiply( y, f ); // coeff a
                } else {
                   r = r.multiply( c );    // coeff ac
                   h = S.multiply( a, f ); // coeff ac
                }
                r = r.subtract( h );
            } else {
                break;
            }
        }
        return r;
    }


    /**
     * GenPolynomial pseudo divide.
     * For univariate polynomials or exact division.
     * @param <C> coefficient type.
     * @param P GenPolynomial.
     * @param S nonzero GenPolynomial.
     * @return quotient with ldcf(S)<sup>m'</sup> P = quotient * S + remainder.
     * @see edu.jas.poly.GenPolynomial#divide(edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>>
           GenPolynomial<C> basePseudoDivide( GenPolynomial<C> P, 
                                              GenPolynomial<C> S ) {
        if ( S == null || S.isZERO() ) {
            throw new RuntimeException(P.getClass().getName()
                                       + " division by zero");
        }
        if ( S.ring.nvar != 1 ) {
           // ok if exact division
           // throw new RuntimeException(this.getClass().getName()
           //                            + " univariate polynomials only");
        }
        if ( P.isZERO() || S.isONE() ) {
            return P;
        }
        C c = S.leadingBaseCoefficient();
        ExpVector e = S.leadingExpVector();
        GenPolynomial<C> h;
        GenPolynomial<C> r = P;
        GenPolynomial<C> q = S.ring.getZERO().clone();

        while ( ! r.isZERO() ) {
            ExpVector f = r.leadingExpVector();
            if ( f.multipleOf(e) ) {
                C a = r.leadingBaseCoefficient();
                f =  f.subtract( e );
                C x = a.remainder(c);
                if ( x.isZERO() ) {
                   C y = a.divide(c);
                   q = q.sum( y, f );
                   h = S.multiply( y, f ); // coeff a
                } else {
                   q = q.sum( a, f );
                   q = q.multiply( c );
                   r = r.multiply( c );    // coeff ac
                   h = S.multiply( a, f ); // coeff ac
                }
                r = r.subtract( h );
            } else {
                break;
            }
        }
        return q;
    }


    /**
     * GenPolynomial pseudo divide.
     * For recursive polynomials.
     * Division by coefficient ring element.
     * @param <C> coefficient type.
     * @param P recursive GenPolynomial.
     * @param s GenPolynomial.
     * @return this/s.
     */
    public static <C extends RingElem<C>>
           GenPolynomial<GenPolynomial<C>> 
           recursiveDivide( GenPolynomial<GenPolynomial<C>> P, 
                            GenPolynomial<C> s ) {
        if ( s == null || s.isZERO() ) {
            throw new RuntimeException(P.getClass().getName()
                                       + " division by zero " + P + ", " + s);
        }
        if ( P.isZERO() ) {
            return P;
        }
        if ( s.isONE() ) {
            return P;
        }
        GenPolynomial<GenPolynomial<C>> p = P.ring.getZERO().clone(); 
        SortedMap<ExpVector,GenPolynomial<C>> pv = p.val; //getMap();
        for ( Map.Entry<ExpVector,GenPolynomial<C>> m1 : P.getMap().entrySet() ) {
            GenPolynomial<C> c1 = m1.getValue();
            ExpVector e1 = m1.getKey();
            GenPolynomial<C> c = PolyUtil.<C>basePseudoDivide(c1,s);
            if ( !c.isZERO() ) {
               pv.put( e1, c ); // or m1.setValue( c )
            } else {
               System.out.println("pu, c1 = " + c1);
               System.out.println("pu, s  = " + s);
               System.out.println("pu, c  = " + c);
               throw new RuntimeException("something is wrong");
            }
        }
        return p;
    }


    /**
     * GenPolynomial sparse pseudo remainder.
     * For recursive polynomials.
     * @param <C> coefficient type.
     * @param P recursive GenPolynomial.
     * @param S nonzero recursive GenPolynomial.
     * @return remainder with ldcf(S)<sup>m'</sup> P = quotient * S + remainder.
     * @see edu.jas.poly.GenPolynomial#remainder(edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>>
           GenPolynomial<GenPolynomial<C>> 
           recursivePseudoRemainder( GenPolynomial<GenPolynomial<C>> P, 
                                     GenPolynomial<GenPolynomial<C>> S) {
        if ( S == null || S.isZERO() ) {
            throw new RuntimeException(P.getClass().getName()
                                       + " division by zero");
        }
        if ( P == null || P.isZERO() ) {
            return P;
        }
        if ( S.isONE() ) {
            return P.ring.getZERO();
        }
        GenPolynomial<C> c = S.leadingBaseCoefficient();
        ExpVector e = S.leadingExpVector();
        GenPolynomial<GenPolynomial<C>> h;
        GenPolynomial<GenPolynomial<C>> r = P; 
        while ( ! r.isZERO() ) {
            ExpVector f = r.leadingExpVector();
            if ( f.multipleOf(e) ) {
                GenPolynomial<C> a = r.leadingBaseCoefficient();
                f =  f.subtract( e );
                GenPolynomial<C> x = c; //test basePseudoRemainder(a,c);
                if ( x.isZERO() ) {
                   GenPolynomial<C> y = PolyUtil.<C>basePseudoDivide(a,c);
                   h = S.multiply( y, f ); // coeff a
                } else {
                   r = r.multiply( c );    // coeff ac
                   h = S.multiply( a, f ); // coeff ac
                }
                r = r.subtract( h );
            } else {
                break;
            }
        }
        return r;
    }


    /**
     * GenPolynomial pseudo divide.
     * For recursive polynomials.
     * @param <C> coefficient type.
     * @param P recursive GenPolynomial.
     * @param S nonzero recursive GenPolynomial.
     * @return quotient with ldcf(S)<sup>m</sup> P = quotient * S + remainder.
     * @see edu.jas.poly.GenPolynomial#remainder(edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>>
           GenPolynomial<GenPolynomial<C>> 
           recursivePseudoDivide( GenPolynomial<GenPolynomial<C>> P, 
                                  GenPolynomial<GenPolynomial<C>> S) {
        if ( S == null || S.isZERO() ) {
            throw new RuntimeException(P.getClass().getName()
                                       + " division by zero");
        }
        if ( S.ring.nvar != 1 ) {
           // ok if exact division
           // throw new RuntimeException(this.getClass().getName()
           //                            + " univariate polynomials only");
        }
        if ( P == null || P.isZERO() ) {
            return P;
        }
        if ( S.isONE() ) {
            return P;
        }
        GenPolynomial<C> c = S.leadingBaseCoefficient();
        ExpVector e = S.leadingExpVector();
        GenPolynomial<GenPolynomial<C>> h;
        GenPolynomial<GenPolynomial<C>> r = P; 
        GenPolynomial<GenPolynomial<C>> q = S.ring.getZERO().clone();
        while ( ! r.isZERO() ) {
            ExpVector f = r.leadingExpVector();
            if ( f.multipleOf(e) ) {
                GenPolynomial<C> a = r.leadingBaseCoefficient();
                f =  f.subtract( e );
                GenPolynomial<C> x = PolyUtil.<C>basePseudoRemainder(a,c);
                if ( x.isZERO() ) {
                   GenPolynomial<C> y = PolyUtil.<C>basePseudoDivide(a,c);
                   q = q.sum( y, f );
                   h = S.multiply( y, f ); // coeff a
                } else {
                   q = q.sum( a, f );
                   q = q.multiply( c );
                   r = r.multiply( c );    // coeff ac
                   h = S.multiply( a, f ); // coeff ac
                }
                r = r.subtract( h );
            } else {
                break;
            }
        }
        return q;
    }


    /**
     * GenPolynomial polynomial derivative main variable.
     * @param <C> coefficient type.
     * @param P GenPolynomial.
     * @return deriviative(P).
     */
    public static <C extends RingElem<C>>
           GenPolynomial<C> 
           baseDeriviative( GenPolynomial<C> P ) {
        if ( P == null || P.isZERO() ) {
            return P;
        }
        GenPolynomialRing<C> pfac = P.ring;
        if ( pfac.nvar > 1 ) { 
           // baseContent not possible by return type
           throw new RuntimeException(P.getClass().getName()
                     + " only for univariate polynomials");
        }
        RingFactory<C> rf = pfac.coFac;
        GenPolynomial<C> d = pfac.getZERO().clone();
        Map<ExpVector,C> dm = d.val; //getMap();
        for ( Map.Entry<ExpVector,C> m : P.getMap().entrySet() ) {
            ExpVector f = m.getKey();  
            long fl = f.getVal(0);
            if ( fl > 0 ) {
               C cf = rf.fromInteger( fl );
               C a = m.getValue(); 
               C x = a.multiply(cf);
               ExpVector e = ExpVector.create( 1, 0, fl-1L );  
               dm.put(e,x);
            }
        }
        return d; 
    }


    /**
     * GenPolynomial recursive polynomial derivative main variable.
     * @param <C> coefficient type.
     * @param P recursive GenPolynomial.
     * @return deriviative(P).
     */
    public static <C extends RingElem<C>>
           GenPolynomial<GenPolynomial<C>> 
           recursiveDeriviative( GenPolynomial<GenPolynomial<C>> P ) {
        if ( P == null || P.isZERO() ) {
            return P;
        }
        GenPolynomialRing<GenPolynomial<C>> pfac = P.ring;
        if ( pfac.nvar > 1 ) { 
           // baseContent not possible by return type
           throw new RuntimeException(P.getClass().getName()
                     + " only for univariate polynomials");
        }
        GenPolynomialRing<C> pr = (GenPolynomialRing<C>)pfac.coFac;
        RingFactory<C> rf = pr.coFac;
        GenPolynomial<GenPolynomial<C>> d = pfac.getZERO().clone();
        Map<ExpVector,GenPolynomial<C>> dm = d.val; //getMap();
        for ( Map.Entry<ExpVector,GenPolynomial<C>> m : P.getMap().entrySet() ) {
            ExpVector f = m.getKey();  
            long fl = f.getVal(0);
            if ( fl > 0 ) {
               C cf = rf.fromInteger( fl );
               GenPolynomial<C> a = m.getValue(); 
               GenPolynomial<C> x = a.multiply(cf);
               ExpVector e = ExpVector.create( 1, 0, fl-1L );  
               dm.put(e,x);
            }
        }
        return d; 
    }


    /**
     * Factor coefficient bound.
     * See SACIPOL.IPFCB: the product of all maxNorms of potential factors
     * is less than or equal to 2**b times the maxNorm of A.
     * @param e degree vector of a GenPolynomial A.
     * @return 2**b.
     */
    public static BigInteger factorBound(ExpVector e) {
        int n = 0;
        java.math.BigInteger p = java.math.BigInteger.ONE;
        java.math.BigInteger v;
        if ( e == null || e.isZERO() ) {
           return BigInteger.ONE;
        }
        for ( int i = 0; i < e.length(); i++ ) {
            if ( e.getVal(i) > 0 ) {
               n += ( 2*e.getVal(i) - 1 );
               v = new java.math.BigInteger( "" + (e.getVal(i) - 1) );
               p = p.multiply( v );
            }
        }
        n += ( p.bitCount() + 1 ); // log2(p)
        n /= 2;
        v = new java.math.BigInteger( "" + 2 );
        v = v.shiftLeft( n );
        BigInteger N = new BigInteger( v );
        return N;
    }


    /**
     * Evaluate at main variable. 
     * @param <C> coefficient type.
     * @param cfac coefficent polynomial ring factory.
     * @param A polynomial to be evaluated.
     * @param a value to evaluate at.
     * @return A( x_1, ..., x_{n-1}, a ).
     */
    public static <C extends RingElem<C>> 
        GenPolynomial<C> 
        evaluateMain( GenPolynomialRing<C> cfac, 
                      GenPolynomial<GenPolynomial<C>> A,
                      C a ) {
        if ( A == null || A.isZERO() ) {
           return cfac.getZERO();
        }
        if ( A.ring.nvar != 1 ) { // todo assert
           throw new RuntimeException("evaluateMain no univariate polynomial");
        }
        if ( a == null || a.isZERO() ) {
           return A.trailingBaseCoefficient();
        }
        // assert decending exponents, i.e. compatible term order
        Map<ExpVector,GenPolynomial<C>> val = A.getMap();
        GenPolynomial<C> B = null;
        long el1 = -1; // undefined
        long el2 = -1;
        for ( ExpVector e : val.keySet() ) {
            el2 = e.getVal(0);
            if ( B == null /*el1 < 0*/ ) { // first turn
               B = val.get( e );
            } else {
               for ( long i = el2; i < el1; i++ ) {
                   B = B.multiply( a );
               }
               B = B.sum( val.get( e ) );
            }
            el1 = el2;
        }
        for ( long i = 0; i < el2; i++ ) {
            B = B.multiply( a );
        }
        return B;
    }


    /**
     * Evaluate at main variable. 
     * @param <C> coefficient type.
     * @param cfac coefficent ring factory.
     * @param A univariate polynomial to be evaluated.
     * @param a value to evaluate at.
     * @return A( a ).
     */
    public static <C extends RingElem<C>> 
        C 
        evaluateMain( RingFactory<C> cfac, 
                      GenPolynomial<C> A,
                      C a ) {
        if ( A == null || A.isZERO() ) {
           return cfac.getZERO();
        }
        if ( A.ring.nvar != 1 ) { // todo assert
           throw new RuntimeException("evaluateMain no univariate polynomial");
        }
        if ( a == null || a.isZERO() ) {
           return A.trailingBaseCoefficient();
        }
        // assert decreasing exponents, i.e. compatible term order
        Map<ExpVector,C> val = A.getMap();
        C B = null;
        long el1 = -1; // undefined
        long el2 = -1;
        for ( ExpVector e : val.keySet() ) {
            el2 = e.getVal(0);
            if ( B == null /*el1 < 0*/ ) { // first turn
               B = val.get( e );
            } else {
               for ( long i = el2; i < el1; i++ ) {
                   B = B.multiply( a );
               }
               B = B.sum( val.get( e ) );
            }
            el1 = el2;
        }
        for ( long i = 0; i < el2; i++ ) {
            B = B.multiply( a );
        }
        return B;
    }


    /**
     * Evaluate at k-th variable. 
     * @param <C> coefficient type.
     * @param cfac coefficient polynomial ring in k variables 
     *        C[x_1, ..., x_k] factory.
     * @param rfac coefficient polynomial ring 
     *        C[x_1, ..., x_{k-1}] [x_k] factory,
     *        a recursive polynomial ring in 1 variable with 
     *        coefficients in k-1 variables.
     * @param nfac polynomial ring in n-1 varaibles
     *        C[x_1, ..., x_{k-1}] [x_{k+1}, ..., x_n] factory,
     *        a recursive polynomial ring in n-k+1 variables with 
     *        coefficients in k-1 variables.
     * @param dfac polynomial ring in n-1 variables.
     *        C[x_1, ..., x_{k-1}, x_{k+1}, ..., x_n] factory.
     * @param A polynomial to be evaluated.
     * @param a value to evaluate at.
     * @return A( x_1, ..., x_{k-1}, a, x_{k+1}, ..., x_n).
     */
    public static <C extends RingElem<C>> 
        GenPolynomial<C>
        evaluate( GenPolynomialRing<C> cfac,
                  GenPolynomialRing<GenPolynomial<C>> rfac, 
                  GenPolynomialRing<GenPolynomial<C>> nfac, 
                  GenPolynomialRing<C> dfac,
                  GenPolynomial<C> A,
                  C a ) {
        if ( rfac.nvar != 1 ) { // todo assert
           throw new RuntimeException("evaluate coefficient ring not univariate");
        }
        if ( A == null || A.isZERO() ) {
           return cfac.getZERO();
        }
        Map<ExpVector,GenPolynomial<C>> Ap = A.contract(cfac);
        GenPolynomialRing<C> rcf = (GenPolynomialRing<C>)rfac.coFac;
        GenPolynomial<GenPolynomial<C>> Ev = nfac.getZERO().clone();
        Map<ExpVector,GenPolynomial<C>> Evm = Ev.val; //getMap();
        for ( Map.Entry<ExpVector,GenPolynomial<C>> m : Ap.entrySet() ) {
            ExpVector e = m.getKey();
            GenPolynomial<C> b = m.getValue();
            GenPolynomial<GenPolynomial<C>> c = recursive( rfac, b );
            GenPolynomial<C> d = evaluateMain(rcf,c,a);
            if ( d != null && !d.isZERO() ) {
               Evm.put(e,d);
            }
        }
        GenPolynomial<C> B = distribute(dfac,Ev);
        return B;
    }


    /**
     * Evaluate at first (lowest) variable. 
     * @param <C> coefficient type.
     * @param cfac coefficient polynomial ring in first variable 
     *        C[x_1] factory.
     * @param dfac polynomial ring in n-1 variables.
     *        C[x_2, ..., x_n] factory.
     * @param A polynomial to be evaluated.
     * @param a value to evaluate at.
     * @return A( a, x_2, ..., x_n).
     */
    public static <C extends RingElem<C>> 
        GenPolynomial<C>
        evaluateFirst( GenPolynomialRing<C> cfac,
                       GenPolynomialRing<C> dfac,
                       GenPolynomial<C> A,
                       C a ) {
        if ( A == null || A.isZERO() ) {
           return dfac.getZERO();
        }
        Map<ExpVector,GenPolynomial<C>> Ap = A.contract(cfac);
        //RingFactory<C> rcf = cfac.coFac; // == dfac.coFac

        GenPolynomial<C> B = dfac.getZERO().clone();
        Map<ExpVector,C> Bm = B.val; //getMap();

        for ( Map.Entry<ExpVector,GenPolynomial<C>> m : Ap.entrySet() ) {
            ExpVector e = m.getKey();
            GenPolynomial<C> b = m.getValue();
            C d = evaluateMain(cfac.coFac,b,a);
            if ( d != null && !d.isZERO() ) {
               Bm.put(e,d);
            }
        }
        return B;
    }


    /**
     * Evaluate at first (lowest) variable. 
     * @param <C> coefficient type.
     * Could also be called evaluateFirst(), but type erasure of A parameter
     * does not allow same name.
     * @param cfac coefficient polynomial ring in first variable 
     *        C[x_1] factory.
     * @param dfac polynomial ring in n-1 variables.
     *        C[x_2, ..., x_n] factory.
     * @param A recursive polynomial to be evaluated.
     * @param a value to evaluate at.
     * @return A( a, x_2, ..., x_n).
     */
    public static <C extends RingElem<C>> 
        GenPolynomial<C>
        evaluateFirstRec( GenPolynomialRing<C> cfac,
                          GenPolynomialRing<C> dfac,
                          GenPolynomial<GenPolynomial<C>> A,
                          C a ) {
        if ( A == null || A.isZERO() ) {
           return dfac.getZERO();
        }
        Map<ExpVector,GenPolynomial<C>> Ap = A.getMap();
        GenPolynomial<C> B = dfac.getZERO().clone();
        Map<ExpVector,C> Bm = B.val; //getMap();
        for ( Map.Entry<ExpVector,GenPolynomial<C>> m : Ap.entrySet() ) {
            ExpVector e = m.getKey();
            GenPolynomial<C> b = m.getValue();
            C d = evaluateMain(cfac.coFac,b,a);
            if ( d != null && !d.isZERO() ) {
               Bm.put(e,d);
            }
        }
        return B;
    }


    /** ModInteger interpolate on first variable.
     * @param <C> coefficient type.
     * @param fac GenPolynomial<C> result factory.
     * @param A GenPolynomial<C>.
     * @param M GenPolynomial<C> interpolation modul of A.
     * @param mi inverse of M(am) in ring fac.coFac.
     * @param B evaluation of other GenPolynomial<C>.
     * @param am evaluation point (interpolation modul) of B, i.e. P(am) = B.
     * @return S, with S mod M == A and S(am) == B.
     */
    public static <C extends RingElem<C>>
        GenPolynomial<GenPolynomial<C>> 
        interpolate( GenPolynomialRing<GenPolynomial<C>> fac,
                     GenPolynomial<GenPolynomial<C>> A,
                     GenPolynomial<C> M,
                     C mi,
                     GenPolynomial<C> B, 
                     C am ) {
        GenPolynomial<GenPolynomial<C>> S = fac.getZERO().clone(); 
        GenPolynomial<GenPolynomial<C>> Ap = A.clone(); 
        SortedMap<ExpVector,GenPolynomial<C>> av = Ap.val; //getMap();
        SortedMap<ExpVector,C> bv = B.getMap();
        SortedMap<ExpVector,GenPolynomial<C>> sv = S.val; //getMap();
        GenPolynomialRing<C> cfac = (GenPolynomialRing<C>)fac.coFac; 
        RingFactory<C> bfac = cfac.coFac; 
        GenPolynomial<C> c = null;
        for ( ExpVector e : bv.keySet() ) {
            GenPolynomial<C> x = av.get( e );
            C y = bv.get( e ); // assert y != null
            if ( x != null ) {
               av.remove( e );
               c = PolyUtil.<C>interpolate(cfac,x,M,mi,y,am);
               if ( ! c.isZERO() ) { // 0 cannot happen
                   sv.put( e, c );
               }
            } else {
               c = PolyUtil.<C>interpolate(cfac,cfac.getZERO(),M,mi,y,am);
               if ( ! c.isZERO() ) { // 0 cannot happen
                  sv.put( e, c ); // c != null
               }
            }
        }
        // assert bv is empty = done
        for ( ExpVector e : av.keySet() ) { // rest of av
            GenPolynomial<C> x = av.get( e ); // assert x != null
            c = PolyUtil.<C>interpolate(cfac,x,M,mi,bfac.getZERO(),am);
            if ( ! c.isZERO() ) { // 0 cannot happen
               sv.put( e, c ); // c != null
            }
        }
        return S;
    }


    /** Univariate polynomial interpolation.
     * @param <C> coefficient type.
     * @param fac GenPolynomial<C> result factory.
     * @param A GenPolynomial<C>.
     * @param M GenPolynomial<C> interpolation modul of A.
     * @param mi inverse of M(am) in ring fac.coFac.
     * @param a evaluation of other GenPolynomial<C>.
     * @param am evaluation point (interpolation modul) of a, i.e. P(am) = a.
     * @return S, with S mod M == A and S(am) == a.
     */
    public static <C extends RingElem<C>>
        GenPolynomial<C> 
        interpolate( GenPolynomialRing<C> fac,
                     GenPolynomial<C> A,
                     GenPolynomial<C> M,
                     C mi,
                     C a, 
                     C am ) {
        GenPolynomial<C> s; 
        C b = PolyUtil.<C>evaluateMain( fac.coFac, A, am ); 
                              // A mod a.modul
        C d = a.subtract( b ); // a-A mod a.modul
        if ( d.isZERO() ) {
           return A;
        }
        b = d.multiply( mi ); // b = (a-A)*mi mod a.modul
        // (M*b)+A mod M = A mod M = 
        // (M*mi*(a-A)+A) mod a.modul = a mod a.modul
        s = M.multiply( b );
        s = s.sum( A );
        return s;
    }


    /**
     * Maximal degree in the coefficient polynomials.
     * @param <C> coefficient type.
     * @return maximal degree in the coefficients.
     */
    public static <C extends RingElem<C>>
           long 
           coeffMaxDegree(GenPolynomial<GenPolynomial<C>> A) {
        if ( A.isZERO() ) {
           return 0; // 0 or -1 ?;
        }
        long deg = 0;
        for ( GenPolynomial<C> a : A.getMap().values() ) {
            long d = a.degree();
            if ( d > deg ) {
               deg = d;
            }
        }
        return deg;
    }


    /**
     * Map a unary function to the coefficients.
     * @param ring result polynomial ring factory.
     * @param p polynomial.
     * @param f evaluation functor.
     * @return new polynomial with coefficients f(p(e)).
     */
    public static <C extends RingElem<C>, D extends RingElem<D>>
           GenPolynomial<D> map(GenPolynomialRing<D> ring,
                                GenPolynomial<C> p,
                                UnaryFunctor<C,D> f) {
        GenPolynomial<D> n = ring.getZERO().clone(); 
        SortedMap<ExpVector,D> nv = n.val;
        for ( Monomial<C> m : p ) {
            D c = f.eval( m.c );
            if ( c != null && !c.isZERO() ) {
                nv.put( m.e, c );
            }
        }
        return n;
    }

}


/**
 * BigRational numerator functor.
 */
class RatNumer implements UnaryFunctor<BigRational,BigInteger> {
    public BigInteger eval(BigRational c) {
        if ( c == null ) {
            return new BigInteger();
        } else {
            return new BigInteger( c.numerator() );
        }
    }
}


/**
 * Conversion of symmetric ModInteger to BigInteger functor.
 */
class ModSymToInt implements UnaryFunctor<ModInteger,BigInteger> {
    public BigInteger eval(ModInteger c) {
        if ( c == null ) {
            return new BigInteger();
        } else {
            return new BigInteger( c.getSymmetricVal() );
        }
    }
}


/**
 * Conversion of ModInteger to BigInteger functor.
 */
class ModToInt implements UnaryFunctor<ModInteger,BigInteger> {
    public BigInteger eval(ModInteger c) {
        if ( c == null ) {
            return new BigInteger();
        } else {
            return new BigInteger( c.getVal() );
        }
    }
}


/**
 * Conversion of BigRational to BigInteger with division by lcm functor.
 * result = num*(lcm/denom).
 */
class RatToInt implements UnaryFunctor<BigRational,BigInteger> {
    java.math.BigInteger lcm;
    public RatToInt(java.math.BigInteger lcm) {
        this.lcm = lcm; //.getVal();
    }
    public BigInteger eval(BigRational c) {
        if ( c == null ) {
            return new BigInteger();
        } else {
            // p = num*(lcm/denom)
            java.math.BigInteger b = lcm.divide( c.denominator() );
            return new BigInteger( c.numerator().multiply( b ) );
        }
    }
}


/**
 * Conversion from BigInteger functor.
 */
class FromInteger<D extends RingElem<D>> 
      implements UnaryFunctor<BigInteger,D> {
    RingFactory<D> ring;
    public FromInteger(RingFactory<D> ring) {
        this.ring = ring;
    }
    public D eval(BigInteger c) {
        if ( c == null ) {
            return ring.getZERO();
        } else {
            return ring.fromInteger( c.getVal() );
        }
    }
}


/**
 * Conversion from GenPolynomial<BigInteger> functor.
 */
class FromIntegerPoly<D extends RingElem<D>> 
      implements UnaryFunctor<GenPolynomial<BigInteger>,GenPolynomial<D>> {

    GenPolynomialRing<D> ring;
    FromInteger<D> fi;


    public FromIntegerPoly(GenPolynomialRing<D> ring) {
        if ( ring == null ) {
            throw new IllegalArgumentException("ring must not be null");
        }
        this.ring = ring;
        fi = new FromInteger<D>(ring.coFac);
    }


    public GenPolynomial<D> eval(GenPolynomial<BigInteger> c) {
        if ( c == null ) {
            return ring.getZERO();
        } else {
            return PolyUtil.<BigInteger,D>map( ring, c, fi );
        }
    }
}


/**
 * Conversion from GenPolynomial<BigRational> to GenPolynomial<BigInteger> functor.
 */
class RatToIntPoly
    implements UnaryFunctor<GenPolynomial<BigRational>,GenPolynomial<BigInteger>> {

    GenPolynomialRing<BigInteger> ring;

    public RatToIntPoly(GenPolynomialRing<BigInteger> ring) {
        if ( ring == null ) {
            throw new IllegalArgumentException("ring must not be null");
        }
        this.ring = ring;
    }

    public GenPolynomial<BigInteger> eval(GenPolynomial<BigRational> c) {
        if ( c == null ) {
            return ring.getZERO();
        } else {
            return PolyUtil.integerFromRationalCoefficients( ring, c );
        }
    }
}


/**
 * Real part functor.
 */
class RealPart implements UnaryFunctor<BigComplex,BigRational> {
    public BigRational eval(BigComplex c) {
        if ( c == null ) {
            return new BigRational();
        } else {
            return c.getRe();
        }
    }
}


/**
 * Imaginary part functor.
 */
class ImagPart implements UnaryFunctor<BigComplex,BigRational> {
    public BigRational eval(BigComplex c) {
        if ( c == null ) {
            return new BigRational();
        } else {
            return c.getIm();
        }
    }
}


/**
 * Rational to complex functor.
 */
class RatToCompl implements UnaryFunctor<BigRational,BigComplex> {
    public BigComplex eval(BigRational c) {
        if ( c == null ) {
            return new BigComplex();
        } else {
            return new BigComplex( c );
        }
    }
}
