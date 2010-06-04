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

import org.apache.commons.collections.primitives.IntCollection;

/**
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:20 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractIntCollectionCollection implements Collection {
    
    public boolean add(Object element) {
        return getIntCollection().add(((Number)element).intValue());
    }

    public boolean addAll(Collection c) {
        return getIntCollection().addAll(CollectionIntCollection.wrap(c));
    }
        
    public void clear() {
        getIntCollection().clear();
    }

    public boolean contains(Object element) {
        return getIntCollection().contains(((Number)element).intValue());
    }
   
    
    public boolean containsAll(Collection c) {
        return getIntCollection().containsAll(CollectionIntCollection.wrap(c));
    }        
        
    public String toString() {
        return getIntCollection().toString();
    }
    
    public boolean isEmpty() {
        return getIntCollection().isEmpty();
    }
    
    /**
     * {@link IntIteratorIterator#wrap wraps} the 
     * {@link org.apache.commons.collections.primitives.IntIterator IntIterator}
     * returned by my underlying 
     * {@link IntCollection IntCollection}, 
     * if any.
     */
    public Iterator iterator() {
        return IntIteratorIterator.wrap(getIntCollection().iterator());
    }
     
    public boolean remove(Object element) {
        return getIntCollection().removeElement(((Number)element).intValue());
    }
    
    public boolean removeAll(Collection c) {
        return getIntCollection().removeAll(CollectionIntCollection.wrap(c));
    }
    
    public boolean retainAll(Collection c) {
        return getIntCollection().retainAll(CollectionIntCollection.wrap(c));
    }
    
    public int size() {
        return getIntCollection().size();
    }
    
    public Object[] toArray() {
        int[] a = getIntCollection().toArray();
        Object[] A = new Object[a.length];
        for(int i=0;i<a.length;i++) {
            A[i] = new Integer(a[i]);
        }
        return A;
    }
    
    public Object[] toArray(Object[] A) {
        int[] a = getIntCollection().toArray();
        if(A.length < a.length) {
            A = (Object[])(Array.newInstance(A.getClass().getComponentType(), a.length));
        }
        for(int i=0;i<a.length;i++) {
            A[i] = new Integer(a[i]);
        }
        if(A.length > a.length) {
            A[a.length] = null;
        }

        return A;
    }

    protected abstract IntCollection getIntCollection();            
}
