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

import junit.framework.TestSuite;

import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.test.BaseJellyTest;

public class TestBreakTag extends BaseJellyTest
{

    public TestBreakTag(String name)
    {
        super(name);
    }

    public static TestSuite suite() throws Exception
    {
        return new TestSuite(TestBreakTag.class);
    }

    public void testSimpleBreakTag() throws Exception
    {
        setUpScript("testBreakTag.jelly");
        Script script = getJelly().compileScript();

        script.run(getJellyContext(), getXMLOutput());

        String simpleResult = (String) getJellyContext().getVariable("simpleResult");

        assertEquals("simpleResult", "12345", simpleResult);
    }

    public void testConditionalBreakTag() throws Exception
    {
        setUpScript("testBreakTag.jelly");
        Script script = getJelly().compileScript();

        script.run(getJellyContext(), getXMLOutput());

        String simpleResult = (String) getJellyContext().getVariable("conditionalResult");

        assertEquals("conditionalResult", "12345", simpleResult);
    }

    public void testVarBreakTag() throws Exception
    {
        setUpScript("testBreakTag.jelly");
        Script script = getJelly().compileScript();

        script.run(getJellyContext(), getXMLOutput());

        String varBroken = (String) getJellyContext().getVariable("varBroken");

        assertEquals("varBroken", "true", varBroken);
    }

    public void testVarNoBreakTag() throws Exception
    {
        setUpScript("testBreakTag.jelly");
        Script script = getJelly().compileScript();

        script.run(getJellyContext(), getXMLOutput());

        String varNotBroken = (String) getJellyContext().getVariable("varNotBroken");

        assertEquals("varNotBroken", "false", varNotBroken);
    }


}
