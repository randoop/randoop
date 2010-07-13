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
package org.apache.commons.collections.primitives.adapters;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.iterators.AbstractTestListIterator;
import org.apache.commons.collections.primitives.ArrayShortList;
import org.apache.commons.collections.primitives.ShortList;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:29 $
 * @author Rodney Waldhoff
 */
public class TestShortListIteratorListIterator extends AbstractTestListIterator {

    // conventional
    // ------------------------------------------------------------------------

    public TestShortListIteratorListIterator(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestShortListIteratorListIterator.class);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    public ListIterator makeEmptyListIterator() {
        return ShortListIteratorListIterator.wrap(makeEmptyShortList().listIterator());
    }
    
    public ListIterator makeFullListIterator() {
        return ShortListIteratorListIterator.wrap(makeFullShortList().listIterator());
    }

    protected ShortList makeEmptyShortList() {
        return new ArrayShortList();
    }
    
    protected ShortList makeFullShortList() {
        ShortList list = makeEmptyShortList();
        short[] elts = getFullElements();
        for(int i=0;i<elts.length;i++) {
            list.add((short)elts[i]);
        }
        return list;
    }
    
    public short[] getFullElements() {
        return new short[] { (short)0, (short)1, (short)2, (short)3, (short)4, (short)5, (short)6, (short)7, (short)8, (short)9 };
    }
    
    public Object addSetValue() {
        return new Short((short)1);
    }

    // tests
    // ------------------------------------------------------------------------

    
    public void testNextHasNextRemove() {
        short[] elements = getFullElements();
        Iterator iter = makeFullIterator();
        for(int i=0;i<elements.length;i++) {
            assertTrue(iter.hasNext());
            assertEquals(new Short(elements[i]),iter.next());
            if(supportsRemove()) {
                iter.remove();
            }
        }        
        assertTrue(! iter.hasNext() );
    }

    public void testEmptyIterator() {
        assertTrue( ! makeEmptyIterator().hasNext() );
        try {
            makeEmptyIterator().next();
            fail("Expected NoSuchElementException");
        } catch(NoSuchElementException e) {
            // expected
        }
        if(supportsRemove()) {
            try {
                makeEmptyIterator().remove();
                fail("Expected IllegalStateException");
            } catch(IllegalStateException e) {
                // expected
            }
        }        
    }

    public void testRemoveBeforeNext() {
        if(supportsRemove()) {
            try {
                makeFullIterator().remove();
                fail("Expected IllegalStateException");
            } catch(IllegalStateException e) {
                // expected
            }
        }        
    }

    public void testRemoveAfterRemove() {
        if(supportsRemove()) {
            Iterator iter = makeFullIterator();
            iter.next();
            iter.remove();
            try {
                iter.remove();
                fail("Expected IllegalStateException");
            } catch(IllegalStateException e) {
                // expected
            }
        }        
    }

}
