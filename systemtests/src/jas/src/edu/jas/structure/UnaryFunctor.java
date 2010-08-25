/*
 * $Id: UnaryFunctor.java 2208 2008-11-15 18:47:54Z kredel $
 */

package edu.jas.structure;


/**
 * Unary functor interface.
 * @param <C> ring element type
 * @param <D> ring element type
 * @author Heinz Kredel
 */

public interface UnaryFunctor< C extends RingElem<C>, D extends RingElem<D> > {


    /**
     * Evaluate.
     * @return evaluated element.
     */
    public D eval(C c);

}
