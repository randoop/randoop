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
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.xpath.XPathTagSupport;

import org.xml.sax.SAXException;

/**
 * A tag which outputs a comment to the underlying XMLOutput based on the
 * contents of its body.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class CommentTag extends XPathTagSupport {

    private String text;

    public CommentTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        String text = getText();
        if (text == null) {
            text = getBodyText(false);
        }
        char[] ch = text.toCharArray();
        try {
            output.comment(ch, 0, ch.length);
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    /**
     * Returns the text.
     * @return String
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the comment text. If no text is specified then the body of the tag
     * is used instead.
     *
     * @param text The comment text to use
     */
    public void setText(String text) {
        this.text = text;
    }

}