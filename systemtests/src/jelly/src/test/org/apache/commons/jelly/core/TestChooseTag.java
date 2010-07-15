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

/**
 * @author <a href="mailto:bwalding@apache.org">Ben Walding</a>
 * @version $Revision: 1.5 $
 */
public class TestChooseTag extends BaseJellyTest
{

    public TestChooseTag(String name)
    {
        super(name);
    }

    public static TestSuite suite() throws Exception
    {
        return new TestSuite(TestChooseTag.class);
    }

    public void testSimpleFileTag() throws Exception
    {
        setUpScript("testChooseTag.jelly");
        Script script = getJelly().compileScript();

        script.run(getJellyContext(), getXMLOutput());

        String resultTrue = (String) getJellyContext().getVariable("result.true");
        String resultFalse = (String) getJellyContext().getVariable("result.false");

        assertEquals("result.true", "AC", resultTrue);
        assertEquals("result.false", "BC", resultFalse);
    }



}
