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

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates a titled border.
 * The border will either be exported as a variable defined by the 'var' attribute
 * or will be set on the parent widget's border property
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class TitledBorderTag extends BorderTagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(TitledBorderTag.class);

    private String title;
    private String titleJustification;
    private String titlePosition;
    private Border border;
    private Font font;
    private Color color;


    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws MissingAttributeException, JellyTagException {
        if ( title == null) {
            throw new MissingAttributeException("title");
        }
        super.doTag(output);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the color of the title for this border. Can be set via a nested <color> tag.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Sets the Font to be used by the title. Can be set via a nested <font> tag.
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Sets the title text for this border.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the justification of the title. The String is case insensitive.
     * Possible values are {LEFT, CENTER, RIGHT, LEADING, TRAILING}
     */
    public void setTitleJustification(String titleJustification) {
        this.titleJustification = titleJustification;
    }

    /**
     * Sets the position of the title. The String is case insensitive.
     * Possible values are {ABOVE_TOP, TOP, BELOW_TOP, ABOVE_BOTTOM, BOTTOM, BELOW_BOTTOM}
     */
    public void setTitlePosition(String titlePosition) {
        this.titlePosition = titlePosition;
    }



    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new Border instance.
     */
    protected Border createBorder() {
        if (border != null) {
            if (titleJustification != null && titlePosition != null) {
                int justification = asTitleJustification(titleJustification);
                int position = asTitlePosition(titlePosition);

                if (font != null) {
                    if (color != null) {
                        return BorderFactory.createTitledBorder(border, title, justification, position, font, color);
                    }
                    else {
                        return BorderFactory.createTitledBorder(border, title, justification, position, font);
                    }
                }
                return BorderFactory.createTitledBorder(border, title, justification, position);
            }
            return BorderFactory.createTitledBorder(border, title);
        }
        return BorderFactory.createTitledBorder(title);
    }

    /**
     * @return the enumeration for the title justification
     */
    protected int asTitleJustification(String text) {
        if (text.equalsIgnoreCase("LEFT")) {
            return TitledBorder.LEFT;
        }
        else if (text.equalsIgnoreCase("CENTER")) {
            return TitledBorder.CENTER;
        }
        else if (text.equalsIgnoreCase("RIGHT")) {
            return TitledBorder.RIGHT;
        }
        else if (text.equalsIgnoreCase("LEADING")) {
            return TitledBorder.LEADING;
        }
        else if (text.equalsIgnoreCase("TRAILING")) {
            return TitledBorder.TRAILING;
        }
        else {
            return TitledBorder.DEFAULT_JUSTIFICATION;
        }
    }

    /**
     * @return the enumeration for the title position
     */
    protected int asTitlePosition(String text) {
        if (text.equalsIgnoreCase("ABOVE_TOP")) {
            return TitledBorder.ABOVE_TOP;
        }
        else if (text.equalsIgnoreCase("TOP")) {
            return TitledBorder.TOP;
        }
        else if (text.equalsIgnoreCase("BELOW_TOP")) {
            return TitledBorder.BELOW_TOP;
        }
        else if (text.equalsIgnoreCase("ABOVE_BOTTOM")) {
            return TitledBorder.ABOVE_BOTTOM;
        }
        else if (text.equalsIgnoreCase("BOTTOM")) {
            return TitledBorder.BOTTOM;
        }
        else if (text.equalsIgnoreCase("BELOW_BOTTOM")) {
            return TitledBorder.BELOW_BOTTOM;
        }
        else {
            return TitledBorder.DEFAULT_POSITION;
        }
    }
}
