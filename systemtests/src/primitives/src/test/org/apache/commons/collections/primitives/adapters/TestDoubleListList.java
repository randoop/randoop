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
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.BulkTest;
import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.RandomAccessDoubleList;

/**
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:29 $
 * @author Rodney Waldhoff
 */
public class TestDoubleListList extends BaseTestList {

    // conventional
    // ------------------------------------------------------------------------

    public TestDoubleListList(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = BulkTest.makeSuite(TestDoubleListList.class);
        return suite;
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    public List makeEmptyList() {
        return new DoubleListList(new ArrayDoubleList());
    }
        
    public Object[] getFullElements() {
        Double[] elts = new Double[10];
        for(int i=0;i<elts.length;i++) {
            elts[i] = new Double((double)i);
        }
        return elts;
    }

    public Object[] getOtherElements() {
        Double[] elts = new Double[10];
        for(int i=0;i<elts.length;i++) {
            elts[i] = new Double((double)(10 + i));
        }
        return elts;
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
        assertNull(DoubleListList.wrap(null));
    }
    
    public void testWrapSerializable() {
        List list = DoubleListList.wrap(new ArrayDoubleList());
        assertNotNull(list);
        assertTrue(list instanceof Serializable);
    }
    
    public void testWrapNonSerializable() {
        List list = DoubleListList.wrap(new RandomAccessDoubleList() { 
            public double get(int i) { throw new IndexOutOfBoundsException(); } 
            public int size() { return 0; } 
        });
        assertNotNull(list);
        assertTrue(!(list instanceof Serializable));
    }
}
