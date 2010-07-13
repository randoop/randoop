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
package org.apache.commons.jelly.tags.define;

import java.io.File;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Tests dynamic tags
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.4 $
  */
public class TestDynamicTags extends TestCase {

    JellyContext context = new JellyContext();
    XMLOutput output;

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(TestDynamicTags.class);

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TestDynamicTags.class);
    }

    public TestDynamicTags(String testName) {
        super(testName);
    }

    public void testParse() throws Exception {
        StringWriter buffer = new StringWriter();
        output = XMLOutput.createXMLOutput(buffer);

        //runScript("src/test/org/apache/commons/jelly/define/babelfishTaglib.jelly");
        runScript("src/test/org/apache/commons/jelly/tags/define/example.jelly");

        log.info("The output was as follows");
        log.info(buffer.toString());
    }

    public void testJellyBean() throws Exception {
        StringWriter buffer = new StringWriter();
        output = XMLOutput.createXMLOutput(buffer);

    log.warn("commented out test, need to rewrite without ant");
        //runScript("src/test/org/apache/commons/jelly/define/jellyBeanSample.jelly");

        log.info("The output was as follows");
        log.info(buffer.toString());
    }

    protected void runScript(String name) throws Exception {
        context.runScript(new File(name), output);
    }
}
