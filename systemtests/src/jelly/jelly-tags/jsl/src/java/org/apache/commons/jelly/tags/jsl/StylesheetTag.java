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
package org.apache.commons.jelly.tags.jsl;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.xpath.XPathSource;
import org.apache.commons.jelly.xpath.XPathTagSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.rule.Rule;
import org.dom4j.rule.Stylesheet;
import org.jaxen.JaxenException;
import org.jaxen.XPath;


/**
 * This tag implements a JSL stylesheet which is similar to an
 * XSLT stylesheet but can use Jelly tags inside it
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.8 $
 */
public class StylesheetTag extends XPathTagSupport implements XPathSource {

    /** The Log to which logging calls will be made. */
    private Log log = LogFactory.getLog(StylesheetTag.class);


    /** Holds the stylesheet which will be applied to the source context. */
    private Stylesheet stylesheet;

    /** Holds value of property mode. */
    private String mode;

    /** The variable which the stylesheet will be output as */
    private String var;

    /** The XPath expression to evaluate. */
    private XPath select;

    /** The XPath source used by TemplateTag and ApplyTemplatesTag to pass XPath contexts */
    private Object xpathSource;

    public StylesheetTag() {
    }


    /**
     * @return the XMLOutput from the stylesheet if available
     */
    public XMLOutput getStylesheetOutput() {
        if (stylesheet instanceof JellyStylesheet) {
            JellyStylesheet jellyStyle = (JellyStylesheet) stylesheet;
            return jellyStyle.getOutput();
        }
        return null;
    }

    /**
     * Sets the XMLOutput to use by the current stylesheet
     */
    public void setStylesheetOutput(XMLOutput output) {
        if (stylesheet instanceof JellyStylesheet) {
            JellyStylesheet jellyStyle = (JellyStylesheet) stylesheet;
            jellyStyle.setOutput(output);
        }
    }

    /**
     * Adds a new template rule to this stylesheet
     */
    public void addTemplate( Rule rule ) {
        getStylesheet().addRule( rule );
    }

    // XPathSource interface
    //-------------------------------------------------------------------------

    /**
     * @return the current XPath iteration value
     *  so that any other XPath aware child tags to use
     */
    public Object getXPathSource() {
        return xpathSource;
    }


    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        stylesheet = createStylesheet(output);

        // run the body to add the rules
        invokeBody(output);
        stylesheet.setModeName(getMode());

        if (var != null) {
            context.setVariable(var, stylesheet);
        }
        else {

            //dom4j seems to only throw generic Exceptions
            try {
                Object source = getSource();

                if (log.isDebugEnabled()) {
                    log.debug("About to evaluate stylesheet on source: " + source);
                }

                stylesheet.run(source);
            }
            catch (Exception e) {
                throw new JellyTagException(e);
            }

        }
    }


    // Properties
    //-------------------------------------------------------------------------

    /**
     * Getter for property mode.
     * @return Value of property mode.
     */
    public String getMode() {
        return mode;
    }

    /**
     * Sets the mode.
     * @param mode New value of property mode.
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    public Stylesheet getStylesheet() {
        return stylesheet;
    }

    /** Sets the variable name to define for this expression
     */
    public void setVar(String var) {
        this.var = var;
    }

    /** Sets the XPath expression to evaluate. */
    public void setSelect(XPath select) {
        this.select = select;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /** @return the source on which the stylesheet should run
     */
    protected Object getSource() throws JaxenException {
        Object source = getXPathContext();
        if ( select != null ) {
            return select.evaluate(source);
        }
        return source;
    }


    /**
     * Factory method to create a new stylesheet
     */
    protected Stylesheet createStylesheet(final XMLOutput output) {
        JellyStylesheet answer = new JellyStylesheet();
        answer.setOutput(output);
        return answer;
    }

    /**
     * Sets the xpathSource.
     * @param xpathSource The xpathSource to set
     */
    void setXPathSource(Object xpathSource) {
        this.xpathSource = xpathSource;
    }

}
