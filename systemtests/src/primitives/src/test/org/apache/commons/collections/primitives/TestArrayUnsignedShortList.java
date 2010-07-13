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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.BulkTest;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:30 $
 * @author Rodney Waldhoff
 */
public class TestArrayUnsignedShortList extends TestIntList {

    // conventional
    // ------------------------------------------------------------------------

    public TestArrayUnsignedShortList(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = BulkTest.makeSuite(TestArrayUnsignedShortList.class);
        return suite;
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    protected IntList makeEmptyIntList() {
        return new ArrayUnsignedShortList();
    }

    public String[] ignoredTests() {
        // sublists are not serializable
        return new String[] { 
            "TestArrayUnsignedShortList.bulkTestSubList.testFullListSerialization",
            "TestArrayUnsignedShortList.bulkTestSubList.testEmptyListSerialization",
            "TestArrayUnsignedShortList.bulkTestSubList.testCanonicalEmptyCollectionExists",
            "TestArrayUnsignedShortList.bulkTestSubList.testCanonicalFullCollectionExists",
            "TestArrayUnsignedShortList.bulkTestSubList.testEmptyListCompatibility",
            "TestArrayUnsignedShortList.bulkTestSubList.testFullListCompatibility",
            "TestArrayUnsignedShortList.bulkTestSubList.testSerializeDeserializeThenCompare",
            "TestArrayUnsignedShortList.bulkTestSubList.testSimpleSerialization"
        };
    }

    // tests
    // ------------------------------------------------------------------------

    // @TODO need to add serialized form to cvs
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
        assertNotNull(new ArrayUnsignedShortList(0));
    }
    
    public void testIllegalArgumentExceptionWhenElementOutOfRange() {
        ArrayUnsignedShortList list = new ArrayUnsignedShortList();
        list.add(ArrayUnsignedShortList.MIN_VALUE);
        list.add(ArrayUnsignedShortList.MAX_VALUE);
        try {
            list.add(-1);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
        try {
            list.add(ArrayUnsignedShortList.MAX_VALUE+1);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    public void testNegativeInitialCapacityIsInvalid() {
        try {
            new ArrayUnsignedShortList(-1);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    public void testCopyConstructor() {
        ArrayUnsignedShortList expected = new ArrayUnsignedShortList();
        for(int i=0;i<10;i++) {
            expected.add(i);
        }
        ArrayUnsignedShortList list = new ArrayUnsignedShortList(expected);
        assertEquals(10,list.size());
        assertEquals(expected,list);
    }

    public void testCopyConstructorWithNull() {
        try {
            new ArrayUnsignedShortList(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }


    public void testTrimToSize() {
        ArrayUnsignedShortList list = new ArrayUnsignedShortList();
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
