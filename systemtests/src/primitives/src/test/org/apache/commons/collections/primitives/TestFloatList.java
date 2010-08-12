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
import org.apache.commons.collections.primitives.adapters.FloatListList;
import org.apache.commons.collections.primitives.adapters.ListFloatList;

/**
 * @version $Revision: 1.5 $ $Date: 2004/02/25 20:46:30 $
 * @author Rodney Waldhoff
 */
public abstract class TestFloatList extends BaseTestList {

    // conventional
    // ------------------------------------------------------------------------

    public TestFloatList(String testName) {
        super(testName);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    // collections testing framework: float list
    // ------------------------------------------------------------------------

    protected abstract FloatList makeEmptyFloatList();

    protected FloatList makeFullFloatList() {
        FloatList list = makeEmptyFloatList();
        float[] values = getFullFloats();
        for(int i=0;i<values.length;i++) {
            list.add(values[i]);
        }
        return list;
    }

    protected float[] getFullFloats() {
        float[] result = new float[19];
        for(int i = 0; i < result.length; i++) {
            result[i] = (float)(i);
        }
        return result;
    }

    protected float[] getOtherFloats() {
        float[] result = new float[16];
        for(int i = 0; i < result.length; i++) {
            result[i] = (float)(i + 43);
        }
        return result;
    }
    
    // collections testing framework: inherited
    // ------------------------------------------------------------------------

    public List makeEmptyList() {
        return new FloatListList(makeEmptyFloatList());
    }
        
    public Object[] getFullElements() {
        return wrapArray(getFullFloats());
    }

    public Object[] getOtherElements() {
        return wrapArray(getOtherFloats());
    }

    // private utils
    // ------------------------------------------------------------------------

    private Float[] wrapArray(float[] primitives) {
        Float[] result = new Float[primitives.length];
        for(int i=0;i<result.length;i++) {
            result[i] = new Float(primitives[i]);            
        }
        return result;
    }

    // tests
    // ------------------------------------------------------------------------

    public void testExceptionOnConcurrentModification() {
        FloatList list = makeFullFloatList();
        FloatIterator iter = list.iterator();
        iter.next();
        list.add((float)3);
        try {
            iter.next();
            fail("Expected ConcurrentModificationException");
        } catch(ConcurrentModificationException e) {
            // expected
        }
    }
    
    public void testAddAllFloatListAtIndex() {
        FloatList source = makeFullFloatList();
        FloatList dest = makeFullFloatList();
        dest.addAll(1,source);
        
        FloatIterator iter = dest.iterator();
        assertTrue(iter.hasNext());
        assertEquals(source.get(0),iter.next(),0d);
        for(int i=0;i<source.size();i++) {
            assertTrue(iter.hasNext());
            assertEquals(source.get(i),iter.next(),0d);
        }
        for(int i=1;i<source.size();i++) {
            assertTrue(iter.hasNext());
            assertEquals(source.get(i),iter.next(),0d);
        }
        assertFalse(iter.hasNext());
    }

    public void testToJustBigEnoughFloatArray() {
        FloatList list = makeFullFloatList();
        float[] dest = new float[list.size()];
        assertSame(dest,list.toArray(dest));
        int i=0;
        for(FloatIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest[i], 0f);
        }
    }
    
