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

package org.apache.commons.jelly.tags.ant;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tag which sets an attribute on the parent Ant Task if the given value is not null.
 * This can be useful when setting parameters on Ant tasks, only if they have been specified
 * via some well defined property, otherwise allowing the inbuilt default to be used.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 */
public class SetPropertyTag extends TagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(SetPropertyTag.class);

    private String name;
    private Object value;
    private Object defaultValue;

    public SetPropertyTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if (name == null) {
            throw new MissingAttributeException("name");
        }
        TaskSource tag = (TaskSource) findAncestorWithClass( TaskSource.class );
        if ( tag == null ) {
            throw new JellyTagException( "This tag must be nested within an Ant task tag" );
        }
        Object value = getValue();
        if (value == null) {
            value = getDefault();
        }
        if (value != null) {
            tag.setTaskProperty(name, value);
        }
    }


    // Properties
    //-------------------------------------------------------------------------

    /**
     * Returns the name.
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value.
     * @return Object
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the name of the Ant task property to set.
     * @param name The name of the Ant task property to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value of the Ant task property to set.
     * @param value The value of the Ant task property to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the defaultValue.
     * @return Object
     */
    public Object getDefault() {
        return defaultValue;
    }

    /**
     * Sets the default value to be used if the specified value is empty.
     */
    public void setDefault(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

}
