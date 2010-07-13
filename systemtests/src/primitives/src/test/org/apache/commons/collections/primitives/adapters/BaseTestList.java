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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.BulkTest;
import org.apache.commons.collections.list.AbstractTestList;

/**
 * @version $Revision: 1.4 $ $Date: 2004/07/29 22:30:55 $
 * @author Rodney Waldhoff
 */
public abstract class BaseTestList extends AbstractTestList {

    // conventional
    // ------------------------------------------------------------------------

    public BaseTestList(String testName) {
        super(testName);
    }

    // tests
    // ------------------------------------------------------------------------

    public final void testAddAllAtIndex() {
        List source = makeFullList();
        List dest = makeFullList();
        
        dest.addAll(1,source);
         
        Iterator iter = dest.iterator();
        assertTrue(iter.hasNext());
        assertEquals(source.get(0),iter.next());
        for(int i=0;i<source.size();i++) {
            assertTrue(iter.hasNext());
            assertEquals(source.get(i),iter.next());
        }
        for(int i=1;i<source.size();i++) {
            assertTrue(iter.hasNext());
            assertEquals(source.get(i),iter.next());
        }
        assertFalse(iter.hasNext());
    }

    /**
     * Override to change assertSame to assertEquals.
     */
    public void testListListIteratorPreviousRemove() {
        if (isRemoveSupported() == false) return;
        resetFull();
        ListIterator it = getList().listIterator();
        Object zero = it.next();
        Object one = it.next();
        Object two = it.next();
        Object two2 = it.previous();
        Object one2 = it.previous();
        assertEquals(one, one2);
        assertEquals(two, two2);
        assertEquals(zero, getList().get(0));
        assertEquals(one, getList().get(1));
        assertEquals(two, getList().get(2));
        it.remove();
        assertEquals(zero, getList().get(0));
        assertEquals(two, getList().get(1));
    }

    /**
     * Override to change assertSame to assertEquals.
     */
    public BulkTest bulkTestSubList() {
        if (getFullElements().length - 6 < 10) return null;
        return new PrimitiveBulkTestSubList(this);
    }


    /**
     * Whole class copied as sub list constructor was package scoped in 3.1.
     */
    public static class PrimitiveBulkTestSubList extends BaseTestList {
        private BaseTestList outer;
    
        PrimitiveBulkTestSubList(BaseTestList outer) {
            super("");
            this.outer = outer;
        }
    
        public Object[] getFullElements() {
            List l = Arrays.asList(outer.getFullElements());
            return l.subList(3, l.size() - 3).toArray();
        }
        public Object[] getOtherElements() {
            return outer.getOtherElements();
        }
        public boolean isAddSupported() {
            return outer.isAddSupported();
        }
        public boolean isSetSupported() {
            return outer.isSetSupported();
        }
        public boolean isRemoveSupported() {
            return outer.isRemoveSupported();
        }
    
        public List makeEmptyList() {
            return outer.makeFullList().subList(4, 4);
        }
        public List makeFullList() {
            int size = getFullElements().length;
            return outer.makeFullList().subList(3, size - 3);
        }
        public void resetEmpty() {
            outer.resetFull();
            this.collection = outer.getList().subList(4, 4);
            this.confirmed = outer.getConfirmedList().subList(4, 4);
        }
        public void resetFull() {
            outer.resetFull();
            int size = outer.confirmed.size();
            this.collection = outer.getList().subList(3, size - 3);
            this.confirmed = outer.getConfirmedList().subList(3, size - 3);
        }
        public void verify() {
            super.verify();
            outer.verify();
        }
        public boolean isTestSerialization() {
            return false;
        }
        /**
         * Override to change assertSame to assertEquals.
         */
        public void testListListIteratorPreviousRemove() {
            if (isRemoveSupported() == false)
                return;
            resetFull();
            ListIterator it = getList().listIterator();
            Object zero = it.next();
            Object one = it.next();
            Object two = it.next();
            Object two2 = it.previous();
            Object one2 = it.previous();
            assertEquals(one, one2);
            assertEquals(two, two2);
            assertEquals(zero, getList().get(0));
            assertEquals(one, getList().get(1));
            assertEquals(two, getList().get(2));
            it.remove();
            assertEquals(zero, getList().get(0));
            assertEquals(two, getList().get(1));
        }
    }
}
