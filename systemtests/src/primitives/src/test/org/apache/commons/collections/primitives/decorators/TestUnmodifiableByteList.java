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
package org.apache.commons.collections.primitives.decorators;

import java.io.Serializable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.primitives.ByteList;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:32 $
 * @author Rodney Waldhoff
 */
public class TestUnmodifiableByteList extends BaseUnmodifiableByteListTest {

    // conventional
    // ------------------------------------------------------------------------

    public TestUnmodifiableByteList(String testName) {
        super(testName);
    }
    

    public static Test suite() {
        return new TestSuite(TestUnmodifiableByteList.class);
    }

    // framework
    // ------------------------------------------------------------------------

    protected ByteList makeUnmodifiableByteList() {
        return UnmodifiableByteList.wrap(makeByteList());
    }

    // tests
    // ------------------------------------------------------------------------

    public void testWrapNull() {
        assertNull(UnmodifiableByteList.wrap(null));
    }

    public void testWrapUnmodifiableByteList() {
        ByteList list = makeUnmodifiableByteList();
        assertSame(list,UnmodifiableByteList.wrap(list));
    }

    public void testWrapSerializableByteList() {
        ByteList list = makeByteList();
        assertTrue(list instanceof Serializable);
        assertTrue(UnmodifiableByteList.wrap(list) instanceof Serializable);
    }

    public void testWrapNonSerializableByteList() {
        ByteList list = makeByteList();
        ByteList ns = list.subList(0,list.size());
        assertTrue(!(ns instanceof Serializable));
        assertTrue(!(UnmodifiableByteList.wrap(ns) instanceof Serializable));
    }
}