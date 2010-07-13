/*
 * $Id: RPseudoReduction.java 1971 2008-08-02 10:41:10Z kredel $
 */

package edu.jas.ring;


import edu.jas.structure.RegularRingElem;


/**
 * Polynomial R pseudo reduction interface. Combines RReduction and
 * PseudoReduction.
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public interface RPseudoReduction<C extends RegularRingElem<C>> extends RReduction<C>,
        PseudoReduction<C> {

}
