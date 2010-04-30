/**
* Copyright 2004 The Apache Software Foundation.
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
package org.apache.commons.jelly.tags.jaxme;

import org.apache.commons.jelly.tags.junit.JellyTestSuite;
import junit.framework.TestSuite;

/**
 *
 *
 * @author <a href="mailto:commons-dev at jakarta.apache.org">Jakarta Commons Development Team</a>
 * @version $Revision: 1.2 $
 */
public class ExecutionTestCase extends JellyTestSuite {


    public static TestSuite suite() throws Exception {
        return createTestSuite(ExecutionTestCase.class, "suite.jelly");
    }
}

