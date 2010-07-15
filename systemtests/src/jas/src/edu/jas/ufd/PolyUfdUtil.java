/*
 * $Id: PolyUfdUtil.java 2185 2008-10-12 17:57:41Z kredel $
 */

package edu.jas.ufd;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.jas.structure.RingElem;
import edu.jas.structure.GcdRingElem;
import edu.jas.structure.RingFactory;

import edu.jas.arith.BigInteger;
import edu.jas.arith.BigRational;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;

import edu.jas.poly.ExpVector;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolyUtil;

import edu.jas.application.Quotient;
import edu.jas.application.QuotientRing;


/**
 * Polynomial ufd utilities, like 
 * conversion between different representations and Hensel lifting.
 * @author Heinz Kredel
 */

public class PolyUfdUtil {


    private static final Logger logger = Logger.getLogger(PolyUfdUtil.class);
    private static boolean debug = logger.isDebugEnabled();


    /**
     * Integral polynomial from rational function coefficients. 
     * Represent as polynomial with integral polynomial coefficients by 
     * multiplication with the lcm of the numerators of the 
     * rational function coefficients.
     * @param fac result polynomial factory.
     * @param A polynomial with rational function coefficients to be converted.
     * @return polynomial with integral polynomial coefficients.
     */
    public static <C extends GcdRingElem<C>> 
        GenPolynomial<GenPolynomial<C>>
        integralFromQuotientCoefficients( GenPolynomialRing<GenPolynomial<C>> fac,
                                          GenPolynomial<Quotient<C>> A ) {
        GenPolynomial<GenPolynomial<C>> B = fac.getZERO().clone();
        if ( A == null || A.isZERO() ) {
           return B;
        }
        GenPolynomial<C> c = null;
        GenPolynomial<C> d;
        GenPolynomial<C> x;
        GreatestCommonDivisor<C> ufd = new GreatestCommonDivisorSubres<C>();
        int s = 0;
        // lcm of denominators
        for ( Quotient<C> y: A.getMap().values() ) {
            x = y.den;
            // c = lcm(c,x)
            if ( c == null ) {
               c = x; 
               s = x.signum();
            } else {
               d = ufd.gcd( c, x );
               c = c.multiply( x.divide( d ) );
            }
        }
        if ( s < 0 ) {
           c = c.negate();
        }
        for ( Map.Entry<ExpVector,Quotient<C>> y: A.getMap().entrySet() ) {
            ExpVector e = y.getKey();
            Quotient<C> a = y.getValue();
            // p = n*(c/d)
            GenPolynomial<C> b = c.divide( a.den );
            GenPolynomial<C> p = a.num.multiply( b );
            //B = B.sum( p, e ); // inefficient
            B.doPutToMap( e, p ); 
        }
        return B;
    }


    /**
     * Integral polynomial from rational function coefficients. 
     * Represent as polynomial with integral polynomial coefficients by 
     * multiplication with the lcm of the numerators of the 
     * rational function coefficients.
     * @param fac result polynomial factory.
     * @param L list of polynomial with rational function coefficients to be converted.
     * @return list of polynomials with integral polynomial coefficients.
     */
    public static <C extends GcdRingElem<C>> 
        List<GenPolynomial<GenPolynomial<C>>>
        integralFromQuotientCoefficients( GenPolynomialRing<GenPolynomial<C>> fac,
                                          Collection<GenPolynomial<Quotient<C>>> L ) {
        if ( L == null ) {
           return null;
        }
        List<GenPolynomial<GenPolynomial<C>>> list 
            = new ArrayList<GenPolynomial<GenPolynomial<C>>>( L.size() );
        for ( GenPolynomial<Quotient<C>> p : L ) {
            list.add( integralFromQuotientCoefficients(fac,p) );
        }
        return list;
    }


