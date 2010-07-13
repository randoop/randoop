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
public class TestFloatCollections extends TestCase {

    //------------------------------------------------------------ Conventional

    public TestFloatCollections(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestFloatCollections.class);
    }

    //---------------------------------------------------------------- Tests

    public void testSingletonFloatListIterator() {
        FloatListIterator iter = FloatCollections.singletonFloatListIterator((float)17);
        assertTrue(!iter.hasPrevious());        
        assertTrue(iter.hasNext());        
        assertEquals(17,iter.next(),(float)0);        
        assertTrue(iter.hasPrevious());        
        assertTrue(!iter.hasNext());        
        assertEquals(17,iter.previous(),(float)0);        
        try {
            iter.set((float)18);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testSingletonFloatIterator() {
        FloatIterator iter = FloatCollections.singletonFloatIterator((float)17);
        assertTrue(iter.hasNext());        
        assertEquals(17,iter.next(),(float)0);        
        assertTrue(!iter.hasNext());        
        try {
            iter.remove();
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testSingletonFloatList() {
        FloatList list = FloatCollections.singletonFloatList((float)17);
        assertEquals(1,list.size());
        assertEquals(17,list.get(0),(float)0);        
        try {
            list.add((float)18);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableFloatListNull() {
        try {
            FloatCollections.unmodifiableFloatList(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyFloatList() {
        assertSame(FloatCollections.EMPTY_FLOAT_LIST,FloatCollections.getEmptyFloatList());
        assertTrue(FloatCollections.EMPTY_FLOAT_LIST.isEmpty());
        try {
            FloatCollections.EMPTY_FLOAT_LIST.add((float)1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableFloatIteratorNull() {
        try {
            FloatCollections.unmodifiableFloatIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyFloatIterator() {
        assertSame(FloatCollections.EMPTY_FLOAT_ITERATOR,FloatCollections.getEmptyFloatIterator());
        assertTrue(! FloatCollections.EMPTY_FLOAT_ITERATOR.hasNext());
        try {
            FloatCollections.EMPTY_FLOAT_ITERATOR.remove();
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableFloatListIteratorNull() {
        try {
            FloatCollections.unmodifiableFloatListIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyFloatListIterator() {
        assertSame(FloatCollections.EMPTY_FLOAT_LIST_ITERATOR,FloatCollections.getEmptyFloatListIterator());
        assertTrue(! FloatCollections.EMPTY_FLOAT_LIST_ITERATOR.hasNext());
        assertTrue(! FloatCollections.EMPTY_FLOAT_LIST_ITERATOR.hasPrevious());
        try {
            FloatCollections.EMPTY_FLOAT_LIST_ITERATOR.add((float)1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }
}

