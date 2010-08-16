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
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;

/**
 * A helper class to run jelly test cases as part of Ant's JUnit tests
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.6 $
 */
public class TestDefaultNamespaceFilter extends TestCase {

    Jelly jelly = null;
    JellyContext context = null;
    XMLOutput xmlOutput = null;

    public TestDefaultNamespaceFilter(String name) {
        super(name);
    }

    public static TestSuite suite() throws Exception {
        return new TestSuite(TestDefaultNamespaceFilter.class);
    }

    public void setUp() throws Exception {
        context = new JellyContext();
        xmlOutput = XMLOutput.createXMLOutput(new StringWriter());

        jelly = new Jelly();

        String script = "nsFilterTest.jelly";
        URL url = this.getClass().getResource(script);
        if ( url == null ) {
            throw new Exception(
                "Could not find Jelly script: " + script
                + " in package of class: " + this.getClass().getName()
            );
        }
        jelly.setUrl(url);
    }

    public void testNamespaceDefined() throws Exception {
        jelly.setDefaultNamespaceURI("jelly:core");
        Script script = jelly.compileScript();
        script.run(context,xmlOutput);
        assertTrue("should have set 'usedDefaultNamespace' variable",
                   context.getVariable("usedDefaultNamespace") != null);
    }

    public void testNamespaceNotDefined() throws Exception {
        Script script = jelly.compileScript();
        script.run(context,xmlOutput);
        assertTrue("should not have set 'usedDefaultNamespace' variable",
                   context.getVariable("usedDefaultNamespace") == null);
    }
}
