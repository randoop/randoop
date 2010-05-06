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
package org.apache.commons.jelly.tags.swing.impl;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a simple "bean-wrapper" for the {@link GridBagConstraints} class
 * which also tracks wether values are set allowing inheritance
 *    (using {@link setBasedOn}.
 *
 * @author <a href="mailto:paul@activemath.org">Paul Libbrecht</a>
 * @version $Revision: $
 */
public class GridBagConstraintBean extends GridBagConstraints {

    private boolean gridxSet = false;
    private boolean gridySet = false;
    private boolean gridwidthSet = false;
    private boolean gridheightSet = false;
    private boolean weightxSet = false;
    private boolean weightySet = false;
    private boolean ipadxSet = false;
    private boolean ipadySet = false;
    private boolean anchorSet = false;
    private boolean fillSet = false;

    /** Logging output */
    private static final Log LOG = LogFactory.getLog(GridBagConstraintBean.class);

    /** error message */
    private static final String ILLEGAL_ANCHOR_MSG = "Anchor must be one of  the GridBagLayout constants for the current Java version.";

    public GridBagConstraintBean() {
    }

    public int getGridx() {
        return gridx;
    }
    public void setGridx(int gridx) {
        this.gridx = gridx;
        this.gridxSet = true;
    }

    public int getGridy() {
        return gridy;
    }
    public void setGridy(int gridy) {
        this.gridy = gridy;
        this.gridySet = true;
    }

    public int getGridwidth() {
        return gridwidth;
    }
    public void setGridwidth(int gridwidth) {
        this.gridwidth = gridwidth;
        this.gridwidthSet = true;
    }

    public int getGridheight() {
        return gridheight;
    }
    public void setGridheight(int gridheight) {
        this.gridheight = gridheight;
        this.gridheightSet = true;
    }

    public double getWeightx() {
        return weightx;
    }
    public void setWeightx(double weightx) {
        this.weightx = weightx;
        this.weightxSet = true;
    }

    public double getWeighty() {
        return weighty;
    }
    public void setWeighty(double weighty) {
        this.weighty = weighty;
        this.weightySet = true;
    }

    public int getIpadx() {
        return ipadx;
    }
    public void setIpadx(int ipadx) {
        this.ipadx = ipadx;
        this.ipadxSet = true;
    }

    public int getIpady() {
        return ipady;
    }
    public void setIpady(int ipady) {
        this.ipady = ipady;
        this.ipadySet = true;
    }

    // TODO: provide better. insetstop, insetsbottom ??
    public Insets getInsets() {
        return insets;
    }
    public void setInsets(Insets insets) {
        this.insets = insets;
    }

    /** Returns the lower-case variant of the constant-name
        *    corresponding to the stored {@link #anchor} attribute.
        *
        *    @see    #anchor
        */
    public String getAnchor() {
        switch (this.anchor) {
            case CENTER :
                return "center";
            case NORTH :
                return "north";
            case NORTHEAST :
                return "northeast";
            case EAST :
                return "east";
            case SOUTHEAST :
                return "southeast";
            case SOUTH :
                return "south";
            case SOUTHWEST :
                return "southwest";
            case WEST :
                return "west";
            case NORTHWEST :
                return "northwest";
        }

        if (this.anchor == getByReflection("LINE_START"))
            return "line_start";
        else if (this.anchor == getByReflection("LINE_END"))
            return "line_end";
        else if (this.anchor == getByReflection("PAGE_START"))
            return "page_start";
        else if (this.anchor == getByReflection("PAGE_END"))
            return "page_end";
        else if (this.anchor == getByReflection("FIRST_LINE_START"))
            return "first_line_start";
        else if (this.anchor == getByReflection("FIRST_LINE_END"))
            return "first_line_end";
        else if (this.anchor == getByReflection("LAST_LINE_START"))
            return "last_line_start";
        else if (this.anchor ==  getByReflection("LAST_LINE_END"))
            return "last_line_end";

        throw new IllegalArgumentException(ILLEGAL_ANCHOR_MSG);
    }

    /** Accepts one of the strings with the same name as the constants
        * and sets the {@link #anchor} value accordingly.
        *    The accepted strings are case-insensitive.
        *
        *    @see #anchor
        */
    public void setAnchor(String anchorString) {
        String lcAnchorString = anchorString.toLowerCase();
        if (lcAnchorString.equals("center"))
            this.anchor = CENTER;
        else if (lcAnchorString.equals("north"))
            this.anchor = NORTH;
        else if (lcAnchorString.equals("northeast"))
            this.anchor = NORTHEAST;
        else if (lcAnchorString.equals("east"))
            this.anchor = EAST;
        else if (lcAnchorString.equals("southeast"))
            this.anchor = SOUTHEAST;
        else if (lcAnchorString.equals("south"))
            this.anchor = SOUTH;
        else if (lcAnchorString.equals("southwest"))
            this.anchor = SOUTHWEST;
        else if (lcAnchorString.equals("west"))
            this.anchor = WEST;
        else if (lcAnchorString.equals("northwest"))
            this.anchor = NORTHWEST;
        else if (lcAnchorString.equals("page_start"))
            this.anchor = getByReflection("PAGE_START");
        else if (lcAnchorString.equals("page_end"))
            this.anchor = getByReflection("PAGE_END");
        else if (lcAnchorString.equals("line_start"))
            this.anchor = getByReflection("LINE_START");
        else if (lcAnchorString.equals("line_end"))
            this.anchor = getByReflection("LINE_END");
        else if (lcAnchorString.equals("first_line_start"))
            this.anchor = getByReflection("FIRST_LINE_START");
        else if (lcAnchorString.equals("first_line_end"))
            this.anchor = getByReflection("FIRST_LINE_END");
        else if (lcAnchorString.equals("last_line_end"))
            this.anchor = getByReflection("LAST_LINE_END");
        else if (lcAnchorString.equals("last_line_start"))
            this.anchor = getByReflection("LAST_LINE_START");
        else
            throw new IllegalArgumentException("Anchor must be the name of one of  the GridBagLayoutConstants (case doesn't matter): center, north, northeast, east, southeast, south, southwest, west, or northwest.");
        this.anchorSet = true;
    }

    /** Returns the lower-case variant of the constant-name
        *    corresponding to the stored {@link #fill} attribute.
        *
        *    @see    #fill
        */
    public String getFill() {
        switch (fill) {
            case NONE :
                return "none";
            case HORIZONTAL :
                return "horizontal";
            case VERTICAL :
                return "vertical";
            case BOTH :
                return "both";
            default :
                throw new IllegalArgumentException("Fill must be the name of one of  the GridBagLayoutConstants: NONE, HORIZONTAL, VERTICAL, BOTH.");
        }
    }
    /** Accepts one of the strings with the same name as the constants
        * and sets the {@link #fill} value accordingly.
        *    The accepted strings are case-insensitive.
        *
        *    @see #fill
        */
    public void setFill(String fillString) {
        String lcFillString = fillString.toLowerCase();
        if (lcFillString.equals("none"))
            this.fill = NONE;
        else if (lcFillString.equals("horizontal"))
            this.fill = HORIZONTAL;
        else if (lcFillString.equals("vertical"))
            this.fill = VERTICAL;
        else if (lcFillString.equals("both"))
            this.fill = BOTH;
        else
            throw new IllegalArgumentException("Fill must be the name of one of  the GridBagLayoutConstants (case does not matter): NONE, HORIZONTAL, VERTICAL, BOTH.");
        this.fillSet = true;
    }

    /** Reads the values in the given grid-bag-constraint-bean that are set and sets
        * them in this object if they have not been set yet.
        */
    public void setBasedOn(GridBagConstraintBean from) {
        if (!gridxSet && from.gridxSet) {
            gridx = from.gridx;
            this.gridxSet = true;
        }
        if (!gridySet && from.gridySet) {
            gridy = from.gridy;
            this.gridySet = true;
        }
        if (!gridwidthSet && from.gridwidthSet) {
            gridwidth = from.gridwidth;
            this.gridwidthSet = true;
        }
        if (!gridheightSet && from.gridheightSet) {
            gridheight = from.gridheight;
            this.gridheightSet = true;
        }
        if (!weightxSet && from.weightxSet) {
            weightx = from.weightx;
            this.weightxSet = true;
        }
        if (!weightySet && from.weightySet) {
            weighty = from.weighty;
            this.weightySet = true;
        }
        if (!ipadxSet && from.ipadxSet) {
            ipadx = from.ipadx;
            this.ipadxSet = true;
        }
        if (!ipadySet && from.ipadySet) {
            ipady = from.ipady;
            this.ipadySet = true;
        }
        if (!fillSet && from.fillSet) {
            fill = from.fill;
            this.fillSet = true;
        }
        if (!anchorSet && from.anchorSet) {
            anchor = from.anchor;
            this.anchorSet = true;
        }
    }

    public String toString() {
        return "GridBagConstraintBean["
            + "gridx="
            + gridx
            + ", gridy="
            + gridy
            + ", gridwidth="
            + gridwidth
            + ", gridheight="
            + gridheight
            + ", weightx="
            + weightx
            + ", weighty="
            + weighty
            + ", ipadx="
            + ipadx
            + ", ipady="
            + ipady
            + ", anchor="
            + getAnchor()
            + ", fill="
            + getFill()
            + ", insets="
            + insets
            + "]";
    }

    private int getByReflection(String field) {
        try {
            Field f = getClass().getField(field);
            Integer rv = (Integer) f.get(this);
            return rv.intValue();
        } catch (SecurityException e) {
            LOG.debug(e);
            throw new IllegalArgumentException(ILLEGAL_ANCHOR_MSG);
        } catch (NoSuchFieldException e) {
            LOG.debug(e);
            throw new IllegalArgumentException(ILLEGAL_ANCHOR_MSG);
        } catch (IllegalArgumentException e) {
            LOG.debug(e);
            throw new IllegalArgumentException(ILLEGAL_ANCHOR_MSG);
        } catch (IllegalAccessException e) {
            LOG.debug(e);
            throw new IllegalArgumentException(ILLEGAL_ANCHOR_MSG);
        }
    }

} // class GridBagConstraintsBean
