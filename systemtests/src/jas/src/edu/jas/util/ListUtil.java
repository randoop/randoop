/*
 * $Id: ListUtil.java 2223 2008-11-16 18:01:43Z kredel $
 */

package edu.jas.util;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import edu.jas.structure.RingElem;
import edu.jas.structure.RingFactory;
import edu.jas.structure.UnaryFunctor;


/**
 * List utilities.
 * For example map functor on list elements.
 * @author Heinz Kredel
 */

public class ListUtil {


    private static final Logger logger = Logger.getLogger(ListUtil.class);
    // private static boolean debug = logger.isDebugEnabled();


    /**
     * Map a unary function to the list.
     * @param f evaluation functor.
     * @return new list elements f(list(i)).
     */
    public static <C extends RingElem<C>,D extends RingElem<D>>
           List<D> map(List<C> list, UnaryFunctor<C,D> f) {
        if ( list == null ) {
            return (List<D>)null;
        }
        List<D> nl;
        if ( list instanceof ArrayList ) {
            nl = new ArrayList<D>( list.size() );
        } else if ( list instanceof LinkedList ) {
            nl = new LinkedList<D>();
        } else {
            throw new RuntimeException("list type not implemented");
        }
        for ( C c : list ) {
            D n = f.eval( c );
            nl.add( n );
        }
        return nl;
    }

}