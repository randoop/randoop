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
package org.apache.commons.jelly.tags.jface;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * This tag adds a listener for double-clicks in this viewer.
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class DoubleClickListenerTag
    extends TagSupport
    implements IDoubleClickListener {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(DoubleClickListenerTag.class);

    private String var = "event";
    private XMLOutput output;

    /*
     * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
     */
    public void doTag(XMLOutput output)
        throws MissingAttributeException, JellyTagException {
        if (var == null) {
            throw new MissingAttributeException("var");
        }

        StructuredViewer viewer = getParentViewer();
        if (viewer == null) {
            throw new JellyTagException("This tag must be nested within a viewer tag");
        }

        viewer.addDoubleClickListener(this);
        this.output = output;
    }

    public StructuredViewer getParentViewer() {
        ViewerTag tag = (ViewerTag) findAncestorWithClass(ViewerTag.class);
        if (tag != null) {
            Viewer viewer = tag.getViewer();
            if (viewer instanceof StructuredViewer) {
                return (StructuredViewer) viewer;
            }
        }
        return null;
    }

    /**
     * @return String
     */
    public String getVar() {
        return var;
    }

    /**
     * Sets the var.
     * @param var The var to set
     */
    public void setVar(String var) {
        this.var = var;
    }

    //  Listener interface
    //-------------------------------------------------------------------------
    /*
     * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
     */
    public void doubleClick(DoubleClickEvent event) {
        try {
            context.setVariable(var, event);
            invokeBody(output);
        } catch (Exception e) {
            log.error(
                "Caught exception: " + e + " while processing event: " + event,
                e);
        }
    }

}
