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
 * An iterator over <code>short</code> values.
 *
 * @see org.apache.commons.collections.primitives.adapters.ShortIteratorIterator
 * @see org.apache.commons.collections.primitives.adapters.IteratorShortIterator
 *
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:25 $
 * 
 * @author Rodney Waldhoff 
 */
public interface ShortIterator {
    /** 
     * Returns <code>true</code> iff I have more elements. 
     * (In other words, returns <code>true</code> iff 
     * a subsequent call to {@link #next next} will return 
     * an element rather than throwing an exception.)
     * 
     * @return <code>true</code> iff I have more elements
     */
    boolean hasNext();
    
    /** 
     * Returns the next element in me.
     * 
     * @return the next element in me
     * @throws NoSuchElementException if there is no next element
     */          
    short next();
    
    /** 
     * Removes from my underlying collection the last 
     * element {@link #next returned} by me 
     * (optional operation). 
     * 
     * @throws UnsupportedOperationException if this operation is not supported
     * @throws IllegalStateException if {@link #next} has not yet been 
     *         called, or {@link #remove} has already been called since 
     *         the last call to {@link #next}.
     */          
    void remove();
}
