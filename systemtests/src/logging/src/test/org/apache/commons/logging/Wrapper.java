/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.commons.logging;


import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Wrapper around test cases that need to have a custom class loader
 * hierarchy assembled.  The wrapper is configured by the following
 * system properties:</p>
 * <ul>
 * <li><strong>wrapper.hierarchy</strong> - Descriptive code describing how
 *     the class loader hierarchy should be assembled:
 *     <ul>
 *     <li><strong>API</strong> - Parent class loader contains
 *         <code>commons-logging-api.jar</code> and child class loader
 *         contains <code>commons-logging.jar</code>.  This is like the
 *         default configuration for Tomcat 4.1.</li>
 *     <li><strong>FULL</strong> - Parent class loader contains
 *         <code>commons-logging.jar</code>.  This is what would happen
 *         if you replaced <code>commons-logging-api.jar</code> with
 *         <code>commons-logging.jar</code> so that you did not need to
 *         include the latter with your application.</li>
 *     </ul>
 *     The child class loader also unconditionally includes
 *     <code>commons-logging-tests.jar</code>.</li>
 * <li><strong>wrapper.junit</strong> - Fully qualified pathname of the
 *     JUnit JAR file.</li>
 * <li><strong>wrapper.log4j</strong> - Fully qualified pathname of the
 *     Log4J JAR file, which will be placed in whichever class loader
 *     <code>commons-logging.jar</code> is placed in, if specified.</li>
 * <li><strong>wrapper.target</strong> - Fully qualified pathname of the
 *     "target" directory created by the build process.  This directory
 *     must contain the <code>commons-logging.jar</code>,
 *     <code>commons-logging-api.jar</code>, and
 *     <code>commons-logging-tests.jar</code> files resulting from the
 *     execution of the <code>compile.tests</code> target.</li>
 * <li><strong>wrapper.testcase</strong> - Fully qualified Java class name
 *     of a TestCase that will ultimately be executed.  This class must
 *     exist in the <code>commons-logging-tests.jar</code> file.</li>
 * </ul>
 *
 * <p>When executed, the system classpath for the wrapper should include
 * only the wrapper class itself.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2004/02/28 21:46:45 $
 */

public class Wrapper {


    public static void main(String args[]) {

        try {

            // Create variables we will need
            List parentList = new ArrayList();
            List childList = new ArrayList();
            URL urls[] = null;

            // Construct URLs for the various JAR files
            File target = new File(System.getProperty("wrapper.target"));
            URL commonsLogging =
                (new File(target, "commons-logging.jar")).toURL();
            URL commonsLoggingApi =
                (new File(target, "commons-logging-api.jar")).toURL();
            URL commonsLoggingTests =
                (new File(target, "commons-logging-tests.jar")).toURL();
            URL junit =
                (new File(System.getProperty("wrapper.junit"))).toURL();
            URL appender = null;
            URL log4j = null;
            if (System.getProperty("wrapper.log4j") != null) {
                log4j =
                    (new File(System.getProperty("wrapper.log4j"))).toURL();
                appender =
                    (new File(target, "commons-logging-appender.jar")).toURL();
            }

            // Construct class loader repository lists for supported scenarios
            if ("API".equals(System.getProperty("wrapper.hierarchy"))) {
                parentList.add(commonsLoggingApi);
                childList.add(commonsLogging);
                if (log4j != null) {
                    childList.add(log4j);
                    childList.add(appender);
                }
            } else { // Assumes "FULL"
                parentList.add(commonsLogging);
                if (log4j != null) {
                    parentList.add(log4j);
                    childList.add(appender);
                }
            }
            childList.add(commonsLoggingTests);
            childList.add(junit);

            // Construt the parent and child class loaders
            urls = (URL[]) parentList.toArray(new URL[parentList.size()]);
            ClassLoader parent =
                new URLClassLoader(urls,
                                   ClassLoader.getSystemClassLoader());
            urls = (URL[]) childList.toArray(new URL[childList.size()]);
            ClassLoader child = new URLClassLoader(urls, parent);

            // Execute the test runner for this TestCase
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(child);
            Class clazz = child.loadClass("junit.textui.TestRunner");
            String params[] = new String[1];
            params[0] = System.getProperty("wrapper.testcase");
            Method method = clazz.getMethod("main",
                                            new Class[] { params.getClass() });
            method.invoke(null, new Object[] { params });
            Thread.currentThread().setContextClassLoader(old);

        } catch (Exception e) {

            System.out.println("Wrapper Exception Occurred:  " + e);
            e.printStackTrace(System.out);
            System.exit(1);

        }

    }



}
