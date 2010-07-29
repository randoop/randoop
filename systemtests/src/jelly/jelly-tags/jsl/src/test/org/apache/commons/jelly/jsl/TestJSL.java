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
package org.apache.commons.jelly.jsl;

import java.io.FileInputStream;
import java.io.InputStream;

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
import org.dom4j.Element;
import org.dom4j.io.SAXContentHandler;

/**
 * Tests the JSL tags.
 * Note this test harness could be written in Jelly script
 * if we had the junit tag library!
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.8 $
 */
public class TestJSL extends TestCase {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(TestJSL.class);

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TestJSL.class);
    }

    public TestJSL(String testName) {
        super(testName);
    }

    public void testExample1() throws Exception {
        Document document = runScript( "src/test/org/apache/commons/jelly/jsl/example.jelly" );
        Element small = (Element) document.selectSingleNode("/html/body/small");

        assertTrue( "<small> starts with 'James Elson'", small.getText().startsWith("James Elson") );
        assertEquals( "I am a title!", small.valueOf( "h2" ).trim() );
        assertEquals( "Twas a dark, rainy night...", small.valueOf( "small" ).trim() );
        assertEquals( "dfjsdfjsdf", small.valueOf( "p" ).trim() );
    }


    protected Document runScript(String fileName) throws Exception {
        InputStream in = new FileInputStream(fileName);
        XMLParser parser = new XMLParser();
        Script script = parser.parse(in);
        script = script.compile();
        JellyContext context = parser.getContext();

        SAXContentHandler contentHandler = new SAXContentHandler();
        XMLOutput output = new XMLOutput( contentHandler );

        contentHandler.startDocument();
        script.run(context, output);
        contentHandler.endDocument();

        return contentHandler.getDocument();
    }
}
