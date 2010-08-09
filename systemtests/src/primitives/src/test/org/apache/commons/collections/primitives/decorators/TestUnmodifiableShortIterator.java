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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.primitives.ShortIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:32 $
 * @author Rodney Waldhoff
 */
public class TestUnmodifiableShortIterator extends BaseUnmodifiableShortIteratorTest {

    // conventional
    // ------------------------------------------------------------------------

    public TestUnmodifiableShortIterator(String testName) {
        super(testName);
    }
    

    public static Test suite() {
        return new TestSuite(TestUnmodifiableShortIterator.class);
    }

    // framework
    // ------------------------------------------------------------------------

    protected ShortIterator makeUnmodifiableShortIterator() {
        return UnmodifiableShortIterator.wrap(makeShortIterator());
    }

    // tests
    // ------------------------------------------------------------------------

    public void testWrapNotNull() {
        assertNotNull(UnmodifiableShortIterator.wrap(makeShortIterator()));
    }

    public void testWrapNull() {
        assertNull(UnmodifiableShortIterator.wrap(null));
    }

    public void testWrapUnmodifiableShortIterator() {
        ShortIterator iter = makeUnmodifiableShortIterator();
        assertSame(iter,UnmodifiableShortIterator.wrap(iter));
    }

}