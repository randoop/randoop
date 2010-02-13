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
package org.apache.commons.collections.primitives.decorators;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections.primitives.LongCollection;
import org.apache.commons.collections.primitives.LongIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:32 $
 * @author Rodney Waldhoff
 */
public class TestBaseProxyLongCollection extends TestCase {

    // conventional
    // ------------------------------------------------------------------------

    public TestBaseProxyLongCollection(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestBaseProxyLongCollection.class);
    }

    // tests
    // ------------------------------------------------------------------------
    
    public void testCollectionCallsAreProxied() {
        final InvocationCounter proxied = new InvocationCounter();
        LongCollection collection = new BaseProxyLongCollection() {
            protected LongCollection getProxiedCollection() {
                return proxied;
            }
        };
        
        assertEquals(0,proxied.getAddCount());
        collection.add(1);
        assertEquals(1,proxied.getAddCount());

        assertEquals(0,proxied.getAddAllCount());
        collection.addAll(null);
        assertEquals(1,proxied.getAddAllCount());
        
        assertEquals(0,proxied.getClearCount());
        collection.clear();
        assertEquals(1,proxied.getClearCount());

        assertEquals(0,proxied.getContainsCount());
        collection.contains(1);
        assertEquals(1,proxied.getContainsCount());

        assertEquals(0,proxied.getContainsAllCount());
        collection.containsAll(null);
        assertEquals(1,proxied.getContainsAllCount());

        assertEquals(0,proxied.getIsEmptyCount());
        collection.isEmpty();
        assertEquals(1,proxied.getIsEmptyCount());

        assertEquals(0,proxied.getIteratorCount());
        collection.iterator();
        assertEquals(1,proxied.getIteratorCount());

        assertEquals(0,proxied.getRemoveAllCount());
        collection.removeAll(null);
        assertEquals(1,proxied.getRemoveAllCount());

        assertEquals(0,proxied.getRetainAllCount());
        collection.retainAll(null);
        assertEquals(1,proxied.getRetainAllCount());

        assertEquals(0,proxied.getRemoveElementCount());
        collection.removeElement(1);
        assertEquals(1,proxied.getRemoveElementCount());

        assertEquals(0,proxied.getSizeCount());
        collection.size();
        assertEquals(1,proxied.getSizeCount());

        assertEquals(0,proxied.getToArrayLongArrayCount());
        collection.toArray(new long[0]);
        assertEquals(1,proxied.getToArrayLongArrayCount());
        
        assertEquals(0,proxied.getToArrayCount());
        collection.toArray();
        assertEquals(1,proxied.getToArrayCount());
        
        assertEquals(0,proxied.getToStringCount());
        collection.toString();
        assertEquals(1,proxied.getToStringCount());
        
        assertEquals(0,proxied.getEqualsCount());
        collection.equals(null);
        assertEquals(1,proxied.getEqualsCount());
        
        assertEquals(0,proxied.getHashCodeCount());
        collection.hashCode();
        assertEquals(1,proxied.getHashCodeCount());
        
    }
    
    // inner classes
    // ------------------------------------------------------------------------

    static class InvocationCounter implements LongCollection {
        private int _toArrayLongArray;
        private int _toArray;
        private int _size;
        private int _retainAll;
        private int _removeElement;
        private int _removeAll;
        private int _iterator;
        private int _isEmpty;
        private int _containsAll;
        private int _contains;
        private int _clear;
        private int _addAll;
        private int _add;

        private int _equals;
        private int _toString;
        private int _hashCode;

        public boolean add(long element) {
            _add++;
            return false;
        }

        public boolean addAll(LongCollection c) {
            _addAll++;
            return false;
        }

        public void clear() {
            _clear++;
        }

        public boolean contains(long element) {
            _contains++;
            return false;
        }

        public boolean containsAll(LongCollection c) {
            _containsAll++;
            return false;
        }

        public boolean isEmpty() {
            _isEmpty++;
            return false;
        }

        public LongIterator iterator() {
            _iterator++;
            return null;
        }

        public boolean removeAll(LongCollection c) {
            _removeAll++;
            return false;
        }

        public boolean removeElement(long element) {
            _removeElement++;
            return false;
        }

        public boolean retainAll(LongCollection c) {
            _retainAll++;
            return false;
        }

        public int size() {
            _size++;
            return 0;
        }

        public long[] toArray() {
            _toArray++;
            return null;
        }

        public long[] toArray(long[] a) {
            _toArrayLongArray++;
            return null;
        }

        public boolean equals(Object obj) {
            _equals++;
            return false;
        }

        public int hashCode() {
            _hashCode++;
            return 0;
        }

        public String toString() {
            _toString++;
            return null;
        }


        public int getAddCount() {
            return _add;
        }

        public int getAddAllCount() {
            return _addAll;
        }

        public int getClearCount() {
            return _clear;
        }

        public int getContainsCount() {
            return _contains;
        }

        public int getContainsAllCount() {
            return _containsAll;
        }

        public int getIsEmptyCount() {
            return _isEmpty;
        }

        public int getIteratorCount() {
            return _iterator;
        }

        public int getRemoveAllCount() {
            return _removeAll;
        }

        public int getRemoveElementCount() {
            return _removeElement;
        }

        public int getRetainAllCount() {
            return _retainAll;
        }

        public int getSizeCount() {
            return _size;
        }

        public int getToArrayCount() {
            return _toArray;
        }

        public int getToArrayLongArrayCount() {
            return _toArrayLongArray;
        }

        public int getEqualsCount() {
            return _equals;
        }

        public int getHashCodeCount() {
            return _hashCode;
        }

        public int getToStringCount() {
            return _toString;
        }

    }
}