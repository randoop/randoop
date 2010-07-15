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
package org.apache.commons.jelly.expression;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.expression.jexl.JexlExpressionFactory;

/**
 * Tests the use of Expression parsing
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.8 $
 */
public class TestDynaBeans extends TestCase {

    protected JellyContext context = new JellyContext();
    protected ExpressionFactory factory = new JexlExpressionFactory();

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TestDynaBeans.class);
    }

    public TestDynaBeans(String testName) {
        super(testName);
    }

    public void testDynaBeans() throws Exception {
        DynaClass dynaClass = createDynaClass();
        DynaBean dynaBean = dynaClass.newInstance();
        dynaBean.set( "stringProperty", "foo" );
        dynaBean.set( "intProperty", new Integer(24) );

        context.setVariable("dbean", dynaBean);

        assertExpression("${dbean.stringProperty}", "foo");
        assertExpression("${dbean.intProperty}", new Integer(24));
    }

    protected DynaClass createDynaClass() {
        DynaProperty[] properties = {
            new DynaProperty("booleanProperty", Boolean.TYPE),
            new DynaProperty("booleanSecond", Boolean.TYPE),
            new DynaProperty("doubleProperty", Double.TYPE),
            new DynaProperty("floatProperty", Float.TYPE),
            new DynaProperty("intProperty", Integer.TYPE),
            new DynaProperty("listIndexed", List.class),
            new DynaProperty("longProperty", Long.TYPE),
            new DynaProperty("mappedProperty", Map.class),
            new DynaProperty("mappedIntProperty", Map.class),
            new DynaProperty("nullProperty", String.class),
            new DynaProperty("shortProperty", Short.TYPE),
            new DynaProperty("stringProperty", String.class),
        };
        return new BasicDynaClass("TestDynaClass", null, properties);
    }


    protected void assertExpression(String expressionText, Object expectedValue) throws Exception {
        Expression expression = CompositeExpression.parse(expressionText, factory);
        assertTrue( "Created a valid expression for: " + expressionText, expression != null );
        Object value = expression.evaluate(context);
        //assertEquals( "Expression for: " + expressionText + " is: " + expression, expectedValue, value );
        assertEquals( "Wrong result for expression: " + expressionText, expectedValue, value );
    }
}
