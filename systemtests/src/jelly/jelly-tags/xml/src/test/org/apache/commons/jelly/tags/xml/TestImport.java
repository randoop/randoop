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

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.parser.XMLParser;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * Test that compiled scripts can access resources
 */
public class TestImport extends TestCase {
    
    private String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<html xmlns=\"http://www.w3.org/TR/xhtml1/strict\" "
        + "xmlns=\"http://www.w3.org/TR/xhtml1/strict\">"
        + "<head><title>Expense Report Summary</title></head>"
        + "<body><p>Total Amount: 12</p></body></html>";
    public TestImport(String name) {
        super(name);
    }
    
    public void testImportResources() throws JellyException, UnsupportedEncodingException, IOException {
        JellyContext context = new JellyContext();
        URL url = TestImport.class.getResource("/resources/import.jelly");
        StringWriter writer = new StringWriter();
        XMLOutput out = XMLOutput.createXMLOutput(writer);
//         this works because of the created child context that has knowledge
//         of the URL
        context.runScript(url, out);
        out.close();
        assertEquals(expected, writer.toString());
    }

    public void testImportResourcesCompiled() throws JellyException, UnsupportedEncodingException, IOException {
        JellyContext context = new JellyContext();
        URL url = TestImport.class.getResource("/resources/import.jelly");
        StringWriter writer = new StringWriter();
        XMLOutput out = XMLOutput.createXMLOutput(writer);
        Script script = context.compileScript(url);
        script.run(context, out);
        out.close();
        assertEquals(expected, writer.toString());
    }

    public void testImportResourcesFromUncompiledScript() throws JellyException, UnsupportedEncodingException, IOException, SAXException {
        JellyContext context = new JellyContext();
        URL url = TestImport.class.getResource("/resources/import.jelly");
        StringWriter writer = new StringWriter();
        XMLOutput out = XMLOutput.createXMLOutput(writer);
        Script script = new XMLParser().parse(url);
        script.run(context, out);
        out.close();
        assertEquals(expected, writer.toString());
    }

}