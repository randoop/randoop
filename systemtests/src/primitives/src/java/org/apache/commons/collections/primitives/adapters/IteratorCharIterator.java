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

import java.util.Iterator;

import org.apache.commons.collections.primitives.CharIterator;

/**
 * Adapts a {@link java.lang.Number Number}-valued 
 * {@link Iterator Iterator} 
 * to the {@link CharIterator CharIterator} 
 * interface.
 * <p />
 * This implementation delegates most methods
 * to the provided {@link Iterator Iterator} 
 * implementation in the "obvious" way.
 *
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
public class IteratorCharIterator implements CharIterator {
    
    /**
     * Create an {@link CharIterator CharIterator} wrapping
     * the specified {@link Iterator Iterator}.  When
     * the given <i>iterator</i> is <code>null</code>,
     * returns <code>null</code>.
     * 
     * @param iterator the (possibly <code>null</code>) 
     *        {@link Iterator Iterator} to wrap
     * @return an {@link CharIterator CharIterator} wrapping the given 
     *         <i>iterator</i>, or <code>null</code> when <i>iterator</i> is
     *         <code>null</code>.
     */
    public static CharIterator wrap(Iterator iterator) {
        return null == iterator ? null : new IteratorCharIterator(iterator);
    }
   
    /**
     * Creates an {@link CharIterator CharIterator} wrapping
     * the specified {@link Iterator Iterator}.
     * @see #wrap
     */
    public IteratorCharIterator(Iterator iterator) {
        _iterator = iterator;
    }
    
    public boolean hasNext() {
        return _iterator.hasNext();
    }
    
    public char next() {
        return ((Character)(_iterator.next())).charValue();
    }
    
    public void remove() {
        _iterator.remove();
    }
    
    private Iterator _iterator = null;

}
