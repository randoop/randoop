
/*
 * $Id: GreatestCommonDivisorModEval.java 2085 2008-08-17 17:53:37Z kredel $
 */

package edu.jas.ufd;


import org.apache.log4j.Logger;

import edu.jas.structure.RingFactory;

import edu.jas.arith.BigInteger;
import edu.jas.arith.ModInteger;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.ExpVector;
import edu.jas.poly.PolyUtil;


/**
 * Greatest common divisor algorithms 
 * with modular evaluation algorithm for recursion.
 * @author Heinz Kredel
 */

public class GreatestCommonDivisorModEval //<C extends GcdRingElem<C> > 
    extends GreatestCommonDivisorAbstract<ModInteger> { 


    private static final Logger logger = Logger.getLogger(GreatestCommonDivisorModEval.class);
    private boolean debug = logger.isDebugEnabled();


    /*
     * Modular gcd algorithm to use.
     */ 
    protected final 
        GreatestCommonDivisorAbstract<ModInteger> mufd   
          // = new GreatestCommonDivisorModular();
             = new GreatestCommonDivisorSimple<ModInteger>();
          // = new GreatestCommonDivisorPrimitive<ModInteger>();
          // = new GreatestCommonDivisorSubres<ModInteger>();


    /*
     * Integer gcd algorithm for fall back.
     */ 
    protected final 
        GreatestCommonDivisorAbstract<BigInteger> iufd   
             = new GreatestCommonDivisorSubres<BigInteger>();
              //new GreatestCommonDivisorSimple<ModInteger>();
              //new GreatestCommonDivisorPrimitive<ModInteger>();


    /**
     * Univariate GenPolynomial greatest comon divisor.
     * Delegate to subresultant baseGcd, should not be needed.
     * @param P univariate GenPolynomial.
     * @param S univariate GenPolynomial.
     * @return gcd(P,S).
     */
    @Override
     public GenPolynomial<ModInteger> baseGcd( GenPolynomial<ModInteger> P,
                                              GenPolynomial<ModInteger> S ) {
        return mufd.baseGcd(P,S);
    }



    /**
     * Univariate GenPolynomial recursive greatest comon divisor.
     * Delegate to subresultant recursiveGcd, should not be needed.
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return gcd(P,S).
     */
    @Override
     public GenPolynomial<GenPolynomial<ModInteger>> 
        recursiveUnivariateGcd( GenPolynomial<GenPolynomial<ModInteger>> P,
                      GenPolynomial<GenPolynomial<ModInteger>> S ) {
        return mufd.recursiveUnivariateGcd(P,S);
    }


    /**
     * GenPolynomial greatest comon divisor, modular evaluation algorithm.
     * Method name must be different because of parameter type erasure.
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return gcd(P,S).
     */
    @Override
     public GenPolynomial<ModInteger> gcd( GenPolynomial<ModInteger> P,
                                          GenPolynomial<ModInteger> S ) {
        if ( S == null || S.isZERO() ) {
            return P;
        }
        if ( P == null || P.isZERO() ) {
            return S;
        }
        GenPolynomialRing<ModInteger> fac = P.ring;
        // special case for univariate polynomials
        if ( fac.nvar <= 1 ) {
           GenPolynomial<ModInteger> T = baseGcd(P,S);
           return T;
        }
        long e = P.degree(0);
        long f = S.degree(0);
        GenPolynomial<ModInteger> q;
        GenPolynomial<ModInteger> r;
        if ( f > e ) {
           r = P;
           q = S;
           long g = f;
           f = e;
           e = g;
        } else {
           q = P;
           r = S;
        }
        r = r.abs();
        q = q.abs();
        // setup factories
        RingFactory<ModInteger> cofac = P.ring.coFac;
        if ( ! cofac.isField() ) {
           logger.warn("cofac is not a field: " + cofac);
        }
        GenPolynomialRing<ModInteger> mfac
           = new GenPolynomialRing<ModInteger>(cofac,fac.nvar-1,fac.tord);
        GenPolynomialRing<ModInteger> ufac
           = new GenPolynomialRing<ModInteger>(cofac,1,fac.tord);
        GenPolynomialRing<GenPolynomial<ModInteger>> rfac
           = new GenPolynomialRing<GenPolynomial<ModInteger>>(ufac,fac.nvar-1,fac.tord);
        // convert polynomials
        GenPolynomial<GenPolynomial<ModInteger>> qr;
        GenPolynomial<GenPolynomial<ModInteger>> rr;
        qr = PolyUtil.<ModInteger>recursive(rfac,q);
        rr = PolyUtil.<ModInteger>recursive(rfac,r);

        // compute univariate contents and primitive parts
        GenPolynomial<ModInteger> a = recursiveContent( rr );
        GenPolynomial<ModInteger> b = recursiveContent( qr );
        // gcd of univariate coefficient contents
        GenPolynomial<ModInteger> c = gcd(a,b);  
        rr = PolyUtil.<ModInteger>recursiveDivide(rr,a); 
        qr = PolyUtil.<ModInteger>recursiveDivide(qr,b); 
        if ( rr.isONE() ) {
           rr = rr.multiply(c);
           r = PolyUtil.<ModInteger>distribute(fac,rr);
           return r;
        }
        if ( qr.isONE() ) {
           qr = qr.multiply(c);
           q = PolyUtil.<ModInteger>distribute(fac,qr);
           return q;
        }
        // compute normalization factor
        GenPolynomial<ModInteger> ac = rr.leadingBaseCoefficient();
        GenPolynomial<ModInteger> bc = qr.leadingBaseCoefficient();
        GenPolynomial<ModInteger> cc = gcd(ac,bc);  
        // compute degrees and degree vectors
        ExpVector rdegv = rr.degreeVector(); 
        ExpVector qdegv = qr.degreeVector(); 
        long rd0 = PolyUtil.<ModInteger>coeffMaxDegree(rr);
        long qd0 = PolyUtil.<ModInteger>coeffMaxDegree(qr);
        long cd0 = cc.degree(0);
        long G = ( rd0 >= qd0 ? rd0 : qd0 ) + cd0;

        // initialize element and degree vector
        ExpVector wdegv = rdegv.subst( 0, rdegv.getVal(0) + 1 );
        // +1 seems to be a hack for the unlucky prime test
        ModInteger inc = cofac.getONE();
        long i = 0;
        long en = inc.getModul().longValue()-1;
        ModInteger end = cofac.fromInteger(en);
        ModInteger mi;
        GenPolynomial<ModInteger> M = null;
        GenPolynomial<ModInteger> mn;
        GenPolynomial<ModInteger> qm;
        GenPolynomial<ModInteger> rm;
        GenPolynomial<ModInteger> cm;
        GenPolynomial<GenPolynomial<ModInteger>> cp = null;
        if ( debug ) {
           logger.debug("c = " + c);
           logger.debug("cc = " + cc);
           logger.debug("G = " + G);
           logger.info("wdegv = " + wdegv);
        }
        for ( ModInteger d = cofac.getZERO(); d.compareTo(end) <= 0; d = d.sum(inc) ) {
            if ( ++i >= en ) {
               logger.error("elements of Z_p exhausted, en = " + en);
               return mufd.gcd(P,S);
               //throw new RuntimeException("prime list exhausted");
            }
            // map normalization factor
            ModInteger nf = PolyUtil.<ModInteger>evaluateMain( cofac, cc, d );
            if ( nf.isZERO() ) {
                continue;
            }
            // map polynomials
            qm = PolyUtil.<ModInteger>evaluateFirstRec( ufac, mfac, qr, d );
            if ( !qm.degreeVector().equals( qdegv ) ) {
                continue;
            }
            rm = PolyUtil.<ModInteger>evaluateFirstRec( ufac, mfac, rr, d );
            if ( !rm.degreeVector().equals( rdegv ) ) {
                continue;
            }
            if ( debug ) {
               logger.debug("eval d = " + d);
            }
            // compute modular gcd in recursion
            //System.out.println("recursion +++++++++++++++++++++++++++++++++++");
            cm = gcd(rm,qm);
            //System.out.println("recursion -----------------------------------");
            //System.out.println("cm = " + cm);
            // test for constant g.c.d
            if ( cm.isConstant() ) {
               logger.debug("cm.isConstant = " + cm + ", c = " + c);
               if ( c.ring.nvar < cm.ring.nvar ) {
                  c = c.extend(mfac,0,0);
               }
               cm = cm.abs().multiply(c);
               q = cm.extend(fac,0,0);
               logger.debug("q             = " + q + ", c = " + c);
               return q;
            }
            // test for unlucky prime
            ExpVector mdegv = cm.degreeVector();
            if ( wdegv.equals(mdegv) ) { // TL = 0
               // prime ok, next round
               if ( M != null ) {
                  if ( M.degree(0) > G ) {
                     logger.info("deg(M) > G: " + M.degree(0) + " > " + G);
                     // continue; // why should this be required?
                  }
               }
            } else { // TL = 3
               boolean ok = false;
               if ( wdegv.multipleOf( mdegv ) ) { // TL = 2 // EVMT(wdegv,mdegv)
                  M = null;  // init chinese remainder
                  ok = true; // prime ok
               }
               if ( mdegv.multipleOf( wdegv ) ) { // TL = 1 // EVMT(mdegv,wdegv)
                  continue; // skip this prime
               }
               if ( !ok ) {
                  M = null; // discard chinese remainder and previous work
                  continue; // prime not ok
               }
            }
            // prepare interpolation algorithm
            cm = cm.multiply(nf);
            if ( M == null ) {
               // initialize interpolation
               M = ufac.getONE();
               cp = rfac.getZERO();
               wdegv = wdegv.gcd( mdegv ); //EVGCD(wdegv,mdegv);
            }
            // interpolate
            mi = PolyUtil.<ModInteger>evaluateMain( cofac, M, d );
            mi = mi.inverse(); // mod p
            cp = PolyUtil.interpolate(rfac,cp,M,mi,cm,d);
            mn = ufac.getONE().multiply(d);
            mn = ufac.univariate(0).subtract(mn);
            M = M.multiply( mn );
            // test for completion
            if ( M.degree(0) > G ) {
               break;
            }
            //long cmn = PolyUtil.<ModInteger>coeffMaxDegree(cp);
            //if ( M.degree(0) > cmn ) {
            // does not work: only if cofactors are also considered?
            // break;
            //}
        }
        // remove normalization
        cp = recursivePrimitivePart( cp ).abs(); 
        cp = cp.multiply( c ); 
        q = PolyUtil.<ModInteger>distribute(fac,cp);
        return q; 
    }

}
