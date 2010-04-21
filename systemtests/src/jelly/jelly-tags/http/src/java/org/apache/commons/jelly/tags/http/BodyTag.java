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

package org.apache.commons.jelly.tags.http;

import java.net.MalformedURLException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * A tag to set the body for posts and puts etc
 *
 * @author  dion
 * @version $Id: BodyTag.java,v 1.3 2002/07/14 16:44:10 dion Exp $
 */
public class BodyTag extends TagSupport {

    /** Creates a new instance of BodyTag */
    public BodyTag() {
    }

    /**
     * Perform the tag functionality. In this case, get the parent http tag,
     * and if it's a post or put, set the request body from the body of this
     * tag.
     *
     * @param xmlOutput for writing output to
     * @throws Exception when any error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {
        HttpTagSupport httpTag = (HttpTagSupport) findAncestorWithClass(
            HttpTagSupport.class);

        HttpMethod httpMethod = null;
        try {
            httpMethod = httpTag.getHttpMethod();
        } catch (MalformedURLException e) {
            throw new JellyTagException(e);
        }

        String bodyText = getBodyText();
        if (httpMethod instanceof PostMethod) {
            PostMethod postMethod = (PostMethod) httpMethod;
            postMethod.setRequestBody(bodyText);
        } else if (httpMethod instanceof PutMethod) {
            PutMethod putMethod = (PutMethod) httpMethod;
            putMethod.setRequestBody(bodyText);
        } else {
            throw new IllegalStateException("Http method from parent was "
                + "not post or put");
        }
    }

}
