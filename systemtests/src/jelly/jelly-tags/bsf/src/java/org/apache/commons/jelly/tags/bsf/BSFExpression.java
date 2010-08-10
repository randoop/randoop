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
package org.apache.commons.jelly.tags.bsf;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFManager;

import java.util.Iterator;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.expression.ExpressionSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/** Represents a BSF expression
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.5 $
  */
public class BSFExpression extends ExpressionSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( BSFExpression.class );

    /** The expression */
    private String text;

    /** The BSF Engine to evaluate expressions */
    private BSFEngine engine;
    /** The BSF Manager to evaluate expressions */
    private BSFManager manager;

    /** The adapter to BSF's ObjectRegistry that uses the JellyContext */
    private JellyContextRegistry registry;

    public BSFExpression(String text, BSFEngine engine, BSFManager manager, JellyContextRegistry registry) {
        this.text = text;
        this.engine = engine;
        this.manager = manager;
        this.registry = registry;
    }

    // Expression interface
    //-------------------------------------------------------------------------
    public String getExpressionText() {
        return "${" + text + "}";
    }

    public Object evaluate(JellyContext context) {
        // XXXX: unfortunately we must sychronize evaluations
        // so that we can swizzle in the context.
        // maybe we could create an expression from a context
        // (and so create a BSFManager for a context)
        synchronized (registry) {
            registry.setJellyContext(context);

            try {
                // XXXX: hack - there must be a better way!!!
                for ( Iterator iter = context.getVariableNames(); iter.hasNext(); ) {
                    String name = (String) iter.next();
                    Object value = context.getVariable( name );
                    manager.declareBean( name, value, value.getClass() );
                }
                return engine.eval( text, -1, -1, text );
            }
            catch (Exception e) {
                log.warn( "Caught exception evaluating: " + text + ". Reason: " + e, e );
                return null;
            }
        }
    }
}
