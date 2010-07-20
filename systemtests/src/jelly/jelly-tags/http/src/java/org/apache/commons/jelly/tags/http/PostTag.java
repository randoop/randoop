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
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;


/**
 * A http post
 *
 * @author  dion
 */
public class PostTag extends HttpTagSupport {

    /** the post method */
    private PostMethod _postMethod;

    /** Creates a new instance of PostTag */
    public PostTag() {
    }

    /**
     * Return a {@link HttpUrlMethod method} to be used for post'ing
     *
     * @return a HttpUrlMethod implementation
     * @throws MalformedURLException when the {@link getUrl() url} or
     * {@link #getPath() path} is invalid
     */
    protected HttpMethod getHttpMethod() throws MalformedURLException {
        if (_postMethod == null) {
            _postMethod = new PostMethod(getResolvedUrl());
        }
        return _postMethod;
    }

    /**
     * Set the current parameters on the url method ready for processing
     *
     * This method <strong>must</strong> be called after
     *  {@link getHttpUrlMethod}
     */
    protected void setParameters(HttpMethod method) {
        NameValuePair nvp = null;
        for (int index = 0; index < getParameters().size(); index++) {
            NameValuePair parameter = (NameValuePair) getParameters().
                get(index);
            ((PostMethod) method).addParameter(parameter);
        }
    }

}
