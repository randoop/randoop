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
public class TestIntCollections extends TestCase {

    //------------------------------------------------------------ Conventional

    public TestIntCollections(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestIntCollections.class);
    }

    //---------------------------------------------------------------- Tests

    public void testSingletonIntListIterator() {
        IntListIterator iter = IntCollections.singletonIntListIterator(17);
        assertTrue(!iter.hasPrevious());        
        assertTrue(iter.hasNext());        
        assertEquals(17,iter.next());        
        assertTrue(iter.hasPrevious());        
        assertTrue(!iter.hasNext());        
        assertEquals(17,iter.previous());        
        try {
            iter.set(18);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testSingletonIntIterator() {
        IntIterator iter = IntCollections.singletonIntIterator(17);
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

    public void testSingletonIntList() {
        IntList list = IntCollections.singletonIntList(17);
        assertEquals(1,list.size());
        assertEquals(17,list.get(0));        
        try {
            list.add(18);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableIntListNull() {
        try {
            IntCollections.unmodifiableIntList(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyIntList() {
        assertSame(IntCollections.EMPTY_INT_LIST,IntCollections.getEmptyIntList());
        assertTrue(IntCollections.EMPTY_INT_LIST.isEmpty());
        try {
            IntCollections.EMPTY_INT_LIST.add(1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableIntIteratorNull() {
        try {
            IntCollections.unmodifiableIntIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyIntIterator() {
        assertSame(IntCollections.EMPTY_INT_ITERATOR,IntCollections.getEmptyIntIterator());
        assertTrue(! IntCollections.EMPTY_INT_ITERATOR.hasNext());
        try {
            IntCollections.EMPTY_INT_ITERATOR.remove();
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableIntListIteratorNull() {
        try {
            IntCollections.unmodifiableIntListIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyIntListIterator() {
        assertSame(IntCollections.EMPTY_INT_LIST_ITERATOR,IntCollections.getEmptyIntListIterator());
        assertTrue(! IntCollections.EMPTY_INT_LIST_ITERATOR.hasNext());
        assertTrue(! IntCollections.EMPTY_INT_LIST_ITERATOR.hasPrevious());
        try {
            IntCollections.EMPTY_INT_LIST_ITERATOR.add(1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }
}

