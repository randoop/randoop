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

import junit.framework.TestCase;

import org.apache.commons.jelly.Jelly;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

/**
 * @author mdelagrange
 *
 */
public class TestCData extends TestCase {

    public TestCData(String arg) {
        super(arg);
    }

    /**
     * CDATA sections should be retained in the output.
     *
     * @throws Exception
     */
    public void testCData() throws Exception {
        Jelly jelly = new Jelly();
        jelly.setScript("file:src/test/org/apache/commons/jelly/test/xml/testCData.jelly");
        Script script = jelly.compileScript();
        JellyContext context = new JellyContext();
        script.run(context, XMLOutput.createDummyXMLOutput());

        String output = (String) context.getVariable("foo");
        assertTrue("'foo' is not null", output != null);

        String golden = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        golden += "<!DOCTYPE foo [\n";
        golden += "  <!ELEMENT foo (#PCDATA)>\n";
        golden += "]><foo></foo>";

        assertEquals("output should contain the CDATA section", golden, output);
    }

    public void testDom4JCData() throws SAXException {
        StringWriter writer = new StringWriter();
        OutputFormat format = new OutputFormat();
        final XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.setEscapeText(false);

        XMLOutput output = new XMLOutput(xmlWriter, xmlWriter);

        String decl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        String golden = "<!DOCTYPE foo [\n";
        golden += "  <!ELEMENT foo (#PCDATA)>\n";
        golden += "]><foo></foo>";

        output.startDocument();
        output.write(golden);
        output.endDocument();
        System.err.println("output was: '" + writer.toString() +"'");
        System.err.println("golden is : '" + golden +"'");
        assertEquals("output should contain the CDATA section",
                decl + golden, writer.toString());
    }

}
