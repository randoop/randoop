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
import org.apache.commons.collections.primitives.adapters.ListLongList;
import org.apache.commons.collections.primitives.adapters.LongListList;

/**
 * @version $Revision: 1.5 $ $Date: 2004/02/25 20:46:30 $
 * @author Rodney Waldhoff
 */
public abstract class TestLongList extends BaseTestList {

    // conventional
    // ------------------------------------------------------------------------

    public TestLongList(String testName) {
        super(testName);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    // collections testing framework: long list
    // ------------------------------------------------------------------------

    protected abstract LongList makeEmptyLongList();

    protected LongList makeFullLongList() {
        LongList list = makeEmptyLongList();
        long[] values = getFullLongs();
        for(int i=0;i<values.length;i++) {
            list.add(values[i]);
        }
        return list;
    }

    protected long[] getFullLongs() {
        long[] result = new long[19];
        for(int i = 0; i < result.length; i++) {
            result[i] = (long)i + ((long)Integer.MAX_VALUE - (long)10);
        }
        return result;
    }

    protected long[] getOtherLongs() {
        long[] result = new long[16];
        for (int i = 0; i < result.length; i++) {
            result[i] = (long)i + (long)43;
        }
        return result;
    }
    
    // collections testing framework: inherited
    // ------------------------------------------------------------------------

    public List makeEmptyList() {
        return new LongListList(makeEmptyLongList());
    }
        
    public Object[] getFullElements() {
        return wrapArray(getFullLongs());
    }

    public Object[] getOtherElements() {
        return wrapArray(getOtherLongs());
    }

    // private utils
    // ------------------------------------------------------------------------

    private Long[] wrapArray(long[] primitives) {
        Long[] result = new Long[primitives.length];
        for(int i=0;i<result.length;i++) {
            result[i] = new Long(primitives[i]);            
        }
        return result;
    }

    // tests
    // ------------------------------------------------------------------------

    public void testExceptionOnConcurrentModification() {
        LongList list = makeFullLongList();
        LongIterator iter = list.iterator();
        iter.next();
        list.add((long)3);
        try {
            iter.next();
            fail("Expected ConcurrentModificationException");
        } catch(ConcurrentModificationException e) {
            // expected
        }
    }
    
    public void testAddAllLongListAtIndex() {
        LongList source = makeFullLongList();
        LongList dest = makeFullLongList();
        dest.addAll(1,source);
        
        LongIterator iter = dest.iterator();
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

    public void testToJustBigEnoughLongArray() {
        LongList list = makeFullLongList();
        long[] dest = new long[list.size()];
        assertSame(dest,list.toArray(dest));
        int i=0;
        for(LongIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest[i]);
        }
    }
    
    public void testToLargerThanNeededLongArray() {
        LongList list = makeFullLongList();
        long[] dest = new long[list.size()*2];
        for(int i=0;i<dest.length;i++) {
            dest[i] = Long.MAX_VALUE;
        }       
        assertSame(dest,list.toArray(dest));
        int i=0;
        for(LongIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest[i]);
        }
        for(;i<dest.length;i++) {
            assertEquals(Long.MAX_VALUE,dest[i]);
        }
    }
    
