/*
 * Copyright 2002-2004 The Apache Software Foundation
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

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * Abstract base class for {@link BooleanList}s backed by random access
 * structures like arrays.
 * <p />
 * Read-only subclasses must override {@link #get} and {@link #size}.
 * Mutable subclasses should also override {@link #set}.  Variably-sized
 * subclasses should also override {@link #add} and {@link #removeElementAt}.
 * All other methods have at least some base implementation derived from
 * these.  Subclasses may choose to override these methods to provide a
 * more efficient implementation.
 * 
 * @since Commons Primitives 1.2
 * @version $Revision: 1.2 $
 */
public abstract class RandomAccessBooleanList extends
        AbstractBooleanCollection implements BooleanList {

    // constructors
    //-------------------------------------------------------------------------

    /** Constructs an empty list. */
    protected RandomAccessBooleanList() { 
    }    

    // fully abstract methods
    //-------------------------------------------------------------------------
    
    public abstract boolean get(int index);
    public abstract int size();

    // unsupported in base
    //-------------------------------------------------------------------------
    
    /** 
     * Unsupported in this implementation. 
     * @throws UnsupportedOperationException since this method is not supported
     */
    public boolean removeElementAt(int index) {
        throw new UnsupportedOperationException();
    }
    
    /** 
     * Unsupported in this implementation. 
     * @throws UnsupportedOperationException since this method is not supported
     */
    public boolean set(int index, boolean element) {
        throw new UnsupportedOperationException();
    }
        
    /** 
     * Unsupported in this implementation. 
     * @throws UnsupportedOperationException since this method is not supported
     */
    public void add(int index, boolean element) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------

    // javadocs here are inherited
    
    public boolean add(boolean element) {
        add(size(),element);
        return true;
    }

    public boolean addAll(int index, BooleanCollection collection) {
        boolean modified = false;
        for(BooleanIterator iter = collection.iterator(); iter.hasNext(); ) {
            add(index++,iter.next());
            modified = true;
        }
        return modified;
    }

    public int indexOf(boolean element) {
        int i = 0;
        for(BooleanIterator iter = iterator(); iter.hasNext(); ) {
            if(iter.next() == element) { 
                return i;
            } else {
                i++;
            }
        }
        return -1;
    }

    public int lastIndexOf(boolean element) {
        for(BooleanListIterator iter = listIterator(size()); iter.hasPrevious(); ) {
            if(iter.previous() == element) {
                return iter.nextIndex();
            }
        }
        return -1;
    }

    public BooleanIterator iterator() {
        return listIterator();
    }

    public BooleanListIterator listIterator() {
        return listIterator(0);
    }

    public BooleanListIterator listIterator(int index) {
        return new RandomAccessBooleanListIterator(this,index);
    }

    public BooleanList subList(int fromIndex, int toIndex) {
        return new RandomAccessBooleanSubList(this,fromIndex,toIndex);
    }

    public boolean equals(Object that) {
        if(this == that) { 
            return true; 
        } else if(that instanceof BooleanList) {
            BooleanList thatList = (BooleanList)that;
            if(size() != thatList.size()) {
                return false;
            }
            for(BooleanIterator thatIter = thatList.iterator(),
                    thisIter = iterator(); thisIter.hasNext();) {
                if(thisIter.next() != thatIter.next()) { 
                    return false; 
                }
            }
            return true;
        } else {
            return false;
        }        
    }

    public int hashCode() {
        int hash = 1;
        for(BooleanIterator iter = iterator(); iter.hasNext(); ) {
            hash = 31*hash + (iter.next() ? 1231 : 1237);
        }
        return hash;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for(BooleanIterator iter = iterator(); iter.hasNext();) {
            buf.append(iter.next());
            if(iter.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }
    
    // protected utilities
    //-------------------------------------------------------------------------
    
    /** Get my count of structural modifications. */
    protected int getModCount() {
        return _modCount;
    }

    /** Increment my count of structural modifications. */
    protected void incrModCount() {
        _modCount++;
    }

    // attributes
    //-------------------------------------------------------------------------
    
    private int _modCount = 0;

    // inner classes
    //-------------------------------------------------------------------------
    
    private static class ComodChecker {
        ComodChecker(RandomAccessBooleanList source) {
            _source = source;  
            resyncModCount();             
        }
        
        protected RandomAccessBooleanList getList() {
            return _source;
        }
        
        protected void assertNotComodified() throws ConcurrentModificationException {
            if(_expectedModCount != getList().getModCount()) {
                throw new ConcurrentModificationException();
            }
        }
            
        protected void resyncModCount() {
            _expectedModCount = getList().getModCount();
        }
        
        private RandomAccessBooleanList _source = null;
        private int _expectedModCount = -1;
    }
    
    protected static class RandomAccessBooleanListIterator
            extends ComodChecker implements BooleanListIterator {
        RandomAccessBooleanListIterator(RandomAccessBooleanList list, int index) {
            super(list);
            if(index < 0 || index > getList().size()) {
                throw new IndexOutOfBoundsException("Index " + index +
                        " not in [0," + getList().size() + ")");
            } else {
                _nextIndex = index;
                resyncModCount();
            }
        }
            
        public boolean hasNext() {
            assertNotComodified();
            return _nextIndex < getList().size();
        }
        
        public boolean hasPrevious() {
            assertNotComodified();
            return _nextIndex > 0;
        }
        
        public int nextIndex() {
            assertNotComodified();
            return _nextIndex;
        }
        
        public int previousIndex() {
            assertNotComodified();
            return _nextIndex - 1;
        }
        
        public boolean next() {
            assertNotComodified();
            if(!hasNext()) {
                throw new NoSuchElementException();
            } else {
                boolean val = getList().get(_nextIndex);
                _lastReturnedIndex = _nextIndex;
                _nextIndex++;
                return val;
            }
        }
        
        public boolean previous() {
            assertNotComodified();
            if(!hasPrevious()) {
                throw new NoSuchElementException();
            } else {
                boolean val = getList().get(_nextIndex-1);
                _lastReturnedIndex = _nextIndex-1;
                _nextIndex--;
                return val;
            }
        }
        
        public void add(boolean value) {
            assertNotComodified();
            getList().add(_nextIndex,value);
            _nextIndex++;
            _lastReturnedIndex = -1;
            resyncModCount();
        }
    
        public void remove() {
            assertNotComodified();
            if(-1 == _lastReturnedIndex) {
                throw new IllegalStateException();
            } else {
                getList().removeElementAt(_lastReturnedIndex);
                _lastReturnedIndex = -1;
                _nextIndex--;
                resyncModCount();
            }
        }
        
        public void set(boolean value) {
            assertNotComodified();
            if(-1 == _lastReturnedIndex) {
                throw new IllegalStateException();
            } else {
                getList().set(_lastReturnedIndex,value);
                resyncModCount();
            }
        }
        
        private int _nextIndex = 0;
        private int _lastReturnedIndex = -1;        
    }   

    protected static class RandomAccessBooleanSubList
            extends RandomAccessBooleanList implements BooleanList {
        RandomAccessBooleanSubList(RandomAccessBooleanList list,
                                   int fromIndex, int toIndex) {
            if(fromIndex < 0 || toIndex > list.size()) {
                throw new IndexOutOfBoundsException();
            } else if(fromIndex > toIndex) {
                throw new IllegalArgumentException();                
            } else {
                _list = list;
                _offset = fromIndex;
                _limit = toIndex - fromIndex;
                _comod = new ComodChecker(list);
                _comod.resyncModCount();
            }            
        }
    
        public boolean get(int index) {
            checkRange(index);
            _comod.assertNotComodified();
            return _list.get(toUnderlyingIndex(index));
        }
    
        public boolean removeElementAt(int index) {
            checkRange(index);
            _comod.assertNotComodified();
            boolean val = _list.removeElementAt(toUnderlyingIndex(index));
            _limit--;
            _comod.resyncModCount();
            incrModCount();
            return val;
        }
    
        public boolean set(int index, boolean element) {
            checkRange(index);
            _comod.assertNotComodified();
            boolean val = _list.set(toUnderlyingIndex(index),element);
            incrModCount();
            _comod.resyncModCount();
            return val;
        }
    
        public void add(int index, boolean element) {
            checkRangeIncludingEndpoint(index);
            _comod.assertNotComodified();
             _list.add(toUnderlyingIndex(index),element);
            _limit++;
            _comod.resyncModCount();
            incrModCount();
        }
    
        public int size() {
            _comod.assertNotComodified();
            return _limit;
        }
    
        private void checkRange(int index) {
            if(index < 0 || index >= size()) {
                throw new IndexOutOfBoundsException("index " + index
                        + " not in [0," + size() + ")");
            }
        }
          
        private void checkRangeIncludingEndpoint(int index) {
            if(index < 0 || index > size()) {
                throw new IndexOutOfBoundsException("index " + index
                        + " not in [0," + size() + "]");
            }
        }
          
        private int toUnderlyingIndex(int index) {
            return (index + _offset);
        }
        
        private int _offset = 0;        
        private int _limit = 0; 
        private RandomAccessBooleanList _list = null;
        private ComodChecker _comod = null;
    
    }
}

