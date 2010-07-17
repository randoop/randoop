/*
 * Copyright 1999-2004 The Apache Software Foundation
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
package org.apache.commons.chain.generic;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.impl.CatalogBase;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.chain.impl.CatalogFactoryBase;
import org.apache.commons.chain.impl.DelegatingCommand;
import org.apache.commons.chain.impl.NonDelegatingCommand;

/**
 * <p>Test case for the <code>LookupCommand</code> class.</p>
 *
 * @author Sean Schofield
 * @version $Revision: 1.2 $
 */

public class LookupCommandTestCase extends TestCase {


    // ---------------------------------------------------- Instance Variables

    /**
     * The instance of {@link Catalog} to use when looking up commands
     */
    protected Catalog catalog;

    /**
     * The {@link LookupCommand} instance under test.
     */
    protected LookupCommand command;

    /**
     * The {@link Context} instance on which to execute the chain.
     */
    protected Context context = null;


    // ---------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public LookupCommandTestCase(String name) {
        super(name);
    }


    // -------------------------------------------------- Overall Test Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        catalog = new CatalogBase();
        CatalogFactoryBase.getInstance().setCatalog(catalog);
        command = new LookupCommand();        
        context = new ContextBase();
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(LookupCommandTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        catalog = null;
        CatalogFactoryBase.getInstance().clear();
        command = null;
        context = null;
    }


    // ------------------------------------------------ Individual Test Methods


    // Test ability to lookup and execute single non-delegating command
    public void testExecuteMethodLookup_1a() {

        // use default catalog
        catalog.addCommand("foo", new NonDelegatingCommand("1a"));
        command.setName("foo");

        try {
            assertTrue("Command should return true",
                       command.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1a");
    }

    // Test ability to lookup and execute a chain
    public void testExecuteMethodLookup_1b() {

        ChainBase chain = new ChainBase();
        chain.addCommand(new DelegatingCommand("1b1"));
        chain.addCommand(new DelegatingCommand("1b2"));
        chain.addCommand(new NonDelegatingCommand("1b3"));
        
        // use default catalog
        catalog.addCommand("foo", chain);
        command.setName("foo");

        try {
            assertTrue("Command should return true",
                       command.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1b1/1b2/1b3");
    }

    // Test ability to lookup and execute single non-delegating command
    // using the context
    public void testExecuteMethodLookup_2a() {

        // use default catalog
        catalog.addCommand("foo", new NonDelegatingCommand("2a"));
        command.setNameKey("nameKey");
        context.put("nameKey", "foo");

        try {
            assertTrue("Command should return true",
                       command.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("2a");
    }

    // Test ability to lookup and execute a chain using the context 
    public void testExecuteMethodLookup_2b() {

        ChainBase chain = new ChainBase();
        chain.addCommand(new DelegatingCommand("2b1"));
        chain.addCommand(new DelegatingCommand("2b2"));
        chain.addCommand(new NonDelegatingCommand("2b3"));

        // use default catalog
        catalog.addCommand("foo", chain);
        command.setNameKey("nameKey");
        context.put("nameKey", "foo");

        try {
            assertTrue("Command should return true",
                       command.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("2b1/2b2/2b3");
    }


    // -------------------------------------------------------- Support Methods


    // Verify the contents of the execution log
    protected void checkExecuteLog(String expected) {
        StringBuffer log = (StringBuffer) context.get("log");
        assertNotNull("Context failed to return log", log);
        assertEquals("Context returned correct log",
                     expected, log.toString());
    }


}
