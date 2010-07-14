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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:30 $
 * @author Rodney Waldhoff
 */
public class TestByteCollections extends TestCase {

    //------------------------------------------------------------ Conventional

    public TestByteCollections(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestByteCollections.class);
    }

    //---------------------------------------------------------------- Tests

    public void testSingletonByteListIterator() {
        ByteListIterator iter = ByteCollections.singletonByteListIterator((byte)17);
        assertTrue(!iter.hasPrevious());        
        assertTrue(iter.hasNext());        
        assertEquals(17,iter.next());        
        assertTrue(iter.hasPrevious());        
        assertTrue(!iter.hasNext());        
        assertEquals(17,iter.previous());        
        try {
            iter.set((byte)18);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testSingletonByteIterator() {
        ByteIterator iter = ByteCollections.singletonByteIterator((byte)17);
        assertTrue(iter.hasNext());        
        assertEquals(17,iter.next());        
        assertTrue(!iter.hasNext());        
        try {
            iter.remove();
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testSingletonByteList() {
        ByteList list = ByteCollections.singletonByteList((byte)17);
        assertEquals(1,list.size());
        assertEquals(17,list.get(0));        
        try {
            list.add((byte)18);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableByteListNull() {
        try {
            ByteCollections.unmodifiableByteList(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyByteList() {
        assertSame(ByteCollections.EMPTY_BYTE_LIST,ByteCollections.getEmptyByteList());
        assertTrue(ByteCollections.EMPTY_BYTE_LIST.isEmpty());
        try {
            ByteCollections.EMPTY_BYTE_LIST.add((byte)1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableByteIteratorNull() {
        try {
            ByteCollections.unmodifiableByteIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyByteIterator() {
        assertSame(ByteCollections.EMPTY_BYTE_ITERATOR,ByteCollections.getEmptyByteIterator());
        assertTrue(! ByteCollections.EMPTY_BYTE_ITERATOR.hasNext());
        try {
            ByteCollections.EMPTY_BYTE_ITERATOR.remove();
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableByteListIteratorNull() {
        try {
            ByteCollections.unmodifiableByteListIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyByteListIterator() {
        assertSame(ByteCollections.EMPTY_BYTE_LIST_ITERATOR,ByteCollections.getEmptyByteListIterator());
        assertTrue(! ByteCollections.EMPTY_BYTE_LIST_ITERATOR.hasNext());
        assertTrue(! ByteCollections.EMPTY_BYTE_LIST_ITERATOR.hasPrevious());
        try {
            ByteCollections.EMPTY_BYTE_LIST_ITERATOR.add((byte)1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }
}

