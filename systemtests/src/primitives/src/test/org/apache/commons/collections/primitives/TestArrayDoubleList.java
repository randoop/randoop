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
public class TestArrayDoubleList extends TestDoubleList {

    // conventional
    // ------------------------------------------------------------------------

    public TestArrayDoubleList(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = BulkTest.makeSuite(TestArrayDoubleList.class);
        return suite;
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    protected DoubleList makeEmptyDoubleList() {
        return new ArrayDoubleList();
    }

    public String[] ignoredTests() {
        // sublists are not serializable
        return new String[] { 
            "TestArrayDoubleList.bulkTestSubList.testFullListSerialization",
            "TestArrayDoubleList.bulkTestSubList.testEmptyListSerialization",
            "TestArrayDoubleList.bulkTestSubList.testCanonicalEmptyCollectionExists",
            "TestArrayDoubleList.bulkTestSubList.testCanonicalFullCollectionExists",
            "TestArrayDoubleList.bulkTestSubList.testEmptyListCompatibility",
            "TestArrayDoubleList.bulkTestSubList.testFullListCompatibility",
            "TestArrayDoubleList.bulkTestSubList.testSerializeDeserializeThenCompare",
            "TestArrayDoubleList.bulkTestSubList.testSimpleSerialization"
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
        DoubleList list = new ArrayDoubleList();
        for (int i = 0; i < 1000; i++) {
            double value = ((double) (Double.MAX_VALUE));
            value -= i;
            list.add(value);
        }
        for (int i = 0; i < 1000; i++) {
            double value = ((double) (Double.MAX_VALUE));
            value -= i;
            assertEquals(value, list.get(i), 0f);
        }
    }

    public void testZeroInitialCapacityIsValid() {
        assertNotNull(new ArrayDoubleList(0));
    }

    public void testNegativeInitialCapacityIsInvalid() {
        try {
            new ArrayDoubleList(-1);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    public void testCopyConstructor() {
        ArrayDoubleList expected = new ArrayDoubleList();
        for(int i=0;i<10;i++) {
            expected.add((double)i);
        }
        ArrayDoubleList list = new ArrayDoubleList(expected);
        assertEquals(10,list.size());
        assertEquals(expected,list);
    }

    public void testCopyConstructorWithNull() {
        try {
            new ArrayDoubleList((DoubleCollection) null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testArrayConstructor() {
        ArrayDoubleList expected = new ArrayDoubleList();
        for (int i = 0; i < 10; i++) {
            expected.add((double) i);
        }
        ArrayDoubleList list = new ArrayDoubleList(expected.toArray());
        assertEquals(10, list.size());
        assertEquals(expected, list);
    }

    public void testArrayConstructorWithNull() {
        try {
            new ArrayDoubleList((double[]) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }


    public void testTrimToSize() {
        ArrayDoubleList list = new ArrayDoubleList();
        for(int j=0;j<3;j++) {
            assertTrue(list.isEmpty());
    
            list.trimToSize();
    
            assertTrue(list.isEmpty());
            
            for(int i=0;i<10;i++) {
                list.add((double)i);
            }
            
            for(int i=0;i<10;i++) {
                assertEquals((double)i,list.get(i), 0f);
            }
            
            list.trimToSize();
    
            for(int i=0;i<10;i++) {
                assertEquals((double)i,list.get(i), 0f);
            }
    
            for(int i=0;i<10;i+=2) {
                list.removeElement((double)i);
            }
            
            for(int i=0;i<5;i++) {
                assertEquals((double)(2*i)+1,list.get(i), 0f);
            }
    
            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals((double)(2*i)+1,list.get(i), 0f);
            }

            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals((double)(2*i)+1,list.get(i), 0f);
            }
    
            list.clear();
        }
    }

}
