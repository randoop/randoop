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
package org.apache.commons.jelly.tags.html;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.xml.ParseTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.cyberneko.html.parsers.SAXParser;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import org.xml.sax.SAXException;


/** A tag which parses some HTML and defines a variable with the parsed Document.
  * The HTML can either be specified as its body or can be passed in via the
  * html property which can be a Reader, InputStream, URL or String URI.
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.9 $
  */
public class ParseTag extends ParseTagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(ParseTag.class);

    /** The HTML to parse, either a String URI, a Reader or InputStream */
    private Object html;
    private String element = "match";
    private String attribute = "no-change";

    public ParseTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        if (getVar() == null) {
            throw new IllegalArgumentException("The var attribute cannot be null");
        }
        Document document = null;
        if (html == null) {
            String text = getText();
            if (text != null) {
                document = parseText(text);
            }
            else {
                document = parseBody(output);
            }
        }
        else {
            document = parse(html);
        }
        context.setVariable(getVar(), document);
    }

    // Properties
    //-------------------------------------------------------------------------
    /** Sets the source of the HTML which is either a String URI, Reader or InputStream */
    public void setHtml(Object html) {
        this.html = html;
    }

    /**
     * Sets whether attributes should be converted to a different case.
     * Possible values are "upper", "lower" or "no-change"
     *
     * @param attribute The processing mode of attributes
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    /**
     * Sets whether elements should be converted to a different case
     * Possible values are "upper", "lower" or "match"
     *
     * @param element The processing mode of elements
     */
    public void setElement(String element) {
        this.element = element;
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new SAXReader
     */
    protected SAXReader createSAXReader() throws SAXException {
        // installs the NeckHTML parser
        SAXParser parser = new SAXParser();
        parser.setProperty(
            "http://cyberneko.org/html/properties/names/elems",
            element
        );
        parser.setProperty(
            "http://cyberneko.org/html/properties/names/attrs",
            attribute
        );
        return new SAXReader( parser );
    }
}
