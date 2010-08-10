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

import java.util.Map;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * This tag creates an SWT dialog.
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 *
 */
public class DialogTag extends WidgetTag {

    /**
     * @param widgetClass
     * @param style
     */
    public DialogTag(Class widgetClass, int style) {
        super(widgetClass, style);
    }

    /**
     * @param widgetClass
     */
    public DialogTag(Class widgetClass) {
        super(widgetClass);
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new dialog
     */
    protected Object newInstance(Class theClass, Map attributes, XMLOutput output)
        throws JellyTagException {
        int style = getStyle(attributes);

        // now lets call the constructor with the parent
        Widget parent = getParentWidget();

        boolean isParentShell = parent instanceof Shell;
        if (parent == null || !isParentShell) {
            throw new JellyTagException("This tag must be nested within a Shell");
        }

        Dialog dialog = (Dialog) createWidget(theClass, parent, style);

        return dialog;
    }

}