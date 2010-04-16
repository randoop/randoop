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

package org.apache.commons.jelly.tags.threads;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Creates a mutex object and stores it in a variable
 *
 * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
 */

public class MutexTag extends TagSupport {
    /** The variable name of the mutex */
    private String var = null;

    /** Create and set the mutex */
    public void doTag(XMLOutput output) throws JellyTagException {
        if (var == null) {
            throw new JellyTagException("mutexes require a var attribute");
        }

        context.setVariable(var, this);
    }

    /**
     * Sets the variable name to export
     * @param var The variable name
     */
    public void setVar(String var) {
        this.var = var;
    }
}
