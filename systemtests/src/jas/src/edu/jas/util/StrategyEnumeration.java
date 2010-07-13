/*
 * $Id: StrategyEnumeration.java 1663 2008-02-05 17:32:07Z kredel $
 */

package edu.jas.util;

/**
 * StrategyEnumeration.
 * This class names possible / implemented strategies for thread pools.
 * @author Heinz Kredel.
 */

public final class StrategyEnumeration {

    public static final StrategyEnumeration FIFO = new StrategyEnumeration();

    public static final StrategyEnumeration LIFO = new StrategyEnumeration();

    private StrategyEnumeration() { }

/**
 * toString.
 */    
    @Override
     public String toString() {
        if (this == FIFO) {
           return "FIFO strategy";
        } else {
           return "LIFO strategy";
        }
    }

}
