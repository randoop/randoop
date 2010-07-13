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

import java.io.File;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/** A tag which conditionally evaluates its body based on some condition
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.16 $
  */

public class IncludeTag extends TagSupport {

    private String uri;
    private File file;

    private boolean shouldExport;
    private boolean shouldInherit;

    public IncludeTag() {
        this.shouldExport = false;
        this.shouldInherit = true;
    }

    public void setInherit(String inherit) {
        if ("true".equals(inherit)) {
            this.shouldInherit = true;
        } else {
            this.shouldInherit = false;
        }
    }

    public void setExport(String export) {
        if ("true".equals(export)) {
            this.shouldExport = true;
        } else {
            this.shouldExport = false;
        }
    }

    public boolean isInherit() {
        return this.shouldInherit;
    }

    public boolean isExport() {
        return this.shouldExport;
    }

    /**
     * @return
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the file to be included which is either an absolute file or a file
     * relative to the current directory
     */
    public void setFile(File file) {
        this.file = file;
    }


    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output)
        throws MissingAttributeException, JellyTagException {

        if (uri == null && file == null) {
            throw new MissingAttributeException("uri");
        }

        // we need to create a new JellyContext of the URI
        // take off the script name from the URL
        String text = null;
        try {
            if (uri != null) {
                text = uri;
                context.runScript(uri, output, isExport(), isInherit());
            }
            else {
                text = file.toString();
                context.runScript(file, output, isExport(), isInherit());
            }
        }
        catch (JellyException e) {
            throw new JellyTagException("could not include jelly script: " + text + ". Reason: " + e, e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    /** Sets the URI (relative URI or absolute URL) for the script to evaluate. */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
