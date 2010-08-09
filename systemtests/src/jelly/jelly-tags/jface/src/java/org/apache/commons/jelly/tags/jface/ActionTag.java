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

import java.util.Map;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.core.UseBeanTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.swt.widgets.Event;

/**
 * This tag creates an JFace Action
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class ActionTag extends UseBeanTag {

    /**
     *  Implementing Action class
     */
    class ActionImpl extends Action {
        public void runWithEvent(Event event) {
            try {
                context.setVariable(var, event);
                invokeBody(output);
            } catch (Exception e) {
                log.error(
                    "Caught exception: "
                        + e
                        + " while processing event: "
                        + event,
                    e);
            }
        }
    }

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(ActionTag.class);
    private XMLOutput output;
    private String var = "event";

    /**
     * @param arg0
     */
    public ActionTag(Class arg0) {
        super(arg0);
    }

    /**
     * @return IContributionManager
     */
    protected IContributionManager getParentContributionManager() {
        MenuManagerTag tag =
            (MenuManagerTag) findAncestorWithClass(MenuManagerTag.class);
        if (tag != null) {
            return tag.getMenuManager();
        }
        return null;
    }

    /**
      * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
      */
    public void doTag(XMLOutput output)
        throws MissingAttributeException, JellyTagException {

        Map attributes = getAttributes();

        Action action = new ActionImpl();
        setBeanProperties(action, attributes);

        IContributionManager cm = getParentContributionManager();
        if (cm != null) {
            cm.add(action);
        }

        this.output = output;
    }

}