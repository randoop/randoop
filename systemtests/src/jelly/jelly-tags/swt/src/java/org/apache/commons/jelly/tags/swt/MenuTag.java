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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

/**
 * This tag creates an SWT Menu
 * </p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version 1.1
 */
public class MenuTag extends WidgetTag {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(MenuTag.class);

    public MenuTag() {
        super(Menu.class);
    }

    public MenuTag(int style) {
        super(Menu.class, style);
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Provides a strategy method to allow a new child widget to be attached to
     * its parent
     *
     * @param parent is the parent widget which is never null
     * @param widget is the new child widget to be attached to the parent
     */
    protected void attachWidgets(Object parent, Widget widget) {
        Menu menu = (Menu) widget;
        if (parent instanceof Decorations) {
            Decorations shell = (Decorations) parent;
            shell.setMenuBar(menu);
        }
        else if (parent instanceof Control) {
            Control control = (Control) parent;
            control.setMenu(menu);
        }
        else if (parent instanceof MenuItem) {
            MenuItem menuItem = (MenuItem) parent;
            menuItem.setMenu(menu);
        }
    }

    /**
     * @see org.apache.commons.jelly.tags.swt.WidgetTag#createWidget(java.lang.Class, org.eclipse.swt.widgets.Widget, int)
     */
    protected Object createWidget(Class theClass, Widget parent, int style)
        throws JellyTagException {

        if (parent instanceof Decorations) {
            return super.createWidget(theClass, parent, style);
        }
        else {
            if (parent instanceof Menu) {
                return new Menu((Menu) parent);
            }
            else if (parent instanceof MenuItem) {
                return new Menu((MenuItem) parent);
            }
            else if (parent instanceof Control) {
                return new Menu((Control) parent);
            }
            else {
                throw new JellyTagException("This tag must be nested inside a <shell>, <menu>, <menuItem> or control tag");
            }
        }
    }

}
