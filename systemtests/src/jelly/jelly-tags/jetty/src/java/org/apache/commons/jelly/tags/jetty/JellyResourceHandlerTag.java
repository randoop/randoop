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
 * A resource handler that uses Jelly scripts to provide resources
 * to a context in a Jetty http server
 *
 * @author  rtl
 * @version $Id: JellyResourceHandlerTag.java,v 1.3 2002/07/14 12:38:22 dion Exp $
 */
public class JellyResourceHandlerTag extends TagSupport {

    /** The http handler that calls the body of the tag. */
    private JellyResourceHttpHandler _jellyResourceHttpHandler;

    /** Creates a new instance of JellyResourceHandlerTag */
    public JellyResourceHandlerTag() {
    }

    /**
     * Perform the tag functionality. In this case, add an http handler
     * to the parent context that runs the script in the body of this tag
     *
     * @param xmlOutput where to send output
     * @throws Exception when an error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {
        HttpContextTag httpContext = (HttpContextTag) findAncestorWithClass(
            HttpContextTag.class);
        if ( httpContext == null ) {
            throw new JellyTagException( "<jellyResourceHandler> tag must be enclosed inside a <httpContext> tag" );
        }

        _jellyResourceHttpHandler =
            new JellyResourceHttpHandler(xmlOutput);

        httpContext.addHandler(_jellyResourceHttpHandler);

        // process any child method handlers
        invokeBody(xmlOutput);
    }

    //--------------------------------------------------------------------------
    // Property accessors/mutators
    //--------------------------------------------------------------------------

    protected JellyResourceHttpHandler getJellyResourceHttpHandler() {
        return _jellyResourceHttpHandler;
    }

}
