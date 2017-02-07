/*
 * @(#)EventObject.java	1.17 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java2.util2;

/**
 * <p>
 * The root class from which all event state objects shall be derived.
 * <p>
 * All Events are constructed with a reference to the object, the "source",
 * that is logically deemed to be the object upon which the Event in question
 * initially occurred upon.
 *
 * @since JDK1.1
 */
public class EventObject implements java.io.Serializable {
  /**
   * The object on which the Event initially occurred.
   */
  protected transient Object source;

  /**
   * Constructs a prototypical Event.
   *
   * @param    source    The object on which the Event initially occurred.
   */
  public EventObject(Object source) {
    if (source == null) {
      throw new IllegalArgumentException("null source");
    }

    this.source = source;
  }

  /**
   * The object on which the Event initially occurred.
   *
   * @return   The object on which the Event initially occurred.
   */
  public Object getSource() {
    return source;
  }

  /**
   * Returns a String representation of this EventObject.
   *
   * @return  A a String representation of this EventObject.
   */
  public String toString() {
    return getClass().getName() + "[source=" + source + "]";
  }
}
