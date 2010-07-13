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

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This tag will run the given Test which could be an individual TestCase or a TestSuite.
 * The TestResult can be specified to capture the output, otherwise the results are output
 * as XML so that they can be formatted in some custom manner.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class RunTag extends TagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(RunTag.class);

    private Test test;
    private TestResult result;
    private TestListener listener;

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        Test test = getTest();
        if ( test == null ) {
            test = (Test) context.getVariable("org.apache.commons.jelly.junit.suite");
        }
        if ( test == null ) {
            throw new MissingAttributeException( "test" );
        }
        TestResult result = getResult();
        if ( result == null ) {
            result = createResult(output);
        }
        TestListener listener = getListener();
        if ( listener == null ) {
            listener = createTestListener(output);
        }
        result.addListener(listener);
        test.run(result);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Returns the TestResult used to capture the output of the test.
     * @return TestResult
     */
    public TestResult getResult() {
        return result;
    }

    /**
     * Returns the Test to be ran.
     * @return Test
     */
    public Test getTest() {
        return test;
    }

    /**
     * Sets the JUnit TestResult used to capture the results of the tst
     * @param result The TestResult to use
     */
    public void setResult(TestResult result) {
        this.result = result;
    }

    /**
     * Sets the JUnit Test to run which could be an individual test or a TestSuite
     * @param test The test to run
     */
    public void setTest(Test test) {
        this.test = test;
    }

    /**
     * Returns the listener.
     * @return TestListener
     */
    public TestListener getListener() {
        return listener;
    }

    /**
     * Sets the TestListener.to be used to format the output of running the unit test cases
     * @param listener The listener to set
     */
    public void setListener(TestListener listener) {
        this.listener = listener;
    }



    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new TestResult to capture the output of
     * the test cases
     */
    protected TestResult createResult(XMLOutput output) {
        return new TestResult();
    }

    /**
     * Factory method to create a new TestListener to capture the output of
     * the test cases
     */
    protected TestListener createTestListener(final XMLOutput output) {
        return new TestListener() {
            public void addError(Test test, Throwable t) {
                try {
                    output.startElement("error");

                    output.startElement("message");
                    output.write(t.getMessage());
                    output.endElement("message");

                    output.startElement("stack");
                    output.write( stackTraceToString(t) );
                    output.endElement("stack");

                    output.endElement("error");
                }
                catch (SAXException e) {
                    handleSAXException(e);
                }
            }

            public void addFailure(Test test, AssertionFailedError t) {
                try {
                    output.startElement("failure");

                    output.startElement("message");
                    output.write(t.getMessage());
                    output.endElement("message");

                    output.startElement("stack");
                    output.write( stackTraceToString(t) );
                    output.endElement("stack");

                    output.endElement("failure");
                }
                catch (SAXException e) {
                    handleSAXException(e);
                }
            }

            public void endTest(Test test) {
                try {
                    output.endElement("test");
                }
                catch (SAXException e) {
                    handleSAXException(e);
                }
            }

            public void startTest(Test test) {
                try {
                    String name = test.toString();
                    AttributesImpl attributes = new AttributesImpl();
                    attributes.addAttribute("", "name", "name", "CDATA", name);

                    output.startElement("test", attributes);
                }
                catch (SAXException e) {
                    handleSAXException(e);
                }
            }
        };
    }

    /**
     * @return the stack trace as a String
     */
    protected String stackTraceToString(Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * Handles SAX Exceptions
     */
    protected void handleSAXException(SAXException e) {
        log.error( "Caught: " + e, e );
    }
}
