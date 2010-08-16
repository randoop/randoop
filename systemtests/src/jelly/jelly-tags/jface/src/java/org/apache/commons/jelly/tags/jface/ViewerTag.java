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
package org.apache.commons.jelly.tags.jface;

import java.util.Map;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.swt.WidgetTag;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

/**
 * This tag creates an JFace Viewer
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class ViewerTag extends WidgetTag {

    private Composite parent;
    private int style = SWT.NULL;

    /**
     * @param widgetClass
     */
    public ViewerTag(Class tagClass) {
        super(tagClass);
    }

    /**
     * @param widgetClass
     * @param style
     */
    public ViewerTag(Class tagClass, int style) {
        super(tagClass);
        this.style = style;
    }

    /*
     * @see org.apache.commons.jelly.tags.core.UseBeanTag#newInstance(java.lang.Class, java.util.Map, org.apache.commons.jelly.XMLOutput)
     */
    protected Object newInstance(
        Class theClass,
        Map attributes,
        XMLOutput output)
        throws JellyTagException {

        int style = getStyle(attributes);

        // now lets call the constructor with the parent
        Widget parent = getParentWidget();
        Viewer viewer = (Viewer) createWidget(theClass, parent, style);

        return viewer;
    }

    /**
     * @return the visible viewer, if there is one.
     */
    public Viewer getViewer() {
        Object bean = getBean();
        if (bean instanceof Viewer) {
            return (Viewer) bean;
        }
        return null;
    }

}
