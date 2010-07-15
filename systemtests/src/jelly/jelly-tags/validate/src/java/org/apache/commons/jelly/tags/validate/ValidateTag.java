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
package org.apache.commons.jelly.tags.validate;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierFilter;
import org.iso_relax.verifier.VerifierHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This tag validates its body using a schema Verifier which can
 * validate against DTDs, XML Schema, RelaxNG, Relax or TREX.
 * Any JARV compliant Verifier could be used.
 * The error messages are output as XML events so that they can be styled by the parent tag.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class ValidateTag extends TagSupport {

    /** The verifier that this tag will use */
    private Verifier verifier;

    /** The SAX ErrorHandler */
    private ErrorHandler errorHandler;

    /** The boolean flag for whether the XML is valid */
    private String var;

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws MissingAttributeException, JellyTagException {
        if ( verifier == null ) {
            throw new MissingAttributeException("verifier");
        }
        boolean valid = false;

        // evaluate the body using the current Verifier
        if ( errorHandler != null ) {

            try {
                // we are redirecting errors to another handler
                // so just filter the body
                VerifierFilter filter = verifier.getVerifierFilter();

                // now install the current output in the filter chain...
                // ####

                ContentHandler handler = filter.getContentHandler();
                handler.startDocument();
                invokeBody( new XMLOutput( handler ) );
                handler.endDocument();
                valid = filter.isValid();
            }
            catch (SAXException e) {
                throw new JellyTagException(e);
            }
        }
        else {
            // outputting the errors to the current output
            verifier.setErrorHandler(
                new ErrorHandler() {
                    public void error(SAXParseException exception) throws SAXException {
                        outputException(output, "error", exception);
                    }

                    public void fatalError(SAXParseException exception) throws SAXException {
                        outputException(output, "fatalError", exception);
                    }

                    public void warning(SAXParseException exception) throws SAXException {
                        outputException(output, "warning", exception);
                    }
                }
            );

            try {
                VerifierHandler handler = verifier.getVerifierHandler();
                handler.startDocument();
                invokeBody( new XMLOutput( handler ) );
                handler.endDocument();
                valid = handler.isValid();
            }
            catch (SAXException e) {
                throw new JellyTagException(e);
            }
        }
        handleValid(valid);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the schema Verifier that this tag will use to verify its body
     *
     * @jelly:required
     */
    public void setVerifier(Verifier verifier) {
        this.verifier = verifier;
    }

    /**
     * @return the ErrorHandler used when validating
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Sets the SAX ErrorHandler which is used to capture
     * XML validation events.
     * If an ErrorHandler is specified
     * then this tag will output its body and redirect all error messages
     * to the ErrorHandler.
     * If no ErrorHandler is specified then this tag will just output the
     * error messages as XML events
     *
     * @jelly:optional
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Sets the name of the variable that will contain a boolean flag for whether or
     * not the XML is valid.
     *
     * @jelly:optional
     */
    public void setVar(String var) {
        this.var = var;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Processes whether or not the document is valid.
     * Derived classes can overload this method to do different things, such
     * as to throw assertion exceptions etc.
     */
    protected void handleValid(boolean valid) {
        if (var != null ) {
            Boolean value = (valid) ? Boolean.TRUE : Boolean.FALSE;
            context.setVariable(var, value);
        }
    }

    /**
     * Outputs the given validation exception as XML to the output
     */
    protected void outputException(XMLOutput output, String name, SAXParseException e) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        String uri = "";
        String type = "CDATA";
        attributes.addAttribute( uri, "column", "column", type, Integer.toString( e.getColumnNumber() ) );
        attributes.addAttribute( uri, "line", "line", type, Integer.toString( e.getLineNumber() ) );

        String publicID = e.getPublicId();
        if ( publicID != null && publicID.length() > 0 ) {
            attributes.addAttribute( uri, "publicID", "publicID", type, publicID );
        }
        String systemID = e.getSystemId();
        if ( systemID != null && systemID.length() > 0 ) {
            attributes.addAttribute( uri, "systemID", "systemID", type, systemID );
        }

        output.startElement( uri, name, name, attributes );
        output.write( e.getMessage() );
        output.endElement( uri, name, name );
    }


}
