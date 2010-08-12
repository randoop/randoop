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

import java.io.IOException;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

import org.mortbay.http.HttpResponse;
import org.mortbay.util.ByteArrayISO8859Writer;

/**
 * Set the response body in a response handler for a Jetty http server
 *
 * @author  rtl
 */
public class ResponseBodyTag extends TagSupport {

    /**
     * Perform the tag functionality. In this case, set the body of a
     * http response found in the jelly context
     *
     * @param xmlOutput where to send output
     * @throws Exception when an error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {

        // get the response from the context
        HttpResponse httpResponse = (HttpResponse) getContext().getVariable("response");
        if (null == httpResponse) {
            throw new JellyTagException("HttpResponse variable not available in Jelly context");
        }

        ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);
        try {
             writer.write(getBodyText());
             writer.flush();
             httpResponse.setContentLength(writer.size());
             writer.writeTo(httpResponse.getOutputStream());
        }
        catch (IOException e) {
            throw new JellyTagException(e);
        }
    }
}

