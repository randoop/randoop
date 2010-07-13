/*
 * Copyright 1999-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.chain.web.servlet;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.chain.Context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;


// Test case for org.apache.commons.chain.web.servlet.ServletGetLocaleCommand

public class ServletGetLocaleCommandTestCase extends TestCase {


    // ---------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ServletGetLocaleCommandTestCase(String name) {
        super(name);
    }


    // ----------------------------------------------------- Instance Variables


    protected Locale locale = null;

    // Servlet API Objects
    protected ServletContext scontext = null;
    protected HttpServletRequest request = null;
    protected HttpServletResponse response = null;
    protected HttpSession session = null;

    // Chain API Objects
    protected Context context = null;
    protected ServletGetLocaleCommand command = null;


    // -------------------------------------------------- Overall Test Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {

    locale = new Locale("en", "US");

    // Set up Servlet API Objects
        scontext = new MockServletContext();
        session = new MockHttpSession(scontext);
        request = new MockHttpServletRequest("/context", "/servlet",
                                             "/path/info", "a=b&c=d",
                                             session);
    ((MockHttpServletRequest) request).setLocale(locale);
        response = new MockHttpServletResponse();

    // Set up Chain API Objects
        context = new ServletWebContext(scontext, request, response);
    command = new ServletGetLocaleCommand();

    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {

        return (new TestSuite(ServletGetLocaleCommandTestCase.class));

    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {

        scontext = null;
        session = null;
        request = null;
        response = null;

        context = null;
    command = null;

    }


    // ------------------------------------------------- Individual Test Methods


    // Test configured behavior
    public void testConfigured() throws Exception {

    command.setLocaleKey("special");
    assertEquals("special", command.getLocaleKey());
    check(context, command);

    }


    // Test default behavior
    public void testDefaut() throws Exception {

    assertEquals("locale", command.getLocaleKey());
    check(context, command);

    }


    // --------------------------------------------------------- Support Methods


    protected void check(Context context, ServletGetLocaleCommand command)
    throws Exception {

    String localeKey = command.getLocaleKey();
    assertNotNull(localeKey);
    Object value = context.get(localeKey);
    assertNull(value);
    boolean result = command.execute(context);
    assertFalse(result);
    value = context.get(localeKey);
    assertNotNull(value);
    assertTrue(value instanceof Locale);
    assertEquals(locale, (Locale) value);

    }


}
