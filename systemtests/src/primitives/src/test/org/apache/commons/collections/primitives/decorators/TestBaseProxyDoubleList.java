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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections.primitives.DoubleCollection;
import org.apache.commons.collections.primitives.DoubleList;
import org.apache.commons.collections.primitives.DoubleListIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:32 $
 * @author Rodney Waldhoff
 */
public class TestBaseProxyDoubleList extends TestCase {

    // conventional
    // ------------------------------------------------------------------------

    public TestBaseProxyDoubleList(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestBaseProxyDoubleList.class);
    }

    // tests
    // ------------------------------------------------------------------------
    
    public void testListCallsAreProxied() {
        final InvocationCounter proxied = new InvocationCounter();
        BaseProxyDoubleList list = new BaseProxyDoubleList() {
            protected DoubleList getProxiedList() {
                return proxied;
            }
        };
        
        assertSame(list.getProxiedList(),list.getProxiedCollection());
        
        assertEquals(0,proxied.getAddCount());
        list.add(1,(double)1);
        assertEquals(1,proxied.getAddCount());

        assertEquals(0,proxied.getAddAllCount());
        list.addAll(1,null);
        assertEquals(1,proxied.getAddAllCount());

        assertEquals(0,proxied.getGetCount());
        list.get(1);
        assertEquals(1,proxied.getGetCount());

        assertEquals(0,proxied.getIndexOfCount());
        list.indexOf((double)1);
        assertEquals(1,proxied.getIndexOfCount());

        assertEquals(0,proxied.getLastIndexOfCount());
        list.lastIndexOf((double)1);
        assertEquals(1,proxied.getLastIndexOfCount());

        assertEquals(0,proxied.getListIteratorCount());
        list.listIterator();
        assertEquals(1,proxied.getListIteratorCount());

        assertEquals(0,proxied.getListIteratorFromCount());
        list.listIterator(1);
        assertEquals(1,proxied.getListIteratorFromCount());

        assertEquals(0,proxied.getRemoveElementAtCount());
        list.removeElementAt(1);
        assertEquals(1,proxied.getRemoveElementAtCount());

        assertEquals(0,proxied.getSetCount());
        list.set(1,(double)1);
        assertEquals(1,proxied.getSetCount());

        assertEquals(0,proxied.getSubListCount());
        list.subList(1,2);
        assertEquals(1,proxied.getSubListCount());
    }
    
    // inner classes
    // ------------------------------------------------------------------------

    static class InvocationCounter extends TestBaseProxyDoubleCollection.InvocationCounter implements DoubleList {
        private int addCount;
        private int addAllCount;
        private int getCount;
        private int indexOfCount;
        private int lastIndexOfCount;
        private int listIteratorCount;
        private int listIteratorFromCount;
        private int removeElementAtCount;
        private int setCount;
        private int subListCount;
        
        public void add(int index, double element) {
            addCount++;
        }

        public boolean addAll(int index, DoubleCollection collection) {
            addAllCount++;
            return false;
        }

        public double get(int index) {
            getCount++;
            return 0;
        }

        public int indexOf(double element) {
            indexOfCount++;
            return 0;
        }

        public int lastIndexOf(double element) {
            lastIndexOfCount++;
            return 0;
        }

        public DoubleListIterator listIterator() {
            listIteratorCount++;
            return null;
        }

        public DoubleListIterator listIterator(int index) {
            listIteratorFromCount++;
            return null;
        }

        public double removeElementAt(int index) {
            removeElementAtCount++;
            return 0;
        }

        public double set(int index, double element) {
            setCount++;
            return 0;
        }

        public DoubleList subList(int fromIndex, int toIndex) {
            subListCount++;
            return null;
        }

        public int getAddAllCount() {
            return addAllCount;
        }

        public int getAddCount() {
            return addCount;
        }

        public int getGetCount() {
            return getCount;
        }

        public int getIndexOfCount() {
            return indexOfCount;
        }

        public int getLastIndexOfCount() {
            return lastIndexOfCount;
        }

        public int getListIteratorCount() {
            return listIteratorCount;
        }

        public int getListIteratorFromCount() {
            return listIteratorFromCount;
        }

        public int getRemoveElementAtCount() {
            return removeElementAtCount;
        }

        public int getSetCount() {
            return setCount;
        }

        public int getSubListCount() {
            return subListCount;
        }

    }
}