    public void testToSmallerThanNeededLongArray() {
        LongList list = makeFullLongList();
        long[] dest = new long[list.size()/2];
        long[] dest2 = list.toArray(dest);
        assertTrue(dest != dest2);
        int i=0;
        for(LongIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest2[i]);
        }
    }
    
    public void testHashCodeSpecification() {
        LongList list = makeFullLongList();
		int hash = 1;
		for(LongIterator iter = list.iterator(); iter.hasNext(); ) {
			long val = iter.next();
			hash = 31*hash + ((int)(val ^ (val >>> 32)));
		}
        assertEquals(hash,list.hashCode());
    }

    public void testEqualsWithTwoLongLists() {
        LongList one = makeEmptyLongList();
        assertEquals("Equals is reflexive on empty list",one,one);
        LongList two = makeEmptyLongList();
        assertEquals("Empty lists are equal",one,two);
        assertEquals("Equals is symmetric on empty lists",two,one);
        
        one.add((long)1);
        assertEquals("Equals is reflexive on non empty list",one,one);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));

        two.add((long)1);
        assertEquals("Non empty lists are equal",one,two);
        assertEquals("Equals is symmetric on non empty list",one,two);
        
        one.add((long)1); one.add((long)2); one.add((long)3); one.add((long)5); one.add((long)8);
        assertEquals("Equals is reflexive on larger non empty list",one,one);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));
        
        two.add((long)1); two.add((long)2); two.add((long)3); two.add((long)5); two.add((long)8);
        assertEquals("Larger non empty lists are equal",one,two);
        assertEquals("Equals is symmetric on larger non empty list",two,one);

        one.add((long)9);
        two.add((long)10);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));

    }

    public void testLongSubListEquals() {
        LongList one = makeEmptyLongList();
        assertEquals(one,one.subList(0,0));
        assertEquals(one.subList(0,0),one);
        
        one.add((long)1);
        assertEquals(one,one.subList(0,1));
        assertEquals(one.subList(0,1),one);

        one.add((long)1); one.add((long)2); one.add((long)3); one.add((long)5); one.add((long)8);
        assertEquals(one.subList(0,4),one.subList(0,4));
        assertEquals(one.subList(3,5),one.subList(3,5));
    }
    
    public void testEqualsWithLongListAndList() {
        LongList ilist = makeEmptyLongList();
        List list = new ArrayList();
        
        assertTrue("Unwrapped, empty List should not be equal to empty LongList.",!ilist.equals(list));
        assertTrue("Unwrapped, empty LongList should not be equal to empty List.",!list.equals(ilist));
        
        assertEquals(new ListLongList(list),ilist);
        assertEquals(ilist,new ListLongList(list));
        assertEquals(new LongListList(ilist),list);
        assertEquals(list,new LongListList(ilist));
        
        ilist.add((long)1);
        list.add(new Long(1));

        assertTrue("Unwrapped, non-empty List is not equal to non-empty LongList.",!ilist.equals(list));
        assertTrue("Unwrapped, non-empty LongList is not equal to non-empty List.",!list.equals(ilist));
        
        assertEquals(new ListLongList(list),ilist);
        assertEquals(ilist,new ListLongList(list));
        assertEquals(new LongListList(ilist),list);
        assertEquals(list,new LongListList(ilist));
                
        ilist.add(1); ilist.add(2); ilist.add(3); ilist.add(5); ilist.add(8);
        list.add(new Long(1)); list.add(new Long(2)); list.add(new Long(3)); list.add(new Long(5)); list.add(new Long(8));

        assertTrue("Unwrapped, non-empty List is not equal to non-empty LongList.",!ilist.equals(list));
        assertTrue("Unwrapped, non-empty LongList is not equal to non-empty List.",!list.equals(ilist));
        
        assertEquals(new ListLongList(list),ilist);
        assertEquals(ilist,new ListLongList(list));
        assertEquals(new LongListList(ilist),list);
        assertEquals(list,new LongListList(ilist));
        
    }

    public void testClearAndSize() {
        LongList list = makeEmptyLongList();
        assertEquals(0, list.size());
        for(int i = 0; i < 100; i++) {
            list.add((long)i);
        }
        assertEquals(100, list.size());
        list.clear();
        assertEquals(0, list.size());
    }

    public void testRemoveViaSubList() {
        LongList list = makeEmptyLongList();
        for(int i = 0; i < 100; i++) {
            list.add((long)i);
        }
        LongList sub = list.subList(25,75);
        assertEquals(50,sub.size());
        for(int i = 0; i < 50; i++) {
            assertEquals(100-i,list.size());
            assertEquals(50-i,sub.size());
            assertEquals(25+i,sub.removeElementAt(0));
            assertEquals(50-i-1,sub.size());
            assertEquals(100-i-1,list.size());
        }
        assertEquals(0,sub.size());
        assertEquals(50,list.size());        
    }
    
    public void testAddGet() {
        LongList list = makeEmptyLongList();
        for (int i = 0; i < 1000; i++) {
            list.add((long)i);
        }
        for (int i = 0; i < 1000; i++) {
            assertEquals((long)i, list.get(i));
        }
    }

    public void testAddAndShift() {
        LongList list = makeEmptyLongList();
        list.add(0, (long)1);
        assertEquals("Should have one entry", 1, list.size());
        list.add((long)3);
        list.add((long)4);
        list.add(1, (long)2);
        for(int i = 0; i < 4; i++) {
            assertEquals("Should get entry back", (long)(i + 1), list.get(i));
        }
        list.add(0, (long)0);
        for (int i = 0; i < 5; i++) {
            assertEquals("Should get entry back", (long)i, list.get(i));
        }
    }

    public void testIsSerializable() throws Exception {
        LongList list = makeFullLongList();
        assertTrue(list instanceof Serializable);
        byte[] ser = writeExternalFormToBytes((Serializable)list);
        LongList deser = (LongList)(readExternalFormFromBytes(ser));
        assertEquals(list,deser);
        assertEquals(deser,list);
    }

    public void testLongListSerializeDeserializeThenCompare() throws Exception {
        LongList list = makeFullLongList();
        if(list instanceof Serializable) {
            byte[] ser = writeExternalFormToBytes((Serializable)list);
            LongList deser = (LongList)(readExternalFormFromBytes(ser));
            assertEquals("obj != deserialize(serialize(obj))",list,deser);
        }
    }

    public void testSubListsAreNotSerializable() throws Exception {
        LongList list = makeFullLongList().subList(2,3);
        assertTrue( ! (list instanceof Serializable) );
    }

    public void testSubListOutOfBounds() throws Exception {
        try {
            makeEmptyLongList().subList(2,3);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullLongList().subList(-1,3);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }


        try {
            makeFullLongList().subList(5,2);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }

        try {
            makeFullLongList().subList(2,makeFullLongList().size()+2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testListIteratorOutOfBounds() throws Exception {
        try {
            makeEmptyLongList().listIterator(2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullLongList().listIterator(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullLongList().listIterator(makeFullLongList().size()+2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testListIteratorSetWithoutNext() throws Exception {
        LongListIterator iter = makeFullLongList().listIterator();
        try {
            iter.set(3);
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    public void testListIteratorSetAfterRemove() throws Exception {
        LongListIterator iter = makeFullLongList().listIterator();
        iter.next();
        iter.remove();
        try {            
            iter.set(3);
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

}
