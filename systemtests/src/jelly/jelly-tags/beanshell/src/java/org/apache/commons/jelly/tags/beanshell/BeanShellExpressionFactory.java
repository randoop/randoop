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

import bsh.EvalError;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionFactory;

/** Represents a factory of <a href="http://www.beanshell.org">beanshell</a> expressions
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.5 $
  */
public class BeanShellExpressionFactory implements ExpressionFactory {

    /**
     * A helper method to return the JellyInterpreter for the given JellyContext
     */
    public static JellyInterpreter getInterpreter(JellyContext context) throws EvalError {

        /**
         * @todo when we can unify the BeanShell and Jelly variable scopes we can share a single
         * BeanShell context for each JellyContext.
         * For now lets create a new one each time, which is slower.
         */
        JellyInterpreter interpreter = new JellyInterpreter();
        interpreter.setJellyContext(context);
        return interpreter;
/*
        JellyInterpreter interpreter
            = (JellyInterpreter) context.getVariable( "org.apache.commons.jelly.beanshell.JellyInterpreter" );
        if ( interpreter == null ) {
            interpreter = new JellyInterpreter();
            interpreter.setJellyContext(context);
            context.setVariable( "org.apache.commons.jelly.beanshell.JellyInterpreter", interpreter );
        }
        return interpreter;
*/
    }


    // ExpressionFactory interface
    //-------------------------------------------------------------------------
    public Expression createExpression(String text) throws JellyException {
        return new BeanShellExpression(text);
    }
}
