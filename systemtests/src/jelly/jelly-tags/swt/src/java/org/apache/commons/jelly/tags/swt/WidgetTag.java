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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.core.UseBeanTag;
import org.apache.commons.jelly.tags.swt.converters.ColorConverter;
import org.apache.commons.jelly.tags.swt.converters.PointConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * This tag creates an SWT widget based on the parent tag, optionally declaring
 * this widget as a variable if the <i>var</i> attribute is specified.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 * @version 1.1
 */
public class WidgetTag extends UseBeanTag {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(WidgetTag.class);

    protected Widget parent;
    private int style = SWT.NULL;

    public WidgetTag(Class widgetClass) {
        super(widgetClass);
    }

    public WidgetTag(Class widgetClass, int style) {
        super(widgetClass);
        this.style = style;
    }

    public String toString() {
        return "WidgetTag[widget=" + getWidget() + "]";
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the visible widget, if there is one.
     */
    public Widget getWidget() {
        Object bean = getBean();
        if (bean instanceof Widget) {
            return (Widget) bean;
        }
        return null;
    }

    /**
     * @return the parent widget which this widget will be added to.
     */
    public Widget getParentWidget() {

        if (parent == null) {
            WidgetTag tag = (WidgetTag) findAncestorWithClass(WidgetTag.class);
            if (tag != null) {
                return tag.getWidget();
            }
        }

        return parent;
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        Map attributes = getAttributes();
        Object parent = attributes.remove("parent");
        if (parent != null) {
            if (parent instanceof Widget) {
                this.parent = (Widget) parent;
            } else {
                throw new JellyTagException(
                    "The parent attribute is not a Widget, it is of type: "
                        + parent.getClass().getName()
                        + " value: "
                        + parent);
            }
        }
        super.doTag(output);
        clearBean();
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new widget
     */
    protected Object newInstance(Class theClass, Map attributes, XMLOutput output)
        throws JellyTagException {
        int style = getStyle(attributes);

        // now lets call the constructor with the parent
        Widget parent = getParentWidget();

        Widget widget = (Widget) createWidget(theClass, parent, style);
        if (parent != null) {
            attachWidgets(parent, widget);
        }

        return widget;
    }

    /*
     * @see org.apache.commons.jelly.tags.core.UseBeanTag#setBeanProperties(java.lang.Object, java.util.Map)
     */
    protected void setBeanProperties(Object bean, Map attributes) throws JellyTagException {

        if (bean instanceof Control) {
            Control control = (Control) bean;

            // Special handling of size property as the Control object breaks the
            // JavaBean naming conventions by overloading the setSize() method
            Object size = attributes.remove("size");
            setSize(control, size);

            // Special handling of color property as the Control object breaks the
            // JavaBean naming conventions by overloading the setBackground() or setForeground() method
            Object colorValue = attributes.remove("background");
            Color background =
                (colorValue instanceof Color)
                    ? (Color) colorValue : getColor(control, colorValue);
            control.setBackground(background);

            colorValue = attributes.remove("foreground");
            Color foreground =
                (colorValue instanceof Color)
                    ? (Color) colorValue : getColor(control, colorValue);
            control.setForeground(foreground);
        }

        super.setBeanProperties(bean, attributes);
    }

    /**
     * Get a color for the control
     * @param control
     * @param colorValue
     */
    protected Color getColor(Control control, Object colorValue) {
        Color color = null;
        if (colorValue != null) {
            RGB rgb = null;
            if (color instanceof Color) {
                color = (Color) colorValue;
            } else {
                rgb = ColorConverter.getInstance().parse(colorValue.toString());
                color = new Color(control.getDisplay(), rgb);
            }
        }
        return color;
    }

    /**
     * set the size of the control
     * @param control
     * @param size
     */
    protected void setSize(Control control, Object size) {
        Point point = null;
        if (size != null) {
            if (size instanceof Point) {
                point = (Point) size;
            } else {
                point = PointConverter.getInstance().parse(size.toString());
            }
            control.setSize(point);
        }

    }

    /**
     * Provides a strategy method to allow a new child widget to be attached to
     * its parent
     *
     * @param parent is the parent widget which is never null
     * @param widget is the new child widget to be attached to the parent
     */
    protected void attachWidgets(Object parent, Widget widget) throws JellyTagException {
        // set the content that will be scrolled if the parent is a ScrolledComposite
        if (parent instanceof ScrolledComposite && widget instanceof Control) {
            ScrolledComposite scrolledComposite = (ScrolledComposite) parent;
            scrolledComposite.setContent((Control) widget);
        }
    }

    /**
     * Factory method to create an instance of the given Widget class with
     * the given parent and SWT style
     *
     * @param theClass is the type of widget to create
     * @param parent is the parent widget
     * @param style the SWT style code
     * @return the new Widget
     */
    protected Object createWidget(Class theClass, Widget parent, int style)
        throws JellyTagException {
        if (theClass == null) {
            throw new JellyTagException("No Class available to create the new widget");
        }

        try {
            if (parent == null) {
                // lets try call a constructor with a single style
                Class[] types = { int.class };
                Constructor constructor = theClass.getConstructor(types);
                if (constructor != null) {
                    Object[] arguments = { new Integer(style)};
                    return constructor.newInstance(arguments);
                }
            } else {
                // lets try to find the constructor with 2 arguments with the 2nd argument being an int
                Constructor[] constructors = theClass.getConstructors();
                if (constructors != null) {
                    for (int i = 0, size = constructors.length; i < size; i++) {
                        Constructor constructor = constructors[i];
                        Class[] types = constructor.getParameterTypes();
                        if (types.length == 2 && types[1].isAssignableFrom(int.class)) {
                            if (types[0].isAssignableFrom(parent.getClass())) {
                                Object[] arguments = { parent, new Integer(style)};
                                return constructor.newInstance(arguments);
                            }
                        }
                    }
                }
            }
            return theClass.newInstance();
        } catch (NoSuchMethodException e) {
            throw new JellyTagException(e);
        } catch (InstantiationException e) {
            throw new JellyTagException(e);
        } catch (IllegalAccessException e) {
            throw new JellyTagException(e);
        } catch (InvocationTargetException e) {
            throw new JellyTagException(e);
        }
    }

    /**
     * Creates the SWT style code for the current attributes
     * @return the SWT style code
     */
    protected int getStyle(Map attributes) throws JellyTagException {
        String text = (String) attributes.remove("style");
        if (text != null) {
            return SwtHelper.parseStyle(SWT.class, text);
        }
        return style;
    }
    
    /** Sets the bean to null, to prevent it from
     * sticking around in the event that this tag instance is
     * cached. This method is called at the end of doTag.
     *
     */
    protected void clearBean() {
        setBean(null);
    }
}
