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

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.iterators.AbstractTestIterator;
import org.apache.commons.collections.primitives.ArrayFloatList;
import org.apache.commons.collections.primitives.FloatList;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:29 $
 * @author Rodney Waldhoff
 */
public class TestFloatIteratorIterator extends AbstractTestIterator {

    // conventional
    // ------------------------------------------------------------------------

    public TestFloatIteratorIterator(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestFloatIteratorIterator.class);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    public Iterator makeEmptyIterator() {
        return FloatIteratorIterator.wrap(makeEmptyFloatList().iterator());
    }
    
    public Iterator makeFullIterator() {
        return FloatIteratorIterator.wrap(makeFullFloatList().iterator());
    }

    protected FloatList makeEmptyFloatList() {
        return new ArrayFloatList();
    }
    
    protected FloatList makeFullFloatList() {
        FloatList list = makeEmptyFloatList();
        float[] elts = getFullElements();
        for(int i=0;i<elts.length;i++) {
            list.add((float)elts[i]);
        }
        return list;
    }
    
    public float[] getFullElements() {
        return new float[] { (float)0, (float)1, (float)2, (float)3, (float)4, (float)5, (float)6, (float)7, (float)8, (float)9 };
    }
    
    // tests
    // ------------------------------------------------------------------------


}
