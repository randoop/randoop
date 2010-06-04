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

/**
 * Respond to a GET request to a Jetty http server
 *
 * @author  rtl
 * @version $Id: PutRequestTag.java,v 1.3 2002/07/14 12:38:22 dion Exp $
 */
public class GetRequestTag extends AbstractMethodHandlerTag {

    /** return the name of the http method handled by this tag */
    public String getMethodHandled() {
        return "GET";
    }
}

