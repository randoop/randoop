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
import junit.framework.TestSuite;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBaseTestCase;
import org.apache.commons.chain.web.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Extension of <code>ContextBaseTestCase</code> to validate the
 * extra functionality of this implementation.
 */

public class ServletWebContextTestCase extends ContextBaseTestCase {


    // ---------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ServletWebContextTestCase(String name) {
        super(name);
    }


    // ----------------------------------------------------- Instance Variables


    // Servlet API Objects
    protected ServletContext scontext = null;
    protected HttpServletRequest request = null;
    protected HttpServletResponse response = null;
    protected HttpSession session = null;


    // -------------------------------------------------- Overall Test Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        scontext = new MockServletContext();
        scontext.setAttribute("akey1", "avalue1");
        scontext.setAttribute("akey2", "avalue2");
        scontext.setAttribute("akey3", "avalue3");
        scontext.setAttribute("akey4", "avalue4");
        ((MockServletContext) scontext).addInitParameter("ikey1", "ivalue1");
        ((MockServletContext) scontext).addInitParameter("ikey2", "ivalue2");
        ((MockServletContext) scontext).addInitParameter("ikey3", "ivalue3");
        session = new MockHttpSession(scontext);
        session.setAttribute("skey1", "svalue1");
        session.setAttribute("skey2", "svalue2");
        session.setAttribute("skey3", "svalue3");
        request = new MockHttpServletRequest("/context", "/servlet",
                                             "/path/info", "a=b&c=d",
                                             session);
        request.setAttribute("rkey1", "rvalue1");
        request.setAttribute("rkey2", "rvalue2");
        ((MockHttpServletRequest) request).addHeader("hkey1", "hvalue1");
        ((MockHttpServletRequest) request).addHeader("hkey2", "hvalue2a");
        ((MockHttpServletRequest) request).addHeader("hkey2", "hvalue2b");
        ((MockHttpServletRequest) request).addParameter("pkey1", "pvalue1");
        ((MockHttpServletRequest) request).addParameter("pkey2", "pvalue2a");
        ((MockHttpServletRequest) request).addParameter("pkey2", "pvalue2b");
        response = new MockHttpServletResponse();
        context = createContext();
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ServletWebContextTestCase.class));
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
    }


    // ------------------------------------------------ Individual Test Methods


    // Test getApplicationScope()
    public void testApplicationScope() {

        Map map = ((WebContext) context).getApplicationScope();
        assertNotNull(map);

        // Initial contents
        checkMapSize(map, 4);
        assertEquals("avalue1", (String) map.get("akey1"));
        assertEquals("avalue2", (String) map.get("akey2"));
        assertEquals("avalue3", (String) map.get("akey3"));
        assertEquals("avalue4", (String) map.get("akey4"));

        // Transparency - entrySet()
        checkEntrySet(map, true);
 
        // Transparency - removal via web object
        scontext.removeAttribute("akey1");
        checkMapSize(map, 3);
        assertNull(map.get("akey1"));

        // Transparency - removal via map
        map.remove("akey2");
        checkMapSize(map, 2);
        assertNull(scontext.getAttribute("akey2"));

        // Transparency - addition via web object
        scontext.setAttribute("akeyA", "avalueA");
        checkMapSize(map, 3);
        assertEquals("avalueA", (String) map.get("akeyA"));

        // Transparency - addition via map
        map.put("akeyB", "avalueB");
        checkMapSize(map, 4);
        assertEquals("avalueB", (String) scontext.getAttribute("akeyB"));

        // Transparency - replacement via web object
        scontext.setAttribute("akeyA", "newvalueA");
        checkMapSize(map, 4);
        assertEquals("newvalueA", (String) map.get("akeyA"));

        // Transparency - replacement via map
        map.put("akeyB", "newvalueB");
        assertEquals(4, map.size());
        assertEquals("newvalueB", (String) scontext.getAttribute("akeyB"));

        // Clearing the map
        map.clear();
        checkMapSize(map, 0);

    }


    // Test equals() and hashCode()
    // Copied from ContextBaseTestCase with customized creation of "other"
    public void testEquals() {

        // FIXME - ServletWebContext needs a better equals()

        // Compare to self
        assertTrue(context.equals(context));
        assertTrue(context.hashCode() == context.hashCode());

        // Compare to equivalent instance
        Context other = new ServletWebContext(scontext, request, response);
        // assertTrue(context.equals(other));
        assertTrue(context.hashCode() == other.hashCode());

        // Compare to non-equivalent instance - other modified
        other.put("bop", "bop value");
        // assertTrue(!context.equals(other));
        assertTrue(context.hashCode() != other.hashCode());

        // Compare to non-equivalent instance - self modified
        other = new ServletWebContext(scontext, request, response);
        context.put("bop", "bop value");
        // assertTrue(!context.equals(other));
        assertTrue(context.hashCode() != other.hashCode());

    }        


    // Test getHeader()
    public void testHeader() {

        Map map = ((WebContext) context).getHeader();
        assertNotNull(map);

        // Initial contents
        checkMapSize(map, 2);
        assertEquals("hvalue1", (String) map.get("hkey1"));
        assertEquals("hvalue2a", (String) map.get("hkey2"));
        assertTrue(map.containsKey("hkey1"));
        assertTrue(map.containsKey("hkey2"));
        assertTrue(map.containsValue("hvalue1"));
        assertTrue(map.containsValue("hvalue2a"));

        // Transparency - entrySet()
        checkEntrySet(map, false);
 
        // Unsupported operations on read-only map
        try {
            map.clear();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.put("hkey3", "hvalue3");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.putAll(new HashMap());
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.remove("hkey1");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }

    }


    // Test getHeaderValues()
    public void testHeaderValues() {

        Map map = ((WebContext) context).getHeaderValues();
        assertNotNull(map);

        // Initial contents
        checkMapSize(map, 2);
        Object value1 = map.get("hkey1");
        assertNotNull(value1);
        assertTrue(value1 instanceof String[]);
        String values1[] = (String[]) value1;
        assertEquals(1, values1.length);
        assertEquals("hvalue1", values1[0]);
        Object value2 = map.get("hkey2");
        assertNotNull(value2);
        assertTrue(value2 instanceof String[]);
        String values2[] = (String[]) value2;
        assertEquals(2, values2.length);
        assertEquals("hvalue2a", values2[0]);
        assertEquals("hvalue2b", values2[1]);
        assertTrue(map.containsKey("hkey1"));
        assertTrue(map.containsKey("hkey2"));
        assertTrue(map.containsValue(values1));
        assertTrue(map.containsValue(values2));

        // Transparency - entrySet()
        checkEntrySet(map, false);
 
        // Unsupported operations on read-only map
        try {
            map.clear();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.put("hkey3", values2);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.putAll(new HashMap());
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.remove("hkey1");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }

    }


    // Test getInitParam()
    public void testInitParam() {

        Map map = ((WebContext) context).getInitParam();
        assertNotNull(map);

        // Initial contents
        checkMapSize(map, 3);
        assertEquals("ivalue1", (String) map.get("ikey1"));
        assertEquals("ivalue2", (String) map.get("ikey2"));
        assertEquals("ivalue3", (String) map.get("ikey3"));
        assertTrue(map.containsKey("ikey1"));
        assertTrue(map.containsKey("ikey2"));
        assertTrue(map.containsKey("ikey3"));
        assertTrue(map.containsValue("ivalue1"));
        assertTrue(map.containsValue("ivalue2"));
        assertTrue(map.containsValue("ivalue3"));

        // Transparency - entrySet()
        checkEntrySet(map, false);
 
        // Unsupported operations on read-only map
        try {
            map.clear();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.put("ikey4", "ivalue4");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.putAll(new HashMap());
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.remove("ikey1");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }

    }


    // Test getParam()
    public void testParam() {

        Map map = ((WebContext) context).getParam();
        assertNotNull(map);

        // Initial contents
        checkMapSize(map, 2);
        assertEquals("pvalue1", (String) map.get("pkey1"));
        assertEquals("pvalue2a", (String) map.get("pkey2"));
        assertTrue(map.containsKey("pkey1"));
        assertTrue(map.containsKey("pkey2"));
        assertTrue(map.containsValue("pvalue1"));
        assertTrue(map.containsValue("pvalue2a"));

        checkEntrySet(map, false);

        // Unsupported operations on read-only map
        try {
            map.clear();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.put("pkey3", "pvalue3");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.putAll(new HashMap());
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.remove("pkey1");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }

    }


    // Test getParamValues()
    public void testParamValues() {

        Map map = ((WebContext) context).getParamValues();
        assertNotNull(map);

        // Initial contents
        checkMapSize(map, 2);
        Object value1 = map.get("pkey1");
        assertNotNull(value1);
        assertTrue(value1 instanceof String[]);
        String values1[] = (String[]) value1;
        assertEquals(1, values1.length);
        assertEquals("pvalue1", values1[0]);
        Object value2 = map.get("pkey2");
        assertNotNull(value2);
        assertTrue(value2 instanceof String[]);
        String values2[] = (String[]) value2;
        assertEquals(2, values2.length);
        assertEquals("pvalue2a", values2[0]);
        assertEquals("pvalue2b", values2[1]);
        assertTrue(map.containsKey("pkey1"));
        assertTrue(map.containsKey("pkey2"));
        assertTrue(map.containsValue(values1));
        assertTrue(map.containsValue(values2));

        // Unsupported operations on read-only map
        try {
            map.clear();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.put("pkey3", values2);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.putAll(new HashMap());
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }
        try {
            map.remove("pkey1");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // expected result
        }

    }


    // Test state of newly created instance
    public void testPristine() {

        super.testPristine();
        ServletWebContext swcontext = (ServletWebContext) context;

        // Properties should all be non-null
        assertNotNull(swcontext.getApplicationScope());
        assertNotNull(swcontext.getHeader());
        assertNotNull(swcontext.getHeaderValues());
        assertNotNull(swcontext.getInitParam());
        assertNotNull(swcontext.getParam());
        assertNotNull(swcontext.getParamValues());
        assertNotNull(swcontext.getRequestScope());
        assertNotNull(swcontext.getSessionScope());

        // Attribute-property transparency
        assertTrue(swcontext.getApplicationScope() ==
                     swcontext.get("applicationScope"));
        assertTrue(swcontext.getHeader() ==
                     swcontext.get("header"));
        assertTrue(swcontext.getHeaderValues() ==
                     swcontext.get("headerValues"));
        assertTrue(swcontext.getInitParam() ==
                     swcontext.get("initParam"));
        assertTrue(swcontext.getParam() ==
                     swcontext.get("param"));
        assertTrue(swcontext.getParamValues() ==
                     swcontext.get("paramValues"));
        assertTrue(swcontext.getRequestScope() ==
                     swcontext.get("requestScope"));
        assertTrue(swcontext.getSessionScope() ==
                     swcontext.get("sessionScope"));

    }


    // Test release()
    public void testRelease() {

        ServletWebContext swcontext = (ServletWebContext) context;
        swcontext.release();

        // Properties should all be null
        assertNull(swcontext.getApplicationScope());
        assertNull(swcontext.getHeader());
        assertNull(swcontext.getHeaderValues());
        assertNull(swcontext.getInitParam());
        assertNull(swcontext.getParam());
        assertNull(swcontext.getParamValues());
        assertNull(swcontext.getRequestScope());
        assertNull(swcontext.getSessionScope());

        // Attributes should all be null
        assertNull(swcontext.get("applicationScope"));
        assertNull(swcontext.get("header"));
        assertNull(swcontext.get("headerValues"));
        assertNull(swcontext.get("initParam"));
        assertNull(swcontext.get("param"));
        assertNull(swcontext.get("paramValues"));
        assertNull(swcontext.get("requestScope"));
        assertNull(swcontext.get("sessionScope"));

    }


    // Test getRequestScope()
    public void testRequestScope() {

        Map map = ((WebContext) context).getRequestScope();
        assertNotNull(map);

        // Initial contents
        checkMapSize(map, 2);
        assertEquals("rvalue1", (String) map.get("rkey1"));
        assertEquals("rvalue2", (String) map.get("rkey2"));

        // Transparency - entrySet()
        checkEntrySet(map, true);
 
        // Transparency - removal via web object
        request.removeAttribute("rkey1");
        checkMapSize(map, 1);
        assertNull(map.get("rkey1"));

       // Transparency - removal via map
        map.remove("rkey2");
        checkMapSize(map, 0);
        assertNull(request.getAttribute("rkey2"));

        // Transparency - addition via web object
        request.setAttribute("rkeyA", "rvalueA");
        checkMapSize(map, 1);
        assertEquals("rvalueA", (String) map.get("rkeyA"));

        // Transparency - addition via map
        map.put("rkeyB", "rvalueB");
        checkMapSize(map, 2);
        assertEquals("rvalueB", (String) request.getAttribute("rkeyB"));

        // Transparency - replacement via web object
        request.setAttribute("rkeyA", "newvalueA");
        checkMapSize(map, 2);
        assertEquals("newvalueA", (String) map.get("rkeyA"));

        // Transparency - replacement via map
        map.put("rkeyB", "newvalueB");
        checkMapSize(map, 2);
        assertEquals("newvalueB", (String) request.getAttribute("rkeyB"));

        // Clearing the map
        map.clear();
        checkMapSize(map, 0);

    }


    // Test getSessionScope()
    public void testSessionScope() {

        Map map = ((WebContext) context).getSessionScope();
        assertNotNull(map);

        // Initial contents
        checkMapSize(map, 3);
        assertEquals("svalue1", (String) map.get("skey1"));
        assertEquals("svalue2", (String) map.get("skey2"));
        assertEquals("svalue3", (String) map.get("skey3"));

        // Transparency - entrySet()
        checkEntrySet(map, true);
 
        // Transparency - removal via web object
        session.removeAttribute("skey1");
        checkMapSize(map, 2);
        assertNull(map.get("skey1"));

        // Transparency - removal via map
        map.remove("skey2");
        checkMapSize(map, 1);
        assertNull(session.getAttribute("skey2"));

        // Transparency - addition via web object
        session.setAttribute("skeyA", "svalueA");
        checkMapSize(map, 2);
        assertEquals("svalueA", (String) map.get("skeyA"));

        // Transparency - addition via map
        map.put("skeyB", "svalueB");
        checkMapSize(map, 3);
        assertEquals("svalueB", (String) session.getAttribute("skeyB"));

        // Transparency - replacement via web object
        session.setAttribute("skeyA", "newvalueA");
        checkMapSize(map, 3);
        assertEquals("newvalueA", (String) map.get("skeyA"));

        // Transparency - replacement via map
        map.put("skeyB", "newvalueB");
        checkMapSize(map, 3);
        assertEquals("newvalueB", (String) session.getAttribute("skeyB"));

        // Clearing the map
        map.clear();
        checkMapSize(map, 0);

    }


    // ------------------------------------------------------- Protected Methods


    protected void checkMapSize(Map map, int size) {
        // Check reported size of the map
        assertEquals(size, map.size());
        // Iterate over key set
        int nk = 0;
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
            keys.next();
            nk++;
        }
        assertEquals(size, nk);
        // Iterate over entry set
        int nv = 0;
        Iterator values = map.entrySet().iterator();
        while (values.hasNext()) {
            values.next();
            nv++;
        }
        assertEquals(size, nv);
        // Count the values
        assertEquals(size, map.values().size());
    }

    // Test to ensure proper entrySet() and are modifiable optionally
    protected void checkEntrySet(Map map, boolean modifiable) {
        assertTrue(map.size() > 1);
        Set entries = map.entrySet();
        assertTrue(map.size() == entries.size());
        Object o = entries.iterator().next();

        assertTrue(o instanceof Map.Entry);

        if (!modifiable) {
            try {
                ((Map.Entry)o).setValue(new Object());
                fail("Should have thrown UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                ; // expected result
            }
        } else {
            // Should pass and not throw UnsupportedOperationException
            Map.Entry e = (Map.Entry)o;
            e.setValue(e.setValue(new Object()));
        }    
    }    

    // Create a new instance of the appropriate Context type for this test case
    protected Context createContext() {
        return (new ServletWebContext(scontext, request, response));
    }


}
