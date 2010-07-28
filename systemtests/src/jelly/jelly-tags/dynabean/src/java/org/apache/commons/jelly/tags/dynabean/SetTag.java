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
package org.apache.commons.jelly.tags.dynabean;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** A tag which sets a variable from the result of an expression
  *
 * @author Theo Niemeijer
 * @version 1.0
  */
public class SetTag extends TagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(SetTag.class);

    /** The variable name to export. */
    private String var;

    /** The variable scope to export */
    private String scope;

    /** The expression to evaluate. */
    private Expression value;

    /** The target object on which to set a property. */
    private Object target;

    /** The name of the property to set on the target object. */
    private String property;

    public SetTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        Object answer = null;
        if ( value != null ) {
            answer = value.evaluate(context);
        }
        else {
            answer = getBodyText();
        }

        // Assume that if a var name and a property is given then
        // var is the name of an object in the context
        if (( var != null )
        && ( property != null)
        && ( target == null ))
        {
            // Get object from context
            if ( scope != null ) {
               target = context.getVariable(var, scope);
            }
            else {
                target = context.getVariable(var);
            }

            if (target != null) {
                var = null;
            }
        }

        if ( var != null ) {


            if ( scope != null ) {
                context.setVariable(var, scope, answer);
            }
            else {
                context.setVariable(var, answer);
            }
        }
        else {
            if ( target == null ) {
                throw new JellyTagException( "Either a 'var' or a 'target' attribute must be defined for this tag" );
            }
            if ( property == null ) {
                throw new JellyTagException( "You must define a 'property' attribute if you specify a 'target'" );
            }
            setPropertyValue( target, property, answer );
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    /** Sets the variable name to define for this expression
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Sets the variable scope for this variable. For example setting this value to 'parent' will
     * set this value in the parent scope. When Jelly is run from inside a Servlet environment
     * then other scopes will be available such as 'request', 'session' or 'application'.
     *
     * Other applications may implement their own custom scopes.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /** Sets the expression to evaluate. */
    public void setValue(Expression value) {
        this.value = value;
    }

    /** Sets the target object on which to set a property. */
    public void setTarget(Object target) {
        this.target = target;
    }

    /** Sets the name of the property to set on the target object. */
    public void setProperty(String property) {
        this.property = property;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void setPropertyValue( Object target, String property, Object value ) {
        try {
            if ( target instanceof Map ) {
                Map map = (Map) target;
                map.put( property, value );
            }
            else {
                BeanUtils.setProperty( target, property, value );
            }
        }
        catch (Exception e) {
            log.error( "Failed to set the property: " + property + " on bean: " + target + " to value: " + value + " due to exception: " + e, e );
        }
    }

}
