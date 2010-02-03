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

import org.apache.commons.collections.primitives.DoubleCollection;
import org.apache.commons.collections.primitives.DoubleIterator;

/**
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:20 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractCollectionDoubleCollection implements DoubleCollection {
    protected AbstractCollectionDoubleCollection() {
    }

    public boolean add(double element) {
        return getCollection().add(new Double(element));
    }
        
    public boolean addAll(DoubleCollection c) {
        return getCollection().addAll(DoubleCollectionCollection.wrap(c));
    }
    
    public void clear() {
        getCollection().clear();
    }

    public boolean contains(double element) {
        return getCollection().contains(new Double(element));
    }
    
    public boolean containsAll(DoubleCollection c) {
        return getCollection().containsAll(DoubleCollectionCollection.wrap(c));
    }        
    
    public String toString() {
        return getCollection().toString();
    }

    public boolean isEmpty() {
        return getCollection().isEmpty();
    }
    
    /**
     * {@link IteratorDoubleIterator#wrap wraps} the 
     * {@link java.util.Iterator Iterator}
     * returned by my underlying 
     * {@link Collection Collection}, 
     * if any.
     */
    public DoubleIterator iterator() {
        return IteratorDoubleIterator.wrap(getCollection().iterator());
    }
     
    public boolean removeElement(double element) {
        return getCollection().remove(new Double(element));
    }
    
    public boolean removeAll(DoubleCollection c) {
        return getCollection().removeAll(DoubleCollectionCollection.wrap(c));
    }
        
    public boolean retainAll(DoubleCollection c) {
        return getCollection().retainAll(DoubleCollectionCollection.wrap(c));
    }
    
    public int size() {
        return getCollection().size();
    }
    
    public double[] toArray() {
        Object[] src = getCollection().toArray();
        double[] dest = new double[src.length];
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Number)(src[i])).doubleValue();
        }
        return dest;
    }
    
    public double[] toArray(double[] dest) {
        Object[] src = getCollection().toArray();
        if(dest.length < src.length) {
            dest = new double[src.length];
        }
        for(int i=0;i<src.length;i++) {
            dest[i] = ((Number)(src[i])).doubleValue();
        }
        return dest;
    }
    
    protected abstract Collection getCollection();
    
}
