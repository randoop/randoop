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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.impl.ScriptBlock;
import org.apache.commons.jelly.impl.TagScript;
import org.apache.commons.jelly.parser.XMLParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Tests the core tags
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.6 $
  */
public class TestParser extends TestCase {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(TestParser.class);

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TestParser.class);
    }

    public TestParser(String testName) {
        super(testName);
    }

    /**
     * Tests that parsing an example script correctly creates the parent
     * relationships
     */
    public void testParser() throws Exception {
        InputStream in = new FileInputStream("src/test/org/apache/commons/jelly/tags/xml/example2.jelly");
        XMLParser parser = new XMLParser();
        Script script = parser.parse(in);
        script = script.compile();

        log.debug("Found: " + script);

        assertTagsHaveParent( script, null, null );
    }

    /**
     * Tests that the Tag in the TagScript has the given parent and then
     * recurse to check its children has the correct parent and so forth.
     */
    protected void assertTagsHaveParent(Script script, Tag parent, JellyContext context) throws Exception {
        if ( context == null )
            context = new JellyContext();
        if ( script instanceof TagScript ) {
            TagScript tagScript = (TagScript) script;
            Tag tag = tagScript.getTag(context);

            assertEquals( "Tag: " + tag + " has the incorrect parent", parent, tag.getParent() );

            assertTagsHaveParent( tag.getBody(), tag, context );
        }
        else if ( script instanceof ScriptBlock ) {
            ScriptBlock block = (ScriptBlock) script;
            for ( Iterator iter = block.getScriptList().iterator(); iter.hasNext(); ) {
                assertTagsHaveParent( (Script) iter.next(), parent, context );
            }
        }
    }
}