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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.expression.jexl.JexlExpressionFactory;

/**
 * Tests the use of Expression parsing
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.8 $
 */
public class TestExpressions extends TestCase {

    protected JellyContext context = new JellyContext();
    protected ExpressionFactory factory = new JexlExpressionFactory();

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TestExpressions.class);
    }

    public TestExpressions(String testName) {
        super(testName);
    }

    public void testExpresssions() throws Exception {
        context.setVariable("topping", "cheese");
        context.setVariable("type", "deepPan");


        assertExpression("foo", "foo");
        assertExpression("${topping}", "cheese");
        assertExpression("some${topping}", "somecheese");
        assertExpression(" some ${topping} ", " some cheese ");
        assertExpression("${topping}y", "cheesey");
        assertExpression("A ${topping} ${type} pizza", "A cheese deepPan pizza");
        assertExpression("${topping}-${type}", "cheese-deepPan");
    }

    public void testAntExpresssions() throws Exception {
        context.setVariable("maven.home.foo", "cheese");

        assertExpression("${maven.home.foo}", "cheese");
        assertExpression("${maven.some.madeup.name}", null);
        assertExpression("cheese ${maven.some.madeup.name}pizza", "cheese pizza");
        assertExpression("ham and ${maven.home.foo} pizza", "ham and cheese pizza");
        assertExpression("${maven.home.foo.length()}", new Integer(6));
    }

    public void testNotConditions() throws Exception {
        context.setVariable("a", Boolean.TRUE);
        context.setVariable("b", Boolean.FALSE);
        context.setVariable("c", "true");
        context.setVariable("d", "false");

        assertExpression("${a}", Boolean.TRUE);
        assertExpression("${!a}", Boolean.FALSE);
        assertExpression("${b}", Boolean.FALSE);
        assertExpression("${!b}", Boolean.TRUE);

        assertExpression("${c}", "true");
        assertExpression("${!c}", Boolean.FALSE);
        assertExpression("${d}", "false");
        assertExpression("${!d}", Boolean.TRUE);
    }

    public void testNotConditionsWithDot() throws Exception {
        context.setVariable("x.a", Boolean.TRUE);
        context.setVariable("x.b", Boolean.FALSE);
        context.setVariable("x.c", "true");
        context.setVariable("x.d", "false");

        assertExpression("${x.a}", Boolean.TRUE);
        assertExpression("${!x.a}", Boolean.FALSE);
        assertExpression("${x.b}", Boolean.FALSE);
        assertExpression("${!x.b}", Boolean.TRUE);

        assertExpression("${x.c}", "true");
        assertExpression("${!x.c}", Boolean.FALSE);
        assertExpression("${x.d}", "false");
        assertExpression("${!x.d}", Boolean.TRUE);
    }

    public void testNull() throws Exception {
        context.setVariable("something.blank", "");
        context.setVariable("something.ok", "cheese");

        assertExpression("${something.blank.length() == 0}", Boolean.TRUE);
        assertExpression("${something.blank == ''}", Boolean.TRUE);
        assertExpression("${something.ok != null}", Boolean.TRUE);
        assertExpression("${something.ok != ''}", Boolean.TRUE);
        // null is a reserved word
        //assertExpression("${something.null != ''}", Boolean.FALSE);
        assertExpression("${unknown == null}", Boolean.TRUE);
    }

    protected void assertExpression(String expressionText, Object expectedValue) throws Exception {
        Expression expression = CompositeExpression.parse(expressionText, factory);
        assertTrue( "Created a valid expression for: " + expressionText, expression != null );
        Object value = expression.evaluate(context);
        assertEquals( "Wrong result for expression: " + expressionText, expectedValue, value );

        String text = expression.getExpressionText();
        assertEquals( "Wrong textual representation for expression text: ", expressionText, text);
    }
}
