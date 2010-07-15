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

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/** Imports another script.
 *
 *  <p>
 *  By default, the imported script does not have access to
 *  the parent script's variable context.  This behaviour
 *  may be modified using the <code>inherit</code> attribute.
 *  </p>
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 * @version $Revision: 1.10 $
 */

public class ImportTag extends TagSupport {

    /**
     * the location of the script being imported, relative to the
     * current script
     */
    private String uri;

    /**
     * Whether the imported script has access to the caller's variables
     */
    private boolean inherit;

    /**
     * The file to be imported. Mutually exclusive with uri.
     * uri takes precedence.
     */
    private String file;

    /**
     * Create a new Import tag.
     */
    public ImportTag() {
    }


    // Tag interface
    //-------------------------------------------------------------------------
    /**
     * Perform tag processing
     * @param output the destination for output
     * @throws MissingAttributeException if a required attribute is missing
     * @throws JellyTagException on any other errors
     */
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if (uri == null && file == null) {
            throw new MissingAttributeException( "uri" );
        }

        try {
            if (uri != null) {
                // we need to create a new JellyContext of the URI
                // take off the script name from the URL
                context.runScript(uri, output, true, isInherit() );
            } else {
                context.runScript(new java.io.File(file), output, true,
                  isInherit());
            }
        }
        catch (JellyException e) {
            throw new JellyTagException("could not import script",e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return whether property inheritence is enabled
     */
    public boolean isInherit() {
        return inherit;
    }

    /**
     * Sets whether property inheritence is enabled or disabled
     */
    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    /**
     * Sets the URI (relative URI or absolute URL) for the script to evaluate.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }


    /**
     * Sets the file for the script to evaluate.
     * @param file The file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

}
