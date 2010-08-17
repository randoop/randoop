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
public class TestLongCollections extends TestCase {

    //------------------------------------------------------------ Conventional

    public TestLongCollections(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestLongCollections.class);
    }

    //---------------------------------------------------------------- Tests

    public void testSingletonLongListIterator() {
        LongListIterator iter = LongCollections.singletonLongListIterator(17);
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

    public void testSingletonLongIterator() {
        LongIterator iter = LongCollections.singletonLongIterator(17);
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

    public void testSingletonLongList() {
        LongList list = LongCollections.singletonLongList(17);
        assertEquals(1,list.size());
        assertEquals(17,list.get(0));        
        try {
            list.add(18);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableLongListNull() {
        try {
            LongCollections.unmodifiableLongList(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyLongList() {
        assertSame(LongCollections.EMPTY_LONG_LIST,LongCollections.getEmptyLongList());
        assertTrue(LongCollections.EMPTY_LONG_LIST.isEmpty());
        try {
            LongCollections.EMPTY_LONG_LIST.add(1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableLongIteratorNull() {
        try {
            LongCollections.unmodifiableLongIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyLongIterator() {
        assertSame(LongCollections.EMPTY_LONG_ITERATOR,LongCollections.getEmptyLongIterator());
        assertTrue(! LongCollections.EMPTY_LONG_ITERATOR.hasNext());
        try {
            LongCollections.EMPTY_LONG_ITERATOR.remove();
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableLongListIteratorNull() {
        try {
            LongCollections.unmodifiableLongListIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyLongListIterator() {
        assertSame(LongCollections.EMPTY_LONG_LIST_ITERATOR,LongCollections.getEmptyLongListIterator());
        assertTrue(! LongCollections.EMPTY_LONG_LIST_ITERATOR.hasNext());
        assertTrue(! LongCollections.EMPTY_LONG_LIST_ITERATOR.hasPrevious());
        try {
            LongCollections.EMPTY_LONG_LIST_ITERATOR.add(1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }
}

