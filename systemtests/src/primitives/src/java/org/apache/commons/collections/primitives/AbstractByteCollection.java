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
package org.apache.commons.collections.primitives;

/**
 * Abstract base class for {@link ByteCollection}s.
 * <p />
 * Read-only subclasses must override {@link #iterator}
 * and {@link #size}.  Mutable subclasses
 * should also override {@link #add} and 
 * {@link ByteIterator#remove ByteIterator.remove}.
 * All other methods have at least some base implementation 
 * derived from these.  Subclasses may choose to override 
 * these methods to provide a more efficient implementation. 
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:25 $
 * 
 * @author Rodney Waldhoff 
 */
public abstract class AbstractByteCollection implements ByteCollection {
    public abstract ByteIterator iterator();
    public abstract int size();
          
    protected AbstractByteCollection() { }
              
    /** Unsupported in this base implementation. */
    public boolean add(byte element) {
        throw new UnsupportedOperationException("add(byte) is not supported.");
    }

    public boolean addAll(ByteCollection c) {
        boolean modified = false;
        for(ByteIterator iter = c.iterator(); iter.hasNext(); ) {
            modified  |= add(iter.next());
        }
        return modified;
    }
    
    public void clear() {
        for(ByteIterator iter = iterator(); iter.hasNext();) {
            iter.next();
            iter.remove();
        }
    }        

    public boolean contains(byte element) {
        for(ByteIterator iter = iterator(); iter.hasNext();) {
            if(iter.next() == element) {
                return true;
            }
        }
        return false;
    }
        
    public boolean containsAll(ByteCollection c) {
        for(ByteIterator iter = c.iterator(); iter.hasNext();) {
            if(!contains(iter.next())) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isEmpty() {
        return (0 == size());
    }
       
    public boolean removeElement(byte element) {
        for(ByteIterator iter = iterator(); iter.hasNext();) {
            if(iter.next() == element) {
                iter.remove();
                return true;
            }
        }
        return false;
    }        
    
    public boolean removeAll(ByteCollection c) {
        boolean modified = false;
        for(ByteIterator iter = c.iterator(); iter.hasNext(); ) {
            modified  |= removeElement(iter.next());
        }
        return modified;
    }       
    
    public boolean retainAll(ByteCollection c) {
        boolean modified = false;
        for(ByteIterator iter = iterator(); iter.hasNext();) {
            if(!c.contains(iter.next())) {
                iter.remove();
                modified = true;
            }
        }
        return modified;
    }
    
    public byte[] toArray() {
        byte[] array = new byte[size()];
        int i = 0;
        for(ByteIterator iter = iterator(); iter.hasNext();) {
            array[i] = iter.next();
            i++;
        }
        return array;
    }
        
    public byte[] toArray(byte[] a) {
        if(a.length < size()) {
            return toArray();
        } else {
            int i = 0;
            for(ByteIterator iter = iterator(); iter.hasNext();) {
                a[i] = iter.next();
                i++;
            }
            return a;
        }            
    }
}
