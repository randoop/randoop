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
import org.mortbay.http.handler.NotFoundHandler;

/**
 * Declare a not found resource handler for a Jetty http server
 *
 * @author  rtl
 * @version $Id: NotFoundHandlerTag.java,v 1.3 2002/07/14 12:38:22 dion Exp $
 */
public class NotFoundHandlerTag extends TagSupport {

    /** Creates a new instance of NotFoundHandlerTag */
    public NotFoundHandlerTag() {
    }

    /**
     * Perform the tag functionality. In this case, add a resource handler
     * to the parent context, setting the allowed methods if required
     *
     * @param xmlOutput where to send output
     * @throws Exception when an error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {
        HttpContextTag httpContext = (HttpContextTag) findAncestorWithClass(
            HttpContextTag.class);
        if ( httpContext == null ) {
            throw new JellyTagException( "<notFoundHandler> tag must be enclosed inside a <httpContext> tag" );
        }
        NotFoundHandler notFoundHandler = new NotFoundHandler();
        httpContext.addHandler(notFoundHandler);
        invokeBody(xmlOutput);
    }

    //--------------------------------------------------------------------------
    // Property accessors/mutators
    //--------------------------------------------------------------------------

}
