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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 *
 * 
 * 
 * 
 * 
 */
public class LogTest extends AbstractLogTest
{

    /**
     * 
     * 
     * @param testName
     * 
     */
    public LogTest(String testName)
    {
        super(testName);
    }

    /**
     * 
     * 
     * 
     */
    public Log getLogObject()
    {
        /**
         * Pickup whatever is found/configured!
         */
        return LogFactory.getLog(this.getClass().getName());
    }

    public static void main(String[] args)
    {
            String[] testCaseName = { LogTest.class.getName() };
            junit.textui.TestRunner.main(testCaseName);    
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(LogTest.class);
        
        return suite;
    }
    
}
