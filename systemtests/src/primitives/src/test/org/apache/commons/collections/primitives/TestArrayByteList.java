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
public class TestArrayByteList extends TestByteList {

    // conventional
    // ------------------------------------------------------------------------

    public TestArrayByteList(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = BulkTest.makeSuite(TestArrayByteList.class);
        return suite;
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    protected ByteList makeEmptyByteList() {
        return new ArrayByteList();
    }

    public String[] ignoredTests() {
        // sublists are not serializable
        return new String[] { 
            "TestArrayByteList.bulkTestSubList.testFullListSerialization",
            "TestArrayByteList.bulkTestSubList.testEmptyListSerialization",
            "TestArrayByteList.bulkTestSubList.testCanonicalEmptyCollectionExists",
            "TestArrayByteList.bulkTestSubList.testCanonicalFullCollectionExists",
            "TestArrayByteList.bulkTestSubList.testEmptyListCompatibility",
            "TestArrayByteList.bulkTestSubList.testFullListCompatibility",
            "TestArrayByteList.bulkTestSubList.testSerializeDeserializeThenCompare",
            "TestArrayByteList.bulkTestSubList.testSimpleSerialization"
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
        ByteList list = new ArrayByteList();
        for (int i = 0; i < 1000; i++) {
            byte value = ((byte) (Byte.MAX_VALUE));
            value -= i;
            list.add(value);
        }
        for (int i = 0; i < 1000; i++) {
            byte value = ((byte) (Byte.MAX_VALUE));
            value -= i;
            assertEquals(value, list.get(i));
        }
    }

    public void testZeroInitialCapacityIsValid() {
        assertNotNull(new ArrayByteList(0));
    }

    public void testNegativeInitialCapacityIsInvalid() {
        try {
            new ArrayByteList(-1);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    public void testCopyConstructor() {
        ArrayByteList expected = new ArrayByteList();
        for(int i=0;i<10;i++) {
            expected.add((byte)i);
        }
        ArrayByteList list = new ArrayByteList(expected);
        assertEquals(10,list.size());
        assertEquals(expected,list);
    }

    public void testCopyConstructorWithNull() {
        try {
            new ArrayByteList((ByteCollection) null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }

    public void testArrayConstructor() {
        ArrayByteList expected = new ArrayByteList();
        for (int i = 0; i < 10; i++) {
            expected.add((byte) i);
        }
        ArrayByteList list = new ArrayByteList(expected.toArray());
        assertEquals(10, list.size());
        assertEquals(expected, list);
    }

    public void testArrayConstructorWithNull() {
        try {
            new ArrayByteList((byte[]) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }


    public void testTrimToSize() {
        ArrayByteList list = new ArrayByteList();
        for(int j=0;j<3;j++) {
            assertTrue(list.isEmpty());
    
            list.trimToSize();
    
            assertTrue(list.isEmpty());
            
            for(int i=0;i<10;i++) {
                list.add((byte)i);
            }
            
            for(int i=0;i<10;i++) {
                assertEquals((byte)i,list.get(i));
            }
            
            list.trimToSize();
    
            for(int i=0;i<10;i++) {
                assertEquals((byte)i,list.get(i));
            }
    
            for(int i=0;i<10;i+=2) {
                list.removeElement((byte)i);
            }
            
            for(int i=0;i<5;i++) {
                assertEquals((byte)(2*i)+1,list.get(i));
            }
    
            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals((byte)(2*i)+1,list.get(i));
            }

            list.trimToSize();
                    
            for(int i=0;i<5;i++) {
                assertEquals((byte)(2*i)+1,list.get(i));
            }
    
            list.clear();
        }
    }

}
