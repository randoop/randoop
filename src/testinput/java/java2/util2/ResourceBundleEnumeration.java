/*
 * @(#)ResourceBundleEnumeration.java	1.3 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java2.util2;

/**
 * Implements an Enumeration that combines elements from a Set and
 * an Enumeration. Used by ListResourceBundle and PropertyResourceBundle.
 */
class ResourceBundleEnumeration implements Enumeration {

  Set set;
  Iterator iterator;
  Enumeration enumeration; // may remain null

  /**
   * Constructs a resource bundle enumeration.
   * @param set an set providing some elements of the enumeration
   * @param enumeration an enumeration providing more elements of the enumeration.
   *        enumeration may be null.
   */
  ResourceBundleEnumeration(Set set, Enumeration enumeration) {
    this.set = set;
    this.iterator = set.iterator();
    this.enumeration = enumeration;
  }

  Object next = null;

  public boolean hasMoreElements() {
    if (next == null) {
      if (iterator.hasNext()) {
        next = iterator.next();
      } else if (enumeration != null) {
        while (next == null && enumeration.hasMoreElements()) {
          next = enumeration.nextElement();
          if (set.contains(next)) {
            next = null;
          }
        }
      }
    }
    return next != null;
  }

  public Object nextElement() {
    if (hasMoreElements()) {
      Object result = next;
      next = null;
      return result;
    } else {
      throw new NoSuchElementException();
    }
  }
}
