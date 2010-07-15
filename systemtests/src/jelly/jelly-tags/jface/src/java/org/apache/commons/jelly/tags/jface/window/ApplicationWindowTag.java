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
package org.apache.commons.jelly.tags.jface.window;

import java.util.Map;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.core.UseBeanTag;
import org.apache.commons.jelly.tags.swt.converters.PointConverter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * This tag creates an JFace ApplicationWindow
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class ApplicationWindowTag extends UseBeanTag {

    private Shell parent;
    private int style = SWT.NULL;

    /**
     * @param widgetClass
     */
    public ApplicationWindowTag(Class tagClass) {
        super(tagClass);
    }

    /*
     * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
     */
    public void doTag(XMLOutput output)
        throws MissingAttributeException, JellyTagException {
        Map attributes = getAttributes();
        Object parent = attributes.remove("parent");
        if (parent != null) {
            if (parent instanceof Shell) {
                this.parent = (Shell) parent;
            } else {
                throw new JellyTagException(
                    "The parent attribute is not a Shell, it is of type: "
                        + parent.getClass().getName()
                        + " value: "
                        + parent);
            }
        }

        super.doTag(output);

        // set Title of aaplicationWindow
        Object title = attributes.remove("title");
        if (title != null) {
            getWindow().getShell().setText((String)title);
        }

        // set size of applicationWindow
        Object size = attributes.remove("size");
        if (size != null) {
            Point point = new PointConverter().parse((String) size);
            getWindow().getShell().setSize(point);
        }
    }

    /*
     * @see org.apache.commons.jelly.tags.core.UseBeanTag#newInstance(java.lang.Class, java.util.Map, org.apache.commons.jelly.XMLOutput)
     */
    protected Object newInstance(
        Class theClass,
        Map attributes,
        XMLOutput output)
        throws JellyTagException {

        return new ApplicationWindowImpl(parent);
    }


    /**
     * @return the visible window, if there is one.
     */
    public Window getWindow() {
        Object bean = getBean();
        if (bean instanceof Window) {
            return (Window) bean;
        }
        return null;
    }

}
