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
package org.apache.commons.jelly.tags.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.parser.XMLParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

/** Tests the parser, the engine and the XML tags
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.4 $
  */
public class TestXMLTags extends TestCase {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(TestXMLTags.class);

    /** basedir for test source */
    private static final String testBaseDir ="src/test/org/apache/commons/jelly/tags/xml";

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TestXMLTags.class);
    }

    public TestXMLTags(String testName) {
        super(testName);
    }

    public void testUnitTests() throws Exception {
        runUnitTest( testBaseDir + "/testForEach.jelly" );
    }

    public void testExpressions() throws Exception {
        runUnitTest( testBaseDir + "/testExpressions.jelly");
    }

    public void testParse() throws Exception {
        InputStream in = new FileInputStream(testBaseDir + "/example.jelly");
        XMLParser parser = new XMLParser();
        Script script = parser.parse(in);
        script = script.compile();
        log.debug("Found: " + script);
        assertTrue("Parsed a Script", script instanceof Script);
        StringWriter buffer = new StringWriter();
        script.run(parser.getContext(), XMLOutput.createXMLOutput(buffer));
        String text = buffer.toString().trim();
        if (log.isDebugEnabled()) {
            log.debug("Evaluated script as...");
            log.debug(text);
        }
        assertEquals("Produces the correct output", "It works!", text);
    }

    public void testTransform() throws Exception {
        String text = evaluteScriptAsText(testBaseDir + "/transformExample.jelly");
        assertEquals("Produces the correct output", "It works!", text);
    }

    public void testTransformAllInLine() throws Exception {
        String text = evaluteScriptAsText(testBaseDir + "/transformExampleAllInLine.jelly");
        assertEquals("Produces the correct output", "It works!", text);
    }

    public void testTransformParams() throws Exception {
        String text = evaluteScriptAsText(testBaseDir + "/transformParamExample.jelly");
        assertEquals("Produces the correct output", "It works!", text);
    }

    public void testTransformParamsInLine() throws Exception {

        String text = evaluteScriptAsText(testBaseDir + "/transformParamExample2.jelly");
        assertEquals("Produces the correct output", "It works!", text);
    }

    public void testTransformSAXOutput() throws Exception {
        String text = evaluteScriptAsText(testBaseDir + "/transformExampleSAXOutput.jelly");
        assertEquals("Produces the correct output", "It works!", text);
    }

    public void testTransformSAXOutputNestedTransforms() throws Exception {
        String text = evaluteScriptAsText(testBaseDir +
            "/transformExampleSAXOutputNestedTransforms.jelly");
        assertEquals("Produces the correct output", "It works!", text);
    }

    public void testTransformSchematron() throws Exception {
        String text = evaluteScriptAsText(testBaseDir +
            "/schematron/transformSchematronExample.jelly");
        assertEquals("Produces the correct output", "Report count=1:assert count=2", text);
    }

    public void testTransformXmlVar() throws Exception {
        String text = evaluteScriptAsText(testBaseDir +
            "/transformExampleXmlVar.jelly");
        assertEquals("Produces the correct output", "It works!", text);
    }

    public void testDoctype() throws Exception {
        String text = evaluteScriptAsText(testBaseDir +
            "/testDoctype.jelly");
        assertEquals("Produces the correct output", "<!DOCTYPE foo PUBLIC \"publicID\" \"foo.dtd\">\n<foo></foo>", text);
    }

    public void runUnitTest(String name) throws Exception {
        Document document = parseUnitTest(name);

        List failures = document.selectNodes( "/*/fail" );
        for ( Iterator iter = failures.iterator(); iter.hasNext(); ) {
            Node node = (Node) iter.next();
            fail( node.getStringValue() );
        }
    }

    public Document parseUnitTest(String name) throws Exception {
        // parse script
        InputStream in = new FileInputStream(name);
        XMLParser parser = new XMLParser();
        Script script = parser.parse(in);
        script = script.compile();
        assertTrue("Parsed a Script", script instanceof Script);
        StringWriter buffer = new StringWriter();
        script.run(parser.getContext(), XMLOutput.createXMLOutput(buffer));

        String text = buffer.toString().trim();
        if (log.isDebugEnabled()) {
            log.debug("Evaluated script as...");
            log.debug(text);
        }

        // now lets parse the output
        return DocumentHelper.parseText( text );
    }

    /**
     * Evaluates the script by the given file name and
     * returns the whitespace trimmed output as text
     */
    protected String evaluteScriptAsText(String fileName) throws Exception {
        JellyContext context = new JellyContext();

        // allow scripts to refer to any resource inside this project
        // using an absolute URI like /src/test/org/apache/foo.xml
        context.setRootURL(new File(".").toURL());

        // cature the output
        StringWriter buffer = new StringWriter();
        XMLOutput output = XMLOutput.createXMLOutput(buffer);

        context.runScript( new File(fileName), output );
        String text = buffer.toString().trim();
        if (log.isDebugEnabled()) {
            log.debug("Evaluated script as...");
            log.debug(text);
        }
        return text;
    }
}
