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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Reader;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A tag which loads text from a file or URI into a Jelly variable.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.8 $
 */
public class LoadTextTag extends TagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(LoadTextTag.class);

    private String var;
    private File file;
    private String uri;
    private String encoding;

    public LoadTextTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if (var == null) {
            throw new MissingAttributeException("var");
        }
        if (file == null && uri == null) {
            throw new JellyTagException( "This tag must have a 'file' or 'uri' specified" );
        }
        
        InputStream in = null;
        if (file != null) {
            if (! file.exists()) {
                throw new JellyTagException( "The file: " + file + " does not exist" );
            }

            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new JellyTagException("could not find the file",e);
            }
        }
        else {
            in = context.getResourceAsStream(uri);
            if (in == null) {
                throw new JellyTagException( "Could not find uri: " + uri );
            }
        }

        Reader reader = null;
        if (encoding != null) {
            try {
                reader = new InputStreamReader(in, encoding);
            } catch (UnsupportedEncodingException e) {
                throw new JellyTagException("unsupported encoding",e);
            }
        } else {
            reader = new InputStreamReader(in);
        }

        String text = null;

        try {
            text = loadText(reader);
        }
        catch (IOException e) {
            throw new JellyTagException(e);
        }

        context.setVariable(var, text);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the name of the variable which will be exported with the text value of the
     * given file.
     */
    public void setVar(String var) {
        this.var = var;
    }
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
     * Returns the var.
     * @return String
     */
    public String getVar() {
        return var;
    }

    /**
     * Sets the file to be parsed as text
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Sets the encoding to use to read the file
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the uri to be parsed as text.
     * This can be an absolute URL or a relative or absolute URI
     * from this Jelly script or the root context.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /** Returns the encoding set.
    * @return the encoding set with {@link #setEncoding(String)}
      */
    public String getEncoding() {
        return encoding;
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Loads all the text from the given Reader
     */
    protected String loadText(Reader reader) throws IOException {
        StringBuffer buffer = new StringBuffer();

        // @todo its probably more efficient to use a fixed char[] buffer instead
        try {
            BufferedReader bufferedReader = new BufferedReader(reader);
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                else {
                    buffer.append(line);
                    buffer.append('\n');
                }
            }
            return buffer.toString();
        }
        finally {
            try {
                reader.close();
            }
            catch (Exception e) {
                log.error( "Caught exception closing Reader: " + e, e);
            }
        }
    }
}
