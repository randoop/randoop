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
import org.apache.commons.collections.primitives.adapters.DoubleListList;
import org.apache.commons.collections.primitives.adapters.ListDoubleList;

/**
 * @version $Revision: 1.5 $ $Date: 2004/02/25 20:46:30 $
 * @author Rodney Waldhoff
 */
public abstract class TestDoubleList extends BaseTestList {

    // conventional
    // ------------------------------------------------------------------------

    public TestDoubleList(String testName) {
        super(testName);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    // collections testing framework: double list
    // ------------------------------------------------------------------------

    protected abstract DoubleList makeEmptyDoubleList();

    protected DoubleList makeFullDoubleList() {
        DoubleList list = makeEmptyDoubleList();
        double[] values = getFullDoubles();
        for(int i=0;i<values.length;i++) {
            list.add(values[i]);
        }
        return list;
    }

    protected double[] getFullDoubles() {
        double[] result = new double[19];
        for(int i = 0; i < result.length; i++) {
            result[i] = (double)(i);
        }
        return result;
    }

    protected double[] getOtherDoubles() {
        double[] result = new double[16];
        for(int i = 0; i < result.length; i++) {
            result[i] = (double)(i + 43);
        }
        return result;
    }
    
    // collections testing framework: inherited
    // ------------------------------------------------------------------------

    public List makeEmptyList() {
        return new DoubleListList(makeEmptyDoubleList());
    }
        
    public Object[] getFullElements() {
        return wrapArray(getFullDoubles());
    }

    public Object[] getOtherElements() {
        return wrapArray(getOtherDoubles());
    }

    // private utils
    // ------------------------------------------------------------------------

    private Double[] wrapArray(double[] primitives) {
        Double[] result = new Double[primitives.length];
        for(int i=0;i<result.length;i++) {
            result[i] = new Double(primitives[i]);            
        }
        return result;
    }

    // tests
    // ------------------------------------------------------------------------

    public void testExceptionOnConcurrentModification() {
        DoubleList list = makeFullDoubleList();
        DoubleIterator iter = list.iterator();
        iter.next();
        list.add((double)3);
        try {
            iter.next();
            fail("Expected ConcurrentModificationException");
        } catch(ConcurrentModificationException e) {
            // expected
        }
    }
    
    public void testAddAllDoubleListAtIndex() {
        DoubleList source = makeFullDoubleList();
        DoubleList dest = makeFullDoubleList();
        dest.addAll(1,source);
        
        DoubleIterator iter = dest.iterator();
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

    public void testToJustBigEnoughDoubleArray() {
        DoubleList list = makeFullDoubleList();
        double[] dest = new double[list.size()];
        assertSame(dest,list.toArray(dest));
        int i=0;
        for(DoubleIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest[i], 0f);
        }
    }
    
    public void testToLargerThanNeededDoubleArray() {
        DoubleList list = makeFullDoubleList();
        double[] dest = new double[list.size()*2];
        for(int i=0;i<dest.length;i++) {
            dest[i] = Double.MAX_VALUE;
        }       
        assertSame(dest,list.toArray(dest));
        int i=0;
        for(DoubleIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest[i], 0f);
        }
        for(;i<dest.length;i++) {
            assertEquals(Double.MAX_VALUE,dest[i], 0f);
        }
    }
    
    public void testToSmallerThanNeededDoubleArray() {
        DoubleList list = makeFullDoubleList();
        double[] dest = new double[list.size()/2];
        double[] dest2 = list.toArray(dest);
        assertTrue(dest != dest2);
        int i=0;
        for(DoubleIterator iter = list.iterator(); iter.hasNext();i++) {
            assertEquals(iter.next(),dest2[i], 0f);
        }
    }
    
    public void testHashCodeSpecification() {
        DoubleList list = makeFullDoubleList();
		int hash = 1;
		for(DoubleIterator iter = list.iterator(); iter.hasNext(); ) {
            long bits = Double.doubleToLongBits(iter.next());
            hash = 31*hash + ((int)(bits ^ (bits >>> 32)));
		}
        assertEquals(hash,list.hashCode());
    }

