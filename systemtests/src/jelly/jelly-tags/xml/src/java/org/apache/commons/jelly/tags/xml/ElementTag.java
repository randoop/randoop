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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/** A tag to produce an XML element which can contain other attributes
  * or elements like the <code>&lt;xsl:element&gt;</code> tag.
  *
  * @author James Strachan
  * @version $Revision: 1.5 $
  */
public class ElementTag extends TagSupport {

    /** The namespace URI */
    private String namespace;

    /** The qualified name */
    private String name;

    /** The XML Attributes */
    private AttributesImpl attributes = new AttributesImpl();

    /** flag set if attributes are output */
    private boolean outputAttributes;

    public ElementTag() {
    }

    /**
     * Sets the attribute of the given name to the specified value.
     *
     * @param name of the attribute
     * @param value of the attribute
     * @throws JellyException if the start element has already been output.
     *   Attributes must be set on the outer element before any content
     *   (child elements or text) is output
     */
    public void setAttributeValue( String name, String value ) throws JellyTagException {
        if (outputAttributes) {
            throw new JellyTagException(
                "Cannot set the value of attribute: "
                + name + " as we have already output the startElement() SAX event"
            );
        }

        // ### we'll assume that all attributes are in no namespace!
        // ### this is severely limiting!
        // ### we should be namespace aware
        int index = attributes.getIndex("", name);
        if (index >= 0) {
            attributes.removeAttribute(index);
        }
        // treat null values as no attribute
        if (value != null) {
            attributes.addAttribute("", name, name, "CDATA", value);
        }
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        int idx = name.indexOf(':');
        final String localName = (idx >= 0)
            ? name.substring(idx + 1)
            : name;

        outputAttributes = false;

        XMLOutput newOutput = new XMLOutput(output) {

            // add an initialize hook to the core content-generating methods

            public void startElement(
                String uri,
                String localName,
                String qName,
                Attributes atts)
                throws SAXException {
                initialize();
                super.startElement(uri, localName, qName, atts);
            }

            public void endElement(String uri, String localName, String qName)
                throws SAXException {
                initialize();
                super.endElement(uri, localName, qName);
            }

            public void characters(char ch[], int start, int length) throws SAXException {
                initialize();
                super.characters(ch, start, length);
            }

            public void ignorableWhitespace(char ch[], int start, int length)
                throws SAXException {
                initialize();
                super.ignorableWhitespace(ch, start, length);
            }
            public void processingInstruction(String target, String data)
                throws SAXException {
                initialize();
                super.processingInstruction(target, data);
            }

            /**
             * Ensure that the outer start element is generated
             * before any content is output
             */
            protected void initialize() throws SAXException {
                if (!outputAttributes) {
                    super.startElement(namespace, localName, name, attributes);
                    outputAttributes = true;
                }
            }
        };

        invokeBody(newOutput);

        try {
            if (!outputAttributes) {
                output.startElement(namespace, localName, name, attributes);
                outputAttributes = true;
            }

            output.endElement(namespace, localName, name);
            attributes.clear();
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the qualified name of the element
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the qualified name of the element
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the namespace URI of the element
     */
    public String getURI() {
        return namespace;
    }

    /**
     * Sets the namespace URI of the element
     */
    public void setURI(String namespace) {
        this.namespace = namespace;
    }
}
