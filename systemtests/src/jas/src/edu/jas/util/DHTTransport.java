/*
 * $Id: DHTTransport.java 1662 2008-02-05 17:22:57Z kredel $
 */

package edu.jas.util;

import java.io.Serializable;


/**
 * Transport container for a distributed version of a HashTable.
 * Immutable objects.
 * @author Heinz Kredel
 */

public class DHTTransport implements Serializable {

  public final Object key;
  public final Object value;


/**
 * Constructs a new DHTTransport Container.
 * @param key 
 * @param value
 */
public DHTTransport(Object key, Object value) {
      this.key = key;
      this.value = value;
  }


  /**
   * toString.
   */
  @Override
public String toString() {
      return "" + this.getClass().getName()
             + "("+key+","+value+")";

  }

}
