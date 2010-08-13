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

import org.apache.commons.collections.primitives.BooleanIterator;

/**
 * Adapts an {@link org.apache.commons.collections.primitives.BooleanIterator
 * BooleanIterator} to the {@link java.util.Iterator Iterator} interface.
 * <p />
 * This implementation delegates most methods to the provided {@link
 * org.apache.commons.collections.primitives.BooleanIterator BooleanIterator}
 * implementation in the "obvious" way.
 *
 * @since Commons Primitives 1.1
 * @version $Revision: 1.2 $ $Date: 2004/04/14 22:23:40 $
 */
public class BooleanIteratorIterator implements Iterator {
    
    /**
     * Create an {@link java.util.Iterator Iterator} wrapping the specified
     * {@link org.apache.commons.collections.primitives.BooleanIterator
     * BooleanIterator}.  When the given <i>iterator</i> is <code>null</code>,
     * returns <code>null</code>.
     * 
     * @param iterator the (possibly <code>null</code>) 
     *        {@link org.apache.commons.collections.primitives.BooleanIterator
     *        BooleanIterator} to wrap
     * @return an {@link java.util.Iterator Iterator} wrapping the given
     *         <i>iterator</i>, or <code>null</code> when <i>iterator</i> is
     *         <code>null</code>.
     */
    public static Iterator wrap(BooleanIterator iterator) {
        return null == iterator ? null : new BooleanIteratorIterator(iterator);
    }
    
    /**
     * Creates an {@link java.util.Iterator Iterator} wrapping the specified
     * {@link org.apache.commons.collections.primitives.BooleanIterator
     * BooleanIterator}.
     * @see #wrap
     */
    public BooleanIteratorIterator(BooleanIterator iterator) {
        _iterator = iterator;
    }
    
    public boolean hasNext() {
        return _iterator.hasNext();
    }
    
    public Object next() {
        return new Boolean(_iterator.next());
    }
    
    public void remove() {
        _iterator.remove();
    }
    
    private BooleanIterator _iterator = null;

}
