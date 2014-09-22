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

import java.util.HashMap;

import java.util.Map;

/** 
 * <p><code>MapTag</code> is a DynaTag implementation which uses a Map
 * to store its attribute values in. Derived tags can then process this
 * Map, change values, add or remove attributes or perform some other form
 * of processsing pretty easily.
 * </p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.8 $
 */

public abstract class MapTagSupport extends DynaTagSupport {

    private Map map;

    /** Sets an attribute value of this tag before the tag is invoked
     */
    public void setAttribute(String name, Object value) {
        getAttributes().put(name, value);
    }

    /** 
     * Helper method which allows derived tags to access the attributes
     * associated with this tag
     */
    protected Map getAttributes() {
        if (map == null) {
            map = createAttributes();
        }
        return map;
    }
    /**
     * A Factory Method which allows derived tags to overload the Map
     * implementation used by this tag
     */

    protected Map createAttributes() {
        return new HashMap();
    }

}
