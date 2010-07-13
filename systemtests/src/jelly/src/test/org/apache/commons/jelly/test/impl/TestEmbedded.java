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
package org.apache.commons.jelly.test.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.Embedded;
import org.xml.sax.InputSource;

/**
 *  Unit case  of Embedded
 *
 * @author <a href="mailto:vinayc@apache.org">Vinay Chandran</a>
 */
public class TestEmbedded extends TestCase
{

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    public static Test suite()
    {
        return new TestSuite(TestEmbedded.class);
    }

    public TestEmbedded(String testName)
    {
        super(testName);
    }

    /**
     *  test Script input as a java.lang.String object
     */
    public void testStringAsScript()
    {
        Embedded embedded = new Embedded();
        String jellyScript =
            "<?xml version=\"1.0\"?>"
                + " <j:jelly xmlns:j=\"jelly:core\">"
                + "jelly-test-case"
                + " </j:jelly>";
        embedded.setScript(jellyScript);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        embedded.setOutputStream(baos);
        boolean status = embedded.execute();
        //executed properly without script errors
        assertTrue("Emebedded execution failed", status);
        //check that the output  confirms the exepected
        assertEquals("jelly-test-case", new String(baos.toByteArray()));
        //test generation of error
        embedded.setScript(jellyScript + "obnoxious-part");
        status = embedded.execute();
        //test failure of execution
        assertFalse("A script with bad XML was executed successfully", status);
        //Asserting the parser generated a errorMsg
        assertNotNull("A script with bad XML didn't generate an error message", embedded.getErrorMsg());
    }

    /**
     *  test Script input as a InputStream
     */
    public void testInputStreamAsScript()
    {
        Embedded embedded = new Embedded();
        String jellyScript =
            "<?xml version=\"1.0\"?>"
                + " <j:jelly xmlns:j=\"jelly:core\">"
                + "jelly-test-case"
                + " </j:jelly>";
        embedded.setScript(new ByteArrayInputStream(jellyScript.getBytes()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        embedded.setOutputStream(baos);
        boolean status = embedded.execute();
        //executed properly without script errors
        assertEquals(status, true);
        //check that the output confirms the expected
        assertEquals("jelly-test-case", new String(baos.toByteArray()));
    }
    
    /**
     * Test simple 'raw' execution of a string. See JELLY-189.
     */
    public void testRawExecuteAsString() throws Exception
    {
        String message =
            "<?xml version=\"1.0\"?>"
                + " <j:jelly xmlns:j=\"jelly:core\">"
                + "jelly-test-case"
                + " </j:jelly>";
       ByteArrayOutputStream output = new ByteArrayOutputStream();
       XMLOutput xmlOutput = XMLOutput.createXMLOutput(output);
       InputSource script = new InputSource( new StringReader(message.toString()) );
       JellyContext context = new JellyContext();
       context.runScript( script, xmlOutput);
       output.close();
       //check that the output confirms the expected
       assertEquals("jelly-test-case", new String(output.toByteArray()));
    }
}
