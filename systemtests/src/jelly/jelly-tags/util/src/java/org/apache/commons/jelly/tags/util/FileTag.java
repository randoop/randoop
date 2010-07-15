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

package org.apache.commons.jelly.tags.util;

import java.io.File;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * A tag which creates a {@link File} from a given name.
 *
 * @author <a href="mailto:dion@apache.org">dIon Gillard</a>
 * @version $Revision: 1.6 $
 */
public class FileTag extends TagSupport {

    /** The file to place into the context */
    private String name;

    /** The variable name to place the file into */
    private String var;

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws MissingAttributeException, JellyTagException {
        boolean available = false;

        if (name == null) {
            throw new MissingAttributeException("name must be specified");
        }

        if (var == null) {
            throw new MissingAttributeException("var must be specified");
        }

        File newFile = new File(name);
        getContext().setVariable(var, newFile);
    }

    /**
     * Name of the file to be placed into the context
     * @param name The fileName to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Name of the variable to contain the file
     * @param var The var to set
     */
    public void setVar(String var) {
        this.var = var;
    }

}
