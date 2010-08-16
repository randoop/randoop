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
 
package org.apache.commons.logging.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.MemoryLog;

import junit.framework.*;

/**
 * Test the MemoryLog.
 * @author J&ouml;rg Schaible
 */
public class MemoryLogTest
        extends TestCase {
    
    public MemoryLogTest(String testName) {
        super(testName);
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        MemoryLog.reset();
    }
    
    public Log getLogObject()
    {
        return (Log) new MemoryLog(this.getClass().getName());
    }
    
    public final void testGetLogEntries()
    {
        MemoryLog log = (MemoryLog)getLogObject();
        log.setLevel(MemoryLog.LOG_LEVEL_DEBUG);
        log.trace("trace");
        log.debug("debug");
        log.info("info");
        log.warn("warn");
        log.error("error", new RuntimeException("error"));
        log.fatal("fatal", new RuntimeException("fatal"));
        List list = MemoryLog.getLogEntries();
        assertEquals(5, list.size());
        assertEquals("debug",((MemoryLog.Entry)list.get(0)).getMessage());
        assertEquals("info",((MemoryLog.Entry)list.get(1)).getMessage());
        assertEquals("warn",((MemoryLog.Entry)list.get(2)).getMessage());
        assertEquals("error",((MemoryLog.Entry)list.get(3)).getMessage());
        assertEquals("error",((MemoryLog.Entry)list.get(3)).getThrowable().getMessage());
        assertEquals("fatal",((MemoryLog.Entry)list.get(4)).getMessage());
        assertEquals("fatal",((MemoryLog.Entry)list.get(4)).getThrowable().getMessage());
        MemoryLog.reset();
        assertEquals(0, MemoryLog.getLogEntries().size());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(MemoryLogTest.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(MemoryLogTest.class);
        
        return suite;
    }
}
