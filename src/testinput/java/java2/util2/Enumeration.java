/*
 * @(#)Enumeration.java	1.20 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java2.util2;

/**
 * An object that implements the Enumeration interface generates a
 * series of elements, one at a time. Successive calls to the
 * <code>nextElement</code> method return successive elements of the
 * series.
 * <p>
 * For example, to print all elements of a vector <i>v</i>:
 * <blockquote><pre>
 *     for (Enumeration e = v.elements() ; e.hasMoreElements() ;) {
 *         System.out.println(e.nextElement());<br>
 *     }
 * </pre></blockquote>
 * <p>
 * Methods are provided to enumerate through the elements of a
 * vector, the keys of a hashtable, and the values in a hashtable.
 * Enumerations are also used to specify the input streams to a
 * <code>SequenceInputStream</code>.
 * <p>
 * NOTE: The functionality of this interface is duplicated by the Iterator
 * interface.  In addition, Iterator adds an optional remove operation, and
 * has shorter method names.  New implementations should consider using
 * Iterator in preference to Enumeration.
 *
 * @see     java2.util2.Iterator
 * @see     java.io.SequenceInputStream
 * @see     java2.util2.Enumeration#nextElement()
 * @see     java2.util2.Hashtable
 * @see     java2.util2.Hashtable#elements()
 * @see     java2.util2.Hashtable#keys()
 * @see     java2.util2.Vector
 * @see     java2.util2.Vector#elements()
 *
 * @author  Lee Boynton
 * @version 1.20, 01/23/03
 * @since   JDK1.0
 */
public interface Enumeration {
  /**
   * Tests if this enumeration contains more elements.
   *
   * @return  <code>true</code> if and only if this enumeration object
   *           contains at least one more element to provide;
   *          <code>false</code> otherwise.
   */
  boolean hasMoreElements();

  /**
   * Returns the next element of this enumeration if this enumeration
   * object has at least one more element to provide.
   *
   * @return     the next element of this enumeration.
   * @exception  NoSuchElementException  if no more elements exist.
   */
  Object nextElement();
}
