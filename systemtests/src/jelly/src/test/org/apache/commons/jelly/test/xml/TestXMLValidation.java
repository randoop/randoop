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
package org.apache.commons.jelly.test.xml;

import java.io.StringWriter;
import java.net.URL;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.jelly.Jelly;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;

/**
 * A test to confirm that invalid documents are
 * reject iff jelly.setValidateXML(true)
 *
 * @author Morgan Delagrange
 * @version $Revision: 1.9 $
 */
public class TestXMLValidation extends TestCase {

    Jelly jelly = null;
    JellyContext context = null;
    XMLOutput xmlOutput = null;

    public TestXMLValidation(String name) {
        super(name);
    }

    public static TestSuite suite() throws Exception {
        return new TestSuite(TestXMLValidation.class);
    }

    public void setUp(String scriptName) throws Exception {
        context = new JellyContext();
        xmlOutput = XMLOutput.createXMLOutput(new StringWriter());

        jelly = new Jelly();

        String script = scriptName;
        URL url = this.getClass().getResource(script);
        if ( url == null ) {
            throw new Exception(
                "Could not find Jelly script: " + script
                + " in package of class: " + this.getClass().getName()
            );
        }
        jelly.setUrl(url);
    }

    public void testInvalidXML1NoValidation() throws Exception {
        // without validation
        setUp("invalidScript1.jelly");
        Script script = jelly.compileScript();
        script.run(context,xmlOutput);
        assertTrue("should have set 'foo' variable to 'bar'",
                   context.getVariable("foo").equals("bar"));

        // do it again, explicitly setting the validateXML variable
        setUp("invalidScript1.jelly");
        jelly.setValidateXML(false);
        script = jelly.compileScript();
        script.run(context,xmlOutput);
        assertTrue("should have set 'foo' variable to 'bar'",
                   context.getVariable("foo").equals("bar"));
    }

    public void testInvalidXML1Validation() throws Exception {
        // with validation
        setUp("invalidScript1.jelly");
        jelly.setValidateXML(true);
        try {
            Script script = jelly.compileScript();
            fail("Invalid scripts should throw JellyException on parse");
        } catch (JellyException e) {
        }
    }

    public void testValidXML1Validation()throws Exception {
        // with validation
        setUp("validScript1.jelly");
        jelly.setValidateXML(true);
        Script script = jelly.compileScript();
        script.run(context,xmlOutput);
        assertTrue("should have set 'foo' variable to 'bar'",
                   context.getVariable("foo").equals("bar"));
    }

}
