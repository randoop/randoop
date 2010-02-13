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

import org.apache.commons.collections.primitives.ArrayFloatList;
import org.apache.commons.collections.primitives.FloatIterator;
import org.apache.commons.collections.primitives.FloatList;
import org.apache.commons.collections.primitives.FloatListIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:32 $
 * @author Rodney Waldhoff
 */
public abstract class BaseUnmodifiableFloatListTest extends TestCase {

    // conventional
    // ------------------------------------------------------------------------

    public BaseUnmodifiableFloatListTest(String testName) {
        super(testName);
    }
    
    // framework
    // ------------------------------------------------------------------------

    protected abstract FloatList makeUnmodifiableFloatList();

    protected FloatList makeFloatList() {
        FloatList list = new ArrayFloatList();
        for(float i=0;i<10;i++) {
            list.add(i);
        }
        return list;
    }

    // tests
    // ------------------------------------------------------------------------
    
    public final void testNotModifiable() throws Exception {
        assertListNotModifiable(makeUnmodifiableFloatList());
    }

    public final void testSublistNotModifiable() throws Exception {
        FloatList list = makeUnmodifiableFloatList();
        assertListNotModifiable(list.subList(0,list.size()-2));
    }
    
    public final void testIteratorNotModifiable() throws Exception {
        FloatList list = makeUnmodifiableFloatList();
        assertIteratorNotModifiable(list.iterator());
        assertIteratorNotModifiable(list.subList(0,list.size()-2).iterator());
    }
    
    public final void testListIteratorNotModifiable() throws Exception {
        FloatList list = makeUnmodifiableFloatList();
        assertListIteratorNotModifiable(list.listIterator());
        assertListIteratorNotModifiable(list.subList(0,list.size()-2).listIterator());
        assertListIteratorNotModifiable(list.listIterator(1));
        assertListIteratorNotModifiable(list.subList(0,list.size()-2).listIterator(1));
    }

    // util
    // ------------------------------------------------------------------------
    
    private void assertListIteratorNotModifiable(FloatListIterator iter) throws Exception {
        assertIteratorNotModifiable(iter);
        
        assertTrue(iter.hasPrevious());
        
        try {
            iter.set((float)2);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            iter.add((float)2);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    private void assertIteratorNotModifiable(FloatIterator iter) throws Exception {
        assertTrue(iter.hasNext());
        iter.next();
        
        try {
            iter.remove();
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    private void assertListNotModifiable(FloatList list) throws Exception {
        try {
            list.add((float)1);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            list.add(1,(float)2);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            list.addAll(makeFloatList());
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            list.addAll(1,makeFloatList());
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
            list.removeElement((float)1);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
        
        try {
            list.removeAll(makeFloatList());
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
                
        try {
            list.retainAll(makeFloatList());
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
            list.set(1,(float)2);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }
}