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
package org.apache.commons.jelly.impl;

import org.apache.commons.jelly.TagLibrary;


/**
 * <p><code>TagLibraryResolver</code> represents an object capable of
 * resolving a URI to a TagLibrary instance.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.12 $
 */
public interface TagLibraryResolver {

    /**
     * Attempts to resolve the given URI to be associated with a TagLibrary
     * otherwise null is returned to indicate no tag library could be found
     * so that the namespace URI should be treated as just vanilla XML.
     */
    public TagLibrary resolveTagLibrary(String uri);
}