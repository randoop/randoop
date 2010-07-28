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

import java.util.HashMap;

import junit.framework.TestCase;

/**
 *
 * @author <a href="proyal@apache.org">peter royal</a>
 */
public class TestJellyContext extends TestCase
{
    public TestJellyContext( String s )
    {
        super( s );
    }

    public void testSetVariablesAndRetainContextEntry()
    {
        final JellyContext jc = new JellyContext();

        assertNotNull( "Initial variable of context", jc.getVariable( "context" ) );

        jc.setVariables( new HashMap() );

        assertNotNull( "Value after setVariables()", jc.getVariable( "context" ) );
    }
}
