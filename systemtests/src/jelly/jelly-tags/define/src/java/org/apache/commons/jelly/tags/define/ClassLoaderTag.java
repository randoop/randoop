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

package org.apache.commons.jelly.tags.define;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.util.ClassLoaderUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Creates a new <code>URLClassLoader</code> to dynamically
 * load tags froms.
 *
 * @author <a href="mailto:stephenh@chase3000.com">Stephen Haberman</a>
 * @version $Revision: 1.6 $
 */
public class ClassLoaderTag extends BeanTag {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(ClassLoaderTag.class);

    /** The name to export the classloader to. */
    private String var;

    /** The URL to load the classes from. */
    private String url;

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the variable to store the class loader in
     */
    public String getVar() {
        return this.var;
    }

    /**
     * @param var the variable to store the class loader in
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * @return the url to load the classes from
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @param url the url to load the classes from
     */
    public void setUrl(String url) {
        this.url = url;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if ( getVar() == null ) {
            throw new MissingAttributeException( "var" );
        }
        if ( getUrl() == null ) {
            throw new MissingAttributeException( "url" );
        }

        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        if (parent == null) {
            parent = ClassLoaderUtils.getClassLoader(getClass());
        }

        URLClassLoader newClassLoader = null;

        try {
            newClassLoader =
              new URLClassLoader( new URL[] { new URL(getUrl()) }, parent );
        } catch (MalformedURLException e) {
            throw new JellyTagException(e);
        }

        log.debug("Storing the new classloader in " + getVar());

        context.setVariable(getVar(), newClassLoader);
    }

}
