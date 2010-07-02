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
public class TestDoubleCollections extends TestCase {

    //------------------------------------------------------------ Conventional

    public TestDoubleCollections(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestDoubleCollections.class);
    }

    //---------------------------------------------------------------- Tests

    public void testSingletonDoubleListIterator() {
        DoubleListIterator iter = DoubleCollections.singletonDoubleListIterator((double)17);
        assertTrue(!iter.hasPrevious());        
        assertTrue(iter.hasNext());        
        assertEquals(17,iter.next(),(double)0);        
        assertTrue(iter.hasPrevious());        
        assertTrue(!iter.hasNext());        
        assertEquals(17,iter.previous(),(double)0);        
        try {
            iter.set((double)18);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testSingletonDoubleIterator() {
        DoubleIterator iter = DoubleCollections.singletonDoubleIterator((double)17);
        assertTrue(iter.hasNext());        
        assertEquals(17,iter.next(),(double)0);        
        assertTrue(!iter.hasNext());        
        try {
            iter.remove();
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testSingletonDoubleList() {
        DoubleList list = DoubleCollections.singletonDoubleList((double)17);
        assertEquals(1,list.size());
        assertEquals(17,list.get(0),(double)0);        
        try {
            list.add((double)18);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableDoubleListNull() {
        try {
            DoubleCollections.unmodifiableDoubleList(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyDoubleList() {
        assertSame(DoubleCollections.EMPTY_DOUBLE_LIST,DoubleCollections.getEmptyDoubleList());
        assertTrue(DoubleCollections.EMPTY_DOUBLE_LIST.isEmpty());
        try {
            DoubleCollections.EMPTY_DOUBLE_LIST.add((double)1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableDoubleIteratorNull() {
        try {
            DoubleCollections.unmodifiableDoubleIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyDoubleIterator() {
        assertSame(DoubleCollections.EMPTY_DOUBLE_ITERATOR,DoubleCollections.getEmptyDoubleIterator());
        assertTrue(! DoubleCollections.EMPTY_DOUBLE_ITERATOR.hasNext());
        try {
            DoubleCollections.EMPTY_DOUBLE_ITERATOR.remove();
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableDoubleListIteratorNull() {
        try {
            DoubleCollections.unmodifiableDoubleListIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyDoubleListIterator() {
        assertSame(DoubleCollections.EMPTY_DOUBLE_LIST_ITERATOR,DoubleCollections.getEmptyDoubleListIterator());
        assertTrue(! DoubleCollections.EMPTY_DOUBLE_LIST_ITERATOR.hasNext());
        assertTrue(! DoubleCollections.EMPTY_DOUBLE_LIST_ITERATOR.hasPrevious());
        try {
            DoubleCollections.EMPTY_DOUBLE_LIST_ITERATOR.add((double)1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }
}

