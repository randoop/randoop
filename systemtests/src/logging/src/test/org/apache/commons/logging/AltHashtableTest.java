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

public class AltHashtableTest extends TestCase {

	public AltHashtableTest(String testName) {
		super(testName);
	}
    
    public void testType() {
        assertTrue(LogFactory.factories instanceof AltHashtable);
    }
    
    public void testPutCalled() throws Exception {
    
        AltHashtable.lastKey = null;
        AltHashtable.lastValue = null;
        ClassLoader classLoader = new ClassLoader() {};
        Thread thread = new Thread(
            new Runnable() {
                public void run() {
                    LogFactory.getLog(AltHashtableTest.class);
                }
            }   
        );
        thread.setContextClassLoader(classLoader);
 
        thread.start();
        thread.join();
        
        assertEquals(classLoader, AltHashtable.lastKey);
        assertNotNull(AltHashtable.lastValue);
    }
}
