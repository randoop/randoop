/*
 * Copyright 2003-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections.primitives;

/**
 * A bi-directional iterator over <code>boolean</code> values.
 *
 * @see org.apache.commons.collections.primitives.adapters.BooleanListIteratorListIterator
 * @see org.apache.commons.collections.primitives.adapters.BooleanIteratorBooleanListIterator
 *
 * @since Commons Primitives 1.1
 * @version $Revision: 1.2 $ $Date: 2004/04/14 22:23:40 $
 */
public interface BooleanListIterator extends BooleanIterator {
    /**
     * Inserts the specified element into my underlying collection
     * (optional operation).
     * The element is inserted immediately before the next element 
     * that would have been returned by {@link #next}, if any,
     * and immediately after the next element that would have been 
     * returned by {@link #previous}, if any.
     * <p/>
     * The new element is inserted immediately before the implied
     * cursor. A subsequent call to {@link #previous} will return
     * the added element, a subsequent call to {@link #next} will
     * be unaffected.  This call increases by one the value that
     * would be returned by a call to {@link #nextIndex} or 
     * {@link #previousIndex}.
     * 
     * @param element the value to be inserted
     * 
     * @throws UnsupportedOperationException when this operation is not 
     *         supported
     * @throws IllegalArgumentException if some aspect of the specified element 
     *         prevents it from being added
     */
    void add(boolean element);

    /** 
     * Returns <code>true</code> iff I have more elements when traversed in
     * the forward direction. (In other words, returns <code>true</code> iff
     * a call to {@link #next} will return an element rather than throwing
     * an exception.
     * 
     * @return <code>true</code> iff I have more elements when 
     *         traversed in the forward direction
     */
    boolean hasNext();
    
    /** 
     * Returns <code>true</code> iff I have more elements when traversed
     * in the reverse direction. (In other words, returns <code>true</code>
     * iff a call to {@link #previous} will return an element rather than
     * throwing an exception.
     * 
     * @return <code>true</code> iff I have more elements when traversed
     *         in the reverse direction
     */
    boolean hasPrevious();

    /** 
     * Returns the next element in me when traversed in the
     * forward direction.
     * 
     * @return the next element in me
     * @throws java.util.NoSuchElementException if there is no next element
     */          
    boolean next();
    
    /** 
     * Returns the index of the element that would be returned
     * by a subsequent call to {@link #next}, or the number 
     * of elements in my iteration if I have no next element.
     * 
     * @return the index of the next element in me
     */          
    int nextIndex();

    /** 
     * Returns the next element in me when traversed in the
     * reverse direction.
     * 
     * @return the previous element in me
     * @throws java.util.NoSuchElementException if there is no previous element
     */          
    boolean previous();

    /** 
     * Returns the index of the element that would be returned
     * by a subsequent call to {@link #previous}, or 
     * <code>-1</code> if I have no previous element.
     * 
     * @return the index of the previous element in me
     */          
    int previousIndex();

    /** 
     * Removes from my underlying collection the last 
     * element returned by {@link #next} or {@link #previous}
     * (optional operation). 
     * 
     * @throws UnsupportedOperationException if this operation is not 
     *         supported
     * @throws IllegalStateException if neither {@link #next} nor
     *         {@link #previous} has yet been called, or 
     *         {@link #remove} or {@link #add} has already been called since 
     *         the last call to {@link #next} or {@link #previous}.
     */          
    void remove();

    /** 
     * Replaces in my underlying collection the last 
     * element returned by {@link #next} or {@link #previous}
     * with the specified value (optional operation). 
     * 
     * @param element the value to replace the last returned element with
     * @throws UnsupportedOperationException if this operation is not 
     *         supported
     * @throws IllegalStateException if neither {@link #next} nor
     *         {@link #previous} has yet been called, or 
     *         {@link #remove} or {@link #add} has already been called since 
     *         the last call to {@link #next} or {@link #previous}.
     * @throws IllegalArgumentException if some aspect of the specified element 
     *         prevents it from being added
     */          
    void set(boolean element);
}
