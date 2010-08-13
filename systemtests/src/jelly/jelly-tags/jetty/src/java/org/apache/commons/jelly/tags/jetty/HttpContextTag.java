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

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.SecurityConstraint.Authenticator;
import org.mortbay.util.Resource;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Declare a context for a Jetty http server
 *
 * @author  rtl
 * @version $Id: HttpContextTag.java,v 1.3 2002/07/14 12:38:22 dion Exp $
 */
public class HttpContextTag extends TagSupport {

    /** parameter path with default*/
    private String _contextPath = JettyHttpServerTag.DEFAULT_CONTEXT_PATH;

    /** parameter resourceBase, with default */
    private String _resourceBase = JettyHttpServerTag.DEFAULT_RESOURCE_BASE;

    /** parameter realmName*/
    private String _realmName;

    /** the actual context this tag refers to */
    private HttpContext _context;

    /** Creates a new instance of HttpContextTag */
    public HttpContextTag() {
        // create an actual context for this tag
        _context = new HttpContext();
    }

    /**
     * Perform the tag functionality. In this case, setup the context path
     * and resource base before adding the context to the parent server
     *
     * @param xmlOutput where to send output
     * @throws Exception when an error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {

        JettyHttpServerTag httpserver = (JettyHttpServerTag) findAncestorWithClass(
            JettyHttpServerTag.class);
        if ( httpserver == null ) {
            throw new JellyTagException( "<httpContext> tag must be enclosed inside a <server> tag" );
        }

        // allow nested tags first, e.g body
        invokeBody(xmlOutput);

        _context.setContextPath(getContextPath());

        // convert the resource string to a URL
        // (this makes URL's relative to the location of the script
        try {
            URL baseResourceURL = getContext().getResource(getResourceBase());
            _context.setBaseResource(Resource.newResource(baseResourceURL));
        }
        catch (MalformedURLException e) {
            throw new JellyTagException(e);
        }
        catch (IOException e) {
            throw new JellyTagException(e);
        }

        if (null != getRealmName()) {
            _context.setRealmName(getRealmName());
        }
        httpserver.addContext(_context);

    }

    /**
     * Add an http handler to the context instance
     *
     * @param handler the handler to add
     */
    public void addHandler(HttpHandler httHandler) {
        _context.addHandler(httHandler);
    }

    /**
     * Add a security constraint for the specified path specification
     * to the context instance
     *
     * @param pathSpec the path specification for the security constraint
     * @param sc the security constraint to add
     */
    public void addSecurityConstraint(String pathSpec, SecurityConstraint sc) {
        _context.addSecurityConstraint(pathSpec, sc);
    }

    /**
     * Add an authenticator to the context instance
     *
     * @param authenticator the authenticator to add
     */
    public void setAuthenticator(Authenticator authenticator)
    {
        _context.setAuthenticator(authenticator);
    }

    //--------------------------------------------------------------------------
    // Property accessors/mutators
    //--------------------------------------------------------------------------
    /**
     * Getter for property context path.
     *
     * @return value of property context path.
     */
    public String getContextPath() {
        return _contextPath;
    }

    /**
     * Setter for property context path.
     *
     * @param path New resourceBase of property context path.
     */
    public void setContextPath(String contextPath) {
        _contextPath = contextPath;
    }

    /**
     * Getter for property resourceBase.
     *
     * @return value of property resourceBase.
     */
    public String getResourceBase() {
        return _resourceBase;
    }

    /**
     * Setter for property resourceBase.
     *
     * @param resourceBase New value of property resourceBase.
     */
    public void setResourceBase(String resourceBase) {
        _resourceBase = resourceBase;
    }

    /**
     * Getter for property realm name.
     *
     * @return value of property realm name.
     */
    public String getRealmName() {
        return _realmName;
    }

    /**
     * Setter for property context path.
     *
     * @param path New resourceBase of property context path.
     */
    public void setRealmName(String realmName) {
        _realmName = realmName;
    }

}
