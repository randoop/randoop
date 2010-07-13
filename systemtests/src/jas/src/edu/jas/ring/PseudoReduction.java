/*
 * $Id: PseudoReduction.java 1701 2008-02-23 20:16:49Z kredel $
 */

package edu.jas.ring;

import java.util.List;

import edu.jas.poly.ExpVector;
import edu.jas.poly.GenPolynomial;
import edu.jas.structure.RingElem;


/**
 * Polynomial pseudo reduction interface.
 * Defines additionaly normalformFactor.
 * @param <C> coefficient type.
 * @author Heinz Kredel
 */

public interface PseudoReduction<C extends RingElem<C>> 
                 extends Reduction<C> {


    /**
     * Normalform with multiplication factor.
     * @param Pp polynomial list.
     * @param Ap polynomial.
     * @return ( nf(Ap), mf ) with respect to Pp and 
               mf as multiplication factor for Ap.
     */
    public PseudoReductionEntry<C> 
           normalformFactor( List<GenPolynomial<C>> Pp, 
                             GenPolynomial<C> Ap );

}