    /**
     * Rational function from integral polynomial coefficients. 
     * Represent as polynomial with type Quotient<C> coefficients.
     * @param fac result polynomial factory.
     * @param A polynomial with integral polynomial coefficients to be converted.
     * @return polynomial with type Quotient<C> coefficients.
     */
    public static <C extends GcdRingElem<C>>
        GenPolynomial<Quotient<C>> 
        quotientFromIntegralCoefficients( GenPolynomialRing<Quotient<C>> fac,
                                          GenPolynomial<GenPolynomial<C>> A ) {
        GenPolynomial<Quotient<C>> B = fac.getZERO().clone();
        if ( A == null || A.isZERO() ) {
           return B;
        }
        RingFactory<Quotient<C>> cfac = fac.coFac;
        QuotientRing<C> qfac = (QuotientRing<C>)cfac; 
        for ( Map.Entry<ExpVector,GenPolynomial<C>> y: A.getMap().entrySet() ) {
            ExpVector e = y.getKey();
            GenPolynomial<C> a = y.getValue();
            Quotient<C> p = new Quotient<C>(qfac, a); // can not be zero
            if ( p != null && !p.isZERO() ) {
               //B = B.sum( p, e ); // inefficient
               B.doPutToMap( e, p ); 
            }
        }
        return B;
    }


    /**
     * Rational function from integral polynomial coefficients. 
     * Represent as polynomial with type Quotient<C> coefficients.
     * @param fac result polynomial factory.
     * @param L list of polynomials with integral polynomial 
     *        coefficients to be converted.
     * @return list of polynomials with type Quotient<C> coefficients.
     */
    public static <C extends GcdRingElem<C>>
        List<GenPolynomial<Quotient<C>>> 
        quotientFromIntegralCoefficients( GenPolynomialRing<Quotient<C>> fac,
                                          Collection<GenPolynomial<GenPolynomial<C>>> L ) {
        if ( L == null ) {
           return null;
        }
        List<GenPolynomial<Quotient<C>>> list 
            = new ArrayList<GenPolynomial<Quotient<C>>>( L.size() );
        for ( GenPolynomial<GenPolynomial<C>> p : L ) {
            list.add( quotientFromIntegralCoefficients(fac,p) );
        }
        return list;
    }


    /**
     * From BigInteger coefficients. 
     * Represent as polynomial with type GenPolynomial&lt;C&gt; coefficients,
     * e.g. ModInteger or BigRational.
     * @param fac result polynomial factory.
     * @param A polynomial with GenPolynomial&lt;BigInteger&gt; coefficients to be converted.
     * @return polynomial with type GenPolynomial&lt;C&gt; coefficients.
     */
    public static <C extends RingElem<C>>
        GenPolynomial<GenPolynomial<C>> 
        fromIntegerCoefficients( GenPolynomialRing<GenPolynomial<C>> fac,
                                 GenPolynomial<GenPolynomial<BigInteger>> A ) {
        GenPolynomial<GenPolynomial<C>> B = fac.getZERO().clone();
        if ( A == null || A.isZERO() ) {
           return B;
        }
        RingFactory<GenPolynomial<C>> cfac = fac.coFac;
        GenPolynomialRing<C> rfac = (GenPolynomialRing<C>)cfac;
        for ( Map.Entry<ExpVector,GenPolynomial<BigInteger>> y: A.getMap().entrySet() ) {
            ExpVector e = y.getKey();
            GenPolynomial<BigInteger> a = y.getValue();
            GenPolynomial<C> p = PolyUtil.<C>fromIntegerCoefficients( rfac, a );
            if ( p != null && !p.isZERO() ) {
               //B = B.sum( p, e ); // inefficient
               B.doPutToMap( e, p ); 
            }
        }
        return B;
    }


    /**
     * From BigInteger coefficients. 
     * Represent as polynomial with type GenPolynomial&lt;C&gt; coefficients,
     * e.g. ModInteger or BigRational.
     * @param fac result polynomial factory.
     * @param L polynomial list with GenPolynomial&lt;BigInteger&gt; 
     * coefficients to be converted.
     * @return polynomial list with polynomials with 
     * type GenPolynomial&lt;C&gt; coefficients.
     */
    public static <C extends RingElem<C>>
        List<GenPolynomial<GenPolynomial<C>>> 
        fromIntegerCoefficients( GenPolynomialRing<GenPolynomial<C>> fac,
                                 List<GenPolynomial<GenPolynomial<BigInteger>>> L ) {
        List<GenPolynomial<GenPolynomial<C>>> K = null;
        if ( L == null ) {
           return K;
        }
        K = new ArrayList<GenPolynomial<GenPolynomial<C>>>( L.size() );
        if ( L.size() == 0 ) {
           return K;
        }
        for ( GenPolynomial<GenPolynomial<BigInteger>> a: L ) {
            GenPolynomial<GenPolynomial<C>> b 
               = fromIntegerCoefficients( fac, a );
            K.add( b );
        }
        return K;
    }

