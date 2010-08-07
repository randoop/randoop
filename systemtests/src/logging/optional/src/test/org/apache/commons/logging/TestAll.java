/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
 
 
package org.apache.commons.logging;

import junit.framework.*;
import org.apache.commons.logging.impl.MemoryLogTest;
import org.apache.commons.logging.impl.WeakHashtableTest;

/**
  * <p> The build script calls just one <code>TestSuite</code> - this one!
  * All tests should be written into separate <code>TestSuite</code>'s
  * and added to this. Don't clutter this class with implementations. </p>
  *
  * @version $Revision: 1.2 $
 */
public class TestAll extends TestCase {

    public TestAll(String testName) {
        super(testName);
    }

    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        suite.addTest(MemoryLogTest.suite());
        suite.addTestSuite(WeakHashtableTest.class);
        suite.addTestSuite(LogFactoryTest.class);
        
        return suite;
    }

    /**
     * This allows the tests to run as a standalone application.
     */
    public static void main(String args[]) {
        String[] testCaseName = { TestAll.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }
}
