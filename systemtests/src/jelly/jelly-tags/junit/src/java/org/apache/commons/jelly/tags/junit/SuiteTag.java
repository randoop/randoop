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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Represents a collection of TestCases.. This tag is analagous to
 * JUnit's TestSuite class.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class SuiteTag extends TagSupport {

    /** the test suite this tag created */
    private TestSuite suite;

    /** the name of the variable of the test suite */
    private String var;

    /** the name of the test suite to create */
    private String name;

    public SuiteTag() {
    }

    /**
     * Adds a new Test to this suite
     */
    public void addTest(Test test) {
        getSuite().addTest(test);
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        suite = createSuite();

        TestSuite parent = (TestSuite) context.getVariable("org.apache.commons.jelly.junit.suite");
        if ( parent == null ) {
            context.setVariable("org.apache.commons.jelly.junit.suite", suite );
        }
        else {
            parent.addTest( suite );
        }

        invokeBody(output);

        if ( var != null ) {
            context.setVariable(var, suite);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public TestSuite getSuite() {
        return suite;
    }

    /**
     * Sets the name of the test suite whichi is exported
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * @return the name of this test suite
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this test suite
     */
    public void setName(String name) {
        this.name = name;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new TestSuite
     */
    protected TestSuite createSuite() {
        if ( name == null ) {
            return new TestSuite();
        }
        else {
            return new TestSuite(name);
        }
    }
}
