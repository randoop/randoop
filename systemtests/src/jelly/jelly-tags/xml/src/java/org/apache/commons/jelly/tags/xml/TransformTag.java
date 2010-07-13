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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.ScriptBlock;
import org.apache.commons.jelly.impl.StaticTagScript;
import org.apache.commons.jelly.impl.TagScript;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/** A tag which parses some XML, applies an xslt transform to it
  * and defines a variable with the transformed Document.
  * The XML can either be specified as its body or can be passed in via the
  * xml property which can be a Reader, InputStream, URL or String URI.
  *
  * The XSL can be passed in via the
  * xslt property which can be a Reader, InputStream, URL or String URI.
  *
  * @author Robert Leftwich
  * @version $Revision: 1.8 $
  */
public class TransformTag extends ParseTag {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(TransformTag.class);

    /** Propert name for lexical handler */
    private static final String LEXICAL_HANDLER_PROPERTY =
        "http://xml.org/sax/properties/lexical-handler";

    /** The xslt to parse, either a String URI, a Reader or InputStream */
    private Object xslt;

    /** The xsl transformer factory */
    private SAXTransformerFactory tf;

    /** the transformer handler, doing the real work */
    private TransformerHandler transformerHandler;

    /**
     * Constructor for TransformTag.
     */
    public TransformTag() {
        super();
        this.tf = (SAXTransformerFactory) TransformerFactory.newInstance();
    }

    // Tag interface
    //-------------------------------------------------------------------------

