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

import java.awt.Component;
import java.awt.GridBagConstraints;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a tabular cell inside a &lt;tl&gt; tag inside a &lt;tableLayout&gt;
 * tag which mimicks the &lt;td&gt; HTML tag.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class TdTag extends TagSupport implements ContainerTag {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(TdTag.class);

    private String align;
    private String valign;
    private int colspan = 1;
    private int rowspan = 1;
    private boolean colfill = false;
    private boolean rowfill = false;

    public TdTag() {
    }

    // ContainerTag interface
    //-------------------------------------------------------------------------

    /**
     * Adds a child component to this parent
     */
    public void addChild(Component component, Object constraints) throws JellyTagException {
        // add my child component to the layout manager
        TrTag tag = (TrTag) findAncestorWithClass( TrTag.class );
        if (tag == null) {
            throw new JellyTagException( "this tag must be nested within a <tr> tag" );
        }
        tag.addCell(component, createConstraints());
    }


    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws JellyTagException {
        invokeBody(output);
    }


    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the horizontal alignment to a case insensitive value of {LEFT, CENTER, RIGHT}
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * Sets the vertical alignment to a case insensitive value of {TOP, MIDDLE, BOTTOM}
     */
    public void setValign(String valign) {
        this.valign = valign;
    }


    /**
     * Sets the number of columns that this cell should span. The default value is 1
     */
    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    /**
     * Sets the number of rows that this cell should span. The default value is 1
     */
    public void setRowspan(int rowspan) {
        this.rowspan = rowspan;
    }

    /**
     * Returns the colfill.
     * @return boolean
     */
    public boolean isColfill() {
        return colfill;
    }

    /**
     * Returns the rowfill.
     * @return boolean
     */
    public boolean isRowfill() {
        return rowfill;
    }

    /**
     * Sets whether or not this column should allow its component to stretch to fill the space available
     */
    public void setColfill(boolean colfill) {
        this.colfill = colfill;
    }

    /**
     * Sets whether or not this row should allow its component to stretch to fill the space available
     */
    public void setRowfill(boolean rowfill) {
        this.rowfill = rowfill;
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new constraints object
     */
    protected GridBagConstraints createConstraints() {
        GridBagConstraints answer = new GridBagConstraints();
        answer.anchor = getAnchor();
        if (colspan < 1) {
            colspan = 1;
        }
        if (rowspan < 1) {
            rowspan = 1;
        }
        if (isColfill())  {
            answer.fill = isRowfill()
                ? GridBagConstraints.BOTH
                : GridBagConstraints.HORIZONTAL;
        }
        else {
            answer.fill = isRowfill()
                ? GridBagConstraints.VERTICAL
                : GridBagConstraints.NONE;
        }
        answer.weightx = 0.2;
        answer.weighty = 0;
        answer.gridwidth = colspan;
        answer.gridheight = rowspan;
        return answer;
    }

    /**
     * @return the GridBagConstraints enumeration for achor
     */
    protected int getAnchor() {
        boolean isTop = "top".equalsIgnoreCase(valign);
        boolean isBottom = "bottom".equalsIgnoreCase(valign);

        if ("center".equalsIgnoreCase(align)) {
            if (isTop) {
                return GridBagConstraints.NORTH;
            }
            else if (isBottom) {
                return GridBagConstraints.SOUTH;
            }
            else {
                return GridBagConstraints.CENTER;
            }
        }
        else if ("right".equalsIgnoreCase(align)) {
            if (isTop) {
                return GridBagConstraints.NORTHEAST;
            }
            else if (isBottom) {
                return GridBagConstraints.SOUTHEAST;
            }
            else {
                return GridBagConstraints.EAST;
            }
        }
        else {
            // defaults to left
            if (isTop) {
                return GridBagConstraints.NORTHWEST;
            }
            else if (isBottom) {
                return GridBagConstraints.SOUTHWEST;
            }
            else {
                return GridBagConstraints.WEST;
            }
        }
    }
}
