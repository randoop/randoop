/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)TransformerHandler.java	1.11 03/01/23
 */
package javax2.xml.transform.sax;

import java.util.Properties;

import javax2.xml.transform.Result;
import javax2.xml.transform.URIResolver;
import javax2.xml.transform.TransformerException;
import javax2.xml.transform.Transformer;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.DTDHandler;


/**
 * A TransformerHandler
 * listens for SAX ContentHandler parse events and transforms
 * them to a Result.
 */
public interface TransformerHandler
    extends ContentHandler, LexicalHandler, DTDHandler {

    /**
     * Enables the user of the TransformerHandler to set the
     * to set the Result for the transformation.
     *
     * @param result A Result instance, should not be null.
     *
     * @throws IllegalArgumentException if result is invalid for some reason.
     */
    public void setResult(Result result) throws IllegalArgumentException;

    /**
     * Set the base ID (URI or system ID) from where relative
     * URLs will be resolved.
     * @param systemID Base URI for the source tree.
     */
    public void setSystemId(String systemID);

    /**
     * Get the base ID (URI or system ID) from where relative
     * URLs will be resolved.
     * @return The systemID that was set with {@link #setSystemId}.
     */
    public String getSystemId();

    /**
     * Get the Transformer associated with this handler, which
     * is needed in order to set parameters and output properties.
     */
    public Transformer getTransformer();
}
