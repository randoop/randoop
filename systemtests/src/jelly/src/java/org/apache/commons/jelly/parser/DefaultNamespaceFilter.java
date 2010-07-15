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
package org.apache.commons.jelly.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * XMLFilter that can provide a default namespace when
 * one has not been declared by the XML document.  Note:
 * this class does not address the namespace of attributes.
 *
 * @author Morgan Delagrange
 */
public class DefaultNamespaceFilter extends XMLFilterImpl {

    protected String uriDefault = null;

    /**
     * Filter for undefined an undefined namespace
     *
     * @param defaultNamespace
     *               uri for the jelly namespace
     * @param reader XMLReader to filter
     */
    public DefaultNamespaceFilter(String defaultNamespace, XMLReader reader) {
        super(reader);
        this.uriDefault = defaultNamespace;
    }

    /**
     * All incoming empty URIs will be remapped to the default.
     *
     * @param prefix incoming prefix
     * @param uri    URI to check and potentially replace
     * @exception SAXException
     */
    public void startPrefixMapping(java.lang.String prefix,
                                   java.lang.String uri)
    throws SAXException {

        if (uri.equals("")) {
            super.startPrefixMapping(prefix,this.uriDefault);
        } else {
            super.startPrefixMapping(prefix,uri);
        }
    }

    /**
     * All incoming empty URIs will be remapped to the default.
     *
     * @param uri       URI to check and potentially replace
     * @param localName
     * @param qName
     * @param atts
     * @exception SAXException
     */
    public void startElement(java.lang.String uri,
                             java.lang.String localName,
                             java.lang.String qName,
                             Attributes atts)
    throws SAXException {

        if (uri.equals("")) {
            super.startElement(this.uriDefault,localName,qName,atts);
        } else {
            super.startElement(uri,localName,qName,atts);
        }

    }

    /**
     * All incoming empty URIs will be remapped to the default.
     *
     * @param namespaceURI
     *                  URI to check and potentially replace
     * @param localName
     * @param qName
     * @exception SAXException
     */
    public void endElement(String namespaceURI, String localName, String qName)
    throws SAXException {
        if (namespaceURI.equals("")) {
            super.endElement(this.uriDefault,localName,qName);
        } else {
            super.endElement(namespaceURI,localName,qName);
        }
    }
}