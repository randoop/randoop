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
import org.apache.commons.jelly.xpath.XPathTagSupport;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.xml.sax.SAXException;

/** A tag which performs a string XPath expression; similar to &lt;xsl:value-of&gt;
  * in XSLT
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.5 $
  */
public class ExprTag extends XPathTagSupport {

    /** The XPath expression to evaluate. */
    private XPath select;

    public ExprTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        Object xpathContext = getXPathContext();

        if (select == null) {
            throw new MissingAttributeException( "select" );
        }

        try {
            String text = select.stringValueOf(xpathContext);
            if ( text != null ) {
                output.write(text);
            }
        }
        catch (SAXException e) {
            throw new JellyTagException(e);
        }
        catch (JaxenException e) {
            throw new JellyTagException(e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    /** Sets the XPath expression to evaluate. */
    public void setSelect(XPath select) {
        this.select = select;
    }
}
