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

import org.apache.commons.collections.primitives.ByteCollection;
import org.apache.commons.collections.primitives.ByteIterator;

/**
 * @since Commons Collections 2.2
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractCollectionByteCollection implements ByteCollection {
    protected AbstractCollectionByteCollection() {
    }

    public boolean add(byte element) {
        return getCollection().add(new Byte(element));
    }
        
    public boolean addAll(ByteCollection c) {
        return getCollection().addAll(ByteCollectionCollection.wrap(c));
    }
    
    public void clear() {
        getCollection().clear();
    }

    public boolean contains(byte element) {
        return getCollection().contains(new Byte(element));
    }
    
    public boolean containsAll(ByteCollection c) {
        return getCollection().containsAll(ByteCollectionCollection.wrap(c));
    }        
    
    public String toString() {
        return getCollection().toString();
    }

    public boolean isEmpty() {
        return getCollection().isEmpty();
    }
    
    /**
     * {@link IteratorByteIterator#wrap wraps} the 
     * {@link java.util.Iterator Iterator}
     * returned by my underlying 
     * {@link Collection Collection}, 
     * if any.
     */
    public ByteIterator iterator() {
        return IteratorByteIterator.wrap(getCollection().iterator());
    }
     
    public boolean removeElement(byte element) {
        return getCollection().remove(new Byte(element));
    }
    
    public boolean removeAll(ByteCollection c) {
        return getCollection().removeAll(ByteCollectionCollection.wrap(c));
    }
        
    public boolean retainAll(ByteCollection c) {
        return getCollection().retainAll(ByteCollectionCollection.wrap(c));
    }
    
    public int size() {
        return getCollection().size();
    }
    
    public byte[] toArray() {
        Object[] src = getCollection().toArray();
        byte[] dest = new byte[src.length];
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Number)(src[i])).byteValue();
        }
        return dest;
    }
    
    public byte[] toArray(byte[] dest) {
        Object[] src = getCollection().toArray();
        if(dest.length < src.length) {
            dest = new byte[src.length];
        }
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Number)(src[i])).byteValue();
        }
        return dest;
    }
    
    protected abstract Collection getCollection();
    
}
