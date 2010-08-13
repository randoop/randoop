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
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test this package.
 * 
 * @version $Revision: 1.9 $ $Date: 2004/02/25 20:46:32 $
 * @author Rodney Waldhoff
 */
public class PackageTestSuite extends TestCase {
    public PackageTestSuite(String testName) {
        super(testName);
    }

    public static void main(String args[]) {
        String[] testCaseName = { PackageTestSuite.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTest(TestBaseProxyByteCollection.suite());
        suite.addTest(TestBaseProxyByteList.suite());
        suite.addTest(TestUnmodifiableByteList.suite());
        suite.addTest(TestUnmodifiableByteIterator.suite());
        suite.addTest(TestUnmodifiableByteListIterator.suite());

        suite.addTest(TestBaseProxyCharCollection.suite());
        suite.addTest(TestBaseProxyCharList.suite());
        suite.addTest(TestUnmodifiableCharList.suite());
        suite.addTest(TestUnmodifiableCharIterator.suite());
        suite.addTest(TestUnmodifiableCharListIterator.suite());

        suite.addTest(TestBaseProxyDoubleCollection.suite());
        suite.addTest(TestBaseProxyDoubleList.suite());
        suite.addTest(TestUnmodifiableDoubleList.suite());
        suite.addTest(TestUnmodifiableDoubleIterator.suite());
        suite.addTest(TestUnmodifiableDoubleListIterator.suite());

        suite.addTest(TestBaseProxyFloatCollection.suite());
        suite.addTest(TestBaseProxyFloatList.suite());
        suite.addTest(TestUnmodifiableFloatList.suite());
        suite.addTest(TestUnmodifiableFloatIterator.suite());
        suite.addTest(TestUnmodifiableFloatListIterator.suite());

        suite.addTest(TestBaseProxyShortCollection.suite());
        suite.addTest(TestBaseProxyShortList.suite());
        suite.addTest(TestUnmodifiableShortList.suite());
        suite.addTest(TestUnmodifiableShortIterator.suite());
        suite.addTest(TestUnmodifiableShortListIterator.suite());

        suite.addTest(TestBaseProxyIntCollection.suite());
        suite.addTest(TestBaseProxyIntList.suite());
        suite.addTest(TestUnmodifiableIntList.suite());
        suite.addTest(TestUnmodifiableIntIterator.suite());
        suite.addTest(TestUnmodifiableIntListIterator.suite());

        suite.addTest(TestBaseProxyLongCollection.suite());
        suite.addTest(TestBaseProxyLongList.suite());
        suite.addTest(TestUnmodifiableLongList.suite());
        suite.addTest(TestUnmodifiableLongIterator.suite());
        suite.addTest(TestUnmodifiableLongListIterator.suite());

        return suite;
    }
}

