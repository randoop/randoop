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

package org.apache.commons.logging.log4j;


import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;


/**
 * <p>TestCase for Log4J logging when running on a system with Log4J present,
 * so that Log4J should be selected and an appropriate
 * logger configured per the configuration properties.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.9 $ $Date: 2004/05/19 20:59:56 $
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
     * <p>The <code>Appender</code> we are utilizing.</p>
     */
    protected TestAppender appender = null;


    /**
     * <p>The <code>Logger</code> we are utilizing.</p>
     */
    protected Logger logger = null;


    /**
     * <p>The message levels that should have been logged.</p>
     */
    protected Level testLevels[] =
    { Level.INFO, Level.WARN, Level.ERROR, Level.FATAL };


    /**
     * <p>The message strings that should have been logged.</p>
     */
    protected String testMessages[] =
    { "info", "warn", "error", "fatal" };


    // ------------------------------------------- JUnit Infrastructure Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        setUpAppender
            ("org/apache/commons/logging/log4j/CustomConfig.properties");
        setUpLogger("TestLogger");
        setUpFactory();
        setUpLog("TestLogger");
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
        Logger.getRootLogger().removeAppender(appender);
        appender = null;
        logger = null;
    }


    // ----------------------------------------------------------- Test Methods


    // Test logging message strings with exceptions
    public void testExceptionMessages() throws Exception {

        logExceptionMessages();
        checkLoggingEvents(true);

    }


    // Test logging plain message strings
    public void testPlainMessages() throws Exception {

        logPlainMessages();
        checkLoggingEvents(false);

    }


    // Test pristine Appender instance
    public void testPristineAppender() {

        assertNotNull("Appender exists", appender);

    }


    // Test pristine Log instance
    public void testPristineLog() {

        super.testPristineLog();

    }


    // Test pristine Logger instance
    public void testPristineLogger() {

        assertNotNull("Logger exists", logger);
        assertEquals("Logger level", Level.INFO, logger.getEffectiveLevel());
        assertEquals("Logger name", "TestLogger", logger.getName());

    }


    // Test Serializability of Log instance
    public void testSerializable() throws Exception {

        super.testSerializable();
        testExceptionMessages();

    }


    // -------------------------------------------------------- Support Methods


    // Check the log instance
    protected void checkLog() {

        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.impl.Log4JLogger",
                     log.getClass().getName());

        // Assert which logging levels have been enabled
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isWarnEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(!log.isDebugEnabled());
        assertTrue(!log.isTraceEnabled());

    }


    // Check the recorded messages
    protected void checkLoggingEvents(boolean thrown) {
        Iterator events = appender.events();
        for (int i = 0; i < testMessages.length; i++) {
            assertTrue("Logged event " + i + " exists",events.hasNext());
            LoggingEvent event = (LoggingEvent) events.next();
            assertEquals("LoggingEvent level",
                         testLevels[i], event.getLevel());
            assertEquals("LoggingEvent message",
                         testMessages[i], event.getMessage());
            /* Does not appear to be logged correctly?
            assertEquals("LoggingEvent class",
                         this.getClass().getName(),
                         event.getLocationInformation().getClassName());
            */
            /* Does not appear to be logged correctly?
            if (thrown) {
                assertEquals("LoggingEvent method",
                             "logExceptionMessages",
                             event.getLocationInformation().getMethodName());
            } else {
                assertEquals("LoggingEvent method",
                             "logPlainMessages",
                             event.getLocationInformation().getMethodName());
            }
            */
            if (thrown) {
                assertNotNull("LoggingEvent thrown",
                              event.getThrowableInformation().getThrowableStrRep());
                assertTrue("LoggingEvent thrown type",
                           event.getThrowableInformation()
                                .getThrowableStrRep()[0]
                                    .indexOf("IndexOutOfBoundsException")>0);
            } else {
                assertNull("LoggingEvent thrown",
                           event.getThrowableInformation());
            }
        }
        assertTrue(!events.hasNext());
        appender.flush();
    }


    // Log the messages with exceptions
    protected void logExceptionMessages() {
        Throwable t = new IndexOutOfBoundsException();
        log.trace("trace", t); // Should not actually get logged
        log.debug("debug", t); // Should not actually get logged
        log.info("info", t);
        log.warn("warn", t);
        log.error("error", t);
        log.fatal("fatal", t);
    }


    // Log the plain messages
    protected void logPlainMessages() {
        log.trace("trace"); // Should not actually get logged
        log.debug("debug"); // Should not actually get logged
        log.info("info");
        log.warn("warn");
        log.error("error");
        log.fatal("fatal");
    }


    // Set up our custom Appender
    protected void setUpAppender(String config) throws Exception {
        Properties props = new Properties();
        InputStream is =
            this.getClass().getClassLoader().getResourceAsStream(config);
        props.load(is);
        is.close();
        PropertyConfigurator.configure(props);
        Enumeration appenders = Logger.getRootLogger().getAllAppenders();
        appender = (TestAppender) appenders.nextElement();
    }


    // Set up our custom Logger
    protected void setUpLogger(String name) throws Exception {
        logger = Logger.getLogger(name);
    }


}
