/*
 * Copyright 2004 The Apache Software Foundation
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
 * Abstract base class for {@link BooleanCollection}s.
 * <p/>
 * Read-only subclasses must override {@link #iterator} and {@link #size}.
 * Mutable subclasses should also override {@link #add} and {@link
 * BooleanIterator#remove BooleanIterator.remove}.  All other methods have
 * at least some base implementation derived from these.  Subclasses may
 * choose to override these methods to provide a more efficient implementation.
 * 
 * @since Commons Primitives 1.1
 * @version $Revision: 1.2 $ $Date: 2004/04/14 22:23:40 $
 */
public abstract class AbstractBooleanCollection implements BooleanCollection {
    public abstract BooleanIterator iterator();
    public abstract int size();
          
    protected AbstractBooleanCollection() { }
              
    /** Unsupported in this base implementation. */
    public boolean add(boolean element) {
        throw new UnsupportedOperationException(
                "add(boolean) is not supported.");
    }

    public boolean addAll(BooleanCollection c) {
        boolean modified = false;
        for(BooleanIterator iter = c.iterator(); iter.hasNext(); ) {
            modified  |= add(iter.next());
        }
        return modified;
    }
    
    public void clear() {
        for(BooleanIterator iter = iterator(); iter.hasNext();) {
            iter.next();
            iter.remove();
        }
    }        

    public boolean contains(boolean element) {
        for(BooleanIterator iter = iterator(); iter.hasNext();) {
            if(iter.next() == element) {
                return true;
            }
        }
        return false;
    }
        
    public boolean containsAll(BooleanCollection c) {
        for(BooleanIterator iter = c.iterator(); iter.hasNext();) {
            if(!contains(iter.next())) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isEmpty() {
        return (0 == size());
    }
       
    public boolean removeElement(boolean element) {
        for(BooleanIterator iter = iterator(); iter.hasNext();) {
            if(iter.next() == element) {
                iter.remove();
                return true;
            }
        }
        return false;
    }        
    
    public boolean removeAll(BooleanCollection c) {
        boolean modified = false;
        for(BooleanIterator iter = c.iterator(); iter.hasNext(); ) {
            modified  |= removeElement(iter.next());
        }
        return modified;
    }       
    
    public boolean retainAll(BooleanCollection c) {
        boolean modified = false;
        for(BooleanIterator iter = iterator(); iter.hasNext();) {
            if(!c.contains(iter.next())) {
                iter.remove();
                modified = true;
            }
        }
        return modified;
    }
    
    public boolean[] toArray() {
        boolean[] array = new boolean[size()];
        int i = 0;
        for(BooleanIterator iter = iterator(); iter.hasNext();) {
            array[i] = iter.next();
            i++;
        }
        return array;
    }
        
    public boolean[] toArray(boolean[] a) {
        if(a.length < size()) {
            return toArray();
        } else {
            int i = 0;
            for(BooleanIterator iter = iterator(); iter.hasNext();) {
                a[i] = iter.next();
                i++;
            }
            return a;
        }            
    }
}
