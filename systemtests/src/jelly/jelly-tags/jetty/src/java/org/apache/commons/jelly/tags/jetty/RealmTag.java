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

package org.apache.commons.jelly.tags.jetty;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

import org.mortbay.http.HashUserRealm;

import java.io.IOException;
import java.net.URL;

/**
 * Declare a user realm for a Jetty http server
 *
 * @author  rtl
 * @version $Id: RealmTag.java,v 1.3 2002/07/14 12:38:22 dion Exp $
 */
public class RealmTag extends TagSupport {

    /** parameter name with default*/
    private String _name;

    /** parameter config, with default */
    private String _config;

    /** Creates a new instance of RealmTag */
    public RealmTag() {
    }

    /**
     * Perform the tag functionality. In this case, add a realm with the
     * specified name using the specified config (preperties) file to the
     * parent server,
     *
     * @param xmlOutput where to send output
     * @throws Exception when an error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {
        JettyHttpServerTag httpserver = (JettyHttpServerTag) findAncestorWithClass(
            JettyHttpServerTag.class);
        if ( httpserver == null ) {
            throw new JellyTagException( "<realm> tag must be enclosed inside a <server> tag" );
        }
        if (null == getName() || null == getConfig()) {
            throw new JellyTagException( "<realm> tag must have a name and a config" );
        }

        // convert the config string to a URL
        // (this makes URL's relative to the location of the script
        try {
            URL configURL = getContext().getResource(getConfig());
            httpserver.addRealm( new HashUserRealm(getName(), configURL.toString() ) );
        } catch (IOException e) {
            throw new JellyTagException(e);
        }

        invokeBody(xmlOutput);
    }

    //--------------------------------------------------------------------------
    // Property accessors/mutators
    //--------------------------------------------------------------------------

    /**
     * Getter for property name.
     *
     * @return value of property name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Setter for property name.
     *
     * @param name New value of property name.
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Getter for property config.
     *
     * @return value of property config.
     */
    public String getConfig() {
        return _config;
    }

    /**
     * Setter for property config.
     *
     * @param config New value of property config.
     */
    public void setConfig(String config) {
        _config = config;
    }

}
