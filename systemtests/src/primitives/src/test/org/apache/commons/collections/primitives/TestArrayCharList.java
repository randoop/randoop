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
public class TestArrayCharList extends TestCharList {

    // conventional
    // ------------------------------------------------------------------------

    public TestArrayCharList(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = BulkTest.makeSuite(TestArrayCharList.class);
        return suite;
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    protected CharList makeEmptyCharList() {
        return new ArrayCharList();
    }

    public String[] ignoredTests() {
        // sublists are not serializable
        return new String[] { 
            "TestArrayCharList.bulkTestSubList.testFullListSerialization",
            "TestArrayCharList.bulkTestSubList.testEmptyListSerialization",
            "TestArrayCharList.bulkTestSubList.testCanonicalEmptyCollectionExists",
            "TestArrayCharList.bulkTestSubList.testCanonicalFullCollectionExists",
            "TestArrayCharList.bulkTestSubList.testEmptyListCompatibility",
            "TestArrayCharList.bulkTestSubList.testFullListCompatibility",
            "TestArrayCharList.bulkTestSubList.testSerializeDeserializeThenCompare",
            "TestArrayCharList.bulkTestSubList.testSimpleSerialization"
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
        CharList list = new ArrayCharList();
        for (int i = 0; i < 1000; i++) {
            char value = ((char) (Character.MAX_VALUE));
            value -= i;
            list.add(value);
        }
        for (int i = 0; i < 1000; i++) {
            char value = ((char) (Character.MAX_VALUE));
            value -= i;
            assertEquals(value, list.get(i));
        }
    }

    public void testZeroInitialCapacityIsValid() {
        assertNotNull(new ArrayCharList(0));
    }

    public void testNegativeInitialCapacityIsInvalid() {
        try {
            new ArrayCharList(-1);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    public void testCopyConstructor() {
        ArrayCharList expected = new ArrayCharList();
        for(int i=0;i<10;i++) {
            expected.add((char)i);
        }
        ArrayCharList list = new ArrayCharList(expected);
        assertEquals(10,list.size());
        assertEquals(expected,list);
    }

    public void testCopyConstructorWithNull() {
        try {
            new ArrayCharList((CharCollection) null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testArrayConstructor() {
        ArrayCharList expected = new ArrayCharList();
        for (int i = 0; i < 10; i++) {
            expected.add((char) i);
        }
        ArrayCharList list = new ArrayCharList(expected.toArray());
        assertEquals(10, list.size());
        assertEquals(expected, list);
    }

    public void testArrayConstructorWithNull() {
        try {
            new ArrayCharList((char[]) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }


    public void testTrimToSize() {
        ArrayCharList list = new ArrayCharList();
        for(int j=0;j<3;j++) {
            assertTrue(list.isEmpty());
    
            list.trimToSize();
    
            assertTrue(list.isEmpty());
            
            for(int i=0;i<10;i++) {
                list.add((char)i);
            }
            
            for(int i=0;i<10;i++) {
                assertEquals((char)i,list.get(i), 0f);
            }
            
            list.trimToSize();
    
            for(int i=0;i<10;i++) {
                assertEquals((char)i,list.get(i), 0f);
            }
    
            for(int i=0;i<10;i+=2) {
                list.removeElement((char)i);
            }
            
            for(int i=0;i<5;i++) {
                assertEquals((char)(2*i)+1,list.get(i), 0f);
            }
    
            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals((char)(2*i)+1,list.get(i), 0f);
            }

            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals((char)(2*i)+1,list.get(i), 0f);
            }
    
            list.clear();
        }
    }

}
