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


import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.chain.Context;


/**
 * Extension of <code>ContextBaseTestCase</code> to validate property
 * delegation.
 */

public class TestContextTestCase extends ContextBaseTestCase {


    // ------------------------------------------------------------ Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public TestContextTestCase(String name) {
        super(name);
    }


    // ---------------------------------------------------- Overall Test Methods


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
        return (new TestSuite(TestContextTestCase.class));
    }


    // ------------------------------------------------- Individual Test Methods


    // Test state of newly created instance
    public void testPristine() {

        super.testPristine();
        assertEquals("readOnly", (String) context.get("readOnly"));
        assertEquals("readWrite", (String) context.get("readWrite"));
        assertEquals("writeOnly", ((TestContext) context).returnWriteOnly());

    }


    // Test a read only property on the Context implementation class
    public void testReadOnly() {

        Object readOnly = context.get("readOnly");
        assertNotNull("readOnly found", readOnly);
        assertTrue("readOnly String",
                   readOnly instanceof String);
        assertEquals("readOnly value", "readOnly", readOnly);

        try {
            context.put("readOnly", "new readOnly");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            ; // Expected result
        }
        assertEquals("readOnly unchanged", "readOnly",
                     (String) context.get("readOnly"));

    }


    // Test a read write property on the Context implementation class
    public void testReadWrite() {

        Object readWrite = context.get("readWrite");
        assertNotNull("readWrite found", readWrite);
        assertTrue("readWrite String",
                   readWrite instanceof String);
        assertEquals("readWrite value", "readWrite", readWrite);

        context.put("readWrite", "new readWrite");
        readWrite = context.get("readWrite");
        assertNotNull("readWrite found", readWrite);
        assertTrue("readWrite String",
                   readWrite instanceof String);
        assertEquals("readWrite value", "new readWrite", readWrite);

    }


    // Test a write only property on the Context implementation class
    public void testWriteOnly() {

        Object writeOnly = ((TestContext) context).returnWriteOnly();
        assertNotNull("writeOnly found", writeOnly);
        assertTrue("writeOnly String",
                   writeOnly instanceof String);
        assertEquals("writeOnly value", "writeOnly", writeOnly);

        context.put("writeOnly", "new writeOnly");
        writeOnly = ((TestContext) context).returnWriteOnly();
        assertNotNull("writeOnly found", writeOnly);
        assertTrue("writeOnly String",
                   writeOnly instanceof String);
        assertEquals("writeOnly value", "new writeOnly", writeOnly);

    }


    // ------------------------------------------------------- Protected Methods


    // Create a new instance of the appropriate Context type for this test case
    protected Context createContext() {
        return (new TestContext());
    }


}