    /** ModInteger Hensel lifting algorithm on coefficients.
     * Let p = A.ring.coFac.modul() = B.ring.coFac.modul() 
     * and assume C == A*B mod p with ggt(A,B) == 1 mod p and
     * S A + T B == 1 mod p. 
     * See Algorithm 6.1. in Geddes et.al.. 
     * Linear version, as it does not lift S A + T B == 1 mod p^{e+1}.
     * @param C GenPolynomial<BigInteger>.
     * @param A GenPolynomial<ModInteger>.
     * @param B other GenPolynomial<ModInteger>.
     * @param S GenPolynomial<ModInteger>.
     * @param T GenPolynomial<ModInteger>.
     * @param M bound on the coefficients of A1 and B1 as factors of C.
     * @return [A1,B1] = lift(C,A,B), with C = A1 * B1.
     */
    @SuppressWarnings("unchecked") 
    public static //<C extends RingElem<C>>
        GenPolynomial<BigInteger>[] 
        liftHensel( GenPolynomial<BigInteger> C,
                    BigInteger M,
                    GenPolynomial<ModInteger> A,
                    GenPolynomial<ModInteger> B, 
                    GenPolynomial<ModInteger> S,
                    GenPolynomial<ModInteger> T ) {
        GenPolynomial<BigInteger>[] AB = (GenPolynomial<BigInteger>[])new GenPolynomial[2];
        if ( C == null || C.isZERO() ) {
           AB[0] = C;
           AB[1] = C;
           return AB;
        }
        if ( A == null || A.isZERO() || B == null || B.isZERO() ) {
           throw new RuntimeException("A and B must be nonzero");
        }
        GenPolynomialRing<BigInteger> fac = C.ring;
        if ( fac.nvar != 1 ) { // todo assert
           throw new RuntimeException("polynomial ring not univariate");
        }
        // setup factories
        GenPolynomialRing<ModInteger> pfac = A.ring;
        RingFactory<ModInteger> p = pfac.coFac;
        RingFactory<ModInteger> q = p;
        ModIntegerRing P = (ModIntegerRing)p;
        ModIntegerRing Q = (ModIntegerRing)q;
        BigInteger Qi = new BigInteger( Q.getModul() );
        BigInteger M2 = M.multiply( M.fromInteger(2) );
        BigInteger Mq = Qi;

        // normalize c and a, b factors, assert p is prime
        GenPolynomial<BigInteger> Ai;
        GenPolynomial<BigInteger> Bi;
        BigInteger c = C.leadingBaseCoefficient();
        C = C.multiply(c); // sic
        ModInteger a = A.leadingBaseCoefficient();
        if ( !a.isONE() ) { // A = A.monic();
           A = A.divide( a );
           S = S.multiply( a );
        }
        ModInteger b = B.leadingBaseCoefficient();
        if ( !b.isONE() ) { // B = B.monic();
           B = B.divide( b );
           T = T.multiply( b );
        }
        ModInteger ci = P.fromInteger( c.getVal() );
        A = A.multiply( ci );
        B = B.multiply( ci );
        T = T.divide( ci );
        S = S.divide( ci );
        Ai = PolyUtil.integerFromModularCoefficients( fac, A );
        Bi = PolyUtil.integerFromModularCoefficients( fac, B );
        // replace leading base coefficients
        ExpVector ea = Ai.leadingExpVector();
        ExpVector eb = Bi.leadingExpVector();
        Ai.doPutToMap(ea,c);
        Bi.doPutToMap(eb,c);

        // polynomials mod p
        GenPolynomial<ModInteger> Ap; 
        GenPolynomial<ModInteger> Bp;
        GenPolynomial<ModInteger> A1p; 
        GenPolynomial<ModInteger> B1p;
        GenPolynomial<ModInteger> Ep;

        // polynomials over the integers
        GenPolynomial<BigInteger> E;
        GenPolynomial<BigInteger> Ea;
        GenPolynomial<BigInteger> Eb;
        GenPolynomial<BigInteger> Ea1;
        GenPolynomial<BigInteger> Eb1;

        while ( Mq.compareTo( M2 ) < 0 ) {
            // compute E=(C-AB)/q over the integers
            E = C.subtract( Ai.multiply(Bi) );
            if ( E.isZERO() ) {
               //System.out.println("leaving on zero error");
               logger.info("leaving on zero error");
               break;
            }
            E = E.divide( Qi );
            // E mod p
            Ep = PolyUtil.<ModInteger>fromIntegerCoefficients(pfac,E); 
            //logger.info("Ep = " + Ep);

            // construct approximation mod p
            Ap = S.multiply( Ep ); // S,T ++ T,S
            Bp = T.multiply( Ep );
            GenPolynomial<ModInteger>[] QR;
            QR = Ap.divideAndRemainder( B );
            GenPolynomial<ModInteger> Qp;
            GenPolynomial<ModInteger> Rp;
            Qp = QR[0];
            Rp = QR[1];
            A1p = Rp;
            B1p = Bp.sum( A.multiply( Qp ) );

            // construct q-adic approximation, convert to integer
            Ea = PolyUtil.integerFromModularCoefficients(fac,A1p);
            Eb = PolyUtil.integerFromModularCoefficients(fac,B1p);
            Ea1 = Ea.multiply( Qi );
            Eb1 = Eb.multiply( Qi );

            Ea = Ai.sum( Eb1 ); // Eb1 and Ea1 are required
            Eb = Bi.sum( Ea1 ); //--------------------------
            assert ( Ea.degree(0)+Eb.degree(0) <= C.degree(0) );
            //if ( Ea.degree(0)+Eb.degree(0) > C.degree(0) ) { // debug
            //   throw new RuntimeException("deg(A)+deg(B) > deg(C)");
            //}

            // prepare for next iteration
            Mq = Qi;
            Qi = new BigInteger( Q.getModul().multiply( P.getModul() ) );
            Q = new ModIntegerRing( Qi.getVal() );
            Ai = Ea;
            Bi = Eb;
        }
        GreatestCommonDivisorAbstract<BigInteger> ufd
            = new GreatestCommonDivisorPrimitive<BigInteger>();

        // remove normalization
        BigInteger ai = ufd.baseContent(Ai);
        Ai = Ai.divide( ai );
        BigInteger bi = c.divide(ai);
        Bi = Bi.divide( bi ); // divide( c/a )
        AB[0] = Ai;
        AB[1] = Bi;
        return AB;
    }


