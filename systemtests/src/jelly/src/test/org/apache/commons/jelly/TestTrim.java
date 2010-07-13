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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.jelly.impl.TextScript;

/**
 * Tests the whitespace triming of scripts.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.9 $
 */
public class TestTrim extends TestCase {

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TestTrim.class);
    }

    public TestTrim(String testName) {
        super(testName);
    }

    public void testTrim() throws Exception {
        TextScript script = new TextScript( "   foo    " );
        script.trimWhitespace();

        assertEquals( "foo", script.getText() );

        script = new TextScript( " foo " );
        script.trimWhitespace();

        assertEquals( "foo", script.getText() );

        script = new TextScript( "foo" );
        script.trimWhitespace();

        assertEquals( "foo", script.getText() );
    }

    public void testTrimStart() throws Exception {
        TextScript script = new TextScript( "   foo    " );
        script.trimStartWhitespace();

        assertEquals( "foo    ", script.getText() );

        script = new TextScript( " foo " );
        script.trimStartWhitespace();

        assertEquals( "foo ", script.getText() );

        script = new TextScript( "foo" );
        script.trimStartWhitespace();

        assertEquals( "foo", script.getText() );
    }

    public void testTrimEnd() throws Exception {
        TextScript script = new TextScript( "   foo    " );
        script.trimEndWhitespace();

        assertEquals( "   foo", script.getText() );

        script = new TextScript( " foo " );
        script.trimEndWhitespace();

        assertEquals( " foo", script.getText() );

        script = new TextScript( "foo" );
        script.trimEndWhitespace();

        assertEquals( "foo", script.getText() );
    }
}
