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
import org.apache.commons.jelly.test.BaseJellyTest;

/**
 * @author <a href="mailto:robert@bull-enterprises.com">Robert McIntosh</a>
 * @version $Revision: 1.9 $
 */
public class TestInvokeStaticTag extends BaseJellyTest {

    public TestInvokeStaticTag(String name) {
        super(name);
    }

    public static TestSuite suite() throws Exception {
        return new TestSuite(TestInvokeStaticTag.class);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     *  Gets the System property 'java.runtime.version' and compares it with,
     *  well, the same system property
     */
     public void testSimpleSystemInvoke() throws Exception {
        setUpScript( "testInvokeStaticTag.jelly" );
        Script script = getJelly().compileScript();

        getJellyContext().setVariable( "test.simpleSystemInvoke",Boolean.TRUE );

        getJellyContext().setVariable( "propertyName", "java.runtime.version" );
        script.run( getJellyContext(),getXMLOutput() );

        assertTrue( System.getProperty( "java.runtime.version" ).equals( getJellyContext().getVariable("propertyName" ) ) );
    }

     /**
     *  Sets the System property 'TEST PROPERTY' to the value 'Jelly is cool' and compares it with,
     *  well, the same system property
     */
    public void testSystemInvoke() throws Exception {
        setUpScript( "testInvokeStaticTag.jelly" );
        Script script = getJelly().compileScript();

        getJellyContext().setVariable( "test.systemInvoke",Boolean.TRUE );

        getJellyContext().setVariable( "propertyName", "TEST PROPERTY" );
        getJellyContext().setVariable( "propertyValue", "Jelly is cool" );
        script.run( getJellyContext(),getXMLOutput() );

        assertTrue( System.getProperty( "TEST PROPERTY" ).equals( "Jelly is cool" ) );

    }

     /**
     *  Uses the java.text.MessageFormat class to format a text message
     *  with 3 arguments.
     */
    public void testMessageFormatInvoke() throws Exception {
        System.out.println( System.getProperties() );
        setUpScript( "testInvokeStaticTag.jelly" );
        Script script = getJelly().compileScript();

        getJellyContext().setVariable( "test.messageFormatInvoke", Boolean.TRUE );

        Object[] args = new Object[3];
        args[0] = "Jelly";
        args[1] = "coolest";
        args[2] = "used";

        getJellyContext().setVariable( "args", args );
        getJellyContext().setVariable( "message", "Is not {0} the {1} thing you have ever {2}?" );
        script.run( getJellyContext(),getXMLOutput() );

        assertNotNull( getJellyContext().getVariable("message") );
        assertTrue( getJellyContext().getVariable("message").equals("Is not Jelly the coolest thing you have ever used?") );

    }

    public void testInvokeThatThrowsException() throws Exception {
        setUpScript( "testInvokeStaticTag.jelly" );
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.invokeThatThrowsException",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        String exceptionMessage = (String) getJellyContext().getVariable("exceptionMessage");
        assertNotNull( exceptionMessage );
        Exception jellyException = (Exception) getJellyContext().getVariable("jellyException");
        assertNull( jellyException );
        Exception exception = (Exception) getJellyContext().getVariable("exceptionThrown");
        assertNotNull( exception );
        assertEquals( exceptionMessage, exception.getMessage() );
    }

    public void testInvokeThatDoesNotHandleException() throws Exception {
        setUpScript( "testInvokeStaticTag.jelly" );
        Script script = getJelly().compileScript();
        getJellyContext().setVariable("test.invokeThatDoesNotHandleException",Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
        String exceptionMessage = (String) getJellyContext().getVariable("exceptionMessage");
        assertNotNull( exceptionMessage );
        JellyException jellyException = (JellyException) getJellyContext().getVariable("jellyException");
        assertNotNull( jellyException );
        assertTrue( "messages are the same", ! exceptionMessage.equals(jellyException.getMessage()) );
        assertTrue( "exception '" + jellyException.getMessage() + "' does not ends with '" +
                exceptionMessage+"'", jellyException.getMessage().endsWith(exceptionMessage) );
        assertNotNull( jellyException.getCause() );
        assertEquals( exceptionMessage, jellyException.getCause().getMessage() );
    }

}
