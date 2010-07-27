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
package org.apache.commons.jelly.impl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/** Tests the core tags
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.9 $
  */
public class TestTagLibraryResolver extends TestCase {

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TestTagLibraryResolver.class);
    }

    public TestTagLibraryResolver(String testName) {
        super(testName);
    }

    public void testResolver() throws Exception {
        /**
         * @todo temporary disbled test case until I can figure out how to get
         * it to work with commons-discovery
         */
/*
        TagLibrary library = resolver.resolveTagLibrary("jelly:test-library" );

        assertTrue( "Found a tag library", library != null );
        assertEquals( "Tag library is of the correct type", "org.apache.commons.jelly.test.impl.DummyTagLibrary", library.getClass().getName() );
*/
    }
}