    /** ModInteger Hensel lifting algorithm on coefficients.
     * Let p = A.ring.coFac.modul() = B.ring.coFac.modul() 
     * and assume C == A*B mod p with ggt(A,B) == 1 mod p and
     * S A + T B == 1 mod p. 
     * See algorithm 6.1. in Geddes et.al. and algorithms 3.5.{5,6} in Cohen. 
     * Quadratic version, as it also lifts S A + T B == 1 mod p^{e+1}.
     * @param C GenPolynomial<BigInteger>.
     * @param A GenPolynomial<ModInteger>.
     * @param B other GenPolynomial<ModInteger>.
     * @param S GenPolynomial<ModInteger>.
     * @param T GenPolynomial<ModInteger>.
     * @param M bound on the coefficients of A1 and B1 as factors of C.
     * @return [A1,B1] = lift(C,A,B), with C = A1 * B1.
     */
    @SuppressWarnings("unchecked") 
    public static //<C extends RingElem<C>>
        GenPolynomial<BigInteger>[] 
        liftHenselQuadratic( GenPolynomial<BigInteger> C,
                             BigInteger M,
                             GenPolynomial<ModInteger> A,
                             GenPolynomial<ModInteger> B, 
                             GenPolynomial<ModInteger> S,
                             GenPolynomial<ModInteger> T ) {
        GenPolynomial<BigInteger>[] AB = (GenPolynomial<BigInteger>[])new GenPolynomial[2];
        if ( C == null || C.isZERO() ) {
           AB[0] = C;
           AB[1] = C;
           return AB;
        }
        if ( A == null || A.isZERO() || B == null || B.isZERO() ) {
           throw new RuntimeException("A and B must be nonzero");
        }
        GenPolynomialRing<BigInteger> fac = C.ring;
        if ( fac.nvar != 1 ) { // todo assert
           throw new RuntimeException("polynomial ring not univariate");
        }
        // setup factories
        GenPolynomialRing<ModInteger> pfac = A.ring;
        RingFactory<ModInteger> p = pfac.coFac;
        RingFactory<ModInteger> q = p;
        ModIntegerRing P = (ModIntegerRing)p;
        ModIntegerRing Q = (ModIntegerRing)q;
        BigInteger Qi = new BigInteger( Q.getModul() );
        BigInteger M2 = M.multiply( M.fromInteger(2) );
        BigInteger Mq = Qi;
        GenPolynomialRing<ModInteger> qfac;
        qfac = new GenPolynomialRing<ModInteger>(Q,pfac);

        // normalize c and a, b factors, assert p is prime
        GenPolynomial<BigInteger> Ai;
        GenPolynomial<BigInteger> Bi;
        BigInteger c = C.leadingBaseCoefficient();
        C = C.multiply(c); // sic
        ModInteger a = A.leadingBaseCoefficient();
        if ( !a.isONE() ) { // A = A.monic();
           A = A.divide( a );
           S = S.multiply( a );
        }
        ModInteger b = B.leadingBaseCoefficient();
        if ( !b.isONE() ) { // B = B.monic();
           B = B.divide( b );
           T = T.multiply( b );
        }
        ModInteger ci = P.fromInteger( c.getVal() );
        A = A.multiply( ci );
        B = B.multiply( ci );
        T = T.divide( ci );
        S = S.divide( ci );
        Ai = PolyUtil.integerFromModularCoefficients( fac, A );
        Bi = PolyUtil.integerFromModularCoefficients( fac, B );
        // replace leading base coefficients
        ExpVector ea = Ai.leadingExpVector();
        ExpVector eb = Bi.leadingExpVector();
        Ai.doPutToMap(ea,c);
        Bi.doPutToMap(eb,c);

        // polynomials mod p
        GenPolynomial<ModInteger> Ap; 
        GenPolynomial<ModInteger> Bp;
        GenPolynomial<ModInteger> A1p; 
        GenPolynomial<ModInteger> B1p;
        GenPolynomial<ModInteger> Ep;
        GenPolynomial<ModInteger> Sp = S;
        GenPolynomial<ModInteger> Tp = T;

        // polynomials mod q
        GenPolynomial<ModInteger> Aq; 
        GenPolynomial<ModInteger> Bq;
        GenPolynomial<ModInteger> Eq;

        // polynomials over the integers
        GenPolynomial<BigInteger> E;
        GenPolynomial<BigInteger> Ea;
        GenPolynomial<BigInteger> Eb;
        GenPolynomial<BigInteger> Ea1;
        GenPolynomial<BigInteger> Eb1;
        GenPolynomial<BigInteger> Si;
        GenPolynomial<BigInteger> Ti;

        Si = PolyUtil.integerFromModularCoefficients( fac, S );
        Ti = PolyUtil.integerFromModularCoefficients( fac, T );

        Aq = PolyUtil.<ModInteger>fromIntegerCoefficients(qfac,Ai); 
        Bq = PolyUtil.<ModInteger>fromIntegerCoefficients(qfac,Bi); 

        while ( Mq.compareTo( M2 ) < 0 ) {
            // compute E=(C-AB)/q over the integers
            E = C.subtract( Ai.multiply(Bi) );
            if ( E.isZERO() ) {
               //System.out.println("leaving on zero error");
               logger.info("leaving on zero error");
               break;
            }
            E = E.divide( Qi );
            // E mod p
            Ep = PolyUtil.<ModInteger>fromIntegerCoefficients(qfac,E); 
            //logger.info("Ep = " + Ep);

            // construct approximation mod p
            Ap = Sp.multiply( Ep ); // S,T ++ T,S
            Bp = Tp.multiply( Ep );
            GenPolynomial<ModInteger>[] QR;
            QR = Ap.divideAndRemainder( Bq );
            GenPolynomial<ModInteger> Qp;
            GenPolynomial<ModInteger> Rp;
            Qp = QR[0];
            Rp = QR[1];
            A1p = Rp;
            B1p = Bp.sum( Aq.multiply( Qp ) );

            // construct q-adic approximation, convert to integer
            Ea = PolyUtil.integerFromModularCoefficients(fac,A1p);
            Eb = PolyUtil.integerFromModularCoefficients(fac,B1p);
            Ea1 = Ea.multiply( Qi );
            Eb1 = Eb.multiply( Qi );
            Ea = Ai.sum( Eb1 ); // Eb1 and Ea1 are required
            Eb = Bi.sum( Ea1 ); //--------------------------
            assert ( Ea.degree(0)+Eb.degree(0) <= C.degree(0) );
            //if ( Ea.degree(0)+Eb.degree(0) > C.degree(0) ) { // debug
            //   throw new RuntimeException("deg(A)+deg(B) > deg(C)");
            //}
            Ai = Ea;
            Bi = Eb;

            // gcd representation factors error --------------------------------
            // compute E=(1-SA-TB)/q over the integers
            E = fac.getONE();
            E = E.subtract( Si.multiply(Ai) ).subtract( Ti.multiply(Bi) );
            E = E.divide( Qi );
            // E mod q
            Ep = PolyUtil.<ModInteger>fromIntegerCoefficients(qfac,E); 
            //logger.info("Ep2 = " + Ep);

            // construct approximation mod q
            Ap = Sp.multiply( Ep ); // S,T ++ T,S
            Bp = Tp.multiply( Ep );
            QR = Bp.divideAndRemainder( Aq ); // Ai == A mod p ?
            Qp = QR[0];
            Rp = QR[1];
            B1p = Rp;
            A1p = Ap.sum( Bq.multiply( Qp ) );

            if ( false && debug ) {
               Eq = A1p.multiply(Aq).sum(B1p.multiply(Bq)).subtract(Ep);
               if ( !Eq.isZERO() ) {
                  System.out.println("A*A1p+B*B1p-Ep2 != 0 " + Eq );
                  throw new RuntimeException("A*A1p+B*B1p-Ep2 != 0 mod " + Q.getModul());
               }
            }

            // construct q-adic approximation, convert to integer
            Ea = PolyUtil.integerFromModularCoefficients(fac,A1p);
            Eb = PolyUtil.integerFromModularCoefficients(fac,B1p);
            Ea1 = Ea.multiply( Qi );
            Eb1 = Eb.multiply( Qi );
            Ea = Si.sum( Ea1 ); // Eb1 and Ea1 are required
            Eb = Ti.sum( Eb1 ); //--------------------------
            Si = Ea;
            Ti = Eb;

            // prepare for next iteration
            //Qi = new BigInteger( Q.getModul().multiply( P.getModul() ) );
            Mq = Qi;
            Qi = new BigInteger( Q.getModul().multiply( Q.getModul() ) );
            Q = new ModIntegerRing( Qi.getVal() );
            qfac = new GenPolynomialRing<ModInteger>(Q,pfac);

            Aq = PolyUtil.<ModInteger>fromIntegerCoefficients(qfac,Ai);
            Bq = PolyUtil.<ModInteger>fromIntegerCoefficients(qfac,Bi);
            Sp = PolyUtil.<ModInteger>fromIntegerCoefficients(qfac,Si);
            Tp = PolyUtil.<ModInteger>fromIntegerCoefficients(qfac,Ti);
            if ( false && debug ) {
               E = Ai.multiply(Si).sum( Bi.multiply(Ti) );
               Eq = PolyUtil.<ModInteger>fromIntegerCoefficients(qfac,E); 
               if ( !Eq.isONE() ) {
                  System.out.println("Ai*Si+Bi*Ti=1 " + Eq );
                  throw new RuntimeException("Ai*Si+Bi*Ti != 1 mod " + Q.getModul());
               }
            }
        }
        GreatestCommonDivisorAbstract<BigInteger> ufd
            = new GreatestCommonDivisorPrimitive<BigInteger>();

        // remove normalization
        BigInteger ai = ufd.baseContent(Ai);
        Ai = Ai.divide( ai );
        BigInteger bi = c.divide(ai);
        Bi = Bi.divide( bi ); // divide( c/a )

        AB[0] = Ai;
        AB[1] = Bi;
        return AB;
    }

}
