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

import org.apache.commons.collections.primitives.FloatIterator;
import org.apache.commons.collections.primitives.TestFloatIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:29 $
 * @author Rodney Waldhoff
 */
public class TestIteratorFloatIterator extends TestFloatIterator {

    // conventional
    // ------------------------------------------------------------------------

    public TestIteratorFloatIterator(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestIteratorFloatIterator.class);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    public FloatIterator makeEmptyFloatIterator() {
        return IteratorFloatIterator.wrap(makeEmptyList().iterator());
    }
    
    public FloatIterator makeFullFloatIterator() {
        return IteratorFloatIterator.wrap(makeFullList().iterator());
    }

    public List makeEmptyList() {
        return new ArrayList();
    }
    
    protected List makeFullList() {
        List list = makeEmptyList();
        float[] elts = getFullElements();
        for(int i=0;i<elts.length;i++) {
            list.add(new Float(elts[i]));
        }
        return list;
    }
    
    public float[] getFullElements() {
        return new float[] { (float)0, (float)1, (float)2, (float)3, (float)4, (float)5, (float)6, (float)7, (float)8, (float)9 };
    }
    
    // tests
    // ------------------------------------------------------------------------


}
