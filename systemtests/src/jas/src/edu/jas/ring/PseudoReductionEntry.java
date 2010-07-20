/*
 * $Id: PseudoReductionEntry.java 1697 2008-02-22 20:36:47Z kredel $
 */

package edu.jas.ring;

import edu.jas.poly.GenPolynomial;
import edu.jas.structure.RingElem;


/**
 * Polynomial reduction container.
 * Used as container for the return value of normalformFactor.
 * @author Heinz Kredel
 */

public class PseudoReductionEntry<C extends RingElem<C>> {

    public final GenPolynomial<C> pol;
    public final C multiplicator;

    public PseudoReductionEntry(GenPolynomial<C> pol,
                                C multiplicator) {
        this.pol = pol;
        this.multiplicator = multiplicator;
    }

}
