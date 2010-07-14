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
import org.apache.commons.httpclient.methods.DeleteMethod;

/**
 * A http delete tag
 *
 * @author  dion
 * @version $Id: DeleteTag.java,v 1.2 2002/07/14 12:38:22 dion Exp $
 */
public class DeleteTag extends HttpTagSupport {

    /** the delete method */
    private DeleteMethod _deleteMethod;

    /**
     * Creates a new instance of DeleteTag
     */
    public DeleteTag() {
    }

    /**
     * @return a url method for a get request
     * @throws MalformedURLException when the url is bad
     */
    protected HttpMethod getHttpMethod() throws MalformedURLException {
        if (_deleteMethod == null) {
            _deleteMethod = new DeleteMethod(getResolvedUrl());
        }
        return _deleteMethod;
    }

}
