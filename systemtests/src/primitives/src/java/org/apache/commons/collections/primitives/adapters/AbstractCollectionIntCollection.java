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

import org.apache.commons.collections.primitives.IntCollection;
import org.apache.commons.collections.primitives.IntIterator;

/**
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:20 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractCollectionIntCollection implements IntCollection {
    protected AbstractCollectionIntCollection() {
    }

    public boolean add(int element) {
        return getCollection().add(new Integer(element));
    }
        
    public boolean addAll(IntCollection c) {
        return getCollection().addAll(IntCollectionCollection.wrap(c));
    }
    
    public void clear() {
        getCollection().clear();
    }

    public boolean contains(int element) {
        return getCollection().contains(new Integer(element));
    }
    
    public boolean containsAll(IntCollection c) {
        return getCollection().containsAll(IntCollectionCollection.wrap(c));
    }        
    
    public String toString() {
        return getCollection().toString();
    }

    public boolean isEmpty() {
        return getCollection().isEmpty();
    }
    
    /**
     * {@link IteratorIntIterator#wrap wraps} the 
     * {@link java.util.Iterator Iterator}
     * returned by my underlying 
     * {@link Collection Collection}, 
     * if any.
     */
    public IntIterator iterator() {
        return IteratorIntIterator.wrap(getCollection().iterator());
    }
     
    public boolean removeElement(int element) {
        return getCollection().remove(new Integer(element));
    }
    
    public boolean removeAll(IntCollection c) {
        return getCollection().removeAll(IntCollectionCollection.wrap(c));
    }
        
    public boolean retainAll(IntCollection c) {
        return getCollection().retainAll(IntCollectionCollection.wrap(c));
    }
    
    public int size() {
        return getCollection().size();
    }
    
    public int[] toArray() {
        Object[] src = getCollection().toArray();
        int[] dest = new int[src.length];
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Number)(src[i])).intValue();
        }
        return dest;
    }
    
    public int[] toArray(int[] dest) {
        Object[] src = getCollection().toArray();
        if(dest.length < src.length) {
            dest = new int[src.length];
        }
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Number)(src[i])).intValue();
        }
        return dest;
    }
    
    protected abstract Collection getCollection();
    
}
