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

import java.io.InputStream;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.util.ClassLoaderUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

/**
 * This creates an image on the parent Widget.
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 * @version CVS ImageTag.java,v 1.5 2004/09/07 02:41:40 dion Exp
 */
public class ImageTag extends TagSupport {

    /** path to file */
    private String src;

    /** variable name, if specified */
    private String var;

    /** resource name, if specified */
    private String resource;

    /**
     * Sets the resource
     * @param resource image resource location
     */
    public void setResource(String resource) {
       this.resource = resource;
    }

    /**
     * Obtains the resource
     * @return the image resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * Sets the variable name
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Obtain the variable name.
     * @return String the variable name
     */
    public String getVar() {
      return this.var;
    }

    /**
     * Sets the src.
     * @param src The src to set
     */
    public void setSrc(String src) {
        this.src = src;
    }

    /**
     * Method getSrc.
     * @return String
     */
    public String getSrc() {
        return src;
    }

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

    // Tag interface
    //-------------------------------------------------------------------------

    /**
     * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
     */
    public void doTag(XMLOutput output) throws JellyTagException {
        // invoke by body just in case some nested tag configures me
        invokeBody(output);

        Widget parent = getParentWidget();

        if (parent == null) {
            throw new JellyTagException("This tag must be nested within a Widget or a Window");
        }

        Image image = null;

        if (getSrc() != null) {
           image = loadLocalImage(parent.getDisplay());
        } else if (getResource() != null) {
           image = loadResourceImage(parent.getDisplay());
        } else {
            throw new JellyTagException("Either an image location or a resource must be specified");
        }

        setWidgetImage(parent, image);

        // store the image as a context variable if specified
        if (var != null) {
            context.setVariable(var, image);
        }
    }

    /**
     * Creates an Image, loaded from the local disk
     */
    private Image loadLocalImage(Display display) {
        return new Image(display, getSrc());
    }

    /**
     * Creates an Image, loaded from a specified resource.
     */
    private Image loadResourceImage(Display display) {
        ClassLoader loader = ClassLoaderUtils.getClassLoader(null, getContext().getUseContextClassLoader(), getClass());
        InputStream stream = loader.getResourceAsStream(getResource());
        return new Image(display, stream);
    }

    /**
     * Add image to a widget
     * @param parent
     * @param image
     * @throws JellyTagException
     */
    protected void setWidgetImage(Widget parent, Image image) throws JellyTagException {
        if (parent instanceof Label) {
            Label label = (Label) parent;
            label.setImage(image);

        } else if (parent instanceof Button) {
            Button button = (Button) parent;
            button.setImage(image);

        } else if (parent instanceof Item) {
            Item item = (Item) parent;
            item.setImage(image);

        } else if (parent instanceof Decorations) {
            Decorations item = (Decorations) parent;
            item.setImage(image);

        } else {
            throw new JellyTagException("This tag must be nested inside a <label>, <button>, <shell> or <item> tag");
        }
    }
}
