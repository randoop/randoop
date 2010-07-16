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

import org.apache.commons.collections.primitives.ShortListIterator;
import org.apache.commons.collections.primitives.TestShortListIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:29 $
 * @author Rodney Waldhoff
 */
public class TestListIteratorShortListIterator extends TestShortListIterator {

    // conventional
    // ------------------------------------------------------------------------

    public TestListIteratorShortListIterator(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestListIteratorShortListIterator.class);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    public ShortListIterator makeEmptyShortListIterator() {
        return ListIteratorShortListIterator.wrap(makeEmptyList().listIterator());
    }
    
    public ShortListIterator makeFullShortListIterator() {
        return ListIteratorShortListIterator.wrap(makeFullList().listIterator());
    }

    public List makeEmptyList() {
        return new ArrayList();
    }
    
    protected List makeFullList() {
        List list = makeEmptyList();
        short[] elts = getFullElements();
        for(int i=0;i<elts.length;i++) {
            list.add(new Short(elts[i]));
        }
        return list;
    }
    
    public short[] getFullElements() {
        return new short[] { (short)0, (short)1, (short)2, (short)3, (short)4, (short)5, (short)6, (short)7, (short)8, (short)9 };
    }
    
    // tests
    // ------------------------------------------------------------------------


}
