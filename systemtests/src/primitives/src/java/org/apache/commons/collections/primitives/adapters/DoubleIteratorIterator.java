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

import org.apache.commons.collections.primitives.DoubleIterator;

/**
 * Adapts an {@link DoubleIterator DoubleIterator} to the
 * {@link java.util.Iterator Iterator} interface.
 * <p />
 * This implementation delegates most methods
 * to the provided {@link DoubleIterator DoubleIterator} 
 * implementation in the "obvious" way.
 *
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
public class DoubleIteratorIterator implements Iterator {
    
    /**
     * Create an {@link Iterator Iterator} wrapping
     * the specified {@link DoubleIterator DoubleIterator}.  When
     * the given <i>iterator</i> is <code>null</code>,
     * returns <code>null</code>.
     * 
     * @param iterator the (possibly <code>null</code>) 
     *        {@link DoubleIterator DoubleIterator} to wrap
     * @return an {@link Iterator Iterator} wrapping the given 
     *         <i>iterator</i>, or <code>null</code> when <i>iterator</i> is
     *         <code>null</code>.
     */
    public static Iterator wrap(DoubleIterator iterator) {
        return null == iterator ? null : new DoubleIteratorIterator(iterator);
    }
    
    /**
     * Creates an {@link Iterator Iterator} wrapping
     * the specified {@link DoubleIterator DoubleIterator}.
     * @see #wrap
     */
    public DoubleIteratorIterator(DoubleIterator iterator) {
        _iterator = iterator;
    }
    
    public boolean hasNext() {
        return _iterator.hasNext();
    }
    
    public Object next() {
        return new Double(_iterator.next());
    }
    
    public void remove() {
        _iterator.remove();
    }
    
    private DoubleIterator _iterator = null;

}
