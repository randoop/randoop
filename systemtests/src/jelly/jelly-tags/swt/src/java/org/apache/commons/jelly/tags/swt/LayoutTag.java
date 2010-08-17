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

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Widget;

/**
 * Creates a new Layout implementations and adds it to the parent Widget.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version 1.1
 */
public class LayoutTag extends LayoutTagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(LayoutTag.class);

    public LayoutTag(Class layoutClass) {
        super(layoutClass);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the Layout if there is one otherwise null
     */
    public Layout getLayout() {
        Object bean = getBean();
        if (bean instanceof Layout) {
            return (Layout) bean;
        }
        return null;
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

        if (parent instanceof Composite) {
            Composite composite = (Composite) parent;
            composite.setLayout(getLayout());

        } else {
            throw new JellyTagException("This tag must be nested within a composite widget tag");
        }
    }

    /**
     * @see org.apache.commons.jelly.tags.swt.LayoutTagSupport#convertValue(java.lang.Object, java.lang.String, java.lang.Object)
     */
    protected Object convertValue(Object bean, String name, Object value)
        throws JellyTagException {

        if (bean instanceof FillLayout
            && name.equals("type")
            && value instanceof String) {
            int style = SwtHelper.parseStyle(SWT.class, (String) value);
            return new Integer(style);
        }
        return super.convertValue(bean, name, value);
    }

}
