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

package org.apache.commons.jelly;

import java.util.Map;

/**
 * <p><code>NamespaceAwareTag</code> represents a Jelly custom tag which
 * needs to be aware of the XML Namespace context in which it is used.
 * When the tag is used it will be given the namespace context which is a
 * Map keyed on the namespace prefixes and the values are the namespace URIs
 * in scope in the tags element.
 * </p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.6 $
 */

public interface NamespaceAwareTag extends Tag {

    /**
     * Sets the namespace context in scope when this tag is used
     *
     * @param prefixToUriMap is a Map where the keys are the namespace
     * prefixes and the values are the namespace URIs
     */
    public void setNamespaceContext(Map prefixToUriMap);
}
