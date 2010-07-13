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
package org.apache.commons.jelly.tags.swing;

import java.awt.Font;
import java.util.Map;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MapTagSupport;
import org.apache.commons.jelly.XMLOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates an Font and attaches it to the parent component or exports the font as
 * a reusable variable that can be attached to multiple widgets.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class FontTag extends MapTagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(FontTag.class);

    /** the current font instance */
    private Font font;

    public FontTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
/*
 * maybe do some type conversions or name mapping code...
 *
    public void setAttribute(String name, Object value) {
        if (name.equals("size")) {
            super.setAttribute(name, ConvertUtils.convert(Integer.class, value));
        }
        else {
            super.setAttribute(name, value);
        }
    }
*/

    public void doTag(final XMLOutput output) throws JellyTagException {
        Map attributes = getAttributes();
        String var = (String) attributes.remove("var");

        font = createFont(attributes);

        if (var != null) {
            context.setVariable(var, font);
        }
        else {
            // now lets add this font to its parent if we have one
            ComponentTag tag = (ComponentTag) findAncestorWithClass( ComponentTag.class );
            if ( tag != null ) {
                tag.setFont(font);
            }
            else {
                throw new JellyTagException( "this tag must be nested within a JellySwing widget tag or the 'var' attribute must be specified" );
            }
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the Font object for this tag
     */
    public Font getFont() {
        return font;
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new Font based on the given properties
     */
    protected Font createFont(Map map) {
        log.info( "Creating font from properties: " + map );
        Font font = new Font(map);
        //Font font = Font.getFont(map);
        log.info( "Created font: " + font );
        return font;
    }
}
