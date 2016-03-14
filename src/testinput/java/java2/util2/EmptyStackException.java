/*
 * @(#)EmptyStackException.java	1.19 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java2.util2;

/**
 * Thrown by methods in the <code>Stack</code> class to indicate
 * that the stack is empty.
 *
 * @author  Jonathan Payne
 * @version 1.19, 01/23/03
 * @see     java2.util2.Stack
 * @since   JDK1.0
 */
public class EmptyStackException extends RuntimeException {
  /**
   * Constructs a new <code>EmptyStackException</code> with <tt>null</tt>
   * as its error message string.
   */
  public EmptyStackException() {}
}
