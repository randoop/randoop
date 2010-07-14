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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * Creates a LayoutData object and sets it on the parent Widget.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version 1.1
 */
public class LayoutDataTag extends LayoutTagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(LayoutDataTag.class);

    public LayoutDataTag(Class layoutDataClass) {
        super(layoutDataClass);
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Either defines a variable or adds the current component to the parent
     */
    protected void processBean(String var, Object bean)
        throws JellyTagException {
        super.processBean(var, bean);

        Widget parent = getParentWidget();

        if (parent instanceof Control) {
            Control control = (Control) parent;
            control.setLayoutData(getBean());
        } else {
            throw new JellyTagException("This tag must be nested within a control widget tag");
        }
    }

    /**
     * @see org.apache.commons.jelly.tags.core.UseBeanTag#newInstance(java.lang.Class, java.util.Map, org.apache.commons.jelly.XMLOutput)
     */
    protected Object newInstance(
        Class theClass,
        Map attributes,
        XMLOutput output)
        throws JellyTagException {

        String text = (String) attributes.remove("style");
        if (text != null) {
            int style = SwtHelper.parseStyle(theClass, text);

            // now lets try invoke a constructor
            Class[] types = { int.class };

            try {
                Constructor constructor = theClass.getConstructor(types);
                if (constructor != null) {
                    Object[] values = { new Integer(style)};
                    return constructor.newInstance(values);
                }
            } catch (NoSuchMethodException e) {
                throw new JellyTagException(e);
            } catch (InstantiationException e) {
                throw new JellyTagException(e);
            } catch (IllegalAccessException e) {
                throw new JellyTagException(e);
            } catch (InvocationTargetException e) {
                throw new JellyTagException(e);
            }
        }
        return super.newInstance(theClass, attributes, output);
    }

    /**
     * @see org.apache.commons.jelly.tags.swt.LayoutTagSupport#convertValue(java.lang.Object, java.lang.String, java.lang.Object)
     */
    protected Object convertValue(Object bean, String name, Object value)
        throws JellyTagException {

        if (bean instanceof GridData) {
            if (name.endsWith("Alignment") && value instanceof String) {
                int style =
                    SwtHelper.parseStyle(bean.getClass(), (String) value);
                return new Integer(style);
            }
        }
        return super.convertValue(bean, name, value);
    }

}
