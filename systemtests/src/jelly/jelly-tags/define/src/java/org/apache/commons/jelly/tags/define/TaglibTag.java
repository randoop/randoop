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
package org.apache.commons.jelly.tags.define;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.DynamicTagLibrary;

/**
 * The &lt;taglib&gt; tag is used to define a new tag library
 * using a Jelly script. The tag library is identified by its
 * {@link #getURI() URI}.
 *
 * The tags for a taglib are declared using the {@link TagTag}.
 *
 * You can 'inherit' tags from a previously defined taglib, as well,
 * allowing runtime extension of tag libraries
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.6 $
 */
public class TaglibTag extends TagSupport {

    /** The namespace URI */
    private String uri;
    /** The new tags being added */
    private DynamicTagLibrary tagLibrary;
    /** Whether or not inheritence is enabled */
    private boolean inherit = true;

    public TaglibTag() {
    }

    public TaglibTag(String uri) {
        this.uri = uri;
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        String uri = getUri();
        tagLibrary = new DynamicTagLibrary( uri );

        // inherit tags from an existing tag library
        if ( isInherit() ) {
            tagLibrary.setParent( context.getTagLibrary( uri ) );
        }
        context.registerTagLibrary( uri, tagLibrary );

        invokeBody(output);

        tagLibrary = null;
    }

    // Properties
    //-------------------------------------------------------------------------
    public String getUri() {
        return uri;
    }

    /**
     * Sets the namespace URI to register this new dynamic tag library with
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    public DynamicTagLibrary getTagLibrary() {
        return tagLibrary;
    }

    /**
     * Returns the inherit.
     * @return boolean
     */
    public boolean isInherit() {
        return inherit;
    }

    /**
     * Sets whether this dynamic tag should inherit from the current existing tag library
     * of the same URI. This feature is enabled by default so that tags can easily be
     * some tags can be overridden in an existing library, such as when making Mock Tags.
     *
     * You can disable this option if you want to disable any tags in the base library,
     * turning them into just normal static XML.
     *
     * @param inherit The inherit to set
     */
    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

}
