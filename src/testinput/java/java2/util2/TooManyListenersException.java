/*
 * @(#)TooManyListenersException.java	1.12 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java2.util2;

/**
 * <p>
 * The <code> TooManyListenersException </code> Exception is used as part of
 * the Java Event model to annotate and implement a unicast special case of
 * a multicast Event Source.
 * </p>
 * <p>
 * The presence of a "throws TooManyListenersException" clause on any given
 * concrete implementation of the normally multicast "void addXyzEventListener"
 * event listener registration pattern is used to annotate that interface as
 * implementing a unicast Listener special case, that is, that one and only
 * one Listener may be registered on the particular event listener source
 * concurrently.
 * </p>
 *
 * @see java2.util2.EventObject
 * @see java2.util2.EventListener
 *
 * @version 1.12 03/01/23
 * @author Laurence P. G. Cable
 * @since  JDK1.1
 */
public class TooManyListenersException extends Exception {

  /**
   * Constructs a TooManyListenersException with no detail message.
   * A detail message is a String that describes this particular exception.
   */
  public TooManyListenersException() {
    super();
  }

  /**
   * Constructs a TooManyListenersException with the specified detail message.
   * A detail message is a String that describes this particular exception.
   * @param s the detail message
   */
  public TooManyListenersException(String s) {
    super(s);
  }
}
