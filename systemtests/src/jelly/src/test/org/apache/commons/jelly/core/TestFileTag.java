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

import java.io.StringWriter;

import junit.framework.TestSuite;

import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.test.BaseJellyTest;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:robert@bull-enterprises.com">Robert McIntosh</a>
 * @version $Revision: 1.7 $
 */
public class TestFileTag extends BaseJellyTest
{

    public TestFileTag(String name)
    {
        super(name);
    }

    public static TestSuite suite() throws Exception
    {
        return new TestSuite(TestFileTag.class);
    }

    public void testSimpleFileTag() throws Exception
    {
        setUpScript("testFileTag.jelly");
        Script script = getJelly().compileScript();

        script.run(getJellyContext(), getXMLOutput());

        String data = (String)getJellyContext().getVariable("testFileTag");

        //FIXME This doesn't take into account attribute ordering
        assertEquals("fully qualified attributes not passed",
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\"></html>",
                data);
    }

    public void testDom4Xmlns() throws SAXException {
        StringWriter writer = new StringWriter();
        OutputFormat format = new OutputFormat();
        final XMLWriter xmlWriter = new HTMLWriter(writer, format);
        xmlWriter.setEscapeText(false);

        XMLOutput output = new XMLOutput(xmlWriter, xmlWriter);

        String golden = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n";
        golden += "<html>";

        output.startDocument();
        output.write(golden);
        output.endDocument();
        assertEquals("output should contain the namespaces", golden, writer.toString());
    }

}
