/*
 * Copyright 2001-2004 The Apache Software Foundation.
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


/**
  * <p> The build script calls just one <code>TestSuite</code> - this one!
  * All tests should be written into separate <code>TestSuite</code>'s
  * and added to this. Don't clutter this class with implementations. </p>
  *
  * <p> This class is based on <code>org.apache.commons.betwixt.TestAll</code> 
  * coded by James Strachan. </p>
  *
  * @author Robert Burrell Donkin
  * @version $Revision: 1.7 $
 */
public class TestAll extends TestCase {

    public TestAll(String testName) {
        super(testName);
    }

    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        suite.addTest(SimpleLogTest.suite());
        suite.addTest(NoOpLogTest.suite());
        suite.addTest(LogTest.suite());
        
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
