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
package org.apache.commons.jelly.tags.core;

import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.parser.XMLParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Parses the output of this tags body or of a given String as a Jelly script
 * then either outputting the Script as a variable or executing the script.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class ParseTag extends TagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(ParseTag.class);

    /** The variable that will be generated for the document */
    private String var;

    /** The markup text to be parsed */
    private String text;

    /** The XMLReader used to parser the document */
    private XMLReader xmlReader;

    /** The Jelly parser */
    private XMLParser jellyParser;

    public ParseTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
     */
    public void doTag(XMLOutput output)
        throws MissingAttributeException, JellyTagException {

        String text = getText();
        if (text != null) {
            parseText(text);
        }
        else {
            parseBody(output);
        }

        Script script = getJellyParser().getScript();
        if (var != null) {
            context.setVariable(var, script);
        }
        else {
            // invoke the script
            script.run(context, output);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    /** The variable name that will be used for the Document variable created
     */
    public String getVar() {
        return var;
    }

    /** Sets the variable name that will be used for the Document variable created
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Returns the text to be parsed
     * @return String
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text to be parsed by this parser
     * @param text The text to be parsed by this parser
     */
    public void setText(String text) {
        this.text = text;
    }


    /** @return the XMLReader used for parsing, creating one lazily if need be  */
    public XMLReader getXMLReader() throws ParserConfigurationException, SAXException {
        if (xmlReader == null) {
            xmlReader = createXMLReader();
        }
        return xmlReader;
    }

    /** Sets the XMLReader used for parsing */
    public void setXMLReader(XMLReader xmlReader) {
        this.xmlReader = xmlReader;
    }


    /**
     * @return XMLParser
     */
    public XMLParser getJellyParser() {
        if (jellyParser == null) {
            jellyParser = createJellyParser();
        }
        return jellyParser;
    }

    /**
     * Sets the jellyParser.
     * @param jellyParser The jellyParser to set
     */
    public void setJellyParser(XMLParser jellyParser) {
        this.jellyParser = jellyParser;
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new XMLReader
     */
    protected XMLReader createXMLReader() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        return parser.getXMLReader();
    }


    /**
     * Parses the body of this tag and returns the parsed document
     */
    protected void parseBody(XMLOutput output) throws JellyTagException {
        ContentHandler handler = getJellyParser();
        XMLOutput newOutput = new XMLOutput(handler);

        try {
            handler.startDocument();
            invokeBody(newOutput);
            handler.endDocument();
        }
        catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }

    /**
     * Parses the give piece of text as being markup
     */
    protected void parseText(String text) throws JellyTagException {
        if ( log.isDebugEnabled() ) {
            log.debug( "About to parse: " + text );
        }

        try {
            getXMLReader().parse( new InputSource( new StringReader( text ) ) );
        }
        catch (Exception e) {
            throw new JellyTagException(e);
        }
    }

    /**
     * Factory method to create a new Jelly parser
     * @return XMLParser
     */
    protected XMLParser createJellyParser() {
        XMLParser answer = new XMLParser();
        answer.setContext(context);
        return answer;
    }
}
