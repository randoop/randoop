/*
 * @(#)Observer.java	1.18 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java2.util2;

/**
 * A class can implement the <code>Observer</code> interface when it
 * wants to be informed of changes in observable objects.
 *
 * @author  Chris Warth
 * @version 1.18, 01/23/03
 * @see     java2.util2.Observable
 * @since   JDK1.0
 */
public interface Observer {
  /**
   * This method is called whenever the observed object is changed. An
   * application calls an <tt>Observable</tt> object's
   * <code>notifyObservers</code> method to have all the object's
   * observers notified of the change.
   *
   * @param   o     the observable object.
   * @param   arg   an argument passed to the <code>notifyObservers</code>
   *                 method.
   */
  void update(Observable o, Object arg);
}
