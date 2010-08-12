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
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.swt.converters.ColorConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Widget;

/**
 * Class to create a {@link Font} instance within Jelly SWT.
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @author Written with much help thanks to the ImageTag class
 * @version CVS $Id: FontTag.java,v 1.2 2004/09/09 12:20:26 dion Exp $
 */
public class FontTag extends TagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(FontTag.class);

    /** Font type */
    private String type;

    /** Font size */
    private int size;

    /** Font style */
    private String style;

    /** Font variable name */
    private String var;

    /**
     * Set the type of this {@link Font}
     *
     * @param type {@link Font} type name
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Obtain the {@link Font} type name
     *
     * @return the {@link Font} type name
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the size of this {@link Font}
     *
     * @param size {@link Font} size
     */
    public void setSize(final int size) {
        this.size = size;
    }

    /**
     * Obtain the {@link Font} size
     *
     * @return the {@link Font} size
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Set the style of this {@link Font} (eg. bold, normal, italics)
     *
     * @param style the style of this {@link Font}
     */
    public void setStyle(final String style) {
        this.style = style;
    }

    /**
     * Obtain the style of this {@link Font}
     *
     * @return the style of this {@link Font}
     */
    public String getStyle() {
        return this.style;
    }

    /**
     * Sets the variable name
     *
     * @param var the variable name of this {@link Font} instance
     */
    public void setVar(final String var) {
        this.var = var;
    }

    /**
     * Obtain the variable name.
     *
     * @return the variable name of this {@link Font} instance
     */
    public String getVar() {
        return this.var;
    }

    /**
     * @return the parent widget which will deliver us a {@link Device} reference
     */
    public Widget getParentWidget() {
        final WidgetTag tag = (WidgetTag) findAncestorWithClass(WidgetTag.class);
        if (tag != null) {
            return tag.getWidget();
        }
        return null;
    }

    // Tag interface
    //-------------------------------------------------------------------------

    /**
     * Creates a {@link Font} instance as defined by the type, size and style
     * attributes, and stores this {@link Font} instance in the Context so that
     * it can be referenced in the Jelly script.
     *
     * @param output {@link XMLOutput} reference
     * @throws JellyTagException if an error occurs
     * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
     */
    public void doTag(final XMLOutput output) throws JellyTagException {
        // invoke by body just in case some nested tag configures me
        invokeBody(output);

        final Widget parent = getParentWidget();

        if (parent == null) {
            throw new JellyTagException(
                "This tag must be nested within a Widget or a Window"
            );
        }

        if (var == null) {
            throw new JellyTagException("This tag requires a context variable name");
        }

        if (type == null) {
            throw new JellyTagException("This tag requires a font type name");
        }

        if (size <= 0) {
            throw new JellyTagException("This tag requires a font size greater than 0");
        }

        if (style == null) {
            if (log.isDebugEnabled()) {
                log.debug("No style set on font " + type + ", defaulting to normal");
            }
        }

        final Font font =
            new Font(
                parent.getDisplay(),
                type,
                size,
                style == null ? SWT.NORMAL : SwtHelper.parseStyle(SWT.class, style)
            );

        // store the Color in the context
        context.setVariable(var, font);
    }
}