    public void testEqualsWithTwoDoubleLists() {
        DoubleList one = makeEmptyDoubleList();
        assertEquals("Equals is reflexive on empty list",one,one);
        DoubleList two = makeEmptyDoubleList();
        assertEquals("Empty lists are equal",one,two);
        assertEquals("Equals is symmetric on empty lists",two,one);
        
        one.add((double)1);
        assertEquals("Equals is reflexive on non empty list",one,one);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));

        two.add((double)1);
        assertEquals("Non empty lists are equal",one,two);
        assertEquals("Equals is symmetric on non empty list",one,two);
        
        one.add((double)1); one.add((double)2); one.add((double)3); one.add((double)5); one.add((double)8);
        assertEquals("Equals is reflexive on larger non empty list",one,one);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));
        
        two.add((double)1); two.add((double)2); two.add((double)3); two.add((double)5); two.add((double)8);
        assertEquals("Larger non empty lists are equal",one,two);
        assertEquals("Equals is symmetric on larger non empty list",two,one);

        one.add((double)9);
        two.add((double)10);
        assertTrue(!one.equals(two));
        assertTrue(!two.equals(one));

    }

    public void testDoubleSubListEquals() {
        DoubleList one = makeEmptyDoubleList();
        assertEquals(one,one.subList(0,0));
        assertEquals(one.subList(0,0),one);
        
        one.add((double)1);
        assertEquals(one,one.subList(0,1));
        assertEquals(one.subList(0,1),one);

        one.add((double)1); one.add((double)2); one.add((double)3); one.add((double)5); one.add((double)8);
        assertEquals(one.subList(0,4),one.subList(0,4));
        assertEquals(one.subList(3,5),one.subList(3,5));
    }
    
    public void testEqualsWithDoubleListAndList() {
        DoubleList ilist = makeEmptyDoubleList();
        List list = new ArrayList();
        
        assertTrue("Unwrapped, empty List should not be equal to empty DoubleList.",!ilist.equals(list));
        assertTrue("Unwrapped, empty DoubleList should not be equal to empty List.",!list.equals(ilist));
        
        assertEquals(new ListDoubleList(list),ilist);
        assertEquals(ilist,new ListDoubleList(list));
        assertEquals(new DoubleListList(ilist),list);
        assertEquals(list,new DoubleListList(ilist));
        
        ilist.add((double)1);
        list.add(new Double((double)1));

        assertTrue("Unwrapped, non-empty List is not equal to non-empty DoubleList.",!ilist.equals(list));
        assertTrue("Unwrapped, non-empty DoubleList is not equal to non-empty List.",!list.equals(ilist));
        
        assertEquals(new ListDoubleList(list),ilist);
        assertEquals(ilist,new ListDoubleList(list));
        assertEquals(new DoubleListList(ilist),list);
        assertEquals(list,new DoubleListList(ilist));
                
        ilist.add((double)1); ilist.add((double)2); ilist.add((double)3); ilist.add((double)5); ilist.add((double)8);
        list.add(new Double((double)1)); list.add(new Double((double)2)); list.add(new Double((double)3)); list.add(new Double((double)5)); list.add(new Double((double)8));

        assertTrue("Unwrapped, non-empty List is not equal to non-empty DoubleList.",!ilist.equals(list));
        assertTrue("Unwrapped, non-empty DoubleList is not equal to non-empty List.",!list.equals(ilist));
        
        assertEquals(new ListDoubleList(list),ilist);
        assertEquals(ilist,new ListDoubleList(list));
        assertEquals(new DoubleListList(ilist),list);
        assertEquals(list,new DoubleListList(ilist));
        
    }

    public void testClearAndSize() {
        DoubleList list = makeEmptyDoubleList();
        assertEquals(0, list.size());
        for(int i = 0; i < 100; i++) {
            list.add((double)i);
        }
        assertEquals(100, list.size());
        list.clear();
        assertEquals(0, list.size());
    }

    public void testRemoveViaSubList() {
        DoubleList list = makeEmptyDoubleList();
        for(int i = 0; i < 100; i++) {
            list.add((double)i);
        }
        DoubleList sub = list.subList(25,75);
        assertEquals(50,sub.size());
        for(int i = 0; i < 50; i++) {
            assertEquals(100-i,list.size());
            assertEquals(50-i,sub.size());
            assertEquals((double)(25+i),sub.removeElementAt(0), 0f);
            assertEquals(50-i-1,sub.size());
            assertEquals(100-i-1,list.size());
        }
        assertEquals(0,sub.size());
        assertEquals(50,list.size());        
    }
    
    public void testAddGet() {
        DoubleList list = makeEmptyDoubleList();
        for (int i = 0; i < 255; i++) {
            list.add((double)i);
        }
        for (int i = 0; i < 255; i++) {
            assertEquals((double)i, list.get(i), 0f);
        }
    }

    public void testAddAndShift() {
        DoubleList list = makeEmptyDoubleList();
        list.add(0, (double)1);
        assertEquals("Should have one entry", 1, list.size());
        list.add((double)3);
        list.add((double)4);
        list.add(1, (double)2);
        for(int i = 0; i < 4; i++) {
            assertEquals("Should get entry back", (double)(i + 1), list.get(i), 0f);
        }
        list.add(0, (double)0);
        for (int i = 0; i < 5; i++) {
            assertEquals("Should get entry back", (double)i, list.get(i), 0f);
        }
    }

    public void testIsSerializable() throws Exception {
        DoubleList list = makeFullDoubleList();
        assertTrue(list instanceof Serializable);
        byte[] ser = writeExternalFormToBytes((Serializable)list);
        DoubleList deser = (DoubleList)(readExternalFormFromBytes(ser));
        assertEquals(list,deser);
        assertEquals(deser,list);
    }

    public void testDoubleListSerializeDeserializeThenCompare() throws Exception {
        DoubleList list = makeFullDoubleList();
        if(list instanceof Serializable) {
            byte[] ser = writeExternalFormToBytes((Serializable)list);
            DoubleList deser = (DoubleList)(readExternalFormFromBytes(ser));
            assertEquals("obj != deserialize(serialize(obj))",list,deser);
        }
    }

    public void testSubListsAreNotSerializable() throws Exception {
        DoubleList list = makeFullDoubleList().subList(2,3);
        assertTrue( ! (list instanceof Serializable) );
    }

    public void testSubListOutOfBounds() throws Exception {
        try {
            makeEmptyDoubleList().subList(2,3);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullDoubleList().subList(-1,3);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }


        try {
            makeFullDoubleList().subList(5,2);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }

        try {
            makeFullDoubleList().subList(2,makeFullDoubleList().size()+2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testListIteratorOutOfBounds() throws Exception {
        try {
            makeEmptyDoubleList().listIterator(2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullDoubleList().listIterator(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        try {
            makeFullDoubleList().listIterator(makeFullDoubleList().size()+2);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testListIteratorSetWithoutNext() throws Exception {
        DoubleListIterator iter = makeFullDoubleList().listIterator();
        try {
            iter.set((double)3);
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    public void testListIteratorSetAfterRemove() throws Exception {
        DoubleListIterator iter = makeFullDoubleList().listIterator();
        iter.next();
        iter.remove();
        try {            
            iter.set((double)3);
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

}
