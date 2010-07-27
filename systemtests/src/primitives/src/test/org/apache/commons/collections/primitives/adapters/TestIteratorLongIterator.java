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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.primitives.LongIterator;
import org.apache.commons.collections.primitives.TestLongIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:29 $
 * @author Rodney Waldhoff
 */
public class TestIteratorLongIterator extends TestLongIterator {

    // conventional
    // ------------------------------------------------------------------------

    public TestIteratorLongIterator(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestIteratorLongIterator.class);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    public LongIterator makeEmptyLongIterator() {
        return IteratorLongIterator.wrap(makeEmptyList().iterator());
    }
    
    public LongIterator makeFullLongIterator() {
        return IteratorLongIterator.wrap(makeFullList().iterator());
    }

    public List makeEmptyList() {
        return new ArrayList();
    }
    
    protected List makeFullList() {
        List list = makeEmptyList();
        long[] elts = getFullElements();
        for(int i=0;i<elts.length;i++) {
            list.add(new Long(elts[i]));
        }
        return list;
    }
    
    public long[] getFullElements() {
        return new long[] { (long)0, (long)1, (long)2, (long)3, (long)4, (long)5, (long)6, (long)7, (long)8, (long)9 };
    }
    
    // tests
    // ------------------------------------------------------------------------


}
