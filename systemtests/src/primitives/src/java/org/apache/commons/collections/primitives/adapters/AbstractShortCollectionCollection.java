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

import org.apache.commons.collections.primitives.ShortCollection;

/**
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:20 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractShortCollectionCollection implements Collection {
    
    public boolean add(Object element) {
        return getShortCollection().add(((Number)element).shortValue());
    }

    public boolean addAll(Collection c) {
        return getShortCollection().addAll(CollectionShortCollection.wrap(c));
    }
        
    public void clear() {
        getShortCollection().clear();
    }

    public boolean contains(Object element) {
        return getShortCollection().contains(((Number)element).shortValue());
    }
   
    
    public boolean containsAll(Collection c) {
        return getShortCollection().containsAll(CollectionShortCollection.wrap(c));
    }        
        
    public String toString() {
        return getShortCollection().toString();
    }
    
    public boolean isEmpty() {
        return getShortCollection().isEmpty();
    }
    
    /**
     * {@link ShortIteratorIterator#wrap wraps} the 
     * {@link org.apache.commons.collections.primitives.ShortIterator ShortIterator}
     * returned by my underlying 
     * {@link ShortCollection ShortCollection}, 
     * if any.
     */
    public Iterator iterator() {
        return ShortIteratorIterator.wrap(getShortCollection().iterator());
    }
     
    public boolean remove(Object element) {
        return getShortCollection().removeElement(((Number)element).shortValue());
    }
    
    public boolean removeAll(Collection c) {
        return getShortCollection().removeAll(CollectionShortCollection.wrap(c));
    }
    
    public boolean retainAll(Collection c) {
        return getShortCollection().retainAll(CollectionShortCollection.wrap(c));
    }
    
    public int size() {
        return getShortCollection().size();
    }
    
    public Object[] toArray() {
        short[] a = getShortCollection().toArray();
        Object[] A = new Object[a.length];
        for(int i=0;i<a.length;i++) {
            A[i] = new Short(a[i]);
        }
        return A;
    }
    
    public Object[] toArray(Object[] A) {
        short[] a = getShortCollection().toArray();
        if(A.length < a.length) {
            A = (Object[])(Array.newInstance(A.getClass().getComponentType(), a.length));
        }
        for(int i=0;i<a.length;i++) {
            A[i] = new Short(a[i]);
        }
        if(A.length > a.length) {
            A[a.length] = null;
        }

        return A;
    }

    protected abstract ShortCollection getShortCollection();            
}
