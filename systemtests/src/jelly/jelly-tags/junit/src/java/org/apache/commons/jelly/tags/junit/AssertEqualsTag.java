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

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;

/**
 * Compares an actual object against an expected object and if they are different
 * then the test will fail.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class AssertEqualsTag extends AssertTagSupport {

    private Expression actual;
    private Expression expected;


    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        String message = getBodyText();

        Object expectedValue = expected.evaluate(context);
        Object actualValue = actual.evaluate(context);

        if (expectedValue == null && actualValue == null) {
            return;
        }
        if (actualValue != null && expectedValue.equals(actualValue)) {
            return;
        }

        String expressions = "\nExpected expression: "
            + expected.getExpressionText()
            + "\nActual expression: "
            + actual.getExpressionText();

        failNotEquals(message, expectedValue, actualValue, expressions);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the actual value which will be compared against the
     * expected value.
     */
    public void setActual(Expression actual) {
        this.actual = actual;
    }

    /**
     * Sets the expected value to be tested against
     */
    public void setExpected(Expression expected) {
        this.expected = expected;
    }
}
