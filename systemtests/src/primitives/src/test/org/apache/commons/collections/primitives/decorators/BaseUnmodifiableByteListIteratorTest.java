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
package org.apache.commons.collections.primitives.decorators;

import org.apache.commons.collections.primitives.ArrayByteList;
import org.apache.commons.collections.primitives.ByteIterator;
import org.apache.commons.collections.primitives.ByteList;
import org.apache.commons.collections.primitives.ByteListIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:32 $
 * @author Rodney Waldhoff
 */
public abstract class BaseUnmodifiableByteListIteratorTest extends BaseUnmodifiableByteIteratorTest {

    // conventional
    // ------------------------------------------------------------------------

    public BaseUnmodifiableByteListIteratorTest(String testName) {
        super(testName);
    }
    

    // framework
    // ------------------------------------------------------------------------

    protected abstract ByteListIterator makeUnmodifiableByteListIterator();

    protected ByteIterator makeUnmodifiableByteIterator() {
        return makeUnmodifiableByteListIterator();
    }

    protected ByteIterator makeByteIterator() {
        return makeByteListIterator();
    }
    
    protected ByteListIterator makeByteListIterator() {
        ByteList list = new ArrayByteList();
        for(byte i=0;i<10;i++) {
            list.add(i);
        }
        return list.listIterator();
    }

    // tests
    // ------------------------------------------------------------------------

    public final void testByteListIteratorNotModifiable() {
        ByteListIterator iter = makeUnmodifiableByteListIterator();
        assertTrue(iter.hasNext());
        iter.next();
        try {
            iter.remove();
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        try {
            iter.add((byte)1);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        try {
            iter.set((byte)3);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    public final void testIterateByteListIterator() {        
        ByteListIterator iter = makeUnmodifiableByteListIterator();
        ByteListIterator expected = makeByteListIterator();
        
        assertTrue(! iter.hasPrevious());
        
        while( expected.hasNext() ) {
            assertTrue(iter.hasNext());
            assertEquals(expected.nextIndex(),iter.nextIndex());
            assertEquals(expected.previousIndex(),iter.previousIndex());
            assertEquals(expected.next(),iter.next());
            assertTrue(iter.hasPrevious());
            assertEquals(expected.nextIndex(),iter.nextIndex());
            assertEquals(expected.previousIndex(),iter.previousIndex());
        }

        assertTrue(! iter.hasNext() );

        while( expected.hasPrevious() ) {
            assertTrue(iter.hasPrevious());
            assertEquals(expected.nextIndex(),iter.nextIndex());
            assertEquals(expected.previousIndex(),iter.previousIndex());
            assertEquals(expected.previous(),iter.previous());
            assertTrue(iter.hasNext());
            assertEquals(expected.nextIndex(),iter.nextIndex());
            assertEquals(expected.previousIndex(),iter.previousIndex());
        }
        assertTrue(! iter.hasPrevious() );
    }

}