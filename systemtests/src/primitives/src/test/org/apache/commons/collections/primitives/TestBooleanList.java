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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.apache.commons.collections.primitives.adapters.BaseTestList;
import org.apache.commons.collections.primitives.adapters.BooleanListList;
import org.apache.commons.collections.primitives.adapters.ListBooleanList;

/**
 * @version $Revision: 1.2 $ $Date: 2004/07/12 19:51:16 $
 * @author Rodney Waldhoff
 */
public abstract class TestBooleanList extends BaseTestList {

    // conventional
    // ------------------------------------------------------------------------

    public TestBooleanList(String testName) {
        super(testName);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    // collections testing framework: boolean list
    // ------------------------------------------------------------------------

    protected abstract BooleanList makeEmptyBooleanList();

    protected BooleanList makeFullBooleanList() {
        BooleanList list = makeEmptyBooleanList();
        boolean[] values = getFullBooleans();
        for(int i=0;i<values.length;i++) {
            list.add(values[i]);
        }
        return list;
    }

    protected boolean[] getFullBooleans() {
        boolean[] result = new boolean[19];
        for(int i = 0; i < result.length; i++) {
            result[i] = true;
        }
        return result;
    }

    protected boolean[] getOtherBooleans() {
        boolean[] result = new boolean[19];
        for(int i = 0; i < result.length; i++) {
            result[i] = false;
        }
        return result;
    }
    
    // collections testing framework: inherited
    // ------------------------------------------------------------------------

    public List makeEmptyList() {
        return new BooleanListList(makeEmptyBooleanList());
    }
        
    public Object[] getFullElements() {
        return wrapArray(getFullBooleans());
    }

    public Object[] getOtherElements() {
        return wrapArray(getOtherBooleans());
    }

    // private utils
    // ------------------------------------------------------------------------

    private Boolean[] wrapArray(boolean[] primitives) {
        Boolean[] result = new Boolean[primitives.length];
        for(int i=0;i<result.length;i++) {
            result[i] = new Boolean(primitives[i]);            
        }
        return result;
    }

    // tests
    // ------------------------------------------------------------------------

    public void testExceptionOnConcurrentModification() {
        BooleanList list = makeFullBooleanList();
        BooleanIterator iter = list.iterator();
        iter.next();
        list.add(true);
        try {
            iter.next();
            fail("Expected ConcurrentModificationException");
        } catch(ConcurrentModificationException e) {
            // expected
        }
    }
    
    public void testAddAllBooleanListAtIndex() {
        BooleanList source = makeFullBooleanList();
        BooleanList dest = makeFullBooleanList();
        dest.addAll(1,source);
        
        BooleanIterator iter = dest.iterator();
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
    
    public void testToJustBigEnoughBooleanArray() {
        BooleanList list = makeFullBooleanList();
        boolean[] dest = new boolean[list.size()];
        assertSame(dest,list.toArray(dest));
        int i=0;
        for(BooleanIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest[i]);
        }
    }
    
    public void testToLargerThanNeededBooleanArray() {
        BooleanList list = makeFullBooleanList();
        boolean[] dest = new boolean[list.size()*2];
        for(int i=0;i<dest.length;i++) {
            dest[i] = true;
        }       
        assertSame(dest,list.toArray(dest));
        int i=0;
        for(BooleanIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest[i]);
        }
        for(;i<dest.length;i++) {
            assertEquals(true,dest[i]);
        }
    }
    
