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

import java.net.URL;

import junit.framework.TestSuite;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An abstract base class for creating a TestSuite via a Jelly script.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.4 $
 */
public abstract class JellyTestSuite {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(JellyTestSuite.class);


    /**
     * Helper method to create a test suite from a file name on the class path
     * in the package of the given class.
     * For example a test could call
     * <code>
     * createTestSuite( Foo.class, "suite.jelly" );
     * </code>
     * which would loaad the 'suite.jelly script from the same package as the Foo
     * class on the classpath.
     *
     * @param testClass is the test class used to load the script via the classpath
     * @param script is the name of the script, which is typically just a name, no directory.
     * @return a newly created TestSuite
     */
    public static TestSuite createTestSuite(Class testClass, String script) throws Exception {
        URL url = testClass.getResource(script);
        if ( url == null ) {
            throw new Exception(
                "Could not find Jelly script: " + script
                + " in package of class: " + testClass.getName()
            );
        }
        return createTestSuite( url );
    }

    /**
     * Helper method to create a test suite from the given Jelly script
     *
     * @param script is the URL to the script which should create a TestSuite
     * @return a newly created TestSuite
     */
    public static TestSuite createTestSuite(URL script) throws Exception {
        JellyContext context = new JellyContext(script);
        XMLOutput output = XMLOutput.createXMLOutput(System.out);
        context = context.runScript(script, output);
        TestSuite answer = (TestSuite) context.getVariable("org.apache.commons.jelly.junit.suite");
        if ( answer == null ) {
            log.warn( "Could not find a TestSuite created by Jelly for the script:" + script );
            // return an empty test suite
            return new TestSuite();
        }
        return answer;
    }
}
