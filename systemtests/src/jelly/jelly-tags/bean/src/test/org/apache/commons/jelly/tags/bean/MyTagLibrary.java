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
package org.apache.commons.jelly.tags.bean;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.impl.TagFactory;
import org.apache.commons.jelly.impl.TagScript;

import org.xml.sax.Attributes;

/**
 * A normal tag library which will use a BeanTag to create beans but this tag
 * library does not derive from BeanTagLibrary and so does not have a &lt;
 * beandef&gt; tag
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class MyTagLibrary extends TagLibrary {

    public MyTagLibrary() {
    }


    // TagLibrary interface
    //-------------------------------------------------------------------------
    public TagScript createTagScript(String name, Attributes attributes) throws JellyException {

        TagFactory factory = new TagFactory() {
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return createBeanTag(name, attributes);
            }
        };
        return new TagScript( factory );
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a Tag for the given tag and attributes. If this
     * tag matches a root bean, then a BeanTag will be created, otherwise a
     * BeanPropertyTag is created to make a nested property.
     */
    protected Tag createBeanTag(String name, Attributes attributes) throws JellyException {
        // is the name bound to a specific class
        Class beanType = getBeanType(name, attributes);
        if (beanType != null) {
            return new BeanTag(beanType, name);
        }

        // its a property tag
        return new BeanPropertyTag(name);
    }

    /**
     * Return the bean class that we should use for the given element name
     *
     * @param name is the XML element name
     * @param attributes the XML attributes
     * @return Class the bean class to use for this element or null if the tag
     * is a nested property
     */
    protected Class getBeanType(String name, Attributes attributes) {
        if (name.equals( "customer")) {
            return Customer.class;
        }
        return null;
    }
}
