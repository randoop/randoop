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
package org.apache.commons.jelly.tags.core;

import java.lang.reflect.InvocationTargetException;
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
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.18 $
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

    /** The default value */
    private Expression defaultValue;

    /** The target object on which to set a property. */
    private Object target;

    /** The name of the property to set on the target object. */
    private String property;

    /** Should we XML encode the body of this tag as text? */
    private boolean encode = true;

    public SetTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        // perform validation up front to fail fast
        if ( var != null ) {
            if ( target != null || property != null ) {
                throw new JellyTagException( "The 'target' and 'property' attributes cannot be used in combination with the 'var' attribute" );
            }
        }
        else {
            if ( target == null ) {
                throw new JellyTagException( "Either a 'var' or a 'target' attribute must be defined for this tag" );
            }
            if ( property == null ) {
                throw new JellyTagException( "The 'target' attribute requires the 'property' attribute" );
            }
        }

        Object answer = null;
        if ( value != null ) {
            answer = value.evaluate(context);
            if (defaultValue != null && isEmpty(answer)) {
                answer = defaultValue.evaluate(context);
            }
        }
        else {
            answer = getBodyText(isEncode());
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

    /**
     * Sets the default value to be used if the value exprsesion results
     * in a null value or blank String
     */
    public void setDefaultValue(Expression defaultValue) {
        this.defaultValue = defaultValue;
    }

    /** Sets the target object on which to set a property. */
    public void setTarget(Object target) {
        this.target = target;
    }

    /** Sets the name of the property to set on the target object. */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Returns whether the body of this tag will be XML encoded or not.
     */
    public boolean isEncode() {
        return encode;
    }

    /**
     * Sets whether the body of the tag should be XML encoded as text (so that &lt; and &gt; are
     * encoded as &amp;lt; and &amp;gt;) or leave the text as XML which is the default.
     * This is only used if this tag is specified with no value so that the text body of this
     * tag is used as the body.
     */
    public void setEncode(boolean encode) {
        this.encode = encode;
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
        } catch (InvocationTargetException e) {
            log.error( "Failed to set the property: " + property + " on bean: " + target + " to value: " + value + " due to exception: " + e, e );
        } catch (IllegalAccessException e) {
            log.error( "Failed to set the property: " + property + " on bean: " + target + " to value: " + value + " due to exception: " + e, e );
        }
    }

    /**
     * @param value
     * @return true if the given value is null or an empty String
     */
    protected boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            String s = (String) value;
            return s.length() == 0;
        }
        return false;
    }

}
