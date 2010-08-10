/*
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.commons.collections.primitives.adapters;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.BulkTest;
import org.apache.commons.collections.primitives.FloatList;
import org.apache.commons.collections.primitives.TestFloatList;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:29 $
 * @author Rodney Waldhoff
 */
public class TestListFloatList extends TestFloatList {

    // conventional
    // ------------------------------------------------------------------------

    public TestListFloatList(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = BulkTest.makeSuite(TestListFloatList.class);
        return suite;
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    /**
     * @see org.apache.commons.collections.primitives.TestFloatList#makeEmptyFloatList()
     */
    protected FloatList makeEmptyFloatList() {
        return new ListFloatList(new ArrayList());
    }
    
    public String[] ignoredTests() {
        // sublists are not serializable
        return new String[] { 
            "TestListFloatList.bulkTestSubList.testFullListSerialization",
            "TestListFloatList.bulkTestSubList.testEmptyListSerialization",
            "TestListFloatList.bulkTestSubList.testCanonicalEmptyCollectionExists",
            "TestListFloatList.bulkTestSubList.testCanonicalFullCollectionExists",
            "TestListFloatList.bulkTestSubList.testEmptyListCompatibility",
            "TestListFloatList.bulkTestSubList.testFullListCompatibility",
            "TestListFloatList.bulkTestSubList.testSerializeDeserializeThenCompare",
            "TestListFloatList.bulkTestSubList.testSimpleSerialization"
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
    public void testWrapNull() {
        assertNull(ListFloatList.wrap(null));
    }
    
    public void testWrapSerializable() {
        FloatList list = ListFloatList.wrap(new ArrayList());
        assertNotNull(list);
        assertTrue(list instanceof Serializable);
    }
    
    public void testWrapNonSerializable() {
        FloatList list = ListFloatList.wrap(new AbstractList() { 
            public Object get(int i) { throw new IndexOutOfBoundsException(); } 
            public int size() { return 0; } 
        });
        assertNotNull(list);
        assertTrue(!(list instanceof Serializable));
    }

}
