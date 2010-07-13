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
package org.apache.commons.jelly.impl;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;

import org.xml.sax.SAXException;

/**
 * <p><code>ExpressionScript</code> outputs the value of an expression as text.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class ExpressionScript implements Script {

    /** the expression evaluated as a String and output by this script */
    private Expression expression;

    public ExpressionScript() {
    }

    public ExpressionScript(Expression expression) {
        this.expression = expression;
    }

    public String toString() {
        return super.toString() + "[expression=" + expression + "]";
    }

    /** @return the expression evaluated as a String and output by this script */
    public Expression getExpression() {
        return expression;
    }

    /** Sets the expression evaluated as a String and output by this script */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    // Script interface
    //-------------------------------------------------------------------------
    public Script compile() {
        return this;
    }

    /** Evaluates the body of a tag */
    public void run(JellyContext context, XMLOutput output) throws JellyTagException {
        Object result = expression.evaluate(context);
        if ( result != null ) {

            try {
              output.objectData(result);
            } catch (SAXException e) {
                throw new JellyTagException("Could not write to XMLOutput",e);
            }

        }
    }
}
