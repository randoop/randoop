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
package org.apache.commons.jelly.tags.swt;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.tags.core.UseBeanTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Widget;

/**
 * An abstract base class for Layout or LayoutData tags.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version 1.1
 */
public abstract class LayoutTagSupport extends UseBeanTag {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(LayoutTagSupport.class);

    private String var;

    public LayoutTagSupport(Class layoutClass) {
        super(layoutClass);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the parent widget which this widget will be added to.
     */
    public Widget getParentWidget() {
        WidgetTag tag = (WidgetTag) findAncestorWithClass(WidgetTag.class);
        if (tag != null) {
            return tag.getWidget();
        }
        return null;
    }

    /**
     * Sets the name of the variable to use to expose the new Layout object.
     * If this attribute is not set then the parent widget tag will have its
     * layout property set.
     */
    public void setVar(String var) {
        this.var = var;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    /**
     * Either defines a variable or adds the current component to the parent
     */
    protected void processBean(String var, Object bean) throws JellyTagException {
        if (var != null) {
            context.setVariable(var, bean);
        }
    }

    /**
     * @see org.apache.commons.jelly.tags.core.UseBeanTag#setBeanProperties(java.lang.Object, java.util.Map)
     */
    protected void setBeanProperties(Object bean, Map attributes) throws JellyTagException {

        if (bean != null) {
            Class theClass = bean.getClass();
            for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                Object value = entry.getValue();

                value = convertValue(bean, name, value);

                try {
                    // lets first see if there's a field available
                    Field field = theClass.getField(name);
                    if (field != null) {
                        if (value instanceof String) {
                            value = ConvertUtils.convert((String) value, field.getType());
                        }
                        field.set(bean, value);
                    } else {
                        BeanUtils.setProperty(bean, name, value);
                    }
                } catch (NoSuchFieldException e) {
                    throw new JellyTagException(e);
                } catch (IllegalAccessException e) {
                    throw new JellyTagException(e);
                } catch (InvocationTargetException e) {
                    throw new JellyTagException(e);
                }
            }
        }
    }

    /**
     * Provides a strategy method that allows values to be converted,
     * particularly to support integer enumerations and String representations.
     *
     * @param bean is the bean on which the property is to be set
     * @param name is the name of the property
     * @param value the value of the property
     * @return the new value
     */
    protected Object convertValue(Object bean, String name, Object value)
        throws JellyTagException {
        return value;
    }

}
