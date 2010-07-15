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

import junit.framework.TestCase;

import org.apache.commons.collections.primitives.ArrayShortList;
import org.apache.commons.collections.primitives.ShortIterator;
import org.apache.commons.collections.primitives.ShortList;
import org.apache.commons.collections.primitives.ShortListIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:32 $
 * @author Rodney Waldhoff
 */
public abstract class BaseUnmodifiableShortListTest extends TestCase {

    // conventional
    // ------------------------------------------------------------------------

    public BaseUnmodifiableShortListTest(String testName) {
        super(testName);
    }
    
    // framework
    // ------------------------------------------------------------------------

    protected abstract ShortList makeUnmodifiableShortList();

    protected ShortList makeShortList() {
        ShortList list = new ArrayShortList();
        for(short i=0;i<10;i++) {
            list.add(i);
        }
        return list;
    }

    // tests
    // ------------------------------------------------------------------------
    
    public final void testNotModifiable() throws Exception {
        assertListNotModifiable(makeUnmodifiableShortList());
    }

    public final void testSublistNotModifiable() throws Exception {
        ShortList list = makeUnmodifiableShortList();
        assertListNotModifiable(list.subList(0,list.size()-2));
    }
    
    public final void testIteratorNotModifiable() throws Exception {
        ShortList list = makeUnmodifiableShortList();
        assertIteratorNotModifiable(list.iterator());
        assertIteratorNotModifiable(list.subList(0,list.size()-2).iterator());
    }
    
    public final void testListIteratorNotModifiable() throws Exception {
        ShortList list = makeUnmodifiableShortList();
        assertListIteratorNotModifiable(list.listIterator());
        assertListIteratorNotModifiable(list.subList(0,list.size()-2).listIterator());
        assertListIteratorNotModifiable(list.listIterator(1));
        assertListIteratorNotModifiable(list.subList(0,list.size()-2).listIterator(1));
    }

    // util
    // ------------------------------------------------------------------------
    
    private void assertListIteratorNotModifiable(ShortListIterator iter) throws Exception {
        assertIteratorNotModifiable(iter);
        
        assertTrue(iter.hasPrevious());
        
        try {
            iter.set((short)2);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            iter.add((short)2);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    private void assertIteratorNotModifiable(ShortIterator iter) throws Exception {
        assertTrue(iter.hasNext());
        iter.next();
        
        try {
            iter.remove();
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    private void assertListNotModifiable(ShortList list) throws Exception {
        try {
            list.add((short)1);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            list.add(1,(short)2);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            list.addAll(makeShortList());
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            list.addAll(1,makeShortList());
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            list.removeElementAt(1);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            list.removeElement((short)1);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            list.removeAll(makeShortList());
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
                
        try {
            list.retainAll(makeShortList());
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }

        try {
            list.clear();
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            list.set(1,(short)2);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }
}