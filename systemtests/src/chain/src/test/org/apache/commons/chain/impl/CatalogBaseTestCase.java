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
package org.apache.commons.chain.impl;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;

import java.util.Iterator;


/**
 * <p>Test case for the <code>CatalogBase</code> class.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2004/02/25 00:01:05 $
 */

public class CatalogBaseTestCase extends TestCase {


    // ---------------------------------------------------- Instance Variables


    /**
     * The {@link Catalog} instance under test.
     */
    protected CatalogBase catalog = null;


    // ---------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public CatalogBaseTestCase(String name) {
        super(name);
    }


    // -------------------------------------------------- Overall Test Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        catalog = new CatalogBase();
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(CatalogBaseTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        catalog = null;
    }


    // ------------------------------------------------ Individual Test Methods


    // Test adding commands
    public void testAddCommand() {
        addCommands();
        checkCommandCount(8);
    }


    // Test getting commands
    public void testGetCommand() {

        addCommands();
        Command command = null;

        command = catalog.getCommand("AddingCommand");
        assertNotNull(command);
        assertTrue(command instanceof AddingCommand);

        command = catalog.getCommand("DelegatingCommand");
        assertNotNull(command);
        assertTrue(command instanceof DelegatingCommand);

        command = catalog.getCommand("DelegatingFilter");
        assertNotNull(command);
        assertTrue(command instanceof DelegatingFilter);

        command = catalog.getCommand("ExceptionCommand");
        assertNotNull(command);
        assertTrue(command instanceof ExceptionCommand);

        command = catalog.getCommand("ExceptionFilter");
        assertNotNull(command);
        assertTrue(command instanceof ExceptionFilter);

        command = catalog.getCommand("NonDelegatingCommand");
        assertNotNull(command);
        assertTrue(command instanceof NonDelegatingCommand);

        command = catalog.getCommand("NonDelegatingFilter");
        assertNotNull(command);
        assertTrue(command instanceof NonDelegatingFilter);

        command = catalog.getCommand("ChainBase");
        assertNotNull(command);
        assertTrue(command instanceof ChainBase);

    }


    // The getNames() method is implicitly tested by checkCommandCount()


    // Test pristine instance
    public void testPristine() {
        checkCommandCount(0);
        assertNull(catalog.getCommand("AddingCommand"));
        assertNull(catalog.getCommand("DelegatingCommand"));
        assertNull(catalog.getCommand("DelegatingFilter"));
        assertNull(catalog.getCommand("ExceptionCommand"));
        assertNull(catalog.getCommand("ExceptionFilter"));
        assertNull(catalog.getCommand("NonDelegatingCommand"));
        assertNull(catalog.getCommand("NonDelegatingFilter"));
        assertNull(catalog.getCommand("ChainBase"));
    }




    // -------------------------------------------------------- Support Methods


    // Add an interesting set of commands to the catalog
    protected void addCommands() {
        catalog.addCommand("AddingCommand", new AddingCommand("", null));
        catalog.addCommand("DelegatingCommand", new DelegatingCommand(""));
        catalog.addCommand("DelegatingFilter", new DelegatingFilter("", ""));
        catalog.addCommand("ExceptionCommand", new ExceptionCommand(""));
        catalog.addCommand("ExceptionFilter", new ExceptionFilter("", ""));
        catalog.addCommand("NonDelegatingCommand", new NonDelegatingCommand(""));
        catalog.addCommand("NonDelegatingFilter", new NonDelegatingFilter("", ""));
        catalog.addCommand("ChainBase", new ChainBase());
    }


    // Verify the number of configured commands
    protected void checkCommandCount(int expected) {
        int n = 0;
        Iterator names = catalog.getNames();
        while (names.hasNext()) {
            String name = (String) names.next();
            n++;
            assertNotNull(name + " exists", catalog.getCommand(name));
        }
        assertEquals("Correct command count", expected, n);
    }


}
