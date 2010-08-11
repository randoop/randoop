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

import org.apache.commons.collections.primitives.FloatListIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:32 $
 * @author Rodney Waldhoff
 */
public class TestUnmodifiableFloatListIterator extends BaseUnmodifiableFloatListIteratorTest {

    // conventional
    // ------------------------------------------------------------------------

    public TestUnmodifiableFloatListIterator(String testName) {
        super(testName);
    }
    

    public static Test suite() {
        return new TestSuite(TestUnmodifiableFloatListIterator.class);
    }

    // framework
    // ------------------------------------------------------------------------

    protected FloatListIterator makeUnmodifiableFloatListIterator() {
        return UnmodifiableFloatListIterator.wrap(makeFloatListIterator());
    }

    // tests
    // ------------------------------------------------------------------------

    public void testWrapNotNull() {
        assertNotNull(UnmodifiableFloatListIterator.wrap(makeFloatListIterator()));
    }

    public void testWrapNull() {
        assertNull(UnmodifiableFloatListIterator.wrap(null));
    }

    public void testWrapUnmodifiableFloatListIterator() {
        FloatListIterator iter = makeUnmodifiableFloatListIterator();
        assertSame(iter,UnmodifiableFloatListIterator.wrap(iter));
    }

}