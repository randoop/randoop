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
package org.apache.commons.chain.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.chain.Context;



/**
 * <p>Test case for the <code>ContextBase</code> class.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2004/02/25 00:01:05 $
 */

public class ContextBaseTestCase extends TestCase {


    // ---------------------------------------------------- Instance Variables


    /**
     * The {@link Context} instance under test.
     */
    protected Context context = null;



    // ---------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ContextBaseTestCase(String name) {
        super(name);
    }


    // -------------------------------------------------- Overall Test Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        context = createContext();
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ContextBaseTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        context = null;
    }


    // ------------------------------------------------ Individual Test Methods


    // Test ability to get, put, and remove attributes
    public void testAttributes() {

        Object value = null;
        checkAttributeCount(0);

        context.put("foo", "This is foo");
        checkAttributeCount(1);
        value = context.get("foo");
        assertNotNull("Returned foo", value);
        assertTrue("Returned foo type", value instanceof String);
        assertEquals("Returned foo value", "This is foo",
                     (String) value);

        context.put("bar", "This is bar");
        checkAttributeCount(2);
        value = context.get("bar");
        assertNotNull("Returned bar", value);
        assertTrue("Returned bar type", value instanceof String);
        assertEquals("Returned bar value", "This is bar",
                     (String) value);

        context.put("baz", "This is baz");
        checkAttributeCount(3);
        value = context.get("baz");
        assertNotNull("Returned baz", value);
        assertTrue("Returned baz type", value instanceof String);
        assertEquals("Returned baz value", "This is baz",
                     (String) value);

        context.put("baz", "This is new baz");
        checkAttributeCount(3); // Replaced, not added
        value = context.get("baz");
        assertNotNull("Returned baz", value);
        assertTrue("Returned baz type", value instanceof String);
        assertEquals("Returned baz value", "This is new baz",
                     (String) value);

        context.remove("bar");
        checkAttributeCount(2);
        assertNull("Did not return bar",
                   context.get("bar"));
        assertNotNull("Still returned foo",
                      context.get("foo"));
        assertNotNull("Still returned baz",
                      context.get("baz"));

        context.clear();
        checkAttributeCount(0);
        assertNull("Did not return foo",
                   context.get("foo"));
        assertNull("Did not return bar",
                   context.get("bar"));
        assertNull("Did not return baz",
                   context.get("baz"));

    }


    // Test containsKey() and containsValue()
    public void testContains() {

        assertTrue(!context.containsKey("bop"));
        assertTrue(!context.containsValue("bop value"));
        context.put("bop", "bop value");
        assertTrue(context.containsKey("bop"));
        assertTrue(context.containsValue("bop value"));
        context.remove("bop");
        assertTrue(!context.containsKey("bop"));
        assertTrue(!context.containsValue("bop value"));

    }


    // Test equals() and hashCode()
    public void testEquals() {

        // Compare to self
        assertTrue(context.equals(context));
        assertTrue(context.hashCode() == context.hashCode());

        // Compare to equivalent instance
        Context other = createContext();
        assertTrue(context.equals(other));
        assertTrue(context.hashCode() == other.hashCode());

        // Compare to non-equivalent instance - other modified
        other.put("bop", "bop value");
        assertTrue(!context.equals(other));
        assertTrue(context.hashCode() != other.hashCode());

        // Compare to non-equivalent instance - self modified
        other = createContext(); // reset to equivalence
        context.put("bop", "bop value");
        assertTrue(!context.equals(other));
        assertTrue(context.hashCode() != other.hashCode());

    }


    // Test keySet()
    public void testKeySet() {

        Set keySet = null;
        Collection all = new ArrayList();

        // Unsupported operations
        keySet = context.keySet();
        try {
            keySet.add("bop");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // Expected result
        }
        try {
            Collection adds = new ArrayList();
            adds.add("bop");
            keySet.addAll(adds);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // Expected result
        }

        // Before-modification checks
        keySet = context.keySet();
        assertEquals(createContext().size(), keySet.size());
        assertTrue(!keySet.contains("foo"));
        assertTrue(!keySet.contains("bar"));
        assertTrue(!keySet.contains("baz"));
        assertTrue(!keySet.contains("bop"));

        // Add the new elements
        context.put("foo", "foo value");
        context.put("bar", "bar value");
        context.put("baz", "baz value");
        all.add("foo");
        all.add("bar");
        all.add("baz");

        // After-modification checks
        keySet = context.keySet();
        assertEquals(expectedAttributeCount() + 3, keySet.size());
        assertTrue(keySet.contains("foo"));
        assertTrue(keySet.contains("bar"));
        assertTrue(keySet.contains("baz"));
        assertTrue(!keySet.contains("bop"));
        assertTrue(keySet.containsAll(all));

        // Remove a single element via remove()
        context.remove("bar");
        all.remove("bar");
        keySet = context.keySet();
        assertEquals(expectedAttributeCount() + 2, keySet.size());
        assertTrue(keySet.contains("foo"));
        assertTrue(!keySet.contains("bar"));
        assertTrue(keySet.contains("baz"));
        assertTrue(!keySet.contains("bop"));
        assertTrue(keySet.containsAll(all));

        // Remove a single element via keySet.remove()
        keySet.remove("baz");
        all.remove("baz");
        keySet = context.keySet();
        assertEquals(expectedAttributeCount() + 1, keySet.size());
        assertTrue(keySet.contains("foo"));
        assertTrue(!keySet.contains("bar"));
        assertTrue(!keySet.contains("baz"));
        assertTrue(!keySet.contains("bop"));
        assertTrue(keySet.containsAll(all));

        // Remove all elements via keySet.clear()
        keySet.clear();
        all.clear();
        assertEquals(expectedAttributeCount(), keySet.size());
        assertTrue(!keySet.contains("foo"));
        assertTrue(!keySet.contains("bar"));
        assertTrue(!keySet.contains("baz"));
        assertTrue(!keySet.contains("bop"));
        assertTrue(keySet.containsAll(all));

        // Add the new elements #2
        context.put("foo", "foo value");
        context.put("bar", "bar value");
        context.put("baz", "baz value");
        all.add("foo");
        all.add("bar");
        all.add("baz");

        // After-modification checks #2
        keySet = context.keySet();
        assertEquals(expectedAttributeCount() + 3, keySet.size());
        assertTrue(keySet.contains("foo"));
        assertTrue(keySet.contains("bar"));
        assertTrue(keySet.contains("baz"));
        assertTrue(!keySet.contains("bop"));
        assertTrue(keySet.containsAll(all));

    }


    // Test state of newly created instance
    public void testPristine() {

        checkAttributeCount(0);
        assertNull("No 'foo' attribute",
                   context.get("foo"));

    }


    // Test putAll()
    public void testPutAll() {

        // Check preconditions
        checkAttributeCount(0);
        assertNull(context.get("foo"));
        assertNull(context.get("bar"));
        assertNull(context.get("baz"));
        assertTrue(!context.containsKey("foo"));
        assertTrue(!context.containsKey("bar"));
        assertTrue(!context.containsKey("baz"));
        assertTrue(!context.containsValue("foo value"));
        assertTrue(!context.containsValue("bar value"));
        assertTrue(!context.containsValue("baz value"));

        // Call putAll()
        Map adds = new HashMap();
        adds.put("foo", "foo value");
        adds.put("bar", "bar value");
        adds.put("baz", "baz value");
        context.putAll(adds);

        // Check postconditions
        checkAttributeCount(3);
        assertEquals("foo value", (String) context.get("foo"));
        assertEquals("bar value", (String) context.get("bar"));
        assertEquals("baz value", (String) context.get("baz"));
        assertTrue(context.containsKey("foo"));
        assertTrue(context.containsKey("bar"));
        assertTrue(context.containsKey("baz"));
        assertTrue(context.containsValue("foo value"));
        assertTrue(context.containsValue("bar value"));
        assertTrue(context.containsValue("baz value"));

    }


    // -------------------------------------------------------- Support Methods


    // Verify the number of defined attributes
    protected void checkAttributeCount(int expected) {
        int actual = 0;
        Iterator keys = context.keySet().iterator();
        while (keys.hasNext()) {
            Object key = (Object) keys.next();
            actual++;
        }
        assertEquals("Correct attribute count",
                     expectedAttributeCount() + expected, actual);
        if (expected == 0) {
            assertTrue("Context should be empty", context.isEmpty());
        } else {
            assertTrue("Context should not be empty", !context.isEmpty());
        }
    }


    // Create a new instance of the appropriate Context type for this test case
    protected Context createContext() {
        return (new ContextBase());
    }


    // Return the expected size() for a Context for this test case
    protected int expectedAttributeCount() {
        return (createContext().size());
    }


}
