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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.primitives.FloatCollection;

/**
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractFloatCollectionCollection implements Collection {
    
    public boolean add(Object element) {
        return getFloatCollection().add(((Number)element).floatValue());
    }

    public boolean addAll(Collection c) {
        return getFloatCollection().addAll(CollectionFloatCollection.wrap(c));
    }
        
    public void clear() {
        getFloatCollection().clear();
    }

    public boolean contains(Object element) {
        return getFloatCollection().contains(((Number)element).floatValue());
    }
   
    
    public boolean containsAll(Collection c) {
        return getFloatCollection().containsAll(CollectionFloatCollection.wrap(c));
    }        
        
    public String toString() {
        return getFloatCollection().toString();
    }
    
    public boolean isEmpty() {
        return getFloatCollection().isEmpty();
    }
    
    /**
     * {@link FloatIteratorIterator#wrap wraps} the 
     * {@link org.apache.commons.collections.primitives.FloatIterator FloatIterator}
     * returned by my underlying 
     * {@link FloatCollection FloatCollection}, 
     * if any.
     */
    public Iterator iterator() {
        return FloatIteratorIterator.wrap(getFloatCollection().iterator());
    }
     
    public boolean remove(Object element) {
        return getFloatCollection().removeElement(((Number)element).floatValue());
    }
    
    public boolean removeAll(Collection c) {
        return getFloatCollection().removeAll(CollectionFloatCollection.wrap(c));
    }
    
    public boolean retainAll(Collection c) {
        return getFloatCollection().retainAll(CollectionFloatCollection.wrap(c));
    }
    
    public int size() {
        return getFloatCollection().size();
    }
    
    public Object[] toArray() {
        float[] a = getFloatCollection().toArray();
        Object[] A = new Object[a.length];
        for(int i=0;i<a.length;i++) {
            A[i] = new Float(a[i]);
        }
        return A;
    }
    
    public Object[] toArray(Object[] A) {
        float[] a = getFloatCollection().toArray();
        if(A.length < a.length) {
            A = (Object[])(Array.newInstance(A.getClass().getComponentType(), a.length));
        }
        for(int i=0;i<a.length;i++) {
            A[i] = new Float(a[i]);
        }
        if(A.length > a.length) {
            A[a.length] = null;
        }

        return A;
    }

    protected abstract FloatCollection getFloatCollection();            
}
