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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.xml.sax.Attributes;

/**
 * <p><code>DynamicTagLibrary</code> represents a TagLibrary which
 * gets created by running a Jelly script.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.6 $
 */
public class DynamicTagLibrary extends TagLibrary {

    private String uri;
    private Map templates = new HashMap();
    private TagLibrary parent;

    public DynamicTagLibrary() {
    }

    public DynamicTagLibrary(String uri) {
        this.uri = uri;
    }

    /** Creates a new script to execute the given tag name and attributes */
    public TagScript createTagScript(final String name, final Attributes attributes)
        throws JellyException {

        return new TagScript(
            new TagFactory() {
                public Tag createTag(String name, Attributes attributes) throws JellyException {
                    return DynamicTagLibrary.this.createTag(name, attributes);
                }
            }
        );
    }

    /** Creates a new Tag for the given tag name if it exists */
    public Tag createTag(String name, Attributes attributes)
        throws JellyException {

        Object value = templates.get(name);
        if ( value instanceof Script ) {
            Script template = (Script) value;
            return new DynamicTag(template);
        }
        else if ( value instanceof TagFactory ) {
            TagFactory factory = (TagFactory) value;
            return factory.createTag(name, attributes);
        }
        else if ( parent != null ) {
            // delegate to the parent
            return parent.createTag(name, attributes);
        }

        return null;
    }

    /**
     * Creates a new tag with the given name and template
     */
    public void registerDynamicTag(String name, Script template) {
        templates.put(name, template);
    }

    /**
     * Creates a new Jelly Bean Tag with the given name
     */
    public void registerBeanTag(String name, TagFactory factory) {
        templates.put(name, factory);
    }

    /**
     * Returns the script associated with the given tag name
     *
     * @param name The tag name
     * @return The script associated with <code>name</code>, or
     *         <code>null</code> if the tag doesn't exist or isn't a script
     */
    public Script getDynamicTag(String name) {
        Object result = templates.get(name);
        return (result instanceof Script) ? (Script) result : null;
    }

    /**
     * Returns the tag library instance which contains the named tag.
     * <p/>
     * If the tag is not registered within this library, the set of
     * parent libraries will be searched.
     *
     * @param name The tag name
     * @return The tag library containing the named tag, or <code>null</code>
     *         if the tag is not registered.
     */
    public DynamicTagLibrary find(String name) {
        DynamicTagLibrary result = null;
        if (templates.get(name) != null) {
            result = this;
        }
        else if (parent instanceof DynamicTagLibrary) {
            result = ((DynamicTagLibrary) parent).find(name);
        }
        return result;
    }

    // Properties
    //-------------------------------------------------------------------------
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


    /**
     * Returns the parent library which will be used to resolve unknown tags.
     * @return TagLibrary
     */
    public TagLibrary getParent() {
        return parent;
    }

    /**
     * Sets the parent to inherit tags from that are not defined in this library.
     * @param parent The parent to set
     */
    public void setParent(TagLibrary parent) {
        this.parent = parent;
    }

}
