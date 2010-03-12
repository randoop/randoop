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

import org.apache.commons.collections.primitives.BooleanIterator;
import org.apache.commons.collections.primitives.BooleanCollection;


/**
 * @since Commons Primitives 1.1
 * @version $Revision: 1.2 $ $Date: 2004/04/14 22:23:40 $
 */
abstract class AbstractCollectionBooleanCollection
        implements BooleanCollection {

    protected AbstractCollectionBooleanCollection() {
    }

    public boolean add(boolean element) {
        return getCollection().add(new Boolean(element));
    }
        
    public boolean addAll(BooleanCollection c) {
        return getCollection().addAll(BooleanCollectionCollection.wrap(c));
    }
    
    public void clear() {
        getCollection().clear();
    }

    public boolean contains(boolean element) {
        return getCollection().contains(new Boolean(element));
    }
    
    public boolean containsAll(BooleanCollection c) {
        return getCollection().containsAll(
                BooleanCollectionCollection.wrap(c));
    }        
    
    public String toString() {
        return getCollection().toString();
    }

    public boolean isEmpty() {
        return getCollection().isEmpty();
    }
    
    /**
     * {@link IteratorBooleanIterator#wrap wraps} the {@link java.util.Iterator
     * Iterator} returned by my underlying {@link java.util.Collection
     * Collection}, if any.
     */
    public BooleanIterator iterator() {
        return IteratorBooleanIterator.wrap(getCollection().iterator());
    }
     
    public boolean removeElement(boolean element) {
        return getCollection().remove(new Boolean(element));
    }
    
    public boolean removeAll(BooleanCollection c) {
        return getCollection().removeAll(BooleanCollectionCollection.wrap(c));
    }
        
    public boolean retainAll(BooleanCollection c) {
        return getCollection().retainAll(BooleanCollectionCollection.wrap(c));
    }
    
    public int size() {
        return getCollection().size();
    }
    
    public boolean[] toArray() {
        Object[] src = getCollection().toArray();
        boolean[] dest = new boolean[src.length];
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Boolean)(src[i])).booleanValue();
        }
        return dest;
    }
    
    public boolean[] toArray(boolean[] dest) {
        Object[] src = getCollection().toArray();
        if(dest.length < src.length) {
            dest = new boolean[src.length];
        }
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Boolean)(src[i])).booleanValue();
        }
        return dest;
    }
    
    protected abstract Collection getCollection();
    
}
