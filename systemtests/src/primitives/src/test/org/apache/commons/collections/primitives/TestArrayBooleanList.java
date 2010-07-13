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
 * @version $Revision: 1.2 $ $Date: 2005/01/03 23:41:05 $
 * @author Rodney Waldhoff
 */
public class TestArrayBooleanList extends TestBooleanList {

    // conventional
    // ------------------------------------------------------------------------

    public TestArrayBooleanList(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = BulkTest.makeSuite(TestArrayBooleanList.class);
        return suite;
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    protected BooleanList makeEmptyBooleanList() {
        return new ArrayBooleanList();
    }

    public String[] ignoredTests() {
        return new String[] { 

                // having only two unique values breaks these:
                "TestArrayBooleanList.bulkTestSubList.testListEquals",
                "TestArrayBooleanList.bulkTestSubList.testCollectionRemoveAll",
                "TestArrayBooleanList.bulkTestSubList.testCollectionRetainAll",

                // sublists are not serializable                
            "TestArrayBooleanList.bulkTestSubList.testFullListSerialization",
            "TestArrayBooleanList.bulkTestSubList.testEmptyListSerialization",
            "TestArrayBooleanList.bulkTestSubList.testCanonicalEmptyCollectionExists",
            "TestArrayBooleanList.bulkTestSubList.testCanonicalFullCollectionExists",
            "TestArrayBooleanList.bulkTestSubList.testEmptyListCompatibility",
            "TestArrayBooleanList.bulkTestSubList.testFullListCompatibility",
            "TestArrayBooleanList.bulkTestSubList.testSerializeDeserializeThenCompare",
            "TestArrayBooleanList.bulkTestSubList.testSimpleSerialization"
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

    public void testZeroInitialCapacityIsValid() {
        assertNotNull(new ArrayBooleanList(0));
    }

    public void testNegativeInitialCapacityIsInvalid() {
        try {
            new ArrayBooleanList(-1);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    public void testCopyConstructor() {
        ArrayBooleanList expected = new ArrayBooleanList();
        for(int i=0;i<10;i++) {
            expected.add(i%2==0);
        }
        ArrayBooleanList list = new ArrayBooleanList(expected);
        assertEquals(10,list.size());
        assertEquals(expected,list);
    }

    public void testCopyConstructorWithNull() {
        try {
            new ArrayBooleanList((BooleanCollection) null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testArrayConstructor() {
        ArrayBooleanList expected = new ArrayBooleanList();
        for (int i = 0; i < 10; i++) {
            expected.add(i % 2 == 0);
        }
        ArrayBooleanList list = new ArrayBooleanList(expected.toArray());
        assertEquals(10, list.size());
        assertEquals(expected, list);
    }

    public void testArrayConstructorWithNull() {
        try {
            new ArrayBooleanList((boolean[]) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }


    public void testTrimToSize() {
        ArrayBooleanList list = new ArrayBooleanList();
        for(int j=0;j<3;j++) {
            assertTrue(list.isEmpty());
    
            list.trimToSize();
    
            assertTrue(list.isEmpty());
            
            for(int i=0;i<10;i++) {
                list.add(i%2==0);
            }
            
            for(int i=0;i<10;i++) {
                assertEquals(i%2==0,list.get(i));
            }
            
            list.trimToSize();
    
            for(int i=0;i<10;i++) {
                assertEquals(i%2==0,list.get(i));
            }
    
            for(int i=0;i<5;i++) {
                list.removeElement(true);
            }
            
            for(int i=0;i<5;i++) {
                assertEquals(false,list.get(i));
            }
    
            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals(false,list.get(i));
            }

            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals(false,list.get(i));
            }
    
            list.clear();
        }
    }

}
