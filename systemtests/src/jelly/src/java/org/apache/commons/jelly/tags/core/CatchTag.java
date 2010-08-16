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
package org.apache.commons.jelly.tags.core;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * A tag which catches exceptions thrown by its body.
 * This allows conditional logic to be performed based on if exceptions
 * are thrown or to do some kind of custom exception logging logic.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class CatchTag extends TagSupport {

    private String var;

    public CatchTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        if (var != null) {
            context.removeVariable(var);
        }
        try {
            invokeBody(output);
        }
        catch (Throwable t) {
            if (var != null) {
                context.setVariable(var, t);
            }
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the name of the variable which is exposed with the Exception that gets
     * thrown by evaluating the body of this tag or which is set to null if there is
     * no exception thrown.
     */
    public void setVar(String var) {
        this.var = var;
    }
}
