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

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.core.UseBeanTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates a Swing Action which on invocation will execute the body of this tag.
 * The Action is then output as a variable for reuse if the 'var' attribute is specified
 * otherwise the action is added to the parent JellySwing widget.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class ActionTag extends UseBeanTag {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(ActionTag.class);

    public ActionTag() {
    }


    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the Action object for this tag
     */
    public Action getAction() {
        return (Action) getBean();
    }


    // Implementation methods
    //-------------------------------------------------------------------------


    /**
     * An existing Action could be specified via the 'action' attribute or an action class
     * may be specified via the 'class' attribute, otherwise a default Action class is created.
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
     * An existing Action could be specified via the 'action' attribute or an action class
     * may be specified via the 'class' attribute, otherwise a default Action class is created.
     */
    protected Object newInstance(Class theClass, Map attributes, final XMLOutput output) throws JellyTagException {
        Action action = (Action) attributes.remove( "action" );
        if ( action == null ) {
            if (theClass != null ) {

                try {
                    return theClass.newInstance();
                } catch (InstantiationException e) {
                    throw new JellyTagException(e);
                } catch (IllegalAccessException e) {
                    throw new JellyTagException(e);
                }

            }
            else {
                action = new AbstractAction() {
                    public void actionPerformed(ActionEvent event) {
                        context.setVariable( "event", event );
                        try {
                            ActionTag.super.invokeBody(output);
                        }
                        catch (Exception e) {
                            log.error( "Caught: " + e, e );
                        }
                    }
                };
            }
        }
        return action;
    }
	
	public void invokeBody(XMLOutput output) {
		// do nothing
	}


    /**
     * Either defines a variable or adds the current component to the parent
     */
    protected void processBean(String var, Object bean) throws JellyTagException {
        if (var != null) {
            context.setVariable(var, bean);
        }
        else {
            ComponentTag tag = (ComponentTag) findAncestorWithClass( ComponentTag.class );
            if ( tag != null ) {
                tag.setAction((Action) bean);
            }
            else {
                throw new JellyTagException( "Either the 'var' attribute must be specified to export this Action or this tag must be nested within a JellySwing widget tag" );
            }
        }
    }


    /**
     * Perform the strange setting of Action properties using its custom API
     */
    protected void setBeanProperties(Object bean, Map attributes) throws JellyTagException {
        Action action = getAction();

        String enabled = "enabled";
        if (attributes.containsKey(enabled)) {
            try {
                BeanUtils.copyProperty(action, enabled, attributes.get(enabled));
            } catch (IllegalAccessException e) {
                throw new JellyTagException("Failed to set the enabled property.", e);
            } catch (InvocationTargetException e) {
                throw new JellyTagException("Failed to set the enabled property.", e);
            }

            attributes.remove(enabled);
        }

        for ( Iterator iter = attributes.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();

            // typically standard Action names start with upper case, so lets upper case it
            name = capitalize(name);
            Object value = entry.getValue();

            action.putValue( name, value );
        }
    }


    protected String capitalize(String text) {
        char ch = text.charAt(0);
        if ( Character.isUpperCase( ch ) ) {
            return text;
        }
        StringBuffer buffer = new StringBuffer(text.length());
        buffer.append( Character.toUpperCase( ch ) );
        buffer.append( text.substring(1) );
        return buffer.toString();
    }

}
