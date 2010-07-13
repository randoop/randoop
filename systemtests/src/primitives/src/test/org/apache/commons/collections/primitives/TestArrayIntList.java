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
public class TestArrayIntList extends TestIntList {

    // conventional
    // ------------------------------------------------------------------------

    public TestArrayIntList(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = BulkTest.makeSuite(TestArrayIntList.class);
        return suite;
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    protected IntList makeEmptyIntList() {
        return new ArrayIntList();
    }

    public String[] ignoredTests() {
        // sublists are not serializable
        return new String[] { 
            "TestArrayIntList.bulkTestSubList.testFullListSerialization",
            "TestArrayIntList.bulkTestSubList.testEmptyListSerialization",
            "TestArrayIntList.bulkTestSubList.testCanonicalEmptyCollectionExists",
            "TestArrayIntList.bulkTestSubList.testCanonicalFullCollectionExists",
            "TestArrayIntList.bulkTestSubList.testEmptyListCompatibility",
            "TestArrayIntList.bulkTestSubList.testFullListCompatibility",
            "TestArrayIntList.bulkTestSubList.testSerializeDeserializeThenCompare",
            "TestArrayIntList.bulkTestSubList.testSimpleSerialization"
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
        IntList list = new ArrayIntList();
        for (int i = 0; i < 1000; i++) {
            int value = ((int) (Short.MAX_VALUE));
            value += i;
            list.add(value);
        }
        for (int i = 0; i < 1000; i++) {
            int value = ((int) (Short.MAX_VALUE));
            value += i;
            assertEquals(value, list.get(i));
        }
    }

    public void testZeroInitialCapacityIsValid() {
        assertNotNull(new ArrayIntList(0));
    }

    public void testNegativeInitialCapacityIsInvalid() {
        try {
            new ArrayIntList(-1);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    public void testCopyConstructor() {
        ArrayIntList expected = new ArrayIntList();
        for(int i=0;i<10;i++) {
            expected.add(i);
        }
        ArrayIntList list = new ArrayIntList(expected);
        assertEquals(10,list.size());
        assertEquals(expected,list);
    }

    public void testCopyConstructorWithNull() {
        try {
            new ArrayIntList((IntCollection) null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testArrayConstructor() {
		ArrayIntList expected = new ArrayIntList();
		for (int i = 0; i < 10; i++) {
			expected.add(i);
		}
		ArrayIntList list = new ArrayIntList(expected.toArray());
		assertEquals(10, list.size());
		assertEquals(expected, list);
	}

	public void testArrayConstructorWithNull() {
		try {
			new ArrayIntList((int[]) null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}


    public void testTrimToSize() {
        ArrayIntList list = new ArrayIntList();
        for(int j=0;j<3;j++) {
            assertTrue(list.isEmpty());
    
            list.trimToSize();
    
            assertTrue(list.isEmpty());
            
            for(int i=0;i<10;i++) {
                list.add(i);
            }
            
            for(int i=0;i<10;i++) {
                assertEquals(i,list.get(i));
            }
            
            list.trimToSize();
    
            for(int i=0;i<10;i++) {
                assertEquals(i,list.get(i));
            }
    
            for(int i=0;i<10;i+=2) {
                list.removeElement(i);
            }
            
            for(int i=0;i<5;i++) {
                assertEquals((2*i)+1,list.get(i));
            }
    
            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals((2*i)+1,list.get(i));
            }

            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals((2*i)+1,list.get(i));
            }
    
            list.clear();
        }
    }

}
