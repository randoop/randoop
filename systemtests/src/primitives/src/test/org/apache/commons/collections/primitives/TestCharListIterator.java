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
package org.apache.commons.collections.primitives;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:30 $
 * @author Rodney Waldhoff
 */
public abstract class TestCharListIterator extends TestCharIterator {

    // conventional
    // ------------------------------------------------------------------------

    public TestCharListIterator(String testName) {
        super(testName);
    }

    // collections testing framework
    // ------------------------------------------------------------------------

    public CharIterator makeEmptyCharIterator() {
        return makeEmptyCharListIterator();
    }
    
    public CharIterator makeFullCharIterator() {
        return makeFullCharListIterator();
    }

    public abstract CharListIterator makeEmptyCharListIterator();
    public abstract CharListIterator makeFullCharListIterator();

    // tests
    // ------------------------------------------------------------------------


}
