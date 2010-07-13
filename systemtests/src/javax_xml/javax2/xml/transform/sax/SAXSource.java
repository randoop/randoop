/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)SAXSource.java	1.13 03/01/23
 */
package javax2.xml.transform.sax;

import javax2.xml.transform.Source;
import javax2.xml.transform.stream.StreamSource;

import java.lang.String;

import java.io.OutputStream;
import java.io.Writer;

import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.InputSource;


/**
 * Acts as an holder for SAX-style Source.
 */
public class SAXSource implements Source {

    /**
     * If {@link javax2.xml.transform.TransformerFactory#getFeature}
     * returns true when passed this value as an argument,
     * the Transformer supports Source input of this type.
     */
    public static final String FEATURE =
        "http://javax2.xml.transform.sax.SAXSource/feature";

    /**
     * Zero-argument default constructor.  If this constructor
     * is used, and no other method is called, the
     * {@link javax2.xml.transform.Transformer}
     * assumes an empty input tree, with a default root node.
     */
    public SAXSource() {}

    /**
     * Create a <code>SAXSource</code>, using an {@link org.xml.sax.XMLReader}
     * and a SAX InputSource. The {@link javax2.xml.transform.Transformer}
     * or {@link javax2.xml.transform.sax.SAXTransformerFactory} will set itself
     * to be the reader's {@link org.xml.sax.ContentHandler}, and then will call
     * reader.parse(inputSource).
     *
     * @param reader An XMLReader to be used for the parse.
     * @param inputSource A SAX input source reference that must be non-null
     * and that will be passed to the reader parse method.
     */
    public SAXSource(XMLReader reader, InputSource inputSource) {
        this.reader      = reader;
        this.inputSource = inputSource;
    }

    /**
     * Create a <code>SAXSource</code>, using a SAX <code>InputSource</code>.
     * The {@link javax2.xml.transform.Transformer} or
     * {@link javax2.xml.transform.sax.SAXTransformerFactory} creates a
     * reader via {@link org.xml.sax.helpers.XMLReaderFactory}
     * (if setXMLReader is not used), sets itself as
     * the reader's {@link org.xml.sax.ContentHandler}, and calls
     * reader.parse(inputSource).
     *
     * @param inputSource An input source reference that must be non-null
     * and that will be passed to the parse method of the reader.
     */
    public SAXSource(InputSource inputSource) {
        this.inputSource = inputSource;
    }

    /**
     * Set the XMLReader to be used for the Source.
     *
     * @param reader A valid XMLReader or XMLFilter reference.
     */
    public void setXMLReader(XMLReader reader) {
        this.reader = reader;
    }

    /**
     * Get the XMLReader to be used for the Source.
     *
     * @return A valid XMLReader or XMLFilter reference, or null.
     */
    public XMLReader getXMLReader() {
        return reader;
    }

    /**
     * Set the SAX InputSource to be used for the Source.
     *
     * @param inputSource A valid InputSource reference.
     */
    public void setInputSource(InputSource inputSource) {
        this.inputSource = inputSource;
    }

    /**
     * Get the SAX InputSource to be used for the Source.
     *
     * @return A valid InputSource reference, or null.
     */
    public InputSource getInputSource() {
        return inputSource;
    }

    /**
     * Set the system identifier for this Source.  If an input source
     * has already been set, it will set the system ID or that
     * input source, otherwise it will create a new input source.
     *
     * <p>The system identifier is optional if there is a byte stream
     * or a character stream, but it is still useful to provide one,
     * since the application can use it to resolve relative URIs
     * and can include it in error messages and warnings (the parser
     * will attempt to open a connection to the URI only if
     * no byte stream or character stream is specified).</p>
     *
     * @param systemId The system identifier as a URI string.
     */
    public void setSystemId(String systemId) {

        if (null == inputSource) {
            inputSource = new InputSource(systemId);
        } else {
            inputSource.setSystemId(systemId);
        }
    }

    /**
     * Get the base ID (URI or system ID) from where URIs
     * will be resolved.
     *
     * @return Base URL for the Source, or null.
     */
    public String getSystemId() {

        return (null != inputSource)
               ? inputSource.getSystemId()
               : null;
    }

    /** The XMLReader to be used for the source tree input. May be null.        */
    private XMLReader reader;

    /** The SAX InputSource to be used for the source tree input.  Should not be null. */
    private InputSource inputSource;

    /**
     * Attempt to obtain a SAX InputSource object from a TrAX Source
     * object.
     *
     * @param source Must be a non-null Source reference.
     *
     * @return An InputSource, or null if Source can not be converted.
     */
    public static InputSource sourceToInputSource(Source source) {

        if (source instanceof SAXSource) {
            return ((SAXSource) source).getInputSource();
        } else if (source instanceof StreamSource) {
            StreamSource ss      = (StreamSource) source;
            InputSource  isource = new InputSource(ss.getSystemId());

            isource.setByteStream(ss.getInputStream());
            isource.setCharacterStream(ss.getReader());
            isource.setPublicId(ss.getPublicId());

            return isource;
        } else {
            return null;
        }
    }
}