    public void testToSmallerThanNeededBooleanArray() {
        BooleanList list = makeFullBooleanList();
        boolean[] dest = new boolean[list.size()/2];
        boolean[] dest2 = list.toArray(dest);
        assertTrue(dest != dest2);
        int i=0;
        for(BooleanIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest2[i]);
        }
    }

    public void testHashCodeSpecification() {
        BooleanList list = makeFullBooleanList();
        int hash = 1;
        for(BooleanIterator iter = list.iterator(); iter.hasNext(); ) {
            hash = 31*hash + new Boolean(iter.next()).hashCode();
        }
        assertEquals(hash,list.hashCode());
    }
    
    public void testEqualsWithTwoBooleanLists() {
        BooleanList one = makeEmptyBooleanList();
        assertEquals("Equals is reflexive on empty list",one,one);
        BooleanList two = makeEmptyBooleanList();
        assertEquals("Empty lists are equal",one,two);
        assertEquals("Equals is symmetric on empty lists",two,one);
        
        one.add(true);
        assertEquals("Equals is reflexive on non empty list",one,one);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));

        two.add(true);
        assertEquals("Non empty lists are equal",one,two);
        assertEquals("Equals is symmetric on non empty list",one,two);
        
        one.add(true); one.add(false);
        one.add(true); one.add(false);
        one.add(true); one.add(false);
        assertEquals("Equals is reflexive on larger non empty list",one,one);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));
        
        two.add(true); two.add(false);
        two.add(true); two.add(false);
        two.add(true); two.add(false);
        assertEquals("Larger non empty lists are equal",one,two);
        assertEquals("Equals is symmetric on larger non empty list",two,one);

        one.add(true);
        two.add(false);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));

    }

    public void testBooleanSubListEquals() {
        BooleanList one = makeEmptyBooleanList();
        assertEquals(one,one.subList(0,0));
        assertEquals(one.subList(0,0),one);
        
        one.add(true);
        assertEquals(one,one.subList(0,1));
        assertEquals(one.subList(0,1),one);

        one.add(true); one.add(false); one.add(true); one.add(true); one.add(false); one.add(false);
        assertEquals(one.subList(0,4),one.subList(0,4));
        assertEquals(one.subList(3,5),one.subList(3,5));
    }
    
    public void testEqualsWithBooleanListAndList() {
        BooleanList ilist = makeEmptyBooleanList();
        List list = new ArrayList();
        
        assertTrue("Unwrapped, empty List should not be equal to empty BooleanList.",!ilist.equals(list));
        assertTrue("Unwrapped, empty BooleanList should not be equal to empty List.",!list.equals(ilist));
        
        assertEquals(new ListBooleanList(list),ilist);
        assertEquals(ilist,new ListBooleanList(list));
        assertEquals(new BooleanListList(ilist),list);
        assertEquals(list,new BooleanListList(ilist));
        
        ilist.add(true);
        list.add(new Boolean(true));

        assertTrue("Unwrapped, non-empty List is not equal to non-empty BooleanList.",!ilist.equals(list));
        assertTrue("Unwrapped, non-empty BooleanList is not equal to non-empty List.",!list.equals(ilist));
        
        assertEquals(new ListBooleanList(list),ilist);
        assertEquals(ilist,new ListBooleanList(list));
        assertEquals(new BooleanListList(ilist),list);
        assertEquals(list,new BooleanListList(ilist));
                
        ilist.add(true); ilist.add(false); ilist.add(true); ilist.add(true); ilist.add(false);
        list.add(new Boolean(true)); list.add(Boolean.FALSE); list.add(Boolean.TRUE); list.add(Boolean.TRUE); list.add(new Boolean(false));

        assertTrue("Unwrapped, non-empty List is not equal to non-empty BooleanList.",!ilist.equals(list));
        assertTrue("Unwrapped, non-empty BooleanList is not equal to non-empty List.",!list.equals(ilist));
        
        assertEquals(new ListBooleanList(list),ilist);
        assertEquals(ilist,new ListBooleanList(list));
        assertEquals(new BooleanListList(ilist),list);
        assertEquals(list,new BooleanListList(ilist));
        
    }

    public void testClearAndSize() {
        BooleanList list = makeEmptyBooleanList();
        assertEquals(0, list.size());
        for(int i = 0; i < 100; i++) {
            list.add(i%2==0);
        }
        assertEquals(100, list.size());
        list.clear();
        assertEquals(0, list.size());
    }

    public void testRemoveViaSubList() {
        BooleanList list = makeEmptyBooleanList();
        for(int i = 0; i < 100; i++) {
            list.add(i%2==0);
        }
        BooleanList sub = list.subList(25,75);
        assertEquals(50,sub.size());
        for(int i = 0; i < 50; i++) {
            assertEquals(100-i,list.size());
            assertEquals(50-i,sub.size());
            assertEquals((25+i)%2==0,sub.removeElementAt(0));
            assertEquals(50-i-1,sub.size());
            assertEquals(100-i-1,list.size());
        }
        assertEquals(0,sub.size());
        assertEquals(50,list.size());        
    }
    
    public void testAddGet() {
        BooleanList list = makeEmptyBooleanList();
        for (int i = 0; i < 255; i++) {
            list.add(i%2==0);
        }
        for (int i = 0; i < 255; i++) {
            assertEquals(i%2==0, list.get(i));
        }
    }

    public void testAddAndShift() {
        BooleanList list = makeEmptyBooleanList();
        list.add(0, true);
        assertEquals("Should have one entry", 1, list.size());
        list.add(true);
        list.add(false);
        list.add(1, false);
        for(int i = 0; i < 4; i++) {
            assertEquals("Should get entry back", (i%2==0), list.get(i));
        }
    }

    public void testIsSerializable() throws Exception {
        BooleanList list = makeFullBooleanList();
        assertTrue(list instanceof Serializable);
        byte[] ser = writeExternalFormToBytes((Serializable)list);
        BooleanList deser = (BooleanList)(readExternalFormFromBytes(ser));
        assertEquals(list,deser);
        assertEquals(deser,list);
    }

    public void testBooleanListSerializeDeserializeThenCompare() throws Exception {
        BooleanList list = makeFullBooleanList();
        if(list instanceof Serializable) {
            byte[] ser = writeExternalFormToBytes((Serializable)list);
            BooleanList deser = (BooleanList)(readExternalFormFromBytes(ser));
            assertEquals("obj != deserialize(serialize(obj))",list,deser);
        }
    }

    public void testSubListsAreNotSerializable() throws Exception {
        BooleanList list = makeFullBooleanList().subList(2,3);
        assertTrue( ! (list instanceof Serializable) );
    }

    public void testSubListOutOfBounds() throws Exception {
        try {
            makeEmptyBooleanList().subList(2,3);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullBooleanList().subList(-1,3);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }


        try {
            makeFullBooleanList().subList(5,2);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }

        try {
            makeFullBooleanList().subList(2,makeFullBooleanList().size()+2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testListIteratorOutOfBounds() throws Exception {
        try {
            makeEmptyBooleanList().listIterator(2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullBooleanList().listIterator(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullBooleanList().listIterator(makeFullBooleanList().size()+2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testListIteratorSetWithoutNext() throws Exception {
        BooleanListIterator iter = makeFullBooleanList().listIterator();
        try {
            iter.set(true);
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    public void testListIteratorSetAfterRemove() throws Exception {
        BooleanListIterator iter = makeFullBooleanList().listIterator();
        iter.next();
        iter.remove();
        try {            
            iter.set(true);
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    public void testCollectionRemoveAll() {
        // Super's impl doesn't work because there are only two unique values in my list.
        BooleanList trueList = new ArrayBooleanList();
        trueList.add(true);
        trueList.add(true);
        trueList.add(true);
        trueList.add(true);
        trueList.add(true);
        BooleanList falseList = new ArrayBooleanList();
        falseList.add(false);
        falseList.add(false);
        falseList.add(false);
        falseList.add(false);
        falseList.add(false);

        BooleanList list = new ArrayBooleanList();
        assertTrue(list.isEmpty());
        assertFalse(list.removeAll(trueList));        
        assertTrue(list.isEmpty());
        
        list.add(false);
        list.add(false);
        assertEquals(2,list.size());
        assertFalse(list.removeAll(trueList));        
        assertEquals(2,list.size());
        
        list.add(true);
        list.add(false);
        list.add(true);
        assertEquals(5,list.size());
        assertTrue(list.removeAll(trueList));        
        assertEquals(3,list.size());
        
        for(BooleanIterator iter = list.iterator(); iter.hasNext();) {
            assertEquals(false,iter.next());
        }

        assertFalse(list.removeAll(trueList));        
        assertEquals(3,list.size());
        
        for(BooleanIterator iter = list.iterator(); iter.hasNext();) {
            assertEquals(false,iter.next());
        }

        assertTrue(list.removeAll(falseList));        
        assertTrue(list.isEmpty());
    }

    public void testCollectionRetainAll() {
        // Super's impl doesn't work because there are only two unique values in my list.
        BooleanList trueList = new ArrayBooleanList();
        trueList.add(true);
        BooleanList falseList = new ArrayBooleanList();
        falseList.add(false);

        BooleanList list = new ArrayBooleanList();
        assertTrue(list.isEmpty());
        assertFalse(list.retainAll(falseList));        
        assertTrue(list.isEmpty());
        
        list.add(false);
        list.add(false);
        assertEquals(2,list.size());
        assertFalse(list.retainAll(falseList));        
        assertEquals(2,list.size());
        
        list.add(true);
        list.add(false);
        list.add(true);
        assertEquals(5,list.size());
        assertTrue(list.retainAll(falseList));        
        assertEquals(3,list.size());
        
        for(BooleanIterator iter = list.iterator(); iter.hasNext();) {
            assertEquals(false,iter.next());
        }

        assertFalse(list.retainAll(falseList));        
        assertEquals(3,list.size());
        
        for(BooleanIterator iter = list.iterator(); iter.hasNext();) {
            assertEquals(false,iter.next());
        }

        assertTrue(list.retainAll(trueList));        
        assertTrue(list.isEmpty());
    }

    public void testListEquals() {
        // Super type doesn't work because there are only two unique values in my list.
    }
}