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
package org.apache.commons.jelly.tags.beanshell;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.tags.beanshell.BeanShellExpressionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/** Tests the BeanShell EL
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.4 $
  */
public class TestBeanShellEL extends TestCase {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( TestBeanShellEL.class );

    /** Jelly context */
    protected JellyContext context;

    /** The factory of Expression objects */
    protected ExpressionFactory factory;


    public static void main( String[] args ) {
        TestRunner.run( suite() );
    }

    public static Test suite() {
        return new TestSuite(TestBeanShellEL.class);
    }

    public TestBeanShellEL(String testName) {
        super(testName);
    }

    public void setUp() {
        context = new JellyContext();
        context.setVariable( "foo", "abc" );
        context.setVariable( "bar", new Integer( 123 ) );
        factory = new BeanShellExpressionFactory();
    }

    public void testEL() throws Exception {
        assertExpression( "foo", "abc" );
        assertExpression( "bar * 2", new Integer( 246 ) );
        assertExpression( "bar == 123", Boolean.TRUE );
        assertExpression( "bar == 124", Boolean.FALSE );
        assertExpression( "foo.equals( \"abc\" )", Boolean.TRUE );
        assertExpression( "foo.equals( \"xyz\" )", Boolean.FALSE );
    }

    /** Evaluates the given expression text and tests it against the expected value */
    protected void assertExpression( String expressionText, Object expectedValue ) throws Exception {
        Expression expr = factory.createExpression( expressionText );
        Object value = expr.evaluate( context );
        assertEquals( "Value of expression: " + expressionText, expectedValue, value );
    }
}

