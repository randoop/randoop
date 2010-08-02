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

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections.primitives.adapters.IteratorLongIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:30 $
 * @author Rodney Waldhoff
 */
public class TestAbstractLongCollection extends TestCase {

    // conventional
    // ------------------------------------------------------------------------

    public TestAbstractLongCollection(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestAbstractLongCollection.class);
    }

    // tests
    // ------------------------------------------------------------------------
    
    public void testAddIsUnsupportedByDefault() {
        LongCollection col = new LongCollectionImpl();
        try {
            col.add((long)1);
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }        
    }
    // inner classes
    // ------------------------------------------------------------------------


    static class LongCollectionImpl extends AbstractLongCollection {
        public LongCollectionImpl() {
        }
        
        public LongIterator iterator() {
            return new IteratorLongIterator(Collections.EMPTY_LIST.iterator());
        }

        public int size() {
            return 0;
        }
    }
}
