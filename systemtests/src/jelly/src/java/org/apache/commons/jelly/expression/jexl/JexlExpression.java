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

package org.apache.commons.jelly.expression.jexl;

import java.util.Map;
import java.util.Set;
import java.util.Collection;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.expression.ExpressionSupport;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a <a href="http://jakarta.apache.org/commons/jexl.html">Jexl</a>
 * expression which fully supports the Expression Language in JSTL and JSP
 * along with some extra features like object method invocation.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.18 $
 */

public class JexlExpression extends ExpressionSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(JexlExpression.class);

    /** The Jexl expression object */
    private Expression expression;

    public JexlExpression(Expression expression) {
        this.expression = expression;
    }

    public String toString() {
        return super.toString() + "[" + expression.getExpression() + "]";
    }

    // Expression interface
    //-------------------------------------------------------------------------
    public String getExpressionText() {
        return "${" + expression.getExpression() + "}";
    }

    public Object evaluate(JellyContext context) {
        try {
            JexlContext jexlContext = new JellyJexlContext( context );
            if (log.isDebugEnabled()) {
                log.debug("Evaluating EL: " + expression.getExpression());
            }
            Object value = expression.evaluate(jexlContext);

            if (log.isDebugEnabled()) {
                log.debug("value of expression: " + value);
            }

            return value;
        }
        catch (Exception e) {
            log.warn("Caught exception evaluating: " + expression + ". Reason: " + e, e);
            return null;
        }
    }
}

class JellyJexlContext implements JexlContext {

    private Map vars;

    JellyJexlContext(JellyContext context) {
        this.vars = new JellyMap( context );
    }

    public void setVars(Map vars) {
        this.vars.clear();
        this.vars.putAll( vars );
    }

    public Map getVars() {
        return this.vars;
    }
}


class JellyMap implements Map {

    private JellyContext context;

    JellyMap(JellyContext context) {
        this.context = context;
    }

    public Object get(Object key) {
        return context.getVariable( (String) key );
    }

    public void clear() {
        // not implemented
    }

    public boolean containsKey(Object key) {
        return ( get( key ) != null );
    }

    public boolean containsValue(Object value) {
        return false;
    }

    public Set entrySet() {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }

    public Set keySet() {
        return null;
    }

    public Object put(Object key, Object value) {
        return null;
    }

    public void putAll(Map t) {
        // not implemented
    }

    public Object remove(Object key) {
        return null;
    }

    public int size() {
        return -1;
    }

    public Collection values() {
        return null;
    }
}
