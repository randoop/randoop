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

import org.apache.commons.collections.primitives.CharIterator;
import org.apache.commons.collections.primitives.TestCharIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:29 $
 * @author Rodney Waldhoff
 */
public class TestIteratorCharIterator extends TestCharIterator {

    // conventional
    // ------------------------------------------------------------------------

    public TestIteratorCharIterator(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestIteratorCharIterator.class);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    public CharIterator makeEmptyCharIterator() {
        return IteratorCharIterator.wrap(makeEmptyList().iterator());
    }
    
    public CharIterator makeFullCharIterator() {
        return IteratorCharIterator.wrap(makeFullList().iterator());
    }

    public List makeEmptyList() {
        return new ArrayList();
    }
    
    protected List makeFullList() {
        List list = makeEmptyList();
        char[] elts = getFullElements();
        for(int i=0;i<elts.length;i++) {
            list.add(new Character(elts[i]));
        }
        return list;
    }
    
    public char[] getFullElements() {
        return new char[] { (char)0, (char)1, (char)2, (char)3, (char)4, (char)5, (char)6, (char)7, (char)8, (char)9 };
    }
    
    // tests
    // ------------------------------------------------------------------------


}
