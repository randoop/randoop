/*
 * Copyright 2002,2004 The Apache Software Foundation.
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
package org.apache.commons.jelly.core;

import java.util.Date;

import junit.framework.TestSuite;

import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.core.Customer;
import org.apache.commons.jelly.test.BaseJellyTest;

/**
 * @author Rodney Waldhoff
 * @version $Revision: 1.8 $ $Date: 2004/10/26 23:54:37 $
 */
public class TestNewTag extends BaseJellyTest {

    public TestNewTag(String name) {
        super(name);
    }

    public static TestSuite suite() throws Exception {
        return new TestSuite(TestNewTag.class);
    }

    public void testSimpleNew() throws Exception {
        setUpScript("testNewTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.simpleNew",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        assertNotNull(getJellyContext().getVariable("foo"));
        assertTrue(getJellyContext().getVariable("foo") instanceof Customer);
        Customer customer = (Customer)(getJellyContext().getVariable("foo"));
        assertNull(customer.getName());
    }

    public void testNewThenOverwrite() throws Exception {
        setUpScript("testNewTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.newThenOverwrite",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        assertNotNull(getJellyContext().getVariable("foo"));
        assertTrue(getJellyContext().getVariable("foo") instanceof Date);
    }

    public void testNewWithLiteralArg() throws Exception {
        setUpScript("testNewTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.newWithLiteralArg",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        assertNotNull(getJellyContext().getVariable("foo"));
        assertTrue(getJellyContext().getVariable("foo") instanceof Customer);
        Customer customer = (Customer)(getJellyContext().getVariable("foo"));
        assertNotNull(customer.getName());
        assertEquals("Jane Doe",customer.getName());
    }

    public void testNewWithTwoArgs() throws Exception {
        setUpScript("testNewTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.newWithTwoArgs",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        assertNotNull(getJellyContext().getVariable("foo"));
        assertTrue(getJellyContext().getVariable("foo") instanceof Customer);
        Customer customer = (Customer)(getJellyContext().getVariable("foo"));
        assertNotNull(customer.getName());
        assertEquals("Jane Doe",customer.getName());
        assertNotNull(customer.getCity());
        assertEquals("Chicago",customer.getCity());
    }

    public void testNewWithExpressionArg() throws Exception {
        setUpScript("testNewTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.newWithExpressionArg",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        assertNotNull(getJellyContext().getVariable("foo"));
        assertTrue(getJellyContext().getVariable("foo") instanceof Customer);
        Customer customer = (Customer)(getJellyContext().getVariable("foo"));
        assertNotNull(customer.getName());
        assertEquals("Jane Doe",customer.getName());
    }

    public void testNewWithNullArg() throws Exception {
        setUpScript("testNewTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.newWithNullArg",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        assertNotNull(getJellyContext().getVariable("foo"));
        assertTrue(getJellyContext().getVariable("foo") instanceof Customer);
        Customer customer = (Customer)(getJellyContext().getVariable("foo"));
        assertNull(customer.getName());
    }

    public void testNewWithNewArg() throws Exception {
        setUpScript("testNewTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.newWithNewArg",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        {
            assertNotNull(getJellyContext().getVariable("foo"));
            assertTrue(getJellyContext().getVariable("foo") instanceof Customer);
            Customer customer = (Customer)(getJellyContext().getVariable("foo"));
            assertNotNull(customer.getName());
            assertEquals("",customer.getName());
        }
        {
            assertNotNull(getJellyContext().getVariable("bar"));
            assertTrue(getJellyContext().getVariable("bar") instanceof Customer);
            Customer customer = (Customer)(getJellyContext().getVariable("bar"));
            assertEquals("Jane Doe",customer.getName());
            assertEquals("Chicago",customer.getCity());
            assertNotNull(customer.getOrders());
            assertEquals(1,customer.getOrders().size());
            assertNotNull(customer.getOrders().get(0));
        }
        {
            assertNotNull(getJellyContext().getVariable("qux"));
            assertTrue(getJellyContext().getVariable("qux") instanceof Customer);
            Customer customer = (Customer)(getJellyContext().getVariable("qux"));
            assertEquals("Jane Doe",customer.getName());
            assertEquals("Chicago",customer.getCity());
            assertNotNull(customer.getOrders());
            assertEquals(1,customer.getOrders().size());
            assertNotNull(customer.getOrders().get(0));
        }
    }

    public void testNewWithUseBeanArg() throws Exception {
        setUpScript("testNewTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.newWithUseBeanArg",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        assertNotNull(getJellyContext().getVariable("foo"));
        assertTrue(getJellyContext().getVariable("foo") instanceof Customer);
        Customer customer = (Customer)(getJellyContext().getVariable("foo"));
        assertEquals("Jane Doe",customer.getName());
        assertEquals("Chicago",customer.getCity());
        assertEquals("Location",customer.getLocation());
    }
}
