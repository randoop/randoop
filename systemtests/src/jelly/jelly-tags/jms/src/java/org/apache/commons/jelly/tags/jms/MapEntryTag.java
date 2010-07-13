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
package org.apache.commons.jelly.tags.jms;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/** Adds a map entry to the outer Map Message tag
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.5 $
  */
public class MapEntryTag extends TagSupport {

    /** Stores the name of the map entry */
    private String name;

    /** Stores the value of the map entry */
    private Object value;


    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if ( name == null ) {
            throw new MissingAttributeException("name");
        }
        MapMessageTag tag = (MapMessageTag) findAncestorWithClass( MapMessageTag.class );
        if ( tag == null ) {
            throw new JellyTagException("<jms:mapEntry> tag must be within a <jms:mapMessage> tag");
        }

        if (value != null) {
            tag.addEntry( name, value );
        }
        else {
            tag.addEntry( name, getBodyText() );
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    /** Sets the name of the entry in the map message
      */
    public void setName(String name) {
        this.name = name;
    }

    /** Sets the value of the entry in the map message.
      * If no value is set then the body of the tag is used
      */
    public void setValue(Object value) {
        this.value = value;
    }
}
