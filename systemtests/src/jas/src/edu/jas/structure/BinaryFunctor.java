/*
 * $Id: BinaryFunctor.java 2208 2008-11-15 18:47:54Z kredel $
 */

package edu.jas.structure;


/**
 * Binary functor interface.
 * @param <C1> ring element type
 * @param <C2> ring element type
 * @param <D> ring element type
 * @author Heinz Kredel
 */

public interface BinaryFunctor<C1 extends RingElem<C1>,
                               C2 extends RingElem<C2>,
                               D extends RingElem<D> > {


    /**
     * Evaluate.
     * @return evaluated element.
     */
    public D eval(C1 c1, C2 c2);

}
