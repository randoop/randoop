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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test this package.
 * 
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:29 $
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

        suite.addTest(TestAdapt.suite());
        
        suite.addTest(TestCollectionByteCollection.suite());
        suite.addTest(TestByteCollectionCollection.suite());
        suite.addTest(TestByteListList.suite());
        suite.addTest(TestListByteList.suite());
        suite.addTest(TestIteratorByteIterator.suite());
        suite.addTest(TestListIteratorByteListIterator.suite());
        suite.addTest(TestByteIteratorIterator.suite());
        suite.addTest(TestByteListIteratorListIterator.suite());

        suite.addTest(TestCollectionShortCollection.suite());
        suite.addTest(TestShortCollectionCollection.suite());
        suite.addTest(TestShortListList.suite());
        suite.addTest(TestListShortList.suite());
        suite.addTest(TestIteratorShortIterator.suite());
        suite.addTest(TestListIteratorShortListIterator.suite());
        suite.addTest(TestShortIteratorIterator.suite());
        suite.addTest(TestShortListIteratorListIterator.suite());

        suite.addTest(TestCollectionCharCollection.suite());
        suite.addTest(TestCharCollectionCollection.suite());
        suite.addTest(TestCharListList.suite());
        suite.addTest(TestListCharList.suite());
        suite.addTest(TestIteratorCharIterator.suite());
        suite.addTest(TestListIteratorCharListIterator.suite());
        suite.addTest(TestCharIteratorIterator.suite());
        suite.addTest(TestCharListIteratorListIterator.suite());

        suite.addTest(TestCollectionIntCollection.suite());
        suite.addTest(TestIntCollectionCollection.suite());
        suite.addTest(TestIntListList.suite());
        suite.addTest(TestListIntList.suite());
        suite.addTest(TestIteratorIntIterator.suite());
        suite.addTest(TestListIteratorIntListIterator.suite());
        suite.addTest(TestIntIteratorIterator.suite());
        suite.addTest(TestIntListIteratorListIterator.suite());
        
		suite.addTest(TestCollectionLongCollection.suite());
		suite.addTest(TestLongCollectionCollection.suite());
		suite.addTest(TestLongListList.suite());
		suite.addTest(TestListLongList.suite());
		suite.addTest(TestIteratorLongIterator.suite());
		suite.addTest(TestListIteratorLongListIterator.suite());
		suite.addTest(TestLongIteratorIterator.suite());
		suite.addTest(TestLongListIteratorListIterator.suite());

        suite.addTest(TestCollectionFloatCollection.suite());
        suite.addTest(TestFloatCollectionCollection.suite());
        suite.addTest(TestFloatListList.suite());
        suite.addTest(TestListFloatList.suite());
        suite.addTest(TestIteratorFloatIterator.suite());
        suite.addTest(TestListIteratorFloatListIterator.suite());
        suite.addTest(TestFloatIteratorIterator.suite());
        suite.addTest(TestFloatListIteratorListIterator.suite());

        suite.addTest(TestCollectionDoubleCollection.suite());
        suite.addTest(TestDoubleCollectionCollection.suite());
        suite.addTest(TestDoubleListList.suite());
        suite.addTest(TestListDoubleList.suite());
        suite.addTest(TestIteratorDoubleIterator.suite());
        suite.addTest(TestListIteratorDoubleListIterator.suite());
        suite.addTest(TestDoubleIteratorIterator.suite());
        suite.addTest(TestDoubleListIteratorListIterator.suite());

        return suite;
    }
}

