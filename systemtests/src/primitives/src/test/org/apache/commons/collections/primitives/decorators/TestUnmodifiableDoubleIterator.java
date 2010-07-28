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

import org.apache.commons.collections.primitives.DoubleIterator;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:32 $
 * @author Rodney Waldhoff
 */
public class TestUnmodifiableDoubleIterator extends BaseUnmodifiableDoubleIteratorTest {

    // conventional
    // ------------------------------------------------------------------------

    public TestUnmodifiableDoubleIterator(String testName) {
        super(testName);
    }
    

    public static Test suite() {
        return new TestSuite(TestUnmodifiableDoubleIterator.class);
    }

    // framework
    // ------------------------------------------------------------------------

    protected DoubleIterator makeUnmodifiableDoubleIterator() {
        return UnmodifiableDoubleIterator.wrap(makeDoubleIterator());
    }

    // tests
    // ------------------------------------------------------------------------

    public void testWrapNotNull() {
        assertNotNull(UnmodifiableDoubleIterator.wrap(makeDoubleIterator()));
    }

    public void testWrapNull() {
        assertNull(UnmodifiableDoubleIterator.wrap(null));
    }

    public void testWrapUnmodifiableDoubleIterator() {
        DoubleIterator iter = makeUnmodifiableDoubleIterator();
        assertSame(iter,UnmodifiableDoubleIterator.wrap(iter));
    }

}