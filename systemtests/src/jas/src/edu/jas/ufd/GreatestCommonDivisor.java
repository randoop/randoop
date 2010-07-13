
/*
 * $Id: GreatestCommonDivisor.java 1631 2008-02-01 10:21:57Z kredel $
 */

package edu.jas.ufd;

import java.io.Serializable;

import java.util.Map;

import edu.jas.structure.GcdRingElem;
import edu.jas.poly.GenPolynomial;


/**
 * Greatest common divisor algorithm interface.
 * @author Heinz Kredel
 * @usage To create classes that implement this interface use the
 * GreatestCommonDivisorFactory. It will select an appropriate 
 * implementation based on the types of polynomial coefficients CT.
 * <pre>
 * GreatestCommonDivisor&lt;CT&gt; engine 
 *   = GCDFactory.&lt;CT&gt;getImplementation( cofac );
 * c = engine.gcd(a,b);
 * </pre>
 * For example, if the coefficient type is BigInteger, the usage looks like
 * <pre>
 * BigInteger cofac = new BigInteger();
 * GreatestCommonDivisor&lt;BigInteger&gt; engine 
 *   = GCDFactory.&lt;BigInteger&gt;getImplementation( cofac );
 * c = engine.gcd(a,b);
 * </pre>
 * @see edu.jas.ufd.GCDFactory#getImplementation( edu.jas.structure.RingFactory f)
 */

public interface GreatestCommonDivisor<C extends GcdRingElem<C> > 
                 extends Serializable {


    /**
     * GenPolynomial content.
     * @param P GenPolynomial.
     * @return cont(P).
     */
    public GenPolynomial<C> content( GenPolynomial<C> P );


    /**
     * GenPolynomial primitive part.
     * @param P GenPolynomial.
     * @return pp(P).
     */
    public GenPolynomial<C> primitivePart( GenPolynomial<C> P );


    /**
     * GenPolynomial greatest comon divisor.
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return gcd(P,S).
     */
    public GenPolynomial<C> gcd( GenPolynomial<C> P,GenPolynomial<C> S );


    /**
     * GenPolynomial least comon multiple.
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return lcm(P,S).
     */
    public GenPolynomial<C> lcm( GenPolynomial<C> P,GenPolynomial<C> S );


    /**
     * GenPolynomial resultant.
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return res(P,S).
     */
    public GenPolynomial<C> resultant( GenPolynomial<C> P, GenPolynomial<C> S );


    /**
     * GenPolynomial greatest squarefree divisor.
     * @param P GenPolynomial.
     * @return squarefree(P).
     */
    public GenPolynomial<C> squarefreePart( GenPolynomial<C> P );


    /**
     * GenPolynomial squarefree factorization.
     * @param P GenPolynomial.
     * @return squarefreeFactors(P).
     */
    public Map<Integer,GenPolynomial<C>> squarefreeFactors( GenPolynomial<C> P );

}