    public void testToLargerThanNeededFloatArray() {
        FloatList list = makeFullFloatList();
        float[] dest = new float[list.size()*2];
        for(int i=0;i<dest.length;i++) {
            dest[i] = Float.MAX_VALUE;
        }       
        assertSame(dest,list.toArray(dest));
        int i=0;
        for(FloatIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest[i], 0f);
        }
        for(;i<dest.length;i++) {
            assertEquals(Float.MAX_VALUE,dest[i], 0f);
        }
    }
    
    public void testToSmallerThanNeededFloatArray() {
        FloatList list = makeFullFloatList();
        float[] dest = new float[list.size()/2];
        float[] dest2 = list.toArray(dest);
        assertTrue(dest != dest2);
        int i=0;
        for(FloatIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest2[i], 0f);
        }
    }
    
    public void testHashCodeSpecification() {
        FloatList list = makeFullFloatList();
		int hash = 1;
		for(FloatIterator iter = list.iterator(); iter.hasNext(); ) {
			hash = 31*hash + Float.floatToIntBits(iter.next());
		}
        assertEquals(hash,list.hashCode());
    }

    public void testEqualsWithTwoFloatLists() {
        FloatList one = makeEmptyFloatList();
        assertEquals("Equals is reflexive on empty list",one,one);
        FloatList two = makeEmptyFloatList();
        assertEquals("Empty lists are equal",one,two);
        assertEquals("Equals is symmetric on empty lists",two,one);
        
        one.add((float)1);
        assertEquals("Equals is reflexive on non empty list",one,one);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));

        two.add((float)1);
        assertEquals("Non empty lists are equal",one,two);
        assertEquals("Equals is symmetric on non empty list",one,two);
        
        one.add((float)1); one.add((float)2); one.add((float)3); one.add((float)5); one.add((float)8);
        assertEquals("Equals is reflexive on larger non empty list",one,one);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));
        
        two.add((float)1); two.add((float)2); two.add((float)3); two.add((float)5); two.add((float)8);
        assertEquals("Larger non empty lists are equal",one,two);
        assertEquals("Equals is symmetric on larger non empty list",two,one);

        one.add((float)9);
        two.add((float)10);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));

    }

    public void testFloatSubListEquals() {
        FloatList one = makeEmptyFloatList();
        assertEquals(one,one.subList(0,0));
        assertEquals(one.subList(0,0),one);
        
        one.add((float)1);
        assertEquals(one,one.subList(0,1));
        assertEquals(one.subList(0,1),one);

        one.add((float)1); one.add((float)2); one.add((float)3); one.add((float)5); one.add((float)8);
        assertEquals(one.subList(0,4),one.subList(0,4));
        assertEquals(one.subList(3,5),one.subList(3,5));
    }
    
    public void testEqualsWithFloatListAndList() {
        FloatList ilist = makeEmptyFloatList();
        List list = new ArrayList();
        
        assertTrue("Unwrapped, empty List should not be equal to empty FloatList.",!ilist.equals(list));
        assertTrue("Unwrapped, empty FloatList should not be equal to empty List.",!list.equals(ilist));
        
        assertEquals(new ListFloatList(list),ilist);
        assertEquals(ilist,new ListFloatList(list));
        assertEquals(new FloatListList(ilist),list);
        assertEquals(list,new FloatListList(ilist));
        
        ilist.add((float)1);
        list.add(new Float((float)1));

        assertTrue("Unwrapped, non-empty List is not equal to non-empty FloatList.",!ilist.equals(list));
        assertTrue("Unwrapped, non-empty FloatList is not equal to non-empty List.",!list.equals(ilist));
        
        assertEquals(new ListFloatList(list),ilist);
        assertEquals(ilist,new ListFloatList(list));
        assertEquals(new FloatListList(ilist),list);
        assertEquals(list,new FloatListList(ilist));
                
        ilist.add((float)1); ilist.add((float)2); ilist.add((float)3); ilist.add((float)5); ilist.add((float)8);
        list.add(new Float((float)1)); list.add(new Float((float)2)); list.add(new Float((float)3)); list.add(new Float((float)5)); list.add(new Float((float)8));

        assertTrue("Unwrapped, non-empty List is not equal to non-empty FloatList.",!ilist.equals(list));
        assertTrue("Unwrapped, non-empty FloatList is not equal to non-empty List.",!list.equals(ilist));
        
        assertEquals(new ListFloatList(list),ilist);
        assertEquals(ilist,new ListFloatList(list));
        assertEquals(new FloatListList(ilist),list);
        assertEquals(list,new FloatListList(ilist));
        
    }

    public void testClearAndSize() {
        FloatList list = makeEmptyFloatList();
        assertEquals(0, list.size());
        for(int i = 0; i < 100; i++) {
            list.add((float)i);
        }
        assertEquals(100, list.size());
        list.clear();
        assertEquals(0, list.size());
    }

    public void testRemoveViaSubList() {
        FloatList list = makeEmptyFloatList();
        for(int i = 0; i < 100; i++) {
            list.add((float)i);
        }
        FloatList sub = list.subList(25,75);
        assertEquals(50,sub.size());
        for(int i = 0; i < 50; i++) {
            assertEquals(100-i,list.size());
            assertEquals(50-i,sub.size());
            assertEquals((float)(25+i),sub.removeElementAt(0), 0f);
            assertEquals(50-i-1,sub.size());
            assertEquals(100-i-1,list.size());
        }
        assertEquals(0,sub.size());
        assertEquals(50,list.size());        
    }
    
    public void testAddGet() {
        FloatList list = makeEmptyFloatList();
        for (int i = 0; i < 255; i++) {
            list.add((float)i);
        }
        for (int i = 0; i < 255; i++) {
            assertEquals((float)i, list.get(i), 0f);
        }
    }

    public void testAddAndShift() {
        FloatList list = makeEmptyFloatList();
        list.add(0, (float)1);
        assertEquals("Should have one entry", 1, list.size());
        list.add((float)3);
        list.add((float)4);
        list.add(1, (float)2);
        for(int i = 0; i < 4; i++) {
            assertEquals("Should get entry back", (float)(i + 1), list.get(i), 0f);
        }
        list.add(0, (float)0);
        for (int i = 0; i < 5; i++) {
            assertEquals("Should get entry back", (float)i, list.get(i), 0f);
        }
    }

    public void testIsSerializable() throws Exception {
        FloatList list = makeFullFloatList();
        assertTrue(list instanceof Serializable);
        byte[] ser = writeExternalFormToBytes((Serializable)list);
        FloatList deser = (FloatList)(readExternalFormFromBytes(ser));
        assertEquals(list,deser);
        assertEquals(deser,list);
    }

    public void testFloatListSerializeDeserializeThenCompare() throws Exception {
        FloatList list = makeFullFloatList();
        if(list instanceof Serializable) {
            byte[] ser = writeExternalFormToBytes((Serializable)list);
            FloatList deser = (FloatList)(readExternalFormFromBytes(ser));
            assertEquals("obj != deserialize(serialize(obj))",list,deser);
        }
    }

    public void testSubListsAreNotSerializable() throws Exception {
        FloatList list = makeFullFloatList().subList(2,3);
        assertTrue( ! (list instanceof Serializable) );
    }

    public void testSubListOutOfBounds() throws Exception {
        try {
            makeEmptyFloatList().subList(2,3);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullFloatList().subList(-1,3);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }


        try {
            makeFullFloatList().subList(5,2);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }

        try {
            makeFullFloatList().subList(2,makeFullFloatList().size()+2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testListIteratorOutOfBounds() throws Exception {
        try {
            makeEmptyFloatList().listIterator(2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullFloatList().listIterator(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullFloatList().listIterator(makeFullFloatList().size()+2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testListIteratorSetWithoutNext() throws Exception {
        FloatListIterator iter = makeFullFloatList().listIterator();
        try {
            iter.set((float)3);
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    public void testListIteratorSetAfterRemove() throws Exception {
        FloatListIterator iter = makeFullFloatList().listIterator();
        iter.next();
        iter.remove();
        try {            
            iter.set((float)3);
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

}
