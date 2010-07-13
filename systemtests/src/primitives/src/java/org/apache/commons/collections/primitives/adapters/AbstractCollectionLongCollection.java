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

import java.util.Collection;

import org.apache.commons.collections.primitives.LongCollection;
import org.apache.commons.collections.primitives.LongIterator;

/**
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractCollectionLongCollection implements LongCollection {
    protected AbstractCollectionLongCollection() {
    }

    public boolean add(long element) {
        return getCollection().add(new Long(element));
    }
        
    public boolean addAll(LongCollection c) {
        return getCollection().addAll(LongCollectionCollection.wrap(c));
    }
    
    public void clear() {
        getCollection().clear();
    }

    public boolean contains(long element) {
        return getCollection().contains(new Long(element));
    }
    
    public boolean containsAll(LongCollection c) {
        return getCollection().containsAll(LongCollectionCollection.wrap(c));
    }        
    
    public String toString() {
        return getCollection().toString();
    }

    public boolean isEmpty() {
        return getCollection().isEmpty();
    }
    
    /**
     * {@link IteratorLongIterator#wrap wraps} the 
     * {@link java.util.Iterator Iterator}
     * returned by my underlying 
     * {@link Collection Collection}, 
     * if any.
     */
    public LongIterator iterator() {
        return IteratorLongIterator.wrap(getCollection().iterator());
    }
     
    public boolean removeElement(long element) {
        return getCollection().remove(new Long(element));
    }
    
    public boolean removeAll(LongCollection c) {
        return getCollection().removeAll(LongCollectionCollection.wrap(c));
    }
        
    public boolean retainAll(LongCollection c) {
        return getCollection().retainAll(LongCollectionCollection.wrap(c));
    }
    
    public int size() {
        return getCollection().size();
    }
    
    public long[] toArray() {
        Object[] src = getCollection().toArray();
        long[] dest = new long[src.length];
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Number)(src[i])).longValue();
        }
        return dest;
    }
    
    public long[] toArray(long[] dest) {
        Object[] src = getCollection().toArray();
        if(dest.length < src.length) {
            dest = new long[src.length];
        }
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Number)(src[i])).longValue();
        }
        return dest;
    }
    
    protected abstract Collection getCollection();
    
}
