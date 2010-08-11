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

/**
 * An abstract base tag to declare a handler for a particular request method
 * in an http context in an http server
 *
 * @author  rtl
 * @version $Id: AbstractMethodHandlerTag.java,v 1.3 2002/07/14 12:38:22 dion Exp $
 */
abstract public class AbstractMethodHandlerTag extends TagSupport {

    /** Override this to return the name of the http method handled by this tag */
    abstract public String getMethodHandled();

    /**
     * Perform the tag functionality. In this case, add a http method handler
     * to the parent that invokes the script in the body of this tag when
     * processing an http request
     *
     * @param xmlOutput where to send output
     * @throws Exception when an error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {
        JellyResourceHandlerTag parentTag =
            (JellyResourceHandlerTag) findAncestorWithClass(
                JellyResourceHandlerTag.class);

        if ( parentTag == null ) {
            throw new JellyTagException( "<" + getMethodHandled().toLowerCase() +
                                      "Request> tag must be enclosed inside a <jellyResourceHandler> tag" );
        }

        // register this tag with the http handler for the appropriate method
        parentTag.getJellyResourceHttpHandler().registerTag(this, getMethodHandled());

        // NOTE - don't invokeBody here as we only want to do it during a request
    }

}

