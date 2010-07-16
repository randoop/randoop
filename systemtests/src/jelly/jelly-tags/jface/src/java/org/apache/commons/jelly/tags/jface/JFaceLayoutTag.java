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

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.tags.jface.window.ApplicationWindowImpl;
import org.apache.commons.jelly.tags.jface.window.ApplicationWindowTag;
import org.apache.commons.jelly.tags.swt.LayoutTag;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

/**
 * Implementation of SWT LayoutTag
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class JFaceLayoutTag extends LayoutTag {

    /**
     * @param layoutClass
     */
    public JFaceLayoutTag(Class layoutClass) {
        super(layoutClass);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jelly.tags.core.UseBeanTag#processBean(java.lang.String, java.lang.Object)
     */
    protected void processBean(String var, Object bean) throws JellyTagException {

        Widget parent = getParentWidget();
        if (parent == null) { // perhaps parent is a Window
            Window window = getParentWindow();
            if (window != null && window instanceof ApplicationWindowImpl) {
                parent = ((ApplicationWindowImpl) window).getContents();
            }
        }

        if (parent instanceof Composite) {
            Composite composite = (Composite) parent;
            composite.setLayout(getLayout());

        } else {
            throw new JellyTagException("This tag must be nested within a composite widget tag");
        }

    }

    /**
     * @return the parent window
     */
    public Window getParentWindow() {
        ApplicationWindowTag tag =
            (ApplicationWindowTag) findAncestorWithClass(ApplicationWindowTag.class);
        if (tag != null) {
            return tag.getWindow();
        }
        return null;
    }
}


