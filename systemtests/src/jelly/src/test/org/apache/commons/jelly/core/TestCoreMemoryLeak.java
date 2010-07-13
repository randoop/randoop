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

/** Tests for basic memory leaking in core tags. Runs a few test scripts many times.
 * @author Hans Gilde
 *
 */
public class TestCoreMemoryLeak extends BaseMemoryLeakTest {

    /** The JUnit constructor.
     * @param name
     */
    public TestCoreMemoryLeak(String name) {
        super(name);
    }
    
    public void testBasicScriptForLeak() throws Exception {
        assertTrue("Leak in core library", runScriptManyTimes("c.jelly", 10000) < 200000);
    }
    
    public void testIncludeTagForLeak() throws Exception {
        assertTrue("Leak in include tag", runScriptManyTimes("a.jelly", 10000) < 200000);
    }

}
