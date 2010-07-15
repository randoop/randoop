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
import org.apache.commons.httpclient.methods.HeadMethod;

/**
 * A http get tag
 *
 * @author  dion
 * @version $Id: HeadTag.java,v 1.2 2002/07/14 12:38:22 dion Exp $
 */
public class HeadTag extends HttpTagSupport {

    /** the head method */
    private HeadMethod _headMethod;

    /**
     * Creates a new instance of HeadTag
     */
    public HeadTag() {
    }

    /**
     * @return a url method for a Head request
     * @throws MalformedURLException when the url is bad
     */
    protected HttpMethod getHttpMethod() throws MalformedURLException {
        if (_headMethod == null) {
            _headMethod = new HeadMethod(getResolvedUrl());
        }
        return _headMethod;
    }

}
