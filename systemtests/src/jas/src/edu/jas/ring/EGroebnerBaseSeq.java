/*
 * $Id: EGroebnerBaseSeq.java 1712 2008-02-24 18:17:30Z kredel $
 */

package edu.jas.ring;

//import java.util.ArrayList;
//import java.util.List;
//import java.util.ListIterator;

import org.apache.log4j.Logger;

import edu.jas.structure.RingElem;

//import edu.jas.poly.ExpVector;
//import edu.jas.poly.GenPolynomial;

//import edu.jas.ring.OrderedDPairlist;

/**
 * E-Groebner Base sequential algorithm.
 * Nearly empty class, only the e-reduction 
 * is used instead of d-reduction.
 * <b>Note:</b> Minimal reduced GBs are again unique.
 * see BWK, section 10.1.
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class EGroebnerBaseSeq<C extends RingElem<C>> 
       extends DGroebnerBaseSeq<C> 
       /*implements GroebnerBase<C>*/ {


    private static final Logger logger = Logger.getLogger(EGroebnerBaseSeq.class);
    private final boolean debug = true; //logger.isDebugEnabled();



    /**
     * Reduction engine.
     */
    protected EReduction<C> red;  // shadow super.red


    /**
     * Constructor.
     */
    public EGroebnerBaseSeq() {
        this( new EReductionSeq<C>() );
    }


    /**
     * Constructor.
     * @param red E-Reduction engine
     */
    public EGroebnerBaseSeq(EReductionSeq<C> red) {
        super(red);
        this.red = red;
    }


}
