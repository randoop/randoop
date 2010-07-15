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
import bsh.Interpreter;

import java.util.Iterator;

import org.apache.commons.jelly.JellyContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Integrates BeanShell's interpreter with Jelly's JellyContext
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.4 $
  */
public class JellyInterpreter extends Interpreter {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( JellyInterpreter.class );

    private JellyContext context;

    public JellyInterpreter() {
    }

    public JellyContext getJellyContext() {
        return context;
    }

    public void setJellyContext(JellyContext context) throws EvalError {
        this.context = context;

        // now pass in all the variables
        for ( Iterator iter = context.getVariableNames(); iter.hasNext(); ) {
            String name = (String) iter.next();
            Object value = context.getVariable(name);
            name = convertVariableName(name);
            if (name != null) {
                set( name, value );
            }
        }

        // lets pass in the Jelly context
        set( "context", context );
    }

/*

    // the following code doesn't work - it seems that
    // all variables must be passed into the Interpreter
    // via set() method

    public Object get(String name) throws EvalError {
        if ( context != null ) {
            Object answer = context.getVariable( name );
            if ( answer != null ) {
                return answer;
            }
        }
        return super.get( name );
    }
*/

    /**
     * Converts variables to a beanshell allowable format or hides names that
     * can't be converted, by returning null.
     * For now lets just turn '.' into '_'
     */
    protected String convertVariableName(String name) {
        return name.replace('.', '_');
    }
}
