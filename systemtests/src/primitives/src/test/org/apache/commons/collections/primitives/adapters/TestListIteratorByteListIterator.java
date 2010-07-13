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

import org.apache.commons.collections.primitives.ByteListIterator;
import org.apache.commons.collections.primitives.TestByteListIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:29 $
 * @author Rodney Waldhoff
 */
public class TestListIteratorByteListIterator extends TestByteListIterator {

    // conventional
    // ------------------------------------------------------------------------

    public TestListIteratorByteListIterator(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestListIteratorByteListIterator.class);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    public ByteListIterator makeEmptyByteListIterator() {
        return ListIteratorByteListIterator.wrap(makeEmptyList().listIterator());
    }
    
    public ByteListIterator makeFullByteListIterator() {
        return ListIteratorByteListIterator.wrap(makeFullList().listIterator());
    }

    public List makeEmptyList() {
        return new ArrayList();
    }
    
    protected List makeFullList() {
        List list = makeEmptyList();
        byte[] elts = getFullElements();
        for(int i=0;i<elts.length;i++) {
            list.add(new Byte(elts[i]));
        }
        return list;
    }
    
    public byte[] getFullElements() {
        return new byte[] { (byte)0, (byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9 };
    }
    
    // tests
    // ------------------------------------------------------------------------


}
