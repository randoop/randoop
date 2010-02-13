/*
 * Copyright 2002-2004 The Apache Software Foundation
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test all the packages.
 * 
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:30 $
 * @author Stephen Colebourne
 */
public class AllTestSuite extends TestCase {
    public AllTestSuite(String testName) {
        super(testName);
    }

    public static void main(String args[]) {
        String[] testCaseName = { AllTestSuite.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTest(org.apache.commons.collections.primitives.PackageTestSuite.suite());
        suite.addTest(org.apache.commons.collections.primitives.adapters.PackageTestSuite.suite());
        suite.addTest(org.apache.commons.collections.primitives.adapters.io.PackageTestSuite.suite());
        suite.addTest(org.apache.commons.collections.primitives.decorators.PackageTestSuite.suite());
        
        return suite;
    }
}

