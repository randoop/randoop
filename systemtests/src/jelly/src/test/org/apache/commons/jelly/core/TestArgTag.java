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
package org.apache.commons.jelly.core;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.core.ArgTag;
import org.apache.commons.jelly.tags.core.ArgTagParent;
import org.apache.commons.jelly.test.BaseJellyTest;

/**
 * @author Rodney Waldhoff
 * @version $Revision: 1.9 $ $Date: 2004/10/26 23:54:37 $
 */
public class TestArgTag extends BaseJellyTest {

    public TestArgTag(String name) {
        super(name);
    }

    public static TestSuite suite() throws Exception {
        return new TestSuite(TestArgTag.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        parentTag = new MockArgTagParent();
        argTag = new ArgTag();
        argTag.setContext(getJellyContext());
        argTag.setParent(parentTag);
        argTag.setBody(new MockScript());
    }

    public void tearDown() throws Exception {
        super.tearDown();
        parentTag = null;
        argTag = null;
    }

    public void testToBooleanFromString() throws Exception {
        argTag.setType("boolean");
        argTag.setValue("true");
        argTag.doTag(getXMLOutput());
        assertEquals(Boolean.TYPE,parentTag.getType(0));
        assertEquals(Boolean.TRUE,parentTag.getValue(0));
    }

    public void testToCharFromString() throws Exception {
        argTag.setType("char");
        argTag.setValue("X");
        argTag.doTag(getXMLOutput());
        assertEquals(Character.TYPE,parentTag.getType(0));
        assertEquals(new Character('X'),parentTag.getValue(0));
    }

    public void testToByteFromString() throws Exception {
        argTag.setType("byte");
        argTag.setValue("17");
        argTag.doTag(getXMLOutput());
        assertEquals(Byte.TYPE,parentTag.getType(0));
        assertEquals(new Byte((byte)17),parentTag.getValue(0));
    }

    public void testToByteFromNumber() throws Exception {
        argTag.setType("byte");
        argTag.setValue(new Double(17.3d));
        argTag.doTag(getXMLOutput());
        assertEquals(Byte.TYPE,parentTag.getType(0));
        assertEquals(new Byte((byte)17),parentTag.getValue(0));
    }

    public void testToShortFromString() throws Exception {
        argTag.setType("short");
        argTag.setValue("17");
        argTag.doTag(getXMLOutput());
        assertEquals(Short.TYPE,parentTag.getType(0));
        assertEquals(new Short((short)17),parentTag.getValue(0));
    }

    public void testToShortFromNumber() throws Exception {
        argTag.setType("short");
        argTag.setValue(new Double(17.3d));
        argTag.doTag(getXMLOutput());
        assertEquals(Short.TYPE,parentTag.getType(0));
        assertEquals(new Short((short)17),parentTag.getValue(0));
    }

    public void testToIntFromString() throws Exception {
        argTag.setType("int");
        argTag.setValue("17");
        argTag.doTag(getXMLOutput());
        assertEquals(Integer.TYPE,parentTag.getType(0));
        assertEquals(new Integer((int)17),parentTag.getValue(0));
    }

    public void testToIntFromNumber() throws Exception {
        argTag.setType("int");
        argTag.setValue(new Double(17.3d));
        argTag.doTag(getXMLOutput());
        assertEquals(Integer.TYPE,parentTag.getType(0));
        assertEquals(new Integer((int)17),parentTag.getValue(0));
    }

    public void testToFloatFromString() throws Exception {
        argTag.setType("float");
        argTag.setValue("17.3");
        argTag.doTag(getXMLOutput());
        assertEquals(Float.TYPE,parentTag.getType(0));
        assertEquals(new Float((float)17.3),parentTag.getValue(0));
    }

    public void testToFloatFromNumber() throws Exception {
        argTag.setType("float");
        argTag.setValue(new Double(17.3d));
        argTag.doTag(getXMLOutput());
        assertEquals(Float.TYPE,parentTag.getType(0));
        assertEquals(new Float((float)17.3),parentTag.getValue(0));
    }

    public void testToLongFromString() throws Exception {
        argTag.setType("long");
        argTag.setValue("17");
        argTag.doTag(getXMLOutput());
        assertEquals(Long.TYPE,parentTag.getType(0));
        assertEquals(new Long((int)17),parentTag.getValue(0));
    }

    public void testToLongFromNumber() throws Exception {
        argTag.setType("long");
        argTag.setValue(new Double(17.3d));
        argTag.doTag(getXMLOutput());
        assertEquals(Long.TYPE,parentTag.getType(0));
        assertEquals(new Long((long)17),parentTag.getValue(0));
    }

    public void testToDoubleFromString() throws Exception {
        argTag.setType("double");
        argTag.setValue("17.3");
        argTag.doTag(getXMLOutput());
        assertEquals(Double.TYPE,parentTag.getType(0));
        assertEquals(new Double((double)17.3),parentTag.getValue(0));
    }

    public void testToDoubleFromNumber() throws Exception {
        argTag.setType("double");
        argTag.setValue(new Long(17L));
        argTag.doTag(getXMLOutput());
        assertEquals(Double.TYPE,parentTag.getType(0));
        assertEquals(new Double((double)17),parentTag.getValue(0));
    }

    public void testToPrimitiveFromNull() throws Exception {
        String[] types = { "boolean", "char", "byte", "short", "int", "float", "long", "double" };
        for(int i=0;i<types.length;i++) {
            argTag.setType(types[i]);
            argTag.setValue(null);
            try {
                argTag.doTag(getXMLOutput());
                fail("Expected JellyException");
            } catch (JellyException e) {
                // expected
            }
        }
    }

    public void testFromNull() throws Exception {
        Class[] types = { Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Float.class, Long.class, Double.class, String.class, Object.class };
        for(int i=0;i<types.length;i++) {
            argTag.setType(types[i].getName());
            argTag.setValue(null);
            argTag.doTag(getXMLOutput());
            assertEquals(types[i],parentTag.getType(i));
            assertNull(parentTag.getValue(i));
        }
    }

    private MockArgTagParent parentTag = null;
    private ArgTag argTag = null;

    class MockArgTagParent extends TagSupport implements ArgTagParent {
        public void addArgument(Class type, Object value) {
            typeList.add(type);
            valueList.add(value);
        }

        public void doTag(XMLOutput output)  {
        }

        private Class getType(int i) {
            return (Class)(typeList.get(i));
        }

        private Object getValue(int i) {
            return valueList.get(i);
        }

        private List typeList = new ArrayList();
        private List valueList = new ArrayList();
    }

    class MockScript implements Script {
        public Script compile() throws JellyException {
            return this;
        }

        public void run(JellyContext context, XMLOutput output) throws JellyTagException {
        }
    }

}