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
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.core.UseBeanTag;
import org.apache.commons.jelly.tags.jface.window.ApplicationWindowTag;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;

/**
 * This tag creates an JFace MenuManager
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class MenuManagerTag extends UseBeanTag {

    private String text;
    private MenuManager mm;

    /**
     * @return the parent window which this widget will be added to.
     */
    public Window getParentWindow() {

        ApplicationWindowTag tag =
            (ApplicationWindowTag) findAncestorWithClass(ApplicationWindowTag
                .class);
        if (tag != null) {
            return tag.getWindow();
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
     */
    public void doTag(XMLOutput output)
        throws MissingAttributeException, JellyTagException {

        Map attributes = getAttributes();
        text = attributes.remove("text").toString();

        if (text == null)
            throw new MissingAttributeException("text attribute is missing");

        mm = new MenuManager(text);

        ApplicationWindow window = (ApplicationWindow) getParentWindow();
        if (window != null) {
            window.getMenuBarManager().add(mm);
        }

        // invoke by body just in case some nested tag configures me
        invokeBody(output);
    }

    /**
     * @return MenuManager
     */
    public MenuManager getMenuManager() {
        return mm;
    }

}
