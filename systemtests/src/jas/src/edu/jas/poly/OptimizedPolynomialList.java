/*
 * $Id: OptimizedPolynomialList.java 1663 2008-02-05 17:32:07Z kredel $
 */

package edu.jas.poly;

import java.util.List;

import edu.jas.structure.RingElem;



/**
 * Container for optimization results.
 * @author Heinz Kredel
 */

public class OptimizedPolynomialList<C extends RingElem<C>> 
                 extends PolynomialList<C> {

/**
 * Permutation vector used to optimize term order.
 */
      public final List<Integer> perm;
 

/**
 * Constructor.
 */
      public OptimizedPolynomialList( List<Integer> P, 
                                      GenPolynomialRing<C> R, 
                                      List<GenPolynomial<C>> L ) {
            super(R,L);
            perm = P;
      }



/**
 * String representation.
 */
      @Override
     public String toString() {
             return "permutation = " + perm 
                 + "\n" + super.toString();
      }

}
