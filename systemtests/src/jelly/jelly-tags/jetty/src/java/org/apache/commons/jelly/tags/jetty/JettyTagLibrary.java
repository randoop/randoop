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

import java.util.Map;

import org.apache.commons.jelly.TagLibrary;

/**
 * A set of jelly tags for instantiating a Jetty HTTP server
 *
 * @author rtl
 * @version $Id: JettyTagLibrary.java,v 1.1 2002/07/14 13:05:14 dion Exp $
 */
public class JettyTagLibrary extends TagLibrary {

    /**
     * Creates a new instance of LatkaTagLibrary
     */
    public JettyTagLibrary() {

        registerTag("jettyHttpServer", JettyHttpServerTag.class);
        registerTag("socketListener", SocketListenerTag.class);
        registerTag("realm", RealmTag.class);
        registerTag("httpContext", HttpContextTag.class);
        registerTag("resourceHandler", ResourceHandlerTag.class);
        registerTag("notFoundHandler", NotFoundHandlerTag.class);
        registerTag("securityHandler", SecurityHandlerTag.class);

        registerTag("jellyResourceHandler", JellyResourceHandlerTag.class);
        registerTag("getRequest", GetRequestTag.class);
        registerTag("postRequest", PostRequestTag.class);
        registerTag("putRequest", PutRequestTag.class);
        registerTag("deleteRequest", DeleteRequestTag.class);
        registerTag("responseHeader", ResponseHeaderTag.class);
        registerTag("responseBody", ResponseBodyTag.class);
        registerTag("responseCode", ResponseCodeTag.class);
    }

    /**
     * @see TagLibarary#getTagClasses()
     *
     * @return a Map of tag name to tag class
     */
    public Map getTagClasses() {
        return super.getTagClasses();
    }

}

