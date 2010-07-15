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
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.Attribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This tag is bound onto a Java Bean class. When the tag is invoked a bean will be created
 * using the tags attributes.
 * The bean may also have an invoke method called invoke(), run(), execute() or some such method
 * which will be invoked after the bean has been configured.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @version $Revision: 1.5 $
 */
public class AttributeTag extends TagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(AttributeTag.class);

    /** the attribute definition */
    private Attribute attribute;

    public AttributeTag() {
        attribute = new Attribute();
    }

    public AttributeTag(Attribute attribute) {
        this.attribute = attribute;
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        BeanTag tag = (BeanTag) findAncestorWithClass( BeanTag.class );
        if ( tag == null ) {
            throw new JellyTagException( "This tag should be nested inside a <define:bean> or <define:jellybean> tag" );
        }

        tag.addAttribute( attribute );
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the name of the attribute
     */
    public void setName(String name) {
        attribute.setName(name);
    }

    /**
     * Sets whether this attribute is mandatory or not
     */
    public void setRequired(boolean required) {
        attribute.setRequired(required);
    }

    /**
     * Sets the default value of this attribute
     */
    public void setDefaultValue(Expression defaultValue) {
        attribute.setDefaultValue(defaultValue);
    }
}
