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
package org.apache.commons.jelly;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.jelly.impl.TextScript;
import org.apache.commons.jelly.parser.XMLParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Tests the core tags
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.15 $
  */
public class TestCoreTags extends TestCase {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(TestCoreTags.class);

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TestCoreTags.class);
    }

    public TestCoreTags(String testName) {
        super(testName);
    }

    public void testArgs() throws Exception {
        InputStream in = new FileInputStream("src/test/org/apache/commons/jelly/test_args.jelly");
        XMLParser parser = new XMLParser();
        Script script = parser.parse(in);
        script = script.compile();
        log.debug("Found: " + script);
        assertTrue("Parsed a Script", script instanceof Script);
        String[] args = { "one", "two", "three" };
        JellyContext context = new JellyContext();
        context.setVariable("args", args);
        StringWriter buffer = new StringWriter();
        script.run(context, XMLOutput.createXMLOutput(buffer));
        String text = buffer.toString().trim();
        if (log.isDebugEnabled()) {
            log.debug("Evaluated script as...");
            log.debug(text);
        }
        assertEquals("Produces the correct output", "one two three", text);
    }

    public void testTrimEndWhitespace() throws Exception {
        TextScript textScript = new TextScript(" ");
        textScript.trimEndWhitespace();
        assertEquals("", textScript.getText());

        textScript = new TextScript("");
        textScript.trimEndWhitespace();
        assertEquals("", textScript.getText());

        textScript = new TextScript(" foo ");
        textScript.trimEndWhitespace();
        assertEquals(" foo", textScript.getText());

        textScript = new TextScript("foo");
        textScript.trimEndWhitespace();
        assertEquals("foo", textScript.getText());
    }

    public void testTrimStartWhitespace() throws Exception {
        TextScript textScript = new TextScript(" ");
        textScript.trimStartWhitespace();
        assertEquals("", textScript.getText());

        textScript = new TextScript("");
        textScript.trimStartWhitespace();
        assertEquals("", textScript.getText());

        textScript = new TextScript(" foo ");
        textScript.trimStartWhitespace();
        assertEquals("foo ", textScript.getText());

        textScript = new TextScript("foo");
        textScript.trimStartWhitespace();
        assertEquals("foo", textScript.getText());
    }
}
