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
package org.apache.commons.collections.primitives.adapters;

import java.util.ListIterator;

import org.apache.commons.collections.primitives.IntListIterator;

/**
 * Adapts a {@link Number}-valued {@link ListIterator ListIterator} 
 * to the {@link IntListIterator IntListIterator} interface.
 * <p />
 * This implementation delegates most methods
 * to the provided {@link IntListIterator IntListIterator} 
 * implementation in the "obvious" way.
 *
 * @since Commons Primitives 0.1
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:20 $
 * @author Rodney Waldhoff 
 */
public class ListIteratorIntListIterator implements IntListIterator {
        
    /**
     * Create an {@link IntListIterator IntListIterator} wrapping
     * the specified {@link ListIterator ListIterator}.  When
     * the given <i>iterator</i> is <code>null</code>,
     * returns <code>null</code>.
     * 
     * @param iterator the (possibly <code>null</code>) 
     *        {@link ListIterator ListIterator} to wrap
     * @return an {@link IntListIterator IntListIterator} wrapping the given 
     *         <i>iterator</i>, or <code>null</code> when <i>iterator</i> is
     *         <code>null</code>.
     */
    public static IntListIterator wrap(ListIterator iterator) {
        return null == iterator ? null : new ListIteratorIntListIterator(iterator);
    }    
    
    /**
     * Creates an {@link IntListIterator IntListIterator} wrapping
     * the specified {@link ListIterator ListIterator}.
     * @see #wrap
     */
    public ListIteratorIntListIterator(ListIterator iterator) {
        _iterator = iterator;
    }
    
    public int nextIndex() {
        return _iterator.nextIndex();
    }

    public int previousIndex() {
        return _iterator.previousIndex();
    }

    public boolean hasNext() {
        return _iterator.hasNext();
    }

    public boolean hasPrevious() {
        return _iterator.hasPrevious();
    }
    
    public int next() {
        return ((Number)_iterator.next()).intValue();
    }

    public int previous() {
        return ((Number)_iterator.previous()).intValue();
    }

    public void add(int element) {
        _iterator.add(new Integer(element));
    }
      
    public void set(int element) {
        _iterator.set(new Integer(element));
    }

    public void remove() {
        _iterator.remove();
    }
      
    private ListIterator _iterator = null;

}
