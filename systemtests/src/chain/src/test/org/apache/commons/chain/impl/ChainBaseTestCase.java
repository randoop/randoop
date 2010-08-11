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
import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;


/**
 * <p>Test case for the <code>ChainBase</code> class.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2005/01/08 04:15:23 $
 */

public class ChainBaseTestCase extends TestCase {


    // ---------------------------------------------------- Instance Variables


    /**
     * The {@link Chain} instance under test.
     */
    protected Chain chain = null;


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
    public ChainBaseTestCase(String name) {
        super(name);
    }


    // -------------------------------------------------- Overall Test Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        chain = new ChainBase();
        context = new ContextBase();
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ChainBaseTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        chain = null;
        context = null;
    }


    // ------------------------------------------------ Individual Test Methods


    // Test the ability to add commands
    public void testCommands() {

        checkCommandCount(0);

        Command command1 = new NonDelegatingCommand("1");
        chain.addCommand(command1);
        checkCommandCount(1);

        Command command2 = new DelegatingCommand("2");
        chain.addCommand(command2);
        checkCommandCount(2);

        Command command3 = new ExceptionCommand("3");
        chain.addCommand(command3);
        checkCommandCount(3);

    }


    // Test execution of a single non-delegating command
    public void testExecute1a() {
        chain.addCommand(new NonDelegatingCommand("1"));
        try {
            assertTrue("Chain returned true",
                       chain.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1");
    }


    // Test execution of a single delegating command
    public void testExecute1b() {
        chain.addCommand(new DelegatingCommand("1"));
        try {
            assertTrue("Chain returned false",
                       !chain.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1");
    }


    // Test execution of a single exception-throwing command
    public void testExecute1c() {
        chain.addCommand(new ExceptionCommand("1"));
        try {
            chain.execute(context);
        } catch (ArithmeticException e) {
            assertEquals("Correct exception id", "1", e.getMessage());
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1");
    }


    // Test execution of an attempt to add a new Command while executing
    public void testExecute1d() {
        chain.addCommand(new AddingCommand("1", chain));
        try {
            chain.execute(context);
        } catch (IllegalStateException e) {
            ; // Expected result
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1");
    }


    // Test execution of a chain that should return true
    public void testExecute2a() {
        chain.addCommand(new DelegatingCommand("1"));
        chain.addCommand(new DelegatingCommand("2"));
        chain.addCommand(new NonDelegatingCommand("3"));
        try {
            assertTrue("Chain returned true",
                       chain.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1/2/3");
    }


    // Test execution of a chain that should return false
    public void testExecute2b() {
        chain.addCommand(new DelegatingCommand("1"));
        chain.addCommand(new DelegatingCommand("2"));
        chain.addCommand(new DelegatingCommand("3"));
        try {
            assertTrue("Chain returned false",
                       !chain.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1/2/3");
    }


    // Test execution of a chain that should throw an exception
    public void testExecute2c() {
        chain.addCommand(new DelegatingCommand("1"));
        chain.addCommand(new DelegatingCommand("2"));
        chain.addCommand(new ExceptionCommand("3"));
        try {
            chain.execute(context);
        } catch (ArithmeticException e) {
            assertEquals("Correct exception id", "3", e.getMessage());
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1/2/3");
    }


    // Test execution of a chain that should throw an exception in the middle
    public void testExecute2d() {
        chain.addCommand(new DelegatingCommand("1"));
        chain.addCommand(new ExceptionCommand("2"));
        chain.addCommand(new NonDelegatingCommand("3"));
        try {
            chain.execute(context);
        } catch (ArithmeticException e) {
            assertEquals("Correct exception id", "2", e.getMessage());
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1/2");
    }


    // Test execution of a single non-delegating filter
    public void testExecute3a() {
        chain.addCommand(new NonDelegatingFilter("1", "a"));
        try {
            assertTrue("Chain returned true",
                       chain.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1/a");
    }


    // Test execution of a single delegating filter
    public void testExecute3b() {
        chain.addCommand(new DelegatingFilter("1", "a"));
        try {
            assertTrue("Chain returned false",
                       !chain.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1/a");
    }


    // Test execution of a single exception-throwing filter
    public void testExecute3c() {
        chain.addCommand(new ExceptionFilter("1", "a"));
        try {
            chain.execute(context);
        } catch (ArithmeticException e) {
            assertEquals("Correct exception id", "1", e.getMessage());
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1/a");
    }


    // Test execution of a chain that should return true
    public void testExecute4a() {
        chain.addCommand(new DelegatingFilter("1", "a"));
        chain.addCommand(new DelegatingCommand("2"));
        chain.addCommand(new NonDelegatingFilter("3", "c"));
        try {
            assertTrue("Chain returned true",
                       chain.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1/2/3/c/a");
    }


    // Test execution of a chain that should return false
    public void testExecute4b() {
        chain.addCommand(new DelegatingCommand("1"));
        chain.addCommand(new DelegatingFilter("2", "b"));
        chain.addCommand(new DelegatingCommand("3"));
        try {
            assertTrue("Chain returned false",
                       !chain.execute(context));
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1/2/3/b");
    }


    // Test execution of a chain that should throw an exception
    public void testExecute4c() {
        chain.addCommand(new DelegatingFilter("1", "a"));
        chain.addCommand(new DelegatingFilter("2", "b"));
        chain.addCommand(new ExceptionFilter("3", "c"));
        try {
            chain.execute(context);
        } catch (ArithmeticException e) {
            assertEquals("Correct exception id", "3", e.getMessage());
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1/2/3/c/b/a");
    }


    // Test execution of a chain that should throw an exception in the middle
    public void testExecute4d() {
        chain.addCommand(new DelegatingFilter("1", "a"));
        chain.addCommand(new ExceptionFilter("2", "b"));
        chain.addCommand(new NonDelegatingFilter("3", "c"));
        try {
            chain.execute(context);
        } catch (ArithmeticException e) {
            assertEquals("Correct exception id", "2", e.getMessage());
        } catch (Exception e) {
            fail("Threw exception: " + e);
        }
        checkExecuteLog("1/2/b/a");
    }


    // Test state of newly created instance
    public void testNewInstance() {
        checkCommandCount(0);
    }


    // -------------------------------------------------------- Support Methods


    // Verify the number of configured commands
    protected void checkCommandCount(int expected) {
        if (chain instanceof ChainBase) {
            Command commands[] = ((ChainBase) chain).getCommands();
            assertNotNull("getCommands() returned a non-null array",
                          commands);
            assertEquals("Correct command count", expected, commands.length);
        }
    }


    // Verify the contents of the execution log
    protected void checkExecuteLog(String expected) {
        StringBuffer log = (StringBuffer) context.get("log");
        assertNotNull("Context failed to return log", log);
        assertEquals("Context returned correct log",
                     expected, log.toString());
    }


}
