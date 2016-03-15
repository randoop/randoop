/*
 * @(#)Stack.java	1.26 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java2.util2;

/**
 * The <code>Stack</code> class represents a last-in-first-out
 * (LIFO) stack of objects. It extends class <tt>Vector</tt> with five
 * operations that allow a vector to be treated as a stack. The usual
 * <tt>push</tt> and <tt>pop</tt> operations are provided, as well as a
 * method to <tt>peek</tt> at the top item on the stack, a method to test
 * for whether the stack is <tt>empty</tt>, and a method to <tt>search</tt>
 * the stack for an item and discover how far it is from the top.
 * <p>
 * When a stack is first created, it contains no items.
 *
 * @author  Jonathan Payne
 * @version 1.26, 01/23/03
 * @since   JDK1.0
 */
public class Stack extends Vector {
  /**
   * Creates an empty Stack.
   */
  public Stack() {}

  /**
   * Pushes an item onto the top of this stack. This has exactly
   * the same effect as:
   * <blockquote><pre>
   * addElement(item)</pre></blockquote>
   *
   * @param   item   the item to be pushed onto this stack.
   * @return  the <code>item</code> argument.
   * @see     java2.util2.Vector#addElement
   */
  public Object push(Object item) {
    addElement(item);

    return item;
  }

  /**
   * Removes the object at the top of this stack and returns that
   * object as the value of this function.
   *
   * @return     The object at the top of this stack (the last item
   *             of the <tt>Vector</tt> object).
   * @exception  EmptyStackException  if this stack is empty.
   */
  public synchronized Object pop() {
    Object obj;
    int len = size();

    obj = peek();
    removeElementAt(len - 1);

    return obj;
  }

  /**
   * Looks at the object at the top of this stack without removing it
   * from the stack.
   *
   * @return     the object at the top of this stack (the last item
   *             of the <tt>Vector</tt> object).
   * @exception  EmptyStackException  if this stack is empty.
   */
  public synchronized Object peek() {
    int len = size();

    if (len == 0) throw new EmptyStackException();
    return elementAt(len - 1);
  }

  /**
   * Tests if this stack is empty.
   *
   * @return  <code>true</code> if and only if this stack contains
   *          no items; <code>false</code> otherwise.
   */
  public boolean empty() {
    return size() == 0;
  }

  /**
   * Returns the 1-based position where an object is on this stack.
   * If the object <tt>o</tt> occurs as an item in this stack, this
   * method returns the distance from the top of the stack of the
   * occurrence nearest the top of the stack; the topmost item on the
   * stack is considered to be at distance <tt>1</tt>. The <tt>equals</tt>
   * method is used to compare <tt>o</tt> to the
   * items in this stack.
   *
   * @param   o   the desired object.
   * @return  the 1-based position from the top of the stack where
   *          the object is located; the return value <code>-1</code>
   *          indicates that the object is not on the stack.
   */
  public synchronized int search(Object o) {
    int i = lastIndexOf(o);

    if (i >= 0) {
      return size() - i;
    }
    return -1;
  }

  /** use serialVersionUID from JDK 1.0.2 for interoperability */
  private static final long serialVersionUID = 1224463164541339165L;
}
