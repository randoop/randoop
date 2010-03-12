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
 * Adapts a {@link Boolean Boolean}-valued {@link java.util.Iterator Iterator}
 * to the {@link org.apache.commons.collections.primitives.BooleanIterator
 * BooleanIterator} interface.
 * <p />
 * This implementation delegates most methods to the provided {@link
 * java.util.Iterator Iterator} implementation in the "obvious" way.
 *
 * @since Commons Primitives 1.2
 * @version $Revision: 1.1 $
 */
public class IteratorBooleanIterator implements BooleanIterator {
    
    /**
     * Create an {@link org.apache.commons.collections.primitives.BooleanIterator
     * BooleanIterator} wrapping the specified {@link java.util.Iterator
     * Iterator}.  When the given <i>iterator</i> is <code>null</code>, returns
     * <code>null</code>.
     * 
     * @param iterator the (possibly <code>null</code>)
     *        {@link java.util.Iterator Iterator} to wrap
     * @return an {@link
     * org.apache.commons.collections.primitives.BooleanIterator BooleanIterator}
     * wrapping the given <i>iterator</i>, or <code>null</code> when <i>
     * iterator</i> is <code>null</code>.
     */
    public static BooleanIterator wrap(Iterator iterator) {
        return null == iterator ? null : new IteratorBooleanIterator(iterator);
    }
   
    /**
     * Creates an {@link org.apache.commons.collections.primitives.BooleanIterator
     * BooleanIterator} wrapping the specified {@link java.util.Iterator Iterator}.
     * @see #wrap
     */
    public IteratorBooleanIterator(Iterator iterator) {
        _iterator = iterator;
    }
    
    public boolean hasNext() {
        return _iterator.hasNext();
    }
    
    public boolean next() {
        return ((Boolean)(_iterator.next())).booleanValue();
    }
    
    public void remove() {
        _iterator.remove();
    }
    
    private Iterator _iterator = null;

}
