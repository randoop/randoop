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
package org.apache.commons.jelly.tags.regexp;

import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.JellyContext;
import junit.framework.TestCase;
import org.apache.commons.jelly.tags.regexp.ContainsTag;

/*** <p><code>ContainsTagTest</code> a class that is useful to perform regexp matches
* in strings.</p>
*
* @author <a href="mailto:christian@inx-soft.com">Christian Amor Kvalheim</a>
* @version $Revision: 1.1 $
*/
public class ContainsTagTest extends TestCase {

    public ContainsTagTest(String name)
    {
      super(name);
    }

    public void setUp() throws Exception
    {
    }

    public void testDoTag() throws Exception
    {
      ContainsTag containsExpTag = new ContainsTag();
      XMLOutput xmlOutput = new XMLOutput();

      containsExpTag.setText("Hello World");
      containsExpTag.setExpr("World");
      containsExpTag.setVar("testvar");
      containsExpTag.setContext(new JellyContext());
      containsExpTag.doTag(xmlOutput);

      assertEquals("TRUE", containsExpTag.getContext().getVariable("testvar").toString().toUpperCase());
    }

    public void tearDown()
    {
    }

}
