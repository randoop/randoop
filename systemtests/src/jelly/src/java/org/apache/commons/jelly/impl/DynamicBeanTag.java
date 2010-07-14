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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.ConvertingWrapDynaBean;
import org.apache.commons.collections.BeanMap;
import org.apache.commons.jelly.DynaBeanTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This tag is bound onto a Java Bean class. When the tag is invoked a bean will be created
 * using the tags attributes.
 * The bean may also have an invoke method called invoke(), run(), execute() or some such method
 * which will be invoked after the bean has been configured.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @version $Revision: 1.7 $
 */
public class DynamicBeanTag extends DynaBeanTagSupport implements BeanSource {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(DynamicBeanTag.class);

    /** Empty arguments for Method.invoke() */
    private static final Object[] emptyArgs = {};

    /** the bean class */
    private Class beanClass;

    /** the current bean instance */
    private Object bean;

    /** the method to invoke on the bean */
    private Method method;

    /**
     * the tag attribute name that is used to declare the name
     * of the variable to export after running this tag
     */
    private String variableNameAttribute;

    /** the current variable name that the bean should be exported as */
    private String var;

    /** the set of attribute names we've already set */
    private Set setAttributesSet = new HashSet();

    /** the attribute definitions */
    private Map attributes;

    /**
     *
     * @param beanClass Class of the bean that will receive the setter events
     * @param attributes
     * @param variableNameAttribute
     * @param method method of the Bean to invoke after the attributes have been set.  Can be null.
     */
    public DynamicBeanTag(Class beanClass, Map attributes, String variableNameAttribute, Method method) {
        this.beanClass = beanClass;
        this.method = method;
        this.attributes = attributes;
        this.variableNameAttribute = variableNameAttribute;
    }

    public void beforeSetAttributes() throws JellyTagException {
        // create a new dynabean before the attributes are set
        try {
            bean = beanClass.newInstance();
            setDynaBean( new ConvertingWrapDynaBean( bean ) );
        } catch (InstantiationException e) {
            throw new JellyTagException("Could not instantiate dynabean",e);
        } catch (IllegalAccessException e) {
            throw new JellyTagException("Could not instantiate dynabean",e);
        }

        setAttributesSet.clear();
    }

    public void setAttribute(String name, Object value) throws JellyTagException {
        boolean isVariableName = false;
        if (variableNameAttribute != null ) {
            if ( variableNameAttribute.equals( name ) ) {
                if (value == null) {
                    var = null;
                }
                else {
                    var = value.toString();
                }
                isVariableName = true;
            }
        }
        if (! isVariableName) {

            // #### strictly speaking we could
            // know what attributes are specified at compile time
            // so this dynamic set is unnecessary
            setAttributesSet.add(name);

            // we could maybe implement attribute specific validation here

            super.setAttribute(name, value);
        }
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {

        // lets find any attributes that are not set and
        for ( Iterator iter = attributes.values().iterator(); iter.hasNext(); ) {
            Attribute attribute = (Attribute) iter.next();
            String name = attribute.getName();
            if ( ! setAttributesSet.contains( name ) ) {
                if ( attribute.isRequired() ) {
                    throw new MissingAttributeException(name);
                }
                // lets get the default value
                Object value = null;
                Expression expression = attribute.getDefaultValue();
                if ( expression != null ) {
                    value = expression.evaluate(context);
                }

                // only set non-null values?
                if ( value != null ) {
                    super.setAttribute(name, value);
                }
            }
        }

        // If the dynamic bean is itself a tag, let it execute itself
        if (bean instanceof Tag)
        {
            Tag tag = (Tag) bean;
            tag.setBody(getBody());
            tag.setContext(getContext());
            tag.setParent(getParent());
            ((Tag) bean).doTag(output);

            return;
        }

        invokeBody(output);

        // export the bean if required
        if ( var != null ) {
            context.setVariable(var, bean);
        }

        // now, I may invoke the 'execute' method if I have one
        if ( method != null ) {
            try {
                method.invoke( bean, emptyArgs );
            }
            catch (IllegalAccessException e) {
                methodInvocationException(bean, method, e);
            }
            catch (IllegalArgumentException e) {
                methodInvocationException(bean, method, e);
            }
            catch (InvocationTargetException e) {
                // methodInvocationError(bean, method, e);

                Throwable inner = e.getTargetException();

                throw new JellyTagException(inner);

            }
        }
    }

    /**
     * Report the state of the bean when method invocation fails
     * so that the user can determine any problems that might
     * be occuring while using dynamic jelly beans.
     *
     * @param bean Bean on which <code>method</code was invoked
     * @param method Method that was invoked
     * @param e Exception throw when <code>method</code> was invoked
     */
    private void methodInvocationException(Object bean, Method method, Exception e) throws JellyTagException {
        log.error("Could not invoke " + method, e);
        BeanMap beanMap = new BeanMap(bean);

        log.error("Bean properties:");
        for (Iterator i = beanMap.keySet().iterator(); i.hasNext();) {
            String property = (String) i.next();
            Object value = beanMap.get(property);
            log.error(property + " -> " + value);
        }

        log.error(beanMap);
        throw new JellyTagException(e);
    }

    // Properties
    //-------------------------------------------------------------------------
    /**
     * @return the bean that has just been created
     */
    public Object getBean() {
        return bean;
    }
}
