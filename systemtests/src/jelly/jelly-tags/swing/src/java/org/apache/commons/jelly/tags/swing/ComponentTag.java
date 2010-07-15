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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.RootPaneContainer;
import javax.swing.border.Border;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.core.UseBeanTag;
import org.apache.commons.jelly.tags.swing.converters.DebugGraphicsConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This tag creates a Swing component and adds it to its parent tag, optionally declaring this
 * component as a variable if the <i>var</i> attribute is specified.</p>
 *
 * <p> This tag clears the reference to it's bean after doTag runs.
 * This means that child tags can access the component (bean) normally
 * during execution but should not hold a reference to this
 * tag after their doTag completes.
 * </p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class ComponentTag extends UseBeanTag implements ContainerTag {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(ComponentTag.class);
    
    /** This is a converter that might normally be used through the 
     * BeanUtils product. However, it only applies to one Component
     * property and not to all ints, so it's not registered with BeanUtils.
     */
    private static final DebugGraphicsConverter debugGraphicsConverter = new DebugGraphicsConverter();
    
    /** the factory of widgets */
    private Factory factory;

    public ComponentTag() {
    }

    public ComponentTag(Factory factory) {
        this.factory = factory;
    }

    public String toString() {
		Component comp = getComponent();
        String componentName = (comp!=null) ? comp.getName() : null;
        if (comp!=null && (componentName == null || componentName.length() == 0))
            componentName = getComponent().toString();
        return "ComponentTag with bean " + componentName;
    }

    /**
     * Sets the Action of this component
     */
    public void setAction(Action action) throws JellyTagException {
        Component component = getComponent();
        if ( component != null ) {
            // lets just try set the 'action' property
            try {
                BeanUtils.setProperty( component, "action", action );
            } catch (IllegalAccessException e) {
                throw new JellyTagException(e);
            } catch (InvocationTargetException e) {
                throw new JellyTagException(e);
            }
        }
    }

    /**
     * Sets the Font of this component
     */
    public void setFont(Font font) throws JellyTagException {
        Component component = getComponent();
        if ( component != null ) {
            // lets just try set the 'font' property
            try {
                BeanUtils.setProperty( component, "font", font );
            }
            catch (IllegalAccessException e) {
                throw new JellyTagException(e);
            }
            catch (InvocationTargetException e) {
                throw new JellyTagException(e);
            }
        }
    }

    /**
     * Sets the Border of this component
     */
    public void setBorder(Border border) throws JellyTagException {
        Component component = getComponent();
        if ( component != null ) {
            try {
                // lets just try set the 'border' property
                BeanUtils.setProperty( component, "border", border );
            }
            catch (IllegalAccessException e) {
                throw new JellyTagException(e);
            }
            catch (InvocationTargetException e) {
                throw new JellyTagException(e);
            }
        }
    }

    /**
     * Sets the LayoutManager of this component
     */
    public void setLayout(LayoutManager layout) throws JellyTagException {
        Component component = getComponent();
        if ( component != null ) {
            if ( component instanceof RootPaneContainer ) {
                RootPaneContainer rpc = (RootPaneContainer) component;
                component = rpc.getContentPane();
            }

            try {
                // lets just try set the 'layout' property
                BeanUtils.setProperty( component, "layout", layout );
            }
            catch (IllegalAccessException e) {
                throw new JellyTagException(e);
            }
            catch (InvocationTargetException e) {
                throw new JellyTagException(e);
            }
        }
    }
	
    
	private String tagName = null;
	
	private XMLOutput currentOutput = null;
	
	/** Puts this tag into the context under the given name
	 * allowing later calls to {@link #rerun()}.
	 * For example, it makes sense to use ${myTag.rerun()} as a child
	 * of an <code>action</code> element.
	 *
	 * @param the name to be used
	 */
	public void setTagName(String name) {
		this.tagName = name;
	}
	
	/** Runs the body of this script again after clearing the content
	 * of this component.
	 * This is useful to use jelly-logic and "re-populate" a part of the user-interface
	 * after having updated a model part (e.g. an XML-document).
	 * @throws JellyTagException if anything
	 */
	public void rerun() throws JellyTagException {
		Component comp = getComponent();
		if(comp instanceof java.awt.Container) {
			((java.awt.Container) comp).removeAll();
		}
		this.doTag(currentOutput,false);
		if ( comp instanceof javax.swing.JComponent ) {
			((javax.swing.JComponent) comp).revalidate();
		}
	}
	

    /**
     * Adds a WindowListener to this component
     */
    public void addWindowListener(WindowListener listener) throws JellyTagException {
        Component component = getComponent();
        if ( component instanceof Window ) {
            Window window = (Window) component;
            window.addWindowListener(listener);
        }
    }

    /**
     * Adds a FocusListener to this component
     */
    public void addFocusListener(FocusListener listener) throws JellyTagException {
        Component component = getComponent();
        component.addFocusListener(listener);
    }

    /**
     * Adds a KeyListener to this component
     */
    public void addKeyListener(KeyListener listener) throws JellyTagException {
        Component component = getComponent();
        component.addKeyListener(listener);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the visible component, if there is one.
     */
    public Component getComponent() {
        Object bean = getBean();
        if ( bean instanceof Component ) {
            return (Component) bean;
        }
        return null;
    }


    // ContainerTag interface
    //-------------------------------------------------------------------------

    /**
     * Adds a child component to this parent
     */
    public void addChild(Component component, Object constraints) throws JellyTagException {
        Object parent = getBean();
        if ( parent instanceof JFrame && component instanceof JMenuBar ) {
            JFrame frame = (JFrame) parent;
            frame.setJMenuBar( (JMenuBar) component );
        }
        else if ( parent instanceof RootPaneContainer ) {
            RootPaneContainer rpc = (RootPaneContainer) parent;
            if (constraints != null) {
                rpc.getContentPane().add( component, constraints );
            }
            else {
                rpc.getContentPane().add( component);
            }
        }
        else if ( parent instanceof JScrollPane ) {
            JScrollPane scrollPane = (JScrollPane) parent;
            scrollPane.setViewportView( component );
        }
        else if ( parent instanceof JSplitPane) {
            JSplitPane splitPane = (JSplitPane) parent;
            if ( splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ) {
                if ( splitPane.getTopComponent() == null ) {
                    splitPane.setTopComponent( component );
                }
                else {
                    splitPane.setBottomComponent( component );
                }
            }
            else {
                if ( splitPane.getLeftComponent() == null ) {
                    splitPane.setLeftComponent( component );
                }
                else {
                    splitPane.setRightComponent( component );
                }
            }
        }
        else if ( parent instanceof JMenuBar && component instanceof JMenu ) {
            JMenuBar menuBar = (JMenuBar) parent;
            menuBar.add( (JMenu) component );
        }
        else if ( parent instanceof Container ) {
            Container container = (Container) parent;
            if (constraints != null) {
                container.add( component, constraints );
            }
            else {
                container.add( component );
            }
        }
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * A class may be specified otherwise the Factory will be used.
     */
    protected Class convertToClass(Object classObject) throws MissingAttributeException, ClassNotFoundException {
        if (classObject == null) {
            return null;
        }
        else {
            return super.convertToClass(classObject);
        }
    }

    /**
     * A class may be specified otherwise the Factory will be used.
     */
    protected Object newInstance(Class theClass, Map attributes, XMLOutput output) throws JellyTagException {
		if (attributes.containsKey("tagName")) {
			this.setTagName((String)attributes.get("tagName"));
			addIgnoreProperty("tagName");
		}
		if(tagName!=null) {
			context.setVariable(tagName,this);
			currentOutput = output;
		}
        try {
            if (theClass != null ) {
                return theClass.newInstance();
            } else {
                return factory.newInstance();
            }
        } catch (IllegalAccessException e) {
            throw new JellyTagException(e);
        } catch (InstantiationException e) {
            throw new JellyTagException(e);
        }
    }


    /**
     * Either defines a variable or adds the current component to the parent
     */
    protected void processBean(String var, Object bean) throws JellyTagException {
        if (var != null) {
            context.setVariable(var, bean);
        }
        Component component = getComponent();
        if ( component != null ) {
            ContainerTag parentTag = (ContainerTag) findAncestorWithClass( ContainerTag.class );
            if ( parentTag != null ) {
                parentTag.addChild(component, getConstraint());
            }
            else {
                if (var == null) {
                    throw new JellyTagException( "The 'var' attribute must be specified or this tag must be nested inside a JellySwing container tag like a widget or a layout" );
                }
            }
        }
    }

    /**
     * Handles wierd properties that don't quite match the Java Beans contract
     */
    protected void setBeanProperties(Object bean, Map attributes) throws JellyTagException {
            
            Component component = getComponent();
            if (component != null) {
                if (attributes.containsKey("location")) {
                    Object value = attributes.get("location");
                    Point p = null;
                    if (value instanceof Point) {
                        p = (Point) value;
                    }
                    else if (value != null) {
                        p =
                            (Point) ConvertUtils.convert(
                                value.toString(),
                                Point.class);
                    }
                    component.setLocation(p);
                    addIgnoreProperty("location");
                }

                if (attributes.containsKey("size")) {
                    Object value = attributes.get("size");
                    Dimension d = null;
                    if (value instanceof Dimension) {
                        d = (Dimension) value;
                    }
                    else if (value != null) {
                        d =
                            (Dimension) ConvertUtils.convert(
                                value.toString(),
                                Dimension.class);
                    }
                    component.setSize(d);
                    addIgnoreProperty("size");
                }
				
                
                if (attributes.containsKey("debugGraphicsOptions")) {
                    try {
                        Object o = debugGraphicsConverter.convert(attributes.get("debugGraphicsOptions"));
                        attributes.put("debugGraphicsOptions", o);
                    } catch (IllegalArgumentException e) {
                        throw new JellyTagException(e);
                    }
                }
                
                if (attributes.containsKey("debugGraphics")) {
                    try {
                        Object o = debugGraphicsConverter.convert(attributes.get("debugGraphics"));
                        attributes.put("debugGraphicsOptions", o);
                    } catch (IllegalArgumentException e) {
                        throw new JellyTagException(e);
                    }
                    
                    addIgnoreProperty("debugGraphics");
                }
                
             super.setBeanProperties(bean, attributes);
        }
    }

    protected Object getConstraint() {
        return null;
    }

    /**Overrides the default UseBean functionality to clear the bean after the
     * tag runs. This prevents us from keeping references to heavy Swing objects
     * around for longer than they are needed.
     * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
     */
    public void doTag(XMLOutput output) throws JellyTagException {
        this.doTag(output,true);
    }
    
    public void doTag(XMLOutput output, boolean resetBean) throws JellyTagException {
        if(resetBean) clearBean();
        super.doTag(output);
        //clearBean();
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
