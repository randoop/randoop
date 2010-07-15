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
package org.apache.commons.jelly.tags.junit;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Represents a single test case in a test suite; this tag is analagous to
 * JUnit's TestCase class.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.6 $
 */
public class CaseTag extends TagSupport {

    private String name;


    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws JellyTagException {
        String name = getName();
        if ( name == null ) {
            name = toString();
        }

        // #### we need to redirect the output to a TestListener
        // or something?
        TestCase testCase = new TestCase(name) {
            protected void runTest() throws Throwable {
                // create a new child context so that each test case
                // will have its own variable scopes
                JellyContext newContext = new JellyContext( context );

                // disable inheritence of variables and tag libraries
                newContext.setExportLibraries(false);
                newContext.setExport(false);

                // invoke the test case
                getBody().run(newContext, output);
            }
        };

        // lets find the test suite
        TestSuite suite = getSuite();
        if ( suite == null ) {
            throw new JellyTagException( "Could not find a TestSuite to add this test to. This tag should be inside a <test:suite> tag" );
        }
        suite.addTest(testCase);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the name of this test case
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this test case
     */
    public void setName(String name) {
        this.name = name;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Strategy method to find the corrent TestSuite to add a new Test case to
     */
    protected TestSuite getSuite() {
        SuiteTag tag = (SuiteTag) findAncestorWithClass( SuiteTag.class );
        if ( tag != null ) {
            return tag.getSuite();
        }
        return (TestSuite) context.getVariable( "org.apache.commons.jelly.junit.suite" );
    }

}
