/*
 * $Id: EReduction.java 1712 2008-02-24 18:17:30Z kredel $
 */

package edu.jas.ring;


import edu.jas.structure.RingElem;


/**
 * Polynomial E-Reduction interface.
 * Empty marker interface since all required methods are already 
 * defined in the DReduction interface.
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public interface EReduction<C extends RingElem<C>> 
                 extends DReduction<C> {


}
