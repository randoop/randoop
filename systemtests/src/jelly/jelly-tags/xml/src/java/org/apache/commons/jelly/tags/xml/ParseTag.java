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
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/** A tag which parses some XML and defines a variable with the parsed Document.
  * The XML can either be specified as its body or can be passed in via the
  * xml property which can be a Reader, InputStream, URL or String URI.
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.7 $
  */
public class ParseTag extends ParseTagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(ParseTag.class);

    /** The xml to parse, either a String URI, a Reader or InputStream */
    private Object xml;

    // Optional properties not defined in JSTL
    /** whether XML validation is enabled or disabled */
    private boolean validate;

    public ParseTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if (getVar() == null) {
            throw new MissingAttributeException("The var attribute cannot be null");
        }

        Document document = getXmlDocument(output);
        context.setVariable(getVar(), document);
    }

    // Properties
    //-------------------------------------------------------------------------
    /** Gets the source of the XML which is either a String URI, Reader or InputStream */
    public Object getXml() {
        return this.xml;
    }

    /** Sets the source of the XML which is either a String URI, a File, Reader or InputStream */
    public void setXml(Object xml) {
        this.xml = xml;
    }

    /** @return whether XML validation is enabled or disabled */
    public boolean getValidate() {
        return validate;
    }

    /** Sets whether XML validation is enabled or disabled */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new SAXReader
     */
    protected SAXReader createSAXReader() {
        return new SAXReader(validate);
    }

    protected Document getXmlDocument(XMLOutput output) throws JellyTagException {
        Document document = null;
        Object xmlObj = this.getXml();

        if (xmlObj == null) {
            String text = getText();
            if (text != null) {
                document = parseText(text);
            }
            else {
                document = parseBody(output);
            }
        }
        else {
            document = parse(xmlObj);
        }

        return document;
    }

}
