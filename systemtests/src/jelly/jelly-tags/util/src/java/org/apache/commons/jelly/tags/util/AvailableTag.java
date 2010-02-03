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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * A tag which evaluates its body if the given file is available.
 * The file can be specified via a File object or via a relative or absolute
 * URI from the current Jelly script.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class AvailableTag extends TagSupport {

    private File file;
    private String uri;

    public AvailableTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws JellyTagException {
        boolean available = false;

        if (file != null) {
            available = file.exists();
        }
        else if (uri != null) {
            try {
                URL url = context.getResource(uri);
                String fileName = url.getFile();
                InputStream is = url.openStream();
                available = (is != null);
                is.close();
            } catch (MalformedURLException e) {
                throw new JellyTagException(e);
            } catch (IOException ioe) {
                available = false;
            }
        }

        if (available) {
            invokeBody(output);
        }
    }

    // Properties
    //-------------------------------------------------------------------------


    /**
     * Returns the file.
     * @return File
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns the uri.
     * @return String
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the file to use to test whether it exists or not.
     * @param file the file to test for
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Sets the URI to use to test for availability.
     * The URI can be a full file based URL or a relative URI
     * or an absolute URI from the root context.
     *
     * @param uri the URI of the file to test
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

}
