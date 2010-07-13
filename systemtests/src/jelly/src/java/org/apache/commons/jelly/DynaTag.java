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

/** 
 * <p><code>DynaTag</code> represents a Jelly custom tag which
 * can take its attributes dynamically and store them in some data structure.
 * Typically a DynaTag may use either a Map or a DynaBean to implement itself
 * which avoids writing explicit getter and setter methods for each possible attribute.
 * </p>
 * <p>
 * This kind of tag can be extremely useful when making HTML-like tags which
 * generally output all the attributes which are used in the markup, except
 * one or two special attributes are used, all others pass through.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.11 $
 */

public interface DynaTag extends Tag {

    /** Sets an attribute value of this tag before the tag is invoked
     */
    public void setAttribute(String name, Object value) throws JellyTagException;

    /**
     * @return the type of the given attribute. By default just return
     * Object.class if this is not known.
     * If this method returns Expression.class then the expression will not
     * be evaluated and just passed in as the attribute value.
     */
    public Class getAttributeType(String name) throws JellyTagException;
}
