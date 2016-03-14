/*
 * @(#)EventListenerProxy.java	1.3 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java2.util2;

/**
 * An abstract wrapper class for an EventListener class which associates a set
 * of additional parameters with the listener. Subclasses must provide the
 * storage and accessor methods for the additional arguments or parameters.
 *
 * Subclasses of EventListerProxy may be returned by getListeners() methods
 * as a way of associating named properties with their listeners.
 *
 * For example, a Bean which supports named properties would have a two
 * argument method signature for adding a PropertyChangeListener for a
 * property:
 *
 *     public void addPropertyChangeListener(String propertyName,
 *                                  PropertyChangeListener listener);
 *
 * If the Bean also implemented the zero argument get listener method:
 *
 *     public PropertyChangeListener[] getPropertyChangeListeners();
 *
 * then the array may contain inner PropertyChangeListeners which are also
 * PropertyChangeListenerProxy objects.
 *
 * If the calling method is interested in retrieving the named property then it
 * would have to test the element to see if it is a proxy class.
 *
 * @since 1.4
 */
public abstract class EventListenerProxy implements EventListener {
  private final EventListener listener;

  /**
   * @param listener The listener object.
   */
  public EventListenerProxy(EventListener listener) {
    this.listener = listener;
  }

  /**
   * @return The listener associated with this proxy.
   */
  public EventListener getListener() {
    return listener;
  }
}
