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

package org.apache.commons.logging.simple;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.impl.SimpleLog;


/**
 * <p>TestCase for simple logging when running with custom configuration
 * properties.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2004/05/30 10:32:25 $
 */
public class CustomConfigTestCase extends DefaultConfigTestCase {


    // ----------------------------------------------------------- Constructors


    /**
     * <p>Construct a new instance of this test case.</p>
     *
     * @param name Name of the test case
     */
    public CustomConfigTestCase(String name) {
        super(name);
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The expected log records.</p>
     */
    protected List expected;


    /**
     * <p>The message levels that should have been logged.</p>
     */
    /*
    protected Level testLevels[] =
    { Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE, Level.SEVERE };
    */


    /**
     * <p>The message strings that should have been logged.</p>
     */
    protected String testMessages[] =
    { "debug", "info", "warn", "error", "fatal" };


    // ------------------------------------------- JUnit Infrastructure Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        expected = new ArrayList();
        setUpFactory();
        setUpLog("DecoratedLogger");
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(CustomConfigTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
        expected = null;
    }


    // ----------------------------------------------------------- Test Methods


    // Test logging message strings with exceptions
    public void testExceptionMessages() throws Exception {

        ((DecoratedSimpleLog) log).clearCache();
        logExceptionMessages();
        checkExpected();

    }


    // Test logging plain message strings
    public void testPlainMessages() throws Exception {

        ((DecoratedSimpleLog) log).clearCache();
        logPlainMessages();
        checkExpected();

    }


    // Test Serializability of standard instance
    public void testSerializable() throws Exception {

        ((DecoratedSimpleLog) log).clearCache();
        logPlainMessages();
        super.testSerializable();
        logExceptionMessages();
        checkExpected();

    }


    // -------------------------------------------------------- Support Methods


    // Check the decorated log instance
    protected void checkDecorated() {

        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.simple.DecoratedSimpleLog",
                     log.getClass().getName());

        // Can we call level checkers with no exceptions?
        assertTrue(log.isDebugEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(!log.isTraceEnabled());
        assertTrue(log.isWarnEnabled());

        // Can we retrieve the current log level?
        assertEquals(SimpleLog.LOG_LEVEL_DEBUG, ((SimpleLog) log).getLevel());

        // Can we validate the extra exposed properties?
        checkDecoratedDateTime();
        assertEquals("DecoratedLogger",
                     ((DecoratedSimpleLog) log).getLogName());
        checkShowDateTime();
        assertTrue(((DecoratedSimpleLog) log).getShowShortName());

    }
    
    /** Hook for subclassses */
    protected void checkShowDateTime() {
        assertTrue(!((DecoratedSimpleLog) log).getShowDateTime());
    }
    
    /** Hook for subclasses */
    protected void checkDecoratedDateTime() {
            assertEquals("yyyy/MM/dd HH:mm:ss:SSS zzz",
                     ((DecoratedSimpleLog) log).getDateTimeFormat());
    }
    


    // Check the actual log records against the expected ones
    protected void checkExpected() {

        List acts = ((DecoratedSimpleLog) log).getCache();
        Iterator exps = expected.iterator();
        int n = 0;
        while (exps.hasNext()) {
            LogRecord exp = (LogRecord) exps.next();
            LogRecord act = (LogRecord) acts.get(n++);
            assertEquals("Row " + n + " type", exp.type, act.type);
            assertEquals("Row " + n + " message", exp.message, act.message);
            assertEquals("Row " + n + " throwable", exp.t, act.t);
        }

    }


    // Check the standard log instance
    protected void checkStandard() {

        checkDecorated();

    }


    // Log the messages with exceptions
    protected void logExceptionMessages() {

        // Generate log records
        Throwable t = new IndexOutOfBoundsException();
        log.trace("trace", t); // Should not actually get logged
        log.debug("debug", t);
        log.info("info", t);
        log.warn("warn", t);
        log.error("error", t);
        log.fatal("fatal", t);

        // Record the log records we expect
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_DEBUG, "debug", t));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_INFO, "info", t));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_WARN, "warn", t));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_ERROR, "error", t));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_FATAL, "fatal", t));

    }


    // Log the plain messages
    protected void logPlainMessages() {

        // Generate log records
        log.trace("trace"); // Should not actually get logged
        log.debug("debug");
        log.info("info");
        log.warn("warn");
        log.error("error");
        log.fatal("fatal");

        // Record the log records we expect
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_DEBUG, "debug", null));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_INFO, "info", null));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_WARN, "warn", null));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_ERROR, "error", null));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_FATAL, "fatal", null));

    }


}
