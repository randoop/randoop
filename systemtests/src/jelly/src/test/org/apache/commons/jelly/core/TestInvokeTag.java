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

import junit.framework.TestSuite;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.core.Customer;
import org.apache.commons.jelly.test.BaseJellyTest;

/**
 * @author Rodney Waldhoff
 * @version $Revision: 1.9 $ $Date: 2004/10/26 23:54:37 $
 */
public class TestInvokeTag extends BaseJellyTest {

    public TestInvokeTag(String name) {
        super(name);
    }

    public static TestSuite suite() throws Exception {
        return new TestSuite(TestInvokeTag.class);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleInvoke() throws Exception {
        setUpScript("testInvokeTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.simpleInvoke",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        assertNotNull(getJellyContext().getVariable("foo"));
        assertTrue(getJellyContext().getVariable("foo") instanceof Customer);
        Customer customer = (Customer)(getJellyContext().getVariable("foo"));
        assertEquals("Jane Doe",customer.getName());
        assertEquals("Chicago",customer.getCity());
        assertNotNull(customer.getOrders());
        assertEquals(1,customer.getOrders().size());
        assertNotNull(customer.getOrders().get(0));
    }

    public void testInvokeWithVar() throws Exception {
        setUpScript("testInvokeTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.invokeWithVar",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        assertNotNull(getJellyContext().getVariable("size"));
        assertTrue(getJellyContext().getVariable("size") instanceof Integer);
        Integer size = (Integer)(getJellyContext().getVariable("size"));
        assertEquals(3,size.intValue());
    }

    public void testInvokeWithReturnedValueAsArg() throws Exception {
        setUpScript("testInvokeTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.invokeWithReturnedValueAsArg",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        assertNotNull(getJellyContext().getVariable("customer"));
        assertTrue(getJellyContext().getVariable("customer") instanceof Customer);
        Customer customer = (Customer)(getJellyContext().getVariable("customer"));
        assertEquals("Jane Doe",customer.getName());
        assertEquals("Chicago",customer.getCity());
    }

    public void testInvokeWithReturnedValueAsArgAndVar() throws Exception {
        setUpScript("testInvokeTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.invokeWithReturnedValueAsArgAndVar",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        assertNotNull(getJellyContext().getVariable("customer"));
        assertTrue(getJellyContext().getVariable("customer") instanceof Customer);
        Customer customer = (Customer)(getJellyContext().getVariable("customer"));
        assertEquals("Jane Doe",customer.getName());
        assertEquals("Chicago",customer.getCity());
        assertNotNull(getJellyContext().getVariable("argtwo"));
        assertEquals("Chicago",getJellyContext().getVariable("argtwo"));
    }

    public void testInvokeThatThrowsException() throws Exception {
        setUpScript("testInvokeTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.invokeThatThrowsException",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        String exceptionMessage = (String) getJellyContext().getVariable("exceptionMessage");
        assertNotNull( exceptionMessage );
        assertNotNull( getJellyContext().getVariable("exceptionBean"));
        Exception jellyException = (Exception) getJellyContext().getVariable("jellyException");
        assertNull( jellyException );
        Exception exception = (Exception) getJellyContext().getVariable("exceptionThrown");
        assertNotNull( exception );
        assertEquals( exceptionMessage, exception.getMessage() );
    }

    public void testInvokeThatDoesNotHandleException() throws Exception {
        setUpScript("testInvokeTag.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.invokeThatDoesNotHandleException",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        String exceptionMessage = (String) getJellyContext().getVariable("exceptionMessage");
        assertNotNull( exceptionMessage );
        assertNotNull( getJellyContext().getVariable("exceptionBean"));
        JellyException jellyException = (JellyException) getJellyContext().getVariable("jellyException");
        assertNotNull( jellyException );
        assertTrue( "messages are the same", ! exceptionMessage.equals(jellyException.getMessage()) );
        assertTrue( "exception '" + jellyException.getMessage() + "' does not ends with '" +
                exceptionMessage+"'", jellyException.getMessage().endsWith(exceptionMessage) );
        assertNotNull( jellyException.getCause() );
        assertEquals( exceptionMessage, jellyException.getCause().getMessage() );
    }


}
