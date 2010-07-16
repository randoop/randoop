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

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates an empty border.
 * The border will either be exported as a variable defined by the 'var' attribute
 * or will be set on the parent widget's border property
 *
 * @author <a href="mailto:robert@bull-enterprises.com">Robert McIntosh</a>
 * @version $Revision: 1.3 $
 */
public class EmptyBorderTag extends BorderTagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(EmptyBorderTag.class);

    private int left   = -1;
    private int right  = -1;
    private int top    = -1;
    private int bottom = -1;

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws MissingAttributeException, JellyTagException {
        if ( left == -1) {
            throw new MissingAttributeException("left");
        }
        if ( right == -1) {
            throw new MissingAttributeException("right");
        }
        if ( top == -1) {
            throw new MissingAttributeException("top");
        }
        if ( bottom == -1) {
            throw new MissingAttributeException("bottom");
        }
        super.doTag(output);
    }

    // Properties
    //-------------------------------------------------------------------------
    /**
     * Sets the left inset
     * @param left
     */
    public void setLeft( int left ) {
        this.left = left;
    }

    /**
     * Sets the right inset
     * @param right
     */
    public void setRight( int right ) {
        this.right = right;
    }

    /**
     * Sets the top inset
     * @param top
     */
    public void setTop( int top ) {
        this.top = top;
    }

    /**
     * Sets the bottom inset
     * @param bottom
     */
    public void setBottom( int bottom ) {
        this.bottom = bottom;
    }

    /**
     * Factory method to create a new EmptyBorder instance.
     */
    protected Border createBorder() {
        return BorderFactory.createEmptyBorder( top, left, bottom, right);
    }

}
