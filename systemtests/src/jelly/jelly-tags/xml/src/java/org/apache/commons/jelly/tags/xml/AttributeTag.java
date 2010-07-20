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
package org.apache.commons.jelly.tags.xml;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/** Adds an XML attribute to the parent element tag like
  * the <code>&lt;xsl:attribute&gt;</code> tag.
  *
  * @author James Strachan
  * @version $Revision: 1.6 $
  */
public class AttributeTag extends TagSupport {

    /** the name of the attribute. */
    private String name;


    public AttributeTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        ElementTag tag = (ElementTag) findAncestorWithClass( ElementTag.class );
        if ( tag == null ) {
            throw new JellyTagException( "<attribute> tag must be enclosed inside an <element> tag" );
        }
        tag.setAttributeValue( getName(), getBodyText( false ) );
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the name of the attribute.
     */
    public String getName() {
        return name;
    }
    /**
     * Sets the name of the attribute
     */
    public void setName(String name) {
        this.name = name;
    }
}
