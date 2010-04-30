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
public class TestCharCollections extends TestCase {

    //------------------------------------------------------------ Conventional

    public TestCharCollections(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestCharCollections.class);
    }

    //---------------------------------------------------------------- Tests

    public void testSingletonCharListIterator() {
        CharListIterator iter = CharCollections.singletonCharListIterator((char)17);
        assertTrue(!iter.hasPrevious());        
        assertTrue(iter.hasNext());        
        assertEquals(17,iter.next());        
        assertTrue(iter.hasPrevious());        
        assertTrue(!iter.hasNext());        
        assertEquals(17,iter.previous());        
        try {
            iter.set((char)18);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testSingletonCharIterator() {
        CharIterator iter = CharCollections.singletonCharIterator((char)17);
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

    public void testSingletonCharList() {
        CharList list = CharCollections.singletonCharList((char)17);
        assertEquals(1,list.size());
        assertEquals(17,list.get(0));        
        try {
            list.add((char)18);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableCharListNull() {
        try {
            CharCollections.unmodifiableCharList(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyCharList() {
        assertSame(CharCollections.EMPTY_CHAR_LIST,CharCollections.getEmptyCharList());
        assertTrue(CharCollections.EMPTY_CHAR_LIST.isEmpty());
        try {
            CharCollections.EMPTY_CHAR_LIST.add((char)1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableCharIteratorNull() {
        try {
            CharCollections.unmodifiableCharIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyCharIterator() {
        assertSame(CharCollections.EMPTY_CHAR_ITERATOR,CharCollections.getEmptyCharIterator());
        assertTrue(! CharCollections.EMPTY_CHAR_ITERATOR.hasNext());
        try {
            CharCollections.EMPTY_CHAR_ITERATOR.remove();
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public void testUnmodifiableCharListIteratorNull() {
        try {
            CharCollections.unmodifiableCharListIterator(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testEmptyCharListIterator() {
        assertSame(CharCollections.EMPTY_CHAR_LIST_ITERATOR,CharCollections.getEmptyCharListIterator());
        assertTrue(! CharCollections.EMPTY_CHAR_LIST_ITERATOR.hasNext());
        assertTrue(! CharCollections.EMPTY_CHAR_LIST_ITERATOR.hasPrevious());
        try {
            CharCollections.EMPTY_CHAR_LIST_ITERATOR.add((char)1);
            fail("Expected UnsupportedOperationExcpetion");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }
}

