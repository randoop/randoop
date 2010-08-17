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

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.expression.ExpressionSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/** Represents a <a href="http://www.beanshell.org">beanshell</a> expression
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.4 $
  */
public class BeanShellExpression extends ExpressionSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( BeanShellExpression.class );

    /** The expression */
    private String text;

    public BeanShellExpression(String text) {
        this.text = text;
    }

    // Expression interface
    //-------------------------------------------------------------------------
    public String getExpressionText() {
        return "${" + text + "}";
    }

    public Object evaluate(JellyContext context) {
        try {
            JellyInterpreter interpreter = new JellyInterpreter();
            interpreter.setJellyContext(context);

            if ( log.isDebugEnabled() ) {
                log.debug( "Evaluating beanshell: " + text );
            }

            return interpreter.eval( text );
        }
        catch (Exception e) {
            log.warn( "Caught exception evaluating: " + text + ". Reason: " + e, e );
            return null;
        }
    }
}
