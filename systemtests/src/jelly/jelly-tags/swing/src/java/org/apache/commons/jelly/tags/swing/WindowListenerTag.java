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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates a WindowListener which is attached to its parent window control which will invoke
 * named Jelly scripts as window events are fired, or will invoke its body if there is no script
 * specified for the named event type.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class WindowListenerTag extends TagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(WindowListenerTag.class);

    private String var;
    private Script activated;
    private Script closed;
    private Script closing;
    private Script deactivated;
    private Script deiconified;
    private Script iconified;
    private Script opened;

    public WindowListenerTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws JellyTagException {

        // now lets add this action to its parent if we have one
        ComponentTag tag = (ComponentTag) findAncestorWithClass( ComponentTag.class );
        if ( tag != null ) {
            WindowListener listener = new WindowListener() {
                public void windowActivated(WindowEvent e) {
                    invokeScript( output, e, activated );
                }

                public void windowClosed(WindowEvent e) {
                    invokeScript( output, e, closed );
                }

                public void windowClosing(WindowEvent e) {
                    invokeScript( output, e, closing );
                }

                public void windowDeactivated(WindowEvent e) {
                    invokeScript( output, e, deactivated );
                }

                public void windowDeiconified(WindowEvent e) {
                    invokeScript( output, e, deiconified );
                }

                public void windowIconified(WindowEvent e) {
                    invokeScript( output, e, iconified );
                }

                public void windowOpened(WindowEvent e) {
                    invokeScript( output, e, opened );
                }
            };
            tag.addWindowListener(listener);
        }
    }

    // Properties
    //-------------------------------------------------------------------------


    /**
     * Sets the name of the variable to use to expose the Event object
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Sets the Script to be executed when the window is activated.
     */
    public void setActivated(Script activated) {
        this.activated = activated;
    }

    /**
     * Sets the Script to be executed when the window is closed.
     */
    public void setClosed(Script closed) {
        this.closed = closed;
    }

    /**
     * Sets the Script to be executed when the window is closing.
     */
    public void setClosing(Script closing) {
        this.closing = closing;
    }

    /**
     * Sets the Script to be executed when the window is deactivated.
     */
    public void setDeactivated(Script deactivated) {
        this.deactivated = deactivated;
    }

    /**
     * Sets the Script to be executed when the window is deiconified.
     */
    public void setDeiconified(Script deiconified) {
        this.deiconified = deiconified;
    }

    /**
     * Sets the Script to be executed when the window is iconified.
     */
    public void setIconified(Script iconified) {
        this.iconified = iconified;
    }

    /**
     * Sets the Script to be executed when the window is opened.
     */
    public void setOpened(Script opened) {
        this.opened = opened;
    }



    // Implementation methods
    //-------------------------------------------------------------------------
    protected void invokeScript(XMLOutput output, WindowEvent event, Script script) {
        if ( var != null ) {
            // define a variable of the event
            context.setVariable(var, event);
        }

        try {
            if ( script != null ) {
                script.run(context, output );
            }
            else {
                // invoke the body
                invokeBody(output);
            }
        }
        catch (Exception e) {
            log.error( "Caught exception processing window event: " + event, e );
        }
    }

}
