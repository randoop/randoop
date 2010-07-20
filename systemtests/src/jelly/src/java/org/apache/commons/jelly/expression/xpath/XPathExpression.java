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
package org.apache.commons.jelly.expression.xpath;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionSupport;
import org.apache.commons.jelly.impl.TagScript;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.VariableContext;
import org.jaxen.XPath;
import org.jaxen.JaxenException;
import org.jaxen.dom4j.Dom4jXPath;

/** An expression which returns an XPath object.
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.4 $
  */
public class XPathExpression extends ExpressionSupport implements VariableContext {

    /** The Log to which logging calls will be made. */
    private Log log = LogFactory.getLog(XPathExpression.class);

    private String text;
    private Expression xpathExpr;
    private JellyContext context;
    private Map uris;

    public XPathExpression() {
    }

    public XPathExpression(String text,
                           Expression xpathExpr,
                           TagScript tagScript) {
        this.text = text;
        this.xpathExpr = xpathExpr;

        Map namespaceContext = tagScript.getNamespaceContext();

        this.uris = createUriMap(namespaceContext);
    }

    public String toString() {
        return getExpressionText();
    }

    // Expression interface
    //-------------------------------------------------------------------------
    public String getExpressionText() {
        return this.text;
    }

    public Object evaluate(JellyContext context) {
        this.context = context;

        try
        {
            XPath xpath = new Dom4jXPath( this.xpathExpr.evaluateAsString( context ) );

            xpath.setVariableContext(this);

            if (log.isDebugEnabled()) {
                log.debug( "Setting the namespace context to be: " + uris );
            }

            xpath.setNamespaceContext( new SimpleNamespaceContext( this.uris ) );

            return xpath;
        }
        catch (JaxenException e)
        {
            log.error( "Error constructing xpath",
                       e );
        }

        return null;
    }

    // VariableContext interface
    //-------------------------------------------------------------------------
    public Object getVariableValue(
        String namespaceURI,
        String prefix,
        String localName) {

        Object value = context.getVariable(localName);

        //log.debug( "Looking up XPath variable of name: " + localName + " value is: " + value );

        return value;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a synchronized Map of non-null and non-blank
     * namespace prefixes to namespace URIs
     */
    protected Map createUriMap(Map namespaceContext) {
        // now lets clone the Map but ignoring default or null prefixes
        Map uris = new Hashtable(namespaceContext.size());
        for (Iterator iter = namespaceContext.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String prefix = (String) entry.getKey();
            if (prefix != null && prefix.length() != 0) {
                uris.put(prefix, entry.getValue());
            }
        }
        return uris;
    }
}
