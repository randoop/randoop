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
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.tags.jface.window.ApplicationWindowImpl;
import org.apache.commons.jelly.tags.jface.window.ApplicationWindowTag;
import org.apache.commons.jelly.tags.jface.wizard.WizardPageTag;
import org.apache.commons.jelly.tags.swt.WidgetTag;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

/**
 * Implementation of SWT WidgetTag
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class JFaceWidgetTag extends WidgetTag implements Tag {

    /**
     * @param widgetClass
     */
    public JFaceWidgetTag(Class widgetClass) {
        super(widgetClass);
    }

    /**
     * @param widgetClass
     * @param style
     */
    public JFaceWidgetTag(Class widgetClass, int style) {
        super(widgetClass, style);
    }

    /*
     * @see org.apache.commons.jelly.tags.swt.WidgetTag#attachWidgets(java.lang.Object, org.eclipse.swt.widgets.Widget)
     */
    protected void attachWidgets(Object parent, Widget widget) throws JellyTagException {
        super.attachWidgets(parent, widget);

        // set Parent composite of wizard page
        if (getParent() instanceof WizardPageTag) {
            WizardPageTag tag = (WizardPageTag) getParent();
            if (tag.getWizardPageImpl().getParentControl() == null) {
                if (widget instanceof Composite) {
                    tag.getWizardPageImpl().setParentComposite((Composite) widget);
                } else {
                    throw new JellyTagException("First child of a <wizardPage> must be of type Composite");
                }
            }
        }
    }

    /*
     * @see org.apache.commons.jelly.tags.swt.WidgetTag#getParentWidget()
     */
    public Widget getParentWidget() {
        parent = super.getParentWidget();

        if (parent == null && getParent() instanceof WizardPageTag) {
            WizardPageTag tag = (WizardPageTag) getParent();
            if (tag != null) {
                WizardPageTag.WizardPageImpl page = tag.getWizardPageImpl();
                return page.getControl();
            }
        }

        if (parent == null) {
            ApplicationWindowTag tag =
                (ApplicationWindowTag) findAncestorWithClass(ApplicationWindowTag.class);
            if (tag != null) {
                Window window = tag.getWindow();
                if (window != null && window instanceof ApplicationWindowImpl) {
                    return ((ApplicationWindowImpl) window).getContents();
                }
            }
        }

        return parent;
    }

}
