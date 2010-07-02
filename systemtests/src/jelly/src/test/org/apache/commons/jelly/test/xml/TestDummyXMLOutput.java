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

import java.net.URL;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.jelly.Jelly;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;

/**
 * Confirm that <i>XMLOutput.createDummyXMLOutput()</i>
 * doesn't do anything funky.
 *
 * @author Morgan Delagrange
 * @version $Revision: 1.7 $
 */
public class TestDummyXMLOutput extends TestCase {

    Jelly jelly = null;
    JellyContext context = null;
    XMLOutput xmlOutput = null;

    public TestDummyXMLOutput(String name) {
        super(name);
    }

    public static TestSuite suite() throws Exception {
        return new TestSuite(TestDummyXMLOutput.class);
    }

    public void setUp(String scriptName) throws Exception {
        this.context = new JellyContext();
        this.xmlOutput = XMLOutput.createDummyXMLOutput();

        this.jelly = new Jelly();

        String script = scriptName;
        URL url = this.getClass().getResource(script);
        if ( url == null ) {
            throw new Exception(
                "Could not find Jelly script: " + script
                + " in package of class: " + this.getClass().getName()
            );
        }
        this.jelly.setUrl(url);
    }

    public void testDummyXMLOutput() throws Exception {
        // without validation
        setUp("producesOutput.jelly");
        Script script = this.jelly.compileScript();
        script.run(this.context,this.xmlOutput);
        assertTrue("should have set 'foo' variable to 'bar'",
                   this.context.getVariable("foo").equals("bar"));

    }

}
