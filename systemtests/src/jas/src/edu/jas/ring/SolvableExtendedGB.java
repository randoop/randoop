/*
 * $Id: SolvableExtendedGB.java 1712 2008-02-24 18:17:30Z kredel $
 */

package edu.jas.ring;

import java.util.List;

import edu.jas.vector.ModuleList;
import edu.jas.poly.GenSolvablePolynomial;
import edu.jas.poly.GenSolvablePolynomialRing;
import edu.jas.poly.PolynomialList;
import edu.jas.structure.RingElem;


/**
  * Container for a GB and transformation matrices.
  * A container for F, G, calG and calF.
  * Immutable objects.
  * @param <C> coefficient type
  * @param F an ideal base.
  * @param G a Groebner base of F.
  * @param F2G a transformation matrix from F to G.
  * @param G2F a transformation matrix from G to F.
  */
public class SolvableExtendedGB<C extends RingElem<C>> {

       public final List<GenSolvablePolynomial<C>> F;
       public final List<GenSolvablePolynomial<C>> G;
       public final List<List<GenSolvablePolynomial<C>>> F2G;
       public final List<List<GenSolvablePolynomial<C>>> G2F;
       public final GenSolvablePolynomialRing<C> ring;


       public SolvableExtendedGB( List<GenSolvablePolynomial<C>> F,
                                  List<GenSolvablePolynomial<C>> G,
                                  List<List<GenSolvablePolynomial<C>>> F2G,
                                  List<List<GenSolvablePolynomial<C>>> G2F) {
            this.F = F;
            this.G = G;
            this.F2G = F2G;
            this.G2F = G2F;
            GenSolvablePolynomialRing<C> r = null;
         if ( G != null ) {
               for ( GenSolvablePolynomial<C> p : G ) {
                   if ( p != null ) {
                      r = p.ring;
                      break;
                   }
               }
               if ( r != null && r.getVars() == null ) {
                  r.setVars( r.evzero.stdVars("y") );
               }
         }
            this.ring = r;
        }


        /** Get the String representation.
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            PolynomialList<C> P;
            ModuleList<C> M;
            StringBuffer s = new StringBuffer("SolvableExtendedGB: \n\n");
            P = new PolynomialList<C>( ring, F );
            s.append("F = " + P + "\n\n");
            P = new PolynomialList<C>( ring, G );
            s.append("G = " + P + "\n\n");
            M = new ModuleList<C>( ring, F2G );
            s.append("F2G = " + M + "\n\n");
            M = new ModuleList<C>( ring, G2F );
            s.append("G2F = " + M + "\n");
            return s.toString();
        }

}
