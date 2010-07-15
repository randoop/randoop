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
package org.apache.commons.jelly.tags.swing;

import java.awt.BorderLayout;
import java.awt.Component;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Represents a layout of a child component within its parent &lt;borderLayout&gt; layout.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class BorderAlignTag extends TagSupport implements ContainerTag {

    private String align;

    // ContainerTag interface
    //-------------------------------------------------------------------------

    /**
     * Adds a child component to this parent
     */
    public void addChild(Component component, Object constraints) throws JellyTagException {
        BorderLayoutTag tag = (BorderLayoutTag) findAncestorWithClass( BorderLayoutTag.class );
        if (tag == null) {
            throw new JellyTagException( "this tag must be nested within a <borderLayout> tag" );
        }
        tag.addLayoutComponent(component, getConstraints());
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws JellyTagException {
        invokeBody(output);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Returns the align.
     * @return String
     */
    public String getAlign() {
        return align;
    }

    /**
     * Sets the alignment of the child component which is a case insensitive value
     * of {NORTH, SOUTH, EAST, WEST, CENTER} which defaults to CENTER
     */
    public void setAlign(String align) {
        this.align = align;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    protected Object getConstraints() {
        if ("north".equalsIgnoreCase(align)) {
            return BorderLayout.NORTH;
        }
        else if ("south".equalsIgnoreCase(align)) {
            return BorderLayout.SOUTH;
        }
        else if ("east".equalsIgnoreCase(align)) {
            return BorderLayout.EAST;
        }
        else if ("west".equalsIgnoreCase(align)) {
            return BorderLayout.WEST;
        }
        else {
            // default to CENTER
            return BorderLayout.CENTER;
        }
    }
}

