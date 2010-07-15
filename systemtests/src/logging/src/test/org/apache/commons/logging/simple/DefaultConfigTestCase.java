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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;


/**
 * <p>TestCase for simple logging when running with zero configuration
 * other than selecting the SimpleLog implementation.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2004/05/29 10:43:35 $
 */

public class DefaultConfigTestCase extends TestCase {


    // ----------------------------------------------------------- Constructors


    /**
     * <p>Construct a new instance of this test case.</p>
     *
     * @param name Name of the test case
     */
    public DefaultConfigTestCase(String name) {
        super(name);
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The {@link LogFactory} implementation we have selected.</p>
     */
    protected LogFactory factory = null;


    /**
     * <p>The {@link Log} implementation we have selected.</p>
     */
    protected Log log = null;


    // ------------------------------------------- JUnit Infrastructure Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        setUpFactory();
        setUpLog("TestLogger");
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(DefaultConfigTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        log = null;
        factory = null;
        LogFactory.releaseAll();
    }


    // ----------------------------------------------------------- Test Methods


    // Test pristine DecoratedSimpleLog instance
    public void testPristineDecorated() {

        setUpDecorated("DecoratedLogger");
        checkDecorated();

    }


    // Test pristine Log instance
    public void testPristineLog() {

        checkStandard();

    }


    // Test pristine LogFactory instance
    public void testPristineFactory() {

        assertNotNull("LogFactory exists", factory);
        assertEquals("LogFactory class",
                     "org.apache.commons.logging.impl.LogFactoryImpl",
                     factory.getClass().getName());

        String names[] = factory.getAttributeNames();
        assertNotNull("Names exists", names);
        assertEquals("Names empty", 0, names.length);

    }


    // Test Serializability of standard instance
    public void testSerializable() throws Exception {

        // Serialize and deserialize the instance
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(log);
        oos.close();
        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        log = (Log) ois.readObject();
        ois.close();

        // Check the characteristics of the resulting object
        checkStandard();

    }


    // -------------------------------------------------------- Support Methods



    // Check the decorated log instance
    protected void checkDecorated() {

        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.simple.DecoratedSimpleLog",
                     log.getClass().getName());

        // Can we call level checkers with no exceptions?
        assertTrue(!log.isDebugEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(!log.isTraceEnabled());
        assertTrue(log.isWarnEnabled());

        // Can we retrieve the current log level?
        assertEquals(SimpleLog.LOG_LEVEL_INFO, ((SimpleLog) log).getLevel());

        // Can we validate the extra exposed properties?
        assertEquals("yyyy/MM/dd HH:mm:ss:SSS zzz",
                     ((DecoratedSimpleLog) log).getDateTimeFormat());
        assertEquals("DecoratedLogger",
                     ((DecoratedSimpleLog) log).getLogName());
        assertTrue(!((DecoratedSimpleLog) log).getShowDateTime());
        assertTrue(((DecoratedSimpleLog) log).getShowShortName());

    }


    // Check the standard log instance
    protected void checkStandard() {

        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.impl.SimpleLog",
                     log.getClass().getName());

        // Can we call level checkers with no exceptions?
        assertTrue(!log.isDebugEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(!log.isTraceEnabled());
        assertTrue(log.isWarnEnabled());

        // Can we retrieve the current log level?
        assertEquals(SimpleLog.LOG_LEVEL_INFO, ((SimpleLog) log).getLevel());

    }


    // Set up decorated log instance
    protected void setUpDecorated(String name) {
        log = new DecoratedSimpleLog(name);
    }


    // Set up factory instance
    protected void setUpFactory() throws Exception {
        factory = LogFactory.getFactory();
    }


    // Set up log instance
    protected void setUpLog(String name) throws Exception {
        log = LogFactory.getLog(name);
    }


}
