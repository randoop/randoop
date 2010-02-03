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

import org.apache.commons.collections.primitives.ShortCollection;
import org.apache.commons.collections.primitives.ShortIterator;

/**
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractCollectionShortCollection implements ShortCollection {
    protected AbstractCollectionShortCollection() {
    }

    public boolean add(short element) {
        return getCollection().add(new Short(element));
    }
        
    public boolean addAll(ShortCollection c) {
        return getCollection().addAll(ShortCollectionCollection.wrap(c));
    }
    
    public void clear() {
        getCollection().clear();
    }

    public boolean contains(short element) {
        return getCollection().contains(new Short(element));
    }
    
    public boolean containsAll(ShortCollection c) {
        return getCollection().containsAll(ShortCollectionCollection.wrap(c));
    }        
    
    public String toString() {
        return getCollection().toString();
    }

    public boolean isEmpty() {
        return getCollection().isEmpty();
    }
    
    /**
     * {@link IteratorShortIterator#wrap wraps} the 
     * {@link java.util.Iterator Iterator}
     * returned by my underlying 
     * {@link Collection Collection}, 
     * if any.
     */
    public ShortIterator iterator() {
        return IteratorShortIterator.wrap(getCollection().iterator());
    }
     
    public boolean removeElement(short element) {
        return getCollection().remove(new Short(element));
    }
    
    public boolean removeAll(ShortCollection c) {
        return getCollection().removeAll(ShortCollectionCollection.wrap(c));
    }
        
    public boolean retainAll(ShortCollection c) {
        return getCollection().retainAll(ShortCollectionCollection.wrap(c));
    }
    
    public int size() {
        return getCollection().size();
    }
    
    public short[] toArray() {
        Object[] src = getCollection().toArray();
        short[] dest = new short[src.length];
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Number)(src[i])).shortValue();
        }
        return dest;
    }
    
    public short[] toArray(short[] dest) {
        Object[] src = getCollection().toArray();
        if(dest.length < src.length) {
            dest = new short[src.length];
        }
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Number)(src[i])).shortValue();
        }
        return dest;
    }
    
    protected abstract Collection getCollection();
    
}