    /**
     * Process this tag instance
     *
     * @param output The pipeline for xml events
     * @throws Exception - when required attributes are missing
     */
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {

        if (null == this.getXslt()) {
            throw new MissingAttributeException("The xslt attribute cannot be null");
        }

        // set a resolver to locate uri
        this.tf.setURIResolver(createURIResolver());

        try {
            this.transformerHandler =
                this.tf.newTransformerHandler(this.getObjAsSAXSource(this.getXslt()));
        }
        catch (TransformerConfigurationException e) {
            throw new JellyTagException(e);
        }

        // run any nested param tags
        this.doNestedParamTag(output);

        try {
            // get a reader to provide SAX events to transformer
            XMLReader xmlReader = this.createXMLReader();
            xmlReader.setContentHandler(this.transformerHandler);
            xmlReader.setProperty(LEXICAL_HANDLER_PROPERTY, this.transformerHandler);

            // handle result differently, depending on if var is specified
            String varName = this.getVar();
            if (null == varName) {
                // pass the result of the transform out as SAX events
                this.transformerHandler.setResult(this.createSAXResult(output));
                xmlReader.parse(this.getXMLInputSource());
            }
            else {
                // pass the result of the transform out as a document
                DocumentResult result = new DocumentResult();
                this.transformerHandler.setResult(result);
                xmlReader.parse(this.getXMLInputSource());

                // output the result as a variable
                Document transformedDoc = result.getDocument();
                this.context.setVariable(varName, transformedDoc);
            }
        }
        catch (SAXException e) {
            throw new JellyTagException(e);
        }
        catch (IOException e) {
            throw new JellyTagException(e);
        }

    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Gets the source of the XSL which is either a String URI, Reader or
     * InputStream
     *
     * @returns xslt    The source of the xslt
     */
    public Object getXslt() {
        return this.xslt;
    }

    /**
     * Sets the source of the XSL which is either a String URI, Reader or
     * InputStream
     *
     * @param xslt    The source of the xslt
     */
    public void setXslt(Object xslt) {
        this.xslt = xslt;
    }

    public void setParameterValue(String name, Object value) {
        this.transformerHandler.getTransformer().setParameter(name, value);
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Creates a new URI Resolver so that URIs inside the XSLT document can be
     * resolved using the JellyContext
     *
     * @return a URI Resolver for the JellyContext
     */
    protected URIResolver createURIResolver() {
        return new URIResolver() {
            public Source resolve(String href, String base)
                throws TransformerException {

                if (log.isDebugEnabled() ) {
                    log.debug( "base: " + base + " href: " + href );
                }

                // pass if we don't have a systemId
                if (null == href)
                    return null;

                // @todo
                // #### this is a pretty simplistic implementation.
                // #### we should really handle this better such that if
                // #### base is specified as an absolute URL
                // #### we trim the end off it and append href
                return new StreamSource(context.getResourceAsStream(href));
            }
        };
    }

    /**
     * Factory method to create a new SAXResult for the given
     * XMLOutput so that the output of an XSLT transform will go
     * directly into the XMLOutput that we are given.
     *
     * @param output The destination of the transform output
     * @return A SAXResult for the transfrom output
     */
    protected Result createSAXResult(XMLOutput output) {
        SAXResult result = new SAXResult(output);
        result.setLexicalHandler(output);
        return result;
    }

    /**
     * Factory method to create a new XMLReader for this tag
     * so that the input of the XSLT transform comes from
     * either the xml var, the nested tag or the tag body.
     *
     * @return XMLReader for the transform input
     * @throws SAXException
     *             If the value of the "org.xml.sax.driver" system property
     *             is null, or if the class cannot be loaded and instantiated.
     */
    protected XMLReader createXMLReader() throws SAXException {
        XMLReader xmlReader = null;
        Object xmlReaderSourceObj = this.getXml();
        // if no xml source specified then get from body
        // otherwise convert it to a SAX source
        if (null == xmlReaderSourceObj) {
            xmlReader = new TagBodyXMLReader(this);
        }
        else {
            xmlReader = XMLReaderFactory.createXMLReader();
        }

        return xmlReader;
    }

    /**
     * Helper method to get the appropriate xml input source
     * so that the input of the XSLT transform comes from
     * either the xml var, the nested tag or the tag body.
     *
     * @return InputSource for the transform input
     */
    protected InputSource getXMLInputSource() {
        InputSource xmlInputSource = null;
        Object xmlInputSourceObj = this.getXml();
        // if no xml source specified then get from tag body
        // otherwise convert it to an input source
        if (null == xmlInputSourceObj) {
            xmlInputSource = new TagBodyInputSource();
        } else {
            xmlInputSource = this.getInputSourceFromObj(xmlInputSourceObj);
        }
        return xmlInputSource;
    }

    /**
     * Helper method to convert the specified object to a SAX source
     *
     * @return SAXSource from the source object or null
     */
    protected SAXSource getObjAsSAXSource(Object saxSourceObj) {
        SAXSource saxSource = null;
        if (null != saxSourceObj) {
            if (saxSourceObj instanceof Document) {
                saxSource =  new DocumentSource((Document) saxSourceObj);
            } else {
                InputSource xmlInputSource =
                    this.getInputSourceFromObj(saxSourceObj);
                saxSource = new SAXSource(xmlInputSource);
            }
        }

        return saxSource;
    }

    /**
     * Helper method to get an xml input source for the supplied object
     *
     * @return InputSource for the object or null
     */
    protected InputSource getInputSourceFromObj(Object sourceObj ) {
        InputSource xmlInputSource = null;
        if (sourceObj instanceof Document) {
            SAXSource saxSource = new DocumentSource((Document) sourceObj);
            xmlInputSource = saxSource.getInputSource();
        } else {
            if (sourceObj instanceof String) {
                String uri = (String) sourceObj;
                xmlInputSource = new InputSource(context.getResourceAsStream(uri));
            }
            else if (sourceObj instanceof Reader) {
                xmlInputSource = new InputSource((Reader) sourceObj);
            }
            else if (sourceObj instanceof InputStream) {
                xmlInputSource = new InputSource((InputStream) sourceObj);
            }
            else if (sourceObj instanceof URL) {
                String uri = ((URL) sourceObj).toString();
                xmlInputSource = new InputSource(context.getResourceAsStream(uri));
            }
            else if (sourceObj instanceof File) {
                try {
                    String uri = ((File) sourceObj).toURL().toString();
                    xmlInputSource = new InputSource(context.getResourceAsStream(uri));
                }
                catch (MalformedURLException e) {
                    throw new IllegalArgumentException(
                        "This should never occur. We should always be able to convert a File to a URL" + e );
                }
            }
            else {
                throw new IllegalArgumentException(
                    "Invalid source argument. Must be a String, Reader, InputStream or URL."
                        + " Was type; "
                        + sourceObj.getClass().getName()
                        + " with value: "
                        + sourceObj);
            }
        }

        return xmlInputSource;
    }

    /**
     * Helper method to run any nested param tags
     *
    * @param output The destination for any SAX output (not actually used)
     */
    private void doNestedParamTag(XMLOutput output) throws JellyTagException {
        // find any nested param tags and run them
        Script bodyScript = this.getBody();
        
        if (bodyScript instanceof ScriptBlock) {
            ScriptBlock scriptBlock = (ScriptBlock) bodyScript;
            List scriptList = scriptBlock.getScriptList();
            for (Iterator iter = scriptList.iterator(); iter.hasNext(); ) {
                Script script = (Script) iter.next();
                if (script instanceof TagScript) {

                    Tag tag = null;
                    try {
                        tag = ((TagScript) script).getTag(getContext());
                    } catch (JellyException e) {
                        throw new JellyTagException(e);
                    }

                    if (tag instanceof ParamTag) {
                        script.run(context, output);
                    }


                }
            }
        }
    }

    
    /** A helper class that converts a transform tag body to an XMLReader
      * to hide the details of where the input for the transform is obtained
      *
      * @author <a href="mailto:robert@leftwich.info">Robert Leftwich</a>
      * @version $Revision: 1.8 $
      */
    private class TagBodyXMLReader implements XMLReader {

        /** The tag whose body is to be read. */
        private Tag tag;

        /** The destination for the sax events generated by the reader. */
        private XMLOutput xmlOutput;

        /** Storage for a DTDHandler if set by the user of the reader. */
        private DTDHandler dtdHandler;

        /** Storage for a ErrorHandler if set by the user of the reader. */
        private ErrorHandler errorHandler;

        /** Storage for a EntityResolver if set by the user of the reader. */
        private EntityResolver entityResolver;

        /**
         * Construct an XMLReader for the specified Tag
         *
         * @param tag    The Tag to convert to an XMLReader
         */
        public TagBodyXMLReader(Tag tag)
        {
            this.tag = tag;
            this.xmlOutput = new XMLOutput();
        }

        // Methods
        //-------------------------------------------------------------------------

        /**
         * Parse an XML source.
         *
         * @param input  The source of the xml
         * @throws SAXException -
         *             Any SAX exception, possibly wrapping another exception.
         * @throws IOException -
         *             An IO exception from the parser, possibly from a byte
                       stream or character stream supplied by the application.
         */
        public void parse(InputSource input)
        throws IOException, SAXException
        {
            // safety check that we are being used correctly
            if (input instanceof TagBodyInputSource) {
                this.doInvokeBody();
            } else {
                throw new SAXException("Invalid input source");
            }
        }

        /**
         * Parse an XML source specified by a system id
         *
         * @param input  The system identifier (URI)
         * @throws SAXException -
         *             Any SAX exception, possibly wrapping another exception.
         * @throws IOException -
         *             An IO exception from the parser, possibly from a byte
                       stream or character stream supplied by the application.
         */
        public void parse(String systemId)
        throws IOException, SAXException
        {
            this.doInvokeBody();
        }

        // Helper methods
        //-------------------------------------------------------------------------

        /**
         * Actually invoke the tag body to generate the SAX events
         *
         * @throws SAXException -
         *             Any SAX exception, possibly wrapping another exception.
         */
        private void doInvokeBody() throws SAXException {
            try {
                if (this.shouldParseBody()) {
                    XMLReader anXMLReader = XMLReaderFactory.createXMLReader();
                    anXMLReader.setContentHandler(this.xmlOutput);
                    anXMLReader.setProperty(LEXICAL_HANDLER_PROPERTY,this.xmlOutput);
                    StringWriter writer = new StringWriter();
                    this.tag.invokeBody(XMLOutput.createXMLOutput(writer));
                    Reader reader = new StringReader(writer.toString());
                    anXMLReader.parse(new InputSource(reader));
                } else {
                    this.tag.invokeBody(this.xmlOutput);
                }
            } catch (Exception ex) {
                throw new SAXException(ex);
            }
        }

        /**
         * Helper method to determin if nested body needs to be parsed by (an
         * xml parser, i.e. its only text) to generate SAX events or not
         *
         * @return True if tag body should be parsed or false if invoked only
         * @throws JellyTagException
         */
        private boolean shouldParseBody() throws JellyTagException {
            boolean result = false;
            // check to see if we need to parse the body or just invoke it
            Script bodyScript = this.tag.getBody();
            
            if (bodyScript instanceof ScriptBlock) {
                ScriptBlock scriptBlock = (ScriptBlock) bodyScript;
                List scriptList = scriptBlock.getScriptList();
                for (Iterator iter = scriptList.iterator(); iter.hasNext(); ) {
                    Script script = (Script) iter.next();
                    if (script instanceof StaticTagScript) {
                        result = true;
                         break;
                    }
                }
            }
            return result;
        }

        // Properties
        //-------------------------------------------------------------------------

        /**
         * Gets the SAX ContentHandler to feed SAX events into
         *
         * @return the SAX ContentHandler to use to feed SAX events into
         */
        public ContentHandler getContentHandler() {
            return this.xmlOutput.getContentHandler();
        }

        /**
         * Sets the SAX ContentHandler to feed SAX events into
         *
         * @param contentHandler is the ContentHandler to use.
         *      This value cannot be null.
         */
        public void setContentHandler(ContentHandler contentHandler) {
            this.xmlOutput.setContentHandler(contentHandler);
            // often classes will implement LexicalHandler as well
            if (contentHandler instanceof LexicalHandler) {
                this.xmlOutput.setLexicalHandler((LexicalHandler) contentHandler);
            }
        }

        /**
         * Gets the DTD Handler to feed SAX events into
         *
         * @return the DTD Handler to use to feed SAX events into
         */
        public DTDHandler getDTDHandler() {
            return this.dtdHandler;
        }

        /**
         * Sets the DTD Handler to feed SAX events into
         *
         * @param the DTD Handler to use to feed SAX events into
         */
        public void setDTDHandler(DTDHandler dtdHandler) {
            this.dtdHandler = dtdHandler;
        }

        /**
         * Gets the Error Handler to feed SAX events into
         *
         * @return the Error Handler to use to feed SAX events into
         */
        public ErrorHandler getErrorHandler() {
            return this.errorHandler;
        }

        /**
         * Sets the Error Handler to feed SAX events into
         *
         * @param the Error Handler to use to feed SAX events into
         */
        public void setErrorHandler(ErrorHandler errorHandler) {
            // save the error handler
            this.errorHandler = errorHandler;
        }

        /**
         * Gets the Entity Resolver to feed SAX events into
         *
         * @return the Entity Resolver to use to feed SAX events into
         */
        public EntityResolver getEntityResolver() {
            return this.entityResolver;
        }

        /**
         * Sets the Entity Resolver to feed SAX events into
         *
         * @param the Entity Resolver to use to feed SAX events into
         */
        public void setEntityResolver(EntityResolver entityResolver) {
            this.entityResolver = entityResolver;
        }

        /**
         * Lookup the value of a property
         *
         * @param name - The property name, which is a fully-qualified URI.
         * @return - The current value of the property.
         * @throws SAXNotRecognizedException -
         *            When the XMLReader does not recognize the property name.
         * @throws SAXNotSupportedException -
         *            When the XMLReader recognizes the property name but
         *            cannot determine its value at this time.
         */
        public Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
        {
            // respond to the lexical handler request
            if (name.equalsIgnoreCase(LEXICAL_HANDLER_PROPERTY)) {
                return this.xmlOutput.getLexicalHandler();
            } else {
                // do nothing
                return null;
            }
        }

        /**
         * Set the value of a property
         *
         * @param name - The property name, which is a fully-qualified URI.
         * @param value - The property value
         * @throws SAXNotRecognizedException -
         *            When the XMLReader does not recognize the property name.
         * @throws SAXNotSupportedException -
         *            When the XMLReader recognizes the property name but
         *            cannot determine its value at this time.
         */
        public void setProperty(String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException
        {
            // respond to the lexical handler setting
            if (name.equalsIgnoreCase(LEXICAL_HANDLER_PROPERTY)) {
                this.xmlOutput.setLexicalHandler((LexicalHandler) value);
            }
        }

        /**
         * Lookup the value of a feature
         *
         * @param name - The feature name, which is a fully-qualified URI.
         * @return - The current state of the feature (true or false)
         * @throws SAXNotRecognizedException -
         *            When the XMLReader does not recognize the feature name.
         * @throws SAXNotSupportedException -
         *            When the XMLReader recognizes the feature name but
         *            cannot determine its value at this time.
         */
        public boolean getFeature(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
        {
            // do nothing
            return false;
        }

        /**
         * Set the value of a feature
         *
         * @param name - The feature name, which is a fully-qualified URI.
         * @param value - The current state of the feature (true or false)
         * @throws SAXNotRecognizedException -
         *            When the XMLReader does not recognize the feature name.
         * @throws SAXNotSupportedException -
         *            When the XMLReader recognizes the feature name but
         *            cannot determine its value at this time.
         */
        public void setFeature(String name, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException
        {
            // do nothing
        }
    }

    /** A marker class used by the TagBodyXMLReader as a sanity check
      * (i.e. The source is not actually used)
      *
      */
    private class TagBodyInputSource extends InputSource {

        /**
         * Construct an instance of this marker class
         */
        public TagBodyInputSource() {
        }
    }

}
