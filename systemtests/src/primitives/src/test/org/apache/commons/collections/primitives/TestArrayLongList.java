/*
 * Copyright 2002-2005 The Apache Software Foundation
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.BulkTest;

/**
 * @version $Revision: 1.4 $ $Date: 2005/01/03 23:41:05 $
 * @author Rodney Waldhoff
 */
public class TestArrayLongList extends TestLongList {

    // conventional
    // ------------------------------------------------------------------------

    public TestArrayLongList(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = BulkTest.makeSuite(TestArrayLongList.class);
        return suite;
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    protected LongList makeEmptyLongList() {
        return new ArrayLongList();
    }

    public String[] ignoredTests() {
        // sublists are not serializable
        return new String[] { 
            "TestArrayLongList.bulkTestSubList.testFullListSerialization",
            "TestArrayLongList.bulkTestSubList.testEmptyListSerialization",
            "TestArrayLongList.bulkTestSubList.testCanonicalEmptyCollectionExists",
            "TestArrayLongList.bulkTestSubList.testCanonicalFullCollectionExists",
            "TestArrayLongList.bulkTestSubList.testEmptyListCompatibility",
            "TestArrayLongList.bulkTestSubList.testFullListCompatibility",
            "TestArrayLongList.bulkTestSubList.testSerializeDeserializeThenCompare",
            "TestArrayLongList.bulkTestSubList.testSimpleSerialization"
        };
    }

    // tests
    // ------------------------------------------------------------------------

    /** @TODO need to add serialized form to cvs */
    public void testCanonicalEmptyCollectionExists() {
        // XXX FIX ME XXX
        // need to add a serialized form to cvs
    }

    public void testCanonicalFullCollectionExists() {
        // XXX FIX ME XXX
        // need to add a serialized form to cvs
    }

    public void testEmptyListCompatibility() {
        // XXX FIX ME XXX
        // need to add a serialized form to cvs
    }

    public void testFullListCompatibility() {
        // XXX FIX ME XXX
        // need to add a serialized form to cvs
    }

    public void testAddGetLargeValues() {
        LongList list = new ArrayLongList();
        for (int i = 0; i < 1000; i++) {
            long value = ((long) (Integer.MAX_VALUE));
            value += i;
            list.add(value);
        }
        for (int i = 0; i < 1000; i++) {
            long value = ((long) (Integer.MAX_VALUE));
            value += i;
            assertEquals(value, list.get(i));
        }
    }

    public void testZeroInitialCapacityIsValid() {
        assertNotNull(new ArrayLongList(0));
    }

    public void testNegativeInitialCapacityIsInvalid() {
        try {
            new ArrayLongList(-1);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    public void testCopyConstructor() {
        ArrayLongList expected = new ArrayLongList();
        for(int i=0;i<10;i++) {
            expected.add((long)i);
        }
        ArrayLongList list = new ArrayLongList(expected);
        assertEquals(10,list.size());
        assertEquals(expected,list);
    }

    public void testCopyConstructorWithNull() {
        try {
            new ArrayLongList((LongCollection) null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testArrayConstructor() {
        ArrayLongList expected = new ArrayLongList();
        for (int i = 0; i < 10; i++) {
            expected.add((long) i);
        }
        ArrayLongList list = new ArrayLongList(expected.toArray());
        assertEquals(10, list.size());
        assertEquals(expected, list);
    }

    public void testArrayConstructorWithNull() {
        try {
            new ArrayLongList((long[]) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }


    public void testTrimToSize() {
        ArrayLongList list = new ArrayLongList();
        for(int j=0;j<3;j++) {
            assertTrue(list.isEmpty());
    
            list.trimToSize();
    
            assertTrue(list.isEmpty());
            
            for(int i=0;i<10;i++) {
                list.add((long)i);
            }
            
            for(int i=0;i<10;i++) {
                assertEquals((long)i,list.get(i));
            }
            
            list.trimToSize();
    
            for(int i=0;i<10;i++) {
                assertEquals((long)i,list.get(i));
            }
    
            for(int i=0;i<10;i+=2) {
                list.removeElement((long)i);
            }
            
            for(int i=0;i<5;i++) {
                assertEquals((long)(2*i)+1,list.get(i));
            }
    
            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals((long)(2*i)+1,list.get(i));
            }

            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals((long)(2*i)+1,list.get(i));
            }
    
            list.clear();
        }
    }

}
