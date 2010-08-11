/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.commons.jelly.tags.define;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.DynamicTagLibrary;

/**
 * &lt;extend&gt; is used to extend a dynamic tag defined in an inherited
 * dynamic tag library
 * <p/>
 *
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see SuperTag
 */
public class ExtendTag extends DefineTagSupport {

    private String name;

    private Script superScript;

    public ExtendTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        DynamicTagLibrary library = getTagLibrary();
        DynamicTagLibrary owner = library.find(getName());
        if (owner == null) {
            throw new JellyTagException(
                "Cannot extend " + getName() + ": dynamic tag not defined");
        }
        if (owner == library) {
            // disallow extension of tags defined within the same tag
            // library
            throw new JellyTagException("Cannot extend " + getName() +
                                     ": dynamic tag defined locally");
        }
        superScript = owner.getDynamicTag(name);
        if (superScript == null) {
            // tag doesn't define a script - disallow this for the moment.
            throw new JellyTagException("Cannot extend " + getName() +
                                     ": tag is not a dynamic tag");
        }

        owner.registerDynamicTag(getName() , getBody());
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the name of the tag to create
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the tag to create
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the parent implementation of this tag
     */
    public Script getSuperScript() {
        return superScript;
    }
}

